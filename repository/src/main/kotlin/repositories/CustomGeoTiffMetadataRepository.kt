package com.inspark.repositories

import com.inspark.domain.GeoTiffMetadata
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface CustomGeoTiffMetadataRepository {
    fun findByFilters(filters: Map<String, String>): Flux<GeoTiffMetadata>
    fun incrementUploadCountByFilename(filename: String): Mono<Int>

}