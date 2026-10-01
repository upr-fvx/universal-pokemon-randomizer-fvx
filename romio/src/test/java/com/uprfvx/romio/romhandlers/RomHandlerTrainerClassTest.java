package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.TrainerClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RomHandlerTrainerClassTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerClassesAreNotEmpty(String romName) {
        loadROM(romName);
        System.out.println(romHandler.getTrainerClasses());
        assertFalse(romHandler.getTrainerClasses().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerClassesHaveUniqueIDs(String romName) {
        loadROM(romName);
        Map<Integer, TrainerClass> byID = new HashMap<>();
        for (TrainerClass tc : romHandler.getTrainerClasses()) {
            if (byID.containsKey(tc.getID())) {
                throw new IllegalStateException("Duplicate trainer class ID: " + tc.getID() + "."
                        + "Used by " + byID.get(tc.getID()) + " and " + tc);
            }
            byID.put(tc.getID(), tc);
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainerClassNamesDoNotChangeWithSaveAndLoad(String romName) {
        loadROM(romName);
        // trainer classes are always loaded once when the ROM is loaded
        List<String> before = romHandler.getTrainerClasses().stream().map(TrainerClass::getName).toList();
        romHandler.saveTrainerClasses();
        romHandler.loadTrainerClasses();
        List<String> after = romHandler.getTrainerClasses().stream().map(TrainerClass::getName).toList();
        assertEquals(before, after);
    }

}
