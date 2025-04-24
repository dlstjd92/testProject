package com.inspark.testproject.services

import reactor.core.publisher.Mono
import com.inspark.testproject.repositories.GeoTiffMetadataRepository
import reactor.core.scheduler.Schedulers

import org.springframework.stereotype.Service
import com.inspark.testproject.domain.GeoTiffMetadata
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener
import java.nio.file.Files
import java.nio.file.Path

@Service
class GdalServiceImpl(
    private val s3Client: S3AsyncClient,
    private val metadataRepository: GeoTiffMetadataRepository
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

            val sizeRegex = Regex("""Size is (\d+), (\d+)""")
            val originRegex = Regex("""Origin = \(([-\d\.]+),([-\d\.]+)\)""")
            val pixelSizeRegex = Regex("""Pixel Size = \(([-\d\.]+),([-\d\.]+)\)""")
            val coordRegex = Regex("""Coordinate System is:\s*(.+?)\n""", RegexOption.DOT_MATCHES_ALL)

            val sizeMatch = sizeRegex.find(output)
            val originMatch = originRegex.find(output)
            val pixelMatch = pixelSizeRegex.find(output)
            val coordMatch = coordRegex.find(output)

            GeoTiffMetadata(
                filename = s3Key,
                width = sizeMatch?.groups?.get(1)?.value?.toInt() ?: 0,
                height = sizeMatch?.groups?.get(2)?.value?.toInt() ?: 0,
                originX = originMatch?.groups?.get(1)?.value?.toDouble() ?: 0.0,
                originY = originMatch?.groups?.get(2)?.value?.toDouble() ?: 0.0,
                pixelSizeX = pixelMatch?.groups?.get(1)?.value?.toDouble() ?: 0.0,
                pixelSizeY = pixelMatch?.groups?.get(2)?.value?.toDouble() ?: 0.0,
                coordinateSystem = coordMatch?.groups?.get(1)?.value?.trim() ?: "Unknown",
                bandCount = 11,
                colorInterpretation = "Undefined",
                compression = "LZW",
//                userName = "inspark"
            )
        }
        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
        .flatMap { metadata ->
            metadataRepository.save(metadata)
                .doOnSuccess { println("[✔] 메타데이터 저장 완료: ${it.filename}") }
                .doOnError { println("[!] 메타데이터 저장 실패: ${it.message}") }
        }
        .doOnError { println("[!] 메타데이터 추출 실패: ${it.message}") }
    }

    override fun process(bucketIn: String, keyIn: String, bucketOut: String, targetKey: String): Mono<Void> {
        val localInput = Files.createTempFile("input_", ".tif")
        val localOutput = Files.createTempFile("output_", ".tif")
        val startTotal = System.currentTimeMillis()
        var downloadDuration = 0L
        var convertDuration = 0L
        var uploadDuration = 0L
        val finalKey = determineFinalKey(keyIn, targetKey)

        return Mono.fromCallable {
            val s = System.currentTimeMillis()
            downloadFileFromS3(bucketIn, keyIn, localInput).block()
            downloadDuration = System.currentTimeMillis() - s
        }
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
            Mono.fromCallable {
                val s = System.currentTimeMillis()
                uploadFileToS3(bucketOut, finalKey, localOutput).block()
                uploadDuration = System.currentTimeMillis() - s
            }
            .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
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
        return if (targetKey.endsWith("/")) {
            val baseName = keyIn.substringBeforeLast(".")
            "$targetKey${baseName}_to_cog_1.tiff"
        } else {
            targetKey
        }
    }
}