package com.inspark.controllers

import com.inspark.services.GdalService
import com.inspark.DTO.ConvertRequest
import com.inspark.DTO.BatchConvertRequest

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/CogConverter")
class GdalConverterController(
    private val gdalService: GdalService
) {
    // 어차피 전부 못씀
    private val concurrency = 15
//
//    @GetMapping("/ping")
//    fun ping(): String {
//        return "GdalConverterController is working!"
//    }

    @PostMapping("/convert")
    fun convertFile(@RequestBody request: ConvertRequest): Mono<Void> {
        return gdalService.process(
            bucketIn = request.bucketIn,
            keyIn = request.key,
            bucketOut = request.bucketOut,
            targetKey = request.targetKey
        )
    }

    @PostMapping("/batch-convert")
    fun convertMultipleFiles(@RequestBody request: BatchConvertRequest): Mono<Void> {
        return reactor.core.publisher.Flux.fromIterable(request.keys)
            .flatMap({ key ->
                gdalService.process(
                    bucketIn = request.bucketIn,
                    keyIn = key,
                    bucketOut = request.bucketOut,
                    targetKey = request.targetKey
                )
            }, concurrency)
            .then()
    }
}