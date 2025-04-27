package com.inspark.services

import com.inspark.domain.GeoTiffMetadata
import com.inspark.repositories.GeoTiffMetadataRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MetadataQueryServiceImpl(
    private val repository: GeoTiffMetadataRepository
) : MetadataQueryService {
    override fun findAll(): Flux<GeoTiffMetadata> = repository.findAll()

    override fun findById(id: Long): Mono<GeoTiffMetadata> = repository.findById(id)

    override fun findByFilename(filename: String): Flux<GeoTiffMetadata> =
        repository.findByFilenameContainingIgnoreCase(filename)

    override fun findByFilters(filters: Map<String, String>): Flux<GeoTiffMetadata> =
        repository.findByFilters(filters)
}