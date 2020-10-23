package api

import RotiApp
import com.adobe.testing.s3mock.junit5.S3MockExtension
import configs.Config
import exceptions.RecordNotFoundException
import helpers.TestDatabase
import io.javalin.Javalin
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kong.unirest.Unirest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import services.FileService
import utils.FileMan

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilesAPITest {
    @RegisterExtension
    @JvmField
    val s3Mock: S3MockExtension = S3MockExtension.builder().silent().withSecureConnection(false).build()

    private lateinit var app: Javalin

    @BeforeAll
    fun setup() {
        app = RotiApp(port = 8000, enableDB = false).init()
        Unirest.config().defaultBaseUrl("http://localhost:8000/api")

        val s3Client = s3Mock.createS3Client()
        s3Client.createBucket(FileMan.getDefaultBucket())

        mockkObject(FileMan)
        every { FileMan.s3Client() } returns(s3Client)

        mockkObject(Config)
        every { Config.devMode } returns "1"
    }

    @AfterAll
    fun tearDown() {
        app.stop()

        unmockkObject(FileMan)
        unmockkObject(Config)
        clearAllMocks()

        Thread.sleep(5_000) // Hack to allow the web server to properly shutdown before continuing on with the test suite.
    }

    @BeforeEach
    fun beforeEachTest() {
        TestDatabase.init()
    }

    @AfterEach
    fun afterEachTest() {
        TestDatabase.purge()
    }

    @Test
    fun `GET files`() {
        var response = Unirest.get("/files/TEST").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("[]")

        FileService.saveFile("TEST", "small_file.blob", javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")!!)
        response = Unirest.get("/files/TEST").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body as String).contains("small_file.blob")
    }

    @Test
    fun `POST files`() {
        var response = Unirest.post("/files/TEST")
            .field("file", javaClass.classLoader.getResourceAsStream("stubs/small_file.blob"), "small_file.blob")
            .asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThat(FileService.getFileNames("TEST").firstOrNull()).isEqualTo("small_file.blob")

        response = Unirest.post("/files/TEST")
            .field("file", javaClass.classLoader.getResourceAsStream("stubs/large_file.blob"), "large_file.blob")
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Provided file is too large to upload")

        response = Unirest.post("/files/TEST")
            .field("dude", javaClass.classLoader.getResourceAsStream("stubs/large_file.blob"), "large_file.blob")
            .asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to upload provided file")
    }

    @Test
    fun `GET a file`() {
        FileService.saveFile("TEST", "small_file.blob", javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")!!)
        var response = Unirest.get("/files/TEST/small_file.blob").asString()
        assertThat(response.status).isEqualTo(200)
        assertThat(response.headers.get("Content-Disposition"))
            .contains("attachment; filename=\"small_file.blob\"")

        response = Unirest.get("/files/TEST/some_file.blob").asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to locate record")
    }

    @Test
    fun `DELETE a file`() {
        FileService.saveFile("TEST", "small_file.blob", javaClass.classLoader.getResourceAsStream("stubs/small_file.blob")!!)
        assertThat(FileService.getFile("TEST", "small_file.blob")).isNotNull()

        var response = Unirest.delete("/files/TEST/small_file.blob").asEmpty()
        assertThat(response.status).isEqualTo(200)
        assertThatThrownBy {
            FileService.getFile("TEST", "small_file.blob")
        }.isInstanceOf(RecordNotFoundException::class.java)

        response = Unirest.delete("/files/TEST/some_file.blob").asString()
        assertThat(response.status).isEqualTo(500)
        assertThat(response.body as String).contains("Unable to locate record")
    }
}
