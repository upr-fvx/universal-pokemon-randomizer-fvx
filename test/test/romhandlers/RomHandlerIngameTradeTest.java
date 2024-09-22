package test.romhandlers;

import com.dabomstew.pkrandom.gamedata.IngameTrade;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RomHandlerIngameTradeTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void tradesDoNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        // assumes trades are not cached by the RomHandler
        List<IngameTrade> before = romHandler.getIngameTrades();
        System.out.println("Before: " + before);
        romHandler.setIngameTrades(before);
        List<IngameTrade> after = romHandler.getIngameTrades();
        System.out.println("After: " + after);
        assertEquals(before, after);
    }

}
