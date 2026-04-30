package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class RomHandlerSpeciesStatsTest extends RomHandlerTest {

    private static class BaseStatRecord {
        private final int hp, attack, defense, spatk, spdef, speed, special;

        public BaseStatRecord(Species pk) {
            this.hp = pk.getHp();
            this.attack = pk.getAttack();
            this.defense = pk.getDefense();
            this.spatk = pk.getSpatk();
            this.spdef = pk.getSpdef();
            this.speed = pk.getSpeed();
            this.special = pk.getSpecial();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BaseStatRecord that)) return false;
            return hp == that.hp && attack == that.attack && defense == that.defense && spatk == that.spatk
                    && spdef == that.spdef && speed == that.speed && special == that.special;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hp, attack, defense, spatk, spdef, speed, special);
        }

        @Override
        public String toString() {
            return String.format("{%d/%d/%d/%d/%d/%d//%d}", hp, attack, defense, spatk, spdef, speed, special);
        }
    }

    private static class HeldItemsRecord {
        private final Item guaranteed, common, rare, darkGrass;

        public HeldItemsRecord(Species pk) {
            this.guaranteed = pk.getGuaranteedHeldItem();
            this.common = pk.getCommonHeldItem();
            this.rare = pk.getRareHeldItem();
            this.darkGrass = pk.getDarkGrassHeldItem();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof HeldItemsRecord that)) return false;
            return Objects.equals(guaranteed, that.guaranteed) && Objects.equals(common, that.common)
                    && Objects.equals(rare, that.rare) && Objects.equals(darkGrass, that.darkGrass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(guaranteed, common, rare, darkGrass);
        }

        @Override
        public String toString() {
            return String.format("{%s/%s/%s/%s}", guaranteed, common, rare, darkGrass);
        }
    }

    private record BreedingInfoRecord(EggGroup primaryEggGroup, EggGroup secondaryEggGroup, int eggCycles) {
        public BreedingInfoRecord(Species pk) {
            BreedingInfo bi = pk.getBreedingInfo();
            this(
                    bi == null ? null : bi.getPrimaryEggGroup(),
                    bi == null ? null : bi.getSecondaryEggGroup(),
                    bi == null ? -1 : bi.getEggCycles()
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void baseFormeOfAlolanFormesHasCorrectAlolanForme(String romName) {
        loadROM(romName);
        SpeciesSet speciesSet = romHandler.getSpeciesSetInclFormes();
        for (Species pk : speciesSet) {
            if (pk.getFormeSuffix().equals("-Alolan")) {
                // Alolan formes must have a base forme that must have the alolan forme as alolanForme
                assertEquals(pk, pk.getBaseForme().getAlolanForme());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allAlolanFormesHaveAlolanSuffix(String romName) {
        loadROM(romName);
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.isAlolan()) {
                System.out.println(pk);
                assertEquals("-Alolan", pk.getFormeSuffix());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allSpeciesWithAlolanSuffixAreAlolanFormes(String romName) {
        loadROM(romName);
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.getFormeSuffix().equals("-Alolan")) {
                System.out.println(pk);
                assertTrue(pk.isAlolan());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void baseStatsDoNotChangeWithLoadAndSave(String romName) {
        loadROM(romName);
        // it always loads the base stats once

        Map<Species, BaseStatRecord> records = new HashMap<>();
        romHandler.getSpeciesSetInclFormes()
                .forEach(pk -> records.put(pk, new BaseStatRecord(pk)));

        romHandler.saveSpeciesStats();
        romHandler.loadSpeciesStats();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(records.get(pk), new BaseStatRecord(pk));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void heldItemsDoNotChangeWithLoadAndSave(String romName) {
        loadROM(romName);
        // it always loads the base stats once

        Map<Species, HeldItemsRecord> records = new HashMap<>();
        romHandler.getSpeciesSetInclFormes()
                .forEach(pk -> records.put(pk, new HeldItemsRecord(pk)));

        romHandler.saveSpeciesStats();
        romHandler.loadSpeciesStats();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(records.get(pk), new HeldItemsRecord(pk));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void breedingInfoDoesNotChangeWithLoadAndSave(String romName) {
        loadROM(romName);
        // it always loads the base stats once

        Map<Species, BreedingInfoRecord> records = new HashMap<>();
        romHandler.getSpeciesSetInclFormes()
                .forEach(pk -> records.put(pk, new BreedingInfoRecord(pk)));

        romHandler.saveSpeciesStats();
        romHandler.loadSpeciesStats();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(records.get(pk), new BreedingInfoRecord(pk));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void printFormes(String romName) {
        loadROM(romName);

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            if (pk.getAltFormes().isEmpty()) continue;
            System.out.println(pk.getNumberAndFullName());
            for (Species altForme : pk.getAltFormes()) {
                System.out.println(altForme.getFormeNumber() + ":\t" + altForme.getNumberAndFullName());
            }
        }
    }

}
