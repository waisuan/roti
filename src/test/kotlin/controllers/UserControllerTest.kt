package controllers

import exceptions.IllegalUserException
import helpers.TestDatabase
import io.javalin.http.Context
import io.javalin.http.context.body
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.Constants
import models.User
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.UserService

class UserControllerTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `loginUser() throws an exception if unsuccessful`() {
        val context = mockk<Context>(relaxed = true)
        val user = User(username = "TEST", password = "TEST")

        every { context.body<User>() } returns user

        assertThatThrownBy {
            UserController.loginUser(context)
        }.isInstanceOf(IllegalUserException::class.java)
    }

    @Test
    fun `loginUser() writes to cookie`() {
        val context = mockk<Context>(relaxed = true)
        val user = User(username = "TEST", password = "TEST", email = "test@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!)

        every { context.body<User>() } returns user
        every { context.url() } returns "http://localhost"

        UserController.loginUser(context)

        verify { context.cookie(Constants.USER_TOKEN.name, any()) }
        verify { context.cookie(Constants.USER_NAME.name, user.username!!) }
        verify { context.json(any<User>()) }
    }

    @Test
    fun `logoutUser() removes cookies`() {
        val context = mockk<Context>(relaxed = true)

        every { context.url() } returns "http://localhost"

        context.cookie(Constants.USER_TOKEN.name, "TOKEN")
        context.cookie(Constants.USER_NAME.name, "USER")

        UserController.logoutUser(context)

        verify { context.removeCookie(Constants.USER_TOKEN.name, "/") }
        verify { context.removeCookie(Constants.USER_NAME.name, "/") }
    }
}
