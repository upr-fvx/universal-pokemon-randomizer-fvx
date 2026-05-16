package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;

import java.util.function.Predicate;

final class CfruDpeRandomPoolAssetGuard {

    private CfruDpeRandomPoolAssetGuard() {
    }

    static SpeciesSet unusableSpecies(SpeciesSet candidates, Predicate<Species> usableSpecies) {
        SpeciesSet unusable = new SpeciesSet();
        for (Species species : candidates) {
            if (!usableSpecies.test(species)) {
                unusable.add(species);
            }
        }
        return unusable;
    }
}
