import java.text.SimpleDateFormat
import java.util.Date

val ktorVersion: String = "3.6.0"
val kotlinVersion: String = "2.4.20"
val coroutinesVersion: String = "1.11.0"

val buildTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
val versionName: String =
    if (project.hasProperty("appVersion")) project.property("appVersion").toString() else "unknown"

description = "tgbot"
java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("kapt")
}

extra["kotlin-coroutines.version"] = coroutinesVersion

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("com.nimbusds:nimbus-jose-jwt:10.10")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-cache")
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
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test-junit5"))
}

dependencyManagement {
    dependencies {
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    }
}

tasks.bootJar {
    archiveFileName.set("app.jar")
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

tasks.bootRun {
    workingDir = rootDir
}
