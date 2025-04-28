package com.inspark.DTO

// 다중명령 받기 위한
data class BatchConvertRequest(
    val bucketIn: String,
    val keys: List<String>,
    val bucketOut: String,
    val targetKey: String
)