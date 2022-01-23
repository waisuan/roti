package configs

object Config {
    val redisUrl = System.getenv("REDIS_URL") ?: "redis://localhost:6379"
    val appPort = System.getenv("PORT")?.toInt()
    val dbUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost/roti"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPwd = System.getenv("DB_PWD") ?: "password"
    val testDbUrl = System.getenv("TEST_DB_URL") ?: "jdbc:postgresql://localhost/roti_test"
    val testDbUser = System.getenv("DB_USER") ?: "postgres"
    val testDbPwd = System.getenv("DB_PWD") ?: "password"
    val devMode: String? = System.getenv("DEV_MODE")
    val enableCache: String? = System.getenv("ENABLE_CACHE")
    val s3AccessKey = System.getenv("S3_ACCESS_KEY") ?: ""
    val s3SecretKey = System.getenv("S3_SECRET_KEY") ?: ""
    val s3Bucket = System.getenv("S3_BUCKET") ?: "dummy-bucket"
    val jwtSecret = System.getenv("JWT_SECRET") ?: "dummy_secret"
    val allowedCorsOrigin = System.getenv("ALLOWED_CORS_ORIGIN") ?: ""
    val sendGridApiKey = System.getenv("SENDGRID_API_KEY") ?: "secret"
    val sysEmailAddress = System.getenv("SYS_EMAIL_ADDRESS") ?: "noreply@roti.com"
    val enableEmail = System.getenv("ENABLE_EMAIL")?.toBoolean() ?: false
    val maxNumOfRegisteredUsers = System.getenv("MAX_REG_USERS")?.toLong()
}
