val coroutinesVersion: String by rootProject.extra

description = "Platform-independent contracts and models shared by Telegram and Discord bot modules"

plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework.data:spring-data-mongodb")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}
