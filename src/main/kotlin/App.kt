import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import configs.Config
import controllers.AuthController
import internal.CommandLineParser
import internal.Database
import internal.EventHandler
import internal.Routes
import io.javalin.Javalin
import tasks.TaskRunner
import utils.logger

fun main(args: Array<String>): Unit = mainBody {
    ArgParser(args).parseInto(::CommandLineParser).run {
        if (executeTask) {
            logger().info(">>> Running task: $taskName $taskArguments")
            Database.init(enableMigrations = false)
            TaskRunner.run(taskName, taskArguments)
        } else {
            RotiApp().init()
        }
    }
}

class RotiApp(private val port: Int = 7000, private val enableDB: Boolean = true) {
    fun init(): Javalin {
        val app = Javalin.create {
            it.accessManager(AuthController::accessManager)
            it.enableCorsForOrigin(Config.allowedCorsOrigin)
            it.enforceSsl = true
            it.enableWebjars()
            it.addStaticFiles("vue/static")
        }

        EventHandler.init(app)
        Routes.init(app)
        if (enableDB)
            Database.init()

        return app.start(Config.appPort ?: port)
    }
}
