import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"

    application
}

group = "me.hives"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.google.cloud:libraries-bom:16.1.0"))
    implementation("com.google.cloud:google-cloud-datastore")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")


    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("io.mockk:mockk:1.12.2")

    testImplementation("org.testcontainers:testcontainers:1.16.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}