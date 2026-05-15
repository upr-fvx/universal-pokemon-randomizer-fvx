package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.constants.ItemIDs;
import com.uprfvx.romio.constants.SpeciesIDs;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvolutionMethodDecisionTest {

    @Test
    public void changeImpossibleEvolutionsMapsFrlgHappinessAndBeauty() {
        Evolution happinessDay = evolution(SpeciesIDs.eevee, EvolutionType.HAPPINESS_DAY, 220, 30);
        Evolution happinessNight = evolution(SpeciesIDs.eevee, EvolutionType.HAPPINESS_NIGHT, 220, 30);
        Evolution highBeauty = evolution(SpeciesIDs.feebas, EvolutionType.HIGH_BEAUTY, 170, 33);

        assertTrue(Gen3RomHandler.updateImpossibleEvolution(happinessDay, true, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(happinessNight, true, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(highBeauty, true, false));

        assertMethod(happinessDay, EvolutionType.STONE, ItemIDs.sunStone);
        assertMethod(happinessNight, EvolutionType.STONE, ItemIDs.moonStone);
        assertMethod(highBeauty, EvolutionType.LEVEL, 35);
    }

    @Test
    public void changeImpossibleEvolutionsIgnoresFrlgOnlyCasesOutsideFrlg() {
        Evolution happinessDay = evolution(SpeciesIDs.eevee, EvolutionType.HAPPINESS_DAY, 220, 30);

        assertFalse(Gen3RomHandler.updateImpossibleEvolution(happinessDay, false, false));

        assertMethod(happinessDay, EvolutionType.HAPPINESS_DAY, 220);
    }

    @Test
    public void changeImpossibleEvolutionsMapsTradeAndTradeItemBranches() {
        Evolution trade = evolution(SpeciesIDs.haunter, EvolutionType.TRADE, 0, 36);
        Evolution poliwhirl = evolution(SpeciesIDs.poliwhirl, EvolutionType.TRADE_ITEM, ItemIDs.kingsRock, 38);
        Evolution slowpoke = evolution(SpeciesIDs.slowpoke, EvolutionType.TRADE_ITEM, ItemIDs.kingsRock, 40);
        Evolution seadra = evolution(SpeciesIDs.seadra, EvolutionType.TRADE_ITEM, ItemIDs.dragonScale, 41);
        Evolution clamperlToHuntail = evolution(SpeciesIDs.clamperl, EvolutionType.TRADE_ITEM, ItemIDs.deepSeaTooth, 31);
        Evolution clamperlToGorebyss = evolution(SpeciesIDs.clamperl, EvolutionType.TRADE_ITEM, ItemIDs.deepSeaScale, 32);
        Evolution genericTradeItem = evolution(SpeciesIDs.onix, EvolutionType.TRADE_ITEM, ItemIDs.metalCoat, 35);

        assertTrue(Gen3RomHandler.updateImpossibleEvolution(trade, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(poliwhirl, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(slowpoke, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(seadra, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(clamperlToHuntail, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(clamperlToGorebyss, false, false));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(genericTradeItem, false, false));

        assertMethod(trade, EvolutionType.LEVEL, 37);
        assertMethod(poliwhirl, EvolutionType.LEVEL, 37);
        assertMethod(slowpoke, EvolutionType.STONE, ItemIDs.waterStone);
        assertMethod(seadra, EvolutionType.LEVEL, 40);
        assertMethod(clamperlToHuntail, EvolutionType.LEVEL, 30);
        assertMethod(clamperlToGorebyss, EvolutionType.STONE, ItemIDs.waterStone);
        assertMethod(genericTradeItem, EvolutionType.LEVEL, 30);
    }

    @Test
    public void changeImpossibleEvolutionsCanUseEstimatedLevelsForLevelTargets() {
        Evolution trade = evolution(SpeciesIDs.haunter, EvolutionType.TRADE, 0, 44);
        Evolution highBeauty = evolution(SpeciesIDs.feebas, EvolutionType.HIGH_BEAUTY, 170, 39);
        Evolution clamperl = evolution(SpeciesIDs.clamperl, EvolutionType.TRADE_ITEM, ItemIDs.deepSeaTooth, 42);

        assertTrue(Gen3RomHandler.updateImpossibleEvolution(trade, false, true));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(highBeauty, true, true));
        assertTrue(Gen3RomHandler.updateImpossibleEvolution(clamperl, false, true));

        assertMethod(trade, EvolutionType.LEVEL, 44);
        assertMethod(highBeauty, EvolutionType.LEVEL, 39);
        assertMethod(clamperl, EvolutionType.LEVEL, 42);
    }

    @Test
    public void removeTimeBasedEvolutionsMapsPairedDayNightToSunMoonStones() {
        Evolution day = evolution(SpeciesIDs.eevee, EvolutionType.HAPPINESS_DAY, 220, 30);
        Evolution night = evolution(SpeciesIDs.eevee, EvolutionType.HAPPINESS_NIGHT, 220, 30);

        assertTrue(AbstractRomHandler.updateTimeBasedEvolution(day, true));
        assertTrue(AbstractRomHandler.updateTimeBasedEvolution(night, true));

        assertMethod(day, EvolutionType.STONE, ItemIDs.sunStone);
        assertMethod(night, EvolutionType.STONE, ItemIDs.moonStone);
    }

    @Test
    public void removeTimeBasedEvolutionsMapsLevelDuskToDuskStone() {
        Evolution levelDusk = evolution(SpeciesIDs.rockruff, EvolutionType.LEVEL_DUSK, 25, 25);

        assertTrue(AbstractRomHandler.updateTimeBasedEvolution(levelDusk, false));

        assertMethod(levelDusk, EvolutionType.STONE, ItemIDs.duskStone);
    }

    @Test
    public void removeTimeBasedEvolutionsPreservesExtraInfoForUnpairedTimelessMapping() {
        Evolution levelDay = evolution(SpeciesIDs.rockruff, EvolutionType.LEVEL_DAY, 25, 25);
        Evolution itemNight = evolution(SpeciesIDs.gligar, EvolutionType.ITEM_NIGHT, ItemIDs.razorFang, 30);

        assertTrue(AbstractRomHandler.updateTimeBasedEvolution(levelDay, false));
        assertTrue(AbstractRomHandler.updateTimeBasedEvolution(itemNight, false));

        assertMethod(levelDay, EvolutionType.LEVEL, 25);
        assertMethod(itemNight, EvolutionType.ITEM, ItemIDs.razorFang);
    }

    @Test
    public void removeTimeBasedEvolutionsIgnoresNonTimeBasedMethods() {
        Evolution level = evolution(SpeciesIDs.bulbasaur, EvolutionType.LEVEL, 16, 16);

        assertFalse(AbstractRomHandler.updateTimeBasedEvolution(level, false));

        assertMethod(level, EvolutionType.LEVEL, 16);
    }

    private static Evolution evolution(int fromNumber, EvolutionType type, int extraInfo, int estimatedLevel) {
        Species from = new Species(fromNumber);
        Species to = new Species(10000 + fromNumber);
        return new Evolution(from, to, type, extraInfo, estimatedLevel);
    }

    private static void assertMethod(Evolution evolution, EvolutionType type, int extraInfo) {
        assertEquals(type, evolution.getType());
        assertEquals(extraInfo, evolution.getExtraInfo());
    }
}
