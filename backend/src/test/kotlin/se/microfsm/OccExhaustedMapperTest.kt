package se.microfsm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OccExhaustedMapperTest {
    @Test
    fun `maps to 409`() {
        assertThat(OccExhaustedMapper().toResponse(OccExhaustedException(3)).status).isEqualTo(409)
    }
}
