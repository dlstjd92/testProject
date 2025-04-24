package com.inspark.testproject.services

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.measureTimeMillis

@SpringBootTest
class AverageTime {

    @Autowired
    private lateinit var gdalServiceImpl: GdalServiceImpl

    @Test
    fun `download performance test`() {
        // TODO: 실제 S3 버킷과 키로 바꿔 주세요
        val bucket = "dev1-apne2-pre-test-scene-bucket"
        val key    = "S1A_IW_GRDH_1SDV_20220311T232515_20220311T232540_042280_050A20_8211.tif"

        // private 메서드 리플렉션으로 가져오기
        val method = GdalServiceImpl::class.java
            .getDeclaredMethod("downloadFileFromS3", String::class.java, String::class.java, Path::class.java)
        method.isAccessible = true

        val times = mutableListOf<Long>()

        repeat(10) { idx ->
            // 임시 파일 생성
            val tempFile = Files.createTempFile("perf_test_", ".tif")

            // 측정
            val duration = measureTimeMillis {
                val mono = method.invoke(gdalServiceImpl, bucket, key, tempFile) as Mono<Void>
                mono.block()    // 블로킹해서 완료 대기
            }

            times += duration
            println("Run ${idx + 1}: ${duration} ms")
            Files.deleteIfExists(tempFile)
        }

        val avg = times.average()
        println("→ Average download time (10 runs): ${"%.2f".format(avg)} ms")
    }
}