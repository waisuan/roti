package utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

object Validator {
    private val algo = Algorithm.HMAC256("secret") // TODO move "secret" to env var

    fun generateToken(expiresAt: LocalDate = LocalDate.now().plusDays(7)): String {
        return JWT
            .create()
            .withSubject(UUID.randomUUID().toString())
            .withExpiresAt(Date.valueOf(expiresAt))
            .sign(algo)
    }

    fun verifyToken(token: String): Boolean {
        val verifier = JWT.require(algo).build()
        return try { verifier.verify(token) != null } catch (e: JWTVerificationException) { false }
    }

    fun isTokenAlmostExpired(token: String, window: Long = 1, startDate: LocalDate = LocalDate.now()): Boolean {
        val verifier = JWT.require(algo).build()
        return try {
            val diff = ChronoUnit.DAYS.between(
                startDate,
                verifier.verify(token).expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            )
            diff in 1..window
        } catch (e: JWTVerificationException) {
            false
        }
    }
}
