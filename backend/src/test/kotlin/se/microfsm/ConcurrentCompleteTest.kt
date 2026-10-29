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
import java.util.concurrent.TimeUnit

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
        val statuses = try {
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
            futures.map { it.get(30, TimeUnit.SECONDS) } // any other exception (-> HTTP 5xx) fails the test here
        } finally {
            pool.shutdownNow()
        }

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

    /**
     * Deterministic: both writers read the job in their own transaction before either writes, so they share one
     * snapshot. Writer A commits first; writer B's UPDATE then fails with 40001, OccRetry re-runs B's whole
     * transaction (which now sees A's commit) and B succeeds.
     */
    @Test
    fun `losing writer succeeds after retry`() {
        val tenant = UUID.randomUUID()
        val id = service.create(tenant, CreateJobRequest("A", "B", java.time.LocalDate.parse("2026-11-02"))).id!!
        val retriesBefore = occRetry.retries
        val bothRead = CountDownLatch(2)
        val aCommitted = CountDownLatch(1)
        val bAttempts = java.util.concurrent.atomic.AtomicInteger()
        val pool = Executors.newFixedThreadPool(2)
        try {
            val a = pool.submit<Job> {
                service.completeWithHooks(tenant, id, 10, afterRead = { bothRead.countDown(); bothRead.await(10, TimeUnit.SECONDS) })
                    .also { aCommitted.countDown() }
            }
            val b = pool.submit<Job> {
                service.completeWithHooks(tenant, id, 20, afterRead = {
                    if (bAttempts.incrementAndGet() == 1) {
                        bothRead.countDown()
                        bothRead.await(10, TimeUnit.SECONDS)
                        // let A commit first; B's write then conflicts
                        aCommitted.await(10, TimeUnit.SECONDS)
                    }
                })
            }
            a.get(30, TimeUnit.SECONDS)
            val result = b.get(30, TimeUnit.SECONDS)
            assertThat(bAttempts.get()).`as`("B ran twice").isEqualTo(2)
            assertThat(occRetry.retries - retriesBefore).isEqualTo(1)
            assertThat(result.actualMinutes).isEqualTo(20)
        } finally {
            pool.shutdownNow()
        }
        val final = service.get(tenant, id)
        assertThat(final.status).isEqualTo(JobStatus.DONE)
        assertThat(final.actualMinutes).isEqualTo(20) // B was last
        assertThat(final.version).isEqualTo(2L) // both writes applied, one after the other
    }
}
