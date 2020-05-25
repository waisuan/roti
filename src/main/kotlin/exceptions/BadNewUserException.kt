package exceptions

import java.lang.Exception

class BadNewUserException(message: String = "Unable to create new user.") : Exception(message)
