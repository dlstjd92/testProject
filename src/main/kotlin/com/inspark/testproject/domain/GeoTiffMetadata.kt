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
    val originX: Double,
    val originY: Double,
    val pixelSizeX: Double,
    val pixelSizeY: Double,
    val width: Int,
    val height: Int,
    val bandCount: Int,
    val colorInterpretation: String,
    val compression: String,
    val uploadCount: Int = 1,
    val lastUploadTime: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)


@Table("raw_geotiff")
data class RawGeoTiff(
    @Id
    val id: Long? = null,
    val originalFilename: String,
    val checksum: String,
    val uploadTime: LocalDateTime = LocalDateTime.now(),
    val fileSize: Long,
    val metadataId: Long? = null
)