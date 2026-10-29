package se.microfsm

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.Statement
import java.util.UUID
import java.util.logging.Logger
import javax.sql.DataSource

@QuarkusTest
class MigratorTest {
    @Inject
    lateinit var ds: DataSource

    class Executed(val sql: String, val autoCommit: Boolean)

    /** DataSource in its own schema whose connections record every statement and any transaction control. */
    private class Recording(private val ds: DataSource, private val schema: String) : DataSource by ds {
        /** Statements whose SQL contains this text fail with an SQLException (failure injection). */
        var failOn: String? = null
        /** Statement.close() throws after a successful execute of SQL containing this text. */
        var failCloseAfter: String? = null
        val executed = mutableListOf<Executed>()
        val transactionControl = mutableListOf<String>()

        override fun getConnection(): Connection {
            val real = ds.connection
            real.createStatement().use { it.execute("SET search_path TO $schema") }
            return Proxy.newProxyInstance(javaClass.classLoader, arrayOf(Connection::class.java)) { _, m, args ->
                when (m.name) {
                    "setAutoCommit" -> if (args[0] == false) transactionControl += "setAutoCommit(false)"
                    "commit", "rollback", "setSavepoint" -> transactionControl += m.name
                }
                if (m.name == "prepareStatement") {
                    executed += Executed(args[0] as String, real.autoCommit)
                    failOn?.let { if ((args[0] as String).contains(it)) throw java.sql.SQLException("injected failure") }
                }
                val result = try { m.invoke(real, *(args ?: emptyArray())) } catch (e: InvocationTargetException) { throw e.targetException }
                if (m.name == "createStatement") {
                    var lastSql: String? = null
                    Proxy.newProxyInstance(javaClass.classLoader, arrayOf(Statement::class.java)) { _, sm, sargs ->
                        if (sm.name.startsWith("execute") && sargs?.firstOrNull() is String) lastSql = sargs[0] as String
                        if (sm.name == "close") {
                            val sql = lastSql
                            val f = failCloseAfter
                            if (f != null && sql != null && sql.contains(f)) {
                                (result as Statement).close()
                                throw java.sql.SQLException("injected close failure")
                            }
                        }
                        if (sm.name.startsWith("execute") && sargs?.firstOrNull() is String) {
                            executed += Executed(sargs[0] as String, real.autoCommit)
                            failOn?.let { if ((sargs[0] as String).contains(it)) throw java.sql.SQLException("injected failure") }
                        }
                        try { sm.invoke(result, *(sargs ?: emptyArray())) } catch (e: InvocationTargetException) { throw e.targetException }
                    }
                } else result
            } as Connection
        }

        override fun getLogWriter(): PrintWriter? = ds.logWriter
        override fun getParentLogger(): Logger = ds.parentLogger
    }

    private fun <T> inFreshSchema(block: (Recording, String) -> T): T {
        val schema = "t_" + UUID.randomUUID().toString().replace("-", "").take(12)
        ds.connection.use { it.createStatement().use { s -> s.execute("CREATE SCHEMA $schema") } }
        try {
            return block(Recording(ds, schema), schema)
        } finally {
            ds.connection.use { it.createStatement().use { s -> s.execute("DROP SCHEMA $schema CASCADE") } }
        }
    }

    private fun count(schema: String, sql: String): Int = ds.connection.use { c ->
        c.createStatement().use { s -> s.executeQuery(sql.replace("{schema}", schema)).use { it.next(); it.getInt(1) } }
    }

    @Test
    fun `applies all migrations, records them, and a rerun is a no-op`() {
        inFreshSchema { rec, schema ->
            assertThat(Migrator(rec).migrate()).isEqualTo(2)
            assertThat(count(schema, "select count(*) from {schema}.schema_migration")).isEqualTo(2)
            assertThat(count(schema, "select count(*) from pg_indexes where schemaname='{schema}' and indexname='job_tenant_date_idx'")).isEqualTo(1)
            assertThat(Migrator(rec).migrate()).isZero()
        }
    }

    @Test
    fun `issues no locks and never mixes statements in a transaction`() {
        inFreshSchema { rec, _ ->
            Migrator(rec).migrate()
            Migrator(rec).migrate()
            assertThat(rec.executed).isNotEmpty()
            assertThat(rec.executed.map { it.sql.lowercase() }).noneMatch { it.contains("advisory") || it.contains("lock ") || it.contains("for update") }
            // auto-commit on for every statement => each statement (DDL or DML) is its own transaction
            assertThat(rec.executed).allMatch { it.autoCommit }
            assertThat(rec.transactionControl).isEmpty()
            assertThat(rec.executed.map { it.sql }).anyMatch { it.startsWith("CREATE TABLE job") }
            assertThat(rec.executed.map { it.sql }).anyMatch { it.startsWith("INSERT INTO schema_migration") }
        }
    }

    @Test
    fun `changed checksum of an applied migration fails`() {
        inFreshSchema { rec, schema ->
            Migrator(rec).migrate()
            ds.connection.use { it.createStatement().use { s -> s.execute("UPDATE $schema.schema_migration SET checksum='x' WHERE version=1") } }
            assertThatThrownBy { Migrator(rec).migrate() }.hasMessageContaining("Checksum mismatch")
        }
    }

    @Test
    fun `expands placeholders`() {
        inFreshSchema { rec, schema ->
            Migrator(rec, mapOf("tbl" to "expanded_tbl"), "db/migrator-test").migrate()
            assertThat(count(schema, "select count(*) from pg_tables where schemaname='{schema}' and tablename='expanded_tbl'")).isEqualTo(1)
        }
    }

