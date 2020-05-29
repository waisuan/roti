package exceptions

import java.lang.Exception

class RecordAlreadyExistsException(message: String = "Record already exists.") : Exception(message)
