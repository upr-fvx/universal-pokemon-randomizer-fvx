package test.com.dabomstew.pkrandom.updaters;

import test.com.dabomstew.pkromio.romhandlers.RomHandlerTest;

import java.util.ArrayList;
import java.util.List;

/**
 * A base class for tests on {@link com.dabomstew.pkrandom.updaters.Updater}s.
 * Extends RomHandlerTest, so it can go through multiple Roms, not because the Updater class is a RomHandler (it's not).
 */
public class UpdaterTest extends RomHandlerTest {

    protected static final int MAX_UPDATE_GEN = 9;
    protected static final String UPDATE_SEPARATOR = " ->";

    protected static String[] getRomNamesAndUpdateToGens() {
        String[] romNames = getRomNames();
        List<String> andUpdateToGens = new ArrayList<>();
        for (String romName : romNames) {
            for (int gen = getGenerationNumberOf(romName) + 1; gen <= MAX_UPDATE_GEN; gen++) {
                andUpdateToGens.add(romName + UPDATE_SEPARATOR + gen);
            }
        }
        return andUpdateToGens.toArray(new String[0]);
    }

    protected void printDiff(List<?> a, List<?> b) {
        boolean equals = true;
        if (a.size() != b.size()) throw new IllegalArgumentException("Input lists have non-equal sizes");
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                equals = false;
                System.out.println("Diff:\t" + a.get(i));
                System.out.println("\t\t" + b.get(i));
            }
        }

        if (equals) {
            System.out.println("No differences");
        }

    }

}
