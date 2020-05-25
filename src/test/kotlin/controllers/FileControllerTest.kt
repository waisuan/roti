package controllers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import exceptions.BadFileUploadException
import io.javalin.http.Context
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.InputStream

class FileControllerTest {
    @AfterEach
    fun clearMocks() {
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `saveFile() throws an exception if uploaded file is missing`() {
        val context = mock<Context>()

        whenever(context.uploadedFile(any())).doAnswer { null }

        assertThatThrownBy {
            FileController.saveFile(context)
        }.isInstanceOf(BadFileUploadException::class.java)
    }
}
