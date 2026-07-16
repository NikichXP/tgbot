import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

val ktorVersion: String = "3.5.0"
val kotlinVersion: String = "2.4.0"
val coroutinesVersion: String = "1.11.0"

val buildTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
val versionName: String = if (project.hasProperty("appVersion")) project.property("appVersion").toString() else "unknown"

group = "com.nikichxp"
version = if (project.hasProperty("appVersion")) project.property("appVersion")!! else "1.1.0"
description = "tgbot"
java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.4.0"
    kotlin("plugin.spring") version "2.4.0"
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.0.2"
extra["kotlin-coroutines.version"] = coroutinesVersion

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
    dependencies {
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
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
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}
