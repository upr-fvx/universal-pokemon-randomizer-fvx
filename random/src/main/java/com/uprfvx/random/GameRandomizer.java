package com.uprfvx.random;

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

import com.uprfvx.random.log.RandomizationLogger;
import com.uprfvx.random.random.RandomSource;
import com.uprfvx.random.random.SeedPicker;
import com.uprfvx.random.randomizers.*;
import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.random.updaters.MoveUpdater;
import com.uprfvx.random.updaters.SpeciesBaseStatUpdater;
import com.uprfvx.random.updaters.TypeEffectivenessUpdater;
import com.uprfvx.random.updaters.Updater;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ResourceBundle;

/**
 * Coordinates the randomization of a game, via a {@link RomHandler}, and various sub-{@link Randomizer}s,
 * and {@link Updater}s.<br>
 * Also passes the results to a {@link RandomizationLogger} and a {@link CheckValueCalculator} for
 * logging/check value calculation.
 * <br><br>
 * Output varies by seed.
 */
public class GameRandomizer {

    public static class Results {

        private Exception e;
        private Exception logE;
        private int checkValue;

        private Results() {}

        public boolean wasSaveSuccessful() {
            return e == null;
        }

        public Exception getException() {
            if (wasSaveSuccessful()) {
                throw new IllegalStateException("Randomization successful; no Exception to be gotten.");
            }
            return e;
        }

        public boolean wasLogSuccessful() {
            return logE == null;
        }

        public Exception getLogException() {
            if (wasLogSuccessful()) {
                throw new IllegalStateException("Logging successful; no Exception to be gotten.");
            }
            return logE;
        }

        public int getCheckValue() {
            return checkValue;
        }
    }

    private final RandomSource randomSource = new RandomSource();

    private final SettingsManager settings;
    private final CustomPlayerGraphics customPlayerGraphics;
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
    private final MoveNameRandomizer moveNameRandomizer;
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

