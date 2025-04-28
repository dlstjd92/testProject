plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
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