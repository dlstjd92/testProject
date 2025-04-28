package com.inspark.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request

@Service
class S3ListServiceImpl(
    private val s3ClientConfig: com.inspark.config.S3ClientConfig
) : S3ListService {

    private val s3Client: S3AsyncClient
        get() = s3ClientConfig.getS3Client()

    override fun listObjectsInBucket(bucket: String, prefix: String): List<String> {
        val builder = ListObjectsV2Request.builder()
            .bucket(bucket)
        if (prefix.isNotBlank()) {
            builder.prefix(prefix)
        }
        val request = builder.build()

        val response = s3Client.listObjectsV2(request).get()
        return response.contents().map { it.key() }
    }
}