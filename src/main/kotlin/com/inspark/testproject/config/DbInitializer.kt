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
                upload_count INT DEFAULT 1,
                last_upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent()
        ).then().subscribe()

        client.sql(
            """
        CREATE TABLE IF NOT EXISTS raw_geotiff_log (
            id IDENTITY PRIMARY KEY,
            original_filename VARCHAR(255),
            checksum VARCHAR(255),
            upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            file_size BIGINT,
            metadata_id BIGINT
        )
        """.trimIndent()
        ).then().subscribe()
    }
}