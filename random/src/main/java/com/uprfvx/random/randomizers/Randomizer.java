package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.random.exceptions.RandomizationException;
import com.uprfvx.romio.gamedata.Move;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * An abstract superclass for all randomizers acting on a {@link RomHandler}.
 */
public abstract class Randomizer {

    protected final RomHandler romHandler;
    protected final RestrictedSpeciesService rSpecService;
    protected final TypeService typeService;
    protected final CopyUpEvolutionsHelper copyUpEvolutionsHelper;

    protected final Settings settings;
    protected final Random random;

    protected boolean changesMade;

    public Randomizer(RomHandler romHandler, Settings settings, Random random) {
        this.romHandler = romHandler;
        this.rSpecService = romHandler.getRestrictedSpeciesService();
        this.typeService = romHandler.getTypeService();
        this.copyUpEvolutionsHelper = new CopyUpEvolutionsHelper(romHandler::getSpeciesSetInclFormes);

        this.settings = settings;
        this.random = random;
    }

    public boolean isChangesMade() {
        return changesMade;
    }

    protected int applyPercentageLevelModifier(int level, int percentageLevelModifier) {
        int modifiedLevel = (int) Math.round(level * (1 + percentageLevelModifier / 100.0));
        return Math.clamp(modifiedLevel, 1, 100);
    }

    /**
     * Detects whether the ROM stores some list of names in ALL CAPS by sampling
     * said list. If a majority of alphabetic characters are uppercase,
     * we assume the ROM uses ALL CAPS.
     */
    protected static boolean detectUpperCaseNames(List<String> names) {
        int upper = 0, lower = 0;
        for (String name : names) {
            if (name == null) continue;
            for (char c : name.toCharArray()) {
                if (Character.isUpperCase(c)) upper++;
                else if (Character.isLowerCase(c)) lower++;
            }
        }
        // If there are essentially no lowercase letters, it's ALL CAPS
        return lower == 0 || (upper > 0 && lower * 10 < upper);
    }

    protected CustomNamesSet getCustomNames() {
        // This is not in line with how most /data resources are loaded for randomization.
        // Am not certain whether this or the other ways are more elegant, might be up
        // for future unification. This works for now though.
        try {
            return CustomNamesSet.readNamesFromFile();
        } catch (IOException e) {
            throw new RandomizationException("Could not read custom names from file.");
        }
    }
}
