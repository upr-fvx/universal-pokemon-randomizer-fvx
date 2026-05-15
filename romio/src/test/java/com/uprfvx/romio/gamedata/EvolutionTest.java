package com.uprfvx.romio.gamedata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvolutionTest {

    private static final int oldExtraInfo = 1;
    private static final int estimatedEvoLvl = 2;
    private static final int newExtraInfo = 3;

    @Test
    public void updateEvolutionMethodWorksIfEvoBeforeAndAfterDoNotUseLevelThreshold() {
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.TRADE, oldExtraInfo, estimatedEvoLvl);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.STONE, newExtraInfo, false);
        System.out.println("Evo after (estimatedEvoLvl must stay unchanged): " + evo);
        assertEquals(EvolutionType.STONE, evo.getType());
        assertEquals(newExtraInfo, evo.getExtraInfo());
        assertEquals(estimatedEvoLvl, evo.getEstimatedEvoLvl());
    }

    @Test
    public void updateEvolutionMethodWorksIfEvoBeforeUsesLevelThresholdAndEvoAfterDoesNot() {
        // Prerequisite: For evolutions that use level threshold, extraInfo == estimatedEvolvl
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.LEVEL, oldExtraInfo, oldExtraInfo);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.STONE, newExtraInfo, false);
        System.out.println("Evo after (estimatedEvoLvl must stay unchanged): " + evo);
        assertEquals(EvolutionType.STONE, evo.getType());
        assertEquals(newExtraInfo, evo.getExtraInfo());
        assertEquals(oldExtraInfo, evo.getEstimatedEvoLvl());
    }

    @Test
    public void updateEvolutionMethodWorksIfEvoBeforeAndAfterUseLevelThreshold() {
        // Prerequisite: For evolutions that use level threshold, extraInfo == estimatedEvolvl
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.LEVEL_DAY, oldExtraInfo, oldExtraInfo);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.LEVEL_NIGHT, newExtraInfo, false);
        System.out.println("Evo after (estimatedEvoLvl must update and equal extraInfo): " + evo);
        assertEquals(EvolutionType.LEVEL_NIGHT, evo.getType());
        assertEquals(newExtraInfo, evo.getExtraInfo());
        assertEquals(newExtraInfo, evo.getEstimatedEvoLvl());
    }

    @Test
    public void updateEvolutionMethodWorksIfEvoBeforeDoesNotUseLevelThresholdAndEvoAfterDoes() {
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.TRADE, oldExtraInfo, estimatedEvoLvl);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.LEVEL, newExtraInfo, false);
        System.out.println("Evo after (estimatedEvoLvl must update and equal extraInfo): " + evo);
        assertEquals(EvolutionType.LEVEL, evo.getType());
        assertEquals(newExtraInfo, evo.getExtraInfo());
        assertEquals(newExtraInfo, evo.getEstimatedEvoLvl());
    }

    @Test
    public void useEstimatedLevelHasNoEffectOnUpdateEvolutionMethodIfNewEvolutionDoesNotUseLevelThreshold() {
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.TRADE, oldExtraInfo, estimatedEvoLvl);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.STONE, newExtraInfo, true);
        System.out.println("Evo after (estimatedEvoLvl must not be used for extraInfo): " + evo);
        assertEquals(EvolutionType.STONE, evo.getType());
        assertEquals(newExtraInfo, evo.getExtraInfo());
        assertEquals(estimatedEvoLvl, evo.getEstimatedEvoLvl());
    }

    @Test
    public void useEstimatedLevelHasEffectOnUpdateEvolutionMethodIfNewEvolutionUsesLevelThreshold() {
        Evolution evo = new Evolution(new Species(0), new Species(1), EvolutionType.TRADE, oldExtraInfo, estimatedEvoLvl);
        System.out.println("Evo before: " + evo);
        evo.updateEvolutionMethod(EvolutionType.LEVEL, newExtraInfo, true);
        System.out.println("Evo after (estimatedEvoLvl must be used for extraInfo): " + evo);
        assertEquals(EvolutionType.LEVEL, evo.getType());
        assertEquals(estimatedEvoLvl, evo.getExtraInfo());
        assertEquals(estimatedEvoLvl, evo.getEstimatedEvoLvl());
    }
}
