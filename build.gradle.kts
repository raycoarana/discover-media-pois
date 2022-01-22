plugins {
    application
    kotlin("jvm") version "1.5.10"
}

group = "com.raycoarana"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("commons-io:commons-io:2.11.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.jsoup:jsoup:1.14.3")
}