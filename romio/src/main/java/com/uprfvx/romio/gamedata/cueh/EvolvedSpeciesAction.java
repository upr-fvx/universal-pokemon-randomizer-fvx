package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}