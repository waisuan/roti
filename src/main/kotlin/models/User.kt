package models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.javalin.core.security.Role

enum class UserRole : Role { ADMIN, NON_ADMIN, GUEST }

data class User(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val is_approved: Boolean? = null,
    val role: UserRole? = null
) {
    @JsonIgnore
    fun isValid(): Boolean {
        return username != null &&
            password != null &&
            email != null
    }
}
