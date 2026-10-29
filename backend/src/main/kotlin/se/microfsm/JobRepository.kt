package se.microfsm

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class JobRepository : PanacheRepositoryBase<Job, UUID>
