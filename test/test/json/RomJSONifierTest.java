package test.json;

import com.dabomstew.pkrandom.json.RomJSONifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import test.romhandlers.RomHandlerTest;

public class RomJSONifierTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void foo(String romName) {
        loadROM(romName);
        RomJSONifier fier = new RomJSONifier(romHandler);
        System.out.println(fier.logAll());
    }
}
