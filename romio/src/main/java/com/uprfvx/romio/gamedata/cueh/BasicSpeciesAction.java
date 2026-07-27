package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface BasicSpeciesAction {
    void applyTo(Species sp);
}
