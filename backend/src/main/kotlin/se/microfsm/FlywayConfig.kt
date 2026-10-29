package se.microfsm

import io.quarkus.flyway.FlywayConfigurationCustomizer
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.flywaydb.core.api.configuration.FluentConfiguration
import java.util.Optional

/**
 * Aurora DSQL has no advisory locks, so Flyway must not take the PostgreSQL transactional lock.
 * Also supplies the `${index_async}` placeholder: empty on PostgreSQL, `ASYNC` on DSQL
 * (`app.flyway.index-async`). Applied in every profile so that dev/test behave like DSQL.
 */
@Singleton
class FlywayCustomizer(
    @ConfigProperty(name = "app.flyway.index-async") private val indexAsync: Optional<String>,
) : FlywayConfigurationCustomizer {
    override fun customize(configuration: FluentConfiguration) {
        configuration.configuration(mapOf(TRANSACTIONAL_LOCK_KEY to "false"))
        configuration.placeholders(configuration.placeholders + (INDEX_ASYNC to indexAsync.orElse("")))
    }

    companion object {
        const val TRANSACTIONAL_LOCK_KEY = "flyway.postgresql.transactional.lock"
        const val INDEX_ASYNC = "index_async"
    }
}
