package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import configs.Config
import helpers.TestDatabase
import io.javalin.http.Context
import io.javalin.http.Handler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import models.Constants
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import services.UserService
import utils.Validator

class AuthControllerTest {

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
    fun `handles request if JWT token has been verified successfully`() {
        val handler = mock<Handler>()
        val context = mock<Context>()
        val token = Validator.generateToken()
        UserService.createUser(User("TEST", "TEST", email = "TEST"))

        whenever(context.cookie(Constants.USER_TOKEN.name)).doAnswer { token }
        whenever(context.cookie(Constants.USER_NAME.name)).doAnswer { "TEST" }
        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("GET")
        whenever(context.status(any())).thenReturn(context)
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify(handler).handle(any())
    }

    @Test
    fun `falls back on authorization header if cookies are not available`() {
        val handler = mock<Handler>()
        val context = mock<Context>()
        val token = Validator.generateToken()
        UserService.createUser(User("TEST", "TEST", email = "TEST"))

        whenever(context.header("Authorization")).doAnswer { "Bearer $token:TEST" }
        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("GET")
        whenever(context.status(any())).thenReturn(context)
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify(handler).handle(any())
    }

    @Test
    fun `discards request if JWT token is invalid`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.cookie(Constants.USER_TOKEN.name)).doAnswer { "BAD_TOKEN" }
        whenever(context.cookie(Constants.USER_NAME.name)).doAnswer { "BAD_USER" }
        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("GET")
        whenever(context.status(any())).thenReturn(context)
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify(context).status(401)
    }

    @Test
    fun `skips auth if path is under dev mode`() {
        val handler = mockk<Handler>()
        val context = mockk<Context>()
        mockkObject(Config)

        every { handler.handle(any()) } returns Unit
        every { context.cookie(any<String>()) } returns null
        every { context.header(any()) } returns null
        every { Config.devMode } returns "1"

        assertThat(Config.devMode).isEqualTo("1")

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        io.mockk.verify { handler.handle(any()) }

        unmockkObject(Config)
    }

    @Test
    fun `skips auth if role is GUEST`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("POST")
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, setOf(UserRole.GUEST))

        verify(handler).handle(any())
    }

    @Test
    fun `skips auth if no user role is required`() {
        val handler = mock<Handler>()
        val context = mock<Context>()

        whenever(context.matchedPath()).thenReturn("/some/path")
        whenever(context.method()).thenReturn("POST")
        whenever(handler.handle(any())).doAnswer { Unit }

        AuthController.accessManager(handler, context, emptySet())

        verify(handler).handle(any())
    }

    @Test
    fun `needs auth if role is NON_ADMIN`() {
        val handler = mockk<Handler>()
        val context = mockk<Context>()

        every { context.matchedPath() } returns "/some/path"
        every { context.method() } returns "POST"
        every { context.header(any()) } returns null
        every { context.status(any()) } returns context
        every { context.cookie(any<String>()) } answers { null }

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        io.mockk.verify { context.status(401) }
    }

    @Test
    fun `needs auth if role is ADMIN`() {
        val handler = mockk<Handler>()
        val context = mockk<Context>()

        every { context.matchedPath() } returns "/some/path"
        every { context.method() } returns "POST"
        every { context.header(any()) } returns null
        every { context.status(any()) } returns context
        every { context.cookie(any<String>()) } answers { null }

        AuthController.accessManager(handler, context, setOf(UserRole.ADMIN))

        io.mockk.verify { context.status(401) }
    }
}
