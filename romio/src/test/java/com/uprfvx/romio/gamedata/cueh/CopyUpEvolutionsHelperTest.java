package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.*;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

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
            Map<NameOnlySpecies, Integer> noEvo,
            Map<NameOnlySpecies, Integer> basic,
            Map<NameOnlySpecies, Integer> evolved,
            Map<NameOnlySpecies, Integer> split
    ) {}

    private final CallCounter callCounter = new CallCounter(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());

    private void applyCallCountingCUEH(SpeciesSet set, boolean evolutionSanity, boolean copySplitEvos) {
        BiFunction<NameOnlySpecies, Integer, Integer> addOne = (_, v) -> v == null ? 1 : v + 1;
        new CopyUpEvolutionsHelper<NameOnlySpecies>(set).apply(evolutionSanity, copySplitEvos,
                pk -> callCounter.basic.compute(pk, addOne),
                (_, pk, _) -> callCounter.evolved.compute(pk, addOne),
                (_, pk, _) -> callCounter.split.compute(pk, addOne),
                pk -> callCounter.noEvo.compute(pk, addOne)
                );
    }

    @Test
    public void NormalEvo_NoEvolutionSanity_UsesNoEvoAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, false, false);

        System.out.println(callCounter);
        assertEquals(Map.of(a, 1, b, 1), callCounter.noEvo);
        assertEquals(Map.of(), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
    }

    @Test
    public void NormalEvo_EvolutionSanity_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
    }

    @Test
    public void SplitEvo_NoCopySplitEvos_UsesBasicAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);
        Evolution evo2 = new Evolution(a, c, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo2);
        c.getEvolutionsTo().add(evo2);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, false);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1, b, 1, c, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
    }

    @Test
    public void SplitEvo_CopySplitEvos_UsesSplitAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Species c = new NameOnlySpecies("C");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);
        Evolution evo2 = new Evolution(a, c, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo2);
        c.getEvolutionsTo().add(evo2);

        SpeciesSet set = new SpeciesSet(List.of(a, b, c));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(), callCounter.evolved);
        assertEquals(Map.of(b, 1, c, 1), callCounter.split);
    }

    @Test
    public void SplitEvoIntoSameSpecies_NoCopySplitEvos_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);
        Evolution evo2 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo2);
        b.getEvolutionsTo().add(evo2);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
    }

    @Test
    public void SplitEvoIntoSameSpecies_CopySplitEvos_UsesEvolvedAction() {
        Species a = new NameOnlySpecies("A");
        Species b = new NameOnlySpecies("B");
        Evolution evo1 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo1);
        b.getEvolutionsTo().add(evo1);
        Evolution evo2 = new Evolution(a, b, EvolutionType.LEVEL, 0);
        a.getEvolutionsFrom().add(evo2);
        b.getEvolutionsTo().add(evo2);

        SpeciesSet set = new SpeciesSet(List.of(a, b));
        applyCallCountingCUEH(set, true, true);

        System.out.println(callCounter);
        assertEquals(Map.of(), callCounter.noEvo);
        assertEquals(Map.of(a, 1), callCounter.basic);
        assertEquals(Map.of(b, 1), callCounter.evolved);
        assertEquals(Map.of(), callCounter.split);
    }

}
