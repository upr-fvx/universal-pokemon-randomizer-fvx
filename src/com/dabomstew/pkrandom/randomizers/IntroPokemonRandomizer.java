package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.game_data.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Random;

public class IntroPokemonRandomizer extends Randomizer {

    public IntroPokemonRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeIntroPokemon() {
        Species pk = rPokeService.getAll(true).getRandomSpecies(random);
        while (!romHandler.setIntroPokemon(pk)) {
            pk = rPokeService.getAll(true).getRandomSpecies(random);
        }
    }
}
