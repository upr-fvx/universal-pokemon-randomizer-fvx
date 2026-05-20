package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3RunningShoesTweakPatchTest {

    private static final int[] CFRU_DPE_RUNNING_DISALLOWED_CAN_RUN_IN_BUILDINGS_PATTERN = new int[] {
            0x10, 0xB5, 0x0A, 0x4B, 0x04, 0x00, 0x0A, 0x48,
            -1, -1, -1, -1,
            0x00, 0x28, 0x01, 0xD1, 0x01, 0x20, 0x10, 0xBD,
            0x20, 0x00, 0x07, 0x4B,
            -1, -1, -1, -1,
            0x00, 0x28, 0xF7, 0xD1,
            0x06, 0x4B, 0xD8, 0x7D, 0x05, 0x38, 0x43, 0x42,
            0x58, 0x41, 0xF2, 0xE7
    };
    private static final int CFRU_DPE_RUNNING_FUNCTION_OFFSET = 0x100;
    private static final int CFRU_DPE_RUNNING_FLAG_BRANCH_OFFSET = CFRU_DPE_RUNNING_FUNCTION_OFFSET + 0x0E;
    private static final int CFRU_DPE_RUNNING_FLAG_BRANCH_BYPASS = 0xE001;

    @Test
    public void vanillaRunWithoutRunningShoesUsesExistingPrefixPatch() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        byte[] rom = new byte[0x100];
        int prefixOffset = 0x20;
        writeBytes(rom, prefixOffset, hexBytes(Gen3Constants.getRunningShoesCheckPrefix(Gen3Constants.RomType_FRLG)));
        writeWord(rom, prefixOffset + 0x12, 0xD123);

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", fireRedRomEntry());

        romHandler.applyMiscTweak(MiscTweak.RUN_WITHOUT_RUNNING_SHOES);

        assertEquals(0, readWord(rom, prefixOffset + 0x12));
    }

    @Test
    public void vanillaRunningShoesIndoorsUsesRunIndoorsOffset() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        byte[] rom = new byte[0x100];
        Gen3RomEntry romEntry = fireRedRomEntry();
        romEntry.putIntValue("RunIndoorsTweakOffset", 0x30);
        rom[0x30] = (byte) 0xFF;

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);

        romHandler.applyMiscTweak(MiscTweak.RUNNING_SHOES_INDOORS);

        assertEquals(0, rom[0x30]);
    }

    @Test
    public void cfruDpeRunWithoutRunningShoesBypassesRunningEnabledFlagBranch() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandlerWithRunningPattern();

        romHandler.applyMiscTweak(MiscTweak.RUN_WITHOUT_RUNNING_SHOES);

        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        assertEquals(CFRU_DPE_RUNNING_FLAG_BRANCH_BYPASS, readWord(rom, CFRU_DPE_RUNNING_FLAG_BRANCH_OFFSET));
    }

    @Test
    public void cfruDpeRunningTweaksAreAvailableOnlyForRecognizedRunningLogic() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandlerWithRunningPattern();

        int available = romHandler.miscTweaksAvailable();

        assertTrue(hasTweak(available, MiscTweak.RUN_WITHOUT_RUNNING_SHOES));
        assertTrue(hasTweak(available, MiscTweak.RUNNING_SHOES_INDOORS));
    }

    @Test
    public void cfruDpeRunningTweaksAreUnsupportedWhenRunningLogicIsUnrecognized() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandlerWithoutRunningPattern();

        int available = romHandler.miscTweaksAvailable();

        assertFalse(hasTweak(available, MiscTweak.RUN_WITHOUT_RUNNING_SHOES));
        assertFalse(hasTweak(available, MiscTweak.RUNNING_SHOES_INDOORS));
    }

    @Test
    public void cfruDpeRunningShoesIndoorsDoesNotUseVanillaRunIndoorsOffset() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandlerWithRunningPattern();
        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        rom[0x30] = (byte) 0xFF;

        assertTrue(Gen3RomHandler.cfruDpeRunningLogicAlreadyAllowsIndoorRunningForDiagnostics(rom));
        romHandler.applyMiscTweak(MiscTweak.RUNNING_SHOES_INDOORS);

        assertEquals((byte) 0xFF, rom[0x30]);
    }

    @Test
    public void cfruDpeRunningTweaksCanBeAppliedTogetherWithoutConflict() throws Exception {
        Gen3RomHandler romHandler = cfruDpeRomHandlerWithRunningPattern();
        byte[] rom = fieldValue(romHandler, "rom", byte[].class);
        rom[0x30] = (byte) 0xFF;

        romHandler.applyMiscTweak(MiscTweak.RUNNING_SHOES_INDOORS);
        romHandler.applyMiscTweak(MiscTweak.RUN_WITHOUT_RUNNING_SHOES);

        assertEquals((byte) 0xFF, rom[0x30]);
        assertEquals(CFRU_DPE_RUNNING_FLAG_BRANCH_BYPASS, readWord(rom, CFRU_DPE_RUNNING_FLAG_BRANCH_OFFSET));
    }

    private static boolean hasTweak(int available, MiscTweak tweak) {
        return (available & tweak.getValue()) != 0;
    }

    private static Gen3RomHandler cfruDpeRomHandlerWithRunningPattern() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        byte[] rom = new byte[0x200];
        writeCfruDpeRunningPattern(rom, CFRU_DPE_RUNNING_FUNCTION_OFFSET);
        Gen3RomEntry romEntry = fireRedRomEntry();
        romEntry.setRomCode("BPRE");
        romEntry.putIntValue("RunIndoorsTweakOffset", 0x30);

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "useCfruDpeGen9SpeciesCount", true);
        return romHandler;
    }

    private static Gen3RomHandler cfruDpeRomHandlerWithoutRunningPattern() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        byte[] rom = new byte[0x200];
        Gen3RomEntry romEntry = fireRedRomEntry();
        romEntry.setRomCode("BPRE");
        romEntry.putIntValue("RunIndoorsTweakOffset", 0x30);

        setField(romHandler, "rom", rom);
        setField(romHandler, "romEntry", romEntry);
        setField(romHandler, "useCfruDpeGen9SpeciesCount", true);
        return romHandler;
    }

    private static void writeCfruDpeRunningPattern(byte[] rom, int offset) {
        for (int i = 0; i < CFRU_DPE_RUNNING_DISALLOWED_CAN_RUN_IN_BUILDINGS_PATTERN.length; i++) {
            int value = CFRU_DPE_RUNNING_DISALLOWED_CAN_RUN_IN_BUILDINGS_PATTERN[i];
            rom[offset + i] = (byte) (value < 0 ? 0 : value);
        }
    }

    private static Gen3RomEntry fireRedRomEntry() throws Exception {
        for (Gen3RomEntry entry : Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini")) {
            if ("Fire Red (U) 1.0".equals(entry.getName())) {
                return new Gen3RomEntry(entry);
            }
        }
        throw new IllegalStateException("Fire Red (U) 1.0 ROM entry not found");
    }

    private static byte[] hexBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    private static void writeBytes(byte[] rom, int offset, byte[] values) {
        System.arraycopy(values, 0, rom, offset, values.length);
    }

    private static int readWord(byte[] rom, int offset) {
        return (rom[offset] & 0xFF) | ((rom[offset + 1] & 0xFF) << 8);
    }

    private static void writeWord(byte[] rom, int offset, int value) {
        rom[offset] = (byte) (value & 0xFF);
        rom[offset + 1] = (byte) ((value >>> 8) & 0xFF);
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
}
