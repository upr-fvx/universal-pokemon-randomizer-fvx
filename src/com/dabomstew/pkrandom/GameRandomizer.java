package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.log.RandomizationLogger;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.random.SeedPicker;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.romhandlers.Gen1RomHandler;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.updaters.MoveUpdater;
import com.dabomstew.pkrandom.updaters.SpeciesBaseStatUpdater;
import com.dabomstew.pkrandom.updaters.TypeEffectivenessUpdater;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;

/**
 * Coordinates and logs the randomization of a game, via a {@link RomHandler}, and various sub-{@link Randomizer}s.
 * Output varies by seed.
 */
public class GameRandomizer {

    private final RandomSource randomSource = new RandomSource();

    private final Settings settings;
    private final RomHandler romHandler;
    private final boolean saveAsDirectory;

    private final RandomizationLogger logger;

    private final SpeciesBaseStatUpdater speciesBSUpdater;
    private final MoveUpdater moveUpdater;
    private final TypeEffectivenessUpdater typeEffUpdater;

    private final IntroPokemonRandomizer introPokeRandomizer;
    private final SpeciesBaseStatRandomizer speciesBSRandomizer;
    private final SpeciesTypeRandomizer speciesTypeRandomizer;
    private final SpeciesAbilityRandomizer speciesAbilityRandomizer;
    private final EvolutionRandomizer evoRandomizer;
    private final StarterRandomizer starterRandomizer;
    private final StaticPokemonRandomizer staticPokeRandomizer;
    private final TradeRandomizer tradeRandomizer;
    private final MoveDataRandomizer moveDataRandomizer;
    private final SpeciesMovesetRandomizer speciesMovesetRandomizer;
    private final TrainerPokemonRandomizer trainerPokeRandomizer;
    private final TrainerMovesetRandomizer trainerMovesetRandomizer;
    private final TrainerNameRandomizer trainerNameRandomizer;
    private final WildEncounterRandomizer wildEncounterRandomizer;
    private final EncounterHeldItemRandomizer encHeldItemRandomizer;
    private final TMTutorMoveRandomizer tmtMoveRandomizer;
    private final TMHMTutorCompatibilityRandomizer tmhmtCompRandomizer;
    private final ItemRandomizer itemRandomizer;
    private final TypeEffectivenessRandomizer typeEffRandomizer;
    private final PaletteRandomizer paletteRandomizer;
    private final MiscTweakRandomizer miscTweakRandomizer;

    public GameRandomizer(Settings settings, RomHandler romHandler, ResourceBundle bundle, boolean saveAsDirectory) {
        this.settings = settings;
        this.romHandler = romHandler;
        this.saveAsDirectory = saveAsDirectory;

        this.speciesBSUpdater = new SpeciesBaseStatUpdater(romHandler);
        this.moveUpdater = new MoveUpdater(romHandler);
        this.typeEffUpdater = new TypeEffectivenessUpdater(romHandler);

        this.introPokeRandomizer = new IntroPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesBSRandomizer = new SpeciesBaseStatRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesTypeRandomizer = new SpeciesTypeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesAbilityRandomizer = new SpeciesAbilityRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.evoRandomizer = new EvolutionRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.starterRandomizer = new StarterRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.staticPokeRandomizer = new StaticPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.tradeRandomizer = new TradeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.moveDataRandomizer = new MoveDataRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesMovesetRandomizer = new SpeciesMovesetRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerPokeRandomizer = new TrainerPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerMovesetRandomizer = new TrainerMovesetRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerNameRandomizer = new TrainerNameRandomizer(romHandler, settings, randomSource.getCosmetic());
        this.wildEncounterRandomizer = new WildEncounterRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.encHeldItemRandomizer = new EncounterHeldItemRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.tmtMoveRandomizer = new TMTutorMoveRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.tmhmtCompRandomizer = new TMHMTutorCompatibilityRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.itemRandomizer = new ItemRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.typeEffRandomizer = new TypeEffectivenessRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        switch (romHandler.generationOfPokemon()) {
            case 1:
                this.paletteRandomizer = new Gen1PaletteRandomizer(romHandler, settings, randomSource.getCosmetic());
                break;
            case 2:
                this.paletteRandomizer = new Gen2PaletteRandomizer(romHandler, settings, randomSource.getCosmetic());
                break;
            case 3:
            case 4:
            case 5:
                this.paletteRandomizer = new Gen3to5PaletteRandomizer(romHandler, settings, randomSource.getCosmetic());
                break;
            default:
                this.paletteRandomizer = null;
        }
        this.miscTweakRandomizer = new MiscTweakRandomizer(romHandler, settings, randomSource.getNonCosmetic());

        this.logger = new RandomizationLogger(randomSource, settings, romHandler, bundle,
                speciesBSUpdater, moveUpdater, typeEffUpdater,
                introPokeRandomizer, speciesBSRandomizer, speciesTypeRandomizer, speciesAbilityRandomizer,
                evoRandomizer, starterRandomizer, staticPokeRandomizer, tradeRandomizer, moveDataRandomizer,
                speciesMovesetRandomizer, trainerPokeRandomizer, trainerMovesetRandomizer, trainerNameRandomizer,
                wildEncounterRandomizer, encHeldItemRandomizer, tmtMoveRandomizer, tmhmtCompRandomizer, itemRandomizer,
                typeEffRandomizer, paletteRandomizer, miscTweakRandomizer);
    }

