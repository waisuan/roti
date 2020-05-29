package tables

import models.UserRole
import org.jetbrains.exposed.dao.id.LongIdTable

object UserTable : LongIdTable("Users") {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 200)
    val email = varchar("email", 100).uniqueIndex()
    val salt = text("salt")
    val is_approved = bool("approved").default(false)
    val role = varchar("role", length = 50).default(UserRole.NON_ADMIN.name)
}
