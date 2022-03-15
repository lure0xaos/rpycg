val javaVersion: String by rootProject
val javafxVersion: String by rootProject

plugins {
    java
    kotlin("jvm")
    id("org.javamodularity.moduleplugin")
    id("org.openjfx.javafxplugin")
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

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    mavenCentral()
}

dependencies {
    @Suppress("GradlePackageUpdate")
    api(group = "org.openjfx", name = "javafx-controls", version = javafxVersion)
    @Suppress("GradlePackageUpdate")
    api(group = "org.openjfx", name = "javafx-fxml", version = javafxVersion)
    @Suppress("GradlePackageUpdate")
    api(group = "org.openjfx", name = "javafx-swing", version = javafxVersion)
    implementation(kotlin("reflect"))
}
