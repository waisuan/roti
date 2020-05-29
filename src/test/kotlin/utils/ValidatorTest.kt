package utils

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidatorTest {
    @Test
    fun `generateToken() should return a valid JWT token`() {
        assertThat(Validator.generateToken()).isNotEmpty()
    }

    @Test
    fun `verifyToken() should return true if JWT token is valid`() {
        val token = Validator.generateToken()
        assertThat(Validator.verifyToken(token)).isTrue()
    }

    @Test
    fun `verifyToken() should return false if JWT token is invalid`() {
        assertThat(Validator.verifyToken("bad token")).isFalse()
    }

    @Test
    fun `token expires within the right amount of time`() {
        var token = Validator.generateToken(expiresAt = LocalDate.now().minusDays(1))
        assertThat(Validator.verifyToken(token)).isFalse()

        token = Validator.generateToken(expiresAt = LocalDate.now().plusDays(10))
        assertThat(Validator.verifyToken(token)).isTrue()
    }
}
