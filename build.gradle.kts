plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "1.4.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.6.0"
    kotlin("kapt") version "2.2.0"
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("res")
        }
    }
}

group = "gecko10000.geckopipes"
version = "0.1"

bukkit {
    name = "GeckoPipes"
    main = "$group.$name"
    apiVersion = "1.13"
    depend = listOf("GeckoLib")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://eldonexus.de/repository/maven-public/")
    mavenLocal()
}

dependencies {
    compileOnly(kotlin("stdlib", version = "2.2.0"))
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("gecko10000.geckolib:GeckoLib:1.1")
    compileOnly("gecko10000.storagepots:StoragePots:0.1")
    compileOnly("net.strokkur", "strokk-commands-annotations", "1.2.4-SNAPSHOT")
    kapt("net.strokkur", "strokk-commands-processor", "1.2.4-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}


tasks.register("update") {
    dependsOn(tasks.build)
    doLast {
        exec {
            workingDir(".")
            commandLine("../../dot/local/bin/update.sh")
        }
    }
}
