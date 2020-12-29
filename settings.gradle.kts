pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
    }

    plugins {
        val kotlinVersion: String by settings
        val protobufPluginVersion: String by settings
        val versionsPluginVersion: String by settings
        val bintrayPluginVersion: String by settings
        val shadowPluginVersion: String by settings
        val dokkaPluginVersion: String by settings
        val versioningPluginVersion: String by settings

        val detektVersion: String by settings
        val ktlintVersion: String by settings
        val kotlinterVersion: String by settings

        id("java")
        id("maven-publish")
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.google.protobuf") version protobufPluginVersion
        id("com.github.ben-manes.versions") version versionsPluginVersion
        id("com.jfrog.bintray") version bintrayPluginVersion
        id("com.github.johnrengelman.shadow") version shadowPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("fr.coppernic.versioning") version versioningPluginVersion

        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }
}
rootProject.name = "konnector"
