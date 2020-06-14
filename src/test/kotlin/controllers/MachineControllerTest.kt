package controllers

import helpers.TestDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MachineControllerTest {
    @BeforeEach
    fun setup() {
        TestDatabase.init()
    }

    @AfterEach
    fun tearDown() {
        Mockito.framework().clearInlineMocks()
        TestDatabase.purge()
    }

    // @Test
    // fun `getAllMachines() should accept & handle query params`() {
    //
    // }
}