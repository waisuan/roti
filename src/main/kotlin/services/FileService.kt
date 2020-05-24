package services

import exceptions.OversizedFileException
import exceptions.RecordNotFoundException
import java.io.File
import java.io.InputStream
import java.util.UUID
import org.apache.commons.io.FileUtils
import utils.FileMan

object FileService {
    var fileMan: FileMan = FileMan

    fun getFileNames(dir: String): List<String> {
        return fileMan.getObjects(dir)
    }

    fun getFile(dir: String, fileName: String): InputStream {
        return fileMan.getObject("$dir/$fileName") ?: throw RecordNotFoundException()
    }

    fun saveFile(dir: String, fileName: String, fileContent: InputStream) {
        val tmpFile = File.createTempFile(fileName, UUID.randomUUID().toString())
        try {
            FileUtils.copyInputStreamToFile(fileContent, tmpFile)
            if (isOversizedFile(tmpFile)) {
                throw OversizedFileException()
            }
            fileMan.saveObject("$dir/$fileName", tmpFile)
        } finally {
            if (tmpFile.exists())
                tmpFile.delete()
        }
    }
    fun deleteFile(dir: String, fileName: String) {
        val fullName = "$dir/$fileName"
        // if (!fileMan.checkIfObjectExists(fullName))
        //     throw RecordNotFoundException()
        fileMan.deleteObject(fullName)
    }

    private fun isOversizedFile(file: File): Boolean {
        return ((file.length() / 1024 / 1024) > 25) // fileSize in MB
    }
}
