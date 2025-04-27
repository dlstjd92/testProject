plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.serialization") version "1.9.25"
    id("org.springframework.boot")
}
repositories {
    mavenCentral()  // 여기 추가
//    maven("https://sdk.amazonaws.com/maven")  // AWS Maven Repository 추가
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":repository"))

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-h2")
    implementation("software.amazon.awssdk.crt:aws-crt:0.30.11")
    implementation("software.amazon.awssdk:s3:2.31.29")

    implementation("software.amazon.awssdk:s3-transfer-manager:2.31.29")
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