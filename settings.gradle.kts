pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
    }

    resolutionStrategy {
        eachPlugin {

            if (requested.id.id == "org.jetbrains.dokka") {
                useModule("org.jetbrains.dokka:dokka-gradle-plugin:${requested.version}")
            }
        }
    }
}
rootProject.name = "konnector"
