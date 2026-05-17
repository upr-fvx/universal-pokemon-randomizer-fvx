package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3EvolutionTableScopeTest {

    @Test
    public void gen3EvolutionReadWriteUsesSameInternalSpeciesIdentityMapping() throws IOException {
        String source = Files.readString(gen3RomHandlerSourcePath());
        String loadEvolutions = methodBody(source, "public void loadEvolutions()");
        String writeEvolutions = methodBody(source, "private void writeEvolutions()");
        String rowOffset = methodBody(source, "private int getEvolutionRowOffset(int baseOffset, Species species)");
        String slotsPerSpecies = methodBody(source, "private int getEvolutionSlotsPerSpecies()");
        String internalSpeciesId = methodBody(source, "private int getEvolutionInternalSpeciesId(Species species)");

        assertTrue(loadEvolutions.contains("int evoOffset = getEvolutionRowOffset(baseOffset, pk);"));
        assertTrue(loadEvolutions.contains("Species evolvingToSpecies = pokesInternal[evolvingTo];"));
        assertTrue(writeEvolutions.contains("int evoOffset = getEvolutionRowOffset(baseOffset, pk);"));
        assertTrue(writeEvolutions.contains("if (evosWritten == evolutionSlotsPerSpecies)"));
        assertTrue(writeEvolutions.contains("writeWord(evoOffset + 4, getEvolutionInternalSpeciesId(evo.getTo()))"));

        assertTrue(rowOffset.contains("getEvolutionInternalSpeciesId(species) * getEvolutionRowSize()"));
        assertTrue(slotsPerSpecies.contains("useCfruDpeGen9SpeciesCount ? CFRU_DPE_EVOLUTION_SLOTS_PER_MON"));
        assertTrue(slotsPerSpecies.contains("GEN3_EVOLUTION_SLOTS_PER_MON"));
        assertTrue(internalSpeciesId.contains("usesInternalSpeciesIdentityForExtendedBpreHack()"));
        assertTrue(internalSpeciesId.contains("return species.getSpeciesSetIdentityNumber();"));
        assertTrue(internalSpeciesId.contains("return pokedexToInternal[species.getNumber()];"));
    }

    @Test
    public void gen3SaveWritesEvolutionsOnlyAsPartOfSpeciesStatsSaveNotTrainerOrMovesetWriters() throws IOException {
        String source = Files.readString(gen3RomHandlerSourcePath());
        String saveSpeciesStats = methodBody(source, "public void saveSpeciesStats()");
        String saveTrainers = methodBody(source, "public void saveTrainers()");
        String setMovesLearnt = methodBody(source, "public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets)");

        assertTrue(saveSpeciesStats.contains("writeEvolutions();"));
        assertFalse(saveTrainers.contains("writeEvolutions"));
        assertFalse(setMovesLearnt.contains("writeEvolutions"));
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
