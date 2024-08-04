package com.dabomstew.pkrandom.services;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.SpeciesIDs;
import com.dabomstew.pkrandom.gamedata.GenRestrictions;
import com.dabomstew.pkrandom.gamedata.MegaEvolution;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.gamedata.SpeciesSet;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A service for restricted Pokemon. After setting restrictions with {@link #setRestrictions(Settings)},
 * you can access a number of <i>unmodifiable</i> {@link SpeciesSet}s, following those restrictions.<br>
 * When randomizing, you generally want to use these sets,
 * rather than anything provided directly by the {@link RomHandler}, like {@link RomHandler#getSpecies()} or
 * {@link RomHandler#getSpeciesSetInclFormes()}.
 * <br><br>
 * This class also provides {@link #randomSpecies(Random)}, to get a random Pokemon from all allowed ones.
 * To get a random Pokemon from the other sets, use {@link SpeciesSet#getRandomSpecies(Random)}.
 */
public class RestrictedSpeciesService {

    private final RomHandler romHandler;

    private boolean restrictionsSet;

    private SpeciesSet all;
    private SpeciesSet allInclAltFormes;
    private SpeciesSet nonLegendaries;
    private SpeciesSet nonLegendariesInclAltFormes;
    private SpeciesSet legendaries;
    private SpeciesSet legendariesInclAltFormes;
    private SpeciesSet ultraBeasts;
    private SpeciesSet ultraBeastsInclAltFormes;
    private Set<MegaEvolution> megaEvolutions;

    public RestrictedSpeciesService(RomHandler romHandler) {
        this.romHandler = romHandler;
    }

    public SpeciesSet getSpecies(boolean noLegendaries, boolean allowAltFormes, boolean allowCosmeticFormes) {
        SpeciesSet allowedSpecs = new SpeciesSet();
        allowedSpecs.addAll(noLegendaries ? getNonLegendaries(allowAltFormes) : getAll(allowAltFormes));
        if (allowAltFormes && !allowCosmeticFormes) {
            allowedSpecs.removeIf(Species::isCosmeticForme);
        }
        return SpeciesSet.unmodifiable(allowedSpecs);
    }

    /**
     * Returns a random non-alt forme {@link Species}.
     */
    public Species randomSpecies(Random random) {
        return getAll(false).getRandomSpecies(random);
    }

    /**
     * Returns an unmodifiable {@link SpeciesSet} containing all {@link Species} that follow the restrictions.
     */
    public SpeciesSet getAll(boolean includeAltFormes) {
        if (!restrictionsSet) {
            throw new IllegalStateException("Restrictions not set.");
        }
        return includeAltFormes ? allInclAltFormes : all;
    }

    /**
     * Returns an unmodifiable {@link SpeciesSet} containing all non-legendary
     * {@link Species} that follow the restrictions.
     */
    public SpeciesSet getNonLegendaries(boolean includeAltFormes) {
        if (!restrictionsSet) {
            throw new IllegalStateException("Restrictions not set.");
        }
        return includeAltFormes ? nonLegendariesInclAltFormes : nonLegendaries;
    }

    /**
     * Returns an unmodifiable {@link SpeciesSet} containing all legendary {@link Species}
     * that follow the restrictions.
     */
    public SpeciesSet getLegendaries(boolean includeAltFormes) {
        if (!restrictionsSet) {
            throw new IllegalStateException("Restrictions not set.");
        }
        return includeAltFormes ? legendariesInclAltFormes : legendaries;
    }

    /**
     * Returns an unmodifiable {@link SpeciesSet} containing all ultra beasts that follow the restrictions.
     * Does NOT contain the legendary ultra beasts.
     */
    public SpeciesSet getUltrabeasts(boolean includeAltFormes) {
        if (!restrictionsSet) {
            throw new IllegalStateException("Restrictions not set.");
        }
        return includeAltFormes ? ultraBeastsInclAltFormes : ultraBeasts;
    }

    /**
     * Returns an unmodifiable {@link Set} containing all {@link MegaEvolution}s that follow the restrictions.
     */
    public Set<MegaEvolution> getMegaEvolutions() {
        if (!restrictionsSet) {
            throw new IllegalStateException("Restrictions not set.");
        }
        return megaEvolutions;
    }

    public SpeciesSet getAbilityDependentFormes() {
        SpeciesSet abilityDependentFormes = new SpeciesSet();
        for (Species sp : allInclAltFormes) {
            if (sp.getBaseForme() != null) {
                if (sp.getBaseNumber() == SpeciesIDs.castform) {
                    // All alternate Castform formes
                    abilityDependentFormes.add(sp);
                } else if (sp.getBaseNumber() == SpeciesIDs.darmanitan && sp.getFormeNumber() == 1) {
                    // Darmanitan-Z
                    abilityDependentFormes.add(sp);
                } else if (sp.getBaseNumber() == SpeciesIDs.aegislash) {
                    // Aegislash-B
                    abilityDependentFormes.add(sp);
                } else if (sp.getBaseNumber() == SpeciesIDs.wishiwashi) {
                    // Wishiwashi-S
                    abilityDependentFormes.add(sp);
                }
            }
        }
        return abilityDependentFormes;
    }

    public SpeciesSet getBannedFormesForPlayerPokemon() {
        SpeciesSet bannedFormes = new SpeciesSet();
        for (Species pk : allInclAltFormes) {
            if (pk.getBaseForme() != null) {
                if (pk.getBaseNumber() == SpeciesIDs.giratina) {
                    // Giratina-O is banned because it reverts back to Altered Forme if
                    // equipped with any item that isn't the Griseous Orb.
                    bannedFormes.add(pk);
                } else if (pk.getBaseNumber() == SpeciesIDs.shaymin) {
                    // Shaymin-S is banned because it reverts back to its original forme
                    // under a variety of circumstances, and can only be changed back
                    // with the Gracidea.
                    bannedFormes.add(pk);
                }
            }
        }
        return bannedFormes;
    }

    public void setRestrictions(Settings settings) {
        GenRestrictions restrictions = null;
        if (settings != null) {
            restrictions = settings.getCurrentRestrictions();

            // restrictions should already be null if "Limit Pokemon" is disabled, but this is a safeguard
            if (!settings.isLimitPokemon()) {
                restrictions = null;
            }
        }

        restrictionsSet = true;

        if (restrictions != null) {
            allInclAltFormes = SpeciesSet.unmodifiable(allInclAltFormesFromRestrictions(restrictions));
            megaEvolutions = romHandler.getMegaEvolutions().stream()
                    .filter(mevo -> allInclAltFormes.contains(mevo.to))
                    .collect(Collectors.toSet());
            megaEvolutions = Collections.unmodifiableSet(megaEvolutions);
        } else {
            allInclAltFormes = SpeciesSet.unmodifiable(romHandler.getSpeciesSetInclFormes());
            megaEvolutions = Collections.unmodifiableSet(new HashSet<>(romHandler.getMegaEvolutions()));
        }

        nonLegendariesInclAltFormes = SpeciesSet.unmodifiable(allInclAltFormes.filter(pk -> !pk.isLegendary()));
        legendariesInclAltFormes = SpeciesSet.unmodifiable(allInclAltFormes.filter(Species::isLegendary));
        ultraBeastsInclAltFormes = SpeciesSet.unmodifiable(allInclAltFormes.filter(Species::isUltraBeast));
        SpeciesSet altFormes = romHandler.getAltFormes();
        all = SpeciesSet.unmodifiable(allInclAltFormes.filter(pk -> !altFormes.contains(pk)));
        nonLegendaries = SpeciesSet.unmodifiable(nonLegendariesInclAltFormes.filter(pk -> !altFormes.contains(pk)));
        legendaries = SpeciesSet.unmodifiable(legendariesInclAltFormes.filter(pk -> !altFormes.contains(pk)));
        ultraBeasts = SpeciesSet.unmodifiable(ultraBeastsInclAltFormes.filter(pk -> !altFormes.contains(pk)));

        //While this doesn't seem like the ideal place to put this,
        //it's the least bad one I can think of at this time.
        //TODO: place this somewhere more sensible.
        for(Species spec : allInclAltFormes) {
            spec.saveOriginalData();
        }
    }

    private SpeciesSet allInclAltFormesFromRestrictions(GenRestrictions restrictions) {
        SpeciesSet allInclAltFormes = new SpeciesSet();
        SpeciesSet allNonRestricted = romHandler.getSpeciesSetInclFormes();

        if (restrictions.allow_gen1) {
            addFromGen(allInclAltFormes, allNonRestricted, 1);
        }
        if (restrictions.allow_gen2) {
            addFromGen(allInclAltFormes, allNonRestricted, 2);
        }
        if (restrictions.allow_gen3) {
            addFromGen(allInclAltFormes, allNonRestricted, 3);
        }
        if (restrictions.allow_gen4) {
            addFromGen(allInclAltFormes, allNonRestricted, 4);
        }
        if (restrictions.allow_gen5) {
            addFromGen(allInclAltFormes, allNonRestricted, 5);
        }
        if (restrictions.allow_gen6) {
            addFromGen(allInclAltFormes, allNonRestricted, 6);
        }
        if (restrictions.allow_gen7) {
            addFromGen(allInclAltFormes, allNonRestricted, 7);
        }

        // If the user specified it, add all the evolutionary relatives for everything in the mainPokemonList
        if (restrictions.allow_evolutionary_relatives) {
            allInclAltFormes.addFullFamilies(false);
        }

        return allInclAltFormes;
    }

    private static void addFromGen(SpeciesSet allInclAltFormes, SpeciesSet allNonRestricted, int gen) {
        allInclAltFormes.addAll(allNonRestricted.filter(sp -> {
            Species baseForme = sp.getBaseForme() == null ? sp : sp.getBaseForme();
            return baseForme.getGeneration() == gen;
        }));
    }
}
