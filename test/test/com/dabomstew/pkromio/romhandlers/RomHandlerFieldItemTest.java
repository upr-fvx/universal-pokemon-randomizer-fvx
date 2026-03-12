package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.gamedata.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RomHandlerFieldItemTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fieldItemsIsNotEmpty(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getFieldItems().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fieldItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Item> before = new ArrayList<>(romHandler.getFieldItems());
        System.out.println("before:");
        System.out.println(before);

        romHandler.setFieldItems(before);

        List<Item> after = romHandler.getFieldItems();
        System.out.println("after:");
        System.out.println(after);
        assertEquals(before, after);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fieldItemsReplacingTMsWithNonTMsAndViceVersaThrowsException(String romName) {
        loadROM(romName);
        List<Item> allItems = romHandler.getItems();

        // find a TM and a non-TM, to use as examples
        Item tm = null;
        Item nonTM = null;
        for (int i = 0; i < allItems.size() && (tm == null || nonTM == null); i++) {
            Item item = allItems.get(i);
            if (item == null) {
                continue;
            }
            if (item.isAllowed()) {
                if (item.isTM()) tm = item;
                else nonTM = item;
            }
        }

        // construct invalid inputs for setFieldItems() by replacing a non-TM with a TM, and vice versa.
        List<Item> withUnexpectedTM = new ArrayList<>(romHandler.getFieldItems());
        for (int i = 0; i < withUnexpectedTM.size(); i++) {
            if (!withUnexpectedTM.get(i).isTM()) {
                withUnexpectedTM.set(i, tm);
            }
        }
        List<Item> withUnexpectedNonTM = new ArrayList<>(romHandler.getFieldItems());
        for (int i = 0; i < withUnexpectedNonTM.size(); i++) {
            if (withUnexpectedNonTM.get(i).isTM()) {
                withUnexpectedNonTM.set(i, nonTM);
            }
        }

        assertThrows(IllegalArgumentException.class, () -> romHandler.setFieldItems(withUnexpectedTM));
        assertThrows(IllegalArgumentException.class, () -> romHandler.setFieldItems(withUnexpectedNonTM));
    }
}
