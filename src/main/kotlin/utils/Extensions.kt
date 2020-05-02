package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

inline fun <reified T : Any> T.logger(): Logger = getLogger(T::class.java)
