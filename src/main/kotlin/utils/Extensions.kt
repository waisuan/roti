package utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

inline fun <reified T : Any> T.logger(): Logger = getLogger(T::class.java)

fun parseJsonString(json: String): Map<String, Any> = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
