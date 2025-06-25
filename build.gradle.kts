plugins {
    id("java")
    id("fabric-loom") version("1.8.10")
    kotlin("jvm") version ("1.9.23")
}

group = "org.example"
version = "2.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

loom {
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
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.5")

    // Fabric API
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.104.0+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.104.0+1.21.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.104.0+1.21.1"))
    modImplementation(fabricApi.module("fabric-networking-api-v1", "0.104.0+1.21.1"))

    // Cobblemon
    modImplementation("com.cobblemon:fabric:1.6.0+1.21.1")

    // Impactor Economy API
    modImplementation("net.impactdev.impactor.api:economy:5.3.0")

    // GooeyLibs
    modImplementation("ca.landonjw.gooeylibs:api:3.1.0-1.21.1-SNAPSHOT")
    modRuntimeOnly("ca.landonjw.gooeylibs:fabric:3.1.0-1.21.1-SNAPSHOT")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
