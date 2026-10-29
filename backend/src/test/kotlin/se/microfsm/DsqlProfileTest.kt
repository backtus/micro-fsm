package se.microfsm

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

class DsqlLikeProfile : QuarkusTestProfile {
    // Unreachable DB, no Dev Services, no migrate-at-start, no password: the app must still boot.
    override fun getConfigOverrides() = mapOf(
        "quarkus.datasource.devservices.enabled" to "false",
        "quarkus.flyway.migrate-at-start" to "false",
        "quarkus.datasource.username" to "admin",
        "quarkus.datasource.db-version" to "16.0",
        "quarkus.hibernate-orm.database.version-check.enabled" to "false",
        "quarkus.hibernate-orm.database.start-offline" to "true",
        "quarkus.datasource.jdbc.url" to "jdbc:postgresql://localhost.invalid:5432/postgres",
        "quarkus.datasource.jdbc.initial-size" to "0",
        "quarkus.datasource.jdbc.min-size" to "0",
        "quarkus.datasource.jdbc.max-size" to "2",
        "quarkus.datasource.jdbc.max-lifetime" to "50M",
    )
}

@QuarkusTest
@TestProfile(DsqlLikeProfile::class)
class DsqlProfileTest {
    @Test
    fun `app starts and validates input without a reachable database`() {
        // Reaching here means startup did not open a DB connection. A 400 comes before any DB access.
        given().get("/jobs?date=2026-11-02").then().statusCode(400)
    }
}
