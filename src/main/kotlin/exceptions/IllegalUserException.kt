package exceptions

import java.lang.Exception

class IllegalUserException(message: String = "Incorrect username/password detected.") : Exception(message)
