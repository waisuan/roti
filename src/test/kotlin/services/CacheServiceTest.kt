package services

import com.fiftyonred.mock_jedis.MockJedis
import configs.Config
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheServiceTest {
    @BeforeEach
    fun beforeEachTest() {
        val mockJedis = MockJedis("dummy_host")
        mockkObject(mockJedis)
        every { mockJedis.shutdown() } answers { null }

        mockkObject(Config)
        every { Config.enableCache } returns "1"

        CacheService.start(mockJedis)
    }

    @AfterEach
    fun afterEachTest() {
        CacheService.purgeCachedMachines()
        CacheService.stop()
        unmockkObject(Config)
    }

    @Test
    fun `setMachines() should save a list of machines in cache memory`() {
        val machines = listOf(Machine(serialNumber = "S001"))
        CacheService.setMachines("SOME_KEY", machines)

        assertThat(CacheService.getMachines("SOME_KEY")).isEqualTo(machines)
    }

    @Test
    fun `setMachineCount() should save the num of machines in cache memory`() {
        CacheService.setMachineCount("SOME_KEY", 10)

        assertThat(CacheService.getMachineCount("SOME_KEY")).isEqualTo(10)
    }

    @Test
    fun `getMachines() should return empty list if machines are not in cache memory`() {
        assertThat(CacheService.getMachines("SOME_KEY")).isEmpty()
    }

    @Test
    fun `getMachineCount() should return NULL if num of machines is not in cache memory`() {
        assertThat(CacheService.getMachineCount("SOME_KEY")).isNull()
    }

    @Test
    fun `isMachinesCached() should return true if machines are cached`() {
        CacheService.setMachines("SOME_KEY", listOf(Machine(serialNumber = "S001")))

        assertThat(CacheService.isMachinesCached("SOME_KEY")).isTrue()
    }

    @Test
    fun `isMachinesCached() should return false if machines are not cached`() {
        assertThat(CacheService.isMachinesCached("SOME_KEY")).isFalse()
    }

    @Test
    fun `purgeCachedMachines() should remove all cached machines from cache memory`() {
        CacheService.setMachines("SOME_KEY_1", listOf(Machine(serialNumber = "S001")))
        CacheService.setMachineCount("SOME_KEY_2", 10)

        assertThat(CacheService.isMachinesCached("SOME_KEY_1")).isTrue()
        assertThat(CacheService.isMachinesCached("SOME_KEY_2")).isTrue()

        CacheService.purgeCachedMachines()

        assertThat(CacheService.isMachinesCached("SOME_KEY_1")).isFalse()
        assertThat(CacheService.isMachinesCached("SOME_KEY_2")).isFalse()
    }

    @Test
    fun `isCacheActive() returns true if jedis is defined and active`() {
        assertThat(CacheService.isCacheActive()).isTrue()
    }

    @Test
    fun `isCacheActive() returns false if jedis is defined but not active`() {
        CacheService.stop()

        val mockJedis = MockJedis("dummy_host")
        mockkObject(mockJedis)
        every { mockJedis.ping() } throws(Exception("Bad stuff happened here..."))

        CacheService.start(mockJedis)

        assertThat(CacheService.isCacheActive()).isFalse()
    }

    @Test
    fun `isCacheActive() returns false if jedis is not defined`() {
        CacheService.stop()
        assertThat(CacheService.isCacheActive()).isFalse()
    }

    @Test
    fun `cacheIsEnabled() returns true if ENABLE_CACHE env var is defined`() {
        assertThat(CacheService.cacheIsEnabled()).isTrue()
    }

    @Test
    fun `cacheIsEnabled() returns false if ENABLE_CACHE env var is not defined`() {
        mockkObject(Config)

        every { Config.enableCache } returns("0")
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        every { Config.enableCache } returns("true")
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        every { Config.enableCache } returns(null)
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        unmockkObject(Config)
    }

    @Test
    fun `cache() returns cache instance if avail and active`() {
        assertThat(CacheService.cache()).isNotNull
    }

    @Test
    fun `cache() returns NULL if cache instance is not avail or is inactive`() {
        CacheService.stop()
        assertThat(CacheService.cache()).isNull()
    }
}
