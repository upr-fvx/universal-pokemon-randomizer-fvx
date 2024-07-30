package com.dabomstew.pkrandom.game_data.cueh;

import com.dabomstew.pkrandom.game_data.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
