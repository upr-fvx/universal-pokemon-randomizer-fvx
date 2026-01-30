package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.gamedata.PickupItem;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerPickupItemTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void pickupItemsDoNotChangeWithGetAndSet(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        loadROM(romName);
        List<PickupItem> before = romHandler.getPickupItems();
        System.out.println("before:");
        System.out.println(toTable(before));

        romHandler.setPickupItems(before);

        List<PickupItem> after = romHandler.getPickupItems();
        System.out.println("after");
        System.out.println(toTable(after));
        // assumes it doesn't cache pickup items at any point
        assertEquals(before, after);
    }

    private String toTable(List<PickupItem> pickupItems) {
        StringBuilder sb = new StringBuilder();
        int maxNameLength = 0;
        for (PickupItem pi : pickupItems) {
            maxNameLength = Math.max(maxNameLength, pi.getItem().getName().length());
        }

        sb.append(new String(new char[maxNameLength]).replace("\0", " "));
        sb.append("| 1-10|11-20|21-30|31-40|41-50|51-60|61-70|71-80|81-90|<=100|\n");

        for (PickupItem pi : pickupItems) {
            sb.append(new String(new char[maxNameLength - pi.getItem().getName().length()])
                    .replace("\0", " "));
            sb.append(pi.getItem().getName());
            sb.append("|");
            for (int i = 0; i < PickupItem.PROBABILITY_SLOTS; i++) {
                String probString = pi.getProbabilities()[i] == 0 ? "-" : pi.getProbabilities()[i] + " %";
                sb.append(new String(new char[5 - probString.length()]).replace("\0", " "));
                sb.append(probString);
                sb.append("|");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
