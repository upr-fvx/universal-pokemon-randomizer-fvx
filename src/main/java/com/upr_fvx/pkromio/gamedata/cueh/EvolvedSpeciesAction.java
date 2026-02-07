package com.upr_fvx.pkromio.gamedata.cueh;

import com.upr_fvx.pkromio.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}