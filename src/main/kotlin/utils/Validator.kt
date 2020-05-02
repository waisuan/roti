package utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.LocalDate
import java.util.UUID

object Validator {
    private val algo = Algorithm.HMAC256("secret")

    fun generateToken(): String {
        return JWT
            .create()
            .withSubject(UUID.randomUUID().toString())
            .withExpiresAt(java.sql.Date.valueOf(LocalDate.now().plusDays(1)))
            .sign(algo)
    }

    fun verifyToken(token: String): Boolean {
        val verifier = JWT.require(algo).build()
        return try { verifier.verify(token) != null } catch (e: JWTVerificationException) { false }
    }
}
