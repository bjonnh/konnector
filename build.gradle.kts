import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc


var serializationRuntimeVersion = "0.20.0-1.4-M2"
val kotlinLoggingVersion = "1.7.6"
var ktorVersion = "1.3.2-1.4-M2"
val woodstoxVersion = "5.2.0"
val moshiVersion = "1.9.3"
val junitApiVersion = "5.6.0"

// gnfinder
val grpcVersion = "1.30.0"
val grpcKotlinVersion = "0.1.4"
val protobufVersion = "3.12.2"
val coroutinesVersion = "1.3.7"

plugins {
    val kotlinVersion = "1.4-M2"
    val protobufVersion = "0.8.12"
    id("java")
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version "1.4-M2"
    id("com.google.protobuf") version protobufVersion
}

group = "net.prod"
version = "0.1-SNAPSHOT"

repositories {
    maven("https://dl.bintray.com/bjonnh/RDF4K")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("http://oss.jfrog.org/artifactory/oss-snapshot-local/")
    maven("https://jcenter.bintray.com")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")

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

    implementation("javax.annotation:javax.annotation-api:1.3.2")

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

// This is to have protobuf able to find the modules
// Source: https://github.com/google/protobuf-gradle-plugin/issues/391#issuecomment-609958243

configurations.forEach {
    if (it.name.toLowerCase().contains("proto")) {
        it.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "java-runtime"))
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