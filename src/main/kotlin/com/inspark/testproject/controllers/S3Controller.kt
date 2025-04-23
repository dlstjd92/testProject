package com.inspark.testproject.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("/CogConverter")
class S3Controller {

    @GetMapping("/ping")
    fun ping(): String {
        return "S3Controller is working!"
    }


}