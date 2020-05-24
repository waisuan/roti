package services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import exceptions.OversizedFileException
import exceptions.RecordNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import utils.FileMan

class FileServiceTest {
    @Test
    fun `saveFile() should be successful if file size is less than 26MB`() {
        val fileMan = mock<FileMan>()
        val fileContent = javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")

        whenever(fileMan.saveObject(any(), any())).doAnswer { Unit }

        FileService.fileMan = fileMan
        FileService.saveFile("TEST", "TEST", fileContent!!)

        verify(fileMan).saveObject(any(), any())
    }

    @Test
    fun `saveFile() should throw an error is file size is above 25MB`() {
        val fileMan = mock<FileMan>()
        val fileContent = javaClass.classLoader.getResourceAsStream("stubs/large_file.blob")

        whenever(fileMan.saveObject(any(), any())).doAnswer { Unit }

        FileService.fileMan = fileMan
        assertThatThrownBy {
            FileService.saveFile("TEST", "TEST", fileContent!!)
        }.isInstanceOf(OversizedFileException::class.java)
    }

    @Test
    fun `deleteFile() should throw an error if file does not exist`() {
        val fileMan = mock<FileMan>()
        whenever(fileMan.checkIfObjectExists(any())).thenReturn(false)

        FileService.fileMan = fileMan
        assertThatThrownBy {
            FileService.deleteFile("TEST", "TEST")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }
}
