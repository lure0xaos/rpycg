@file:Suppress("GradlePackageUpdate")

val javaVersion: String by rootProject
val javafxVersion: String by rootProject

val appMainClass: String by project
val appModule: String by project
val appName: String by rootProject

val os: org.gradle.internal.os.OperatingSystem = org.gradle.internal.os.OperatingSystem.current()
val appInstallerType: String by rootProject
val appIconIco: String by project
val appIconPng: String by project
val appCopyright: String by rootProject
val appVendor: String by rootProject

plugins {
    java
    application
    id("org.openjfx.javafxplugin") version ("0.0.10")
    id("org.beryx.jlink") version ("2.24.2")
}

group = "gargoyle.renpy-cheat-generator"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
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
    implementation(project(":fx"))
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    implementation("org.openjfx:javafx-swing:$javafxVersion")
    implementation("org.projectlombok:lombok:1.18.20")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
    launcher {
        name = appName
        mainClass.set(appMainClass)
    }
    jpackage {
        installerType = appInstallerType
        installerName = appName
        appVersion = project.version.toString()
        if (os.isWindows) {
            icon = rootProject.file(appIconIco).path
            installerOptions = listOf(
                "--description", rootProject.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
                "--win-dir-chooser",
                "--win-menu",
                "--win-per-user-install",
                "--win-shortcut"
            )
        }
        if (os.isLinux) {
            icon = rootProject.file(appIconPng).path
            installerOptions = listOf(
                "--description", rootProject.description,
                "--copyright", appCopyright,
                "--vendor", appVendor,
                "--linux-shortcut"
            )
        }
        if (os.isMacOsX) {
            icon = rootProject.file(appIconPng).path
        }
    }
}

tasks.build {
    dependsOn.addAll(listOf(tasks.jpackage))
}
