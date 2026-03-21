import java.nio.charset.StandardCharsets

dependencies {
    implementation(project(":random")) // to get Version
}

val RELEASE_NOTES_PATH = "docs/src/_release_notes"

tasks.register("checkVersionUpdated") {
    dependsOn(":random:getVersionName")
    doLast {
        val releaseName = (rootProject.extra["randomizerVersion"] as String).replace(".", "_")
        val posReleaseNotePath = "$RELEASE_NOTES_PATH/$releaseName.md"
        println(File(posReleaseNotePath).absolutePath)
        if (File(posReleaseNotePath).exists()) {
            error("Could not finalize release note; file already exists with the file name the finalized release note " +
                    "would come to take: $posReleaseNotePath\n" +
                    "You might have forgotten to update the Randomizer Version.")
        }
    }
}

// identifies stray [] but not when used in markdown links
val SQUARE_BRACKET_REGEX = "(?<!\\])\\[[^\\]]*\\](?![\\[\\(])".toRegex()

tasks.register("checkReleaseNoteDone") {
    doLast {
        val rnPath = "$RELEASE_NOTES_PATH/release-note-next.md"
        val text = File(rnPath).readText(StandardCharsets.UTF_8)
        if (SQUARE_BRACKET_REGEX.find(text) != null) {
            val matches = SQUARE_BRACKET_REGEX.findAll(text)
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