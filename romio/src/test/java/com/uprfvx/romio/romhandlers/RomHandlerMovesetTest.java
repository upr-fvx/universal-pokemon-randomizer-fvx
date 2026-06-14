package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class RomHandlerMovesetTest extends RomHandlerTest {



    @ParameterizedTest
    @MethodSource("getRomNames")
    public void eggMovesDoNoNotChangeWithGetAndSet(String romName) {
        assumeFalse(getGenerationNumberOf(romName) == 1);
        loadROM(romName);
        Map<Integer, List<Integer>> before = deepCopyEggMoves(romHandler.getEggMoves());
        romHandler.setEggMoves(romHandler.getEggMoves());
        Map<Integer, List<Integer>> after = romHandler.getEggMoves();
        assertEquals(before, after);
    }

    private Map<Integer, List<Integer>> deepCopyEggMoves(Map<Integer, List<Integer>> original) {
        return original.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new java.util.ArrayList<>(entry.getValue())
                ));
    }

}
