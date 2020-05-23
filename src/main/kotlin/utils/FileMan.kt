package utils

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import java.io.File
import java.io.InputStream

object FileMan {
    private val s3Client = AmazonS3ClientBuilder
        .standard()
        .withCredentials(AWSStaticCredentialsProvider(
            BasicAWSCredentials(
                System.getenv("S3_ACCESS_KEY"),
                System.getenv("S3_SECRET_KEY"))
        ))
        .withRegion(
            Regions.EU_WEST_2
        )
        .build()
    private const val BUCKET_NAME = "roti-api"

    fun getObject(objectName: String): InputStream? {
        return s3Client.getObject(BUCKET_NAME, objectName).objectContent.delegateStream
    }

    fun saveObject(objectName: String, objectBody: File) {
        s3Client.putObject(BUCKET_NAME, objectName, objectBody)
    }
}
