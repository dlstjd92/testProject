package com.inspark.DTO

data class ListRequest(
    val bucket: String,
    val prefix: String? = null
)