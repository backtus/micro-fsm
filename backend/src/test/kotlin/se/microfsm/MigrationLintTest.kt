package se.microfsm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.readText

/** Aurora DSQL rules: one DDL per migration, no extensions/triggers/json, async indexes only. */
class MigrationLintTest {
    private val dir = Path.of("src/main/resources/db/migration")

    private fun files() = Files.list(dir).use { s -> s.filter { it.extension == "sql" }.toList() }

    private fun violations(name: String, sql: String): List<String> {
        val code = sql.lines().joinToString("\n") { it.substringBefore("--") }
        val v = mutableListOf<String>()
        val statements = code.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (statements.size != 1) v += "$name: ${statements.size} statements (exactly 1 allowed)"
        if (Regex("create\\s+extension", RegexOption.IGNORE_CASE).containsMatchIn(code)) v += "$name: CREATE EXTENSION"
        if (Regex("\\btrigger\\b", RegexOption.IGNORE_CASE).containsMatchIn(code)) v += "$name: TRIGGER"
        if (Regex("\\bjsonb?\\b", RegexOption.IGNORE_CASE).containsMatchIn(code)) v += "$name: json/jsonb"
        if (Regex("\\breferences\\b|foreign\\s+key", RegexOption.IGNORE_CASE).containsMatchIn(code)) v += "$name: foreign key / REFERENCES"
        if (Regex("\\b(insert\\s+into|update\\s+\\w+\\s+set|delete\\s+from)\\b", RegexOption.IGNORE_CASE).containsMatchIn(code)) v += "$name: DML in a migration (keep DDL and DML apart)"
        if (Regex("create\\s+(unique\\s+)?index", RegexOption.IGNORE_CASE).containsMatchIn(code) &&
            !Regex("index\\s+\\$\\{index_async}", RegexOption.IGNORE_CASE).containsMatchIn(code)
        ) {
            v += "$name: CREATE INDEX without \${index_async}"
        }
        return v
    }

    @Test
    fun `existing migrations comply`() {
        val all = files()
        assertThat(all).isNotEmpty()
        assertThat(all.flatMap { violations(it.name, it.readText()) }).isEmpty()
    }

    @Test
    fun `lint detects violations`() {
        assertThat(violations("a", "CREATE TABLE a (id int); CREATE TABLE b (id int);")).isNotEmpty()
        assertThat(violations("a", "CREATE EXTENSION pgcrypto;")).isNotEmpty()
        assertThat(violations("a", "CREATE TRIGGER t BEFORE INSERT ON a EXECUTE FUNCTION f();")).isNotEmpty()
        assertThat(violations("a", "CREATE TABLE a (d jsonb);")).isNotEmpty()
        assertThat(violations("a", "CREATE TABLE a (id int, b int REFERENCES c (id));")).isNotEmpty()
        assertThat(violations("a", "ALTER TABLE a ADD FOREIGN KEY (b) REFERENCES c (id);")).isNotEmpty()
        assertThat(violations("a", "ALTER TABLE a ADD CONSTRAINT fk FOREIGN KEY (b) REFERENCES c (id);")).isNotEmpty()
        assertThat(violations("a", "INSERT INTO a (id) VALUES (1);")).isNotEmpty()
        assertThat(violations("a", "UPDATE a SET b = 1;")).isNotEmpty()
        assertThat(violations("a", "CREATE INDEX i ON a (b);")).isNotEmpty()
        assertThat(violations("a", "CREATE INDEX \${index_async} i ON a (b);")).isEmpty()
        assertThat(violations("a", "-- one; two\nCREATE TABLE a (id int);")).isEmpty()
    }
}
