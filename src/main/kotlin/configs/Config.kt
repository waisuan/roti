package configs

object Config {
    val redisUrl = System.getenv("REDIS_URL") ?: "redis://localhost:6379"
    val appPort = System.getenv("PORT")?.toInt()
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost/roti"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPwd = System.getenv("DB_PWD") ?: "password"
    val devMode: String? = System.getenv("DEV_MODE")
    val enableCache: String? = System.getenv("ENABLE_CACHE")
    val s3AccessKey = System.getenv("S3_ACCESS_KEY") ?: ""
    val s3SecretKey = System.getenv("S3_SECRET_KEY") ?: ""
}
