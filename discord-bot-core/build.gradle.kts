description = "Discord API access: interaction signature verification and storage"

plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api(project(":discord-bot-api"))
    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api")
}
