package models

import io.javalin.core.security.Role

enum class RotiRole : Role { ANYONE, LOGGED_IN }

enum class UserRole : Role { ADMIN, NON_ADMIN }

data class User(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val is_approved: Boolean = false,
    val role: String = UserRole.NON_ADMIN.name
) {
    fun isValid(): Boolean {
        return username != null &&
            password != null &&
            email != null
    }
}
