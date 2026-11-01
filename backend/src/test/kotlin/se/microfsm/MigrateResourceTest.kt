package se.microfsm

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class MigrateEnabled : QuarkusTestProfile {
    override fun getConfigOverrides() = mapOf("app.migrate.endpoint-enabled" to "true")
}

@QuarkusTest
class MigrateResourceDisabledTest {
    @Test
    fun `endpoint is 404 unless enabled`() {
        given().post("/internal/migrate").then().statusCode(404)
    }
}

@QuarkusTest
@TestProfile(MigrateEnabled::class)
class MigrateResourceEnabledTest {
    @Test
    fun `enabled endpoint runs migrator, rerun is a no-op`() {
        // dev/test migrate at start already
        given().post("/internal/migrate").then().statusCode(200).body(equalTo("Applied 0 migration(s)"))
    }
}
