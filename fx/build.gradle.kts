@file:Suppress("GradlePackageUpdate")

val javaVersion: String by rootProject
val javafxVersion: String by rootProject

plugins {
    java
    id("org.openjfx.javafxplugin") version ("0.0.10")
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

repositories {
    mavenCentral()
}

dependencies {
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
