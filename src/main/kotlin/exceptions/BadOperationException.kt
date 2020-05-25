package exceptions

import java.lang.Exception

class BadOperationException(recordType: String) :
    Exception("Unable to operate on $recordType record")
