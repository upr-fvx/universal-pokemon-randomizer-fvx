package com.dabomstew.pkrandom.gamedata.cueh;

import com.dabomstew.pkrandom.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}