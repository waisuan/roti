package models

import io.javalin.core.security.Role

enum class RotiRole : Role { ANYONE, LOGGED_IN }

data class User(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null
) {
    fun isValid(): Boolean {
        return username != null && password != null && email != null
    }
}
