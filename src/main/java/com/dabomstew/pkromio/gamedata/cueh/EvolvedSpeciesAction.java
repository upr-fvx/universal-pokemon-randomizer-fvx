package com.dabomstew.pkromio.gamedata.cueh;

import com.dabomstew.pkromio.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}