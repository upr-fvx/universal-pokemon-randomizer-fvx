package test.com.dabomstew.pkromio.gbspace;

import com.dabomstew.pkromio.gbspace.BankDividedFreedSpace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BankDividedFreedSpaceTest {

    @Test
    public void freedSpaceChunksDoNotMergeOverBankBorders() {
        BankDividedFreedSpace fs = new BankDividedFreedSpace(4, 4, new int[0]);
        fs.free(0, 4);
        fs.free(4, 4);
        fs.free(12, 4);
        fs.free(8, 4);
        System.out.println(fs);
        assertEquals(-1, fs.findAndUnfree(5));
    }

    @Test
    public void canUnfreeFromReservedBankIfSpecified() {
        BankDividedFreedSpace fs = new BankDividedFreedSpace(4, 4, new int[]{0});
        fs.free(0, 4);
        System.out.println(fs);
        assertNotEquals(-1, fs.findAndUnfreeInBank(4, 0));
    }

    @Test
    public void canNotUnfreeFromReservedBankIfNotSpecified() {
        BankDividedFreedSpace fs = new BankDividedFreedSpace(4, 4, new int[]{0});
        fs.free(0, 4);
        System.out.println(fs);
        assertEquals(-1, fs.findAndUnfree(4));
    }

    @Test
    public void canUnfreeFromNonReservedBankWhenNotSpecified() {
        BankDividedFreedSpace fs = new BankDividedFreedSpace(4, 4, new int[]{0, 2, 3});
        fs.free(0, 4);
        fs.free(5, 1);
        System.out.println(fs);
        assertEquals(5, fs.findAndUnfree(1));
    }

}
