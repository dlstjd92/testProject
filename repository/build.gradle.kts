plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.25" // ✅ 이 줄 추가!
}

dependencies {
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc") // repository에서 리액티브 리포지토리 선언 시
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