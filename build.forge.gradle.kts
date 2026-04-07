plugins {
	id("mod-platform")
	id("net.neoforged.moddev.legacyforge")
}

platform {
	loader = "forge"
	dependencies {
		required("minecraft") {
			forgeVersionRange = "[${prop("deps.minecraft")}]"
		}
		required("forge") {
			forgeVersionRange = "[${prop("deps.forge")},)"
		}
	}
}

legacyForge {
	version = "${property("deps.minecraft")}-${property("deps.forge")}"

	val accessTransformerFile = rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg")
	if (accessTransformerFile.exists()) {
		validateAccessTransformers = true
		accessTransformers.from(accessTransformerFile)
	} else {
		validateAccessTransformers = false
	}

	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "Forge Client (${stonecutter.current.version})"
			programArgument("--username=Spookipup")
		}
		register("server") {
			server()
			gameDirectory = file("run/")
			ideName = "Forge Server (${stonecutter.current.version})"
		}
	}


	mods {
		register(prop("mod.forge_id")) {
			sourceSet(sourceSets["main"])
		}
	}
}

mixin {
	add(sourceSets.main.get(), "${prop("mod.id")}.mixins.refmap.json")
	config("${prop("mod.id")}.mixins.json")
	config("${prop("mod.id")}.client.mixins.json")
	config("${prop("mod.id")}.compat.betterdungeons.mixins.json")
	config("${prop("mod.id")}.compat.betterfortresses.mixins.json")
}

repositories {
	mavenCentral()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
}

dependencies {
	annotationProcessor("org.spongepowered:mixin:${libs.versions.mixin.get()}:processor")

	compileOnly("io.github.llamalad7:mixinextras-common:0.4.1")
	implementation("io.github.llamalad7:mixinextras-forge:0.4.1")
	jarJar("io.github.llamalad7:mixinextras-forge:0.4.1")
	implementation(libs.moulberry.mixinconstraints)
	jarJar(libs.moulberry.mixinconstraints)
}

sourceSets {
	main {
		java.srcDir("src/client/java")
		resources.srcDir("src/client/resources")
		resources.srcDir(
			"${rootDir}/versions/datagen/${stonecutter.current.version.split("-")[0]}/src/main/generated"
		)
	}
}

tasks.named("createMinecraftArtifacts") {
	dependsOn(tasks.named("stonecutterGenerate"))
}

val adoptiumJava17 = javaToolchains.launcherFor {
	languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17))
	vendor.set(org.gradle.jvm.toolchain.JvmVendorSpec.ADOPTIUM)
}

listOf("runClient", "runServer").forEach { runTask ->
	tasks.named<org.gradle.api.tasks.JavaExec>(runTask) {
		javaLauncher.set(adoptiumJava17)
	}
}

stonecutter {

}
