pluginManagement {
	repositories {
		mavenLocal()
		mavenCentral()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
		maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
		maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
		maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
		maven("https://maven.terraformersmc.com/") { name = "TerraformersMC" }
		exclusiveContent {
			forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
			filter { includeGroup("maven.modrinth") }
		}
	}
	includeBuild("build-logic")
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	id("dev.kikugie.stonecutter") version "0.8"
}

stonecutter {
	create(rootProject) {
		version("26.1-fabric", "26.1").buildscript = "build.fabric26.gradle.kts"
		version("1.21.1-fabric", "1.21.1").buildscript = "build.fabric21.gradle.kts"
		version("1.21.1-neoforge", "1.21.1").buildscript = "build.neoforge.gradle.kts"
		version("1.20.1-fabric", "1.20.1").buildscript = "build.fabric.gradle.kts"
		version("1.20.1-forge", "1.20.1").buildscript = "build.forge.gradle.kts"

		vcsVersion = "26.1-fabric"
	}
}
