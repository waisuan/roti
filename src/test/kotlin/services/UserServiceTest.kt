package services

import configs.Config
import exceptions.BadNewUserException
import exceptions.BadOperationException
import exceptions.IllegalUserException
import exceptions.RecordAlreadyExistsException
import exceptions.UnapprovedUserException
import helpers.TestDatabase
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.time.LocalDateTime
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mindrot.jbcrypt.BCrypt
import tables.UserTable

class UserServiceTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun teardown() {
        TestDatabase.purge()
    }

    @Test
    fun `createUser() should create a new user successfully`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.password]).isNotEmpty()
            assertThat(BCrypt.checkpw(user.password, foundUser[UserTable.password])).isTrue()
            assertThat(foundUser[UserTable.salt]).isNotEmpty()
            assertThat(foundUser[UserTable.isApproved]).isFalse()
            assertThat(foundUser[UserTable.role]).isEqualTo(UserRole.NON_ADMIN.name)
            assertThat(foundUser[UserTable.createdAt]).isNotNull()
        }
    }

    @Test
    fun `createUser() should create a new user with hashed password`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.password]).isNotEmpty()
            assertThat(foundUser[UserTable.password]).isNotEqualTo(user.password)
            assertThat(foundUser[UserTable.salt]).isNotEmpty()
            assertThat(BCrypt.checkpw(user.password, foundUser[UserTable.password])).isTrue()
        }
    }

    @Test
    fun `createUser() should throw an exception if new user is invalid`() {
        var user = User()
        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(BadNewUserException::class.java)

        user = User(username = "Evan.S")
        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(BadNewUserException::class.java)

        user = User(username = "Evan.S", email = "some_email")
        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(BadNewUserException::class.java)
    }

    @Test
    fun `createUser() should throw an exception if new user already exists`() {
        var user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)
        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(RecordAlreadyExistsException::class.java)

        user = User("evan.s.2", "password", "evan.s@test.com")
        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(RecordAlreadyExistsException::class.java)
    }

    @Test
    fun `deleteUser() should delete user successfully`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        UserService.deleteUser("evan.s")
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNull()
        }
    }

    @Test
    fun `deleteUser() should throw an exception if user does not exist`() {
        assertThatThrownBy {
            UserService.deleteUser("evan.s")
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on User record")
    }

    @Test
    fun `deleteUsers() should delete multiple users accordingly`() {
        UserService.createUser(User("evan.s", "password", "evan.s@test.com"))
        UserService.createUser(User("evan.s.2", "password", "evan.s.2@test.com"))
        UserService.createUser(User("evan.s.3", "password", "evan.s.3@test.com"))

        UserService.deleteUsers(listOf("evan.s", "evan.s.2", "evan.s.3"))
        val foundUsers = UserService.getUsers()
        assertThat(foundUsers.size).isEqualTo(0)
    }

    @Test
    fun `deleteUsers() should throw an exception is user has no username`() {
        UserService.createUser(User("evan.s", "password", "evan.s@test.com"))

        assertThatThrownBy {
            UserService.deleteUsers(listOf(""))
        }.isInstanceOf(BadOperationException::class.java)
    }

    @Test
    fun `updateUser() should update existing user accordingly`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        var updatedUser = User(email = "some_email", password = "new_password")
        UserService.updateUser("evan.s", updatedUser)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.email]).isEqualTo("some_email")

            assertThat(BCrypt.checkpw(updatedUser.password, foundUser[UserTable.password])).isTrue()
        }

        updatedUser = User(role = UserRole.ADMIN)
        UserService.updateUser("evan.s", updatedUser)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.role]).isEqualTo(UserRole.ADMIN.name)
        }

        updatedUser = User(isApproved = true)
        UserService.updateUser("evan.s", updatedUser)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.isApproved]).isTrue()
        }
    }

    @Test
    fun `updateUser() should throw an exception if user does not exist`() {
        val updatedUser = User(email = "some_email")
        assertThatThrownBy {
            UserService.updateUser("evan.s", updatedUser)
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on User record")
    }

    @Test
    fun `updateUsers() should update multiple users accordingly`() {
        UserService.createUser(User("evan.s", "password", "evan.s@test.com"))
        UserService.createUser(User("evan.s.2", "password", "evan.s.2@test.com"))
        UserService.createUser(User("evan.s.3", "password", "evan.s.3@test.com"))

        UserService.updateUsers(listOf(
            User(username = "evan.s", isApproved = true),
            User(username = "evan.s.2", role = UserRole.ADMIN),
            User(username = "evan.s.3", email = "new_mail@test.com")
        ))
        val foundUsers = UserService.getUsers()
        assertThat(foundUsers.size).isEqualTo(3)
        assertThat(foundUsers.find { it.username.equals("evan.s") }!!.isApproved).isTrue()
        assertThat(foundUsers.find { it.username.equals("evan.s.2") }!!.role).isEqualTo(UserRole.ADMIN)
        assertThat(foundUsers.find { it.username.equals("evan.s.3") }!!.email).isEqualTo("new_mail@test.com")
    }

    @Test
    fun `updateUsers() should throw an exception is user has no username`() {
        UserService.createUser(User("evan.s", "password", "evan.s@test.com"))

        assertThatThrownBy {
            UserService.updateUsers(listOf(
                User(isApproved = true)
            ))
        }.isInstanceOf(BadOperationException::class.java)
    }

    @Test
    fun `loginUser() should validate user appropriately`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!, true)
        assertThat(UserService.loginUser(user)).isNotEmpty()

        val incorrectPwdUser = User("evan.s", "noob", "evan.s@test.com")
        assertThatThrownBy {
            UserService.loginUser(incorrectPwdUser)
        }.isInstanceOf(IllegalUserException::class.java)

        val badUser = User("bad.evan", "password", "evan.s@test.com")
        assertThatThrownBy {
            UserService.loginUser(badUser)
        }.isInstanceOf(IllegalUserException::class.java)

        val unapprovedUser = User("evan.s_2", "password", "evan.s.2@test.com")
        UserService.createUser(unapprovedUser)
        assertThatThrownBy {
            UserService.loginUser(unapprovedUser)
        }.isInstanceOf(UnapprovedUserException::class.java)
    }

    @Test
    fun `approveUser() sets is_approved flag for user record`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        UserService.approveUser(user.username!!, true)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.isApproved]).isTrue()
        }

        UserService.approveUser(user.username!!, false)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.isApproved]).isFalse()
        }

        UserService.approveUser(user.username!!)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.isApproved]).isTrue()
        }
    }

    @Test
    fun `getUser() returns user if exists in DB`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        UserService.getUser(user.username!!).let {
            assertThat(it).isNotNull
            assertThat(it).isEqualTo(user.copy(password = null, isApproved = false, role = UserRole.NON_ADMIN, createdAt = it!!.createdAt))
        }
    }

    @Test
    fun `getUser() returns null if does not exist in DB`() {
        assertThat(UserService.getUser("some_user")).isNull()
    }

    @Test
    fun `getUsers() returns a list of users from the DB`() {
        assertThat(UserService.getUsers()).isEmpty()

        UserService.createUser(User("evan.s", "password", "evan.s@test.com"))
        UserService.createUser(User("evan.s.2", "password", "evan.s.2@test.com"))
        UserService.createUser(User("evan.s.3", "password", "evan.s.3@test.com"))

        assertThat(UserService.getUsers().size).isEqualTo(3)
        UserService.getUsers().forEach {
            assertThat(it).isEqualTo(User(username = it.username, email = it.email, password = null, isApproved = false, role = UserRole.NON_ADMIN, createdAt = it.createdAt))
        }
    }

    @Test
    fun `getRejectedUsers() does not return users that are still unapproved within the designated grace period for approval`() {
        val user = UserService.createUser(User("evan.s", "password", "evan.s@test.com")).let {
            UserService.getUser(username = "evan.s")!!
        }

        UserService.getRejectedUsers().let {
            assertThat(it).isEmpty()
        }

        transaction {
            TransactionManager.current().exec("""
                update users set created_at='${LocalDateTime.now().minusDays(4)}' where username='${user.username}'
            """.trimIndent())
        }
        UserService.getRejectedUsers().let {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun `getRejectedUsers() does not return users that have been approved`() {
        val user = UserService.createUser(User("evan.s", "password", "evan.s@test.com")).let {
            UserService.getUser(username = "evan.s")!!
        }.let {
            UserService.approveUser(it.username!!)
            UserService.getUser(it.username!!)!!
        }

        UserService.getRejectedUsers().let {
            assertThat(it).isEmpty()
        }

        transaction {
            TransactionManager.current().exec("""
                update users set created_at='${LocalDateTime.now().minusDays(100)}' where username='${user.username}'
            """.trimIndent())
        }
        UserService.getRejectedUsers().let {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun `getRejectedUsers() returns a list of users that have not been approved for more than 5 days`() {
        val user = UserService.createUser(User("evan.s", "password", "evan.s@test.com")).let {
            UserService.getUser(username = "evan.s")!!
        }

        transaction {
            TransactionManager.current().exec("""
                update users set created_at='${LocalDateTime.now().minusDays(6)}' where username='${user.username}'
            """.trimIndent())
        }
        UserService.getRejectedUsers().let {
            assertThat(it.map { user -> user.username }).isEqualTo(listOf(user.username))
        }
    }

    @Test
    fun `getUsersByRole() returns a list of users filtered by a specific role`() {
        (1..10).forEach { idx ->
            UserService.createUser(User(idx.toString(), "password$idx", "$idx@mail.com")).also {
                // Even usernames will have an ADMIN role.
                if (idx % 2 == 0)
                    UserService.updateUser(idx.toString(), User(role = UserRole.ADMIN))
                else
                    UserService.updateUser(idx.toString(), User(role = UserRole.NON_ADMIN))
            }
        }
        UserService.getUsersByRole(UserRole.ADMIN).let {
            assertThat(it.map(User::username)).isEqualTo(listOf("2", "4", "6", "8", "10"))
        }
        UserService.getUsersByRole(UserRole.NON_ADMIN).let {
            assertThat(it.map(User::username)).isEqualTo(listOf("1", "3", "5", "7", "9"))
        }
    }

    @Test
    fun `createUser() should throw an exception if max number of registered users has been reached`() {
        mockkObject(Config)
        every { Config.maxNumOfRegisteredUsers } returns 3

        (1..3).forEach { idx ->
            assertDoesNotThrow {
                UserService.createUser(
                    User(username = "ESia${idx}", password = "password_${idx}", email = "esia_${idx}@mail.com")
                )
            }
        }
        assertThatThrownBy { UserService.createUser(
            User(username = "Esia4", password = "password_4", email = "esia_4@mail.com")
        ) }.isInstanceOf(BadNewUserException::class.java)

        unmockkObject(Config)
    }
}
