package com.inspark.testproject.repositories

import com.inspark.testproject.domain.GeoTiffMetadata
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface CustomGeoTiffMetadataRepository {
    fun findByFilters(filters: Map<String, String>): Flux<GeoTiffMetadata>
    fun incrementUploadCountByFilename(filename: String): Mono<Int>
    fun getUploadCount(filename: String): Mono<Int>

}