package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3IntroMonVisualSourceDiagnosticsTest {

    private static final int INTRO_CRY_OFFSET = 0x20;
    private static final int INTRO_IMAGE_OFFSET = 0x30;
    private static final int INTRO_PALETTE_OFFSET = 0x38;
    private static final int INTRO_OTHER_OFFSET = 0x40;
    private static final int IMAGE_TABLE_OFFSET = 0x100;
    private static final int PALETTE_TABLE_OFFSET = 0x3000;
    private static final int ROWLET_FRONT_ASSET = 0x5000;
    private static final int ROWLET_PALETTE_ASSET = 0x5100;

    @Test
    public void introMonVisualSourceDiagnosticsReadsKnownFrlgLiteralsAndPointers() {
        byte[] rom = new byte[512];
        Species[] species = speciesTable(25, 26);
        int imageTableOffset = 128;
        int paletteTableOffset = 256;

        rom[10] = 25;
        rom[30] = 25;
        writePointer(rom, 20, imageTableOffset + 25 * 8);
        writePointer(rom, 24, paletteTableOffset + 25 * 8);
        writePointer(rom, 40, paletteTableOffset + 26 * 8);

        Gen3RomHandler.Gen3IntroMonVisualSourceDiagnostics diagnostics =
                Gen3RomHandler.buildIntroMonVisualSourceDiagnostics(rom, 10, 20, 40, 30,
                        imageTableOffset, paletteTableOffset, species);

        assertEquals(5, diagnostics.candidates().size());
        assertCandidate(diagnostics.candidates(), "IntroCryOffset", 10, 25, -1, -1, "Species25");
        assertCandidate(diagnostics.candidates(), "IntroOtherOffset", 30, 25, -1, -1, "Species25");
        assertCandidate(diagnostics.candidates(), "IntroImageOffset/front-table", 20, -1,
                imageTableOffset + 25 * 8, 25, "Species25");
        assertCandidate(diagnostics.candidates(), "IntroImageOffset+4/palette-table", 24, -1,
                paletteTableOffset + 25 * 8, 25, "Species25");
        assertCandidate(diagnostics.candidates(), "IntroPaletteOffset/palette-table", 40, -1,
                paletteTableOffset + 26 * 8, 26, "Species26");
    }

    @Test
    public void introMonVisualSourceComparisonDetectsChangedAndUnchangedCandidates() {
        byte[] baseRom = new byte[512];
        byte[] outputRom = new byte[512];
        Species[] species = speciesTable(25, 26);
        int imageTableOffset = 128;
        int paletteTableOffset = 256;
        baseRom[10] = 25;
        outputRom[10] = 26;
        baseRom[30] = 25;
        outputRom[30] = 25;
        writePointer(baseRom, 20, imageTableOffset + 25 * 8);
        writePointer(outputRom, 20, imageTableOffset + 26 * 8);
        writePointer(baseRom, 24, paletteTableOffset + 25 * 8);
        writePointer(outputRom, 24, paletteTableOffset + 26 * 8);

        Gen3RomHandler.Gen3IntroMonVisualSourceDiagnostics base =
                Gen3RomHandler.buildIntroMonVisualSourceDiagnostics(baseRom, 10, 20, 24, 30,
                        imageTableOffset, paletteTableOffset, species);
        Gen3RomHandler.Gen3IntroMonVisualSourceDiagnostics output =
                Gen3RomHandler.buildIntroMonVisualSourceDiagnostics(outputRom, 10, 20, 24, 30,
                        imageTableOffset, paletteTableOffset, species);

        Gen3RomHandler.Gen3IntroMonVisualSourceComparison comparison =
                Gen3RomHandler.compareIntroMonVisualSourceDiagnostics(base, output);

        assertTrue(changed(comparison, "IntroCryOffset"));
        assertTrue(changed(comparison, "IntroImageOffset/front-table"));
        assertTrue(changed(comparison, "IntroImageOffset+4/palette-table"));
        assertTrue(comparison.candidates().stream()
                .anyMatch(candidate -> candidate.source().equals("IntroOtherOffset")
                        && !candidate.changedFromBase()));
    }

    @Test
    public void introPokemonInternalSpeciesIdUsesIdentityForExtendedBpreHack() {
        Species species = new Species(0);
        species.setName("ExtendedSpecies");
        species.setSpeciesSetIdentityNumber(300);

        int internalSpecies = Gen3RomHandler.getIntroPokemonInternalSpeciesId(species, new int[] {0, 1}, true);

        assertEquals(300, internalSpecies);
    }

    @Test
    public void introPokemonInternalSpeciesIdRejectsSpeciesZeroWithoutIdentityMode() {
        Species species = new Species(0);
        species.setName("Invalid");

        int internalSpecies = Gen3RomHandler.getIntroPokemonInternalSpeciesId(species, new int[] {0, 1}, false);

        assertEquals(0, internalSpecies);
    }

    @Test
    public void cfruDpeSetIntroPokemonAcceptsExtendedIdentityViaVisualPointerTable() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();
        Species rowlet = species(0, SpeciesIDs.rowlet, "Rowlet");

        assertTrue(romHandler.setIntroPokemon(rowlet));

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertEquals(IMAGE_TABLE_OFFSET + SpeciesIDs.rowlet * 8, readPointer(rom, INTRO_IMAGE_OFFSET));
        assertEquals(PALETTE_TABLE_OFFSET + SpeciesIDs.rowlet * 8, readPointer(rom, INTRO_IMAGE_OFFSET + 4));
        assertEquals(ROWLET_FRONT_ASSET, readPointer(rom,
                IMAGE_TABLE_OFFSET + SpeciesIDs.nidoranFemale * 8));
        assertEquals(ROWLET_PALETTE_ASSET, readPointer(rom,
                PALETTE_TABLE_OFFSET + SpeciesIDs.nidoranFemale * 8));
        assertEquals(0x7F, rom[INTRO_CRY_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[INTRO_OTHER_OFFSET] & 0xFF);
    }

    @Test
    public void cfruDpeSetIntroPokemonRejectsMissingExtendedIdentityWithoutWriting() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler();
        Species invalid = species(0, 0, "Invalid");

        assertFalse(romHandler.setIntroPokemon(invalid));

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertEquals(0x7F, rom[INTRO_CRY_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[INTRO_IMAGE_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[INTRO_IMAGE_OFFSET + 4] & 0xFF);
        assertEquals(0x7F, rom[INTRO_OTHER_OFFSET] & 0xFF);
    }

    @Test
    public void cfruDpeSetIntroPokemonRejectsExtendedIdentityWithInvalidVisualPointers() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandler(false, true);
        Species rowlet = species(0, SpeciesIDs.rowlet, "Rowlet");

        assertFalse(romHandler.setIntroPokemon(rowlet));

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertEquals(0x7F, rom[INTRO_CRY_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[INTRO_IMAGE_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[INTRO_IMAGE_OFFSET + 4] & 0xFF);
        assertEquals(0x7F, rom[INTRO_OTHER_OFFSET] & 0xFF);
        assertEquals(0x7F, rom[IMAGE_TABLE_OFFSET + SpeciesIDs.nidoranFemale * 8] & 0xFF);
        assertEquals(0x7F, rom[PALETTE_TABLE_OFFSET + SpeciesIDs.nidoranFemale * 8] & 0xFF);
    }

    @Test
    public void introMonVisualSourceSearchFindsChangedAndUnchangedCfruDpeCandidates() {
        byte[] baseRom = new byte[4096];
        byte[] outputRom = new byte[4096];
        Species[] species = speciesTable(29, 237);
        int imageTableOffset = 512;
        int paletteTableOffset = 1024;
        int nidoran = 29;
        int hitmontop = 237;
        int baseFrontAssetPointer = 1400;
        int outputFrontAssetPointer = 1500;
        int basePaletteAssetPointer = 1600;
        int outputPaletteAssetPointer = 1700;

        baseRom[10] = (byte) nidoran;
        outputRom[10] = (byte) hitmontop;
        writeWord(baseRom, 20, nidoran);
        writeWord(outputRom, 20, nidoran);
        writePointer(baseRom, 30, imageTableOffset + nidoran * 8);
        writePointer(outputRom, 30, imageTableOffset + hitmontop * 8);
        writePointer(baseRom, 40, baseFrontAssetPointer);
        writePointer(outputRom, 40, baseFrontAssetPointer);
        writePointer(baseRom, 50, paletteTableOffset + nidoran * 8);
        writePointer(outputRom, 50, paletteTableOffset + hitmontop * 8);
        writePointer(baseRom, 60, basePaletteAssetPointer);
        writePointer(outputRom, 60, outputPaletteAssetPointer);
        writePointer(baseRom, imageTableOffset + nidoran * 8, baseFrontAssetPointer);
        writePointer(outputRom, imageTableOffset + hitmontop * 8, outputFrontAssetPointer);
        writePointer(baseRom, paletteTableOffset + nidoran * 8, basePaletteAssetPointer);
        writePointer(outputRom, paletteTableOffset + hitmontop * 8, outputPaletteAssetPointer);

        List<Gen3RomHandler.Gen3IntroMonVisualSourceSearchCandidate> candidates =
                Gen3RomHandler.findIntroMonVisualSourceSearchCandidates(baseRom, outputRom, nidoran, hitmontop,
                        10, 30, 50, 20, imageTableOffset, paletteTableOffset, species);

        assertSearchCandidate(candidates, "raw-u8-species", 10, true);
        assertSearchCandidate(candidates, "raw-u16-species", 20, false);
        assertSearchCandidate(candidates, "front-table-entry-pointer", 30, true);
        assertSearchCandidate(candidates, "front-asset-pointer", 40, false);
        assertSearchCandidate(candidates, "palette-table-entry-pointer", 50, true);
        assertSearchCandidate(candidates, "palette-asset-pointer", 60, true);
        assertTrue(candidates.stream().anyMatch(candidate -> candidate.offset() == 40
                && candidate.baseValue().contains("base-front-asset")
                && candidate.outputValue().contains("base-front-asset")));
    }

    @Test
    public void cfruDpeIntroVisualSourcePointerTableEntryIsSyncedToTargetSpeciesAssetPointer() {
        byte[] rom = new byte[4096];
        int tableOffset = 128;
        int visibleSourceSpecies = 29;
        int targetSpecies = 237;
        int visibleSourceEntryOffset = tableOffset + visibleSourceSpecies * 8;
        int targetEntryOffset = tableOffset + targetSpecies * 8;
        int oldVisibleSourcePointer = 1400;
        int targetPointer = 1800;
        writePointer(rom, visibleSourceEntryOffset, oldVisibleSourcePointer);
        writePointer(rom, targetEntryOffset, targetPointer);

        boolean synced = Gen3RomHandler.syncCfruDpeIntroVisualSourcePointerTableEntry(
                rom, tableOffset, visibleSourceSpecies, targetSpecies);

        assertTrue(synced);
        assertEquals(targetPointer, readPointer(rom, visibleSourceEntryOffset));
    }

    @Test
    public void cfruDpeIntroVisualSourcePointerTableEntrySkipsInvalidTargets() {
        byte[] rom = new byte[4096];
        int tableOffset = 128;
        int visibleSourceSpecies = 29;
        int targetSpecies = 237;
        int visibleSourceEntryOffset = tableOffset + visibleSourceSpecies * 8;
        int oldVisibleSourcePointer = 1400;
        writePointer(rom, visibleSourceEntryOffset, oldVisibleSourcePointer);

        boolean synced = Gen3RomHandler.syncCfruDpeIntroVisualSourcePointerTableEntry(
                rom, tableOffset, visibleSourceSpecies, targetSpecies);

        assertFalse(synced);
        assertEquals(oldVisibleSourcePointer, readPointer(rom, visibleSourceEntryOffset));
    }

    private static void assertCandidate(List<Gen3RomHandler.Gen3IntroMonVisualSourceCandidate> candidates,
                                        String source, int offset, int rawSpeciesId, int pointer,
                                        int expectedSpeciesId, String decodedSpecies) {
        assertTrue(candidates.stream().anyMatch(candidate -> candidate.source().equals(source)
                && candidate.offset() == offset
                && candidate.rawSpeciesId() == rawSpeciesId
                && candidate.pointer() == pointer
                && candidate.expectedSpeciesId() == expectedSpeciesId
                && candidate.decodedSpecies().equals(decodedSpecies)));
    }

    private static void assertSearchCandidate(
            List<Gen3RomHandler.Gen3IntroMonVisualSourceSearchCandidate> candidates,
            String candidateType,
            int offset,
            boolean changedFromBase) {
        assertTrue(candidates.stream().anyMatch(candidate -> candidate.candidateType().equals(candidateType)
                && candidate.offset() == offset
                && candidate.changedFromBase() == changedFromBase));
    }

    private static boolean changed(Gen3RomHandler.Gen3IntroMonVisualSourceComparison comparison, String source) {
        return comparison.candidates().stream()
                .anyMatch(candidate -> candidate.source().equals(source) && candidate.changedFromBase());
    }

    private static Gen3RomHandler cfruDpeRomHandler() throws Exception {
        return cfruDpeRomHandler(true, true);
    }

    private static Gen3RomHandler cfruDpeRomHandler(boolean writeFrontPointer,
                                                    boolean writePalettePointer) throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Gen3RomEntry romEntry = fireRedRomEntry();
        romEntry.setRomCode("BPRE");
        romEntry.putIntValue("PokemonCount", Gen3Constants.unhackedMaxPokedex + 1);
        romEntry.putIntValue("PokemonFrontImages", IMAGE_TABLE_OFFSET);
        romEntry.putIntValue("PokemonNormalPalettes", PALETTE_TABLE_OFFSET);
        romEntry.putIntValue("IntroCryOffset", INTRO_CRY_OFFSET);
        romEntry.putIntValue("IntroImageOffset", INTRO_IMAGE_OFFSET);
        romEntry.putIntValue("IntroPaletteOffset", INTRO_PALETTE_OFFSET);
        romEntry.putIntValue("IntroOtherOffset", INTRO_OTHER_OFFSET);

        byte[] rom = new byte[0x6000];
        for (int i = 0; i < rom.length; i++) {
            rom[i] = (byte) 0x7F;
        }
        if (writeFrontPointer) {
            writePointer(rom, IMAGE_TABLE_OFFSET + SpeciesIDs.rowlet * 8, ROWLET_FRONT_ASSET);
        }
        if (writePalettePointer) {
            writePointer(rom, PALETTE_TABLE_OFFSET + SpeciesIDs.rowlet * 8, ROWLET_PALETTE_ASSET);
        }

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "pokedexToInternal", new int[] {0, 1});
        setField(romHandler, "isRomHack", true);
        setField(romHandler, "useCfruDpeGen9SpeciesCount", true);
        return romHandler;
    }

    private static Species species(int number, int speciesSetIdentityNumber, String name) {
        Species species = new Species(number);
        species.setSpeciesSetIdentityNumber(speciesSetIdentityNumber);
        species.setName(name);
        return species;
    }

    private static Gen3RomEntry fireRedRomEntry() throws Exception {
        for (Gen3RomEntry entry : Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini")) {
            if ("Fire Red (U) 1.0".equals(entry.getName())) {
                return new Gen3RomEntry(entry);
            }
        }
        throw new IllegalStateException("Fire Red (U) 1.0 ROM entry not found");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static <T> T fieldValue(Object target, String name, Class<T> fieldType) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return fieldType.cast(field.get(target));
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void writeWord(byte[] rom, int offset, int value) {
        rom[offset] = (byte) (value & 0xFF);
        rom[offset + 1] = (byte) ((value >>> 8) & 0xFF);
    }

    private static void writePointer(byte[] rom, int offset, int value) {
        int pointer = value + 0x8000000;
        rom[offset] = (byte) (pointer & 0xFF);
        rom[offset + 1] = (byte) ((pointer >>> 8) & 0xFF);
        rom[offset + 2] = (byte) ((pointer >>> 16) & 0xFF);
        rom[offset + 3] = (byte) ((pointer >>> 24) & 0xFF);
    }

    private static int readPointer(byte[] rom, int offset) {
        int rawPointer = (rom[offset] & 0xFF)
                + ((rom[offset + 1] & 0xFF) << 8)
                + ((rom[offset + 2] & 0xFF) << 16)
                + (((rom[offset + 3] & 0xFF)) << 24);
        return rawPointer - 0x8000000;
    }

    private static Species[] speciesTable(int... rawSpeciesIds) {
        int maxSpeciesId = 0;
        for (int rawSpeciesId : rawSpeciesIds) {
            maxSpeciesId = Math.max(maxSpeciesId, rawSpeciesId);
        }
        Species[] species = new Species[maxSpeciesId + 1];
        for (int rawSpeciesId : rawSpeciesIds) {
            Species entry = new Species(rawSpeciesId);
            entry.setName("Species" + rawSpeciesId);
            entry.setSpeciesSetIdentityNumber(rawSpeciesId);
            species[rawSpeciesId] = entry;
        }
        return species;
    }
}
