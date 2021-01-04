import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import java.util.Properties

val localPropertiesFile = file("local.properties")
val localProperties = if (localPropertiesFile.exists()) {
    val properties = Properties()
    properties.load(localPropertiesFile.inputStream())
    properties
} else null

plugins {
    id("java")
    id("maven-publish")
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.protobuf")
    id("com.github.ben-manes.versions")
    id("com.jfrog.bintray")
    id("com.github.johnrengelman.shadow")
    id("org.jetbrains.dokka")
    id("fr.coppernic.versioning")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jmailen.kotlinter")
}

buildscript {
    val protobufPluginVersion: String by project
    repositories {
        mavenCentral()
    }
    dependencies {
        "classpath"("com.google.protobuf:protobuf-gradle-plugin:$protobufPluginVersion")
    }
}

val publicationName = "maven"
group = "net.nprod"
version = "0.1.26" + if (System.getProperty("snapshot")?.isEmpty() != false) {
    ""
} else {
    "-SNAPSHOT"
}

repositories {
    mavenCentral()
    jcenter()
    google()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://oss.jfrog.org/artifactory/oss-snapshot-local/")
}

dependencies {
    val serializationRuntimeVersion: String by project
    val kotlinLoggingVersion: String by project
    val ktorVersion: String by project
    val woodstoxVersion: String by project
    val junitApiVersion: String by project
    val javaxAnnotationVersion: String by project
    val slf4jVersion: String by project
// gnfinder
    val grpcVersion: String by project
    val grpcKotlinVersion: String by project
    val protobufVersion: String by project
    val guavaVersion: String by project

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", serializationRuntimeVersion)

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion") {
        exclude("org.slf4j")
    }

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$slf4jVersion")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("com.fasterxml.woodstox:woodstox-core:$woodstoxVersion")

    // Protobuf and grpc for gnfinder
    // We need this one as API because the client needs it
    api("com.google.guava:guava:$guavaVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    runtimeOnly("io.grpc:grpc-netty-shaded:$grpcVersion") {
        exclude("org.slf4j")
    }

    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitApiVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitApiVersion")
}

/**
 * Protobuf configuration (used for gnfinder)
 */

protobuf {
    val protobufVersion: String by project
    val grpcVersion: String by project
    val grpcKotlinVersion: String by project
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact =
                "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar" // Why jdk7 still dealing with that Android old crap?
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

// This is needed so IntelliJ finds the protobuf generated code

sourceSets {
    main {
        java.srcDir("${project.buildDir}/generated/source/proto/main/java")
        java.srcDir("${project.buildDir}/generated/source/proto/main/grpc")
        java.srcDir("${project.buildDir}/generated/source/proto/main/grpckt")
    }
}

/**
 * The compatibility section (minimal version of Java compatible)
 */

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    dokkaHtml.configure {
        outputDirectory.set(buildDir.resolve("dokka"))
    }

    withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
        dependsOn("build")
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka")
}

publishing {

    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
    repositories {
        localProperties?.let {
            val localMaven: String by it
            maven {
                name = "localDev"
                url = uri("file:///$localMaven")
            }
        }
    }
}

/**
 * Configuration of test framework
 */

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// For publishing

bintray {
    user = System.getenv("BINTRAY_USER") ?: System.getProperty("bintray.user")
    key = System.getenv("BINTRAY_KEY") ?: System.getProperty("bintray.key")
    dryRun = false
    publish = true
    setPublications(publicationName)
    pkg(
        delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
            repo = "Konnector"
            name = "konnector"
            setLicenses("MIT")
            vcsUrl = "https://github.com/bjonnh/konnector"
        }
    )
}

// Quality

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated/") }
        include("**/kotlin/**")
    }
}

tasks {
    "lintKotlinMain"(org.jmailen.gradle.kotlinter.tasks.LintTask::class) {
        exclude { element -> element.file.path.contains("generated/") }
    }
}

kotlinter {
    ignoreFailures = project.hasProperty("lintContinueOnError")
    experimentalRules = project.hasProperty("lintKotlinExperimental")
}

detekt {
    val detektVersion: String by project
    toolVersion = detektVersion
    config = rootProject.files("qc/detekt.yml")
    buildUponDefaultConfig = true
    baseline = rootProject.file("qc/detekt-baseline.xml")
}
