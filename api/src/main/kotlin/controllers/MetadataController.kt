package com.inspark.controllers

import com.inspark.domain.GeoTiffMetadata
import com.inspark.services.MetadataQueryService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/metadata")
class MetadataController(
    private val metadataQueryService: MetadataQueryService
) {

    @GetMapping
    fun getAll(): Flux<GeoTiffMetadata> =
        metadataQueryService.findAll()

//    @GetMapping("/{id}")
//    fun getById(@PathVariable id: Long): Mono<GeoTiffMetadata> =
//        metadataQueryService.findById(id)
//
//    @GetMapping("/search")
//    fun searchByFilename(@RequestParam filename: String): Flux<GeoTiffMetadata> =
//        metadataQueryService.findByFilename(filename)

    @GetMapping("/filter")
    fun filterByParams(@RequestParam allParams: Map<String, String>): Flux<GeoTiffMetadata> =
        metadataQueryService.findByFilters(allParams)
}