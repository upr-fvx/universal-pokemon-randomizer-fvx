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

PlatformConfig.entries.forEach { cfg ->
    val taskName = cfg.name.replace("_", "")
    var extension = "tar.gz"
    var decompresser: (File) -> Any = { tarTree(it) }
    if (cfg.jmodsInZip) {
        extension = "zip"
        decompresser = { zipTree(it) }
    }

    val download = tasks.register<Download>("downloadJmods$taskName") {
        src("http://api.adoptium.net/v3/binary/latest/25/ga/${cfg.apiOS}/${cfg.apiArchitecture}/jmods/hotspot/normal/eclipse")
        dest(layout.buildDirectory.file("jmods/compressed/${cfg.name}.$extension"))
        overwrite(false)
    }

    val decompress = tasks.register<Copy>("decompressJmods$taskName") {
        dependsOn(download)
        from(download.map { t -> decompresser(t.dest) }) {
            // This removes the intermediary "JDK-[version]-jmods" directory,
            // which is nice because we don't know "[version]".
            eachFile {
                relativePath = RelativePath(relativePath.isFile(), relativePath.segments.tail())
            }
            exclude("**-jmods")
        }
        into(layout.buildDirectory.dir("jmods/decompressed/${cfg.name}"))
    }

    val jlink = tasks.register<Exec>("jlink$taskName") {
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

    val copy = tasks.register<Copy>("moveIntoReleaseDir$taskName") {
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

    tasks.register<Zip>("createReleaseZip$taskName") {
        dependsOn(copy)

        from(layout.buildDirectory.dir("target/${cfg.name}"))
        destinationDirectory = file("build/dist")
        archiveFileName = "UPR_FVX_${cfg.name}.zip" // TODO: make it include version
        // TODO: make tar.gz in MAC+Unix
    }
}

tasks.register("createReleaseZips") {
//    dependsOn(
//        createReleaseZipLinuxx86,
//        createReleaseZipLinuxARM,
//        createReleaseZipWindows,
//        createReleaseZipMacx86,
//        createReleaseZipMacARM
//    )
}

// The jlink utility is used to create a "Java Runtime Image", essentially a mini-JDK/JRE
// that contains only the modules of Java that the program uses.
// By depending on the "Java Runtime Image", developers can write in modern Java
// (previously, the Randomizer was locked to Java 8 since that's the newest version that gets
// official Oracle JREs, and low-tech end users WILL download from the "download Java" page),
// and end users do not have to download any JRE at all!

// The jdeps utility is used to figure out which Java modules the program uses.
// While very useful, it does not always work. Any module "not found" must be manually identified.

// The jlink task currently doesn't have a way of checking if it is "UP-TO-DATE", beyond checking
// if a "java-runtime-image" directory already exists. As the "Java Runtime Image" could become
// outdated, whenever a new dependency is added or the Randomizer's own code starts relying on
// additional Java modules, every now and again jdeps must be ran to confirm the list of modules
// is conclusive. If it isn't, the module list must be updated and the "java-runtime-image" dir
// deleted.
// TODO: figure out how often "every now and again" is

//tasks.register("jdeps", Exec) {
//    dependsOn moveIntoReleaseDir
//
//    executable = javaHome.map { it.file("bin/jdeps").asFile.absolutePath }.get()
//    args = [
//            layout.buildDirectory.dir("target").get().asFile.absolutePath + "/UPR-FVX.jar"
//    ]
//}

fun detectPlatform(): PlatformConfig {
    // detects the platform you're running this on
    val name = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val platform: PlatformConfig;
    if (name.contains("Linux")) {
        platform = if (arch == "aarch64") PlatformConfig.Linux_ARM else PlatformConfig.Linux_x86
    } else if (name.contains("Windows")) {
        platform = PlatformConfig.Windows
    } else if (name.contains("Mac") || name.contains("mac")) {
        platform = if (arch == "aarch64") PlatformConfig.Mac_ARM else PlatformConfig.Mac_x86
    } else {
        error("Unidentified platform: " + name + " (arch: " + arch + ")")
    }
    return platform
}

tasks.register<Exec>("launch") {
    val platform: PlatformConfig = detectPlatform()
    println(platform)

    println("./launcher.$platform.launcherExtension")

    dependsOn("moveIntoReleaseDirLinuxx86")

    workingDir("build/target/Linux_x86")
    commandLine("bash", "./launcher.$platform.launcherExtension")
}

tasks.register<Exec>("relaunch") {
    workingDir("build/target")
    commandLine("bash", "./launcher.sh")
}

