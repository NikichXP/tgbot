description = "AI chat summary bot"

plugins {
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":tg-bot-api"))
    implementation("org.springframework:spring-context")
    implementation("org.slf4j:slf4j-api")
}
