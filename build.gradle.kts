import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20-Beta"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("maven-publish")
}

group = "nxt"
version = "0.0.2"

repositories {
    maven("https://repo.flawcra.cc/mirrors")
}

val shadows = listOf<String>(
    "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.10",  // Kotlin Standard Library
    "net.oneandone.reflections8:reflections8:0.11.7", // Library for Reflections (Dynamic Class Loading)
    "org.javassist:javassist:3.29.2-GA",             // Library for Reflections (Dynamic Class Loading)
    "io.ktor:ktor-client-core-jvm:2.2.3",               // Library for HTTP Requests
    "io.ktor:ktor-client-cio-jvm:2.2.3",                // Library for HTTP Requests
    "com.github.TheFruxz:Ascend:22.0.0"
)

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.8.9")

    shadows.forEach { dependency ->
        implementation(dependency)
        shadow(dependency)
    }
}
tasks.test {
    useJUnitPlatform()
}

tasks {

    build {
        dependsOn("shadowJar")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    withType<ProcessResources> {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    withType<ShadowJar> {
        mergeServiceFiles()
        configurations = listOf(project.configurations.shadow.get())
        archiveFileName.set("NxTLobby.jar")
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/NxTCrew/NxTLobby")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}