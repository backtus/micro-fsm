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
 * - `V<n>__<description>.sql` files, applied in numeric order, recorded in `schema_migration` *after* they ran;
 *   a changed checksum of an applied file fails the run. A crash between DDL and history insert needs manual repair.
 */
class Migrator(
    private val ds: DataSource,
    private val placeholders: Map<String, String> = mapOf("index_async" to ""),
    private val location: String = "db/migration",
) {
    private val log = Logger.getLogger(Migrator::class.java)

    fun migrate(): Int {
        val migrations = load()
        ds.connection.use { c ->
            c.autoCommit = true
            exec(c, "CREATE TABLE IF NOT EXISTS schema_migration (version bigint PRIMARY KEY, description varchar(200) NOT NULL, checksum varchar(64) NOT NULL, applied_at timestamptz NOT NULL)")
            val applied = mutableMapOf<Long, String>()
            c.createStatement().use { st ->
                st.executeQuery("SELECT version, checksum FROM schema_migration").use { while (it.next()) applied[it.getLong(1)] = it.getString(2) }
            }
            var count = 0
            for (m in migrations) {
                val done = applied[m.version]
                if (done != null) {
                    check(done == m.checksum) { "Checksum mismatch for applied migration V${m.version} (${m.name})" }
                    continue
                }
                log.infof("Applying migration V%d %s", m.version, m.description)
                exec(c, expand(m.sql)) // own transaction (auto-commit)
                c.prepareStatement("INSERT INTO schema_migration (version, description, checksum, applied_at) VALUES (?, ?, ?, now())").use {
                    it.setLong(1, m.version); it.setString(2, m.description.take(200)); it.setString(3, m.checksum)
                    it.executeUpdate() // separate transaction from the DDL above
                }
                count++
            }
            return count
        }
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
            val pattern = Regex("V(\\d+)__(.+)\\.sql")
            return list.mapNotNull { p ->
                val m = pattern.matchEntire(p.fileName.toString()) ?: return@mapNotNull null
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
