package tasks

import picocli.CommandLine
import services.UserService

@CommandLine.Command(name = "purge_rejected_users", mixinStandardHelpOptions = true, description = ["delete rejected users from DB"])
class PurgeRejectedUsersTask : Task {
    override fun call(): Int {
        UserService.getRejectedUsers().forEach {
            println("Deleting: $it")
            UserService.deleteUser(it.username!!)
        }
        return 0
    }
}
