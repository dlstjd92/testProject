package com.inspark.testproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["com.inspark"])
@EnableR2dbcRepositories(basePackages = ["com.inspark.repositories"])
class TestProjectApplication

fun main(args: Array<String>) {
    runApplication<TestProjectApplication>(*args)
}
