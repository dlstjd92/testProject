package com.inspark.testproject.services

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.test.assertTrue

class GdalServiceImplUnitTest {

    private lateinit var s3Client: S3AsyncClient
    private lateinit var service: GdalServiceImpl

    @BeforeEach
    fun setup() {
        s3Client = mockk()
        service = spyk(GdalServiceImpl(s3Client))
    }

    @Test
    fun `should process a GeoTIFF successfully`() {
        // Arrange
        every {
            s3Client.getObject(any<GetObjectRequest>(), any<Path>())
        } returns CompletableFuture.completedFuture(mockk<GetObjectResponse>())

        every {
            s3Client.putObject(any<PutObjectRequest>(), any<Path>())
        } returns CompletableFuture.completedFuture(mockk<PutObjectResponse>())

        every { service.convertToCOG(any(), any()) } returns true
        justRun { service.extractMetadata(any()) }

        // Act
        val result = service.process("bucketIn", "file.tif", "bucketOut", "user")

        // Assert
        assertTrue(result)
    }
}