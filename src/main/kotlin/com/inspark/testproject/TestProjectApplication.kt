package com.inspark.testproject

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TestProjectApplication

fun main(args: Array<String>) {
    runApplication<TestProjectApplication>(*args)
}
