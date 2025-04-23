package com.inspark.testproject.controllers

import com.inspark.testproject.services.S3ListService
import com.inspark.testproject.dto.ListRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/S3")
class S3Controller(
    private val s3ListService: S3ListService,
) {

    @GetMapping("/ping")
    fun ping(): String {
        return "S3Controller is working!"
    }

    @PostMapping("/getList")
    fun listBucketFiles(@RequestBody request: ListRequest): List<String> {
        return s3ListService.listObjectsInBucket(request.bucket)
    }

}