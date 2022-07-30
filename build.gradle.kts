project.group = "gargoyle.renpy-cheat-generator"
project.description = """
    ''
""".trimIndent()
project.version = "1.0.0"

plugins {
    kotlin("jvm") version ("1.6.21")
    id("org.javamodularity.moduleplugin") version ("1.8.11")
    id("org.openjfx.javafxplugin") version ("0.0.13")
    id("org.beryx.jlink") version ("2.25.0")
}

repositories {
    mavenCentral()
}
