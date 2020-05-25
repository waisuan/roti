package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import exceptions.IllegalUserException
import helpers.TestDatabase
import io.javalin.http.Context
import models.User
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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

        whenever(context.json(any())).thenReturn(context)
        whenever(context.body<User>()).thenReturn(user)

        assertThatThrownBy {
            UserController.loginUser(context)
        }.isInstanceOf(IllegalUserException::class.java)
    }
}
