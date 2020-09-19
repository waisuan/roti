package services

import com.fiftyonred.mock_jedis.MockJedis
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import models.Machine
import org.assertj.core.api.Assertions.assertThat
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheServiceTest {
    @BeforeAll
    fun setup() {
        mockkObject(CacheService)
        every { CacheService.cache() } returns MockJedis("dummy_host")
    }

    @AfterAll
    fun tearDown() {
        unmockkObject(CacheService)
    }

    @AfterEach
    fun cleanUp() {
        CacheService.purgeCachedMachines()
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

    // FIXME: These tests don't work :(
    // @Test
    // fun `isCacheActive() returns true if jedis is defined and active`() {
    //     EnvironmentVariables().set("ENABLE_CACHE", "1")
    //
    //     mockkConstructor(Jedis::class)
    //     every { anyConstructed<Jedis>().ping() } returns null
    //
    //     CacheService.start()
    //     verify { anyConstructed<Jedis>().ping() }
    //     assertThat(CacheService.isCacheActive()).isTrue()
    //
    //     unmockkConstructor(Jedis::class)
    //     EnvironmentVariables().set("ENABLE_CACHE", null)
    // }
    //
    // @Test
    // fun `isCacheActive() returns false if jedis is defined but not active`() {
    //     EnvironmentVariables().set("ENABLE_CACHE", "1")
    //
    //     mockkConstructor(Jedis::class)
    //     every { anyConstructed<Jedis>().ping() } throws Exception("I'm not alive")
    //
    //     CacheService.start()
    //     verify { anyConstructed<Jedis>().ping() }
    //     assertThat(CacheService.isCacheActive()).isFalse()
    //
    //     unmockkConstructor(Jedis::class)
    //     EnvironmentVariables().set("ENABLE_CACHE", null)
    // }

    @Test
    fun `isCacheActive() returns false if jedis is not defined`() {
        assertThat(CacheService.isCacheActive()).isFalse()
    }

    @Test
    fun `cacheIsEnabled() returns true if ENABLE_CACHE env var is defined`() {
        EnvironmentVariables().set("ENABLE_CACHE", "1")

        assertThat(CacheService.cacheIsEnabled()).isTrue()

        EnvironmentVariables().set("ENABLE_CACHE", null)
    }

    @Test
    fun `cacheIsEnabled() returns false if ENABLE_CACHE env var is not defined`() {
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        EnvironmentVariables().set("ENABLE_CACHE", "0")
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        EnvironmentVariables().set("ENABLE_CACHE", "true")
        assertThat(CacheService.cacheIsEnabled()).isFalse()

        EnvironmentVariables().set("ENABLE_CACHE", null)
        assertThat(CacheService.cacheIsEnabled()).isFalse()
    }
}
