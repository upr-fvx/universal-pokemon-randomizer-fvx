package test.romhandlers;

import com.dabomstew.pkrandom.gamedata.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

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

}
