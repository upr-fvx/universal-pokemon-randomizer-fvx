package test.com.dabomstew.pkrandom.updaters;

import com.dabomstew.pkrandom.updaters.MoveUpdater;
import com.dabomstew.pkromio.gamedata.Move;
import com.dabomstew.pkromio.gamedata.MoveCategory;
import com.dabomstew.pkromio.gamedata.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MoveUpdaterTest extends UpdaterTest {

    // all the attributes that may be changed by MoveUpdater.
    // this must of course be updated if it gets the ability to change more attributes
    private static class MoveRecord{
        String name;
        int power;
        int pp;
        double hitratio;
        Type type;
        MoveCategory category;

        MoveRecord(String name, int power, int pp, double hitratio, Type type, MoveCategory category) {
            this.name = name;
            this.power = power;
            this.pp = pp;
            this.hitratio = hitratio;
            this.type = type;
            this.category = category;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MoveRecord that = (MoveRecord) o;
            return power == that.power && pp == that.pp && Double.compare(that.hitratio, hitratio) == 0 && name.equals(that.name) && type == that.type && category == that.category;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, power, pp, hitratio, type, category);
        }

        @Override
        public String toString() {
            return "MoveRecord{" +
                    "name='" + name + '\'' +
                    ", power=" + power +
                    ", pp=" + pp +
                    ", hitratio=" + hitratio +
                    ", type=" + type +
                    ", category=" + category +
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
        new MoveUpdater(romHandler).updateMoves(updateToGen);
    }

    @ParameterizedTest
    @MethodSource("getRomNamesAndUpdateToGens")
    public void updatingChangesSomeMoves(String romNameAndUpdateToGen) {
        String[] split = romNameAndUpdateToGen.split(UPDATE_SEPARATOR);
        String romName = split[0];
        int updateToGen = Integer.parseInt(split[1]);

        loadROM(romName);
        List<MoveRecord> before = toRecords(romHandler.getMoves());
        new MoveUpdater(romHandler).updateMoves(updateToGen);
        List<MoveRecord> after = toRecords(romHandler.getMoves());
        printDiff(before, after);
        assertNotEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void updatingToAllGensInOrderIsTheSameAsJustUpdatingToLast(String romName) {
        loadROM(romName);

        MoveUpdater mu = new MoveUpdater(romHandler);
        for (int gen = getGenerationNumberOf(romName) + 1; gen <= MAX_UPDATE_GEN; gen++) {
            mu.updateMoves(gen);
        }
        List<MoveRecord> afterAllInOrder = toRecords(romHandler.getMoves());
        mu.updateMoves(MAX_UPDATE_GEN);
        assertEquals(afterAllInOrder, toRecords(romHandler.getMoves()));
    }

    private List<MoveRecord> toRecords(List<Move> moves) {
        List<MoveRecord> records = new ArrayList<>(moves.size());
        for (Move m : moves) {
            if (m != null) {
                records.add(new MoveRecord(m.name, m.power, m.pp, m.hitratio, m.type, m.category));
            }
        }
        return records;
    }

}
