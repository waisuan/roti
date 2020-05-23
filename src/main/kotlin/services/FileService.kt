package services

import exceptions.OversizedFileException
import exceptions.RecordNotFoundException
import java.io.File
import java.io.InputStream
import java.util.UUID
import org.apache.commons.io.FileUtils
import utils.FileMan

object FileService {
    fun getFile(dir: String, fileName: String): InputStream {
        return FileMan.getObject("$dir/$fileName") ?: throw RecordNotFoundException()
    }

    fun saveFile(dir: String, fileName: String, fileContent: InputStream) {
        val tmpFile = File.createTempFile(fileName, UUID.randomUUID().toString())
        try {
            FileUtils.copyInputStreamToFile(fileContent, tmpFile)
            if ((tmpFile.length() / 1024 / 1024) > 25) { // fileSize in MB
                throw OversizedFileException()
            }
            FileMan.saveObject("$dir/$fileName", tmpFile)
        } finally {
            if (tmpFile.exists())
                tmpFile.delete()
        }
    }
}
