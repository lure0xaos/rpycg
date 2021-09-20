@file:Suppress("GradlePackageUpdate")

val appMainClass: String by project
val appModule: String by project
val appInstallerType: String by project
val appCopyright: String by project
val appVendor: String by project
val javaVersion: String by project
val javafxVersion: String by project
val iconPath: String by project

val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()

project.group = "gargoyle.renpy-cheat-generator"
project.description = """
    ''
""".trimIndent()
project.version = "1.0.0"

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version ("0.0.10")
    id("org.beryx.jlink") version ("2.24.2")
    kotlin("jvm") version "1.5.30"
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf("-Xdoclint:none", "-Xlint:all")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = javafxVersion
    }
}

javafx {
    version = javafxVersion
    modules(
        "javafx.controls",
        "javafx.fxml",
        "javafx.swing"
    )
}

application {
    mainClass.set(appMainClass)
    mainModule.set(appModule)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    implementation("org.openjfx:javafx-swing:$javafxVersion")
    implementation("org.projectlombok:lombok:1.18.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to appMainClass,
                "Implementation-Title" to appModule
            )
        )
    }
    archiveBaseName.set(appModule)
    archiveVersion.set(project.version.toString())
    configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = appModule
        mainClass.set(appMainClass)
    }
    jpackage {
        installerType = appInstallerType
        installerName = project.name
        appVersion = project.version.toString()
        if (os.isWindows) {
            icon = "$iconPath/RPyCGApp.ico"
            installerOptions = listOf(
                "--description", project.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
//                    "--win-console",
                "--win-dir-chooser",
                "--win-menu",
                "--win-per-user-install",
                "--win-shortcut"
            )
        }
        if (os.isLinux) {
            icon = "$iconPath/RPyCGApp.png"
            installerOptions = listOf(
                "--description", project.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
                "--linux-shortcut"
            )
        }
        if (os.isMacOsX) {
            icon = "$iconPath/RPyCGApp.png"
        }
    }
}

tasks.build {
    dependsOn.addAll(listOf(tasks.jpackage))
}
