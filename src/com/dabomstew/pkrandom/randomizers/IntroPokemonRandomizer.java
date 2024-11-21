package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Random;

public class IntroPokemonRandomizer extends Randomizer {

    public IntroPokemonRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeIntroPokemon() {
        Species pk = rSpecService.getAll(true).getRandomSpecies(random);
        while (!romHandler.setIntroPokemon(pk)) {
            pk = rSpecService.getAll(true).getRandomSpecies(random);
        }
    }
}
