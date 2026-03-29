import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter

dependencies {
    implementation(project(":random")) // to get Version
}

val releaseNotesPath = "docs/src/_release_notes"
val templatePath = "$releaseNotesPath/release-note-template.md"
val nextPath = "$releaseNotesPath/release-note-next.md"

// identifies stray [] but not when used in Markdown links
val squareBracketRegex = "(?!\\[VERSION\\])(?!\\[OS\\])(?<!\\])\\[[^\\]]*\\](?![\\[\\(])".toRegex()
// identifies html-style comments that end with newline
val commentRegex = "<!--[\\s\\S]*?-->\\r?\\n".toRegex()

// same as the names in random/build.gradle.kts#PlatformConfig,
// but importing them appeared unreasonably cumbersome
val platformNames = arrayOf("Windows", "Linux_x86", "Linux_ARM", "Mac_x86", "Mac_ARM")

tasks.register("checkVersionUpdated") {
    group = "release setup"
    dependsOn(":random:getVersionName")
    doLast {
        val releaseName = (rootProject.extra["randomizerVersion"] as String).replace(".", "_")
        val posReleaseNotePath = "$releaseNotesPath/$releaseName.md"
        println(File(posReleaseNotePath).absolutePath)
        if (File(posReleaseNotePath).exists()) {
            error("Could not finalize release note; file already exists with the file name the finalized release note " +
                    "would come to take: $posReleaseNotePath\n" +
                    "You might have forgotten to update the Randomizer Version.")
        }
    }
}

tasks.register("checkReleaseNoteDone") {
    group = "release setup"
    doLast {
        val text = File(nextPath).readText(StandardCharsets.UTF_8)
            .replace(commentRegex, "") // much easier than to use regex to ignore brackets in comments.
        if (squareBracketRegex.find(text) != null) {
            val matches = squareBracketRegex.findAll(text)
            var matchesString = ""
            matches.iterator().forEach {
                matchResult -> matchesString += "\t" + matchResult.value + "\n"
            }
            error("Could not finalize release note; it still contains stray square brackets []:\n$matchesString" +
                    "They must be cleaned up before continuing.")
        }
    }
}

tasks.register("checkFinalizeReleaseNote") {
    group = "release setup"
    dependsOn("checkVersionUpdated")
    dependsOn("checkReleaseNoteDone")
}

tasks.register("finalizeReleaseNote") {
    group = "release"
    dependsOn(":random:getVersionName")
    dependsOn("checkFinalizeReleaseNote")
    doLast {
        val releaseName = (rootProject.extra["randomizerVersion"] as String)
        val releaseNameDotless = releaseName.replace(".", "_")
        val releaseNamePath = "$releaseNotesPath/$releaseNameDotless.md"

        var text = File(nextPath).readText(StandardCharsets.UTF_8)
        val sb = StringBuilder()
        sb.append("---\n")
        sb.append("name: $releaseName\n")
        sb.append("date: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}\n")
        sb.append("download: ")
        var iter = platformNames.iterator()
        while (iter.hasNext()) {
            sb.append("https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/releases/download/" +
                    "${releaseName.replace("v", "vFVX")}/" +
                    "UPR_FVX-$releaseNameDotless-${iter.next()}.zip")
            if (iter.hasNext()) sb.append(", ") else sb.append("\n")
        }
        sb.append("download_short_names: ")
        iter = platformNames.iterator()
        while (iter.hasNext()) {
            sb.append(iter.next())
            if (iter.hasNext()) sb.append(", ") else sb.append("\n")
        }
        sb.append("---\n")
        sb.append(text.replace(commentRegex, "")
            .replace("[VERSION]", releaseNameDotless))

        File(releaseNamePath).writeText(sb.toString(), StandardCharsets.UTF_8)
        File(templatePath).copyTo(File(nextPath), true)
    }
}