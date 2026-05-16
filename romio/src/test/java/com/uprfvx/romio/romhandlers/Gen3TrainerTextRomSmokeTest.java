package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3TrainerTextRomSmokeTest {

    private static final String ROM_PATH_PROPERTY = "uprfvx.gen3TrainerTextSmokeRom";
    private static final String ROM_PATH_ENV = "UPRFVX_GEN3_TRAINER_TEXT_SMOKE_ROM";

    @Test
    public void trainerTextWriterReloadSmokeOptIn() {
        String romPath = configuredRomPath();
        assumeTrue(romPath != null && !romPath.isBlank(),
                "Set -D" + ROM_PATH_PROPERTY + "=<private-rom> to run the Gen3 trainer text ROM smoke.");

        RomHandler romHandler = loadGen3Rom(romPath);
        exerciseTrainerNames(romHandler);
        exerciseTrainerClassNames(romHandler);
    }

    private static String configuredRomPath() {
        String property = System.getProperty(ROM_PATH_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(ROM_PATH_ENV);
    }

    private static RomHandler loadGen3Rom(String romPath) {
        RomHandler.Factory factory = new Gen3RomHandler.Factory();
        assertTrue(factory.isLoadable(romPath), "Configured ROM is not loadable as a Gen3 ROM.");
        RomHandler romHandler = factory.create();
        assertTrue(romHandler.loadRom(romPath), "Configured Gen3 ROM could not be loaded.");
        return romHandler;
    }

    private static void exerciseTrainerNames(RomHandler romHandler) {
        List<String> before = romHandler.getTrainerNames();
        assertFalse(before.isEmpty(), "Loaded ROM has no trainer names.");

        int originalMaxLength = romHandler.maxTrainerNameLength();
        String replacement = chooseReplacement(romHandler, originalMaxLength, before.get(0));
        assumeTrue(replacement != null, "Loaded ROM has no safe trainer-name replacement for the smoke.");

        List<String> edited = new ArrayList<>(before);
        edited.set(0, replacement);

        assertTrue(romHandler.internalStringLength(replacement) <= originalMaxLength);
        assertEquals(replacement.length(), romHandler.internalStringLength(replacement));

        romHandler.setTrainerNames(edited);
        romHandler.saveTrainers();
        romHandler.loadTrainers();

        List<String> reloaded = romHandler.getTrainerNames();
        assertEquals(before.size(), reloaded.size());
        assertEquals(replacement, reloaded.get(0));
        assertNotEquals(before.get(0), reloaded.get(0));
        assertEquals(originalMaxLength, romHandler.maxTrainerNameLength());
    }

    private static void exerciseTrainerClassNames(RomHandler romHandler) {
        List<String> before = romHandler.getTrainerClassNames();
        assertFalse(before.isEmpty(), "Loaded ROM has no trainer class names.");

        int originalMaxLength = romHandler.maxTrainerClassNameLength();
        String replacement = chooseReplacement(romHandler, originalMaxLength, before.get(0));
        assumeTrue(replacement != null, "Loaded ROM has no safe trainer-class replacement for the smoke.");

        List<String> edited = new ArrayList<>(before);
        edited.set(0, replacement);

        assertTrue(romHandler.internalStringLength(replacement) <= originalMaxLength);
        assertEquals(replacement.length(), romHandler.internalStringLength(replacement));

        romHandler.setTrainerClassNames(edited);

        List<String> reloaded = romHandler.getTrainerClassNames();
        assertEquals(before.size(), reloaded.size());
        assertEquals(replacement, reloaded.get(0));
        assertNotEquals(before.get(0), reloaded.get(0));
        assertEquals(originalMaxLength, romHandler.maxTrainerClassNameLength());
    }

    private static String chooseReplacement(RomHandler romHandler, int maxInternalLength, String original) {
        for (String candidate : List.of("AX", "BY", "CZ", "DEX", "TEST")) {
            int internalLength = romHandler.internalStringLength(candidate);
            if (!candidate.equals(original) && internalLength == candidate.length()
                    && internalLength <= maxInternalLength) {
                return candidate;
            }
        }
        return null;
    }
}
