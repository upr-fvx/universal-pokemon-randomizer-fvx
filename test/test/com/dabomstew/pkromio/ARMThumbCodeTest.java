package test.com.dabomstew.pkromio;

import com.dabomstew.pkromio.ARMThumbCode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ARMThumbCodeTest {

    private static final byte[] IN = new byte[]{ // without any branch instructions
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    };

    private static final byte[] IN_BL = new byte[]{ // with BL instructions
            0, (byte) 0xF0, 50, (byte) 0xF8,
            0, (byte) 0xF0, 60, (byte) 0xF8,
            0, (byte) 0xF0, 70, (byte) 0xF8,
    };

    private static final int END_OFFSET = IN.length;

    @Test
    public void toBytesSameAsOriginalBytes() {
        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        System.out.println(code);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertArrayEquals(IN, out);
    }

    @Test
    public void canInsertBytesAtStart() {
        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.insertInstructions(0, (byte) 0x20, (byte) 0x21);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals((byte) 0x20, out[0]);
        assertEquals((byte) 0x21, out[1]);
    }

    @Test
    public void canInsertBytesAtEnd() {
        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.insertInstructions(END_OFFSET, (byte) 0x20, (byte) 0x21);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals((byte) 0x20, out[out.length - 2]);
        assertEquals((byte) 0x21, out[out.length - 1]);
    }

    @Test
    public void canInsertBytesAtMiddle() {
        int insertPoint = 2;

        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.insertInstructions(insertPoint, (byte) 0x20, (byte) 0x21);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals((byte) 0x20, out[insertPoint]);
        assertEquals((byte) 0x21, out[insertPoint + 1]);
    }

    @Test
    public void canRemoveFromStart() {
        int remLen = 2;

        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.removeInstructions(0, remLen);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals(IN.length - remLen * 2, out.length);
        assertArrayEquals(Arrays.copyOfRange(IN, remLen * 2, IN.length), out);
    }

    @Test
    public void canRemoveFromEnd() {
        int remLen = 2;

        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.removeInstructions(IN.length - remLen * 2, remLen);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals(IN.length - remLen * 2, out.length);
        assertArrayEquals(Arrays.copyOfRange(IN, 0, IN.length - remLen * 2), out);
    }

    @Test
    public void canRemoveFromMiddle() {
        int remLen = 2;
        int remPoint = 2;

        System.out.println("before: " + Arrays.toString(IN));
        ARMThumbCode code = new ARMThumbCode(IN);
        code.removeInstructions(remPoint, remLen);
        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals(IN.length - remLen * 2, out.length);
        assertArrayEquals(Arrays.copyOfRange(IN, 0, remPoint), Arrays.copyOfRange(out, 0, remPoint));
        assertArrayEquals(Arrays.copyOfRange(IN, remPoint + remLen * 2, IN.length),
                Arrays.copyOfRange(out, remPoint, out.length));
    }

    @Test
    public void blDoesNotChangeFromInsertionAfter() {
        System.out.println("before: " + Arrays.toString(IN_BL));

        ARMThumbCode code = new ARMThumbCode(IN_BL);
        System.out.println(code);
        code.insertInstructions(0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
        System.out.println(code);

        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        assertEquals(IN_BL[0], out[0]);
        assertEquals(IN_BL[1], out[1]);
        assertEquals(IN_BL[2], out[2]);
        assertEquals(IN_BL[3], out[3]);
    }

    @Test
    public void blDisplacementIncreasesFromInsertionBeforeBySameAmount() {
        System.out.println("before: " + Arrays.toString(IN_BL));

        ARMThumbCode code = new ARMThumbCode(IN_BL);
        System.out.println(code);
        code.insertInstructions(0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
        System.out.println(code);

        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        // We check the first two BL instructions.
        // Just the lowest disp bits should have changed here
        assertEquals(IN_BL[0], out[4]);
        assertEquals(IN_BL[1], out[5]);
        assertEquals(IN_BL[2] + 4, out[6]);
        assertEquals(IN_BL[3], out[7]);
        // ---
        assertEquals(IN_BL[4], out[8]);
        assertEquals(IN_BL[5], out[9]);
        assertEquals(IN_BL[6] + 4, out[10]);
        assertEquals(IN_BL[7], out[11]);
    }

    @Test
    public void blDisplacementDecreasesFromRemovalBeforeBySameAmount() {
        System.out.println("before: " + Arrays.toString(IN_BL));

        ARMThumbCode code = new ARMThumbCode(IN_BL);
        System.out.println(code);
        code.removeInstructions(0, 2);
        System.out.println(code);

        byte[] out = code.toBytes();
        System.out.println("after: " + Arrays.toString(out));

        // We check the first two BL instructions.
        // Just the lowest disp bits should have changed here
        assertEquals(IN_BL[4], out[0]);
        assertEquals(IN_BL[5], out[1]);
        assertEquals(IN_BL[6] - 4, out[2]);
        assertEquals(IN_BL[7], out[3]);
        // ---
        assertEquals(IN_BL[8], out[4]);
        assertEquals(IN_BL[9], out[5]);
        assertEquals(IN_BL[10] - 4, out[6]);
        assertEquals(IN_BL[11], out[7]);
    }



}
