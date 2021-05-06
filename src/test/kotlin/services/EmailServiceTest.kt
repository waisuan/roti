package services

import com.sendgrid.Response
import com.sendgrid.SendGrid
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.verify
import models.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    fun `sendRegistrationSuccessful() should send an email out to the given new user`() {
        val newUser = User(username = "Evan", email = "evan@mail.com")
        EmailService.sendRegistrationSuccessful(newUser)
        verify { anyConstructed<SendGrid>().api(any()) }
    }
}
