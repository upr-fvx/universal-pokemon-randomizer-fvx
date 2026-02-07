package com.upr_fvx.pkrandom.randomizers;

import com.upr_fvx.pkrandom.Settings;
import com.upr_fvx.pkromio.gamedata.Species;
import com.upr_fvx.pkromio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.upr_fvx.pkromio.romhandlers.RomHandler;
import com.upr_fvx.pkromio.services.RestrictedSpeciesService;
import com.upr_fvx.pkromio.services.TypeService;

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
