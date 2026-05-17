package com.uprfvx.random;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free guardrails for Gen3 starter/rival option flow.
 */
class GameRandomizerStarterRivalSyncTest {

    @Test
    void gen3StarterRandomizationSyncsOpeningRivalWhenThroughGameOptionIsOff() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String trainerPokemonFlow = methodBody(source, "private void maybeRandomizeTrainerPokemon()");

        assertTrue(trainerPokemonFlow.contains("startersChanged && !rivalCarriesStarterThroughout"));
        assertTrue(trainerPokemonFlow.contains("romHandler.generationOfPokemon() == 3"));
        assertTrue(trainerPokemonFlow.contains("trainerPokeRandomizer.makeFirstRivalCarryStarter()"));
        assertTrue(trainerPokemonFlow.indexOf("trainerPokeRandomizer.randomizeTrainerPokes()")
                < trainerPokemonFlow.indexOf("trainerPokeRandomizer.makeFirstRivalCarryStarter()"),
                "Opening Rival starter sync must run after Trainer Pokemon randomization");
    }

    @Test
    void throughGameRivalStarterOptionStillUsesFullRivalCarryPath() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String trainerPokemonFlow = methodBody(source, "private void maybeRandomizeTrainerPokemon()");

        assertTrue(trainerPokemonFlow.contains("&& rivalCarriesStarterThroughout"));
        assertTrue(trainerPokemonFlow.contains("trainerPokeRandomizer.makeRivalCarryStarter()"));
    }

    private static Path gameRandomizerSourcePath() {
        Path moduleRelative = Path.of("src/main/java/com/uprfvx/random/GameRandomizer.java");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("random/src/main/java/com/uprfvx/random/GameRandomizer.java");
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
