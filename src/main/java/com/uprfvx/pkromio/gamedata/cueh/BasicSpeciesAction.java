package com.uprfvx.pkromio.gamedata.cueh;

import com.uprfvx.pkromio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
