package dao

import org.jetbrains.exposed.dao.id.LongIdTable

object UserDao : LongIdTable("Users") {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 200)
    val email = varchar("email", 100)
    val salt = text("salt")
}
