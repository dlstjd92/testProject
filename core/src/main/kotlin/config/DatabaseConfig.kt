package com.inspark.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory

@Configuration
open class DatabaseConfig {

    @Bean
    open fun connectionFactory(): ConnectionFactory {
        return H2ConnectionFactory(
            H2ConnectionConfiguration.builder()
                .url("mem:testdb;DB_CLOSE_DELAY=-1;")
                .username("inspark")
                .build()
        )
    }

}
