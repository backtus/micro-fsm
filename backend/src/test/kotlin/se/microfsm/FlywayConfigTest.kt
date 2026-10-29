package se.microfsm

import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.database.postgresql.PostgreSQLConfigurationExtension
import org.junit.jupiter.api.Test
import java.util.Optional

class FlywayConfigTest {
    private fun lock(cfg: FluentConfiguration) =
        cfg.pluginRegister.getExact(PostgreSQLConfigurationExtension::class.java).isTransactionalLock

    @Test
    fun `postgres transactional lock is disabled`() {
        val cfg = FluentConfiguration()
        assertThat(lock(cfg)).isTrue() // Flyway default, proves the test is meaningful
        FlywayCustomizer(Optional.empty()).customize(cfg)
        assertThat(lock(cfg)).isFalse()
    }

    @Test
    fun `index_async placeholder defaults to empty and can be ASYNC`() {
        val plain = FluentConfiguration().also { FlywayCustomizer(Optional.empty()).customize(it) }
        assertThat(plain.placeholders["index_async"]).isEqualTo("")
        val dsql = FluentConfiguration().also { FlywayCustomizer(Optional.of("ASYNC")).customize(it) }
        assertThat(dsql.placeholders["index_async"]).isEqualTo("ASYNC")
    }
}
