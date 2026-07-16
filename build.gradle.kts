plugins {
    java
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
