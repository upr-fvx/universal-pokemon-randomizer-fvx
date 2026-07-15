package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CopyUpEvolutionsHelperTest {

    // Just a tiny class for making the printouts nice. Could probably be used in other places too.
    private static class NameOnlySpecies extends Species {
        static int number = 0;
        public NameOnlySpecies(String name) {
            number++;
            super(number);
            super.setName(name);
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private record CallCounter(
            Map<Species, Integer> noEvo,
            Map<Species, Integer> basic,
            Map<Species, Integer> evolved,
            Map<Species, Integer> split,
            Map<Species, Integer> altForme,
            Map<Species, Integer> cosmetic
    ) {}

    private final CallCounter callCounter = new CallCounter(
            new HashMap<>(), new HashMap<>(), new HashMap<>(),
            new HashMap<>(), new HashMap<>(), new HashMap<>()
    );

    private void applyCallCountingCUEH(SpeciesSet set, boolean evolutionSanity, boolean copySplitEvos) {
        applyCallCountingCUEH(set, evolutionSanity, copySplitEvos, false);
    }

    private void applyCallCountingCUEH(SpeciesSet set, boolean evolutionSanity, boolean copySplitEvos, boolean treatMegasAsEvos) {
        BiFunction<Species, Integer, Integer> addOne = (_, v) -> v == null ? 1 : v + 1;

        CopyUpEvolutionsHelper.Options options = new CopyUpEvolutionsHelper.Options
                .Builder(pk -> callCounter.basic.compute(pk, addOne), // basicAction
                         (_, pk, _) -> callCounter.evolved.compute(pk, addOne)) // evolvedAction
                .splitAction((_, pk, _) -> callCounter.split.compute(pk, addOne))
                .noEvoAction(pk -> callCounter.noEvo.compute(pk, addOne))
                .altFormeAction(((_, pk) -> callCounter.altForme.compute(pk, addOne)))
                .cosmeticAction(((_, pk) -> callCounter.cosmetic.compute(pk, addOne)))

                .evolutionSanity(evolutionSanity)
                .copySplitEvos(copySplitEvos)
                .treatMegasAsEvos(treatMegasAsEvos)
                .build();
        new CopyUpEvolutionsHelper(set).apply(options);
    }

    private void addEvolutionBetween(Species from, Species to) {
        Evolution evo = new Evolution(from, to, EvolutionType.LEVEL, 0);
        from.getEvolutionsFrom().add(evo);
        to.getEvolutionsTo().add(evo);
    }

    @Test
    public void NormalEvo_NoEvolutionSanity_UsesNoEvoAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        addEvolutionBetween(a, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, false, false);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1, b, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NormalEvo_EvolutionSanity_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        addEvolutionBetween(a, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitEvo_NoCopySplitEvos_UsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, b, 1, c, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitEvo_CopySplitEvos_UsesSplitAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(b, 1, c, 1), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitEvoIntoSameSpecies_NoCopySplitEvos_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitEvoIntoSameSpecies_CopySplitEvos_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NonCosmeticForme_NoEvolutionSanity_UsesAltFormeAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, false, false);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(b, 1), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NonCosmeticForme_EvolutionSanity_UsesAltFormeAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(b, 1), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NonCosmeticFormeAndEvo_NoEvolutionSanity_UsesAltFormeAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        b.addAltForme(1, c);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, false, true);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1, b, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(c, 1), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NonCosmeticFormeAndEvo_EvolutionSanity_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        b.addAltForme(1, c);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, b, 1), callCounter.basic);
        assertEquals(Map.of(c, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NonCosmeticFormeAndSplitEvo_EvolutionSanity_UsesSplitAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        b.addAltForme(1, c);
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(b, 1, c, 1), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void CosmeticForme_NoEvolutionSanity_UsesCosmeticAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);
        b.setEssentiallyCosmetic();

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, false, true);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(b, 1), callCounter.cosmetic);
    }

    @Test
    public void CosmeticForme_EvolutionSanity_UsesCosmeticAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);
        b.setEssentiallyCosmetic();

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(b, 1), callCounter.cosmetic);
    }

    @Test
    public void CosmeticFormeAndEvo_NoEvolutionSanity_UsesCosmeticAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        b.addAltForme(1, c);
        c.setEssentiallyCosmetic();
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, false, true);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1, b, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(c, 1), callCounter.cosmetic);
    }

    @Test
    public void CosmeticFormeAndEvo_EvolutionSanity_UsesCosmeticAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        b.addAltForme(1, c);
        c.setEssentiallyCosmetic();
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, b, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(c, 1), callCounter.cosmetic);
    }

    @Test
    public void MegaEvoForme_NoTreatMegasAsEvos_UsesAltFormeAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);
        b.setMegaEvolution(null);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(b, 1), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void MegaEvoForme_TreatMegasAsEvos_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);
        b.setMegaEvolution(null);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitMegaEvoForme_NoTreatMegasAsEvos_UsesAltFormeAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        a.addAltForme(1, b);
        b.setMegaEvolution(null);
        a.addAltForme(2, c);
        c.setMegaEvolution(null);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(b, 1, c, 1), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void SplitMegaEvoForme_TreatMegasAsEvos_NoCopySplitEvos_UsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        a.addAltForme(1, b);
        b.setMegaEvolution(null);
        a.addAltForme(2, c);
        c.setMegaEvolution(null);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, false, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, b, 1, c, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }


    @Test
    public void SplitMegaEvoForme_TreatMegasAsEvos_CopySplitEvos_UsesSplitAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        a.addAltForme(1, b);
        b.setMegaEvolution(null);
        a.addAltForme(2, c);
        c.setMegaEvolution(null);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(b, 1, c, 1), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NotInSet_BasicEvo_EvolutionUsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        addEvolutionBetween(a, b);

        SpeciesSet set = new SpeciesSet(List.of(b));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(b, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NotInSet_MiddleEvo_EvolutionUsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        addEvolutionBetween(a, b);
        addEvolutionBetween(b, c);

        SpeciesSet set = new SpeciesSet(List.of(a, c));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, c, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NotInSet_FinalEvo_MiddleEvoCountsAsFinal() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        addEvolutionBetween(a, b);
        addEvolutionBetween(b, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        new CopyUpEvolutionsHelper(set).apply(new CopyUpEvolutionsHelper.Options.Builder(
                null,
                (_, _, toMonIsFinalEvo) -> {
                    if (!toMonIsFinalEvo) fail();
                }
        ).build());
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NotInSet_SplitEvo_OtherSplitEvoUsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        addEvolutionBetween(a, b);
        addEvolutionBetween(a, c);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void NotInSet_BaseForme_AltFormeUsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        a.addAltForme(1, b);

        SpeciesSet set = new SpeciesSet(List.of(b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(b, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
        assertEquals(Map.of(), callCounter.altForme);
        assertEquals(Map.of(), callCounter.cosmetic);
    }

    @Test
    public void ComplexLine_AppliedInRightOrder() {
        Species rattata = new NameOnlySpecies("Rattata");
        Species raticate = new NameOnlySpecies("Raticate");
        Species rattataAlolan = new NameOnlySpecies("Rattata-Alolan");
        Species raticateAlolan = new NameOnlySpecies("Raticate-Alolan");
        Species raticateAlolanTotem = new NameOnlySpecies("Raticate-Alolan-Totem");

        rattata.addAltForme(1, rattataAlolan);
        raticate.addAltForme(1, raticateAlolan);
        raticate.addAltForme(2, raticateAlolanTotem);
        raticateAlolanTotem.setConceptualBaseForme(raticateAlolan);
        raticateAlolanTotem.setEssentiallyCosmetic();
        addEvolutionBetween(rattata, raticate);
        addEvolutionBetween(rattataAlolan, raticateAlolan);

        Map<Species, String> chainPerSpecies = new HashMap<>();
        SpeciesSet set = new SpeciesSet(List.of(rattata, raticate, rattataAlolan, raticateAlolan, raticateAlolanTotem));

        BasicSpeciesAction basicAction = pk -> chainPerSpecies.put(pk, pk.getName());
        AltFormeAction nonBasicAction = (from, to) -> chainPerSpecies.put(to, chainPerSpecies.get(from) + " -> " + to.getName());
        new CopyUpEvolutionsHelper(set).apply(
                new CopyUpEvolutionsHelper.Options
                        .Builder(basicAction, (from, to, _) -> nonBasicAction.applyTo(from, to))
                        .altFormeAction(nonBasicAction)
                        .cosmeticAction(nonBasicAction)
                        .build()
        );

        assertEquals("Rattata", chainPerSpecies.get(rattata));
        assertEquals("Rattata -> Raticate", chainPerSpecies.get(raticate));
        assertEquals("Rattata -> Rattata-Alolan", chainPerSpecies.get(rattataAlolan));
        assertEquals("Rattata -> Rattata-Alolan -> Raticate-Alolan", chainPerSpecies.get(raticateAlolan));
        assertEquals("Rattata -> Rattata-Alolan -> Raticate-Alolan -> Raticate-Alolan-Totem", chainPerSpecies.get(raticateAlolanTotem));
    }

}
