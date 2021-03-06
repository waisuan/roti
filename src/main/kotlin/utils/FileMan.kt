package utils

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import configs.Config
import java.io.File
import java.io.InputStream
import java.lang.Exception

object FileMan {
    private var s3Client: AmazonS3? = null
    private val BUCKET_NAME = Config.s3Bucket

    fun s3Client(): AmazonS3 {
        if (s3Client == null) {
            s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(AWSStaticCredentialsProvider(
                    BasicAWSCredentials(
                        Config.s3AccessKey,
                        Config.s3SecretKey)
                ))
                .withRegion(
                    Regions.EU_WEST_2
                )
                .build()
        }
        return s3Client!!
    }

    fun getDefaultBucket(): String {
        return BUCKET_NAME
    }

    fun getObjects(filter: String): List<String> {
        return s3Client().listObjects(BUCKET_NAME).objectSummaries.filter {
            it.key.startsWith("$filter/")
        }.map {
            it.key.removePrefix("$filter/")
        }
    }

    fun getObject(objectName: String): InputStream? {
        return s3Client().getObject(BUCKET_NAME, objectName).objectContent.delegateStream
    }

    fun checkIfObjectExists(objectName: String): Boolean {
        return try {
            s3Client().getObjectMetadata(BUCKET_NAME, objectName)
            true
        } catch (e: Exception) {
            logger().info(e.message)
            false
        }
    }

    fun saveObject(objectName: String, objectBody: File) {
        s3Client().putObject(BUCKET_NAME, objectName, objectBody)
    }

    fun deleteObject(objectName: String) {
        s3Client().deleteObject(BUCKET_NAME, objectName)
    }
}
