package com.upr_fvx.pkromio.graphics;

import com.upr_fvx.pkromio.graphics.packs.Gen2PlayerCharacterGraphics;
import com.upr_fvx.pkromio.graphics.packs.GraphicsPackEntry;
import com.upr_fvx.pkromio.graphics.palettes.Gen2SpritePaletteID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Gen2PlayerCharacterGraphicsTest {

    @Test
    public void spritePaletteIDIsSetCorrectly() {
        for (Gen2SpritePaletteID id : Gen2SpritePaletteID.values()) {
            String foo = "[Foo]\nSpritePalette=" + id.toString();
            GraphicsPackEntry entry = GraphicsPackEntry.readAllFromString(foo).get(0);
            Gen2PlayerCharacterGraphics pcg = new Gen2PlayerCharacterGraphics(entry);

            assertEquals(id, pcg.getSpritePaletteID());
        }
    }

    @Test
    public void spritePaletteIDIsSetCorrectlyFromLowercase() {
        for (Gen2SpritePaletteID id : Gen2SpritePaletteID.values()) {
            String foo = "[Foo]\nSpritePalette=" + id.toString().toLowerCase();
            GraphicsPackEntry entry = GraphicsPackEntry.readAllFromString(foo).get(0);
            Gen2PlayerCharacterGraphics pcg = new Gen2PlayerCharacterGraphics(entry);

            assertEquals(id, pcg.getSpritePaletteID());
        }
    }

    @Test
    public void spritePaletteIDIsSetCorrectlyWithSpacesAtEnd() {
        for (Gen2SpritePaletteID id : Gen2SpritePaletteID.values()) {
            String foo = "[Foo]\nSpritePalette=" + id.toString().toLowerCase() + "    ";
            GraphicsPackEntry entry = GraphicsPackEntry.readAllFromString(foo).get(0);
            Gen2PlayerCharacterGraphics pcg = new Gen2PlayerCharacterGraphics(entry);

            assertEquals(id, pcg.getSpritePaletteID());
        }
    }

}
