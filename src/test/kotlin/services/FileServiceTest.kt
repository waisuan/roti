package services

import exceptions.OversizedFileException
import exceptions.RecordNotFoundException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.io.File
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.FileMan

class FileServiceTest {
    @BeforeEach
    fun setup() {
        mockkObject(FileMan)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(FileMan)
        clearAllMocks()
    }

    @Test
    fun `saveFile() should be successful if file size is less than 26MB`() {
        val fileContent = javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")

        every { FileMan.getObjects(any()) } returns(emptyList())
        every { FileMan.saveObject(any(), any()) } returns(Unit)

        FileService.saveFile("TEST", "TEST", fileContent!!)

        verify { FileMan.saveObject("TEST/TEST", any<File>()) }
    }

    @Test
    fun `saveFile() should delete files in given directory before saving`() {
        val fileContent = javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")

        every { FileMan.getObjects(any()) } returns(listOf("SOME_FILE"))
        every { FileMan.checkIfObjectExists(any()) } returns(true)
        every { FileMan.deleteObject(any()) } returns(Unit)
        every { FileMan.saveObject(any(), any()) } returns(Unit)

        FileService.saveFile("TEST", "TEST", fileContent!!)

        verify { FileMan.deleteObject("TEST/SOME_FILE") }
        verify { FileMan.saveObject("TEST/TEST", any<File>()) }
    }

    @Test
    fun `saveFile() should throw an error is file size is above 25MB`() {
        val fileContent = javaClass.classLoader.getResourceAsStream("stubs/large_file.blob")

        every { FileMan.saveObject(any(), any()) } returns(Unit)

        assertThatThrownBy {
            FileService.saveFile("TEST", "TEST", fileContent!!)
        }.isInstanceOf(OversizedFileException::class.java)
    }

    @Test
    fun `deleteFile() should delete file in the given directory`() {
        every { FileMan.checkIfObjectExists(any()) } returns(true)
        every { FileMan.deleteObject(any()) } returns(Unit)

        FileService.deleteFile("TEST", "TEST")

        verify { FileMan.deleteObject("TEST/TEST") }
    }

    @Test
    fun `deleteFile() should throw an error if file does not exist`() {
        every { FileMan.checkIfObjectExists(any()) } returns(false)

        assertThatThrownBy {
            FileService.deleteFile("TEST", "TEST")
        }.isInstanceOf(RecordNotFoundException::class.java)
    }

    @Test
    fun `deleteFiles() should delete all files in a given directory`() {
        every { FileMan.checkIfObjectExists(any()) } returns(true)
        every { FileMan.deleteObject(any()) } returns(Unit)
        every { FileMan.getObjects(any()) } returns(listOf("FileA", "FileB", "FileC"))

        FileService.deleteFiles("SOME_DIR")

        verify(exactly = 3) {
            FileMan.deleteObject(any())
        }
        verify {
            FileMan.deleteObject("SOME_DIR/FileA")
        }
        verify {
            FileMan.deleteObject("SOME_DIR/FileB")
        }
        verify {
            FileMan.deleteObject("SOME_DIR/FileC")
        }
    }

    @Test
    fun `deleteFiles() should swallow any errors during deletion process`() {
        every { FileMan.checkIfObjectExists(any()) } returns(true)
        every { FileMan.deleteObject(any()) } throws Exception("Bad stuff happened.")
        every { FileMan.getObjects(any()) } returns(listOf("FileA", "FileB", "FileC"))

        FileService.deleteFiles("SOME_DIR")
    }
}
