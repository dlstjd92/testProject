package com.inspark.testproject.services

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.S3Object
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class S3ListServiceTest {

    private lateinit var s3Client: S3AsyncClient
    private lateinit var s3ListService: S3ListServiceImpl

    @BeforeEach
    fun setup() {
        s3Client = mockk()
        s3ListService = S3ListServiceImpl(s3Client)
    }

    @Test
    fun `should return list of object keys from bucket`() {
        // Given
        val bucketName = "test-bucket"
        val objects = listOf(
            S3Object.builder().key("file1.tif").build(),
            S3Object.builder().key("folder/file2.tif").build()
        )
        val response = ListObjectsV2Response.builder()
            .contents(objects)
            .build()

        every {
            s3Client.listObjectsV2(any<ListObjectsV2Request>())
        } returns CompletableFuture.completedFuture(response)

        // When
        val result = s3ListService.listObjectsInBucket(bucketName)

        // Then
        assertEquals(listOf("file1.tif", "folder/file2.tif"), result)
    }
}