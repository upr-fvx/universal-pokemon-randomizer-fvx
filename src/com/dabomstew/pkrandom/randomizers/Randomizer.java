package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.cueh.CopyUpEvolutionsHelper;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.services.RestrictedPokemonService;
import com.dabomstew.pkrandom.services.TypeService;

import java.util.Random;

/**
 * An abstract superclass for all randomizers acting on a {@link RomHandler}.
 */
public abstract class Randomizer {

    protected final RomHandler romHandler;
    protected final RestrictedPokemonService rPokeService;
    protected final TypeService typeService;
    protected final CopyUpEvolutionsHelper<Species> copyUpEvolutionsHelper;

    protected final Settings settings;
    protected final Random random;

    protected boolean changesMade;

    public Randomizer(RomHandler romHandler, Settings settings, Random random) {
        this.romHandler = romHandler;
        this.rPokeService = romHandler.getRestrictedPokemonService();
        this.typeService = romHandler.getTypeService();
        this.copyUpEvolutionsHelper = new CopyUpEvolutionsHelper<>(romHandler::getSpeciesSet);

        this.settings = settings;
        this.random = random;
    }

    public boolean isChangesMade() {
        return changesMade;
    }
}
