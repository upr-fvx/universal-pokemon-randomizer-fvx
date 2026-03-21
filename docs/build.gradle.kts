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
val squareBracketRegex = "(?<!\\])\\[[^\\]]*\\](?![\\[\\(])".toRegex()
// identifies html-style comments that end with newline
val commentRegex = "<!--[\\s\\S]*?-->\\r?\\n".toRegex()

tasks.register("checkVersionUpdated") {
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
    doLast {
        val text = File(nextPath).readText(StandardCharsets.UTF_8)
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
    dependsOn("checkVersionUpdated")
    dependsOn("checkReleaseNoteDone")
}

tasks.register("finalizeReleaseNote") {
    dependsOn(":random:getVersionName")
    dependsOn("checkFinalizeReleaseNote")
    doLast {
        val releaseName = (rootProject.extra["randomizerVersion"] as String).replace(".", "_")
        val releaseNamePath = "$releaseNotesPath/$releaseName.md"

        var text = File(nextPath).readText(StandardCharsets.UTF_8)
        val sb = StringBuilder()
        sb.append("---\n")
        sb.append("name: ${rootProject.extra["randomizerVersion"] as String}\n")
        sb.append("date: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}\n")
        sb.append("---\n")
        sb.append(text.replace(commentRegex, ""))

        File(releaseNamePath).writeText(sb.toString(), StandardCharsets.UTF_8)
        File(templatePath).copyTo(File(nextPath), true)
    }
}