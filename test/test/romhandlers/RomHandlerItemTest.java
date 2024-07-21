package test.romhandlers;

import com.dabomstew.pkrandom.game_data.Item;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class RomHandlerItemTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void itemNamesExist(String romName) {
        loadROM(romName);
        List<Item> items = romHandler.getItems();
        items.forEach(System.out::println);
    }

}
