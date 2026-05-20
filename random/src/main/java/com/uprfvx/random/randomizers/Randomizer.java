package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Item;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.ItemMechanicExclusionOptions;
import com.uprfvx.romio.services.ItemMechanicPredicates;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    protected int applyPercentageLevelModifier(int level, int percentageLevelModifier) {
        int modifiedLevel = (int) Math.round(level * (1 + percentageLevelModifier / 100.0));
        return Math.max(1, Math.min(100, modifiedLevel));
    }

    protected List<Item> filterAllowedMechanicItems(Collection<Item> items) {
        List<Item> filtered = new ArrayList<>();
        ItemMechanicExclusionOptions options = itemMechanicExclusionOptions();
        for (Item item : items) {
            if (ItemMechanicPredicates.isItemAllowed(item, options)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    protected Set<Item> filterAllowedMechanicItemSet(Collection<Item> items) {
        return new LinkedHashSet<>(filterAllowedMechanicItems(items));
    }

    private ItemMechanicExclusionOptions itemMechanicExclusionOptions() {
        return new ItemMechanicExclusionOptions(
                settings.isIncludeMegaItems(),
                settings.isIncludeZCrystalItems(),
                settings.isIncludeDynamaxGmaxItems());
    }
}
