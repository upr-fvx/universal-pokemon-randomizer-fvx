package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.romhandlers.AbstractRomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            if (!(o instanceof BaseStatRecord)) return false;
            BaseStatRecord that = (BaseStatRecord) o;
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

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void baseStatsDoNotChangeWithLoadAndSave(String romName) {
        loadROM(romName);
        // it always loads the base stats once

        Map<Species, BaseStatRecord> records = new HashMap<>();
        romHandler.getSpeciesSetInclFormes()
                .forEach(pk -> records.put(pk, new BaseStatRecord(pk)));

        ((AbstractRomHandler) romHandler).savePokemonStats();
        ((AbstractRomHandler) romHandler).loadPokemonStats();

        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            System.out.println(pk.getFullName());
            assertEquals(records.get(pk), new BaseStatRecord(pk));
        }
    }

}
