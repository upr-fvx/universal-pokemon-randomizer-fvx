package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.romhandlers.romentries.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Has methods that return ROM names, to easily test sets of ROMs.
 */
public class Roms {

    public enum Region {
        USA('U'), EUROPE_ENGLISH('E'), JAPAN('J'), FRANCE('F'), GERMANY('G'), SPAIN('S'),
        ITALY('I'), KOREA('K');

        private final char id;

        Region(char id) {
            this.id = id;
        }
    }

    // These are ROMs which are allegedly (according to the .ini files) supported by the UPR.
    // However, I could not procure the actual ROMs... Rather than running the romIsTestable() testcase each
    // time to find this out there is this manual list to filter them out.
    // -- voliol 2023-02-26
    //
    // There are also the Korean Gold/Silver, which have (very) incomplete ROM entries. They can be set as "testable"
    // as soon as some basic offsets are found/added. Otherwise they just flood the test cases with errors because they
    // crash trying to load.
    // -- voliol 2024-01-21
    private static final List<String> UNTESTABLE = Arrays.asList("Green (J)(T-Eng)", "Crystal SpeedChoice v3",
            "Ruby (U/E) 1.2", "Sapphire (U/E) 1.2", "Emerald (T-Eng)", "Gold (K)", "Silver (K)");

    private static final List<String> ALL_GEN_1_ROMS;
    private static final List<String> ALL_GEN_2_ROMS;
    private static final List<String> ALL_GEN_3_ROMS;
    private static final List<String> ALL_GEN_4_ROMS;
    private static final List<String> ALL_GEN_5_ROMS;
    private static final List<String> ALL_GEN_6_ROMS;
    private static final List<String> ALL_GEN_7_ROMS;

    static {
        try {
            ALL_GEN_1_ROMS = getNames(Gen1RomEntry.READER.readEntriesFromFile("gen1_offsets.ini"));
            ALL_GEN_2_ROMS = getNames(Gen2RomEntry.READER.readEntriesFromFile("gen2_offsets.ini"));
            ALL_GEN_3_ROMS = getNames(Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini"));
            ALL_GEN_4_ROMS = getNames(Gen4RomEntry.READER.readEntriesFromFile("gen4_offsets.ini"));
            ALL_GEN_5_ROMS = getNames(Gen5RomEntry.READER.readEntriesFromFile("gen5_offsets.ini"));
            ALL_GEN_6_ROMS = getNames(Gen6RomEntry.READER.readEntriesFromFile("gen6_offsets.ini"));
            ALL_GEN_7_ROMS = getNames(Gen7RomEntry.READER.readEntriesFromFile("gen7_offsets.ini"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<List<String>> ALL_ROMS_BY_GENERATION = Arrays.asList(ALL_GEN_1_ROMS, ALL_GEN_2_ROMS,
            ALL_GEN_3_ROMS, ALL_GEN_4_ROMS, ALL_GEN_5_ROMS, ALL_GEN_6_ROMS, ALL_GEN_7_ROMS);

    private static List<String> getNames(List<? extends RomEntry> romEntries) {
        return romEntries.stream().map(RomEntry::getName).collect(Collectors.toList());
    }

    public static String[] getAllRoms() {
        return getRoms(new int[]{1, 2, 3, 4, 5, 6, 7}, Region.values(), true);
    }

    public static String[] getRoms(int[] generations, Region[] regions, boolean includeUntestable) {
        List<String> roms = new ArrayList<>();
        for (int gen : generations) {
            List<String> ofGen = ALL_ROMS_BY_GENERATION.get(gen - 1);
            if (gen < 6) {
                ofGen.removeIf(s -> !isOfRegion(s, regions));
            }
            roms.addAll(ofGen);
        }
        if (!includeUntestable) {
            roms.removeAll(UNTESTABLE);
        }

        return roms.toArray(new String[0]);
    }

    public static boolean isOfRegion(String romName, Region... regions) {
        if (ALL_GEN_6_ROMS.contains(romName) || ALL_GEN_7_ROMS.contains(romName)) {
            return true;
        }
        for (Region region : regions) {
            if (romName.contains("(" + region.id)) {
                return true;
            }
        }
        return false;
    }

}
