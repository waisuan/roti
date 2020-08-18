package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import exceptions.IllegalUserException
import helpers.TestDatabase
import io.javalin.http.Context
import models.Constants
import models.User
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import services.UserService

class UserControllerTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun tearDown() {
        Mockito.framework().clearInlineMocks()
        TestDatabase.purge()
    }

    @Test
    fun `loginUser() throws an exception if unsuccessful`() {
        val context = mock<Context>()
        val user = User(username = "TEST", password = "TEST")

        whenever(context.body<User>()).thenReturn(user)

        assertThatThrownBy {
            UserController.loginUser(context)
        }.isInstanceOf(IllegalUserException::class.java)
    }

    @Test
    fun `loginUser() writes to cookie`() {
        val context = mock<Context>()
        val user = User(username = "TEST", password = "TEST", email = "test@mail.com")
        UserService.createUser(user)
        UserService.approveUser(user.username!!)

        whenever(context.body<User>()).thenReturn(user)

        UserController.loginUser(context)

        verify(context).cookie(eq(Constants.USER_TOKEN.name), any(), any())
        verify(context).cookie(eq(Constants.USER_NAME.name), eq(user.username!!), any())
    }

    @Test
    fun `logoutUser() removes cookies`() {
        val context = mock<Context>()

        context.cookie(Constants.USER_TOKEN.name, "TOKEN")
        context.cookie(Constants.USER_NAME.name, "USER")

        UserController.logoutUser(context)

        verify(context).removeCookie(eq(Constants.USER_TOKEN.name), eq("/"))
        verify(context).removeCookie(eq(Constants.USER_NAME.name), eq("/"))
    }
}
