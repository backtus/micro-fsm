package se.microfsm

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

/**
 * Calls the service layer concurrently: in dev/test, quarkus-amazon-lambda-http feeds HTTP requests through a
 * single-threaded mock event loop, which would serialize the requests and hide all conflicts.
 * Local Postgres runs REPEATABLE READ (see application.properties), which raises SQLSTATE 40001 on
 * concurrent updates just like DSQL's OCC.
 */
@QuarkusTest
class ConcurrentCompleteTest {
    @Inject
    lateinit var occRetry: OccRetry

    @Inject
    lateinit var service: JobService

    @Test
    fun `20 concurrent completes give no 5xx and a consistent final state`() {
        val tenant = UUID.randomUUID()
        val job = service.create(tenant, CreateJobRequest("A", "B", java.time.LocalDate.parse("2026-11-02")))
        val id = job.id!!

        val retriesBefore = occRetry.retries
        val n = 20
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(n)
        val futures = (1..n).map { i ->
            pool.submit<Int> {
                start.await()
                try {
                    service.complete(tenant, id, i)
                    200
                } catch (e: OccExhaustedException) {
                    409 // what OccExhaustedMapper returns over HTTP
                }
            }
        }
        start.countDown()
        val statuses = futures.map { it.get() } // any other exception (-> HTTP 5xx) fails the test here
        pool.shutdown()

        val ok = statuses.count { it == 200 }
        assertThat(ok).isGreaterThanOrEqualTo(1)
        assertThat(occRetry.retries - retriesBefore).isGreaterThan(0)

        val final = given().header("X-Tenant-Id", tenant.toString()).contentType(ContentType.JSON)
            .get("/jobs/$id").then().statusCode(200).extract()
        assertThat(final.path<String>("status")).isEqualTo("DONE")
        assertThat(final.path<Int>("actualMinutes")).isBetween(1, n)
        // every successful complete bumped the optimistic-lock version exactly once
        assertThat(final.path<Int>("version")).isEqualTo(ok)
    }
}
