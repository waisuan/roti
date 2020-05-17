package exceptions

import java.lang.Exception

class RecordNotFoundException(message: String = "Unable to locate record.") : Exception(message)
