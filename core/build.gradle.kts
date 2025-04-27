plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.25" // ✅ 이 줄 추가!
}


dependencies {
    implementation("io.r2dbc:r2dbc-spi")
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("software.amazon.awssdk:s3:2.25.4")
    implementation("io.r2dbc:r2dbc-h2")
    runtimeOnly("com.h2database:h2")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // JVM 21로 설정
    }
}