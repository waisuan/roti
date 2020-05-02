package services

import helpers.TestDatabase
import models.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

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

        val createdUser = UserService.getAllUsers().firstOrNull()
        assertThat(createdUser).isNotNull()
        assertThat(createdUser).isEqualTo(user.username)
    }

    @Test
    fun `loginUser() should validate user successfully`() {
        val user = User("evan.s", "password", "evan.s@test.com")
        UserService.createUser(user)
        assertThat(UserService.loginUser(user)).isTrue()

        val badUser = User("bad.evan", "password", "evan.s@test.com")
        assertThat(UserService.loginUser(badUser)).isFalse()
    }
}