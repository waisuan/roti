package services

import exceptions.BadNewUserException
import exceptions.BadOperationException
import exceptions.IllegalUserException
import exceptions.RecordAlreadyExistsException
import exceptions.UnapprovedUserException
import models.User
import models.UserRole
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.mindrot.jbcrypt.BCrypt
import tables.UserTable
import tables.UserTable.email
import tables.UserTable.is_approved
import tables.UserTable.password
import tables.UserTable.role
import tables.UserTable.username
import utils.Validator

object UserService {
    fun createUser(user: User) {
        if (!user.isValid())
            throw BadNewUserException()

        transaction {
            if ((UserTable.select { username eq user.username!! }.firstOrNull() != null) ||
                (UserTable.select { email eq user.email!! }.firstOrNull() != null))
                throw RecordAlreadyExistsException()

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
                if (user.role != null)
                    it[role] = user.role.name
                if (user.is_approved != null)
                    it[is_approved] = user.is_approved
            }
            if (res == 0)
                throw BadOperationException(User::class.java.simpleName)
        }
    }

    fun updateUsers(users: List<User>) {
        users.forEach { user ->
            if (user.username == null)
                throw BadOperationException(User::class.java.simpleName)
            updateUser(user.username, user)
        }
    }

    fun deleteUser(username: String) {
        transaction {
            val res = UserTable.deleteWhere { UserTable.username eq username }
            if (res == 0)
                throw BadOperationException(User::class.java.simpleName)
        }
    }

    fun deleteUsers(users: List<String>) {
        users.forEach { username ->
            deleteUser(username)
        }
    }

    fun loginUser(user: User): String {
        val foundUser = transaction {
            UserTable.select { username eq user.username!! }.firstOrNull()
        } ?: throw IllegalUserException()

        if (!foundUser[is_approved])
            throw UnapprovedUserException()

        if (!BCrypt.checkpw(user.password, foundUser[password]))
            throw IllegalUserException()

        return Validator.generateToken()
    }

    fun approveUser(username: String, approveFlag: Boolean = true) {
        transaction {
            val res = UserTable.update({ UserTable.username eq username }) {
                it[is_approved] = approveFlag
            }
            if (res == 0)
                throw BadOperationException(User::class.java.simpleName)
        }
    }

    fun getUser(username: String): User? {
        return transaction {
            UserTable.select { UserTable.username eq username }.map {
                toUserModel(it)
            }
        }.firstOrNull()
    }

    fun getUsers(): List<User> {
        return transaction {
            UserTable.selectAll().orderBy(username).map { toUserModel(it) }
        }
    }

    fun getUserRoles(): List<UserRole> {
        return UserRole.values().toMutableList()
    }

    private fun toUserModel(row: ResultRow): User {
        return User(username = row[username], email = row[email], is_approved = row[is_approved], role = UserRole.valueOf(row[role]))
    }

    private fun generateHashAndSalt(password: String): Pair<String, String> {
        val salt = BCrypt.gensalt()
        return Pair(BCrypt.hashpw(password, salt), salt)
    }
}
