package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Species;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3IntroMonVisualSourceDiagnosticsTest {

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
