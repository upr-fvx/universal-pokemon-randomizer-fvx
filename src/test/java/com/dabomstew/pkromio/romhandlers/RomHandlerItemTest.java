package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.gamedata.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerItemTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void itemNamesExist(String romName) {
        loadROM(romName);
        for (Item item : romHandler.getItems()) {
            if (item == null) {
                System.out.println("null");
            } else {
                System.out.println((item.isAllowed() ? "" : "(B)-- ") + item);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void noQuestionMarkItemIsAllowed(String romName) {
        loadROM(romName);
        for (Item item : romHandler.getItems()) {
            if (item == null) continue;
            if (item.isAllowed()) {
                System.out.println(item);
                String withoutQuestionMarks = item.getName().replace("?", "").trim();
                assertNotEquals("", withoutQuestionMarks);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allTMsAreAllowed(String romName) {
        // No banned item should be counted as a TM, even if it technically is within the game's code.
        // This is a safeguard to prevent these from being used (also, so you can just ask "is this a TM?"
        // instead of asking whether the item is allowed as well).
        loadROM(romName);
        for (Item item : romHandler.getItems()) {
            if (item == null) continue;
            if (item.isTM()) {
                System.out.println(item);
                assertTrue(item.isAllowed());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allAllowedItemsWithTMNamesAreTMs(String romName) {
        // Might fail for non-English ROMs
        loadROM(romName);
        for (Item item : romHandler.getItems()) {
            if (item == null) continue;
            if (item.getName().matches("TM\\d+")) {

                System.out.println(item.getName());
                if (item.isAllowed()) {
                    assertTrue(item.isTM());
                } else {
                    System.out.println("-- banned");
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void allTMsHaveTMNames(String romName) {
        // Obviously fails for non-English ROMs
        assumeTrue(Roms.isOfRegion(romName, Roms.Region.USA, Roms.Region.EUROPE_ENGLISH));
        loadROM(romName);
        for (Item item : romHandler.getItems()) {
            if (item == null) continue;
            if (item.isTM()) {
                System.out.println(item.getName());
                assertTrue(item.getName().matches("TM\\d+"));
            }
        }
    }

}
