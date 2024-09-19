package test.romhandlers;

import com.dabomstew.pkrandom.gamedata.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void noAllQuestionMarkItemIsAllowed(String romName) {
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
        // No banned item should be counted as a TM, even if it technically is within the game of the code.
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

}
