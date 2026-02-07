package com.uprfvx.pkrandom.randomizers;

import com.uprfvx.pkrandom.Settings;
import com.uprfvx.pkromio.gamedata.Species;
import com.uprfvx.pkromio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.pkromio.romhandlers.RomHandler;
import com.uprfvx.pkromio.services.RestrictedSpeciesService;
import com.uprfvx.pkromio.services.TypeService;

import java.util.Random;

/**
 * An abstract superclass for all randomizers acting on a {@link RomHandler}.
 */
public abstract class Randomizer {

    protected final RomHandler romHandler;
    protected final RestrictedSpeciesService rSpecService;
    protected final TypeService typeService;
    protected final CopyUpEvolutionsHelper<Species> copyUpEvolutionsHelper;

    protected final Settings settings;
    protected final Random random;

    protected boolean changesMade;

    public Randomizer(RomHandler romHandler, Settings settings, Random random) {
        this.romHandler = romHandler;
        this.rSpecService = romHandler.getRestrictedSpeciesService();
        this.typeService = romHandler.getTypeService();
        this.copyUpEvolutionsHelper = new CopyUpEvolutionsHelper<>(romHandler::getSpeciesSetInclFormes);

        this.settings = settings;
        this.random = random;
    }

    public boolean isChangesMade() {
        return changesMade;
    }
}
