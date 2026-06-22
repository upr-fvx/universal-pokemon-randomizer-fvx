package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction<T extends Species> {
    // TODO: no reason to have these be generic now that Gen1Species is gone
    void applyTo(T sp);
}
