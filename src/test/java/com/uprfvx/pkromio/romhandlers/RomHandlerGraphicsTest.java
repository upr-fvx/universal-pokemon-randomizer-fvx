package com.uprfvx.pkromio.romhandlers;

import com.uprfvx.pkromio.constants.Gen3Constants;
import com.uprfvx.pkromio.gamedata.Gen1Species;
import com.uprfvx.pkromio.gamedata.PlayerCharacterType;
import com.uprfvx.pkromio.gamedata.Species;
import com.uprfvx.pkromio.graphics.packs.*;
import com.uprfvx.pkromio.graphics.palettes.Palette;
import com.uprfvx.pkromio.graphics.palettes.SGBPaletteID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerGraphicsTest extends RomHandlerTest {

    private static final String TEST_CPG_PATH = "test/resources/players";
    
    private static class PaletteRecord {
        private final Palette normal, shiny;
        private final SGBPaletteID sgbID;

        public PaletteRecord(Species pk) {
            this.normal = pk.getNormalPalette() == null ? null : new Palette(pk.getNormalPalette());
            this.shiny = pk.getShinyPalette() == null ? null : new Palette(pk.getShinyPalette());
            // not pretty but super brief
            this.sgbID = pk instanceof Gen1Species ? ((Gen1Species) pk).getPaletteID() : null;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PaletteRecord)) return false;
            PaletteRecord that = (PaletteRecord) o;
            return Objects.equals(normal, that.normal) && Objects.equals(shiny, that.shiny);
        }

        @Override
        public int hashCode() {
            return Objects.hash(normal, shiny);
        }

        @Override
        public String toString() {
            if (sgbID == null) {
                return String.format("{N:%s, S:%s}", normal, shiny);
            } else {
                return String.format("{ID:%s}", sgbID);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void palettesDoNotChangeWithLoadAndSave(String romName) {
        loadROM(romName);
        // it always loads the palettes once, if possible

        Map<Species, PaletteRecord> records = new HashMap<>();
        romHandler.getSpeciesSetInclFormes()
                .forEach(pk -> records.put(pk, new PaletteRecord(pk)));

        romHandler.savePokemonPalettes();
        romHandler.loadPokemonPalettes();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(records.get(pk), new PaletteRecord(pk));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allPalettesCanBeChangedWithLoadAndSave(String romName) {
        assumeTrue(getGenerationNumberOf(romName) > 1);
        assumeTrue(getGenerationNumberOf(romName) < 6);
        loadROM(romName);
        // it always loads the palettes once, if possible

        int palSize = romHandler.getSpecies().get(1).getNormalPalette().size();
        Palette replacePalette = new Palette(palSize);

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            pk.setNormalPalette(replacePalette);
            pk.setShinyPalette(replacePalette);
        }
        romHandler.savePokemonPalettes();
        romHandler.loadPokemonPalettes();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(replacePalette, pk.getNormalPalette());
            assertEquals(replacePalette, pk.getShinyPalette());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allPalettesCanBeChangedWithLoadAndSaveGen1(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 1);
        loadROM(romName);
        // it always loads the palettes once, if possible

        SGBPaletteID replaceID = SGBPaletteID.BADGE;

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            ((Gen1Species) pk).setPaletteID(replaceID);
        }
        romHandler.savePokemonPalettes();
        romHandler.savePokemonPalettes();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(replaceID, ((Gen1Species) pk).getPaletteID());
        }
    }
    
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void dumpAllPokemonImages(String romName) {
        loadROM(romName);
        romHandler.dumpAllPokemonImages();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canSetCustomPlayerGraphicsWithoutThrowing(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasCustomPlayerGraphicsSupport());
        GraphicsPack pack = getCustomPlayerGraphicsPack();
        romHandler.setCustomPlayerGraphics(new CustomPlayerGraphics(pack, PlayerCharacterType.PC1));
        if (romHandler.hasMultiplePlayerCharacters()) {
            romHandler.setCustomPlayerGraphics(new CustomPlayerGraphics(pack, PlayerCharacterType.PC1));
        }
        assertTrue(true);
    }

    private static final List<GraphicsPackEntry> cpgEntries = initCPGEntries();

    private static List<GraphicsPackEntry> initCPGEntries() {
        try {
            return GraphicsPackEntry.readAllFromFolder(TEST_CPG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GraphicsPack getCustomPlayerGraphicsPack() {
        switch (romHandler.generationOfPokemon()) {
            case 1:
                return new Gen1PlayerCharacterGraphics(cpgEntries.get(0));
            case 2:
                return new Gen2PlayerCharacterGraphics(cpgEntries.get(1));
            case 3:
                Gen3RomHandler gen3RomHandler = (Gen3RomHandler) romHandler;
                return gen3RomHandler.getRomEntry().getRomType() == Gen3Constants.RomType_FRLG ?
                        new FRLGPlayerCharacterGraphics(cpgEntries.get(3)) :
                        new RSEPlayerCharacterGraphics(cpgEntries.get(2));
            default:
                return null;
        }
    }
}
