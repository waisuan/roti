package services

import com.sendgrid.Content
import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import configs.Config
import models.User
import utils.logger

object EmailService {
    // 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

    fun sendRegistrationSuccessful(newUser: User) {
        send(to = newUser.email!!, subject = "Registration Successful!", body = """
            Hi ${newUser.username},
            
            Your registration was successful.
            You'll need to be approved by an admin user before being able to log in.
            Don't worry, you'll be notified through email once that happens.
        """.trimIndent())
    }

    private fun send(from: String = "noreply@roti.com", to: String, subject: String, body: String) {
        val request = Request().also {
            it.method = Method.POST
            it.endpoint = "mail/send"
            it.body = Mail(Email(from), subject, Email(to), Content("text/plain", body)).build()
        }
        val response = SendGrid(Config.sendGridApiKey).api(request)
        logger().info("${response.statusCode} - ${response.body}")
    }
}
