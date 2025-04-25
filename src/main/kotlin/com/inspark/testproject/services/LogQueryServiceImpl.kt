package com.inspark.testproject.services

import com.inspark.testproject.domain.RawGeoTiff
import com.inspark.testproject.repositories.RawGeoTiffRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class LogQueryServiceImpl(
    private val rawGeoTiffRepository: RawGeoTiffRepository
) : LogQueryService {
    override fun findAll(): Flux<RawGeoTiff> =
        rawGeoTiffRepository.findAll()
}