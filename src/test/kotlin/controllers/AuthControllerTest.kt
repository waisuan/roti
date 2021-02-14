package controllers

import configs.Config
import helpers.TestDatabase
import io.javalin.http.Context
import io.javalin.http.Handler
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import models.Constants
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.UserService
import utils.Validator

class AuthControllerTest {

    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun tearDown() {
        TestDatabase.purge()
    }

    @Test
    fun `handles request if JWT token has been verified successfully`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)
        val token = Validator.generateToken()
        UserService.createUser(User("TEST", "TEST", email = "TEST"))

        every { context.header(any()) } returns null
        every { context.cookie(Constants.USER_TOKEN.name) } returns token
        every { context.cookie(Constants.USER_NAME.name) } returns "TEST"

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify { handler.handle(any()) }
    }

    @Test
    fun `falls back on authorization header if cookies are not available`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)
        val token = Validator.generateToken()
        UserService.createUser(User("TEST", "TEST", email = "TEST"))

        every { context.header(any()) } returns "Bearer $token:TEST"
        every { context.cookie(any<String>()) } returns null

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify { handler.handle(any()) }
    }

    @Test
    fun `discards request if JWT token is invalid`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)

        every { context.header(any()) } returns null
        every { context.cookie(Constants.USER_TOKEN.name) } returns "BAD_TOKEN"
        every { context.cookie(Constants.USER_NAME.name) } returns "BAD_USER"

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify { handler wasNot Called }
        verify { context.status(401) }
    }

    @Test
    fun `skips auth if path is under dev mode`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)
        mockkObject(Config)

        every { context.header(any()) } returns null
        every { Config.devMode } returns "1"

        assertThat(Config.devMode).isEqualTo("1")

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify { handler.handle(any()) }

        unmockkObject(Config)
    }

    @Test
    fun `skips auth if role is GUEST`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)

        every { context.header(any()) } returns null

        AuthController.accessManager(handler, context, setOf(UserRole.GUEST))

        verify { handler.handle(any()) }
    }

    @Test
    fun `skips auth if no user role is required`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)

        every { context.header(any()) } returns null

        AuthController.accessManager(handler, context, emptySet())

        verify { handler.handle(any()) }
    }

    @Test
    fun `needs auth if role is NON_ADMIN`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)

        every { context.header(any()) } returns null
        every { context.cookie(any<String>()) } answers { null }

        AuthController.accessManager(handler, context, setOf(UserRole.NON_ADMIN))

        verify { handler wasNot Called }
        verify { context.status(401) }
    }

    @Test
    fun `needs auth if role is ADMIN`() {
        val handler = mockk<Handler>(relaxUnitFun = true)
        val context = mockk<Context>(relaxed = true)

        every { context.header(any()) } returns null
        every { context.cookie(any<String>()) } answers { null }

        AuthController.accessManager(handler, context, setOf(UserRole.ADMIN))

        verify { handler wasNot Called }
        verify { context.status(401) }
    }
}
