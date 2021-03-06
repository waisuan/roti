package tasks

import picocli.CommandLine
import services.UserService
import utils.logger

@CommandLine.Command(name = "purge_rejected_users", mixinStandardHelpOptions = true, description = ["delete rejected users from DB"])
class PurgeRejectedUsersTask : Task {
    override fun call(): Int {
        UserService.getRejectedUsers().forEach {
            logger().info("Deleting: $it")
            UserService.deleteUser(it.username!!)
        }
        return 0
    }
}
