description = "Karma bot: reactions, ratings and message stats"

plugins {
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":tg-bot-api"))
    implementation("org.springframework:spring-context")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.slf4j:slf4j-api")
}
