package test.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import test.romhandlers.Generation;
import test.romhandlers.Roms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO: once supported by JUnit, convert to ParameterizedContainer
//support is intended to be added sometime this year? that's all I got
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RandomizerTest {

    // update if the amount of supported generation increases,
    // and expect some test cases to need updating too, though hopefully only in a minor way
    protected static final int HIGHEST_GENERATION = 7;

    protected static final Random RND = new Random();

    private static final String TEST_ROMS_PATH = "test/roms";
    private static final String LAST_DOT_REGEX = "\\.+(?![^.]*\\.)";

    private static final String TEST_CPG_PATH = "test/players";

    //extremely hacky workaround for lack of ParameterizedContainer
    private final static Map<String, TestRomHandler> romHandlers = new HashMap<>();

    public static String[] getRomNames() {
        return Roms.getRoms(new int[]{1, 2, 3, 4, 5, 6, 7}, new Roms.Region[]{Roms.Region.USA}, false);
    }

    public static String[] getAllRomNames() {
        return Roms.getAllRoms();
    }

    public static String[] getRomNamesInFolder() {
        List<String> names;
        try (Stream<Path> paths = Files.walk(Paths.get(TEST_ROMS_PATH))) {
            names = paths.filter(Files::isRegularFile)
                    .map(p -> p.toFile().getName()).filter(s -> !s.endsWith(".txt"))
                    .map(s -> s.split(LAST_DOT_REGEX)[0])
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return names.toArray(new String[0]);
    }

    protected TestRomHandler romHandler;

    //TODO: once parameterized containers are available, make this a @BeforeEach
    // (and remove the hacky part)
    protected void activateRomHandler(String romName) {
        romHandler = romHandlers.get(romName);
        romHandler.prepare();
    }

    @AfterEach
    public void resetROMHandler() {
        romHandler.reset();
    }

    private static void loadROM(String romName) {
        Generation gen = getGenerationOf(romName);
        if (gen == null) {
            throw new IllegalArgumentException("Could not find the generation of " + romName);
        }
        String fullRomName = TEST_ROMS_PATH + "/" + romName + gen.getFileSuffix();
        RomHandler.Factory factory = gen.createFactory();
        if (!factory.isLoadable(fullRomName)) {
            throw new IllegalArgumentException("ROM is not loadable.");
        }
        RomHandler romHandler = factory.create();
        romHandler.loadRom(fullRomName);
        // Sets restrictions to... not restrict.
        // This can be overturned later for tests interested in certain restrictions.
        romHandler.getRestrictedSpeciesService().setRestrictions(new Settings());

        romHandlers.put(romName, new TestRomHandler(romHandler));
    }

    @BeforeAll
    static public void loadROMs() {
        for(String romName : getRomNames()) {
            System.out.println("Loading " + romName +  "...");
            loadROM(romName);
            System.out.println("Finished loading " + romName);
        }
    }



    protected static Generation getGenerationOf(String romName) {
        return Generation.GAME_TO_GENERATION.get(stripToBaseRomName(romName));
    }

    /**
     * Used to fast check the gen number of a ROM. Really useful for assume... methods, since loading the ROM to use
     * RomHandler.generationOfPokemon() is almost as slow as running the test cases. Increasingly relevant with
     * newer/bigger ROMs involved.
     */
    protected static int getGenerationNumberOf(String romName) {
        return getGenerationOf(romName).getNumber();
    }

    /**
     * A fast check whether a ROM uses an AbstractGBRomHandler.
     */
    protected static boolean isGBGame(String romName) {
        return getGenerationNumberOf(romName) <= 3;
    }

    /**
     * Strips the ROM name into just its base - e.g. "Crystal (S)" => "Crystal" and "Fire Red (U)(1.1)" => "Fire Red".
     *
     * @param romName The full name of the ROM
     */
    private static String stripToBaseRomName(String romName) {
        return romName.split("\\(")[0].trim();
    }

}
