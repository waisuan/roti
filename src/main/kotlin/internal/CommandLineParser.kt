package internal

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class CommandLineParser(parser: ArgParser) {
    val executeTask by parser.flagging("-e", "--execute", help = "trigger a CLI task")
    val taskName by parser.positional("TASK_NAME", help = "the name of the task to run")
        .default("")
    val taskArguments by parser.storing("-A", "--arguments", help = "any input arguments for the task")
        .default("")
}