    @Test
    fun `default placeholder turns index_async into nothing and ASYNC is substituted verbatim`() {
        val sql = Migrator(ds).load().first { it.version == 2L }.sql
        assertThat(sql).contains("\${index_async}")
        val expanded = Migrator::class.java.getDeclaredMethod("expand", String::class.java).apply { isAccessible = true }
        assertThat(expanded.invoke(Migrator(ds, mapOf("index_async" to "ASYNC")), sql) as String).contains("CREATE INDEX ASYNC job_tenant_date_idx")
    }

    private fun mt(dir: String) = "db/mt/$dir"
    private fun tableExists(schema: String, t: String) =
        count(schema, "select count(*) from pg_tables where schemaname='{schema}' and tablename='$t'") == 1
    private fun historyRows(schema: String) = count(schema, "select count(*) from {schema}.schema_migration")

    @Test
    fun `bad checksum anywhere in history is detected before any pending migration runs`() {
        inFreshSchema { rec, schema ->
            Migrator(rec, location = mt("a")).migrate()
            assertThatThrownBy { Migrator(rec, location = mt("changed")).migrate() }.hasMessageContaining("Checksum mismatch for applied migration V2")
            assertThat(tableExists(schema, "t3")).isFalse() // V3 pending but nothing ran
        }
    }

    @Test
    fun `applied migration whose file is missing fails`() {
        inFreshSchema { rec, _ ->
            Migrator(rec, location = mt("a")).migrate()
            assertThatThrownBy { Migrator(rec, location = mt("missing")).migrate() }.hasMessageContaining("Applied migration V2 has no file")
        }
    }

    @Test
    fun `out-of-order pending migration is rejected without running anything`() {
        inFreshSchema { rec, schema ->
            Migrator(rec, location = mt("v2only")).migrate()
            assertThatThrownBy { Migrator(rec, location = mt("a")).migrate() }.hasMessageContaining("Out-of-order migration V1")
            assertThat(tableExists(schema, "t1")).isFalse()
            assertThat(historyRows(schema)).isEqualTo(1)
        }
    }

    @Test
    fun `malformed file name and empty location are rejected`() {
        inFreshSchema { rec, schema ->
            assertThatThrownBy { Migrator(rec, location = mt("badname")).migrate() }.hasMessageContaining("V3_add_table.sql")
            assertThat(tableExists(schema, "t1")).isFalse()
            assertThatThrownBy { Migrator(rec, location = mt("empty")).migrate() }.hasMessageContaining("No migrations found")
        }
    }

    @Test
    fun `failing statement keeps STARTED marker and stops later migrations`() {
        inFreshSchema { rec, schema ->
            assertThatThrownBy { Migrator(rec, location = mt("failing")).migrate() }.hasMessageContaining("nosuchtype")
            assertThat(tableExists(schema, "t1")).isTrue() // V1 completed before the failure
            assertThat(tableExists(schema, "t2")).isFalse()
            assertThat(tableExists(schema, "t3")).isFalse()
            assertThat(count(schema, "select count(*) from {schema}.schema_migration where version = 1 and state = 'DONE'")).isEqualTo(1)
            assertThat(count(schema, "select count(*) from {schema}.schema_migration where version = 2 and state = 'STARTED'")).isEqualTo(1)
            assertThat(count(schema, "select count(*) from {schema}.schema_migration where version >= 3")).isZero()
            assertThatThrownBy { Migrator(rec, location = mt("failing")).migrate() }.hasMessageContaining("Repair required")
        }
    }

    @Test
    fun `failure after the statement ran leaves DDL visible and blocks the next run until repaired`() {
        inFreshSchema { rec, schema ->
            rec.failOn = "SET state = 'DONE'"
            assertThatThrownBy { Migrator(rec, location = mt("a")).migrate() }.hasMessageContaining("injected failure")
            rec.failOn = null
            assertThat(tableExists(schema, "t1")).isTrue()
            assertThat(count(schema, "select count(*) from {schema}.schema_migration where version = 1 and state = 'STARTED'")).isEqualTo(1)
            assertThatThrownBy { Migrator(rec, location = mt("a")).migrate() }
                .hasMessageContaining("V1 is in state STARTED").hasMessageContaining("Repair required")
            assertThat(tableExists(schema, "t2")).isFalse()
            // repair as documented in AGENTS.md: statement is known to have applied -> mark DONE, then rerun
            ds.connection.use { it.createStatement().use { s -> s.execute("UPDATE $schema.schema_migration SET state='DONE' WHERE version=1") } }
            assertThat(Migrator(rec, location = mt("a")).migrate()).isEqualTo(1)
            assertThat(tableExists(schema, "t2")).isTrue()
        }
    }

    @Test
    fun `failure in statement cleanup after a committed DDL keeps STARTED and requires repair`() {
        inFreshSchema { rec, schema ->
            rec.failCloseAfter = "t1"
            assertThatThrownBy { Migrator(rec, location = mt("a")).migrate() }.hasMessageContaining("injected close failure")
            rec.failCloseAfter = null
            assertThat(tableExists(schema, "t1")).isTrue()
            assertThat(count(schema, "select count(*) from {schema}.schema_migration where version = 1 and state = 'STARTED'")).isEqualTo(1)
            assertThatThrownBy { Migrator(rec, location = mt("a")).migrate() }.hasMessageContaining("Repair required")
        }
    }
}
