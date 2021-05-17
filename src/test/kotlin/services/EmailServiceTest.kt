package services

import com.sendgrid.Response
import com.sendgrid.SendGrid
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.verify
import models.User
import models.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class EmailServiceTest {
    @BeforeEach
    fun setUp() {
        mockkConstructor(SendGrid::class)
        every { anyConstructed<SendGrid>().api(any()) } returns Response(200, "", emptyMap())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `sendRegistrationSuccessful() should send a confirmation email out to the new user and to admin(s) for approval`() {
        mockkObject(UserService)
        every { UserService.getUsersByRole(UserRole.ADMIN) } returns listOf(User("admin", email = "admin@mail.com"))

        EmailService.sendRegistrationSuccessful(User(username = "Evan", email = "evan@mail.com"))
        verify {
            anyConstructed<SendGrid>().api(withArg { request ->
                assertThat(request.body).isIn(listOf("""
                    {"from":{"email":"noreply@roti.com"},"subject":"Registration Successful!","personalizations":[{"to":[{"email":"evan@mail.com"}]}],"content":[{"type":"text/plain","value":"Hi Evan,\n\nYour registration was successful.\nYou'll need to be approved by an admin user before being able to log in.\nDon't worry, you'll be notified through email once that happens."}]}
                """.trimIndent(), """
                    {"from":{"email":"noreply@roti.com"},"subject":"New user requires approval","personalizations":[{"to":[{"email":"admin@mail.com"}]}],"content":[{"type":"text/plain","value":"Hi admin,\n\nEvan is a new user and requires approval from you before they are able to log in."}]}
                """.trimIndent()))
            })
        }
    }

    @Test
    fun `should fail silently if email could not be sent out`() {
        every { anyConstructed<SendGrid>().api(any()) } throws Exception("No emails for you today!")
        assertDoesNotThrow { EmailService.sendRegistrationSuccessful(User(username = "Evan", email = "evan@mail.com")) }
    }
}
