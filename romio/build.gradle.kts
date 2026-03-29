plugins {
    id("java-test-fixtures")
}

dependencies {
    implementation(project(":utils"))
}

tasks.named<Test>("test") {
    filter {
        excludeTestsMatching("*RomHandler*Test")
    }
}

tasks.register<Test>("testROMs") {
    description = "Runs tests dependent on loading ROM files."
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    systemProperty("romsPath", rootProject.file("roms").absolutePath)

    shouldRunAfter("test")

    useJUnitPlatform()
    ignoreFailures = true
    maxHeapSize = "4G"

    filter {
        includeTestsMatching("*RomHandler*Test")
    }
}