package com.inspark.testproject.services

import reactor.core.publisher.Mono
import com.inspark.testproject.repositories.GeoTiffMetadataRepository
import com.inspark.testproject.repositories.RawGeoTiffRepository
import reactor.core.scheduler.Schedulers

import java.time.LocalDateTime

import org.springframework.stereotype.Service
import com.inspark.testproject.domain.GeoTiffMetadata
import com.inspark.testproject.domain.RawGeoTiff
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@Service
class GdalServiceImpl(
    private val s3Client: S3AsyncClient,
    private val metadataRepository: GeoTiffMetadataRepository,
    private val rawGeoTiffRepository: RawGeoTiffRepository
) : GdalService {
    // Initialize crtClient, listener, and transferManager once for reuse
    private val crtClient = S3AsyncClient.crtBuilder()
        .region(software.amazon.awssdk.regions.Region.AP_NORTHEAST_2)
        .minimumPartSizeInBytes(8 * 1024 * 1024)
        .maxConcurrency(64)
        .targetThroughputInGbps(20.0)
        .build()

    private val listener = LoggingTransferListener.create()

    private val transferManager = S3TransferManager.builder()
        .s3Client(crtClient)

        .build()

    private val threadCount = 15
    private val scheduler = Schedulers.newParallel("gdal-parallel", threadCount)

     private fun downloadFileFromS3(bucket: String, key: String, localPath: Path): Mono<Void> {
         println("[📥] S3 파일 다운로드 시작: bucket=$bucket, key=$key")

         val downloadRequest = DownloadFileRequest.builder()
             .getObjectRequest { it.bucket(bucket).key(key) }
             .destination(localPath)
             .addTransferListener(listener)
             .build()

         return Mono.fromFuture {
             transferManager.downloadFile(downloadRequest)
                 .completionFuture()
         }.then()
     }
//    private fun downloadFileFromS3(bucket: String, key: String, localPath: Path): Mono<Void> {
//        println("[📥] S3 파일 다운로드 (AWS CLI) 시작: bucket=$bucket, key=$key")
//        return Mono.fromCallable {
//            val cliCommand = listOf(
//                "aws", "s3", "cp",
//                "s3://$bucket/$key",
//                localPath.toString(),
//                "--expected-size",
//                "1073741824",
////                "--only-show-errors",
////                "--quiet"
//            )
//            val process = ProcessBuilder(cliCommand)
//                .inheritIO()
//                .start()
//            if (process.waitFor() != 0) {
//                throw RuntimeException("AWS CLI 다운로드 실패: exit code=${process.exitValue()}")
//            }
//        }
//        .subscribeOn(scheduler)
//        .then()
//    }

    fun convertToCOG(inputPath: Path, outputPath: Path): Boolean {
        val fileSizeBytes = Files.size(inputPath)
        val isBigTiffNeeded = fileSizeBytes > 4L * 1024 * 1024 * 1024 // 4GB

        val command = mutableListOf(
            "gdal_translate", "-of", "COG",
            "-co", "COMPRESS=ZSTD",
            "-co", "NUM_THREADS=ALL_CPUS"
        )
        if (isBigTiffNeeded) {
            command.addAll(listOf("-co", "BIGTIFF=YES"))
        }
        command.add(inputPath.toString())
        command.add(outputPath.toString())

        val processBuilder = ProcessBuilder(command)
            .inheritIO()
        // 할당할 GDAL 캐시 크기(1 GB)
        processBuilder.environment()["GDAL_CACHEMAX"] = "1073741824"
        val process = processBuilder.start()
        return process.waitFor() == 0
    }

    fun uploadFileToS3(bucket: String, key: String, localPath: Path): Mono<Void> {
        println("[📤] S3 파일 업로드 시작: bucket=$bucket, key=$key")

        val uploadRequest = software.amazon.awssdk.transfer.s3.model.UploadFileRequest.builder()
            .putObjectRequest { it.bucket(bucket).key(key) }
            .source(localPath)
            .addTransferListener(listener)
            .build()

        return Mono.fromFuture {
            transferManager.uploadFile(uploadRequest).completionFuture()
        }.then()
    }

    fun extractMetadata(file: Path, s3Key: String): Mono<GeoTiffMetadata> {
        return Mono.fromCallable {
            val process = ProcessBuilder("gdalinfo", file.toString())
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            // Helper regexes
            val sizeRegex = Regex("""Size is (\d+), (\d+)""")
            val originRegex = Regex("""Origin = \(([-\d\.]+),([-\d\.]+)\)""")
            val pixelSizeRegex = Regex("""Pixel Size = \(([-\d\.]+),([-\d\.]+)\)""")
            val coordRegex = Regex("""Coordinate System is:\s*(.+?)\n""", RegexOption.DOT_MATCHES_ALL)

            val sizeMatch = sizeRegex.find(output)
            val originMatch = originRegex.find(output)
            val pixelMatch = pixelSizeRegex.find(output)
            val coordMatch = coordRegex.find(output)

            // Extract compression type
            val compression = extractFieldFromGdalInfo(output, "COMPRESSION")
                ?: extractFieldFromGdalInfo(output, "Compression")
                ?: "Unknown"
            // Extract color interpretation (first band)
            val colorInterpretation = extractColorInterpretation(output)
            // Count bands
            val bandCount = countBands(output)

            val baseName = s3Key.substringAfterLast("/")
            GeoTiffMetadata(
                filename = baseName,
                width = sizeMatch?.groups?.get(1)?.value?.toInt() ?: 0,
                height = sizeMatch?.groups?.get(2)?.value?.toInt() ?: 0,
                originX = originMatch?.groups?.get(1)?.value?.toDouble() ?: 0.0,
                originY = originMatch?.groups?.get(2)?.value?.toDouble() ?: 0.0,
                pixelSizeX = pixelMatch?.groups?.get(1)?.value?.toDouble() ?: 0.0,
                pixelSizeY = pixelMatch?.groups?.get(2)?.value?.toDouble() ?: 0.0,
                coordinateSystem = coordMatch?.groups?.get(1)?.value?.trim() ?: "Unknown",
                bandCount = bandCount,
                colorInterpretation = colorInterpretation,
                compression = compression
            )
        }
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap { metadata ->
            val baseName = s3Key.substringAfterLast("/")
            val filenamePrefix = baseName.substringBeforeLast(".")
            // Check for existing metadata by filename (deduplication)
            metadataRepository.findByFilenameStartingWith(filenamePrefix)
                .collectList()
                .flatMap { existingList ->
                    val now = java.time.Instant.now()
                    // Find exact match (deduplication by filename)
                    val existing = existingList.find { it.filename == metadata.filename }
                    val sequence = if (existingList.isEmpty()) 1 else existingList.size + 1
                    val nameOnly = baseName.substringBeforeLast(".")
                    val finalS3Key = "%s_to_cog_%d".format(nameOnly, sequence)
                    val updatedMetadata = if (existing != null) {
                        existing.copy(
                            uploadCount = existing.uploadCount + 1,
                            lastUploadTime = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault())
                        )
                    } else {
                        metadata.copy(
                            uploadCount = 1,
                            lastUploadTime = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault())
                        )
                    }
                    // Save or update metadata (deduplicated)
                    metadataRepository.save(updatedMetadata)
                        .flatMap { savedMeta ->
                            // Compute checksum
                            val checksum = computeFileChecksum(file)
                            // Insert RawGeoTiff row for logging
                            val rawGeoTiff = RawGeoTiff(
                                id = null,
                                originalFilename = finalS3Key,
                                checksum = checksum,
                                metadataId = savedMeta.id,
                                uploadTime = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault()),
                                fileSize = Files.size(file)
                            )
                            rawGeoTiffRepository.save(rawGeoTiff)
                                .thenReturn(savedMeta)
                        }
                        .doOnSuccess { println("[✔] 메타데이터 저장 완료: ${updatedMetadata.filename}, 저장 S3 Key: $finalS3Key") }
                        .doOnError { println("[!] 메타데이터 저장 실패: ${it.message}") }
                }
        }
        .doOnError { println("[!] 메타데이터 추출 실패: ${it.message}") }
    }
    /**
     * Extracts a field value from gdalinfo output.
     * Example: extractFieldFromGdalInfo(output, "COMPRESSION") -> "LZW"
     */
    private fun extractFieldFromGdalInfo(output: String, field: String): String? {
        // Try to find "  COMPRESSION=VALUE" or "  Compression=VALUE"
        val regex = Regex("""\s+$field(?:=|:)\s*([^\s]+)""", RegexOption.IGNORE_CASE)
        return regex.find(output)?.groups?.get(1)?.value
    }

    /**
     * Extracts color interpretation for the first band from gdalinfo output.
     * Looks for "Band 1 ..." followed by "ColorInterp=..." or "Color Interpretation = ..."
     */
    private fun extractColorInterpretation(output: String): String {
        val band1Regex = Regex(
            """Band 1 .+?(ColorInterp(?:=|:)\s*([^\s]+)|Color Interpretation(?:=|:)\s*([^\s]+))""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        val match = band1Regex.find(output)
        return match?.groups?.get(2)?.value
            ?: match?.groups?.get(3)?.value
            ?: "Undefined"
    }

    /**
     * Counts the number of bands in gdalinfo output.
     * Looks for lines like "Band 1", "Band 2", etc.
     */
    private fun countBands(output: String): Int {
        val bandRegex = Regex("""Band \d+""")
        return bandRegex.findAll(output).count()
    }

    /**
     * Computes the SHA-256 checksum of a file.
     */
    private fun computeFileChecksum(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    override fun process(bucketIn: String, keyIn: String, bucketOut: String, targetKey: String): Mono<Void> {
        val localInput = Files.createTempFile("input_", ".tif")
        val localOutput = Files.createTempFile("output_", ".tiff")
        val startTotal = System.currentTimeMillis()
        var downloadDuration = 0L
        var convertDuration = 0L
        var uploadDuration = 0L
        var downloadStart = 0L
        val finalKey = determineFinalKey(keyIn, targetKey)

        return downloadFileFromS3(bucketIn, keyIn, localInput)
            .doOnSubscribe { downloadStart = System.currentTimeMillis() }
            .doOnSuccess { downloadDuration = System.currentTimeMillis() - downloadStart }
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        .then(
            Mono.fromCallable {
                val s = System.currentTimeMillis()
                val success = convertToCOG(localInput, localOutput)
                if (!success) {
                    throw RuntimeException("COG 변환 실패")
                }
                convertDuration = System.currentTimeMillis() - s
            }
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        )
        .then(
            extractMetadata(localOutput, keyIn)
                .flatMap { savedMeta ->
                    val baseName = keyIn.substringBeforeLast(".")
                    val seq = savedMeta.uploadCount
                    // Determine final key: if targetKey is a prefix (ends with '/'), append generated name; otherwise use provided targetKey
                    val finalUploadKey = if (targetKey.endsWith("/")) {
                        "${targetKey}${baseName}_to_cog_${seq}.tiff"
                    } else {
                        targetKey
                    }
                    uploadFileToS3(bucketOut, finalUploadKey, localOutput)
                }
        )
        .doOnSuccess {
            val totalSeconds = (System.currentTimeMillis() - startTotal) / 1000.0
            println("[⏱] 전체 소요 시간: ${"%.2f".format(totalSeconds)}초 " +
                    "(다운로드: ${downloadDuration/1000}s, " +
                    "변환: ${convertDuration/1000}s, " +
                    "업로드: ${uploadDuration/1000}s)")
        }
        .doOnError {
            println("[!] 처리 중 오류 발생: ${it.message}")
        }
        .doFinally {
            localInput.toFile().delete()
            localOutput.toFile().delete()
        }
        .then()
    }

    private fun determineFinalKey(keyIn: String, targetKey: String): String {
        // This method is now only used for initial S3 key logic, but actual S3 key for COG
        // file saving should use the sequence number from uploadCount in extractMetadata.
        return if (targetKey.endsWith("/")) {
            val baseName = keyIn.substringBeforeLast(".")
            "$targetKey${baseName}_to_cog_1.tiff"
        } else {
            targetKey
        }
    }
}