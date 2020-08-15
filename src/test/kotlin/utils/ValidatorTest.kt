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

    @Test
    fun `isTokenAlmostExpired() returns true if token is almost expired`() {
        var token = Validator.generateToken(LocalDate.now().plusDays(1))
        assertThat(Validator.isTokenAlmostExpired(token)).isTrue()

        token = Validator.generateToken(LocalDate.now().plusDays(5))
        assertThat(Validator.isTokenAlmostExpired(token, 5)).isTrue()

        token = Validator.generateToken(LocalDate.now().plusDays(5))
        assertThat(Validator.isTokenAlmostExpired(token, 5, LocalDate.now().plusDays(1))).isTrue()
        assertThat(Validator.isTokenAlmostExpired(token, 5, LocalDate.now().plusDays(2))).isTrue()
        assertThat(Validator.isTokenAlmostExpired(token, 5, LocalDate.now().plusDays(3))).isTrue()
        assertThat(Validator.isTokenAlmostExpired(token, 5, LocalDate.now().plusDays(4))).isTrue()
    }

    @Test
    fun `isTokenAlmostExpired() returns false if token is not almost expired`() {
        val token = Validator.generateToken()
        assertThat(Validator.isTokenAlmostExpired(token)).isFalse()
        assertThat(Validator.isTokenAlmostExpired(token, 6)).isFalse()
    }
}
