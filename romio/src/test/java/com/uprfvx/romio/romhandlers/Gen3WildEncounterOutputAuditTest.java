package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Encounter;
import com.uprfvx.romio.gamedata.EncounterArea;
import com.uprfvx.romio.gamedata.EncounterType;
import com.uprfvx.romio.gamedata.Species;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Gen3WildEncounterOutputAuditTest {

    @Test
    public void wildEncounterOutputAuditSummarizesChangedAndUnchangedSlots() {
        Species rattata = species(19, "Rattata");
        Species pidgey = species(16, "Pidgey");
        Species sentret = species(161, "Sentret");

        EncounterArea baseRouteOne = area("Route 1 Grass/Cave", 1, "Route 1", EncounterType.WALKING, 25,
                encounter(rattata, 2, 4), encounter(pidgey, 3, 5));
        EncounterArea outputRouteOne = area("Route 1 Grass/Cave", 1, "Route 1", EncounterType.WALKING, 25,
                encounter(rattata, 2, 4), encounter(sentret, 3, 5));

        Gen3RomHandler.FrlgWildEncounterOutputAuditReport report =
                Gen3RomHandler.buildFrlgWildEncounterOutputAudit(List.of(baseRouteOne), List.of(outputRouteOne));

        assertEquals(2, report.summary().totalEncounterSlots());
        assertEquals(1, report.summary().changedSlots());
        assertEquals(1, report.summary().unchangedSlots());
        assertEquals(50.0, report.summary().changedPercentage(), 0.001);

        Gen3RomHandler.FrlgWildEncounterOutputAuditRow unchanged = report.rows().get(0);
        assertEquals("Route 1 Grass/Cave", unchanged.areaName());
        assertEquals("Route 1", unchanged.locationTag());
        assertEquals(EncounterType.WALKING, unchanged.encounterType());
        assertEquals(0, unchanged.slotIndex());
        assertEquals(19, unchanged.baseSpeciesId());
        assertEquals(19, unchanged.outputSpeciesId());
        assertFalse(unchanged.changedFromBase());

        Gen3RomHandler.FrlgWildEncounterOutputAuditRow changed = report.rows().get(1);
        assertEquals(1, changed.slotIndex());
        assertEquals(16, changed.baseSpeciesId());
        assertEquals("Pidgey", changed.baseSpeciesName());
        assertEquals(161, changed.outputSpeciesId());
        assertEquals("Sentret", changed.outputSpeciesName());
        assertTrue(changed.changedFromBase());
    }

    @Test
    public void wildEncounterOutputAuditTreatsMissingOutputSlotsAsChanged() {
        EncounterArea baseRouteTwo = area("Route 2 Grass/Cave", 2, "Route 2", EncounterType.WALKING, 30,
                encounter(species(10, "Caterpie"), 3));

        Gen3RomHandler.FrlgWildEncounterOutputAuditReport report =
                Gen3RomHandler.buildFrlgWildEncounterOutputAudit(List.of(baseRouteTwo), List.of());

        assertEquals(1, report.summary().totalEncounterSlots());
        assertEquals(1, report.summary().changedSlots());
        Gen3RomHandler.FrlgWildEncounterOutputAuditRow row = report.rows().get(0);
        assertEquals(10, row.baseSpeciesId());
        assertEquals(-1, row.outputSpeciesId());
        assertEquals("<missing>", row.outputSpeciesName());
        assertTrue(row.changedFromBase());
    }

    private static EncounterArea area(String name, int mapIndex, String locationTag, EncounterType encounterType,
                                      int rate, Encounter... encounters) {
        EncounterArea area = new EncounterArea(List.of(encounters));
        area.setIdentifiers(name, mapIndex, encounterType, locationTag);
        area.setRate(rate);
        return area;
    }

    private static Encounter encounter(Species species, int level) {
        return encounter(species, level, 0);
    }

    private static Encounter encounter(Species species, int level, int maxLevel) {
        Encounter encounter = new Encounter();
        encounter.setSpecies(species);
        encounter.setLevel(level);
        encounter.setMaxLevel(maxLevel);
        return encounter;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }
}
