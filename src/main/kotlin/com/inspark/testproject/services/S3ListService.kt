package com.inspark.testproject.services

interface S3ListService {
    fun listObjectsInBucket(bucket: String): List<String>
}