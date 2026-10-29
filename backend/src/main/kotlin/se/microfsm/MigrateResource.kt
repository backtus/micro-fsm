package se.microfsm

import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * One-off migration trigger for deployed environments (SnapStart-safe: never runs at init). Only the dedicated
 * migrate Lambda sets `APP_MIGRATE_ENDPOINT_ENABLED=true`; it has no HTTP API in front and is invoked directly with a
 * synthetic API Gateway v2 event (see AGENTS.md). Everywhere else the endpoint answers 404.
 */
@Path("/internal/migrate")
class MigrateResource @Inject constructor(
    private val migrate: MigrateAtStart,
    @ConfigProperty(name = "app.migrate.endpoint-enabled", defaultValue = "false") private val enabled: Boolean,
) {
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    fun run(): String {
        if (!enabled) throw NotFoundException()
        return "Applied ${migrate.migrator().migrate()} migration(s)"
    }
}
