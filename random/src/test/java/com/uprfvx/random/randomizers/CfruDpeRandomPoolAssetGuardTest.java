package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CfruDpeRandomPoolAssetGuardTest {

    @Test
    public void unusableSpeciesReturnsOnlyCandidatesRejectedByAssetPredicate() {
        Species valid = species(1, "Valid");
        Species invalid = species(2, "Invalid");
        SpeciesSet candidates = new SpeciesSet();
        candidates.add(valid);
        candidates.add(invalid);

        SpeciesSet unusable = CfruDpeRandomPoolAssetGuard.unusableSpecies(candidates, species -> species != invalid);

        assertEquals(Set.of(invalid), unusable);
        assertEquals(Set.of(valid, invalid), candidates);
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }
}
