package com.inspark.testproject.controllers

import com.inspark.testproject.services.GdalService
import com.inspark.testproject.dto.ConvertRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/CogConverter")
class GdalConverterController(
    private val gdalService: GdalService
) {

    @GetMapping("/ping")
    fun ping(): String {
        return "GdalConverterController is working!"
    }

    @PostMapping("/convert")
    fun convertFile(@RequestBody request: ConvertRequest): Boolean {
        return gdalService.process(request.bucketIn, request.key, request.bucketOut, request.userPrefix)
    }
}