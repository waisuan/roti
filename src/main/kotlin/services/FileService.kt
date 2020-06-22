package services

import exceptions.OversizedFileException
import exceptions.RecordNotFoundException
import java.io.File
import java.io.InputStream
import java.util.UUID
import org.apache.commons.io.FileUtils
import utils.FileMan

object FileService {
    fun getFileNames(dir: String): List<String> {
        return FileMan.getObjects(dir)
    }

    fun getFile(dir: String, fileName: String): InputStream {
        val fullName = "$dir/$fileName"
        if (!FileMan.checkIfObjectExists(fullName))
            throw RecordNotFoundException()
        return FileMan.getObject("$dir/$fileName")!!
    }

    fun saveFile(dir: String, fileName: String, fileContent: InputStream) {
        val tmpFile = File.createTempFile(fileName, UUID.randomUUID().toString())
        try {
            FileUtils.copyInputStreamToFile(fileContent, tmpFile)
            if (isOversizedFile(tmpFile)) {
                throw OversizedFileException()
            }
            FileMan.saveObject("$dir/$fileName", tmpFile)
        } finally {
            if (tmpFile.exists())
                tmpFile.delete()
        }
    }

    fun deleteFile(dir: String, fileName: String) {
        val fullName = "$dir/$fileName"
        if (!FileMan.checkIfObjectExists(fullName))
            throw RecordNotFoundException()
        FileMan.deleteObject(fullName)
    }

    private fun isOversizedFile(file: File): Boolean {
        return ((file.length() / 1024 / 1024) > 25) // fileSize in MB
    }
}
