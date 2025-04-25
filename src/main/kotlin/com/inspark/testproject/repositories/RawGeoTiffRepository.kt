package com.inspark.testproject.repositories

import com.inspark.testproject.domain.RawGeoTiff
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface RawGeoTiffRepository : R2dbcRepository<RawGeoTiff, Long>{

}
