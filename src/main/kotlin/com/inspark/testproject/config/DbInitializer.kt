package com.inspark.testproject.config

import jakarta.annotation.PostConstruct
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
class DbInitializer(private val client: DatabaseClient) {

    @PostConstruct
    fun init() {
        client.sql(
            """
            CREATE TABLE IF NOT EXISTS geotiff_metadata (
                id IDENTITY PRIMARY KEY,
                filename VARCHAR(255),
                width INT,
                height INT,
                coordinate_system VARCHAR(255),
                origin_x DOUBLE,
                origin_y DOUBLE,
                pixel_size_x DOUBLE,
                pixel_size_y DOUBLE,
                band_count INT,
                color_interpretation VARCHAR(255),
                compression VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        ).then().subscribe()
    }
}