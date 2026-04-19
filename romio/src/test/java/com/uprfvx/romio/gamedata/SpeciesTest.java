package com.uprfvx.romio.gamedata;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class SpeciesTest {
    
    private static final int A_NUM = 1;
    private static final int B_NUM = 2;
    private static final int C_NUM = 3;
    private static final int D_NUM = 4;

    private Species a = new Species(A_NUM);
    private Species b = new Species(B_NUM);
    private Species c = new Species(C_NUM);
    private Species d = new Species(D_NUM);
    {
        a.setName("A");
        b.setName("B");
        c.setName("C");
        d.setName("D");
    }
    private Species aCopy = new Species(A_NUM);
    private Species bCopy = new Species(B_NUM);
    private Species cCopy = new Species(C_NUM);
    private Species dCopy = new Species(D_NUM);
    
    /**
     * Selects which Species objects may be used in this test.
     */
    private void use(Species... toUse) {
        if (!List.of(toUse).contains(a)) {
            a = null;
        } else if(!List.of(toUse).contains(b)) {
            b = null;
        } else if(!List.of(toUse).contains(c)) {
            c = null;
        } else if(!List.of(toUse).contains(d)) {
            d = null;
        } else if (!List.of(toUse).contains(aCopy)) {
            aCopy = null;
        } else if (!List.of(toUse).contains(bCopy)) {
            bCopy = null;
        } else if (!List.of(toUse).contains(cCopy)) {
            cCopy = null;
        } else if (!List.of(toUse).contains(dCopy)) {
            dCopy = null;
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
    
    @Test
    public void transferAttributesToCopy_OriginalIsNull_ThrowsNullPointerException() {
        assertThrowsExactly(NullPointerException.class,
                () -> Species.transferAttributesToCopy(null, Map.of()));
    }

    @Test
    public void transferAttributesToCopy_OriginalToCopiesIsNull_ThrowsNullPointerException() {
        use(a);
        assertThrowsExactly(NullPointerException.class,
                () -> Species.transferAttributesToCopy(a, null));
    }

    @Test
    public void transferAttributesToCopy_CopyIsNotInOriginalToCopies_ThrowsIllegalArgumentException() {
        use(a);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> Species.transferAttributesToCopy(a, Map.of()));
    }

    @Test
    public void transferAttributesToCopy_CopyHasDifferentNumber_ThrowsIllegalArgumentException() {
        use(a, b);
        Species aCopy = new Species(99);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> Species.transferAttributesToCopy(a, Map.of(a, aCopy)));
    }
    
    @Test
    public void transferAttributesToCopy_CopyHasDifferentClass_ThrowsIllegalArgumentException() {
        use(a, b);
        Species aCopy = new Gen1Species(A_NUM);
        assertThrowsExactly(IllegalArgumentException.class,
                () -> Species.transferAttributesToCopy(a, Map.of(a, aCopy)));
    }


    @Test
    public void transferAttributesToCopy_WithSimplestAttributes_TransfersAllAttributes() {
        // Simplest -> primitives and enums.
        // Items and BreedingInfo get their own tests since they rely on full Objects,
        // even if these are simple ones.
        use(a, aCopy);
        a.setName("original");
        a.setGeneration(1);
        a.setPrimaryType(Type.NORMAL);
        a.setSecondaryType(null);
        a.setHp(2);
        a.setAttack(3);
        a.setDefense(4);
        a.setSpatk(5);
        a.setSpdef(6);
        a.setSpeed(7);
        a.setSpecial(8);
        a.setAbility1(9);
        a.setAbility2(10);
        a.setAbility3(11);
        a.setExpYield(12);
        a.setCatchRate(13);
        a.setGenderRatio(14);
        a.setCallRate(15);
        a.setFrontImageDimensions(16);
        a.setGrowthCurve(ExpCurve.MEDIUM_FAST);

        System.out.println(a);
        System.out.println(aCopy);
        Species.transferAttributesToCopy(a, Map.of(a, aCopy));

        assertEquals("original", aCopy.getName());
        assertEquals(1, aCopy.getGeneration());
        assertEquals(Type.NORMAL, aCopy.getPrimaryType(false));
        assertNull(aCopy.getSecondaryType(false));
        assertEquals(2, aCopy.getHp());
        assertEquals(3, aCopy.getAttack());
        assertEquals(4, aCopy.getDefense());
        assertEquals(5, aCopy.getSpatk());
        assertEquals(6, aCopy.getSpdef());
        assertEquals(7, aCopy.getSpeed());
        assertEquals(8, aCopy.getSpecial());
        assertEquals(9, aCopy.getAbility1());
        assertEquals(10, aCopy.getAbility2());
        assertEquals(11, aCopy.getAbility3());
        assertEquals(12, aCopy.getExpYield());
        assertEquals(13, aCopy.getCatchRate());
        assertEquals(14, aCopy.getGenderRatio());
        assertEquals(15, aCopy.getCallRate());
        assertEquals(16, aCopy.getFrontImageDimensions());
        assertEquals(ExpCurve.MEDIUM_FAST, aCopy.getGrowthCurve());
    }

    @Test
    public void transferAttributesToCopy_WithGuaranteedHeldItem_TransfersGuaranteedHeldItem() {
        use(a, aCopy);
        Item guaranteedItem = new Item(1, "Guaranteed");
        a.setGuaranteedHeldItem(guaranteedItem);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy));

        assertEquals(guaranteedItem, aCopy.getGuaranteedHeldItem());
    }

    @Test
    public void transferAttributesToCopy_WithNonGuaranteedHeldItems_TransfersNonGuaranteedHeldItems() {
        use(a, aCopy);
        Item commonItem = new Item(1, "Common");
        Item rareItem = new Item(2, "Rare");
        Item darkGrassItem = new Item(3, "DarkGrass");
        a.setCommonHeldItem(commonItem);
        a.setRareHeldItem(rareItem);
        a.setDarkGrassHeldItem(darkGrassItem);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy));

        assertEquals(commonItem, aCopy.getCommonHeldItem());
        assertEquals(rareItem, aCopy.getRareHeldItem());
        assertEquals(darkGrassItem, aCopy.getDarkGrassHeldItem());
    }

    @Test
    public void transferAttributesToCopy_WithBreedingInfo_TransfersBreedingInfo() {
        use(a, aCopy);
        BreedingInfo breedingInfo = new BreedingInfo(EggGroup.FIELD, null, 1);
        a.setBreedingInfo(breedingInfo);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy));

        assertNotNull(aCopy.getBreedingInfo());
        assertEquals(EggGroup.FIELD, aCopy.getBreedingInfo().getPrimaryEggGroup());
        assertNull(aCopy.getBreedingInfo().getSecondaryEggGroup());
        assertEquals(1, aCopy.getBreedingInfo().getEggCycles());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_FormesGetCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy, b, bCopy));

        assertEquals(aCopy, aCopy.getForme(0));
        assertEquals(bCopy, aCopy.getForme(1));
    }

    @Test
    public void transferAttributesToCopy_WithFormes_CosmeticFormesGetCopied() {
        use(a, aCopy);
        a.addCosmeticAltForme(1);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy));

        assertEquals(List.of(0, 1), aCopy.getCosmeticFormeNumbers());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_FormeNumberGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);

        Species.transferAttributesToCopy(a, Map.of(a, aCopy, b, bCopy));
        Species.transferAttributesToCopy(b, Map.of(a, aCopy, b, bCopy));

        assertEquals(1, bCopy.getFormeNumber());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_FormeSuffixGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);
        b.setFormeSuffix("Suffix");

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);

        assertEquals("Suffix", bCopy.getFormeSuffix());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_BaseFormeGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);

        assertEquals(aCopy, bCopy.getBaseForme());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_ConceptualBaseFormeGetsCopied() {
        use(a, b, c, aCopy, bCopy, cCopy);
        a.addAltForme(1, b);
        a.addAltForme(2, c);
        c.setConceptualBaseForme(b);

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy, c, cCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);
        Species.transferAttributesToCopy(c, originalToCopy);

        assertEquals(bCopy, cCopy.getConceptualBaseForme());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_AlolanGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);
        b.setAlolan();

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);

        assertTrue(bCopy.isAlolan());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_EssentiallyCosmeticGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);
        b.setEssentiallyCosmetic();

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);

        assertTrue(bCopy.isEssentiallyCosmetic());
    }

    @Test
    public void transferAttributesToCopy_WithFormes_IgnoreCosmeticGetsCopied() {
        use(a, b, aCopy, bCopy);
        a.addAltForme(1, b);
        b.setEssentiallyCosmetic();
        b.setIgnoreCosmetic();

        Map<Species, Species> originalToCopy = Map.of(a, aCopy, b, bCopy);
        Species.transferAttributesToCopy(a, originalToCopy);
        Species.transferAttributesToCopy(b, originalToCopy);

        assertTrue(bCopy.isIgnoreCosmetic());
    }
}
