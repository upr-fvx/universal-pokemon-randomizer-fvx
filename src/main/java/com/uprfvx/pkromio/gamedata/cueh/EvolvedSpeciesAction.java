package com.uprfvx.pkromio.gamedata.cueh;

import com.uprfvx.pkromio.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}