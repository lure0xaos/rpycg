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
    kotlin("jvm")
    application
    id("org.javamodularity.moduleplugin")
    id("org.openjfx.javafxplugin")
    id("org.beryx.jlink")
}

group = rootProject.group
version = rootProject.version

java {
    modularity.inferModulePath.set(true)
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

tasks.compileJava {
    options.encoding = Charsets.UTF_8.toString()
}

tasks.compileKotlin {
    destinationDirectory.set(tasks.compileJava.get().destinationDirectory)
    targetCompatibility = javaVersion
    kotlinOptions {
        jvmTarget = javaVersion
    }
}

javafx {
    version = javafxVersion
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set(appMainClass)
    mainModule.set(appModule)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":fx"))
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
    dependsOn += tasks.jpackage
}

tasks.startScripts {
    (unixStartScriptGenerator as org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator).template =
        resources.text.fromFile("unixStartScript.txt")
    (windowsStartScriptGenerator as org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator).template =
        resources.text.fromFile("windowsStartScript.txt")
}
