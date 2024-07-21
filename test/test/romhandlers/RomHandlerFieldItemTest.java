package test.romhandlers;

import com.dabomstew.pkrandom.game_data.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RomHandlerFieldItemTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void fieldItemsDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        List<Item> before = romHandler.getRegularFieldItems();
        System.out.println("before:");
        System.out.println(before);

        romHandler.setRegularFieldItems(before);

        List<Item> after = romHandler.getRegularFieldItems();
        System.out.println("after");
        System.out.println(after);
        // assumes it doesn't cache field items at any point
        assertEquals(before, after);
    }
}