    public GameRandomizer(SettingsManager settings, CustomPlayerGraphics customPlayerGraphics, RomHandler romHandler,
                          ResourceBundle bundle, boolean saveAsDirectory) {
        this.settings = settings;
        this.customPlayerGraphics = customPlayerGraphics;
        this.romHandler = romHandler;
        this.saveAsDirectory = saveAsDirectory;

        this.speciesBSUpdater = new SpeciesBaseStatUpdater(romHandler);
        this.moveUpdater = new MoveUpdater(romHandler);
        this.typeEffUpdater = new TypeEffectivenessUpdater(romHandler);

        this.introPokeRandomizer = new IntroPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesBSRandomizer = romHandler.generationOfPokemon() == 1 ?
                new Gen1SpeciesBaseStatRandomizer(romHandler, settings, randomSource.getNonCosmetic()) :
                new SpeciesBaseStatRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesTypeRandomizer = new SpeciesTypeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.speciesAbilityRandomizer = new SpeciesAbilityRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.evoRandomizer = new EvolutionRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.starterRandomizer = new StarterRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.staticPokeRandomizer = new StaticPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.tradeRandomizer = new TradeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.moveDataRandomizer = new MoveDataRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.moveNameRandomizer = new MoveNameRandomizer(romHandler, settings, randomSource.getCosmetic());
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
                evoRandomizer, starterRandomizer, staticPokeRandomizer, tradeRandomizer, moveDataRandomizer, moveNameRandomizer,
                speciesMovesetRandomizer, trainerPokeRandomizer, trainerMovesetRandomizer, trainerNameRandomizer,
                wildEncounterRandomizer, encHeldItemRandomizer, tmtMoveRandomizer, tmhmtCompRandomizer, itemRandomizer,
                typeEffRandomizer, paletteRandomizer, miscTweakRandomizer);
    }

    public Results randomize(final String filename) {
        return randomize(filename, new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
    }

    public Results randomize(final String filename, final PrintStream log) {
        long seed = SeedPicker.pickSeed();
        // long seed = 123456789;    // TESTING
        return randomize(filename, log, seed);
    }

    public Results randomize(final String filename, final PrintStream log, long seed) {
        Results results = new Results();
        try {
            final long startTime = System.currentTimeMillis();
            randomSource.seed(seed);

            setupSpeciesRestrictions();
            applyUpdaters();
            applyRandomizers();
            maybeSetCustomPlayerGraphics();

            results.checkValue = new CheckValueCalculator(romHandler, settings).calculate();
            if (romHandler.shouldWriteCheckValue()) {
                romHandler.writeCheckValue(results.checkValue);
            }

            boolean couldSave = romHandler.saveRom(filename, seed, saveAsDirectory);

            try {
                logger.logResults(log, startTime);
            } catch (Exception e) {
                results.logE = e;
            }

            if (!couldSave) {
                results.e = new IOException("Could not save ROM, reason unknown.");
            }
        } catch (Exception e) {
            results.e = e;
        }

        return results;
    }

    private void setupSpeciesRestrictions() {
        GenRestrictions restrictions = new GenRestrictions();

        restrictions.setGenAllowed(1, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_1));
        restrictions.setGenAllowed(2, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_2));
        restrictions.setGenAllowed(3, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_3));
        restrictions.setGenAllowed(4, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_4));
        restrictions.setGenAllowed(5, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_5));
        restrictions.setGenAllowed(6, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_6));
        restrictions.setGenAllowed(7, !(boolean) settings.get(Settings.Name.LIMIT_BAN_GENERATION_7));
        restrictions.setAllowEvolutionaryRelatives(settings.get(Settings.Name.LIMIT_ALLOW_RELATIVES));

        romHandler.getRestrictedSpeciesService().setRestrictions(restrictions);
        romHandler.removeEvosForPokemonPool();
    }

    private void applyUpdaters() {
        if (settings.get(Settings.Name.UPDATE_TYPE_EFFECTIVENESS)) {
            typeEffUpdater.updateTypeEffectiveness();
        }
        if (settings.get(Settings.Name.UPDATE_MOVES)) {
            int generation = settings.get(Settings.Name.UPDATE_MOVES_TO_GENERATION);
            moveUpdater.updateMoves(generation);
        }
        if (settings.get(Settings.Name.UPDATE_SPECIES_BASE_STATS)) {
            int generation = settings.get(Settings.Name.SPECIES_UPDATE_BASE_STATS_TO_GENERATION);
            speciesBSUpdater.updateSpeciesStats(generation);
        }
    }

    private void maybeSetCustomPlayerGraphics() {
        // This setting/feature sticks out for being atypical,
        // versus the rest of the randomizer.....
        // But if we consider the GameRandomizer to be
        // "the thing that does all the changes to the ROM, chosen through the UI",
        // then it makes sense that this should be here.
        if (customPlayerGraphics != null) {
            romHandler.setCustomPlayerGraphics(customPlayerGraphics);
        }
    }


    private void applyRandomizers() {

        maybeRandomizeTypeEffectiveness();

        maybeRandomizeMoveData();

        maybeApplyMiscTweaks();

        maybeStandardizeEXPCurves();

        // Applied before anything that can be carried up evolutions, so the new evos are used for that.
        maybeRandomizeEvolutions();

        maybeRandomizeSpeciesBaseStatTotals();

        // Applied after both evo and BST randomization, so the right evos/BSTs are used.
        if (settings.get(Settings.Name.SPECIES_EVOLUTIONS_ADJUST_LEVELS_FOR_STRENGTH)) {
            evoRandomizer.adjustEvolutionLevels();
        }

        maybeRandomizeSpeciesTypes();
        maybeRandomizeWildHeldItems();
        maybeRandomizeSpeciesBaseStats();
        maybeRandomizeSpeciesAbilities();

        maybeApplyEvolutionImprovements();

        // Applied after species types both some settings and the in-game strings should depend on the new types.
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
        maybeApplyMetronomeMode();

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
        Settings.TypeEffectivenessMod mod = settings.get(Settings.Name.RANDOMIZE_TYPE_EFFECTIVENESS);
        switch (mod) {
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
                boolean addImmunities = settings.get(Settings.Name.TYPE_INVERSE_ADD_RANDOM_IMMUNITIES);
                typeEffRandomizer.invertTypeEffectiveness(addImmunities);
        }
    }

    private void maybeRandomizeMoveData() {
        if (settings.get(Settings.Name.MOVES_RANDOMIZE_POWER)) {
            moveDataRandomizer.randomizeMovePowers();
        }

        if (settings.get(Settings.Name.MOVES_RANDOMIZE_ACCURACY)) {
            moveDataRandomizer.randomizeMoveAccuracies();
        }

        if (settings.get(Settings.Name.MOVES_RANDOMIZE_PP)) {
            moveDataRandomizer.randomizeMovePPs();
        }

        if (settings.get(Settings.Name.MOVES_RANDOMIZE_TYPE)) {
            moveDataRandomizer.randomizeMoveTypes();
        }

        if (settings.get(Settings.Name.MOVES_RANDOMIZE_NAME)) {
            moveNameRandomizer.randomizeMoveNames();
        }

        if ((boolean) settings.get(Settings.Name.MOVES_RANDOMIZE_CATEGORY)
                && romHandler.hasPhysicalSpecialSplit()) {
            moveDataRandomizer.randomizeMoveCategory();
        }
    }

    private void maybeApplyMiscTweaks() {
        miscTweakRandomizer.applyMiscTweaks();
    }

    private void maybeStandardizeEXPCurves() {
        if (settings.get(Settings.Name.STANDARDIZE_SPECIES_EXP_CURVES)) {
            speciesBSRandomizer.standardizeEXPCurves();
        }
    }

    private void maybeRandomizeSpeciesTypes() {
        Settings.SpeciesTypesMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_TYPES);
        if (mod != Settings.SpeciesTypesMod.UNCHANGED) {
            speciesTypeRandomizer.randomizeSpeciesTypes();
        }
    }

    private void maybeRandomizeWildHeldItems() {
        if (settings.get(Settings.Name.WILD_RANDOMIZE_HELD_ITEMS)) {
            encHeldItemRandomizer.randomizeWildHeldItems();
        }
    }

    private void maybeRandomizeEvolutions() {
        Settings.EvolutionsMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_EVOLUTIONS);
        if (mod != Settings.EvolutionsMod.UNCHANGED) {
            evoRandomizer.randomizeEvolutions();
        }
    }

    private void maybeRandomizeSpeciesBaseStatTotals() {
        Settings.BSTMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_BASE_STAT_TOTALS);
        if (mod != Settings.BSTMod.UNCHANGED) {
            speciesBSRandomizer.randomizeBSTs();
        }
    }

    private void maybeRandomizeSpeciesBaseStats() {
        Settings.BaseStatDistributionsMod mod =
                settings.get(Settings.Name.RANDOMIZE_SPECIES_BASE_STAT_DISTRIBUTIONS);
        switch (mod) {
            case SHUFFLE:
                speciesBSRandomizer.shuffleSpeciesStats();
                break;
            case RANDOM:
                speciesBSRandomizer.randomizeSpeciesStats();
        }
    }

    private void maybeRandomizeSpeciesAbilities() {
        Settings.AbilitiesMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_ABILITIES);
        if (mod == Settings.AbilitiesMod.RANDOMIZE) {
            speciesAbilityRandomizer.randomizeAbilities();
        }
    }

    private void maybeApplyEvolutionImprovements() {
        boolean useEstimatedLevels = settings.get(Settings.Name.SPECIES_EVOLUTIONS_CHANGES_USE_ESTIMATED_LEVELS);

        // Trade evolutions (etc.) removal
        if (settings.get(Settings.Name.SPECIES_EVOLUTIONS_MAKE_POSSIBLE)) {
            Settings.MovesetsMod movesetsMod = settings.get(Settings.Name.RANDOMIZE_SPECIES_MOVESETS);
            boolean changeMoveEvos = movesetsMod != Settings.MovesetsMod.UNCHANGED;
            romHandler.removeImpossibleEvolutions(changeMoveEvos, useEstimatedLevels);
        }

        // Easier evolutions
        if (settings.get(Settings.Name.SPECIES_EVOLUTIONS_MAKE_EASIER)) {
            int easierLevel = settings.get(Settings.Name.SPECIES_EVOLUTIONS_EASIER_SCALING_LEVEL);
            romHandler.condenseLevelEvolutions(easierLevel);

            boolean wildsRandomizer = settings.get(Settings.Name.RANDOMIZE_WILD_ENCOUNTERS);
            romHandler.makeEvolutionsEasier(wildsRandomizer, useEstimatedLevels);
        }

        // Remove time-based evolutions
        if (settings.get(Settings.Name.SPECIES_EVOLUTIONS_REMOVE_TIME_BASED)) {
            romHandler.removeTimeBasedEvolutions();
        }
    }

    private void maybeRandomizeStarters() {
        Settings.StartersMod mod = settings.get(Settings.Name.RANDOMIZE_STARTERS);
        if (mod != Settings.StartersMod.UNCHANGED) {
            starterRandomizer.randomizeStarters();
        }
        if (settings.get(Settings.Name.STARTERS_RANDOMIZE_HELD_ITEMS)) {
            starterRandomizer.randomizeStarterHeldItems();
        }
    }

    private void maybeRandomizeMovesets() {
        // Movesets
        // 1. Randomize movesets
        // 2. Reorder moves by damage
        // Note: "Metronome only" is handled after trainers instead

        Settings.MovesetsMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_MOVESETS);
        if (mod != Settings.MovesetsMod.UNCHANGED
                && mod != Settings.MovesetsMod.METRONOME_ONLY) {
            speciesMovesetRandomizer.randomizeMovesLearnt();
            speciesMovesetRandomizer.randomizeEggMoves();
        }

        if (settings.get(Settings.Name.MOVESETS_ORDER_BY_DAMAGE)) {
            speciesMovesetRandomizer.orderDamagingMovesByDamage();
        }
    }

    private void maybeRandomizeTMMoves() {
        Settings.TMMovesMod mod = settings.get(Settings.Name.RANDOMIZE_TM_MOVES);
        if (mod == Settings.TMMovesMod.RANDOM) {
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

        Settings.TMsHMsCompatibilityMod mod = settings.get(Settings.Name.RANDOMIZE_TM_AND_HM_COMPATABILITY);
        switch (mod) {
            case COMPLETELY_RANDOM:
            case RANDOM_PREFER_TYPE:
                tmhmtCompRandomizer.randomizeTMHMCompatibility();
                break;
            case FULL:
                tmhmtCompRandomizer.fullTMHMCompatibility();
        }

        if (settings.get(Settings.Name.TM_COMPATABILITY_LEVEL_UP_SANITY)) {
            tmhmtCompRandomizer.ensureTMCompatSanity();
            if (settings.get(Settings.Name.TM_COMPATABILITY_FOLLOW_EVOLUTIONS)) {
                tmhmtCompRandomizer.ensureTMEvolutionSanity();
            }
        }

        if (settings.get(Settings.Name.TMS_FULL_HM_COMPATABILITY)) {
            tmhmtCompRandomizer.fullHMCompatibility();
        }

        // Copy TM/HM compatibility to cosmetic formes if it was changed at all
        if (tmhmtCompRandomizer.isTMHMChangesMade()) {
            tmhmtCompRandomizer.copyTMCompatibilityToCosmeticFormes();
        }
    }

    private void maybeRandomizeMoveTutorMoves() {
        if (romHandler.hasMoveTutors()) {
            Settings.MoveTutorMovesMod mod = settings.get(Settings.Name.RANDOMIZE_TUTOR_MOVES);
            if (mod == Settings.MoveTutorMovesMod.RANDOM) {
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
            Settings.MoveTutorsCompatibilityMod mod = settings.get(Settings.Name.RANDOMIZE_TUTOR_COMPATABILITY);
            switch (mod) {
                case COMPLETELY_RANDOM:
                case RANDOM_PREFER_TYPE:
                    tmhmtCompRandomizer.randomizeMoveTutorCompatibility();
                    break;
                case FULL:
                    tmhmtCompRandomizer.fullMoveTutorCompatibility();
            }

            if (settings.get(Settings.Name.TUTOR_COMPATABILITY_LEVEL_UP_SANITY)) {
                tmhmtCompRandomizer.ensureMoveTutorCompatSanity();
                if (settings.get(Settings.Name.TUTOR_COMPATABILITY_FOLLOW_EVOLUTIONS)) {
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
        // 1. Modify levels first to get larger level variety if additional Pokemon are added in the next step
        // 2. Add extra Trainer Pokemon with level between lowest and highest original trainer Pokemon
        // 3. Set trainers to be double battles and add extra Pokemon if necessary
        // 4. Modify rivals to carry starters
        // 5. Randomize Trainer Pokemon (or evolve if not randomizing, i.e., UNCHANGED and no additional Pkmn)

        if (!settings.isDefault(Settings.Name.TRAINERS_LEVEL_MODIFIER_PERCENT)) {
            trainerPokeRandomizer.applyTrainerLevelModifier();
        }

        boolean additionalPokemonAdded = !settings.isDefault(Settings.Name.TRAINERS_BOSSES_ADDITIONAL_POKEMON_COUNT)
                || !settings.isDefault(Settings.Name.TRAINERS_IMPORTANT_ADDITIONAL_POKEMON_COUNT)
                || !settings.isDefault(Settings.Name.TRAINERS_REGULAR_ADDITIONAL_POKEMON_COUNT);
        if (additionalPokemonAdded) {
            trainerPokeRandomizer.addTrainerPokemon();
        }

        BattleStyle.Modification battleStyleMod = settings.get(Settings.Name.TRAINERS_RANDOMIZE_BATTLE_STYLE);
        if (battleStyleMod != BattleStyle.Modification.UNCHANGED) {
            trainerPokeRandomizer.modifyBattleStyle();
        }

        Settings.TrainersMod trainersMod = settings.get(Settings.Name.RANDOMIZE_TRAINER_POKEMON);
        Settings.StartersMod startersMod = settings.get(Settings.Name.RANDOMIZE_STARTERS);
        if ((trainersMod != Settings.TrainersMod.UNCHANGED
                || startersMod != Settings.StartersMod.UNCHANGED)
                && (boolean) settings.get(Settings.Name.TRAINERS_RIVAL_CARRIES_STARTER)) {
            trainerPokeRandomizer.makeRivalCarryStarter();
        }

        if(trainersMod != Settings.TrainersMod.UNCHANGED) {
            trainerPokeRandomizer.randomizeTrainerPokes();
        } else if (settings.get(Settings.Name.TRAINERS_EVOLVE_POKEMON)) {
            trainerPokeRandomizer.evolveTrainerPokemonAsFarAsLegal();
        }
    }

    private void maybeRandomizeTrainerMovesets() {
        if ((boolean) settings.get(Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_BOSSES)
                || (boolean) settings.get(Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_IMPORTANT)
                || (boolean) settings.get(Settings.Name.TRAINERS_BETTER_MOVESETS_FOR_REGULAR)) {
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
        if ((boolean) settings.get(Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_BOSSES)
                || (boolean) settings.get(Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_IMPORTANT)
                || (boolean) settings.get(Settings.Name.TRAINERS_ADD_HELD_ITEMS_TO_REGULAR)) {
            trainerPokeRandomizer.randomizeTrainerHeldItems();
        }
    }

    private void maybeRandomizeTrainerNames() {
        if (romHandler.canChangeTrainerText()) {
            if (settings.get(Settings.Name.COSMETIC_RANDOMIZE_TRAINER_CLASS_NAMES)) {
                trainerNameRandomizer.randomizeTrainerClassNames();
            }

            if (settings.get(Settings.Name.COSMETIC_RANDOMIZE_TRAINER_NAMES)) {
                trainerNameRandomizer.randomizeTrainerNames();
            }
        }
    }

    private void maybeApplyMetronomeMode() {
        Settings.MovesetsMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_MOVESETS);
        if (mod == Settings.MovesetsMod.METRONOME_ONLY) {
            speciesMovesetRandomizer.metronomeOnlyMode();
        }
    }

    private void maybeRandomizeStaticPokemon() {
        if (romHandler.canChangeStaticPokemon()) {
            Settings.StaticPokemonMod mod = settings.get(Settings.Name.RANDOMIZE_STATIC_ENCOUNTERS);
            if (mod != Settings.StaticPokemonMod.UNCHANGED) {
                staticPokeRandomizer.randomizeStaticPokemon();
            } else if (!settings.isDefault(Settings.Name.STATICS_LEVEL_MODIFIER_PERCENT)) {
                staticPokeRandomizer.onlyChangeStaticLevels();
            }
        }
    }

    private void maybeRandomizeTotemPokemon() {
        if (romHandler.hasTotemPokemon()) {
            Settings.TotemPokemonMod totemMod = settings.get(Settings.Name.RANDOMIZE_TOTEM_POKEMON);
            Settings.AllyPokemonMod allyMod = settings.get(Settings.Name.TOTEMS_RANDOMIZE_ALLIES);
            Settings.AuraMod auraMod = settings.get(Settings.Name.TOTEMS_RANDOMIZE_AURAS);

            if (totemMod != Settings.TotemPokemonMod.UNCHANGED ||
                    allyMod != Settings.AllyPokemonMod.UNCHANGED ||
                    auraMod != Settings.AuraMod.UNCHANGED ||
                    (boolean) settings.get(Settings.Name.TOTEMS_RANDOMIZE_HELD_ITEMS) ||
                    !settings.isDefault(Settings.Name.TOTEMS_LEVEL_MODIFIER_PERCENT)) {

                staticPokeRandomizer.randomizeTotemPokemon();
            }
        }
    }

    private void maybeRandomizeWildPokemon() {
        Settings.CatchRateMod catchRateMod = settings.get(Settings.Name.WILD_MINIMUM_CATCH_RATE_SELECTION);
        if (catchRateMod != Settings.CatchRateMod.UNCHANGED) {
            wildEncounterRandomizer.changeCatchRates();
        }

        if ((boolean) settings.get(Settings.Name.RANDOMIZE_WILD_ENCOUNTERS)
                || !settings.isDefault(Settings.Name.WILD_LEVEL_MODIFIER_PERCENT)) {
            wildEncounterRandomizer.randomizeEncounters();
        }
    }

    private void maybeRandomizeInGameTrades() {
        Settings.InGameTradesMod mod = settings.get(Settings.Name.RANDOMIZE_IN_GAME_TRADES);
        switch (mod) {
            case RANDOMIZE_GIVEN:
            case RANDOMIZE_GIVEN_AND_REQUESTED:
                tradeRandomizer.randomizeIngameTrades();
        }
    }

    private void maybeRandomizeFieldItems() {
        Settings.FieldItemsMod mod = settings.get(Settings.Name.RANDOMIZE_FIELD_ITEMS);
        switch (mod) {
            case SHUFFLE:
            case RANDOM:
            case RANDOM_EVEN:
                itemRandomizer.randomizeFieldItems();
        }
    }

    private void maybeRandomizeShops() {
        Settings.ShopItemsMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIAL_SHOP_ITEMS);
        switch (mod) {
            case SHUFFLE:
                itemRandomizer.shuffleShopItems();
                break;
            case RANDOM:
                itemRandomizer.randomizeShopItems();
        }
        if (settings.get(Settings.Name.SHOP_ITEMS_BALANCE_PRICES)) {
            romHandler.setBalancedShopPrices();
        }
        if (settings.get(Settings.Name.SHOP_ITEMS_ADD_CHEAP_RARE_CANDY)) {
            itemRandomizer.addCheapRareCandiesToShops();
        }
    }

    private void maybeRandomizePickupItems() {
        Settings.PickupItemsMod mod = settings.get(Settings.Name.RANDOMIZE_PICKUP_ITEMS);
        if (mod == Settings.PickupItemsMod.RANDOM) {
            itemRandomizer.randomizePickupItems();
        }
    }

    private void maybeRandomizePokemonPalettes() {
        Settings.SpeciesPalettesMod mod = settings.get(Settings.Name.RANDOMIZE_SPECIES_PALETTES);
        if (mod == Settings.SpeciesPalettesMod.RANDOM) {
            paletteRandomizer.randomizePokemonPalettes();
        }
    }

    private void maybeRandomizeIntroPokemon() {
        // TODO: move to live with the misc tweaks?
        if (!(boolean) settings.get(Settings.Name.COSMETIC_RANDOM_INTRO_MON)) {
            introPokeRandomizer.randomizeIntroPokemon();
        }
    }
}