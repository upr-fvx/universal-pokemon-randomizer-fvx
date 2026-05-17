package com.uprfvx.random;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free guardrails for option flow around Evolutions.
 */
class GameRandomizerEvolutionIsolationTest {

    @Test
    void movesetsAndTrainerNamesDoNotCallEvolutionMutatorsInGameRandomizerFlow() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String movesets = methodBody(source, "private void maybeRandomizeMovesets()");
        String trainerNames = methodBody(source, "private void maybeRandomizeTrainerNames()");

        assertFalse(movesets.contains("evoRandomizer"));
        assertFalse(movesets.contains("removeImpossibleEvolutions"));
        assertFalse(movesets.contains("condenseLevelEvolutions"));
        assertFalse(movesets.contains("makeEvolutionsEasier"));
        assertFalse(movesets.contains("removeTimeBasedEvolutions"));

        assertFalse(trainerNames.contains("evoRandomizer"));
        assertFalse(trainerNames.contains("removeImpossibleEvolutions"));
        assertFalse(trainerNames.contains("condenseLevelEvolutions"));
        assertFalse(trainerNames.contains("makeEvolutionsEasier"));
        assertFalse(trainerNames.contains("removeTimeBasedEvolutions"));
    }

    @Test
    void evolutionRandomizerAndEvolutionImprovementsAreOnlyBehindEvolutionSettings() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String evolutions = methodBody(source, "private void maybeRandomizeEvolutions()");
        String improvements = methodBody(source, "private void maybeApplyEvolutionImprovements()");

        assertTrue(evolutions.contains("settings.getEvolutionsMod() != Settings.EvolutionsMod.UNCHANGED"));
        assertTrue(evolutions.contains("evoRandomizer.randomizeEvolutions()"));

        assertTrue(improvements.contains("settings.isChangeImpossibleEvolutions()"));
        assertTrue(improvements.contains("romHandler.removeImpossibleEvolutions"));
        assertTrue(improvements.contains("settings.isMakeEvolutionsEasier()"));
        assertTrue(improvements.contains("romHandler.condenseLevelEvolutions"));
        assertTrue(improvements.contains("romHandler.makeEvolutionsEasier"));
        assertTrue(improvements.contains("settings.isRemoveTimeBasedEvolutions()"));
        assertTrue(improvements.contains("romHandler.removeTimeBasedEvolutions"));
    }

    @Test
    void evolutionLogSectionOnlyComesFromEvolutionRandomizerChanges() throws IOException {
        String source = Files.readString(randomizationLoggerSourcePath());
        String shouldLogEvolutions = methodBody(source, "private boolean shouldLogEvolutions()");

        assertTrue(shouldLogEvolutions.contains("return evoRandomizer.isChangesMade();"));
        assertFalse(shouldLogEvolutions.contains("speciesMovesetRandomizer"));
        assertFalse(shouldLogEvolutions.contains("trainerNameRandomizer"));
    }

    private static Path gameRandomizerSourcePath() {
        Path moduleRelative = Path.of("src/main/java/com/uprfvx/random/GameRandomizer.java");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("random/src/main/java/com/uprfvx/random/GameRandomizer.java");
    }

    private static Path randomizationLoggerSourcePath() {
        Path moduleRelative = Path.of("src/main/java/com/uprfvx/random/log/RandomizationLogger.java");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("random/src/main/java/com/uprfvx/random/log/RandomizationLogger.java");
    }

    private static String methodBody(String source, String signature) {
        int signatureIndex = source.indexOf(signature);
        assertTrue(signatureIndex >= 0, "Missing method signature: " + signature);

        int bodyStart = source.indexOf('{', signatureIndex);
        assertTrue(bodyStart >= 0, "Missing method body: " + signature);

        int depth = 0;
        for (int i = bodyStart; i < source.length(); i++) {
            char current = source.charAt(i);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return source.substring(bodyStart, i + 1);
                }
            }
        }
        throw new AssertionError("Unterminated method body: " + signature);
    }
}
