package com.inspark.services

import reactor.core.publisher.Flux
import com.inspark.domain.RawGeoTiff  // 로그 테이블 엔티티

/**
 * 로그 테이블(raw_geotiff)을 조회하는 서비스 인터페이스
 */
interface LogQueryService {
    fun findAll(): Flux<RawGeoTiff>
}