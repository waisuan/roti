package services

import com.sendgrid.Content
import com.sendgrid.Email
import com.sendgrid.Mail
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.SendGrid
import configs.Config
import models.User
import models.UserRole
import utils.logger

object EmailService {
    fun sendRegistrationSuccessful(newUser: User) {
        send(to = newUser.email!!, subject = "Registration Successful!", body = """
            Hi ${newUser.username},
            
            Your registration was successful.
            You'll need to be approved by an admin user before being able to log in.
            Don't worry, you'll be notified through email once that happens.
        """.trimIndent())

        UserService.getUsersByRole(UserRole.ADMIN).forEach { adminUser ->
            send(to = adminUser.email!!, subject = "New user requires approval", body = """
                Hi ${adminUser.username},
                
                ${newUser.username} is a new user and requires approval from you before they are able to log in.
            """.trimIndent())
        }
    }

    fun sendUserApprovalStatus(username: String, approveFlag: Boolean) {
        UserService.getUser(username)!!.let { user ->
            send(to = user.email!!, subject = "Your account has been updated", body = """
                Hi ${user.username},
            
                Your account's approval status has been updated to: $approveFlag
            """.trimIndent())
        }
    }

    private fun send(from: String = Config.sysEmailAddress, to: String, subject: String, body: String) {
        if (!Config.enableEmail) {
            logger().warn("Sending of emails is currently DISABLED.")
            return
        }
        runCatching {
            val request = Request().also {
                it.method = Method.POST
                it.endpoint = "mail/send"
                it.body = Mail(Email(from), subject, Email(to), Content("text/plain", body)).build()
            }
            val response = SendGrid(Config.sendGridApiKey).api(request)
            logger().info("${response.statusCode} - Email successfully sent to $to ${response.body}")
        }.onFailure {
            logger().error("Unable to send email: ${it.message}")
        }
    }
}
