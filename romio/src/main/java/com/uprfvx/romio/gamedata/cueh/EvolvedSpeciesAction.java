package com.uprfvx.romio.gamedata.cueh;

import com.uprfvx.romio.gamedata.Species;

@FunctionalInterface
public interface EvolvedSpeciesAction {
    void applyTo(Species evFrom, Species evTo, boolean toMonIsFinalEvo);
}