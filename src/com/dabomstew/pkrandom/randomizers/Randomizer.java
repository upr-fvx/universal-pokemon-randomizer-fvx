package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.cueh.CopyUpEvolutionsHelper;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import com.dabomstew.pkrandom.services.TypeService;

import java.util.Random;

/**
 * An abstract superclass for all randomizers acting on a {@link RomHandler}.
 */
public abstract class Randomizer {

    protected final RomHandler romHandler;
    protected final RestrictedSpeciesService rSpecService;
    protected final TypeService typeService;
    protected final CopyUpEvolutionsHelper<Species> copyUpEvolutionsHelper;

    protected final SettingsManager settings;
    protected final Random random;

    protected boolean changesMade;

    public Randomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        this.romHandler = romHandler;
        this.rSpecService = romHandler.getRestrictedSpeciesService();
        this.typeService = romHandler.getTypeService();
        this.copyUpEvolutionsHelper = new CopyUpEvolutionsHelper<>(romHandler::getSpeciesSet);

        this.settings = settings;
        this.random = random;
    }

    public boolean isChangesMade() {
        return changesMade;
    }
}
