package com.inspark.services

import com.inspark.domain.RawGeoTiff
import com.inspark.repositories.RawGeoTiffRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class LogQueryServiceImpl(
    private val rawGeoTiffRepository: RawGeoTiffRepository
) : LogQueryService {
    override fun findAll(): Flux<RawGeoTiff> =
        rawGeoTiffRepository.findAll()
}