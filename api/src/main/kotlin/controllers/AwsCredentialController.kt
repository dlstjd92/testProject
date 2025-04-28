package com.inspark.controllers

import com.inspark.config.S3ClientConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/aws")
class AwsCredentialController @Autowired constructor(
    private val s3ClientConfig: S3ClientConfig
) {

    data class AwsCredentialsRequest(
        val accessKeyId: String,
        val secretAccessKey: String
    )

    @PostMapping("/credentials")
    fun setCredentials(@RequestBody request: AwsCredentialsRequest): String {
        if (request.accessKeyId.isBlank() || request.secretAccessKey.isBlank()) {
            return "Access Key와 Secret Key 모두 입력해야 합니다."
        }

        s3ClientConfig.setCredentials(request.accessKeyId, request.secretAccessKey)
        s3ClientConfig.initializeS3Client()
        return "AWS 인증정보가 성공적으로 등록되었고, S3 클라이언트가 초기화되었습니다."
    }
}