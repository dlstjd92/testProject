package com.inspark.config


import org.springframework.context.annotation.Configuration

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager

@Configuration
open class S3ClientConfig {

    private lateinit var accessKeyId: String
    private lateinit var secretAccessKey: String

    private var s3Client: S3AsyncClient? = null

    private var transferManager: S3TransferManager? = null

    fun setCredentials(accessKeyId: String, secretAccessKey: String) {
        this.accessKeyId = accessKeyId
        this.secretAccessKey = secretAccessKey
        createS3Client()

    }

    private fun createS3Client() {
        if (!::accessKeyId.isInitialized || !::secretAccessKey.isInitialized ||
            accessKeyId.isBlank() || secretAccessKey.isBlank()
        ) {
            throw IllegalStateException("AWS Access Key ID와 Secret Access Key가 설정되지 않았습니다.")
        }



        // crtBuilder 기반 생성
        val client = S3AsyncClient.crtBuilder()
            .region(Region.AP_NORTHEAST_2)
            .minimumPartSizeInBytes(8 * 1024 * 1024)
            .maxConcurrency(64)
            .targetThroughputInGbps(20.0)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                )
            )
            .build()


//        s3Client = client

        // TransferManager 생성
        transferManager = S3TransferManager.builder()
            .s3Client(client)
            .build()

        // netty기반 s3조회용 자격증명
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

    fun getTransferManager(): S3TransferManager {
        return transferManager ?: throw IllegalStateException("S3 클라이언트가 아직 초기화되지 않았습니다.")
    }
}