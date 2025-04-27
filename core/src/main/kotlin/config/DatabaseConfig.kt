package com.inspark.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory

@Configuration
open class DatabaseConfig {

    @Bean
    open fun connectionFactory(): ConnectionFactory {
        return H2ConnectionFactory(
            H2ConnectionConfiguration.builder()
                .url("mem:testdb;DB_CLOSE_DELAY=-1;")
                .username("sa")
                .build()
        )
    }

    @Bean
    open fun r2dbcEntityTemplate(connectionFactory: ConnectionFactory): R2dbcEntityTemplate =
        R2dbcEntityTemplate(connectionFactory)

    @Bean
    open fun r2dbcTransactionManager(connectionFactory: ConnectionFactory): R2dbcTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    open fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(transactionManager)
    }
}
