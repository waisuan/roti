package services

import exceptions.BadNewUserException
import exceptions.BadOperationException
import exceptions.IllegalUserException
import helpers.TestDatabase
import java.lang.Exception
import models.User
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mindrot.jbcrypt.BCrypt
import tables.UserTable

class UserServiceTest {
    @Before
    fun setup() {
        TestDatabase.init()
    }

    @After
    fun teardown() {
        TestDatabase.purge()
    }

    @Test
    fun `createUser() should create a new user successfully`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        assertThat(UserService.loginUser(user)).isNotEmpty()
    }

    @Test
    fun `createUser() should create a new user with hashed password`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.first()
            assertThat(foundUser[UserTable.password]).isNotEmpty()
            assertThat(foundUser[UserTable.password]).isNotEqualTo(user.password)
            assertThat(foundUser[UserTable.salt]).isNotEmpty()
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
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        assertThatThrownBy {
            UserService.createUser(user)
        }.isInstanceOf(Exception::class.java)
            .hasMessageContaining("duplicate key")
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
    fun `updateUser() should update existing user accordingly`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)

        val updatedUser = User(email = "some_email", password = "new_password")
        UserService.updateUser("evan.s", updatedUser)
        transaction {
            val foundUser = UserTable.select { UserTable.username eq user.username!! }.firstOrNull()
            assertThat(foundUser).isNotNull
            assertThat(foundUser!![UserTable.email]).isEqualTo("some_email")

            assertThat(BCrypt.checkpw(updatedUser.password, foundUser[UserTable.password]))
                .isTrue()
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
    fun `deleteUser() should throw an exception if user does not exist`() {
        assertThatThrownBy {
            UserService.deleteUser("evan.s")
        }.isInstanceOf(BadOperationException::class.java)
            .hasMessageContaining("Unable to operate on User record")
    }

    @Test
    fun `loginUser() should validate user appropriately`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)
        assertThat(UserService.loginUser(user)).isNotEmpty()

        val incorrectPwdUser = User("evan.s", "noob", "evan.s@test.com")
        assertThatThrownBy {
            UserService.loginUser(incorrectPwdUser)
        }.isInstanceOf(IllegalUserException::class.java)

        val badUser = User("bad.evan", "password", "evan.s@test.com")
        assertThatThrownBy {
            UserService.loginUser(badUser)
        }.isInstanceOf(IllegalUserException::class.java)
    }
}
