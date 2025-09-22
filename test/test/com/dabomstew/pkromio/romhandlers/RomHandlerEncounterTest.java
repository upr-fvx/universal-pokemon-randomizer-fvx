package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.constants.*;
import com.dabomstew.pkromio.gamedata.EncounterArea;
import com.dabomstew.pkromio.romhandlers.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerEncounterTest extends RomHandlerTest {

    private static final String EARLIER_OUTPUTS_PATH = "test/resources/encounters/";

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersAreNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getEncounters(false).isEmpty());
        assertFalse(romHandler.getEncounters(true).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersDoNotChangeWithGetAndSetNotUsingTimeOfDay(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(false);
        System.out.println(encounterAreas);
        List<EncounterArea> before = new ArrayList<>(encounterAreas);
        romHandler.setEncounters(false, encounterAreas);
        assertEquals(before, romHandler.getEncounters(false));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersDoNotChangeWithGetAndSetUsingTimeOfDay(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);
        System.out.println(encounterAreas);
        List<EncounterArea> before = new ArrayList<>(encounterAreas);
        romHandler.setEncounters(true, encounterAreas);
        assertEquals(before, romHandler.getEncounters(true));
    }

    /**
     * This test checks whether you've accidentally broken the reading of encounters
     * by comparing the current output with logged output in text files. <br>
     * If you <b>intentionally</b> change something
     * about how the encounters are read, like the EncounterArea names, you can expect this test to fail.
     * In that case, check it only differs in the way you want, and then update the text files
     * (using {@link #overwriteEarlierRandomizerCodeOutput(String)}).
     */
    @ParameterizedTest
    @MethodSource("getRomNames")
    public void encountersAreIdenticalToEarlierRandomizerCodeOutput(String romName) throws IOException {
        loadROM(romName);

        StringWriter sw = new StringWriter();
        printAllEncounters(sw);

        String orig = readFile(EARLIER_OUTPUTS_PATH + romHandler.getROMName() + ".txt");
        assertEquals(orig.replaceAll("\r\n", "\n"),
                sw.toString().replaceAll("\r\n", "\n"));
    }

    /**
     * For when changes have <b>intentionally</b> been made to how the encounters are read, and detected by
     * {@link #encountersAreIdenticalToEarlierRandomizerCodeOutput(String)}.<br>
     * Overwrites the earlier output with the current one.
     */
    @Disabled
    @ParameterizedTest()
    @MethodSource("getRomNames")
    public void overwriteEarlierRandomizerCodeOutput(String romName) throws IOException {
        loadROM(romName);

        StringWriter sw = new StringWriter();
        printAllEncounters(sw);

        String path = EARLIER_OUTPUTS_PATH + romHandler.getROMName() + ".txt";
        writeFile(path, sw.toString().replaceAll("\r\n", "\n"));
    }

    private void printAllEncounters(StringWriter sw) {
        PrintWriter pw = new PrintWriter(sw);

        List<EncounterArea> noTimeOfDay = romHandler.getEncounters(false);
        List<EncounterArea> useTimeOfDay = romHandler.getEncounters(true);

        pw.println("useTimeOfDay=false");
        pw.println(encounterAreasToMultilineString(noTimeOfDay));
        pw.println("");
        pw.println("useTimeOfDay=true");
        pw.println(encounterAreasToMultilineString(useTimeOfDay));
        pw.close();
    }

    private String encounterAreasToMultilineString(List<EncounterArea> encounterAreas) {
        StringBuilder sb = new StringBuilder();
        sb.append("[EncounterAreas:");
        for (EncounterArea area : encounterAreas) {
            sb.append(String.format("\n\t[Name = %s, Rate = %d, MapIndex = %d,",
                    area.getDisplayName(), area.getRate(), area.getMapIndex()));
            sb.append(String.format("\n\t\tEncounters = %s", new ArrayList<>(area)));
            if (!area.getBannedSpecies().isEmpty()) {
                sb.append(String.format("\n\t\tBanned = %s", area.getBannedSpecies()));
            }
            sb.append("]");
        }
        sb.append("\n]");
        return sb.toString();
    }

    private static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static void writeFile(String path, String toWrite) throws IOException {
        Files.write(Paths.get(path), toWrite.getBytes(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allEncounterAreasHaveALocationTagNoTimeOfDay(String romName) {
        loadROM(romName);
        for (EncounterArea area : romHandler.getEncounters(false)) {
            assertNotNull(area.getLocationTag());
            assertNotEquals("", area.getLocationTag());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allEncounterAreasHaveALocationTagUseTimeOfDay(String romName) {
        loadROM(romName);
        for (EncounterArea area : romHandler.getEncounters(true)) {
            assertNotNull(area.getLocationTag());
            assertNotEquals("", area.getLocationTag());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allLocationTagsAreFoundInTraverseOrder(String romName) {
        loadROM(romName);
        Set<String> inOrder = new HashSet<>(getLocationTagsTraverseOrder());
        Set<String> used = new HashSet<>();
        for (EncounterArea area : romHandler.getEncounters(false)) {
            used.add(area.getLocationTag());
        }
        for (EncounterArea area : romHandler.getEncounters(true)) {
            used.add(area.getLocationTag());
        }
        System.out.println("In traverse order:\n" + inOrder);
        System.out.println("Used:\n" + used);
        Set<String> notUsed = new HashSet<>(used);
        notUsed.removeAll(inOrder);
        System.out.println("Used but not in traverse order:\n" + notUsed);
        assertTrue(notUsed.isEmpty());
    }

    private List<String> getLocationTagsTraverseOrder() {
        if (romHandler instanceof Gen1RomHandler) {
            return Gen1Constants.locationTagsTraverseOrder;
        } else if (romHandler instanceof Gen2RomHandler) {
            return Gen2Constants.locationTagsTraverseOrder;
        } else if (romHandler instanceof Gen3RomHandler) {
            return Gen3Constants.getLocationTagsTraverseOrder(romHandler.getROMType());
        } else if (romHandler instanceof Gen4RomHandler) {
            return Gen4Constants.getLocationTagsTraverseOrder(romHandler.getROMType());
        } else if (romHandler instanceof Gen5RomHandler) {
            return Gen5Constants.getLocationTagsTraverseOrder(romHandler.getROMType());
        } else if (romHandler instanceof Gen6RomHandler) {
            return Gen6Constants.getLocationTagsTraverseOrder(romHandler.getROMType());
        } else if (romHandler instanceof  Gen7RomHandler) {
            return Gen7Constants.getLocationTagsTraverseOrder(romHandler.getROMType());
        }
        return Collections.emptyList();
    }

    private static final List<String> encounterTerminators = Arrays.asList(
            "Grass/Cave", "Yellow Flowers", "Red Flowers", "Purple Flowers", "Rough Terrain/Tall Grass", "Long Grass",
            "Surf",
            "Common Horde", "Uncommon Horde", "Rare Horde", "DexNav Foreign Encounter",
            "Old Rod", "Good Rod", "Super Rod",
            "Rock Smash"
    );

    private static String simplifyTag(String tag) {
        for (String terminator : encounterTerminators) {
            if (tag.endsWith(terminator)) {
                tag = tag.replace(terminator, "");
                break;
            }
        }
        tag = tag.split(", Table")[0]; // for Gen 7
        tag = tag.replace("é", "e");
        tag = tag.trim().toUpperCase();

        tag = tag.replace('’', '\''); //apostrophes, not single quote

        tag = tag.replace("\\C ", "");


        return tag;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void locationTagsAndDisplayNamesMatchUp(String romName) {
        // TODO: look over this; currently most games fail this test.
        //  Should the test change, or the underlying systems?
        assumeTrue(Roms.isOfRegion(romName, Roms.Region.USA, Roms.Region.EUROPE_ENGLISH));
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        String lastTag = "NOT A LOCATION TAG";
        int count = 0;
        for (int i = 0; i < encounterAreas.size(); i++) {
            EncounterArea area = encounterAreas.get(i);
            String areaTag = area.getLocationTag();
            if (!areaTag.equals(lastTag)) {
                System.out.println("\t Start location: " + areaTag + "; Index: " + i);
                lastTag = areaTag;
                count = 1;
            } else {
                count++;
            }
            if(area.getEncounterType() != null) {
                System.out.println("\t\t\t " + count + " " + area.getEncounterType().name()
                        + " " + area.getSpeciesInArea().toStringShort() + " " + area.getDisplayName());
            } else {
                System.out.println("\t\t\t " + count
                        + " " + area.getSpeciesInArea().toStringShort() + " " + area.getDisplayName());
            }

            String simplifiedDisplayName = simplifyTag(area.getDisplayName());
            checkLocationTagAgainstDisplayName(areaTag, simplifiedDisplayName);
        }

    }

    private void checkLocationTagAgainstDisplayName(String locationTag, String displayName) {

        //switch for special cases
        switch(locationTag) {
            case "UNUSED":
                //doesn't need to match anything
                break;
            case "HAU'OLI CITY":
                assertTrue(displayName.contains(locationTag) || displayName.contains("HAU'OLI OUTSKIRTS")
                        || displayName.contains("TRAINERS' SCHOOL"));
                break;
            case "ROUTE 2":
                assertTrue(displayName.contains(locationTag) || displayName.contains("BERRY FIELDS"));
                break;
            case "PANIOLA TOWN":
                assertTrue(displayName.contains("PANIOLA"));
                break;
            default:
                assertTrue(displayName.contains(locationTag));
                break;
        }

    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void hasMapIndicesIsCorrect(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        Map<Integer, List<EncounterArea>> areasByMapIndex = EncounterArea.groupAreasByMapIndex(encounterAreas);

        List<Integer> maps = new ArrayList<>(areasByMapIndex.keySet());
        maps.sort(null);

        if(maps.size() > 1) {
            for (int map : maps) {
                System.out.println("\t" + map + ":");
                for (EncounterArea area : areasByMapIndex.get(map)) {
                    System.out.println("\t\t" + area.getDisplayName());
                }
            }
        } else {
            System.out.println("No map indices.");
        }

        if(romHandler.hasMapIndices()) {
            assertNotEquals(areasByMapIndex.size(), 1, "No map indices when hasMapIndices is true!");
        } else {
            assertEquals(areasByMapIndex.size(), 1,
                    "Map indices are present, but hasMapIndices is false!");
            //Assumptions.abort("Does not have map indices.");
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void hasEncounterTypes(String romName) {
        loadROM(romName);
        List<EncounterArea> encounterAreas = romHandler.getEncounters(true);

        String lastLocation = "NOT A LOCATION";
        for(EncounterArea area : encounterAreas) {
            if(!area.getLocationTag().equals(lastLocation)) {
                System.out.println("\t" + area.getLocationTag() + ":");
                lastLocation = area.getLocationTag();
            }
            assertNotNull(area.getEncounterType());
            System.out.println("\t\t" + area.getEncounterType().name() + ": " + area.getDisplayName());
        }
    }
}
