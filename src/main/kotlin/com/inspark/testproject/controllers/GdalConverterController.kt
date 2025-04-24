package com.inspark.testproject.controllers

import com.inspark.testproject.services.GdalService
import com.inspark.testproject.dto.ConvertRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/CogConverter")
class GdalConverterController(
    private val gdalService: GdalService
) {

    private val concurrency = 5

    @GetMapping("/ping")
    fun ping(): String {
        return "GdalConverterController is working!"
    }

    @PostMapping("/convert")
    fun convertFile(@RequestBody request: ConvertRequest): Mono<Void> {
        return gdalService.process(
            bucketIn = request.bucketIn,
            keyIn = request.key,
            bucketOut = request.bucketOut,
            targetKey = request.targetKey
        )
    }

    data class BatchConvertRequest(
        val bucketIn: String,
        val keys: List<String>,
        val bucketOut: String,
        val targetKey: String
    )

    @PostMapping("/batch-convert")
    fun convertMultipleFiles(@RequestBody request: BatchConvertRequest): Mono<Void> {
        return reactor.core.publisher.Flux.fromIterable(request.keys)
            .flatMap({ key ->
                val baseName = key.substringBeforeLast(".")
                val finalTargetKey = "${request.targetKey}${baseName}_to_cog_1.tiff"

                gdalService.process(
                    bucketIn = request.bucketIn,
                    keyIn = key,
                    bucketOut = request.bucketOut,
                    targetKey = finalTargetKey
                )
            }, concurrency)
            .then()
    }
}