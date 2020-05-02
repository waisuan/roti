package services

import dao.UserDao
import dao.UserDao.password
import dao.UserDao.username
import models.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object UserService {
    fun getAllUsers(): List<String> {
        return transaction {
            UserDao.selectAll().map {
                it[username]
            }
        }
    }

    fun createUser(user: User) {
        transaction {
            UserDao.insert {
                it[username] = user.username
                it[email] = user.email!!

                val salt = BCrypt.gensalt()
                it[password] = BCrypt.hashpw(user.password, salt)
                it[UserDao.salt] = salt
            }
        }
    }

    fun loginUser(user: User): Boolean {
        val foundUser = transaction {
            UserDao.select { username eq user.username }.firstOrNull()
        } ?: return false

        if (!BCrypt.checkpw(user.password, foundUser[password]))
            return false

        return true
    }
}
