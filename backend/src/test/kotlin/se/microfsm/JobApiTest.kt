package se.microfsm

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class JobApiTest {
    private val body = """{"customerName":"Anna Svensson","address":"Storgatan 1, Malmö","scheduledDate":"2026-11-02"}"""

    private fun create(tenant: UUID, date: String = "2026-11-02"): String =
        given().header("X-Tenant-Id", tenant.toString()).contentType(ContentType.JSON)
            .body("""{"customerName":"A","address":"B","scheduledDate":"$date"}""")
            .post("/jobs").then().statusCode(201).extract().path("id")

    @Test
    fun `create then get round-trips`() {
        val tenant = UUID.randomUUID()
        val id = given().header("X-Tenant-Id", tenant.toString()).contentType(ContentType.JSON).body(body)
            .post("/jobs").then().statusCode(201).body("status", equalTo("PLANNED")).extract().path<String>("id")
        assertThat(UUID.fromString(id).version()).isEqualTo(7)
        given().header("X-Tenant-Id", tenant.toString()).get("/jobs/$id").then().statusCode(200)
            .body("customerName", equalTo("Anna Svensson"))
            .body("address", equalTo("Storgatan 1, Malmö"))
            .body("scheduledDate", equalTo("2026-11-02"))
            .body("actualMinutes", equalTo(null))
    }

    @Test
    fun `ids are UUIDv7 and increase`() {
        val tenant = UUID.randomUUID()
        val ids = (1..50).map { UUID.fromString(create(tenant)) }
        assertThat(ids).allMatch { it.version() == 7 }
        val timestamps = ids.map { it.mostSignificantBits ushr 16 }
        assertThat(timestamps).isSorted()
    }

    @Test
    fun `list returns only own tenant and date`() {
        val a = UUID.randomUUID()
        val b = UUID.randomUUID()
        val mine = create(a)
        create(a, "2026-11-03")
        create(b)
        given().header("X-Tenant-Id", a.toString()).get("/jobs?date=2026-11-02").then().statusCode(200)
            .body("size()", equalTo(1)).body("[0].id", equalTo(mine))
    }

    @Test
    fun `other tenant gets 404`() {
        val id = create(UUID.randomUUID())
        given().header("X-Tenant-Id", UUID.randomUUID().toString()).get("/jobs/$id").then().statusCode(404)
    }

    @Test
    fun `missing or invalid tenant header gives 400`() {
        given().get("/jobs?date=2026-11-02").then().statusCode(400)
        given().header("X-Tenant-Id", "nope").get("/jobs?date=2026-11-02").then().statusCode(400)
        given().contentType(ContentType.JSON).body(body).post("/jobs").then().statusCode(400)
        given().get("/jobs/${UUID.randomUUID()}").then().statusCode(400)
    }

    @Test
    fun `invalid date gives 400`() {
        given().header("X-Tenant-Id", UUID.randomUUID().toString()).get("/jobs?date=2026-13-45").then().statusCode(400)
    }
}
