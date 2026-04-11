package com.uprfvx.romio.gamedata;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class SpeciesTest {

    private Species a, b, c, d;

    // This is a bit silly, but usage-wise it's nice
    private void use(Species... toUse) {
        for (Species pk : toUse) {
            if (pk == a) {
                a = new Species(1);
                a.setName("a");
            } else if (pk == b) {
                b = new Species(2);
                b.setName("b");
            } else if (pk == c) {
                c = new Species(3);
                c.setName("c");
            } else if (pk == d) {
                d = new Species(4);
                d.setName("d");
            }
        }
    }

    @Test
    public void getBaseForme_OfBaseForme_ReturnsItself() {
        use(a);
        assertEquals(a, a.getBaseForme());
    }

    @Test
    public void getBaseForme_OfAltForme_ReturnsBaseForme() {
        use(a, b);
        a.addAltForme(1, b);
        assertEquals(a, b.getBaseForme());
    }

    @Test
    public void isBaseForme_OnUnmodifiedSpecies_ReturnsTrue() {
        assertTrue(new Species(0).isBaseForme());
    }

    @Test
    public void isBaseForme_OnSpeciesWithAltForme_ReturnsTrue() {
        use(a, b);
        a.addAltForme(1, b);
        assertTrue(a.isBaseForme());
    }

    @Test
    public void isBaseForme_OnAltForme_ReturnsFalse() {
        use(a, b);
        a.addAltForme(1, b);
        assertFalse(b.isBaseForme());
    }

    @Test
    public void setConceptualBaseForme_AlreadyHasConceptualBaseForme_ThrowsIllegalStateException() {
        use(a, b, c, d);
        a.addAltForme(1, b);
        a.addAltForme(2, c);
        a.addAltForme(3, d);
        d.setConceptualBaseForme(b);
        assertThrowsExactly(IllegalStateException.class,
                () -> d.setConceptualBaseForme(c));
    }

    @Test
    public void setConceptualBaseForme_OnBaseForme_ThrowsIllegalStateException() {
        use(a, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.setConceptualBaseForme(a));
    }

    @Test
    public void setConceptualBaseForme_ConceptualBaseFormeIsBaseForme_ThrowsIllegalArgumentException() {
        use(a, b);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> b.setConceptualBaseForme(a));
    }

    @Test
    public void setConceptualBaseForme_ConceptualBaseFormeIsAltFormeOfAnotherSpecies_ThrowsIllegalStateException() {
        use(a, b, c, d);
        a.addAltForme(1, b);
        c.addAltForme(1, d);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.setConceptualBaseForme(d));
    }

    @Test
    public void addAltForme_FormeNumberIsUniqueAltFormeIsUnchanged_DoesNotThrow() {
        use(a, b);
        a.addAltForme(1, b);
    }

    @Test
    public void addAltForme_ToItself_ThrowsIllegalArgumentException() {
        use(a);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> a.addAltForme(1, a));
    }

    @Test
    public void addAltForme_ToAltForme_ThrowsIllegalStateException() {
        use(a, b, c);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.addAltForme(2, c));
    }

    @Test
    public void addAltForme_FormeNumberIsZero_ThrowsIllegalStateException() {
        // IllegalState instead of IllegalArgument because internally it's the same check as
        // any other overlapping forme number.
        use(a, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> a.addAltForme(0, b));
    }

    @Test
    public void addAltForme_FormeNumberIsAlreadyUsed_ThrowsIllegalStateException() {
        use(a, b, c);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> a.addAltForme(1, c));
    }

    @Test
    public void addAltForme_AltFormeIsAltFormeOfAnotherSpecies_ThrowsIllegalStateException() {
        use(a, b, c);
        a.addAltForme(1, c);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.addAltForme(2, c));
    }

    @Test
    public void addCosmeticAltForme_ToAltForme_ThrowsIllegalStateException() {
        use(a, b);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.addCosmeticAltForme(2));
    }

    @Test
    public void addCosmeticAltForme_FormeNumberIsAlreadyUsed_ThrowsIllegalStateException() {
        use(a, b);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> a.addCosmeticAltForme(1));
    }

    @Test
    public void getForme_OfAltForme_ThrowsIllegalStateException() {
        use(a, b);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                () -> b.getForme(0));
    }

    @Test
    public void getForme_FormeNumberIsZero_ReturnsItself() {
        use(a);
        assertEquals(a, a.getForme(0));
    }

    @Test
    public void getForme_FormeNumberIsThatOfNonCosmeticForme_ReturnsThatForme() {
        use(a, b);
        a.addAltForme(1, b);
        assertEquals(b, a.getForme(1));
    }

    @Test
    public void getForme_FormeNumberIsThatOfCosmeticForme_ReturnsItself() {
        use(a);
        a.addCosmeticAltForme(1);
        assertEquals(a, a.getForme(1));
    }

    @Test
    public void getForme_FormeNumberIsUnassigned_ThrowsNoSuchElementException() {
        use(a);
        assertThrowsExactly(NoSuchElementException.class,
                () -> a.getForme(1));
    }

    @Test
    public void getAltFormes_NoAltFormesAssigned_ReturnsEmptySpeciesSet() {
        use(a);
        assertEquals(new SpeciesSet(), a.getAltFormes());
    }

    @Test
    public void getAltFormes_NonCosmeticAltFormesAssigned_ReturnsSpeciesSetWithThoseFormes() {
        use(a, b, c);
        a.addAltForme(1, b);
        a.addAltForme(2, c);
        assertEquals(new SpeciesSet(List.of(b, c)), a.getAltFormes());
    }

    @Test
    public void getAltFormes_OnlyCosmeticAltFormesAssigned_ReturnsEmptySpeciesSet() {
        use(a);
        a.addCosmeticAltForme(1);
        assertEquals(new SpeciesSet(), a.getAltFormes());
    }

    @Test
    public void getFormeNumber_OfBaseForme_ReturnsZero() {
        use(a);
        assertEquals(0, a.getFormeNumber());
    }

    @Test
    public void getFormeNumber_OfAltForme_ReturnsFormeNumberAssignedInSet() {
        use(a, b);
        a.addAltForme(1, b);
        assertEquals(1, b.getFormeNumber());
    }

    @Test
    public void setAlolan_OnBaseForme_ThrowsIllegalStateException() {
        use(a);
        assertThrowsExactly(IllegalStateException.class,
                a::setAlolan);
    }

    @Test
    public void setEssentiallyCosmetic_OnBaseForme_ThrowsIllegalStateException() {
        use(a);
        assertThrowsExactly(IllegalStateException.class,
                a::setEssentiallyCosmetic);
    }

    @Test
    public void setIgnoreCosmetic_OnBaseForme_ThrowsIllegalStateException() {
        use(a);
        assertThrowsExactly(IllegalStateException.class,
                a::setIgnoreCosmetic);
    }

    @Test
    public void setIgnoreCosmetic_OnNonEssentiallyCosmeticAltForme_ThrowsIllegalStateException() {
        use(a, b);
        a.addAltForme(1, b);
        assertThrowsExactly(IllegalStateException.class,
                b::setIgnoreCosmetic);
    }
}
