package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvolutionMakeEasierDecisionTest {

    @Test
    public void condenseLevelEvolutionsCapsIntermediateAndFinalLevelEvolutions() {
        Species base = species(1);
        Species middle = species(2);
        Species finalStage = species(3);
        Evolution intermediate = evolution(base, middle, EvolutionType.LEVEL, 45);
        Evolution finalEvolution = evolution(middle, finalStage, EvolutionType.LEVEL, 60);

        int highestLevel = AbstractRomHandler.condenseLevelEvolutions(List.of(base, middle, finalStage), 60, 40,
                pk -> {});

        assertEquals(30, intermediate.getExtraInfo());
        assertEquals(30, intermediate.getEstimatedEvoLvl());
        assertEquals(40, finalEvolution.getExtraInfo());
        assertEquals(40, finalEvolution.getEstimatedEvoLvl());
        assertEquals(40, highestLevel);
    }

    @Test
    public void condenseLevelEvolutionsCapsEstimatedLevelsForNonLevelEvolutions() {
        Species base = species(4);
        Species middle = species(5);
        Species finalStage = species(6);
        Evolution intermediate = evolution(base, middle, EvolutionType.STONE, ItemIDs.fireStone, 50);
        Evolution finalEvolution = evolution(middle, finalStage, EvolutionType.TRADE, 0, 70);

        int highestLevel = AbstractRomHandler.condenseLevelEvolutions(List.of(base, middle, finalStage), 70, 40,
                pk -> {});

        assertEquals(ItemIDs.fireStone, intermediate.getExtraInfo());
        assertEquals(30, intermediate.getEstimatedEvoLvl());
        assertEquals(0, finalEvolution.getExtraInfo());
        assertEquals(40, finalEvolution.getEstimatedEvoLvl());
        assertEquals(40, highestLevel);
    }

    @Test
    public void condenseLevelEvolutionsDoesNothingWhenMaxLevelIsNotBelowHighestEvolutionLevel() {
        Species base = species(7);
        Species finalStage = species(8);
        Evolution evolution = evolution(base, finalStage, EvolutionType.LEVEL, 45);

        int highestLevel = AbstractRomHandler.condenseLevelEvolutions(List.of(base, finalStage), 45, 45,
                pk -> {});

        assertEquals(45, evolution.getExtraInfo());
        assertEquals(45, evolution.getEstimatedEvoLvl());
        assertEquals(45, highestLevel);
    }

    private static Species species(int number) {
        return new Species(number);
    }

    private static Evolution evolution(Species from, Species to, EvolutionType type, int extraInfo) {
        return evolution(from, to, type, extraInfo, type.usesLevelThreshold() ? extraInfo : 0);
    }

    private static Evolution evolution(Species from, Species to, EvolutionType type, int extraInfo, int estimatedLevel) {
        Evolution evolution = new Evolution(from, to, type, extraInfo, estimatedLevel);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
        return evolution;
    }
}
