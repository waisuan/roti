package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import models.RotiRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import utils.Validator

class AuthControllerTest {

    @AfterEach
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `handles request if JWT token has been verified successfully`() {
        val handler = mock<Handler>()
        val context = mock<Context>()
        val token = Validator.generateToken()

        whenever(context.header(any())).doAnswer { "Bearer $token" }
        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("GET")
        whenever(context.status(any())).thenReturn(context)
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, HashSet<Role>())

        verify(handler).handle(any())
    }

    @Test
    fun `discards request if JWT token is invalid`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.header(any())).doAnswer { "Bearer BAD_TOKEN" }
        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("GET")
        whenever(context.status(any())).thenReturn(context)
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, HashSet<Role>())

        verify(context).status(401)
    }

    @Test
    fun `skips auth if path is under dev mode`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(handler.handle(any())).doAnswer { Unit }

        val envVar = EnvironmentVariables()
        envVar.set("DEV_MODE", "1")
        assertThat(System.getenv("DEV_MODE")).isEqualTo("1")

        AuthController.accessManager(handler, context, HashSet<Role>())

        verify(handler).handle(any())
    }

    @Test
    fun `skips auth if role is ANYONE`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.matchedPath()).thenReturn("/users/login")
        whenever(context.method()).thenReturn("POST")
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(RotiRole.ANYONE))

        verify(handler).handle(any())
    }

    @Test
    fun `needs auth if role is LOGGED_IN`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.matchedPath()).thenReturn("/users/login")
        whenever(context.method()).thenReturn("POST")
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(RotiRole.LOGGED_IN))

        verify(context).status(401)
    }
}
