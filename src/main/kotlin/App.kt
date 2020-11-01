import configs.Config
import controllers.AuthController
import internal.Database
import internal.EventHandler
import internal.Routes
import io.javalin.Javalin

fun main() {
    RotiApp().init()
}

class RotiApp(private val port: Int = 7000, private val enableDB: Boolean = true) {
    fun init(): Javalin {
        val app = Javalin.create {
            it.accessManager(AuthController::accessManager)
            it.enableCorsForAllOrigins()
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
