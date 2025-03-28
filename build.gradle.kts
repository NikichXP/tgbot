import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

val ktorVersion: String = "3.0.3"
val kotlinVersion: String = "2.1.0"
val coroutinesVersion: String = "1.10.1"

val buildTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
val versionName: String = if (project.hasProperty("version")) project.version.toString() else "unknown"

group = "com.nikichxp"
version = project.property("version") ?: "1.1.0"
description = "tgbot"
java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.github.wnameless.json:json-flattener:0.17.2")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        this.jvmTarget.set(JvmTarget.JVM_17)
    }
}

abstract class PrintVersion : DefaultTask() {
    @TaskAction
    fun printVersion() {
        println(project.version)
    }
}

tasks.register<PrintVersion>("printVersion")

tasks.bootJar {
    archiveFileName.set("app.jar")
}
