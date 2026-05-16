package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Encounter;
import com.uprfvx.romio.gamedata.EncounterArea;
import com.uprfvx.romio.gamedata.EncounterType;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3WildEncounterRomSmokeTest {

    private static final String ROM_PATH_PROPERTY = "uprfvx.gen3WildSmokeRom";
    private static final String ROM_PATH_ENV = "UPRFVX_GEN3_WILD_SMOKE_ROM";

    @Test
    public void wildEncounterWriterReloadSmokeOptIn() {
        String romPath = configuredRomPath();
        assumeTrue(romPath != null && !romPath.isBlank(),
                "Set -D" + ROM_PATH_PROPERTY + "=<private-rom> to run the Gen3 wild encounter ROM smoke.");

        RomHandler romHandler = loadGen3Rom(romPath);
        List<EncounterArea> before = romHandler.getEncounters(false);
        assumeTrue(!before.isEmpty(), "Loaded ROM has no wild encounter areas.");

        List<EncounterArea> edited = deepCopyAreas(before);
        SlotEdit edit = chooseSmallestSafeEdit(romHandler, edited);
        assumeTrue(edit != null, "Loaded ROM has no safe species change for the wild encounter smoke.");

        edited.get(edit.areaIndex()).get(edit.slotIndex()).setSpecies(edit.replacement());
        List<EncounterSlot> expected = encounterSlots(edited);

        romHandler.setEncounters(false, edited);
        List<EncounterArea> reloaded = romHandler.getEncounters(false);

        assertEquals(slotShapes(before), slotShapes(reloaded));
        assertEquals(expected, encounterSlots(reloaded));
        assertNotEquals(edit.original().getNumber(), edit.replacement().getNumber());
        if (edit.replacement().getNumber() > 1000) {
            assertTrue(reloaded.get(edit.areaIndex()).get(edit.slotIndex()).getSpecies().getNumber() > 1000);
        }
    }

    private static String configuredRomPath() {
        String property = System.getProperty(ROM_PATH_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(ROM_PATH_ENV);
    }

    private static RomHandler loadGen3Rom(String romPath) {
        RomHandler.Factory factory = new Gen3RomHandler.Factory();
        assertTrue(factory.isLoadable(romPath), "Configured ROM is not loadable as a Gen3 ROM.");
        RomHandler romHandler = factory.create();
        assertTrue(romHandler.loadRom(romPath), "Configured Gen3 ROM could not be loaded.");
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());
        return romHandler;
    }

    private static SlotEdit chooseSmallestSafeEdit(RomHandler romHandler, List<EncounterArea> areas) {
        Species highSpecies = replacementSpecies(romHandler, 1001);
        for (int areaIndex = 0; areaIndex < areas.size(); areaIndex++) {
            EncounterArea area = areas.get(areaIndex);
            for (int slotIndex = 0; slotIndex < area.size(); slotIndex++) {
                Species original = area.get(slotIndex).getSpecies();
                if (original == null) {
                    continue;
                }
                Species replacement = highSpecies != null && highSpecies.getNumber() != original.getNumber()
                        ? highSpecies : replacementSpeciesDifferentFrom(romHandler, original);
                if (replacement != null) {
                    return new SlotEdit(areaIndex, slotIndex, original, replacement);
                }
            }
        }
        return null;
    }

    private static Species replacementSpecies(RomHandler romHandler, int minimumNumber) {
        return romHandler.getSpeciesInclFormes().stream()
                .filter(Objects::nonNull)
                .filter(species -> species.getNumber() >= minimumNumber)
                .findFirst()
                .orElse(null);
    }

    private static Species replacementSpeciesDifferentFrom(RomHandler romHandler, Species original) {
        return romHandler.getSpeciesInclFormes().stream()
                .filter(Objects::nonNull)
                .filter(species -> species.getNumber() != original.getNumber())
                .findFirst()
                .orElse(null);
    }

    private static List<EncounterArea> deepCopyAreas(List<EncounterArea> areas) {
        List<EncounterArea> copies = new ArrayList<>();
        for (EncounterArea area : areas) {
            EncounterArea copy = new EncounterArea();
            copy.setIdentifiers(area.getDisplayName(), area.getMapIndex(), area.getEncounterType(),
                    area.getLocationTag());
            copy.setRate(area.getRate());
            copy.setPostGame(area.isPostGame());
            if (area.isPartiallyPostGame()) {
                copy.setPartiallyPostGameCutoff(area.getPartiallyPostGameCutoff());
            }
            copy.setForceMultipleSpecies(area.isForceMultipleSpecies());
            copy.banAllSpecies(area.getBannedSpecies());
            for (Encounter encounter : area) {
                copy.add(copyEncounter(encounter));
            }
            copies.add(copy);
        }
        return copies;
    }

    private static Encounter copyEncounter(Encounter encounter) {
        Encounter copy = new Encounter();
        copy.setSpecies(encounter.getSpecies());
        copy.setLevel(encounter.getLevel());
        copy.setMaxLevel(encounter.getMaxLevel());
        copy.setFormeNumber(encounter.getFormeNumber());
        copy.setSOS(encounter.isSOS());
        copy.setSosType(encounter.getSosType());
        return copy;
    }

    private static List<SlotShape> slotShapes(List<EncounterArea> areas) {
        List<SlotShape> shapes = new ArrayList<>();
        for (EncounterArea area : areas) {
            for (Encounter encounter : area) {
                shapes.add(new SlotShape(area.getDisplayName(), area.getMapIndex(), area.getEncounterType(),
                        area.getLocationTag(), area.getRate(), encounter.getLevel(), encounter.getMaxLevel()));
            }
        }
        return shapes;
    }

    private static List<EncounterSlot> encounterSlots(List<EncounterArea> areas) {
        List<EncounterSlot> slots = new ArrayList<>();
        for (EncounterArea area : areas) {
            for (Encounter encounter : area) {
                slots.add(new EncounterSlot(area.getDisplayName(), area.getMapIndex(), area.getEncounterType(),
                        area.getLocationTag(), area.getRate(), encounter.getLevel(), encounter.getMaxLevel(),
                        encounter.getSpecies().getNumber()));
            }
        }
        return slots;
    }

    private record SlotShape(String areaName, int mapIndex, EncounterType encounterType, String locationTag,
                             int encounterRate, int level, int maxLevel) {
    }

    private record EncounterSlot(String areaName, int mapIndex, EncounterType encounterType, String locationTag,
                                 int encounterRate, int level, int maxLevel, int speciesNumber) {
    }

    private record SlotEdit(int areaIndex, int slotIndex, Species original, Species replacement) {
    }
}
