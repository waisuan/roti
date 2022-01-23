package models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.javalin.core.security.Role
import java.time.LocalDateTime

enum class UserRole : Role { ADMIN, NON_ADMIN, GUEST }

@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
    val username: String? = null,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    val password: String? = null,
    val email: String? = null,
    val isApproved: Boolean? = null,
    val role: UserRole? = null,
    var token: String? = null,

    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime? = null
) {
    @JsonIgnore
    fun isValid(): Boolean {
        return username != null &&
            password != null &&
            email != null
    }
}
