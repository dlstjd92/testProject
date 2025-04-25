package com.inspark.testproject.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import com.inspark.testproject.services.LogQueryService
import com.inspark.testproject.domain.RawGeoTiff

@RestController
@RequestMapping("/logs")
class LogController(
    private val logQueryService: LogQueryService
) {
    @GetMapping
    fun getAll(): Flux<RawGeoTiff> =
        logQueryService.findAll()
}