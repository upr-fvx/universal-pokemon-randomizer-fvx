package com.upr_fvx.pkromio.gamedata.cueh;

import com.upr_fvx.pkromio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
