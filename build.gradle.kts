plugins {
    java
}

group = "br.com.universe"
version = "1.3.3"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
        name = "placeholderapi"
    }

    maven("https://repo.glaremasters.me/repository/towny/") {
        name = "towny"
        mavenContent { includeGroup("com.palmergames.bukkit.towny") }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("me.clip:placeholderapi:2.11.6")
    // Towny API only; Towny itself is supplied by the server.
    compileOnly("com.palmergames.bukkit.towny:towny:0.103.2.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    archiveBaseName.set("HudUniVerse")
}
