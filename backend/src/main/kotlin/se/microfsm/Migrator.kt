package se.microfsm

import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Optional
import javax.sql.DataSource

class Migration(val version: Long, val description: String, val name: String, val sql: String) {
    val checksum: String = MessageDigest.getInstance("SHA-256").digest(sql.toByteArray()).joinToString("") { "%02x".format(it) }
}

/**
 * Minimal schema migrator for Aurora DSQL (and local PostgreSQL), replacing Flyway:
 * - no locks of any kind (DSQL has no advisory locks); the caller guarantees a single writer
 *   (migrations run as a one-off invoke);
 * - every statement runs on an auto-commit connection, i.e. in its own transaction, so DDL and DML never share one;
 * - one statement per migration file (enforced by MigrationLintTest);
 * - strict file names `V<n>__<description>.sql`; anything else (or no migrations at all) is an error;
 * - the whole history is validated BEFORE any SQL runs: every applied version must still exist with the same
 *   checksum, no row may be unfinished, and no pending version may be lower than the highest applied one;
 * - per migration: insert history row `STARTED`, run the statement, mark the row `DONE`. If the statement fails
 *   the row is removed again. If the process dies (or the final update fails) the `STARTED` row stays and the next
 *   run refuses to continue until someone repairs it (see AGENTS.md, "Repairing a failed migration").
 */
class Migrator(
    private val ds: DataSource,
    private val placeholders: Map<String, String> = mapOf("index_async" to ""),
    private val location: String = "db/migration",
) {
    private val log = Logger.getLogger(Migrator::class.java)

    companion object {
        val NAME = Regex("V(\\d+)__([A-Za-z0-9_]+)\\.sql")
    }

    fun migrate(): Int {
        val migrations = load()
        ds.connection.use { c ->
            c.autoCommit = true
            exec(c, "CREATE TABLE IF NOT EXISTS schema_migration (version bigint PRIMARY KEY, description varchar(200) NOT NULL, checksum varchar(64) NOT NULL, state varchar(10) NOT NULL, applied_at timestamptz NOT NULL)")
            val pending = preflight(migrations, readHistory(c))
            for (m in pending) {
                log.infof("Applying migration V%d %s", m.version, m.description)
                c.prepareStatement("INSERT INTO schema_migration (version, description, checksum, state, applied_at) VALUES (?, ?, ?, 'STARTED', now())").use {
                    it.setLong(1, m.version); it.setString(2, m.description.take(200)); it.setString(3, m.checksum)
                    it.executeUpdate()
                }
                try {
                    exec(c, expand(m.sql)) // own transaction (auto-commit)
                } catch (e: Exception) {
                    try { deleteRow(c, m.version) } catch (cleanup: Exception) { e.addSuppressed(cleanup) }
                    throw e
                }
                c.prepareStatement("UPDATE schema_migration SET state = 'DONE' WHERE version = ?").use {
                    it.setLong(1, m.version)
                    it.executeUpdate()
                }
            }
            return pending.size
        }
    }

    private class Row(val checksum: String, val state: String)

    private fun readHistory(c: java.sql.Connection): Map<Long, Row> {
        val rows = mutableMapOf<Long, Row>()
        c.createStatement().use { st ->
            st.executeQuery("SELECT version, checksum, state FROM schema_migration").use { while (it.next()) rows[it.getLong(1)] = Row(it.getString(2), it.getString(3)) }
        }
        return rows
    }

    private fun deleteRow(c: java.sql.Connection, version: Long) {
        c.prepareStatement("DELETE FROM schema_migration WHERE version = ?").use { it.setLong(1, version); it.executeUpdate() }
    }

    /** Validates history against the files; returns the pending migrations in order. Runs no migration SQL. */
    private fun preflight(migrations: List<Migration>, history: Map<Long, Row>): List<Migration> {
        val byVersion = migrations.associateBy { it.version }
        history.entries.sortedBy { it.key }.forEach { (v, row) ->
            check(row.state == "DONE") {
                "Migration V$v is in state ${row.state}: a previous run did not finish. Repair required (see AGENTS.md, " +
                    "\"Repairing a failed migration\"); not applying anything."
            }
            val file = byVersion[v] ?: error("Applied migration V$v has no file in $location (missing or renamed)")
            check(file.checksum == row.checksum) { "Checksum mismatch for applied migration V$v (${file.name})" }
        }
        val pending = migrations.filter { it.version !in history }
        val maxApplied = history.keys.maxOrNull()
        if (maxApplied != null) {
            pending.firstOrNull { it.version < maxApplied }?.let {
                error("Out-of-order migration V${it.version}: V$maxApplied is already applied. Renumber it above V$maxApplied.")
            }
        }
        return pending
    }

    private fun exec(c: java.sql.Connection, sql: String) = c.createStatement().use { it.execute(sql) }

    private fun expand(sql: String) = placeholders.entries.fold(sql) { acc, (k, v) -> acc.replace("\${$k}", v) }

    fun load(): List<Migration> {
        val url = Thread.currentThread().contextClassLoader.getResource(location)
            ?: error("Migration location not found on classpath: $location")
        val uri = url.toURI()
        val fs = if (uri.scheme == "jar") FileSystems.newFileSystem(uri, emptyMap<String, Any>()) else null
        try {
            val dir: Path = if (fs != null) fs.getPath("/$location") else Path.of(uri)
            val list = Files.list(dir).use { s -> s.toList() }
            val sqlFiles = list.filter { it.fileName.toString().endsWith(".sql", ignoreCase = true) }
            sqlFiles.map { it.fileName.toString() }.filterNot { NAME.matches(it) }.let {
                require(it.isEmpty()) { "Malformed migration file name(s) in $location: $it (expected V<number>__<description>.sql)" }
            }
            require(sqlFiles.isNotEmpty()) { "No migrations found in $location" }
            return sqlFiles.map { p ->
                val m = NAME.matchEntire(p.fileName.toString())!!
                Migration(m.groupValues[1].toLong(), m.groupValues[2].replace('_', ' '), p.fileName.toString(), Files.readString(p))
            }.sortedBy { it.version }.also { all ->
                check(all.map { it.version }.toSet().size == all.size) { "Duplicate migration versions" }
            }
        } finally {
            fs?.close()
        }
    }
}

@ApplicationScoped
class MigrateAtStart @Inject constructor(
    private val ds: DataSource,
    @ConfigProperty(name = "app.migrate.at-start", defaultValue = "false") private val atStart: Boolean,
    @ConfigProperty(name = "app.migrate.index-async") private val indexAsync: Optional<String>,
) {
    fun migrator() = Migrator(ds, mapOf("index_async" to indexAsync.orElse("")))

    fun onStart(@Observes e: StartupEvent) {
        if (atStart) migrator().migrate()
    }
}
