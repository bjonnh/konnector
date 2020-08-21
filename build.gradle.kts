import com.google.protobuf.gradle.*

val publicationName = "maven"
group = "net.nprod"
version = "0.1.4" + if (System.getProperty("snapshot")?.isEmpty() != false) {
    ""
} else {
    "-SNAPSHOT"
}

var serializationRuntimeVersion = "1.0.0-RC"
val kotlinLoggingVersion = "1.8.0.1"
var ktorVersion = "1.4.0"
val woodstoxVersion = "6.2.1"
val moshiVersion = "1.9.3"
val junitApiVersion = "5.6.0"
val javaxAnnotationVersion = "1.3.2"
// gnfinder
val grpcVersion = "1.30.2"
val grpcKotlinVersion = "0.1.4"
val protobufVersion = "3.12.2"
val coroutinesVersion = "1.4.0"

// bintray/jfrog

plugins {
    val kotlinVersion = "1.4.0"
    val protobufVersion = "0.8.12"
    id("java")
    id("maven-publish")
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("com.google.protobuf") version protobufVersion
    id("com.github.ben-manes.versions") version "0.28.0"
    id("com.jfrog.bintray") version "1.8.5"
    id("com.github.johnrengelman.shadow") version "2.0.2"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("fr.coppernic.versioning") version "3.1.2"
}

repositories {
    mavenCentral()

    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://oss.jfrog.org/artifactory/oss-snapshot-local/")
    jcenter()
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationRuntimeVersion") // JVM dependency

    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion") {
        exclude("org.slf4j")
    }

    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("com.fasterxml.woodstox:woodstox-core:$woodstoxVersion")

    // For net.nprod.connector.crossref
    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")

    // Protobuf and grpc for gnfinder
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
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion"
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
        outputDirectory = buildDir.resolve("dokka").path
    }

    withType<com.jfrog.bintray.gradle.tasks.BintrayUploadTask> {
        dependsOn("build")
    }
}


val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

/*val javadocJar by tasks.registering(Jar::class) {
    dependsOn("dokkaH")
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka")
}*/

publishing {
    publications {
        create<MavenPublication>(publicationName) {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
            artifact(sourcesJar.get())
            //artifact(javadocJar.get())
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
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig> {
        repo = "Konnector"
        name = "konnector"
        setLicenses("MIT")
        vcsUrl = "https://github.com/bjonnh/konnector"
    })
}