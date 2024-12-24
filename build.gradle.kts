plugins {
    id("java")
    id("dev.architectury.loom") version("1.2-SNAPSHOT")
    id("architectury-plugin") version("3.4-SNAPSHOT")
    kotlin("jvm") version ("1.8.10")
}

group = "org.example"
version = "1.2.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

repositories {
    mavenCentral()
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.0")

    // Fabric API
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.89.3+1.20.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.89.3+1.20.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.89.3+1.20.1"))

    // Cobblemon
    modImplementation("com.cobblemon:fabric:1.5.0+1.20.1-SNAPSHOT")

    // Impactor Economy API
    modImplementation("net.impactdev.impactor.api:economy:5.2.0-SNAPSHOT")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
