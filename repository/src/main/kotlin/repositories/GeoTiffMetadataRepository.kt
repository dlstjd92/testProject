package com.inspark.repositories

import com.inspark.domain.GeoTiffMetadata
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface GeoTiffMetadataRepository : R2dbcRepository<GeoTiffMetadata, Long>, CustomGeoTiffMetadataRepository {
    fun findByFilenameContainingIgnoreCase(filename: String): Flux<GeoTiffMetadata>
//    fun findByFilenameStartingWith(prefix: String): Flux<GeoTiffMetadata>
}