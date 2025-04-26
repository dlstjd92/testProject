package com.inspark.testproject.dto

data class ListRequest(
    val bucket: String,
    val prefix: String? = null
)