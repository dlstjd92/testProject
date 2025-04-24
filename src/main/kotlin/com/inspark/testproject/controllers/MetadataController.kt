package com.inspark.testproject.controllers

import com.inspark.testproject.domain.GeoTiffMetadata
import com.inspark.testproject.services.MetadataQueryService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/metadata")
class MetadataController(
    private val metadataQueryService: MetadataQueryService
) {

    @GetMapping
    fun getAll(): Flux<GeoTiffMetadata> =
        metadataQueryService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Mono<GeoTiffMetadata> =
        metadataQueryService.findById(id)

    @GetMapping("/search")
    fun searchByFilename(@RequestParam filename: String): Flux<GeoTiffMetadata> =
        metadataQueryService.findByFilename(filename)
}