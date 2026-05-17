package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3WildEncounterTableScopeTest {

    @Test
    public void gen3WildEncounterReadWriteScopeIsBaseWildPokemonTableOnly() throws IOException {
        String source = Files.readString(gen3RomHandlerSourcePath());

        String getEncounters = methodBody(source, "public List<EncounterArea> getEncounters(boolean useTimeOfDay)");
        String setEncounters = methodBody(source, "public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas)");

        assertTrue(getEncounters.contains("romEntry.getIntValue(\"WildPokemon\")"));
        assertTrue(setEncounters.contains("romEntry.getIntValue(\"WildPokemon\")"));

        assertTrue(getEncounters.contains("readPointer(offs + 4"));
        assertTrue(getEncounters.contains("readPointer(offs + 8"));
        assertTrue(getEncounters.contains("readPointer(offs + 12"));
        assertTrue(getEncounters.contains("readPointer(offs + 16"));

        assertTrue(setEncounters.contains("writeEncounterArea(walkingOffset"));
        assertTrue(setEncounters.contains("writeEncounterArea(surfingOffset"));
        assertTrue(setEncounters.contains("writeEncounterArea(rockSmashOffset"));
        assertTrue(setEncounters.contains("writeEncounterArea(fishingOffsets"));

        assertFalse(getEncounters.contains("gWildMonDayHeaders"));
        assertFalse(getEncounters.contains("gWildMonNightHeaders"));
        assertFalse(getEncounters.contains("gWildMonMorningHeaders"));
        assertFalse(getEncounters.contains("gWildMonEveningHeaders"));
        assertFalse(setEncounters.contains("gWildMonDayHeaders"));
        assertFalse(setEncounters.contains("gWildMonNightHeaders"));
        assertFalse(setEncounters.contains("gWildMonMorningHeaders"));
        assertFalse(setEncounters.contains("gWildMonEveningHeaders"));
    }

    private static Path gen3RomHandlerSourcePath() {
        Path moduleRelative = Path.of("src/main/java/com/uprfvx/romio/romhandlers/Gen3RomHandler.java");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("romio/src/main/java/com/uprfvx/romio/romhandlers/Gen3RomHandler.java");
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
