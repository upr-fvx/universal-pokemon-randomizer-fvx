package com.uprfvx.pkromio.graphics;

import com.uprfvx.pkromio.graphics.packs.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerCharacterGraphicsTest {

    private static final String TEST_CPG_PATH = "test/resources/players";

    private static final List<GraphicsPackEntry> cpgEntries = initCPGEntries();

    private static List<GraphicsPackEntry> initCPGEntries() {
        try {
            return GraphicsPackEntry.readAllFromFolder(TEST_CPG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GraphicsPackEntry getEntry(String name) {
        // yes this is O(N) but eh, N is like 8
        return cpgEntries.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No entry named " + name));
    }

    @Test
    public void fromSheetGiveSameImagesAsFromSeparate_Gen1() {
        Gen1PlayerCharacterGraphics separate = new Gen1PlayerCharacterGraphics(getEntry("Gen1-separate"));
        Gen1PlayerCharacterGraphics sheet = new Gen1PlayerCharacterGraphics(getEntry("Gen1-sheet"));
        assertEquals(separate, sheet);
    }

    @Test
    public void fromSheetGiveSameImagesAndPalsAsFromSeparate_Gen2() {
        Gen2PlayerCharacterGraphics separate = new Gen2PlayerCharacterGraphics(getEntry("Gen2-separate"));
        Gen2PlayerCharacterGraphics sheet = new Gen2PlayerCharacterGraphics(getEntry("Gen2-sheet"));
        assertEquals(separate, sheet);
    }

    @Test
    public void fromSheetGiveSameImagesAndPalsAsFromSeparate_RSE() {
        RSEPlayerCharacterGraphics separate = new RSEPlayerCharacterGraphics(getEntry("RSE-separate"));
        RSEPlayerCharacterGraphics sheet = new RSEPlayerCharacterGraphics(getEntry("RSE-sheet"));
        assertEquals(separate, sheet);
    }

    @Test
    public void fromSheetGiveSameImagesAndPalsAsFromSeparate_FRLG() {
        FRLGPlayerCharacterGraphics separate = new FRLGPlayerCharacterGraphics(getEntry("FRLG-separate"));
        FRLGPlayerCharacterGraphics sheet = new FRLGPlayerCharacterGraphics(getEntry("FRLG-sheet"));
        assertEquals(separate, sheet);
    }
}
