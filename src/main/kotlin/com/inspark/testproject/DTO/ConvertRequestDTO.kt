package com.inspark.testproject.dto

data class ConvertRequest(
    val bucketIn: String,
    val key: String,
    val bucketOut: String,
    val userPrefix: String
)