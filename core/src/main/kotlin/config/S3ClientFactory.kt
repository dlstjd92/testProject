package com.inspark.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

@Configuration
open class S3ClientConfig {

    private lateinit var accessKeyId: String
    private lateinit var secretAccessKey: String

    fun setCredentials(accessKeyId: String, secretAccessKey: String) {
        this.accessKeyId = accessKeyId
        this.secretAccessKey = secretAccessKey
    }

    private var s3Client: S3AsyncClient? = null

    fun createS3Client() {
        if (!::accessKeyId.isInitialized || !::secretAccessKey.isInitialized ||
            accessKeyId.isBlank() || secretAccessKey.isBlank()
        ) {
            throw IllegalStateException("AWS Access Key ID와 Secret Access Key가 설정되지 않았습니다.")
        }
        s3Client = S3AsyncClient.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                )
            )
            .build()
    }

    fun initializeS3Client() {
        createS3Client()
    }

    fun getS3Client(): S3AsyncClient {
        return s3Client ?: throw IllegalStateException("S3 클라이언트가 아직 초기화되지 않았습니다.")
    }
}