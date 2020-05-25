package services

import exceptions.BadNewUserException
import exceptions.BadOperationException
import exceptions.IllegalUserException
import models.User
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import tables.UserTable
import tables.UserTable.password
import tables.UserTable.username
import utils.Validator

object UserService {
    fun createUser(user: User) {
        if (!user.isValid())
            throw BadNewUserException()

        transaction {
            UserTable.insert {
                it[username] = user.username!!
                it[email] = user.email!!

                val (hash, salt) = generateHashAndSalt(user.password!!)
                it[password] = hash
                it[UserTable.salt] = salt
            }
        }
    }

    fun updateUser(username: String, user: User) {
        transaction {
            val res = UserTable.update({ UserTable.username eq username }) {
                if (user.email != null)
                    it[email] = user.email
                if (user.password != null) {
                    val (hash, salt) = generateHashAndSalt(user.password)
                    it[password] = hash
                    it[UserTable.salt] = salt
                }
            }
            if (res == 0)
                throw BadOperationException("User")
        }
    }

    fun deleteUser(username: String) {
        transaction {
            val res = UserTable.deleteWhere { UserTable.username eq username }
            if (res == 0)
                throw BadOperationException("User")
        }
    }

    fun loginUser(user: User): String {
        val foundUser = transaction {
            UserTable.select { username eq user.username!! }.firstOrNull()
        } ?: throw IllegalUserException()

        if (!BCrypt.checkpw(user.password, foundUser[password]))
            throw IllegalUserException()

        return Validator.generateToken()
    }

    private fun generateHashAndSalt(password: String): Pair<String, String> {
        val salt = BCrypt.gensalt()
        return Pair(BCrypt.hashpw(password, salt), salt)
    }
}
