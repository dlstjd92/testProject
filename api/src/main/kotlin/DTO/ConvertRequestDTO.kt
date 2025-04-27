package com.inspark.DTO

data class ConvertRequest(
    val bucketIn: String,
    val key: String,
    val bucketOut: String,
    val targetKey: String
)