package test.com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkrandom.updaters.TypeEffectivenessUpdater;
import com.dabomstew.pkromio.gamedata.Effectiveness;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gamedata.TypeTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TypeEffectivenessUpdaterTest extends UpdaterTest {

    // Unlike the other Updaters it's simple to just check for every single update, so we do that.
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void updatingTypeEffectivenessWorks(String romName) {
        assumeTrue(getGenerationNumberOf(romName) < 6);
        loadROM(romName);
        new TypeEffectivenessUpdater(romHandler).updateTypeEffectiveness();

        TypeTable typeTable = romHandler.getTypeTable();
        if (romHandler.generationOfPokemon() == 1) {
            assertEquals(Effectiveness.NEUTRAL, typeTable.getEffectiveness(Type.POISON, Type.BUG));
            assertEquals(Effectiveness.HALF, typeTable.getEffectiveness(Type.BUG, Type.POISON));
            assertEquals(Effectiveness.DOUBLE, typeTable.getEffectiveness(Type.GHOST, Type.PSYCHIC));
            assertEquals(Effectiveness.HALF, typeTable.getEffectiveness(Type.ICE, Type.FIRE));
        } else {
            assertEquals(Effectiveness.NEUTRAL, typeTable.getEffectiveness(Type.GHOST, Type.STEEL));
            assertEquals(Effectiveness.NEUTRAL, typeTable.getEffectiveness(Type.DARK, Type.STEEL));
        }
    }

}
