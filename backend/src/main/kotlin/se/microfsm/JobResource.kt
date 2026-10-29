package se.microfsm

import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

data class CreateJobRequest(val customerName: String, val address: String, val scheduledDate: LocalDate)

data class CompleteJobRequest(val actualMinutes: Int)

data class JobDto(
    val id: UUID,
    val customerName: String,
    val address: String,
    val scheduledDate: LocalDate,
    val status: JobStatus,
    val actualMinutes: Int?,
    val version: Long,
    val createdAt: Instant,
)

fun Job.toDto() = JobDto(id!!, customerName, address, scheduledDate, status, actualMinutes, version, createdAt)

@Provider
class OccExhaustedMapper : ExceptionMapper<OccExhaustedException> {
    override fun toResponse(e: OccExhaustedException): Response =
        Response.status(Response.Status.CONFLICT).entity(mapOf("error" to "conflict, retry later")).build()
}

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class JobResource(private val service: JobService) {

    @POST
    fun create(@HeaderParam("X-Tenant-Id") tenantHeader: String?, req: CreateJobRequest): Response {
        val tenant = tenant(tenantHeader)
        if (req.customerName.isBlank() || req.address.isBlank()) throw BadRequestException("customerName and address are required")
        val job = service.create(tenant, req).toDto()
        return Response.status(Response.Status.CREATED).header("Location", "/jobs/${job.id}").entity(job).build()
    }

    @GET
    @Path("/{id}")
    fun get(@HeaderParam("X-Tenant-Id") tenantHeader: String?, @PathParam("id") id: UUID): JobDto =
        service.get(tenant(tenantHeader), id).toDto()

    @GET
    fun list(@HeaderParam("X-Tenant-Id") tenantHeader: String?, @QueryParam("date") date: String?): List<JobDto> {
        val tenant = tenant(tenantHeader)
        val day = try {
            LocalDate.parse(date ?: throw BadRequestException("date=YYYY-MM-DD is required"))
        } catch (e: DateTimeParseException) {
            throw BadRequestException("date must be YYYY-MM-DD")
        }
        return service.list(tenant, day).map { it.toDto() }
    }

    @POST
    @Path("/{id}/complete")
    fun complete(
        @HeaderParam("X-Tenant-Id") tenantHeader: String?,
        @PathParam("id") id: UUID,
        req: CompleteJobRequest,
    ): JobDto {
        val tenant = tenant(tenantHeader)
        if (req.actualMinutes < 0) throw BadRequestException("actualMinutes must be >= 0")
        return service.complete(tenant, id, req.actualMinutes).toDto()
    }

    private fun tenant(header: String?): UUID =
        try {
            UUID.fromString(header ?: throw BadRequestException("X-Tenant-Id header is required"))
        } catch (e: IllegalArgumentException) {
            throw BadRequestException("X-Tenant-Id must be a UUID")
        }
}
