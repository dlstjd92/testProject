package com.inspark.testproject.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("geotiff_metadata")
data class GeoTiffMetadata(
    @Id
    val id: Long? = null,

    val filename: String,
    val coordinateSystem: String,
    val originX: Double = 0.0,
    val originY: Double = 0.0,
    val pixelSizeX: Double = 0.0,
    val pixelSizeY: Double = 0.0,
    val width: Int = 0,
    val height: Int = 0,
    val bandCount: Int = 0,
    val colorInterpretation: String,
    val compression: String,
    val uploadCount: Int = 1,
    val lastUploadTime: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)


@Table("raw_geotiff_log")
data class RawGeoTiff(
    @Id
    val id: Long? = null,
    val originalFilename: String,
    val checksum: String,
    val uploadTime: LocalDateTime = LocalDateTime.now(),
    val fileSize: Long,
    val metadataId: Long? = null
)