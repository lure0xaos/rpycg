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
    @Suppress("GradlePackageUpdate")
    implementation("org.openjfx:javafx-controls:$javafxVersion")
    @Suppress("GradlePackageUpdate")
    implementation("org.openjfx:javafx-fxml:$javafxVersion")
    @Suppress("GradlePackageUpdate")
    implementation("org.openjfx:javafx-swing:$javafxVersion")

    implementation("org.jetbrains:annotations:22.0.0")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.8.1")
    testImplementation("org.mockito:mockito-junit-jupiter:3.12.4")
}

tasks.test {
    useJUnitPlatform()
}
