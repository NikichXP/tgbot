description = "Discord contracts and models for bot modules"

plugins {
    `java-library`
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api(project(":common-bot-api"))
}
