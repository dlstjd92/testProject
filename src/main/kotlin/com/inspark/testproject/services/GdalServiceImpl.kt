package com.inspark.testproject.services

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Files
import java.nio.file.Path

@Service
class GdalServiceImpl(
    private val s3Client: S3AsyncClient
) : GdalService {
    private fun downloadFileFromS3(bucket: String, key: String, localPath: Path) {
        val request = GetObjectRequest.builder().bucket(bucket).key(key).build()
        s3Client.getObject(request, localPath).get() // 블로킹
    }

    fun convertToCOG(inputPath: Path, outputPath: Path): Boolean {
        val process = ProcessBuilder("gdal_translate", "-of", "COG", inputPath.toString(), outputPath.toString())
            .inheritIO().start()
        return process.waitFor() == 0
    }

    fun uploadFileToS3(bucket: String, key: String, localPath: Path) {
        val request = PutObjectRequest.builder().bucket(bucket).key(key).build()
        s3Client.putObject(request, localPath).get()
    }

    fun extractMetadata(file: Path) {
        try {
            val process = ProcessBuilder("gdalinfo", file.toString())
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            println("[i] Metadata:\n$output")
        } catch (e: Exception) {
            println("[!] 메타데이터 추출 실패: ${e.message}")
        }
    }

    override fun process(bucketIn: String, keyIn: String, bucketOut: String, userPrefix: String): Boolean {
        val localInput = Files.createTempFile("input_", ".tif")
        val localOutput = Files.createTempFile("output_", ".tif")
        downloadFileFromS3(bucketIn, keyIn, localInput)

        val startTime = System.currentTimeMillis()
        val success = convertToCOG(localInput, localOutput)
        val duration = (System.currentTimeMillis() - startTime) / 1000.0
        println("[⏱] 변환 소요 시간: ${duration}초")

        if (success) {
            extractMetadata(localOutput)
            // Desktop file naming and collision handling
            val baseName = keyIn.substringBeforeLast(".")
            val targetDir = Path.of(System.getProperty("user.home"), "Desktop", userPrefix)
            Files.createDirectories(targetDir)
            var outputName = "${baseName}_to_cog.tiff"
            val outputPath = targetDir.resolve(outputName)
            var suffix = 1
            while (Files.exists(outputPath)) {
                outputName = "${baseName}_to_cog_$suffix.tiff"
                suffix++
            }
            val finalPath = targetDir.resolve(outputName)
            // For testing: skip upload and retain the file locally
            // uploadFileToS3(bucketOut, outputKey, localOutput)

            // Copy the file to the user's Desktop - For test
            try {
                Files.copy(localOutput, finalPath)
                println("[📁] 바탕화면에 복사 완료: $finalPath")
            } catch (e: Exception) {
                println("[!] 바탕화면 복사 실패: ${e.message}")
            }
        }

        localInput.toFile().delete()
        localOutput.toFile().delete()
        return success
    }
}