    public int randomize(final String filename) {
        return randomize(filename, new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
    }

    public int randomize(final String filename, final PrintStream log) {
        long seed = SeedPicker.pickSeed();
        // long seed = 123456789;    // TESTING
        return randomize(filename, log, seed);
    }

    public int randomize(final String filename, final PrintStream log, long seed) {

        final long startTime = System.currentTimeMillis();
        randomSource.seed(seed);

        setupSpeciesRestrictions();
        applyUpdaters();
        applyRandomizers();
        maybeSetCustomPlayerGraphics();

        romHandler.saveRom(filename, seed, saveAsDirectory);

        logger.logResults(log, startTime);

        return new CheckValueCalculator(romHandler, settings).calculate();
    }

    private void setupSpeciesRestrictions() {
        romHandler.getRestrictedSpeciesService().setRestrictions(settings);
        if (settings.isLimitPokemon()) {
            romHandler.removeEvosForPokemonPool();
        }
    }

    private void applyUpdaters() {
        if (settings.isUpdateTypeEffectiveness()) {
            typeEffUpdater.updateTypeEffectiveness();
        }
        if (settings.isUpdateMoves()) {
            moveUpdater.updateMoves(settings.getUpdateMovesToGeneration());
        }
        if (settings.isUpdateBaseStats()) {
            speciesBSUpdater.updateSpeciesStats(settings.getUpdateBaseStatsToGeneration());
        }
    }

    private void maybeSetCustomPlayerGraphics() {
        // this setting/feature sticks out for being atypical,
        // versus the rest of the randomizer..... is this the right place for it to be?
        if (settings.getCustomPlayerGraphicsMod() == Settings.CustomPlayerGraphicsMod.RANDOM) {
            romHandler.setCustomPlayerGraphics(settings.getCustomPlayerGraphics(),
                    settings.getCustomPlayerGraphicsCharacterMod());
        }
    }


    private void applyRandomizers() {

        maybeRandomizeTypeEffectiveness();

        maybeRandomizeMoveData();

        maybeApplyMiscTweaks();

        maybeStandardizeEXPCurves();

        maybeRandomizeSpeciesTypes();

        maybeRandomizeWildHeldItems();

        // Applied after type to pick new evos based on new types.
        maybeRandomizeEvolutions();

        maybeRandomizeSpeciesBaseStats();
        maybeRandomizeSpeciesAbilities();

        maybeApplyEvolutionImprovements();

        // Applied after type to update the strings correctly based on new types
        maybeRandomizeStarters();

        maybeRandomizeMovesets();

        maybeRandomizeTMMoves();
        maybeRandomizeTMHMCompatibility();

        maybeRandomizeMoveTutorMoves();
        maybeRandomizeMoveTutorCompatibility();

        // Applied before trainer randomization so "trainers use local pokémon"
        // may be based on new "local pokémon".
        maybeRandomizeWildPokemon();

        maybeRandomizeTrainerPokemon();
        maybeRandomizeTrainerMovesets();
        maybeFixTrainerZCrystals();

        maybeRandomizeTrainerHeldItems();
        maybeRandomizeTrainerNames();

        // Apply metronome only mode now that trainers have been dealt with
        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            speciesMovesetRandomizer.metronomeOnlyMode();
        }

        maybeRandomizeStaticPokemon();
        maybeRandomizeTotemPokemon();

        maybeRandomizeInGameTrades();

        maybeRandomizeFieldItems();
        maybeRandomizeShops();
        maybeRandomizePickupItems();

        maybeRandomizePokemonPalettes();

        maybeRandomizeIntroPokemon();
    }

