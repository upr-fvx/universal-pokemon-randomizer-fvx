// This root-level build.gradle contains build info that can be/is shared between the modules.

plugins {
    java
    id("io.github.file5.guidesigner") version "1.0.2"
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.named<Test>("test") {
        systemProperty("romsPath", rootProject.file("roms").absolutePath)
        useJUnitPlatform()
        ignoreFailures = true
    }
}

