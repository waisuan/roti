package exceptions

import java.lang.Exception

class UnapprovedUserException(message: String = "User has not been approved.") : Exception(message)
