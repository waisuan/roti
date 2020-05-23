package exceptions

import java.lang.Exception

class OversizedFileException(
    message: String = "Provided file is too large to upload. It must be less than 10MB."
) : Exception(message)
