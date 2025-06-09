package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Random;

public class IntroPokemonRandomizer extends Randomizer {

    Species introSpecies;

    public IntroPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeIntroPokemon() {
        Species pk = rSpecService.getAll(true).getRandomSpecies(random);
        while (!romHandler.setIntroPokemon(pk)) {
            pk = rSpecService.getAll(true).getRandomSpecies(random);
        }
        introSpecies = pk;
        changesMade = true;
    }

    public Species getIntroSpecies() {
        return introSpecies;
    }
}
