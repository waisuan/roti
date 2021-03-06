package tasks

import exceptions.UnrecognizedTaskException
import java.util.concurrent.Callable
import org.reflections.Reflections
import picocli.CommandLine
import utils.logger

object TaskRunner {
    fun run(taskName: String, args: String?) {
        runCatching {
            Reflections(javaClass.`package`.name).getSubTypesOf(Task::class.java)
                .filter { (it.annotations.first() as CommandLine.Command).name == taskName }
                .let {
                    require(it.size == 1)
                    CommandLine(Class.forName(it.first().name)).let { command ->
                        if (args != null && args.isNotEmpty()) {
                            command.execute(*args.split("\\s+".toRegex()).toTypedArray())
                        } else
                            command.execute()
                    }
                }
        }.onFailure {
            logger().error(it.stackTraceToString())
            throw UnrecognizedTaskException()
        }
    }
}

interface Task : Callable<Int>
