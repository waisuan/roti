package exceptions

import java.lang.Exception

class BadFileUploadException(message: String = "Unable to upload provided file.") : Exception(message)
