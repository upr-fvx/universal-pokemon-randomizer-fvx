package com.dabomstew.pkromio;

import com.dabomstew.pkromio.exceptions.EncryptedROMException;
import com.dabomstew.pkromio.exceptions.InvalidROMException;
import com.dabomstew.pkromio.romhandlers.*;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class RomOpener {

    private static final RomHandler.Factory[] FACTORIES = new RomHandler.Factory[]{new Gen1RomHandler.Factory(), new Gen2RomHandler.Factory(),
            new Gen3RomHandler.Factory(), new Gen4RomHandler.Factory(), new Gen5RomHandler.Factory(),
            new Gen6RomHandler.Factory(), new Gen7RomHandler.Factory()};

    private Map<String, String> gameUpdates;
    private boolean extraMemoryAvailable;

    private EnumMap<InvalidROMException.Type, Consumer<File>> invalidROMResponses;
    private Consumer<File> extraMemoryNotAvailableResponse;
    private Consumer<File> encryptedROMResponse;
    private Consumer<File> exceptionResponse;
    private Consumer<File> unsupportedROMResponse;

    public void setGameUpdates(Map<String, String> gameUpdates) {
        this.gameUpdates = gameUpdates;
    }

    public void setExtraMemoryAvailable(boolean extraMemoryAvailable) {
        this.extraMemoryAvailable = extraMemoryAvailable;
    }

    public void setInvalidROMResponses(EnumMap<InvalidROMException.Type, Consumer<File>> invalidROMResponses) {
        this.invalidROMResponses = invalidROMResponses;
    }

    public void setExtraMemoryNotAvailableResponse(Consumer<File> extraMemoryNotAvailableResponse) {
        this.extraMemoryNotAvailableResponse = extraMemoryNotAvailableResponse;
    }

    public void setEncryptedROMResponse(Consumer<File> encryptedROMResponse) {
        this.encryptedROMResponse = encryptedROMResponse;
    }

    public void setExceptionResponse(Consumer<File> exceptionResponse) {
        this.exceptionResponse = exceptionResponse;
    }

    public void setUnsupportedROMResponse(Consumer<File> unsupportedROMResponse) {
        this.unsupportedROMResponse = unsupportedROMResponse;
    }

    /**
     * Returns null if the Rom could not be loaded.
     */
    public RomHandler openRomFile(File romFile) {
        RomHandler romHandler;

        try {
            FileFunctions.validateRomFile(romFile);
        } catch (InvalidROMException e) {
            invalidROMResponses.get(e.getType()).accept(romFile);
            return null;
        }

        for (RomHandler.Factory rhf : FACTORIES) {
            if (rhf.isLoadable(romFile.getAbsolutePath())) {
                romHandler = rhf.create();

                if (!extraMemoryAvailable && romHandler instanceof Abstract3DSRomHandler) {
                    extraMemoryNotAvailableResponse.accept(romFile);
                    return null;
                }

                // TODO: do something nice about threaded-ness, so we can have a spinning load icon
                try {
                    romHandler.loadRom(romFile.getAbsolutePath());
                    if (gameUpdates.containsKey(romHandler.getROMCode())) {
                        romHandler.loadGameUpdate(gameUpdates.get(romHandler.getROMCode()));
                    }
                    return romHandler;

                } catch (EncryptedROMException ex) {
                    encryptedROMResponse.accept(romFile);
                    return null;
                } catch (Exception ex) {
                    exceptionResponse.accept(romFile);
                    return null;
                }
            }
        }

        unsupportedROMResponse.accept(romFile);
        return null;
    }


}
