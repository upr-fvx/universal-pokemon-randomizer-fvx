package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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
        assertFalse(getEncounters.contains("gSwarmTable"));
        assertFalse(setEncounters.contains("gSwarmTable"));
    }

    @Test
    public void cfruRouteOneFrigibaxSourceIsSwarmNotBaseWildPokemonScope() throws IOException {
        Path cfruWildTablesPath = cfruSourcePath("src/Tables/wild_encounter_tables.c");
        Path cfruWildEncounterPath = cfruSourcePath("src/wild_encounter.c");
        Path cfruConfigPath = cfruSourcePath("src/config.h");

        assumeTrue(Files.isRegularFile(cfruWildTablesPath), "CFRU source tree is not available");
        assumeTrue(Files.isRegularFile(cfruWildEncounterPath), "CFRU source tree is not available");
        assumeTrue(Files.isRegularFile(cfruConfigPath), "CFRU source tree is not available");

        String wildTablesSource = Files.readString(cfruWildTablesPath);
        String wildEncounterSource = Files.readString(cfruWildEncounterPath);
        String configSource = Files.readString(cfruConfigPath);

        String swarmTable = initializerBody(wildTablesSource, "const struct SwarmData gSwarmTable[]");
        assertTrue(swarmTable.contains(".mapName = MAPSEC_ROUTE_1"));
        assertTrue(swarmTable.contains(".species = SPECIES_FRIGIBAX"));

        assertTrue(wildTablesSource.contains("const struct WildPokemonHeader gWildMonMorningHeaders[]"));
        assertTrue(wildTablesSource.contains("const struct WildPokemonHeader gWildMonDayHeaders[]"));
        assertTrue(wildTablesSource.contains("const struct WildPokemonHeader gWildMonEveningHeaders[]"));
        assertTrue(wildTablesSource.contains("const struct WildPokemonHeader gWildMonNightHeaders[]"));
        assertTrue(wildTablesSource.contains("#define FIRERED_GEN9_ENABLE_ROUTE1_CUSTOM_WILD 0"));

        assertTrue(configSource.contains("#define TIME_ENABLED"));
        assertTrue(configSource.contains("#define SWARM_CHANCE"));
        assertTrue(wildEncounterSource.contains("extern const struct SwarmData gSwarmTable[]"));
        assertTrue(wildEncounterSource.contains("TryGenerateSwarmMon(level, wildMonIndex"));
        assertTrue(wildEncounterSource.contains("mapName == GetCurrentRegionMapSectionId()"));
        assertTrue(wildEncounterSource.contains("Random() % 100 < SWARM_CHANCE"));

        String getEncounters = methodBody(Files.readString(gen3RomHandlerSourcePath()),
                "public List<EncounterArea> getEncounters(boolean useTimeOfDay)");
        String setEncounters = methodBody(Files.readString(gen3RomHandlerSourcePath()),
                "public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas)");
        assertFalse(getEncounters.contains("gSwarmTable"));
        assertFalse(setEncounters.contains("gSwarmTable"));
    }

    private static Path gen3RomHandlerSourcePath() {
        Path moduleRelative = Path.of("src/main/java/com/uprfvx/romio/romhandlers/Gen3RomHandler.java");
        if (Files.isRegularFile(moduleRelative)) {
            return moduleRelative;
        }
        return Path.of("romio/src/main/java/com/uprfvx/romio/romhandlers/Gen3RomHandler.java");
    }

    private static Path cfruSourcePath(String relativePath) {
        List<Path> candidates = List.of(
                Path.of("../CFRU-expansion", relativePath),
                Path.of("../../CFRU-expansion", relativePath),
                Path.of("02_external/CFRU-expansion", relativePath));
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return candidates.get(0);
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

    private static String initializerBody(String source, String signature) {
        int signatureIndex = source.indexOf(signature);
        assertTrue(signatureIndex >= 0, "Missing initializer signature: " + signature);

        int bodyStart = source.indexOf('{', signatureIndex);
        assertTrue(bodyStart >= 0, "Missing initializer body: " + signature);

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
        throw new AssertionError("Unterminated initializer body: " + signature);
    }
}
