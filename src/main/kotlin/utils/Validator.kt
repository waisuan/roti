package utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.sql.Date
import java.time.LocalDate
import java.util.UUID

object Validator {
    private val algo = Algorithm.HMAC256("secret") // TODO move "secret" to env var

    fun generateToken(expiresAt: LocalDate = LocalDate.now().plusDays(1)): String {
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
}
