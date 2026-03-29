package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    void applyTo(T sp);
}
