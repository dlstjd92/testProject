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

//    val userName: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)