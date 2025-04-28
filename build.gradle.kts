plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    group = "com.inspark"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }

    apply(plugin = "io.spring.dependency-management")
    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.4")
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("software.amazon.awssdk:s3:2.25.4")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.25.4")
    implementation("software.amazon.awssdk.crt:aws-crt:0.30.11")

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.25")

    implementation(project(":core"))
    implementation(project(":service"))
    implementation(project(":domain"))
    implementation(project(":api"))
    implementation(project(":repository"))
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}
tasks.withType<Jar> {
    enabled = false
}
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
    mainClass.set("com.inspark.testproject.TestProjectApplicationKt")
    archiveFileName.set("testProject-boot.jar")
}
tasks.withType<Test> {
    enabled = false
}

apply(plugin = "org.springframework.boot")