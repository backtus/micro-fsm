package se.microfsm

import io.quarkus.narayana.jta.QuarkusTransaction
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.ws.rs.NotFoundException
import java.time.LocalDate
import java.util.UUID

class OccConfig {
    @Produces
    @Singleton
    fun occRetry(): OccRetry = OccRetry()
}

@ApplicationScoped
class JobService @Inject constructor(
    private val repo: JobRepository,
    private val occRetry: OccRetry,
) {
    fun create(tenant: UUID, req: CreateJobRequest): Job = tx {
        val job = Job(tenant, req.customerName, req.address, req.scheduledDate)
        repo.persist(job)
        job
    }

    fun get(tenant: UUID, id: UUID): Job = tx { find(tenant, id) }

    fun list(tenant: UUID, date: LocalDate): List<Job> = tx { repo.list("tenantId = ?1 and scheduledDate = ?2 order by createdAt", tenant, date) }

    /** Transaction is opened inside the retry loop so each attempt starts from a fresh snapshot. */
    fun complete(tenant: UUID, id: UUID, minutes: Int): Job = completeWithHooks(tenant, id, minutes)

    /** [afterRead] runs inside the transaction between read and write; it exists so tests can force interleavings. */
    internal fun completeWithHooks(tenant: UUID, id: UUID, minutes: Int, afterRead: () -> Unit = {}): Job = occRetry.run {
        tx {
            val job = find(tenant, id)
            afterRead()
            job.status = JobStatus.DONE
            job.actualMinutes = minutes
            job
        }
    }

    private fun find(tenant: UUID, id: UUID): Job =
        repo.find("id = ?1 and tenantId = ?2", id, tenant).firstResult() ?: throw NotFoundException()

    private fun <T> tx(block: () -> T): T = QuarkusTransaction.requiringNew().call(block)
}
