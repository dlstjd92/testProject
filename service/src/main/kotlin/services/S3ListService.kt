package com.inspark.services

interface S3ListService {
    fun listObjectsInBucket(bucket: String, prefix: String = ""): List<String>
}