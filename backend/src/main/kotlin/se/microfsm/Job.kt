package se.microfsm

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class JobStatus { PLANNED, DONE }

@Entity
@Table(name = "job")
class Job(
    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,
    @Column(name = "customer_name", nullable = false)
    var customerName: String,
    @Column(nullable = false)
    var address: String,
    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDate,
) {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    var id: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: JobStatus = JobStatus.PLANNED

    @Column(name = "actual_minutes")
    var actualMinutes: Int? = null

    @Version
    @Column(nullable = false)
    var version: Long = 0

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
}
