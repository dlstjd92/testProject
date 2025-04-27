plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.25"
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
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