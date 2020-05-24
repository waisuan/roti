package services

import models.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import tables.UserTable
import tables.UserTable.password
import tables.UserTable.username

object UserService {
    fun createUser(user: User) {
        transaction {
            UserTable.insert {
                it[username] = user.username
                it[email] = user.email!!

                val salt = BCrypt.gensalt()
                it[password] = BCrypt.hashpw(user.password, salt)
                it[UserTable.salt] = salt
            }
        }
    }

    // fun updateUser

    // fun deleteUser

    fun loginUser(user: User): Boolean {
        val foundUser = transaction {
            UserTable.select { username eq user.username }.firstOrNull()
        } ?: return false

        if (!BCrypt.checkpw(user.password, foundUser[password]))
            return false

        return true
    }
}
