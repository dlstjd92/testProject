package com.inspark.services

import reactor.core.publisher.Mono

interface GdalService {
    fun process(bucketIn: String, keyIn: String, bucketOut: String, targetKey: String): Mono<Void>
}