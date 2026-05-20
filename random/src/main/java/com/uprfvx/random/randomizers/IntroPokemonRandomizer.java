package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.Random;

public class IntroPokemonRandomizer extends Randomizer {

    Species introSpecies;

    public IntroPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeIntroPokemon() {
        SpeciesSet candidates = new SpeciesSet(rSpecService.getAll(true));
        while (!candidates.isEmpty()) {
            Species pk = candidates.getRandomSpecies(random, true);
            if (romHandler.setIntroPokemon(pk)) {
                introSpecies = pk;
                changesMade = true;
                return;
            }
        }
    }

    public Species getIntroSpecies() {
        return introSpecies;
    }
}
