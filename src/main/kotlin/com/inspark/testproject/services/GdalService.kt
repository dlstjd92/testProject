package com.inspark.testproject.services

interface GdalService {
    fun process(bucketIn: String, keyIn: String, bucketOut: String, userPrefix: String): Boolean
}