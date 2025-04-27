package com.inspark.testproject.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
class DatabaseConfig(private val connectionFactory: ConnectionFactory) {

    @Bean
    fun r2dbcEntityTemplate(): R2dbcEntityTemplate =
        R2dbcEntityTemplate(connectionFactory)

    @Bean
    fun r2dbcTransactionManager(): R2dbcTransactionManager =
        R2dbcTransactionManager(connectionFactory)

    @Bean
    fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(transactionManager)
    }
}
