package com.dabomstew.pkromio;

import com.dabomstew.pkromio.exceptions.EncryptedROMException;
import com.dabomstew.pkromio.romhandlers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class RomOpener {

    private static final RomHandler.Factory[] FACTORIES = new RomHandler.Factory[]{new Gen1RomHandler.Factory(), new Gen2RomHandler.Factory(),
            new Gen3RomHandler.Factory(), new Gen4RomHandler.Factory(), new Gen5RomHandler.Factory(),
            new Gen6RomHandler.Factory(), new Gen7RomHandler.Factory()};

    public enum FailType {
        UNREADABLE,
        INVALID_TOO_SHORT,
        INVALID_ZIP,
        INVALID_RAR,
        INVALID_IPS,
        EXTRA_MEMORY_NOT_AVAILABLE, ENCRYPTED_ROM, UNSUPPORTED_ROM,
        UNDEFINED_FAILURE
    }

    public static class Results {
        private static Results success(RomHandler romHandler) {
            Results r = new Results();
            r.romHandler = romHandler;
            return r;
        }

        private static Results failure(FailType failType) {
            Results r = new Results();
            r.failType = failType;
            return r;
        }

        private RomHandler romHandler;

        private FailType failType;

        public boolean wasOpeningSuccessful() {
            return romHandler != null;
        }

        public RomHandler getRomHandler() {
            if (!wasOpeningSuccessful()) {
                throw new IllegalStateException("opening the ROM file failed - romHandler not initialized");
            }
            return romHandler;
        }

        public FailType getFailType() {
            if (wasOpeningSuccessful()) {
                throw new IllegalStateException("opening the ROM file succeeded - no FailType");
            }
            return failType;
        }
    }

    private Map<String, String> gameUpdates;
    private boolean extraMemoryAvailable;

    public void setGameUpdates(Map<String, String> gameUpdates) {
        this.gameUpdates = gameUpdates;
    }

    public void setExtraMemoryAvailable(boolean extraMemoryAvailable) {
        this.extraMemoryAvailable = extraMemoryAvailable;
    }

    public Results openRomFile(File romFile) {
        RomHandler romHandler;

        FailType invalidity = detectInvalidROM(romFile);
        if (invalidity != null) {
            return Results.failure(invalidity);
        }

        for (RomHandler.Factory rhf : FACTORIES) {
            if (rhf.isLoadable(romFile.getAbsolutePath())) {
                romHandler = rhf.create();

                // TODO: this instanceof is not pretty
                if (!extraMemoryAvailable && romHandler instanceof Abstract3DSRomHandler) {
                    return Results.failure(FailType.EXTRA_MEMORY_NOT_AVAILABLE);
                }

                try {
                    romHandler.loadRom(romFile.getAbsolutePath());
                    if (gameUpdates.containsKey(romHandler.getROMCode())) {
                        romHandler.loadGameUpdate(gameUpdates.get(romHandler.getROMCode()));
                    }
                    return Results.success(romHandler);

                } catch (EncryptedROMException e) {
                    return Results.failure(FailType.ENCRYPTED_ROM);
                } catch (Exception e) {
                    return Results.failure(FailType.UNDEFINED_FAILURE);
                }
            }
        }

        return Results.failure(FailType.UNSUPPORTED_ROM);
    }

    /**
     * Checks for common filetypes that aren't ROMs,
     * by reading the first 10 bytes of the file.<br>
     * Returns a {@link FailType}, or null if it could not find any faults.
     */
    private FailType detectInvalidROM(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            byte[] sig = new byte[10];
            int sigLength = fis.read(sig);
            fis.close();
            if (sigLength < 10) {
                return FailType.INVALID_TOO_SHORT;
            }
            if (sig[0] == 0x50 && sig[1] == 0x4b && sig[2] == 0x03 && sig[3] == 0x04) {
                return FailType.INVALID_ZIP;
            }
            if (sig[0] == 0x52 && sig[1] == 0x61 && sig[2] == 0x72 && sig[3] == 0x21 && sig[4] == 0x1A
                    && sig[5] == 0x07) {
                return FailType.INVALID_RAR;
            }
            if (sig[0] == 'P' && sig[1] == 'A' && sig[2] == 'T' && sig[3] == 'C' && sig[4] == 'H') {
                return FailType.INVALID_ZIP;
            }
        } catch (IOException ex) {
            return FailType.UNREADABLE;
        }

        return null;
    }

}
