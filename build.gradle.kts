import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.5.16" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.4.20" apply false
    kotlin("plugin.spring") version "2.4.20" apply false
    kotlin("kapt") version "2.4.20" apply false
}

allprojects {
    group = "com.nikichxp"
    version = if (project.hasProperty("appVersion")) project.property("appVersion")!! else "1.1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    configurations.all {
        // JUnit 5 only: never let JUnit 4 or the vintage engine in, even transitively
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            this.jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

abstract class PrintVersion : DefaultTask() {
    @TaskAction
    fun printVersion() {
        println(project.version)
    }
}

tasks.register<PrintVersion>("printVersion")
