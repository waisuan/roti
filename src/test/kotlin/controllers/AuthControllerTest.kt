package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.javalin.core.security.Role
import io.javalin.http.Context
import io.javalin.http.Handler
import org.junit.After
import org.junit.Test
import org.mockito.Mockito
import utils.Validator

class AuthControllerTest {

    @After
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
}
