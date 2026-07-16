plugins {
    java
    id("com.modrinth.minotaur") version "2.9.0"
}

group = property("group") as String
version = property("version") as String

val paperVersion = property("paperVersion") as String
val skriptVersion = property("skriptVersion") as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.skriptlang.org/releases")
    maven("https://jitpack.io")
}

dependencies {
    // Compile-only: the server provides both at runtime.
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly("com.github.SkriptLang:Skript:$skriptVersion")
}

// The Java version the pinned Paper requires; no toolchain pin, so any newer JDK can run the build.
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.processResources {
    val tokens = mapOf("version" to version)
    inputs.properties(tokens)
    filesMatching("plugin.yml") {
        expand(tokens)
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("skdialogs")
    versionNumber.set(version as String)
    versionType.set("release")
    uploadFile.set(tasks.jar)
    // Binary-compatible back to 1.21.8, the first Paper with the full dialog API.
    gameVersions.addAll("1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1.1", "26.1.2", "26.2")
    loaders.add("paper")
    dependencies {
        required.project("skript")
    }
    changelog.set("https://github.com/ch99q/skdialogs/releases/tag/v$version")
    // The README, with relative links made absolute so they work off GitHub.
    syncBodyFrom.set(rootProject.file("README.md").readText()
            .replace(Regex("]\\((?!https?://|#)"), "](https://github.com/ch99q/skdialogs/blob/main/"))
}
