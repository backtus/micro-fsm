package se.microfsm

import org.jboss.logging.Logger
import java.sql.SQLException
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong

/** Thrown when a transaction still conflicts after all attempts. Mapped to HTTP 409. */
class OccExhaustedException(val attempts: Int) : RuntimeException("Concurrent update conflict after $attempts attempts")

/**
 * Re-runs [block] on serialization failures (SQLSTATE 40001, which Aurora DSQL uses for OCC conflicts).
 * [block] must open and finish its own transaction: the transaction goes inside the loop, not around it.
 * Logs only attempt count and SQLSTATE (never SQL, parameters or data).
 */
class OccRetry(
    private val maxAttempts: Int = 3,
    private val baseDelayMs: Long = 25,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
) {
    private val log = Logger.getLogger(OccRetry::class.java)
    private val retryCounter = AtomicLong()

    /** Total number of retries performed since startup (observability and tests). */
    val retries: Long get() = retryCounter.get()

    fun <T> run(block: () -> T): T {
        for (attempt in 1..maxAttempts) {
            try {
                return block()
            } catch (e: Exception) {
                if (!isSerializationFailure(e)) throw e
                log.warnf("OCC conflict sqlstate=%s attempt=%d/%d", SERIALIZATION_FAILURE, attempt, maxAttempts)
                if (attempt == maxAttempts) throw OccExhaustedException(maxAttempts)
                retryCounter.incrementAndGet()
                // exponential backoff with full jitter
                val cap = baseDelayMs shl (attempt - 1)
                sleeper(ThreadLocalRandom.current().nextLong(1, cap + 1))
            }
        }
        throw IllegalStateException("unreachable")
    }

    companion object {
        const val SERIALIZATION_FAILURE = "40001"

        fun isSerializationFailure(t: Throwable): Boolean {
            val seen = HashSet<Throwable>()
            val queue = ArrayDeque<Throwable>().apply { add(t) }
            while (queue.isNotEmpty()) {
                val cur = queue.removeFirst()
                if (!seen.add(cur)) continue
                if (cur is SQLException && cur.sqlState == SERIALIZATION_FAILURE) return true
                cur.cause?.let(queue::add)
                if (cur is SQLException) cur.nextException?.let(queue::add)
                queue.addAll(cur.suppressed)
            }
            return false
        }
    }
}
