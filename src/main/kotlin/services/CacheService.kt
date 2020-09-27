package services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import configs.Config
import java.lang.Exception
import java.net.URI
import models.Machine
import redis.clients.jedis.Jedis
import utils.logger

object CacheService {
    private var jedis: Jedis? = null
    private val gson: Gson = Gson()

    private const val CACHED_MACHINES = "cached_machines"

    fun start(cache: Jedis? = null) {
        if (jedis == null && cacheIsEnabled())
            jedis = cache ?: Jedis(URI(Config.redisUrl))
    }

    fun stop() {
        cache()?.shutdown()
        jedis = null
    }

    fun setMachines(key: String, machines: List<Machine>) {
        cache()?.hset(CACHED_MACHINES, key, gson.toJson(machines)) ?: warning()
    }

    fun setMachineCount(key: String, count: Long) {
        cache()?.hset(CACHED_MACHINES, key, count.toString()) ?: warning()
    }

    fun isMachinesCached(key: String): Boolean {
        return cache()?.hexists(CACHED_MACHINES, key) ?: false
    }

    fun getMachines(key: String): List<Machine> {
        if (!isMachinesCached(key)) {
            logMiss(key)
            return emptyList()
        }

        logHit(key)
        return cache()!!.hget(CACHED_MACHINES, key).let { cachedMachines ->
            gson.fromJson<List<Machine>>(cachedMachines, object : TypeToken<List<Machine>>() {}.type)
        }
    }

    fun getMachineCount(key: String): Long? {
        if (!isMachinesCached(key)) {
            logMiss(key)
            return null
        }

        logHit(key)
        return cache()!!.hget(CACHED_MACHINES, key).toLong()
    }

    fun purgeCachedMachines() {
        cache()?.del(CACHED_MACHINES) ?: warning()
    }

    fun cache(): Jedis? {
        return jedis.takeIf { isCacheActive() }
    }

    fun isCacheActive(): Boolean {
        if (jedis == null)
            return false
        try {
            jedis!!.ping()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun cacheIsEnabled(): Boolean {
        return Config.enableCache?.equals("1") ?: false
    }

    private fun logMiss(key: String) {
        logger().info("Cache MISS: $key")
    }

    private fun logHit(key: String) {
        logger().info("Cache HIT: $key")
    }

    private fun warning() {
        logger().warn("Cache is not alive. Therefore, nothing is cached.")
    }
}
