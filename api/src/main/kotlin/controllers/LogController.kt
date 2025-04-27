package com.inspark.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import com.inspark.services.LogQueryService
import com.inspark.domain.RawGeoTiff

@RestController
@RequestMapping("/logs")
class LogController(
    private val logQueryService: LogQueryService
) {
    @GetMapping
    fun getAll(): Flux<RawGeoTiff> =
        logQueryService.findAll()
}