plugins {
    kotlin("jvm") version "1.8.21"
}

repositories {
    mavenCentral()
    maven("https://maven.wagyourtail.xyz/snapshots")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
}

dependencies {
    implementation("xyz.wagyourtail.unimined:unimined:1.0.0-SNAPSHOT")
}