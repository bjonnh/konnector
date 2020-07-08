pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()

        maven("https://plugins.gradle.org/m2/")
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
            if (requested.id.id == "org.jetbrains.dokka") {
                useModule("org.jetbrains.dokka:dokka-gradle-plugin:${requested.version}")
            }
        }
    }
}
rootProject.name = "konnector"
