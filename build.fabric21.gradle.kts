plugins {
	id("fabric-loom")
	id("maven-publish")
}

fun prop(name: String): String = property(name) as String

val minecraftVersion = prop("deps.minecraft")
val loader = "fabric"
val modVersion = prop("mod.version") + prop("mod.channel_tag")

version = "$modVersion+$minecraftVersion+$loader"
group = prop("mod.group")

base {
	archivesName = prop("mod.archive_name")
}

loom {
	splitEnvironmentSourceSets()

	mods {
		register(prop("mod.id")) {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	runs.named("client") {
		client()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "client"
		programArgs("--username=Spookipup")
		configName = "Fabric Client"
	}

	runs.named("server") {
		server()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "server"
		configName = "Fabric Server"
	}
}

repositories {
	mavenCentral()
}

dependencies {
	minecraft("com.mojang:minecraft:$minecraftVersion")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to inputs.properties["version"]))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

java {
	toolchain {
		languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(21))
	}

	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	inputs.property("archivesName", project.base.archivesName)

	from(rootProject.file("LICENSE")) {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = project.base.archivesName.get()
			from(components["java"])
		}
	}
}
