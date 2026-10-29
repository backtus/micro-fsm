package se.microfsm

import io.quarkus.narayana.jta.QuarkusTransaction
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import java.time.LocalDate
import java.util.UUID

@ApplicationScoped
class JobService @Inject constructor(
    private val repo: JobRepository,
) {
    fun create(tenant: UUID, req: CreateJobRequest): Job = tx {
        val job = Job(tenant, req.customerName, req.address, req.scheduledDate)
        repo.persist(job)
        job
    }

    fun get(tenant: UUID, id: UUID): Job = tx { find(tenant, id) }

    fun list(tenant: UUID, date: LocalDate): List<Job> = tx { repo.list("tenantId = ?1 and scheduledDate = ?2 order by createdAt", tenant, date) }

    private fun find(tenant: UUID, id: UUID): Job =
        repo.find("id = ?1 and tenantId = ?2", id, tenant).firstResult() ?: throw NotFoundException()

    private fun <T> tx(block: () -> T): T = QuarkusTransaction.requiringNew().call(block)
}
