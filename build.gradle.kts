// This root-level build.gradle contains build info that can be/is shared between the modules.

plugins {
    java
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
        useJUnitPlatform()
        ignoreFailures = true
    }
}

