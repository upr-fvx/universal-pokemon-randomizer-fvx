package com.dabomstew.pkromio.gamedata.cueh;

import com.dabomstew.pkromio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
