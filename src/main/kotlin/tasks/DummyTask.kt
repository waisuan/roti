package tasks

import picocli.CommandLine

@CommandLine.Command(name = "dummy", mixinStandardHelpOptions = true, description = ["some task"])
class DummyTask : Task {
    @CommandLine.Option(names = ["-f", "--file"], required = false, description = ["the archive file"], defaultValue = "")
    var file: String = ""

    @CommandLine.Parameters(paramLabel = "FILE", description = ["one ore more files to archive"], split = ",")
    var files: List<String> = emptyList()

    override fun call(): Int {
        println("I was executed with $file $files")
        return 0
    }
}
