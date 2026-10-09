val ktorVersion: String by rootProject.extra

description = "Voice notes transcription and processing bot"

plugins {
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":tg-bot-api"))
    implementation("org.slf4j:slf4j-api")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
}