    private void maybeRandomizeTypeEffectiveness() {
        if (settings.getTypeEffectivenessMod() != Settings.TypeEffectivenessMod.UNCHANGED) {
            switch (settings.getTypeEffectivenessMod()) {
                case RANDOM:
                    typeEffRandomizer.randomizeTypeEffectiveness(false);
                    break;
                case RANDOM_BALANCED:
                    typeEffRandomizer.randomizeTypeEffectiveness(true);
                    break;
                case KEEP_IDENTITIES:
                    typeEffRandomizer.randomizeTypeEffectivenessKeepIdentities();
                    break;
                case INVERSE:
                    typeEffRandomizer.invertTypeEffectiveness(settings.isInverseTypesRandomImmunities());
            }
        }
    }

    private void maybeRandomizeMoveData() {
        if (settings.isRandomizeMovePowers()) {
            moveDataRandomizer.randomizeMovePowers();
        }

        if (settings.isRandomizeMoveAccuracies()) {
            moveDataRandomizer.randomizeMoveAccuracies();
        }

        if (settings.isRandomizeMovePPs()) {
            moveDataRandomizer.randomizeMovePPs();
        }

        if (settings.isRandomizeMoveTypes()) {
            moveDataRandomizer.randomizeMoveTypes();
        }

        if (settings.isRandomizeMoveCategory() && romHandler.hasPhysicalSpecialSplit()) {
            moveDataRandomizer.randomizeMoveCategory();
        }
    }

    private void maybeApplyMiscTweaks() {
        if (settings.getCurrentMiscTweaks() != MiscTweak.NO_MISC_TWEAKS) {
            miscTweakRandomizer.applyMiscTweaks();
        }
    }

    private void maybeStandardizeEXPCurves() {
        if (settings.isStandardizeEXPCurves()) {
            speciesBSRandomizer.standardizeEXPCurves();
        }
    }

    private void maybeRandomizeSpeciesTypes() {
        if (settings.getSpeciesTypesMod() != Settings.SpeciesTypesMod.UNCHANGED) {
            speciesTypeRandomizer.randomizeSpeciesTypes();
        }
    }

    private void maybeRandomizeWildHeldItems() {
        if (settings.isRandomizeWildPokemonHeldItems()) {
            encHeldItemRandomizer.randomizeWildHeldItems();
        }
    }

    private void maybeRandomizeEvolutions() {
        if (settings.getEvolutionsMod() != Settings.EvolutionsMod.UNCHANGED) {
            evoRandomizer.randomizeEvolutions();
        }
    }

    private void maybeRandomizeSpeciesBaseStats() {
        switch (settings.getBaseStatisticsMod()) {
            case SHUFFLE:
                speciesBSRandomizer.shuffleSpeciesStats();
                break;
            case RANDOM:
                speciesBSRandomizer.randomizeSpeciesStats();
        }
    }

    private void maybeRandomizeSpeciesAbilities() {
        if (settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE) {
            speciesAbilityRandomizer.randomizeAbilities();
        }
    }

    private void maybeApplyEvolutionImprovements() {
        // Trade evolutions (etc.) removal
        if (settings.isChangeImpossibleEvolutions()) {
            romHandler.removeImpossibleEvolutions(settings);
        }

        // Easier evolutions
        if (settings.isMakeEvolutionsEasier()) {
            romHandler.condenseLevelEvolutions(40, 30);
            romHandler.makeEvolutionsEasier(settings);
        }

        // Remove time-based evolutions
        if (settings.isRemoveTimeBasedEvolutions()) {
            romHandler.removeTimeBasedEvolutions();
        }
    }

    private void maybeRandomizeStarters() {
        if (settings.getStartersMod() != Settings.StartersMod.UNCHANGED) {
            starterRandomizer.randomizeStarters();
        }
        if (settings.isRandomizeStartersHeldItems() && !(romHandler instanceof Gen1RomHandler)) {
            starterRandomizer.randomizeStarterHeldItems();
        }
    }

    private void maybeRandomizeMovesets() {
        // Movesets
        // 1. Randomize movesets
        // 2. Reorder moves by damage
        // Note: "Metronome only" is handled after trainers instead

        if (settings.getMovesetsMod() != Settings.MovesetsMod.UNCHANGED &&
                settings.getMovesetsMod() != Settings.MovesetsMod.METRONOME_ONLY) {
            speciesMovesetRandomizer.randomizeMovesLearnt();
            speciesMovesetRandomizer.randomizeEggMoves();
        }

        if (settings.isReorderDamagingMoves()) {
            speciesMovesetRandomizer.orderDamagingMovesByDamage();
        }
    }

