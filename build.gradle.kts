import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("org.springframework.boot") version "3.5.15" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.4.20" apply false
    kotlin("plugin.spring") version "2.4.20" apply false
    kotlin("kapt") version "2.4.20" apply false
}

val ktorVersion by extra("3.6.0")
val coroutinesVersion by extra("1.11.0")

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

    plugins.withId("io.spring.dependency-management") {
        extra["kotlin-coroutines.version"] = coroutinesVersion
        extra["kotlin.version"] = getKotlinPluginVersion()

        configure<DependencyManagementExtension> {
            imports {
                mavenBom(SpringBootPlugin.BOM_COORDINATES)
            }
            dependencies {
                dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                dependency("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
        }

        dependencies {
            "testImplementation"("org.springframework.boot:spring-boot-starter-test")
            "testImplementation"(kotlin("test-junit5"))
        }
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
