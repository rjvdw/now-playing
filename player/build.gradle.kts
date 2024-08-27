val jackson_version: String by project
val kotlinx_coroutines_version: String by project
val ktor_version: String by project
val woodstox_version: String by project

plugins {
    id("buildlogic.kotlin-library-conventions")
    kotlin("plugin.serialization") version "2.0.10"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("com.fasterxml.woodstox:woodstox-core:$woodstox_version")
}
