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
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    @Suppress("GradlePackageUpdate")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    @Suppress("GradlePackageUpdate")
    implementation("org.openjfx:javafx-swing:$javafxVersion")

    implementation(kotlin("reflect"))

    implementation("org.jetbrains:annotations:23.0.0")
}
