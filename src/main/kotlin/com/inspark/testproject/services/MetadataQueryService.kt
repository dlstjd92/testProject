package com.inspark.testproject.services

import com.inspark.testproject.domain.GeoTiffMetadata
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MetadataQueryService {
    fun findAll(): Flux<GeoTiffMetadata>
    fun findById(id: Long): Mono<GeoTiffMetadata>
    fun findByFilename(filename: String): Flux<GeoTiffMetadata>
    fun findByFilters(filters: Map<String, String>): Flux<GeoTiffMetadata>
}