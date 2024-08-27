val kotlinx_coroutines_version: String by project
val logback_version: String by project

plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation(project(":player"))
}

application {
    mainClass = "dev.rdcl.AppKt"
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.jar {
    dependsOn(":player:jar")

    manifest {
        attributes["Main-Class"] = "dev.rdcl.AppKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}
