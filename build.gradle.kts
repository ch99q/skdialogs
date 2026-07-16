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
    // Both are compile-only: the server provides Paper and Skript at runtime. The dialog API is
    // referenced directly, so a breaking change to it fails this compile against the pinned Paper
    // version rather than surfacing at runtime on a server.
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly("com.github.SkriptLang:Skript:$skriptVersion")
}

// Paper 26.2 requires Java 25, so target it. No toolchain is pinned, so the build uses whatever
// JDK 25+ runs it, in CI and on a contributor's machine alike.
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

// Uploads the built jar as a Modrinth version; CI runs this task on tagged releases.
modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("skdialogs")
    versionNumber.set(version as String)
    versionType.set("release")
    uploadFile.set(tasks.jar)
    // The jar compiles against the pinned Paper but stays binary-compatible back to 1.21.8, the
    // first Paper with the full dialog API (verified by compiling against every version, and by
    // running the release jar on 1.21.8 and the pin). Extend this list as new versions verify.
    gameVersions.addAll("1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1.1", "26.1.2", "26.2")
    loaders.add("paper")
    dependencies {
        required.project("skript")
    }
    changelog.set("https://github.com/ch99q/skdialogs/releases/tag/v$version")
    // The project description mirrors the README, with relative links made absolute so they
    // survive leaving GitHub.
    syncBodyFrom.set(rootProject.file("README.md").readText()
            .replace(Regex("]\\((?!https?://|#)"), "](https://github.com/ch99q/skdialogs/blob/main/"))
}
