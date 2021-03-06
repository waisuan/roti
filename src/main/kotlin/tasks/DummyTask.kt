package tasks

import picocli.CommandLine

@CommandLine.Command(name = "dummy", mixinStandardHelpOptions = true, description = ["some task"])
class DummyTask : Task {
    @CommandLine.Option(names = ["-f", "--file"], required = false, defaultValue = "")
    var file: String = ""

    @CommandLine.Parameters(paramLabel = "FILE", split = ",")
    var files: List<String> = emptyList()

    override fun call(): Int {
        println("This is just a dummy task >>> $file $files")
        return 0
    }
}
