package com.uprfvx.random.updaters;

import com.uprfvx.romio.gamedata.basestats.BaseStats;
import com.uprfvx.romio.gamedata.basestats.Gen1BaseStats;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class SpeciesBaseStatUpdaterTest extends UpdaterTest {

    private static class BaseStatRecord {
        String name;
        int hp, atk, def, spAtk, spDef, speed, special;

        BaseStatRecord(String name, BaseStats baseStats) {
            this.name = name;
            this.hp = baseStats.getHp();
            this.atk = baseStats.getAttack();
            this.def = baseStats.getDefense();
            this.speed = baseStats.getSpeed();

            if (baseStats instanceof Gen1BaseStats gen1BaseStats) {
                this.spAtk = -1;
                this.spDef = -1;
                this.special = gen1BaseStats.getSpecial();
            } else {
                this.spAtk = baseStats.getSpatk();
                this.spDef = baseStats.getSpdef();
                this.special = -1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BaseStatRecord that = (BaseStatRecord) o;
            return hp == that.hp && atk == that.atk && def == that.def && spAtk == that.spAtk && spDef == that.spDef && speed == that.speed && special == that.special && name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, hp, atk, def, spAtk, spDef, speed, special);
        }

        @Override
        public String toString() {
            return "BaseStatRecord{" +
                    "name='" + name + '\'' +
                    ", hp=" + hp +
                    ", atk=" + atk +
                    ", def=" + def +
                    ", spAtk=" + spAtk +
                    ", spDef=" + spDef +
                    ", speed=" + speed +
                    ", special=" + special +
                    '}';
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNamesAndUpdateToGens")
    public void updatingDoesNotThrowException(String romNameAndUpdateToGen) {
        String[] split = romNameAndUpdateToGen.split(UPDATE_SEPARATOR);
        String romName = split[0];
        int updateToGen = Integer.parseInt(split[1]);

        loadROM(romName);
        new SpeciesBaseStatUpdater(romHandler).updateSpeciesStats(updateToGen);
    }

    @ParameterizedTest
    @MethodSource("getRomNamesAndUpdateToGens")
    public void updatingChangesSomeBaseStats(String romNameAndUpdateToGen) {
        String[] split = romNameAndUpdateToGen.split(UPDATE_SEPARATOR);
        String romName = split[0];
        int updateToGen = Integer.parseInt(split[1]);

        assumeTrue(updateToGen >= 6);

        loadROM(romName);
        List<BaseStatRecord> before = toRecords(romHandler.getSpeciesInclFormes());
        new SpeciesBaseStatUpdater(romHandler).updateSpeciesStats(updateToGen);
        List<BaseStatRecord> after = toRecords(romHandler.getSpeciesInclFormes());
        printDiff(before, after);
        assertNotEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void updatingToAllGensInOrderIsTheSameAsJustUpdatingToLast(String romName) {
        loadROM(romName);

        SpeciesBaseStatUpdater bsu = new SpeciesBaseStatUpdater(romHandler);
        for (int gen = getGenerationNumberOf(romName) + 1; gen <= MAX_UPDATE_GEN; gen++) {
            bsu.updateSpeciesStats(gen);
        }
        List<BaseStatRecord> afterAllInOrder = toRecords(romHandler.getSpeciesInclFormes());
        new SpeciesBaseStatUpdater(romHandler).updateSpeciesStats(MAX_UPDATE_GEN);
        assertEquals(afterAllInOrder, toRecords(romHandler.getSpeciesInclFormes()));
    }

    private List<BaseStatRecord> toRecords(List<Species> pokes) {
        List<BaseStatRecord> records = new ArrayList<>(pokes.size());
        for (Species pk : pokes) {
            if (pk != null) {
                records.add(new BaseStatRecord(pk.getFullName(), pk.getBaseStats()));
            }
        }
        return records;
    }


}
