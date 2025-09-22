package test.com.dabomstew.pkromio.gamedata;

import com.dabomstew.pkromio.gamedata.GenRestrictions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GenRestrictionsTest {

    @Test
    public void initWithNoArgs_AllowsAllGens() {
        GenRestrictions gr = new GenRestrictions();
        for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            assertTrue(gr.isGenAllowed(gen));
        }
    }

    @Test
    public void initWithNoArgs_AllowsEvolutionaryRelatives() {
        GenRestrictions gr = new GenRestrictions();
        assertTrue(gr.isAllowEvolutionaryRelatives());
    }

    @Test
    public void initWithIntArg_StateIsZero_AllowsNoGens() {
        GenRestrictions gr = new GenRestrictions(0);
        for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            assertFalse(gr.isGenAllowed(gen));
        }
    }

    @Test
    public void initWithIntArg_StateIsZero_DoesNotAllowsEvolutionaryRelatives() {
        GenRestrictions gr = new GenRestrictions(0);
        assertFalse(gr.isAllowEvolutionaryRelatives());
    }

    @Test
    public void initWithIntArg_StateIsTwo_AllowsOnlyGen1() {
        GenRestrictions gr = new GenRestrictions(2);
        assertTrue(gr.isGenAllowed(1));
        for (int gen = 2; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            assertFalse(gr.isGenAllowed(gen));
        }
    }

    @Test
    public void nothingSelected_NoGenAllowed_ReturnsTrue() {
        GenRestrictions gr = new GenRestrictions(0);
        assertTrue(gr.nothingSelected());
    }

    @Test
    public void nothingSelected_AllGensAllowed_ReturnsTrue() {
        GenRestrictions gr = new GenRestrictions();
        assertFalse(gr.nothingSelected());
    }

    @Test
    public void toInt_NoGenAllowed_ReturnsZero() {
        GenRestrictions gr = new GenRestrictions(0);
        assertEquals(0, gr.toInt());
    }

    @Test
    public void toInt_Gen1Allowed_ReturnsTwo() {
        GenRestrictions gr = new GenRestrictions(0);
        gr.setGenAllowed(1, true);
        assertEquals(0b10, gr.toInt());
    }

    @Test
    public void toInt_Gen3Allowed_ReturnsEight() {
        GenRestrictions gr = new GenRestrictions(0);
        gr.setGenAllowed(3, true);
        assertEquals(0b1000, gr.toInt());
    }

    @Test
    public void toInt_Gen13Allowed_ReturnsTen() {
        GenRestrictions gr = new GenRestrictions(0);
        gr.setGenAllowed(1, true);
        gr.setGenAllowed(3, true);
        assertEquals(0b1010, gr.toInt());
    }

    @Test
    public void setGenAllowed_Gen1True_IsMethodReturnsTrue() {
        GenRestrictions gr = new GenRestrictions(0); // init it as false
        gr.setGenAllowed(1, true);
        assertTrue(gr.isGenAllowed(1));
    }

    @Test
    public void setGenAllowed_Gen1False_IsMethodReturnsFalse() {
        GenRestrictions gr = new GenRestrictions(1); // init it as true
        gr.setGenAllowed(1, false);
        assertFalse(gr.isGenAllowed(1));
    }

    @Test
    public void limitToGen_AllAllowedLimitTo3_Gen123Allowed() {
        GenRestrictions gr = new GenRestrictions();
        gr.limitToGen(3);
        for (int gen = 1; gen <= 3; gen++) {
            assertTrue(gr.isGenAllowed(gen));
        }
        for (int gen = 4; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            assertFalse(gr.isGenAllowed(gen));
        }
    }

    @Test
    public void limitToGen_Gen3To5AllowedLimitTo3_Gen3Allowed() {
        GenRestrictions gr = new GenRestrictions(0);
        gr.setGenAllowed(3, true);
        gr.setGenAllowed(4, true);
        gr.setGenAllowed(5, true);
        gr.limitToGen(3);
        assertFalse(gr.isGenAllowed(1));
        assertFalse(gr.isGenAllowed(2));
        assertTrue(gr.isGenAllowed(3));
        for (int gen = 4; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            assertFalse(gr.isGenAllowed(gen));
        }
    }

    @Test
    public void setAllowEvolutionaryRelatives_True_IsMethodReturnsTrue() {
        GenRestrictions gr = new GenRestrictions(0); // init it as false
        gr.setAllowEvolutionaryRelatives(true);
        assertTrue(gr.isAllowEvolutionaryRelatives());
    }

    @Test
    public void setAllowEvolutionaryRelatives_False_GetMethodReturnsFalse() {
        GenRestrictions gr = new GenRestrictions(); // init it as true
        gr.setAllowEvolutionaryRelatives(false);
        assertFalse(gr.isAllowEvolutionaryRelatives());
    }

    // This class contains no tests for allowTrainerSwapMegaEvolvables(),
    // megaEvolutionsOfEveryTypeAreInPool(), and megaEvolutionsAreInPool().
    // This is because they do *not* fit inside GenRestrictions.
    // When they are moved to a more appropriate class, they may be tested there.

}
