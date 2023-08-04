import xyz.wagyourtail.unimined.internal.minecraft.MinecraftProvider
import xyz.wagyourtail.unimined.minecraft.patch.prcraft.PrcraftMinecraftTransformer

plugins {
    id("java")
    id("xyz.wagyourtail.unimined")
}

unimined.useGlobalCache = false

group = "com.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://maven.jemnetworks.com/releases")
}

val minecraft_version = project.properties["minecraft_version"] as String
val prcraft_version = project.properties["prcraft_version"] as String


unimined.minecraft(sourceSets.main.get()) {
    version(minecraft_version)

// default mappings add packages to some classes...
// for remapping support you can uncomment this block
//    mappings {
//        retroMCP()
//
//        devNamespace("mcp")
//    }

    // add our custom transformer
    customPatcher(PrcraftMinecraftTransformer(project, this as MinecraftProvider)) {
        loader(prcraft_version)
        serverOnly = false // Change to true to only include server classes
    }
}

dependencies {
}

tasks.compileJava {
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("prcraft.json") {
        expand("version" to project.version)
    }
}