    private void maybeRandomizeTMMoves() {
        if (!(settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY)
                && settings.getTmsMod() == Settings.TMsMod.RANDOM) {
            tmtMoveRandomizer.randomizeTMMoves();
        }
    }

    private void maybeRandomizeTMHMCompatibility() {
        // TM/HM compatibility
        // 1. Randomize TM/HM compatibility
        // 2. Ensure levelup move sanity
        // 3. Follow evolutions
        // 4. Full HM compatibility
        // 5. Copy to cosmetic forms

        switch (settings.getTmsHmsCompatibilityMod()) {
            case COMPLETELY_RANDOM:
            case RANDOM_PREFER_TYPE:
                tmhmtCompRandomizer.randomizeTMHMCompatibility();
                break;
            case FULL:
                tmhmtCompRandomizer.fullTMHMCompatibility();
        }

        if (settings.isTmLevelUpMoveSanity()) {
            tmhmtCompRandomizer.ensureTMCompatSanity();
            if (settings.isTmsFollowEvolutions()) {
                tmhmtCompRandomizer.ensureTMEvolutionSanity();
            }
        }

        if (settings.isFullHMCompat()) {
            tmhmtCompRandomizer.fullHMCompatibility();
        }

        // Copy TM/HM compatibility to cosmetic formes if it was changed at all
        if (tmhmtCompRandomizer.isTMHMChangesMade()) {
            tmhmtCompRandomizer.copyTMCompatibilityToCosmeticFormes();
        }
    }

    private void maybeRandomizeMoveTutorMoves() {
        if (romHandler.hasMoveTutors()) {
            if (!(settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY)
                    && settings.getMoveTutorMovesMod() == Settings.MoveTutorMovesMod.RANDOM) {
                tmtMoveRandomizer.randomizeMoveTutorMoves();
            }
        }
    }

    private void maybeRandomizeMoveTutorCompatibility() {
        if (romHandler.hasMoveTutors()) {
            // Move Tutor Compatibility
            // 1. Randomize MT compatibility
            // 2. Ensure levelup move sanity
            // 3. Follow evolutions
            // 4. Copy to cosmetic forms

            switch (settings.getMoveTutorsCompatibilityMod()) {
                case COMPLETELY_RANDOM:
                case RANDOM_PREFER_TYPE:
                    tmhmtCompRandomizer.randomizeMoveTutorCompatibility();
                    break;
                case FULL:
                    tmhmtCompRandomizer.fullMoveTutorCompatibility();
            }

            if (settings.isTutorLevelUpMoveSanity()) {
                tmhmtCompRandomizer.ensureMoveTutorCompatSanity();
                if (settings.isTutorFollowEvolutions()) {
                    tmhmtCompRandomizer.ensureMoveTutorEvolutionSanity();
                }
            }

            // Copy move tutor compatibility to cosmetic formes if it was changed at all
            if (tmhmtCompRandomizer.isTutorChangesMade()) {
                tmhmtCompRandomizer.copyMoveTutorCompatibilityToCosmeticFormes();
            }
        }
    }

    private void maybeRandomizeTrainerPokemon() {
        // Trainer Pokemon
        // 1. Add extra Trainer Pokemon
        // 2. Set trainers to be double battles and add extra Pokemon if necessary
        // 3. Modify levels
        // 4. Modify rivals to carry starters
        // 5. Randomize Trainer Pokemon (or force fully evolved if not randomizing)

        if (settings.getAdditionalRegularTrainerPokemon() > 0
                || settings.getAdditionalImportantTrainerPokemon() > 0
                || settings.getAdditionalBossTrainerPokemon() > 0) {
            trainerPokeRandomizer.addTrainerPokemon();
        }

        if (settings.isDoubleBattleMode()) {
            trainerPokeRandomizer.setDoubleBattleMode();
        }

        if (settings.isTrainersLevelModified()) {
            trainerPokeRandomizer.applyTrainerLevelModifier();
        }

        if ((settings.getTrainersMod() != Settings.TrainersMod.UNCHANGED
                || settings.getStartersMod() != Settings.StartersMod.UNCHANGED)
                && settings.isRivalCarriesStarterThroughout()) {
            trainerPokeRandomizer.makeRivalCarryStarter();
        }

        if (settings.getTrainersMod() != Settings.TrainersMod.UNCHANGED) {
            trainerPokeRandomizer.randomizeTrainerPokes();
        } else if (settings.isTrainersForceFullyEvolved()) {
            trainerPokeRandomizer.forceFullyEvolvedTrainerPokes();
        }
    }

