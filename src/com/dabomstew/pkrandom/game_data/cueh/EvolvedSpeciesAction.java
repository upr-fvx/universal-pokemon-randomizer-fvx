package com.dabomstew.pkrandom.game_data.cueh;

import com.dabomstew.pkrandom.game_data.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction<T extends Species> {
    void applyTo(T evFrom, T evTo, boolean toMonIsFinalEvo);
}