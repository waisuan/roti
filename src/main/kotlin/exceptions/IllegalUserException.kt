package exceptions

import java.lang.Exception

class IllegalUserException(message: String = "User does not exist.") : Exception(message)
