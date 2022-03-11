project.group = "gargoyle.renpy-cheat-generator"
project.description = """
    ''
""".trimIndent()
project.version = "1.0.0"

plugins {
    kotlin("jvm") version ("1.6.10")
    id("org.javamodularity.moduleplugin") version ("1.8.10")
    id("org.openjfx.javafxplugin") version ("0.0.12")
    id("org.beryx.jlink") version ("2.25.0")
}

repositories {
    mavenCentral()
}
