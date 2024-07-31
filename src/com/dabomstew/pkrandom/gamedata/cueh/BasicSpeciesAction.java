package com.dabomstew.pkrandom.gamedata.cueh;

import com.dabomstew.pkrandom.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
