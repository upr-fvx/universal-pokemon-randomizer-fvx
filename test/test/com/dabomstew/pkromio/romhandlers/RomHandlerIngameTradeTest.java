package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.gamedata.InGameTrade;
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
        List<InGameTrade> before = romHandler.getInGameTrades();
        System.out.println("Before: " + before);
        romHandler.setInGameTrades(before);
        List<InGameTrade> after = romHandler.getInGameTrades();
        System.out.println("After: " + after);
        assertEquals(before, after);
    }

}
