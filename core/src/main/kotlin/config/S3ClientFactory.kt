package com.inspark.config
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import java.io.File

@Configuration
open class S3ClientConfig {
    private val CONFIG_PATH = "config.json"

    @Serializable
    data class AwsConfig(
        val access_key_id: String,
        val secret_access_key: String,
        val region: String = "ap-northeast-2"
    )

    @Bean
    open fun s3AsyncClient(): S3AsyncClient {
        val text = File(CONFIG_PATH).readText()
        val config = Json.decodeFromString<AwsConfig>(text)

        return S3AsyncClient.builder()
            .region(Region.of(config.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(config.access_key_id, config.secret_access_key)
                )
            ).build()
    }
}