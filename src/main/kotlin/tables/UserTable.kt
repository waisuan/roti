package tables

import java.time.LocalDateTime
import models.UserRole
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.datetime

object UserTable : LongIdTable("Users") {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 200)
    val email = varchar("email", 100).uniqueIndex()
    val salt = text("salt")
    val isApproved = bool("approved").default(false)
    val role = varchar("role", length = 50).default(UserRole.NON_ADMIN.name)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}
