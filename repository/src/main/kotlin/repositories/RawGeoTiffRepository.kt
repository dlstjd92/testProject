package com.inspark.repositories

import com.inspark.domain.RawGeoTiff
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository

@Repository
interface RawGeoTiffRepository : R2dbcRepository<RawGeoTiff, Long>{

}
