// This line is a result of IDE recommendation, without quite understanding
// WHY it needs to import the Download class in addition to defining.
// If info about this is online (almost certainly) it is obscured in a way I couldn't find it.
// -- voliol 2026-03-04
import de.undercouch.gradle.tasks.download.Download

plugins {
    id("de.undercouch.download") version "5.7.0"
}
apply(plugin = "io.github.file5.guidesigner")

dependencies {
    // Internal dependencies
    implementation(project(":romio"))
    implementation(project(":utils"))
    testImplementation(testFixtures(project(":romio")))

    // Needed to compile the IntelliJ GUI Designer forms
    implementation("com.jetbrains.intellij.java:java-gui-forms-rt:+")
    implementation("com.jgoodies:forms:1.1-preview")

    // Other dependencies
    implementation("com.formdev:flatlaf:3.7")
}

tasks.jar {
    archiveFileName.set("UPR-FVX.jar")
    manifest {
        attributes("Main-Class" to "com.uprfvx.random.gui.RandomizerGUI")
    }

    // Makes the jar fat (include all runtime dependencies)
    dependsOn(configurations.runtimeClasspath)
    from(
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    ) {
        exclude("META-INF/LICENSE")
    }
}

tasks.register<Delete>("clearReleaseDir") {
    delete("build/target")
}

// We get the launcher here, so we can use the same JDK for running jlink and jdeps,
// as is used for compiling the program (the "toolchain" JDK).
val launcher = javaToolchains.launcherFor {
    languageVersion.set(java.toolchain.languageVersion)
}
val javaHome = launcher.map { it.metadata.installationPath }

// All of these are 64-bit
enum class PlatformConfig(
    val apiOS: String, val apiArchitecture: String,
    val launcherExtension: String,
    val jmodsInZip: Boolean = false
) {
    Linux_x86("linux", "x64", "sh"),
    Linux_ARM("linux", "aarch64", "sh"),
    Windows("windows", "x64", "bat", jmodsInZip = true),
    Mac_x86("mac", "x64", "command"),
    Mac_ARM("mac", "aarch64", "command")
}
fun taskName(cfg: PlatformConfig): String {
    return cfg.name.replace("_", "")
}

val zipTasks = mutableListOf<TaskProvider<Zip>>()
PlatformConfig.entries.forEach { cfg ->
    var extension = "tar.gz"
    var decompresser: (File) -> Any = { tarTree(it) }
    if (cfg.jmodsInZip) {
        extension = "zip"
        decompresser = { zipTree(it) }
    }

    val download = tasks.register<Download>("downloadJmods${taskName(cfg)}") {
        src("http://api.adoptium.net/v3/binary/latest/25/ga/${cfg.apiOS}/${cfg.apiArchitecture}/jmods/hotspot/normal/eclipse")
        dest(layout.buildDirectory.file("jmods/compressed/${cfg.name}.$extension"))
        overwrite(false)
    }

    val decompress = tasks.register<Copy>("decompressJmods${taskName(cfg)}") {
        dependsOn(download)
        from(download.map { t -> decompresser(t.dest) }) {
            // This removes the intermediary "JDK-[version]-jmods" directory,
            // which is nice because we don't know "[version]".
            eachFile {
                relativePath = RelativePath(relativePath.isFile,
                    relativePath.segments.drop(1).toString())
            }
            exclude("**-jmods")
        }
        into(layout.buildDirectory.dir("jmods/decompressed/${cfg.name}"))
    }

    val jlink = tasks.register<Exec>("jlink${taskName(cfg)}") {
        dependsOn(decompress)

        val modulePath = layout.buildDirectory.dir("jmods/decompressed/${cfg.name}")
        val outputDir = layout.buildDirectory.dir("java-runtime-image/${cfg.name}")
        val modules = "java.base,java.desktop,java.logging,java.naming"

        onlyIf {
            !outputDir.get().asFile.exists()
        }

        executable = javaHome.map { it.file("bin/jlink").asFile.absolutePath }.get()
        args(
                "--module-path", modulePath.get().asFile.absolutePath,
                "--add-modules", modules,
                "--output", outputDir.get().asFile.absolutePath
        )
    }

    val copy = tasks.register<Copy>("moveIntoReleaseDir${taskName(cfg)}") {
        dependsOn("jar")
        dependsOn("clearReleaseDir")
        dependsOn(jlink)

        from("src/main/resources") {
            exclude("data/players_unused")
            exclude("com")
        }
        val launcherName = "launcher.${cfg.launcherExtension}"
        from("src/main/launcher") {
            include("README.txt")
            expand("launcherName" to launcherName)
        }
        from("src/main/launcher") {
            include("**.${cfg.launcherExtension}")
            rename {launcherName}
        }
        from(layout.buildDirectory.dir("libs"))
        from(layout.buildDirectory.dir("java-runtime-image/${cfg.name}")) {
            into("java")
        }

        into(layout.buildDirectory.dir("target/${cfg.name}"))
    }

    val zip = tasks.register<Zip>("createReleaseZip${taskName(cfg)}") {
        dependsOn(copy)

        from(layout.buildDirectory.dir("target/${cfg.name}"))
        destinationDirectory = file("build/dist")
        archiveFileName = "UPR_FVX_${cfg.name}.zip" // TODO: make it include version
        // TODO: make tar.gz in MAC+Unix
    }
    zipTasks.add(zip)
}

tasks.register("createReleaseZips") {
    dependsOn(zipTasks)
}

tasks.register<Exec>("jdeps") {
    dependsOn(tasks.jar)

    executable = javaHome.map { it.file("bin/jdeps").asFile.absolutePath }.get()
    args(
        layout.buildDirectory.dir("libs").get().asFile.absolutePath + "/UPR-FVX.jar"
    )
}

fun detectPlatform(): PlatformConfig {
    // detects the platform you're running this on
    val name = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    return if (name.contains("Linux")) {
        if (arch == "aarch64") PlatformConfig.Linux_ARM else PlatformConfig.Linux_x86
    } else if (name.contains("Windows")) {
        PlatformConfig.Windows
    } else if (name.contains("Mac") || name.contains("mac")) {
        if (arch == "aarch64") PlatformConfig.Mac_ARM else PlatformConfig.Mac_x86
    } else {
        error("Unidentified platform: $name (arch: $arch)")
    }
}

tasks.register<Exec>("launch") {
    val platform: PlatformConfig = detectPlatform()
    dependsOn("moveIntoReleaseDir${taskName(platform)}")

    workingDir(layout.buildDirectory.dir("target/${platform.name}"))
    commandLine("bash", "./launcher.${platform.launcherExtension}")
}

tasks.register<Exec>("relaunch") {
    val platform: PlatformConfig = detectPlatform()

    workingDir(layout.buildDirectory.dir("target/${platform.name}"))
    commandLine("bash", "./launcher.${platform.launcherExtension}")
}

// TODO: why does this cause instrumentForms to fail???
tasks.named<Test>("test") {
    filter {
        excludeTestsMatching("*Randomizer*Test")
        excludeTestsMatching("*Updater*Test")
    }
}

// TODO: this shares a bunch of info with random:testROMs, would be nice to combine them
tasks.register<Test>("testROMs") {
    description = "Runs tests dependent on loading ROM files."
    group = "verification"

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    systemProperty("romsPath", rootProject.file("roms").absolutePath)

    shouldRunAfter("test")

    useJUnitPlatform()
    ignoreFailures = true

    filter {
        includeTestsMatching("*Randomizer*Test")
        includeTestsMatching("*Updater*Test")
    }
}

