package se.microfsm

import io.agroal.api.AgroalDataSource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Runs the application in the real `%dsql` profile (DSQL connector driver, application.properties settings).
 * Only the URL stays at its unreachable placeholder and pool metrics are switched on to observe connections.
 */
class DsqlProfile : QuarkusTestProfile {
    override fun getConfigProfile() = "dsql"
    override fun getConfigOverrides() = mapOf(
        "quarkus.datasource.devservices.enabled" to "false",
        "quarkus.datasource.jdbc.enable-metrics" to "true",
    )
}

@QuarkusTest
@TestProfile(DsqlProfile::class)
class DsqlProfileTest {
    @Inject
    lateinit var ds: AgroalDataSource

    @ConfigProperty(name = "quarkus.hibernate-orm.database.start-offline")
    lateinit var startOffline: String

    @ConfigProperty(name = "quarkus.hibernate-orm.database.version-check.enabled")
    lateinit var versionCheck: String

    @ConfigProperty(name = "app.migrate.index-async")
    lateinit var indexAsync: String

    @ConfigProperty(name = "app.migrate.at-start")
    lateinit var migrateAtStart: String

    @Test
    fun `effective dsql configuration`() {
        val cfg = ds.configuration
        val pool = cfg.connectionPoolConfiguration()
        val factory = pool.connectionFactoryConfiguration()
        assertThat(factory.connectionProviderClass().name).isEqualTo("software.amazon.dsql.jdbc.DSQLConnector")
        assertThat(factory.jdbcUrl()).startsWith("jdbc:aws-dsql:postgresql://")
        assertThat(factory.principal()?.name).isEqualTo("admin")
        assertThat(factory.credentials()).`as`("no password configured").isEmpty()
        assertThat(pool.minSize()).isZero()
        assertThat(pool.initialSize()).isZero()
        assertThat(pool.maxSize()).isEqualTo(2)
        assertThat(pool.maxLifetime()).isEqualTo(Duration.ofMinutes(50))
        assertThat(startOffline).isEqualTo("true")
        assertThat(versionCheck).isEqualTo("false")
        assertThat(indexAsync).isEqualTo("ASYNC")
        assertThat(migrateAtStart).isEqualTo("false")
    }

    @Test
    fun `starts without opening a database connection`() {
        // validation fails with 400 before any DB access, and the pool never created a connection
        given().get("/jobs?date=2026-11-02").then().statusCode(400)
        assertThat(ds.metrics.creationCount()).isZero()
        assertThat(ds.metrics.activeCount()).isZero()
    }
}
