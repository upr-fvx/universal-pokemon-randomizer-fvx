package com.uprfvx.random;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ROM-free guardrails for Gen3 starter/rival option flow.
 */
class GameRandomizerStarterRivalSyncTest {

    @Test
    void gen3TrainerPokemonMutationsAlwaysReapplyOpeningRivalCounterStarter() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String trainerPokemonFlow = methodBody(source, "private void maybeRandomizeTrainerPokemon()");

        assertTrue(trainerPokemonFlow.contains("boolean openingRivalCounterStarterNeedsReapply"));
        assertTrue(trainerPokemonFlow.contains("romHandler.generationOfPokemon() == 3"));
        assertTrue(trainerPokemonFlow.contains("&& (startersChanged || trainerPokemonRandomized)"));
        assertTrue(trainerPokemonFlow.contains("trainerPokeRandomizer.makeFirstRivalCarryStarter()"));
        assertTrue(trainerPokemonFlow.indexOf("trainerPokeRandomizer.randomizeTrainerPokes()")
                < trainerPokemonFlow.indexOf("trainerPokeRandomizer.makeFirstRivalCarryStarter()"),
                "Opening Rival starter sync must run after Trainer Pokemon randomization");
        assertFalse(trainerPokemonFlow.contains("startersChanged && !rivalCarriesStarterThroughout"),
                "Opening Rival counter-starter must not depend on Rival Carries Starter Through Game being off");
    }

    @Test
    void throughGameRivalStarterOptionStillUsesFullRivalCarryPath() throws IOException {
        String source = Files.readString(gameRandomizerSourcePath());
        String trainerPokemonFlow = methodBody(source, "private void maybeRandomizeTrainerPokemon()");

        assertTrue(trainerPokemonFlow.contains("&& rivalCarriesStarterThroughout"));
        assertTrue(trainerPokemonFlow.contains("trainerPokeRandomizer.makeRivalCarryStarter()"));
        assertTrue(trainerPokemonFlow.indexOf("trainerPokeRandomizer.makeRivalCarryStarter()")
                < trainerPokemonFlow.indexOf("trainerPokeRandomizer.randomizeTrainerPokes()"),
                "Rival starter should be installed before Trainer Pokemon randomization can protect it");
        assertTrue(trainerPokemonFlow.lastIndexOf("trainerPokeRandomizer.makeRivalCarryStarter()")
                > trainerPokemonFlow.indexOf("trainerPokeRandomizer.randomizeTrainerPokes()"),
                "Rival starter should be corrected after Trainer Pokemon randomization");
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
