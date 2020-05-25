package utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.simple.SimpleLogger

class ExtensionsTest {
    @Test
    fun `logger() returns a valid Logger object`() {
        assertThat(logger()).isNotNull()
        assertThat(logger()::class.java).isEqualTo(SimpleLogger::class.java)
    }
}
