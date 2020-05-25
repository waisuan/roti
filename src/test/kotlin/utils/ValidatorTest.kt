package utils

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
}