    private void maybeRandomizeTrainerMovesets() {
        if (settings.isBetterTrainerMovesets()) {
            trainerMovesetRandomizer.randomizeTrainerMovesets();
        }
    }

    private void maybeFixTrainerZCrystals() {
        // if earlier randomization could have led to unusable Z-crystals, fix them to something usable here
        if (speciesMovesetRandomizer.isChangesMade() || trainerPokeRandomizer.isChangesMade()
                || trainerMovesetRandomizer.isChangesMade()) {
            trainerPokeRandomizer.randomUsableZCrystals();
        }
    }

    private void maybeRandomizeTrainerHeldItems() {
        if (settings.isRandomizeHeldItemsForBossTrainerPokemon()
                || settings.isRandomizeHeldItemsForImportantTrainerPokemon()
                || settings.isRandomizeHeldItemsForRegularTrainerPokemon()) {
            trainerPokeRandomizer.randomizeTrainerHeldItems();
        }
    }

    private void maybeRandomizeTrainerNames() {
        if (romHandler.canChangeTrainerText()) {
            if (settings.isRandomizeTrainerClassNames()) {
                trainerNameRandomizer.randomizeTrainerClassNames();
            }

            if (settings.isRandomizeTrainerNames()) {
                trainerNameRandomizer.randomizeTrainerNames();
            }
        }
    }

    private void maybeRandomizeStaticPokemon() {
        if (romHandler.canChangeStaticPokemon()) {
            if (settings.getStaticPokemonMod() != Settings.StaticPokemonMod.UNCHANGED) { // Legendary for L
                staticPokeRandomizer.randomizeStaticPokemon();
            } else if (settings.isStaticLevelModified()) {
                staticPokeRandomizer.onlyChangeStaticLevels();
            }
        }
    }

    private void maybeRandomizeTotemPokemon() {
        if (romHandler.hasTotemPokemon()) {
            if (settings.getTotemPokemonMod() != Settings.TotemPokemonMod.UNCHANGED ||
                    settings.getAllyPokemonMod() != Settings.AllyPokemonMod.UNCHANGED ||
                    settings.getAuraMod() != Settings.AuraMod.UNCHANGED ||
                    settings.isRandomizeTotemHeldItems() ||
                    settings.isTotemLevelsModified()) {

                staticPokeRandomizer.randomizeTotemPokemon();
            }
        }
    }

    private void maybeRandomizeWildPokemon() {
        if (settings.isUseMinimumCatchRate()) {
            wildEncounterRandomizer.changeCatchRates();
        }

        if (settings.isRandomizeWildPokemon() || settings.isWildLevelsModified()) {
            wildEncounterRandomizer.randomizeEncounters();
        }
    }

    private void maybeRandomizeInGameTrades() {
        switch (settings.getInGameTradesMod()) {
            case RANDOMIZE_GIVEN:
            case RANDOMIZE_GIVEN_AND_REQUESTED:
                tradeRandomizer.randomizeIngameTrades();
        }
    }

    private void maybeRandomizeFieldItems() {
        switch (settings.getFieldItemsMod()) {
            case SHUFFLE:
                itemRandomizer.shuffleFieldItems();
                break;
            case RANDOM:
            case RANDOM_EVEN:
                itemRandomizer.randomizeFieldItems();
        }
    }

    private void maybeRandomizeShops() {
        switch (settings.getShopItemsMod()) {
            case SHUFFLE:
                itemRandomizer.shuffleShopItems();
                break;
            case RANDOM:
                itemRandomizer.randomizeShopItems();
        }
    }

    private void maybeRandomizePickupItems() {
        if (settings.getPickupItemsMod() == Settings.PickupItemsMod.RANDOM) {
            itemRandomizer.randomizePickupItems();
        }
    }

    private void maybeRandomizePokemonPalettes() {
        if (settings.getPokemonPalettesMod() == Settings.PokemonPalettesMod.RANDOM) {
            paletteRandomizer.randomizePokemonPalettes();
        }
    }

    private void maybeRandomizeIntroPokemon() {
        // Note: this is the only randomization that applies even if no setting is checked.
        // Essentially, it works as confirmation that the Randomizer was applied at all.
        if (romHandler.canSetIntroPokemon()) {
            introPokeRandomizer.randomizeIntroPokemon();
        }
    }
}