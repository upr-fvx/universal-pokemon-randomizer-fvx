package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.romhandlers.Generation;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.romhandlers.Roms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

//TODO: once supported by JUnit, convert to ParameterizedContainer
//support is intended to be added sometime this year? that's all I got
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RandomizerTest {


    protected static final Random RND = new Random();

    private static final String TEST_ROMS_PATH = System.getProperty("romsPath");

    //extremely hacky workaround for lack of ParameterizedContainer
    private final static Map<String, TestRomHandler> romHandlers = new HashMap<>();

    public static String[] getRomNames() {
        return Roms.getRoms(new int[]{4,5,6,7}, new Roms.Region[]{Roms.Region.USA}, true, true, false);
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
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());

        romHandlers.put(romName, new TestRomHandler(romHandler));
    }

    @BeforeAll
    static public void loadROMs() {
        List<String> romNames = Arrays.asList(getRomNames());
        romNames.sort(Comparator.comparingInt(RandomizerTest::getGenerationNumberOf));
        Collections.reverse(romNames);
        //load late generations first, when there should be the most memory available
        //sort and reverse rather than simply reverse sort because US/UM should be before S/M
        for(String romName : romNames) {
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
     * Strips the ROM name into just its base - e.g. "Crystal (S)" => "Crystal" and "Fire Red (U)(1.1)" => "Fire Red".
     *
     * @param romName The full name of the ROM
     */
    private static String stripToBaseRomName(String romName) {
        return romName.split("\\(")[0].trim();
    }

}
