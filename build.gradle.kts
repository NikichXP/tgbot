
plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("com.github.wnameless.json:json-flattener:0.12.0")
    implementation("org.yaml:snakeyaml:1.29")
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:2.7.6")
    implementation("org.springframework.boot:spring-boot-starter-web:2.7.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-client-core-jvm:2.1.3")
    implementation("io.ktor:ktor-client-cio-jvm:2.1.3")
    implementation("org.jetbrains.kotlin:kotlin-test:1.7.21")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.6")
}

group = "com.nikichxp"
version = "1.0.0"
description = "tgbot"
java.sourceCompatibility = JavaVersion.VERSION_1_8

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
