package com.inspark.repositories

import com.inspark.domain.GeoTiffMetadata
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
open class CustomGeoTiffMetadataRepositoryImpl(
    private val db: DatabaseClient
) : CustomGeoTiffMetadataRepository {

    private val logger = org.slf4j.LoggerFactory.getLogger(CustomGeoTiffMetadataRepositoryImpl::class.java)

    override fun findByFilters(filters: Map<String, String>): Flux<GeoTiffMetadata> {
        // 기본 쿼리
        var sql = "SELECT * FROM geotiff_metadata"
        if (filters.isNotEmpty()) {
            val whereBuilder = StringBuilder()
            filters.entries.forEachIndexed { index, (field, value) ->
                // 카멜케이스 스네이크로 바꾸기
                val column = field.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
                if (index > 0) {
                    whereBuilder.append(" AND ")
                } else {
                    whereBuilder.append(" WHERE ")
                }
                // 스트링타입 필드는 Like로 포함하는거 찾기
                when (field) {
                    "filename", "coordinateSystem", "colorInterpretation", "compression" ->
                        whereBuilder.append("$column LIKE :$field")
                    else ->
                        whereBuilder.append("$column = :$field")
                }
            }
            sql += whereBuilder.toString()
            logger.info("Generated SQL: {} with filters: {}", sql, filters)
        }

        var spec = db.sql(sql)
        // 바인딩
        filters.forEach { (field, value) ->
            when (field) {
                "filename", "coordinateSystem", "colorInterpretation", "compression" ->
                    spec = spec.bind(field, "%$value%")
                else -> {

                    val intValue = value.toIntOrNull() ?: 0
                    spec = spec.bind(field, intValue)
                }
            }
        }

        logger.info("Bound filter values: {}", filters)

        return spec.map { row, _ ->
            GeoTiffMetadata(
                id                 = row.get("id", java.lang.Long::class.java)!!.toLong(),
                filename           = row.get("filename", String::class.java)!!,
                coordinateSystem   = row.get("coordinate_system", String::class.java)!!,
                originX            = row.get("origin_x", java.lang.Double::class.java)?.toDouble() ?: 0.0,
                originY            = row.get("origin_y", java.lang.Double::class.java)?.toDouble() ?: 0.0,
                pixelSizeX         = row.get("pixel_size_x", java.lang.Double::class.java)?.toDouble() ?: 0.0,
                pixelSizeY         = row.get("pixel_size_y", java.lang.Double::class.java)?.toDouble() ?: 0.0,
                width              = row.get("width", java.lang.Integer::class.java)?.toInt() ?: 0,
                height             = row.get("height", java.lang.Integer::class.java)?.toInt() ?: 0,
                bandCount          = row.get("band_count", java.lang.Integer::class.java)?.toInt() ?: 0,
                colorInterpretation= row.get("color_interpretation", String::class.java)!!,
                compression        = row.get("compression", String::class.java)!!,
                uploadCount        = row.get("upload_count", java.lang.Integer::class.java)?.toInt() ?: 0,
                lastUploadTime     = row.get("last_upload_time", java.time.LocalDateTime::class.java)!!,
                createdAt          = row.get("created_at", java.time.LocalDateTime::class.java)!!
            )
        }.all()
    }

    override fun incrementUploadCountByFilename(filename: String): Mono<Int> {
        return db.inConnection { connection ->
            Mono.from(
                connection.createStatement(
                    """
                    UPDATE geotiff_metadata
                    SET upload_count = upload_count + 1,
                        last_upload_time = CURRENT_TIMESTAMP
                    WHERE filename = ?
                    """
                )
                .bind(0, filename)
                .execute()
            ).then(
                Mono.from(
                    connection.createStatement(
                        """
                        SELECT upload_count FROM geotiff_metadata
                        WHERE filename = ?
                        """
                    )
                    .bind(0, filename)
                    .execute()
                )
                .flatMap { result ->
                    Mono.from(result.map { row, _ ->
                        row.get("upload_count", Integer::class.java)?.toInt() ?: 0
                    })
                }
            )
        }
    }


}