plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("kapt") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    kotlin("plugin.jpa") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

fun getGitHash(): String =
    providers
        .exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText
        .get()
        .trim()

group = "kr.hhplus.be"
version = getGitHash()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmToolchain(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // DB
    runtimeOnly("com.mysql:mysql-connector-j")
    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.14.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("user.timezone", "UTC")
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
}

// Docker Compose 연동 태스크 추가
val dockerComposeUp by tasks.registering(Exec::class) {
    group = "docker"
    description = "Run docker compose up for local infrastructure."
    commandLine(
        "sh",
        "-c",
        "command -v docker >/dev/null 2>&1 && docker compose up -d || echo 'docker not found, skipping docker compose up'",
    )
    workingDir = project.projectDir
}
val dockerComposeDown by tasks.registering(Exec::class) {
    group = "docker"
    description = "Run docker compose down for local infrastructure."
    commandLine(
        "sh",
        "-c",
        "command -v docker >/dev/null 2>&1 && docker compose down || echo 'docker not found, skipping docker compose down'",
    )
    workingDir = project.projectDir
}
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    dependsOn(dockerComposeUp)
}
