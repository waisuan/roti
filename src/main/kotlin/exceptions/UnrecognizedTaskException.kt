package exceptions

import java.lang.Exception

class UnrecognizedTaskException(message: String = "Unable to run task because it's not recognized.") : Exception(message)
