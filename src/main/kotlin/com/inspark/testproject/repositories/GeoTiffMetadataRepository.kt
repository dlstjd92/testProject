package com.inspark.testproject.repositories

import com.inspark.testproject.domain.GeoTiffMetadata
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface GeoTiffMetadataRepository : R2dbcRepository<GeoTiffMetadata, Long> {
    fun findByFilenameContainingIgnoreCase(filename: String): Flux<GeoTiffMetadata>
}