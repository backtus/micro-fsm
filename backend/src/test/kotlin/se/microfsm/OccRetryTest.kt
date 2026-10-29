package se.microfsm

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OccRetryTest {
    private val delays = mutableListOf<Long>()
    private val retry = OccRetry(maxAttempts = 3, baseDelayMs = 10, sleeper = { delays += it })

    private fun conflict() = RuntimeException("commit failed", RuntimeException("wrapped", SQLException("boom", "40001")))

    @Test
    fun `retries on 40001 inside cause chain until success`() {
        var calls = 0
        val result = retry.run {
            calls++
            if (calls <= 2) throw conflict()
            "ok"
        }
        assertThat(result).isEqualTo("ok")
        assertThat(calls).isEqualTo(3)
        assertThat(delays).hasSize(2)
        assertThat(delays[0]).isBetween(1L, 10L)
        assertThat(delays[1]).isBetween(1L, 20L)
        assertThat(retry.retries).isEqualTo(2)
    }

    @Test
    fun `finds 40001 in next exception`() {
        val e = SQLException("outer", "XX000").apply { nextException = SQLException("inner", "40001") }
        assertThat(OccRetry.isSerializationFailure(e)).isTrue()
    }

    @Test
    fun `other sqlstate is not retried`() {
        var calls = 0
        assertThatThrownBy {
            retry.run { calls++; throw RuntimeException(SQLException("dup", "23505")) }
        }.isInstanceOf(RuntimeException::class.java)
        assertThat(calls).isEqualTo(1)
        assertThat(delays).isEmpty()
    }

    @Test
    fun `non-sql exception is not retried`() {
        var calls = 0
        assertThatThrownBy { retry.run { calls++; throw IllegalStateException("x") } }
            .isInstanceOf(IllegalStateException::class.java)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun `exhaustion throws OccExhaustedException after max attempts`() {
        var calls = 0
        assertThatThrownBy { retry.run { calls++; throw conflict() } }
            .isInstanceOf(OccExhaustedException::class.java)
        assertThat(calls).isEqualTo(3)
    }
}
