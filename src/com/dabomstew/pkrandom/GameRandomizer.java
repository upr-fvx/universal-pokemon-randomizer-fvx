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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import com.dabomstew.pkrandom.game_data.*;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.random.SeedPicker;
import com.dabomstew.pkrandom.randomizers.Gen1PaletteRandomizer;
import com.dabomstew.pkrandom.randomizers.Gen2PaletteRandomizer;
import com.dabomstew.pkrandom.randomizers.Gen3to5PaletteRandomizer;
import com.dabomstew.pkrandom.randomizers.PaletteRandomizer;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.romhandlers.Gen1RomHandler;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.updaters.MoveUpdater;
import com.dabomstew.pkrandom.updaters.PokemonBaseStatUpdater;
import com.dabomstew.pkrandom.updaters.TypeEffectivenessUpdater;

/**
 * Coordinates and logs the randomization of a game, via a {@link RomHandler}, and various sub-{@link Randomizer}s.
 * Output varies by seed.
 */
public class GameRandomizer {

    private static final String NEWLINE = System.getProperty("line.separator");

    private final RandomSource randomSource = new RandomSource();

    private final Settings settings;
    private final RomHandler romHandler;
    private final ResourceBundle bundle;
    private final boolean saveAsDirectory;

    private final PokemonBaseStatUpdater pokeBSUpdater;
    private final MoveUpdater moveUpdater;
    private final TypeEffectivenessUpdater typeEffUpdater;

    private final IntroPokemonRandomizer introPokeRandomizer;
    private final PokemonBaseStatRandomizer pokeBSRandomizer;
    private final PokemonTypeRandomizer pokeTypeRandomizer;
    private final PokemonAbilityRandomizer pokeAbilityRandomizer;
    private final EvolutionRandomizer evoRandomizer;
    private final StarterRandomizer starterRandomizer;
    private final StaticPokemonRandomizer staticPokeRandomizer;
    private final TradeRandomizer tradeRandomizer;
    private final MoveDataRandomizer moveDataRandomizer;
    private final PokemonMovesetRandomizer pokeMovesetRandomizer;
    private final TrainerPokemonRandomizer trainerPokeRandomizer;
    private final TrainerMovesetRandomizer trainerMovesetRandomizer;
    private final TrainerNameRandomizer trainerNameRandomizer;
    private final EncounterRandomizer encounterRandomizer;
    private final PokemonWildHeldItemRandomizer pokeHeldItemRandomizer;
    private final TMTutorMoveRandomizer tmtMoveRandomizer;
    private final TMHMTutorCompatibilityRandomizer tmhmtCompRandomizer;
    private final ItemRandomizer itemRandomizer;
    private final TypeEffectivenessRandomizer typeEffRandomizer;
    private final PaletteRandomizer paletteRandomizer;
    private final MiscTweakRandomizer miscTweakRandomizer;

    public GameRandomizer(Settings settings, RomHandler romHandler, ResourceBundle bundle, boolean saveAsDirectory) {
        this.settings = settings;
        this.romHandler = romHandler;
        this.bundle = bundle;
        this.saveAsDirectory = saveAsDirectory;

        this.pokeBSUpdater = new PokemonBaseStatUpdater(romHandler);
        this.moveUpdater = new MoveUpdater(romHandler);
        this.typeEffUpdater = new TypeEffectivenessUpdater(romHandler);

        this.introPokeRandomizer = new IntroPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.pokeBSRandomizer = new PokemonBaseStatRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.pokeTypeRandomizer = new PokemonTypeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.pokeAbilityRandomizer = new PokemonAbilityRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.evoRandomizer = new EvolutionRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.starterRandomizer = new StarterRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.staticPokeRandomizer = new StaticPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.tradeRandomizer = new TradeRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.moveDataRandomizer = new MoveDataRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.pokeMovesetRandomizer = new PokemonMovesetRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerPokeRandomizer = new TrainerPokemonRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerMovesetRandomizer = new TrainerMovesetRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.trainerNameRandomizer = new TrainerNameRandomizer(romHandler, settings,  randomSource.getCosmetic());
        this.encounterRandomizer = new EncounterRandomizer(romHandler, settings, randomSource.getNonCosmetic());
        this.pokeHeldItemRandomizer = new PokemonWildHeldItemRandomizer(romHandler, settings, randomSource.getNonCosmetic());
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

        int checkValue = 0;

        log.println("Randomizer Version: " + Version.VERSION_STRING);
        log.println("Random Seed: " + seed);
        log.println("Settings String: " + Version.VERSION + settings.toString());
        log.println();

        // Limit Pokemon
        // 1. Set Pokemon pool according to limits (or lack thereof)
        // 2. If limited, remove evolutions that are outside of the pool

        romHandler.getRestrictedPokemonService().setRestrictions(settings);

        if (settings.isLimitPokemon()) {
            romHandler.removeEvosForPokemonPool();
        }

        // Type effectiveness

        if (settings.isUpdateTypeEffectiveness()) {
            typeEffUpdater.updateTypeEffectiveness();
        }
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
        if (typeEffUpdater.isUpdated() || typeEffRandomizer.isChangesMade()) {
            log.println("--Type Effectiveness--");
            log.println(romHandler.getTypeTable().toBigString() + NEWLINE);
        }

        // Move updates & data changes
        // 1. Update moves to a future generation
        // 2. Randomize move stats

        if (settings.isUpdateMoves()) {
            moveUpdater.updateMoves(settings.getUpdateMovesToGeneration());
        }

        if (moveUpdater.isUpdated()) {
            logMoveUpdates(log);
        }

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

        // Misc Tweaks
        if (settings.getCurrentMiscTweaks() != MiscTweak.NO_MISC_TWEAKS) {
            miscTweakRandomizer.applyMiscTweaks();
        }

        // Update base stats to a future generation
        if (settings.isUpdateBaseStats()) {
            pokeBSUpdater.updatePokemonStats(settings.getUpdateBaseStatsToGeneration());
        }

        // Standardize EXP curves
        if (settings.isStandardizeEXPCurves()) {
            pokeBSRandomizer.standardizeEXPCurves();
        }

        // Pokemon Types
        if (settings.getTypesMod() != Settings.TypesMod.UNCHANGED) {
            pokeTypeRandomizer.randomizePokemonTypes();
        }

        // Wild Held Items
        if (settings.isRandomizeWildPokemonHeldItems()) {
            pokeHeldItemRandomizer.randomizeWildHeldItems();
        }

        // Random Evos
        // Applied after type to pick new evos based on new types.

        if (settings.getEvolutionsMod() != Settings.EvolutionsMod.UNCHANGED) {
            evoRandomizer.randomizeEvolutions();
        }

        if (evoRandomizer.isChangesMade()) {
            logEvolutionChanges(log);
        }

        // Base stat randomization
        switch (settings.getBaseStatisticsMod()) {
            case SHUFFLE:
                pokeBSRandomizer.shufflePokemonStats();
                break;
            case RANDOM:
                pokeBSRandomizer.randomizePokemonStats();
        }

        // Abilities
        if (settings.getAbilitiesMod() == Settings.AbilitiesMod.RANDOMIZE) {
            pokeAbilityRandomizer.randomizeAbilities();
        }

        // Log Pokemon traits (stats, abilities, etc) if any have changed
        if (pokeBSUpdater.isUpdated() || pokeBSRandomizer.isChangesMade() || pokeTypeRandomizer.isChangesMade() ||
                pokeAbilityRandomizer.isChangesMade() || pokeHeldItemRandomizer.isChangesMade()) {
            logPokemonTraitChanges(log);
        } else {
            log.println("Pokemon base stats & type: unchanged" + NEWLINE);
        }

        for (Species pkmn : romHandler.getPokemon()) {
            if (pkmn != null) {
                checkValue = addToCV(checkValue, pkmn.getHp(), pkmn.getAttack(), pkmn.getDefense(), pkmn.getSpeed(), pkmn.getSpatk(),
                        pkmn.getSpdef(), pkmn.getAbility1(), pkmn.getAbility2(), pkmn.getAbility3());
            }
        }

        // Trade evolutions removal
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

        // Log everything afterwards, so that "impossible evolutions" can account for "easier evolutions"
        if (settings.isChangeImpossibleEvolutions()) {
            log.println("--Removing Impossible Evolutions--");
            logUpdatedEvolutions(log, romHandler.getImpossibleEvoUpdates(), romHandler.getEasierEvoUpdates());
        }

        if (settings.isMakeEvolutionsEasier()) {
            log.println("--Making Evolutions Easier--");
            if (!(romHandler instanceof Gen1RomHandler)) {
                log.println("Friendship evolutions now take 160 happiness (was 220).");
            }
            logUpdatedEvolutions(log, romHandler.getEasierEvoUpdates(), null);
        }

        if (settings.isRemoveTimeBasedEvolutions()) {
            log.println("--Removing Timed-Based Evolutions--");
            logUpdatedEvolutions(log, romHandler.getTimeBasedEvoUpdates(), null);
        }

        // Starter Pokemon
        // Applied after type to update the strings correctly based on new types
        if(settings.getStartersMod() != Settings.StartersMod.UNCHANGED) {
            starterRandomizer.randomizeStarters();
        }
        if (settings.isRandomizeStartersHeldItems() && !(romHandler instanceof Gen1RomHandler)) {
            starterRandomizer.randomizeStarterHeldItems();
        }

        if (starterRandomizer.isChangesMade()) {
            logStarters(log);
        }

        // Move Data Log
        // Placed here so it matches its position in the randomizer interface
        if (moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated()) {
            logMoveChanges(log);
        } else {
            log.println("Move Data: Unchanged." + NEWLINE);
        }

        // Movesets
        // 1. Randomize movesets
        // 2. Reorder moves by damage
        // Note: "Metronome only" is handled after trainers instead

        if (settings.getMovesetsMod() != Settings.MovesetsMod.UNCHANGED &&
                settings.getMovesetsMod() != Settings.MovesetsMod.METRONOME_ONLY) {
            pokeMovesetRandomizer.randomizeMovesLearnt();
            pokeMovesetRandomizer.randomizeEggMoves();
        }

        if (settings.isReorderDamagingMoves()) {
            pokeMovesetRandomizer.orderDamagingMovesByDamage();
        }

        // Show the new movesets if applicable
        if (pokeMovesetRandomizer.isChangesMade()) {
            logMovesetChanges(log);
        } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("Pokemon Movesets: Metronome Only." + NEWLINE);
        } else {
            log.println("Pokemon Movesets: Unchanged." + NEWLINE);
        }

        // TMs

        if (!(settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY)
                && settings.getTmsMod() == Settings.TMsMod.RANDOM) {
            tmtMoveRandomizer.randomizeTMMoves();
        }

        if (tmtMoveRandomizer.isTMChangesMade()) {
            checkValue = logTMMoves(log, checkValue);
        } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("TM Moves: Metronome Only." + NEWLINE);
        } else {
            log.println("TM Moves: Unchanged." + NEWLINE);
        }

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

        // Copy TM/HM compatibility to cosmetic formes if it was changed at all, and log changes
        if (tmhmtCompRandomizer.isTMHMChangesMade()) {
            tmhmtCompRandomizer.copyTMCompatibilityToCosmeticFormes();
            logTMHMCompatibility(log);
        }

        // Move Tutors
        if (romHandler.hasMoveTutors()) {

            List<Integer> oldMtMoves = romHandler.getMoveTutorMoves();

            if (!(settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY)
                    && settings.getMoveTutorMovesMod() == Settings.MoveTutorMovesMod.RANDOM) {
                tmtMoveRandomizer.randomizeMoveTutorMoves();
            }

            if (tmtMoveRandomizer.isTutorChangesMade()) {
                checkValue = logMoveTutorMoves(log, checkValue, oldMtMoves);
            } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
                log.println("Move Tutor Moves: Metronome Only." + NEWLINE);
            } else {
                log.println("Move Tutor Moves: Unchanged." + NEWLINE);
            }

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
                logTutorCompatibility(log);
            }

        }

        // do part of wild Pokemon early if needed
        if (settings.isTrainersUseLocalPokemon() &&
                (settings.getWildPokemonMod() != Settings.WildPokemonMod.UNCHANGED ||
                        settings.isWildLevelsModified())) {
            encounterRandomizer.randomizeEncounters();
        }

        // Trainer Pokemon
        // 1. Add extra Trainer Pokemon
        // 2. Set trainers to be double battles and add extra Pokemon if necessary
        // 3. Randomize Trainer Pokemon
        // 4. Modify rivals to carry starters
        // 5. Force Trainer Pokemon to be fully evolved

        if (settings.getAdditionalRegularTrainerPokemon() > 0
                || settings.getAdditionalImportantTrainerPokemon() > 0
                || settings.getAdditionalBossTrainerPokemon() > 0) {
            trainerPokeRandomizer.addTrainerPokemon();
        }

        if (settings.isDoubleBattleMode()) {
            trainerPokeRandomizer.setDoubleBattleMode();
        }

        if(settings.getTrainersMod() != Settings.TrainersMod.UNCHANGED) {
                trainerPokeRandomizer.randomizeTrainerPokes();
        } else {
            if (settings.isTrainersLevelModified()) {
                    trainerPokeRandomizer.onlyChangeTrainerLevels();
            }
        }

        if ((settings.getTrainersMod() != Settings.TrainersMod.UNCHANGED
                || settings.getStartersMod() != Settings.StartersMod.UNCHANGED)
                && settings.isRivalCarriesStarterThroughout()) {
            trainerPokeRandomizer.makeRivalCarryStarter();
        }

        if (settings.isTrainersForceFullyEvolved()) {
            trainerPokeRandomizer.forceFullyEvolvedTrainerPokes();
        }

        if (settings.isBetterTrainerMovesets()) {
            trainerMovesetRandomizer.randomizeTrainerMovesets();
        }

        if (pokeMovesetRandomizer.isChangesMade() || trainerPokeRandomizer.isChangesMade()
                || trainerMovesetRandomizer.isChangesMade()) {
            // if earlier randomization could have led to unusable Z-crystals, fix them to something usable here
            trainerPokeRandomizer.randomUsableZCrystals();
        }

        if (settings.isRandomizeHeldItemsForBossTrainerPokemon()
                || settings.isRandomizeHeldItemsForImportantTrainerPokemon()
                || settings.isRandomizeHeldItemsForRegularTrainerPokemon()) {
            trainerPokeRandomizer.randomizeTrainerHeldItems();
        }

        List<String> originalTrainerNames = getTrainerNames();

        // Trainer names & class names randomization
        if (romHandler.canChangeTrainerText()) {
            if (settings.isRandomizeTrainerClassNames()) {
                trainerNameRandomizer.randomizeTrainerClassNames();
            }

            if (settings.isRandomizeTrainerNames()) {
                trainerNameRandomizer.randomizeTrainerNames();
            }
        }

        if (trainerPokeRandomizer.isChangesMade() || trainerMovesetRandomizer.isChangesMade()
                || trainerNameRandomizer.isChangesMade()) {
            maybeLogTrainerChanges(log, originalTrainerNames, trainerNameRandomizer.isChangesMade(),
                    trainerMovesetRandomizer.isChangesMade());
        } else {
            log.println("Trainers: Unchanged." + NEWLINE);
        }

        // Apply metronome only mode now that trainers have been dealt with
        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            pokeMovesetRandomizer.metronomeOnlyMode();
        }

        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer t : trainers) {
            for (TrainerPokemon tpk : t.pokemon) {
                checkValue = addToCV(checkValue, tpk.level, tpk.species.getNumber());
            }
        }

        // Static Pokemon
        if (romHandler.canChangeStaticPokemon()) {
            List<StaticEncounter> oldStatics = romHandler.getStaticPokemon();
            if (settings.getStaticPokemonMod() != Settings.StaticPokemonMod.UNCHANGED) { // Legendary for L
                staticPokeRandomizer.randomizeStaticPokemon();
            } else if (settings.isStaticLevelModified()) {
                staticPokeRandomizer.onlyChangeStaticLevels();
            }

            if (staticPokeRandomizer.isStaticChangesMade()) {
                checkValue = logStaticPokemon(log, checkValue, oldStatics);
            } else {
                log.println("Static Pokemon: Unchanged." + NEWLINE);
            }
        }

        // Totem Pokemon
        if (romHandler.hasTotemPokemon()) {
            List<TotemPokemon> oldTotems = romHandler.getTotemPokemon();
            if (settings.getTotemPokemonMod() != Settings.TotemPokemonMod.UNCHANGED ||
                    settings.getAllyPokemonMod() != Settings.AllyPokemonMod.UNCHANGED ||
                    settings.getAuraMod() != Settings.AuraMod.UNCHANGED ||
                    settings.isRandomizeTotemHeldItems() ||
                    settings.isTotemLevelsModified()) {

                staticPokeRandomizer.randomizeTotemPokemon();
            }

            if (staticPokeRandomizer.isTotemChangesMade()) {
                checkValue = logTotemPokemon(log, checkValue, oldTotems);
            } else {
                log.println("Totem Pokemon: Unchanged." + NEWLINE);
            }
        }

        // Wild Pokemon

        if (settings.isUseMinimumCatchRate()) {
            encounterRandomizer.changeCatchRates();
        }

        if (!settings.isTrainersUseLocalPokemon() &&
                (settings.getWildPokemonMod() != Settings.WildPokemonMod.UNCHANGED ||
                settings.isWildLevelsModified())) {
            encounterRandomizer.randomizeEncounters();
        }

        if (encounterRandomizer.isChangesMade()) {
            logWildPokemonChanges(log);
        } else {
            log.println("Wild Pokemon: Unchanged." + NEWLINE);
        }

        boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                (settings.getWildPokemonMod() == Settings.WildPokemonMod.UNCHANGED && settings.isWildLevelsModified());
        List<EncounterArea> encounterAreas = romHandler.getEncounters(useTimeBasedEncounters);
        for (EncounterArea area : encounterAreas) {
            for (Encounter e : area) {
                checkValue = addToCV(checkValue, e.getLevel(), e.getSpecies().getNumber());
            }
        }


        // In-game trades

        List<IngameTrade> oldTrades = romHandler.getIngameTrades();
        switch (settings.getInGameTradesMod()) {
            case RANDOMIZE_GIVEN:
            case RANDOMIZE_GIVEN_AND_REQUESTED:
                tradeRandomizer.randomizeIngameTrades();
        }

        if (tradeRandomizer.isChangesMade()) {
            logTrades(log, oldTrades);
        }

        // Field Items
        switch (settings.getFieldItemsMod()) {
            case SHUFFLE:
                itemRandomizer.shuffleFieldItems();
                break;
            case RANDOM:
            case RANDOM_EVEN:
                itemRandomizer.randomizeFieldItems();
        }

        // Shops

        switch (settings.getShopItemsMod()) {
            case SHUFFLE:
                itemRandomizer.shuffleShopItems();
                break;
            case RANDOM:
                itemRandomizer.randomizeShopItems();
        }

        if (itemRandomizer.isShopChangesMade()) {
            logShops(log);
        }

        // Pickup Items
        if (settings.getPickupItemsMod() == Settings.PickupItemsMod.RANDOM) {
            itemRandomizer.randomizePickupItems();
            logPickupItems(log);
        }

        // Test output for placement history
        // romHandler.renderPlacementHistory();

        if (settings.getPokemonPalettesMod() == Settings.PokemonPalettesMod.RANDOM) {
            paletteRandomizer.randomizePokemonPalettes();
        }

        if (settings.getCustomPlayerGraphicsMod() == Settings.CustomPlayerGraphicsMod.RANDOM) {
            romHandler.setCustomPlayerGraphics(settings.getCustomPlayerGraphics(),
                    settings.getCustomPlayerGraphicsCharacterMod());
        }

        // Intro Pokemon...
        introPokeRandomizer.randomizeIntroPokemon();

        // Record check value?
        romHandler.writeCheckValueToROM(checkValue);

        // Save
        romHandler.saveRom(filename, seed, saveAsDirectory);

        // Log tail
        String gameName = romHandler.getROMName();
        if (romHandler.hasGameUpdateLoaded()) {
            gameName = gameName + " (" + romHandler.getGameUpdateVersion() + ")";
        }
        log.println("------------------------------------------------------------------");
        log.println("Randomization of " + gameName + " completed.");
        log.println("Time elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
        log.println("RNG Calls: " + randomSource.callsSinceSeed());
        log.println("------------------------------------------------------------------");
        log.println();

        // Diagnostics
        log.println("--ROM Diagnostics--");
        if (!romHandler.isRomValid()) {
            log.println(bundle.getString("Log.InvalidRomLoaded"));
        }
        romHandler.printRomDiagnostics(log);

        return checkValue;
    }

    private int logMoveTutorMoves(PrintStream log, int checkValue, List<Integer> oldMtMoves) {
        log.println("--Move Tutor Moves--");
        List<Integer> newMtMoves = romHandler.getMoveTutorMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < newMtMoves.size(); i++) {
            log.printf("%-10s -> %-10s" + NEWLINE, moves.get(oldMtMoves.get(i)).name,
                    moves.get(newMtMoves.get(i)).name);
            checkValue = addToCV(checkValue, newMtMoves.get(i));
        }
        log.println();
        return checkValue;
    }

    private int logTMMoves(PrintStream log, int checkValue) {
        log.println("--TM Moves--");
        List<Integer> tmMoves = romHandler.getTMMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < tmMoves.size(); i++) {
            log.printf("TM%02d %s" + NEWLINE, i + 1, moves.get(tmMoves.get(i)).name);
            checkValue = addToCV(checkValue, tmMoves.get(i));
        }
        log.println();
        return checkValue;
    }

    private void logTrades(PrintStream log, List<IngameTrade> oldTrades) {
        log.println("--In-Game Trades--");
        List<IngameTrade> newTrades = romHandler.getIngameTrades();
        int size = oldTrades.size();
        for (int i = 0; i < size; i++) {
            IngameTrade oldT = oldTrades.get(i);
            IngameTrade newT = newTrades.get(i);
            log.printf("Trade %-11s -> %-11s the %-11s        ->      %-11s -> %-15s the %s" + NEWLINE,
                    oldT.requestedSpecies != null ? oldT.requestedSpecies.fullName() : "Any",
                    oldT.nickname, oldT.givenSpecies.fullName(),
                    newT.requestedSpecies != null ? newT.requestedSpecies.fullName() : "Any",
                    newT.nickname, newT.givenSpecies.fullName());
        }
        log.println();
    }

    private void logMovesetChanges(PrintStream log) {
        log.println("--Pokemon Movesets--");
        List<String> movesets = new ArrayList<>();
        Map<Integer, List<MoveLearnt>> moveData = romHandler.getMovesLearnt();
        Map<Integer, List<Integer>> eggMoves = romHandler.getEggMoves();
        List<Move> moves = romHandler.getMoves();
        List<Species> pkmnList = romHandler.getPokemonInclFormes();
        int i = 1;
        for (Species pkmn : pkmnList) {
            if (pkmn == null || pkmn.isActuallyCosmetic()) {
                continue;
            }
            StringBuilder evoStr = new StringBuilder();
            try {
                evoStr.append(" -> ").append(pkmn.getEvolutionsFrom().get(0).getTo().fullName());
            } catch (Exception e) {
                evoStr.append(" (no evolution)");
            }

            StringBuilder sb = new StringBuilder();

            if (romHandler instanceof Gen1RomHandler) {
                sb.append(String.format("%03d %s", i, pkmn.fullName()))
                        .append(evoStr).append(System.getProperty("line.separator"))
                        .append(String.format("HP   %-3d", pkmn.getHp())).append(System.getProperty("line.separator"))
                        .append(String.format("ATK  %-3d", pkmn.getAttack())).append(System.getProperty("line.separator"))
                        .append(String.format("DEF  %-3d", pkmn.getDefense())).append(System.getProperty("line.separator"))
                        .append(String.format("SPEC %-3d", pkmn.getSpecial())).append(System.getProperty("line.separator"))
                        .append(String.format("SPE  %-3d", pkmn.getSpeed())).append(System.getProperty("line.separator"));
            } else {
                sb.append(String.format("%03d %s", i, pkmn.fullName()))
                        .append(evoStr).append(System.getProperty("line.separator"))
                        .append(String.format("HP  %-3d", pkmn.getHp())).append(System.getProperty("line.separator"))
                        .append(String.format("ATK %-3d", pkmn.getAttack())).append(System.getProperty("line.separator"))
                        .append(String.format("DEF %-3d", pkmn.getDefense())).append(System.getProperty("line.separator"))
                        .append(String.format("SPA %-3d", pkmn.getSpatk())).append(System.getProperty("line.separator"))
                        .append(String.format("SPD %-3d", pkmn.getSpdef())).append(System.getProperty("line.separator"))
                        .append(String.format("SPE %-3d", pkmn.getSpeed())).append(System.getProperty("line.separator"));
            }

            i++;

            List<MoveLearnt> data = moveData.get(pkmn.getNumber());
            for (MoveLearnt ml : data) {
                try {
                    if (ml.level == 0) {
                        sb.append("Learned upon evolution: ")
                                .append(moves.get(ml.move).name).append(System.getProperty("line.separator"));
                    } else {
                        sb.append("Level ")
                                .append(String.format("%-2d", ml.level))
                                .append(": ")
                                .append(moves.get(ml.move).name).append(System.getProperty("line.separator"));
                    }
                } catch (NullPointerException ex) {
                    sb.append("invalid move at level").append(ml.level);
                }
            }
            List<Integer> eggMove = eggMoves.get(pkmn.getNumber());
            if (eggMove != null && eggMove.size() != 0) {
                sb.append("Egg Moves:").append(System.getProperty("line.separator"));
                for (Integer move : eggMove) {
                    sb.append(" - ").append(moves.get(move).name).append(System.getProperty("line.separator"));
                }
            }

            movesets.add(sb.toString());
        }
        Collections.sort(movesets);
        for (String moveset : movesets) {
            log.println(moveset);
        }
        log.println();
    }

    private void logMoveUpdates(PrintStream log) {
        log.println("--Move Updates--");
        List<Move> moves = romHandler.getMoves();
        Map<Integer, boolean[]> moveUpdates = moveUpdater.getMoveUpdates();
        for (int moveID : moveUpdates.keySet()) {
            boolean[] changes = moveUpdates.get(moveID);
            Move mv = moves.get(moveID);
            List<String> nonTypeChanges = new ArrayList<>();
            if (changes[0]) {
                nonTypeChanges.add(String.format("%d power", mv.power));
            }
            if (changes[1]) {
                nonTypeChanges.add(String.format("%d PP", mv.pp));
            }
            if (changes[2]) {
                nonTypeChanges.add(String.format("%.00f%% accuracy", mv.hitratio));
            }
            String logStr = "Made " + mv.name;
            // type or not?
            if (changes[3]) {
                logStr += " be " + mv.type + "-type";
                if (nonTypeChanges.size() > 0) {
                    logStr += " and";
                }
            }
            if (changes[4]) {
                if (mv.category == MoveCategory.PHYSICAL) {
                    logStr += " a Physical move";
                } else if (mv.category == MoveCategory.SPECIAL) {
                    logStr += " a Special move";
                } else if (mv.category == MoveCategory.STATUS) {
                    logStr += " a Status move";
                }
            }
            if (nonTypeChanges.size() > 0) {
                logStr += " have ";
                if (nonTypeChanges.size() == 3) {
                    logStr += nonTypeChanges.get(0) + ", " + nonTypeChanges.get(1) + " and " + nonTypeChanges.get(2);
                } else if (nonTypeChanges.size() == 2) {
                    logStr += nonTypeChanges.get(0) + " and " + nonTypeChanges.get(1);
                } else {
                    logStr += nonTypeChanges.get(0);
                }
            }
            log.println(logStr);
        }
        log.println();
    }

    private void logEvolutionChanges(PrintStream log) {
        log.println("--Randomized Evolutions--");
        List<Species> allPokes = romHandler.getPokemonInclFormes();
        for (Species pk : allPokes) {
            if (pk != null && !pk.isActuallyCosmetic()) {
                int numEvos = pk.getEvolutionsFrom().size();
                if (numEvos > 0) {
                    StringBuilder evoStr = new StringBuilder(pk.getEvolutionsFrom().get(0).getTo().fullName());
                    for (int i = 1; i < numEvos; i++) {
                        if (i == numEvos - 1) {
                            evoStr.append(" and ").append(pk.getEvolutionsFrom().get(i).getTo().fullName());
                        } else {
                            evoStr.append(", ").append(pk.getEvolutionsFrom().get(i).getTo().fullName());
                        }
                    }
                    log.printf("%-15s -> %-15s" + NEWLINE, pk.fullName(), evoStr);
                }
            }
        }

        log.println();
    }

    private void logPokemonTraitChanges(final PrintStream log) {
        List<Species> allPokes = romHandler.getPokemonInclFormes();
        String[] itemNames = romHandler.getItemNames();
        // Log base stats & types
        log.println("--Pokemon Base Stats & Types--");
        if (romHandler instanceof Gen1RomHandler) {
            log.println("NUM|NAME      |TYPE             |  HP| ATK| DEF| SPE|SPEC");
            for (Species pkmn : allPokes) {
                if (pkmn != null) {
                    String typeString = pkmn.getPrimaryType(false) == null ? "???" : pkmn.getPrimaryType(false).toString();
                    if (pkmn.getSecondaryType(false) != null) {
                        typeString += "/" + pkmn.getSecondaryType(false).toString();
                    }
                    log.printf("%3d|%-10s|%-17s|%4d|%4d|%4d|%4d|%4d" + NEWLINE, pkmn.getNumber(), pkmn.fullName(), typeString,
                            pkmn.getHp(), pkmn.getAttack(), pkmn.getDefense(), pkmn.getSpeed(), pkmn.getSpecial());
                }

            }
        } else {
            String nameSp = "      ";
            String nameSpFormat = "%-13s";
            String abSp = "    ";
            String abSpFormat = "%-12s";
            if (romHandler.generationOfPokemon() == 5) {
                nameSp = "         ";
            } else if (romHandler.generationOfPokemon() == 6) {
                nameSp = "            ";
                nameSpFormat = "%-16s";
                abSp = "      ";
                abSpFormat = "%-14s";
            } else if (romHandler.generationOfPokemon() >= 7) {
                nameSp = "            ";
                nameSpFormat = "%-16s";
                abSp = "        ";
                abSpFormat = "%-16s";
            }

            log.print("NUM|NAME" + nameSp + "|TYPE             |  HP| ATK| DEF|SATK|SDEF| SPD");
            int abils = romHandler.abilitiesPerPokemon();
            for (int i = 0; i < abils; i++) {
                log.print("|ABILITY" + (i + 1) + abSp);
            }
            log.print("|ITEM");
            log.println();
            int i = 0;
            for (Species pkmn : allPokes) {
                if (pkmn != null && !pkmn.isActuallyCosmetic()) {
                    i++;
                    String typeString = pkmn.getPrimaryType(false) == null ? "???" : pkmn.getPrimaryType(false).toString();
                    if (pkmn.getSecondaryType(false) != null) {
                        typeString += "/" + pkmn.getSecondaryType(false).toString();
                    }
                    log.printf("%3d|" + nameSpFormat + "|%-17s|%4d|%4d|%4d|%4d|%4d|%4d", i, pkmn.fullName(), typeString,
                            pkmn.getHp(), pkmn.getAttack(), pkmn.getDefense(), pkmn.getSpatk(), pkmn.getSpdef(), pkmn.getSpeed());
                    if (abils > 0) {
                        log.printf("|" + abSpFormat + "|" + abSpFormat, romHandler.abilityName(pkmn.getAbility1()),
                                pkmn.getAbility1() == pkmn.getAbility2() ? "--" : romHandler.abilityName(pkmn.getAbility2()));
                        if (abils > 2) {
                            log.printf("|" + abSpFormat, romHandler.abilityName(pkmn.getAbility3()));
                        }
                    }
                    log.print("|");
                    if (pkmn.getGuaranteedHeldItem() > 0) {
                        log.print(itemNames[pkmn.getGuaranteedHeldItem()] + " (100%)");
                    } else {
                        int itemCount = 0;
                        if (pkmn.getCommonHeldItem() > 0) {
                            itemCount++;
                            log.print(itemNames[pkmn.getCommonHeldItem()] + " (common)");
                        }
                        if (pkmn.getRareHeldItem() > 0) {
                            if (itemCount > 0) {
                                log.print(", ");
                            }
                            itemCount++;
                            log.print(itemNames[pkmn.getRareHeldItem()] + " (rare)");
                        }
                        if (pkmn.getDarkGrassHeldItem() > 0) {
                            if (itemCount > 0) {
                                log.print(", ");
                            }
                            log.print(itemNames[pkmn.getDarkGrassHeldItem()] + " (dark grass only)");
                        }
                    }
                    log.println();
                }

            }
        }
        log.println();
    }

    private void logTMHMCompatibility(final PrintStream log) {
        log.println("--TM Compatibility--");
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        List<Integer> tmHMs = new ArrayList<>(romHandler.getTMMoves());
        tmHMs.addAll(romHandler.getHMMoves());
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(log, compat, tmHMs, moveData, true);
    }

    private void logTutorCompatibility(final PrintStream log) {
        log.println("--Move Tutor Compatibility--");
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        List<Integer> tutorMoves = romHandler.getMoveTutorMoves();
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(log, compat, tutorMoves, moveData, false);
    }

    private void logCompatibility(final PrintStream log, Map<Species, boolean[]> compat, List<Integer> moveList,
                                  List<Move> moveData, boolean includeTMNumber) {
        int tmCount = romHandler.getTMCount();
        for (Map.Entry<Species, boolean[]> entry : compat.entrySet()) {
            Species pkmn = entry.getKey();
            if (pkmn.isActuallyCosmetic()) continue;
            boolean[] flags = entry.getValue();

            String nameSpFormat = "%-14s";
            if (romHandler.generationOfPokemon() >= 6) {
                nameSpFormat = "%-17s";
            }
            log.printf("%3d " + nameSpFormat, pkmn.getNumber(), pkmn.fullName() + " ");

            for (int i = 1; i < flags.length; i++) {
                String moveName = moveData.get(moveList.get(i - 1)).name;
                if (moveName.length() == 0) {
                    moveName = "(BLANK)";
                }
                int moveNameLength = moveName.length();
                if (flags[i]) {
                    if (includeTMNumber) {
                        if (i <= tmCount) {
                            log.printf("|TM%02d %" + moveNameLength + "s ", i, moveName);
                        } else {
                            log.printf("|HM%02d %" + moveNameLength + "s ", i - tmCount, moveName);
                        }
                    } else {
                        log.printf("|%" + moveNameLength + "s ", moveName);
                    }
                } else {
                    if (includeTMNumber) {
                        log.printf("| %" + (moveNameLength + 4) + "s ", "-");
                    } else {
                        log.printf("| %" + (moveNameLength - 1) + "s ", "-");
                    }
                }
            }
            log.println("|");
        }
        log.println();
    }

    private void logUpdatedEvolutions(final PrintStream log, Set<EvolutionUpdate> updatedEvolutions,
                                      Set<EvolutionUpdate> otherUpdatedEvolutions) {
        for (EvolutionUpdate evo : updatedEvolutions) {
            if (otherUpdatedEvolutions != null && otherUpdatedEvolutions.contains(evo)) {
                log.println(evo.toString() + " (Overwritten by \"Make Evolutions Easier\", see below)");
            } else {
                log.println(evo.toString());
            }
        }
        log.println();
    }

    private void logStarters(final PrintStream log) {

        // TODO: log starter held items

        switch (settings.getStartersMod()) {
            case CUSTOM:
                log.println("--Custom Starters--");
                break;
            case COMPLETELY_RANDOM:
                log.println("--Random Starters--");
                break;
            case RANDOM_BASIC:
                log.println("--Random Basic Starters--");
                break;
            case RANDOM_WITH_TWO_EVOLUTIONS:
                log.println("--Random 2-Evolution Starters--");
        }

        List<Species> starters = romHandler.getStarters();
        int i = 1;
        for (Species starter : starters) {
            log.println("Set starter " + i + " to " + starter.fullName());
            i++;
        }
        log.println();
    }

    private void logWildPokemonChanges(final PrintStream log) {

        log.println("--Wild Pokemon--");
        boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                (settings.getWildPokemonMod() == Settings.WildPokemonMod.UNCHANGED && settings.isWildLevelsModified());
        List<EncounterArea> encounterAreas = romHandler.getSortedEncounters(useTimeBasedEncounters);
        int idx = 0;
        for (EncounterArea area : encounterAreas) {
            idx++;
            log.print("Set #" + idx + " ");
            if (area.getDisplayName() != null) {
                log.print("- " + area.getDisplayName() + " ");
            }
            log.print("(rate=" + area.getRate() + ")");
            log.println();
            for (Encounter e : area) {
                StringBuilder sb = new StringBuilder();
                if (e.isSOS()) {
                    String stringToAppend;
                    switch (e.getSosType()) {
                        case RAIN:
                            stringToAppend = "Rain SOS: ";
                            break;
                        case HAIL:
                            stringToAppend = "Hail SOS: ";
                            break;
                        case SAND:
                            stringToAppend = "Sand SOS: ";
                            break;
                        default:
                            stringToAppend = "  SOS: ";
                    }
                    sb.append(stringToAppend);
                }
                sb.append(e.getSpecies().fullName()).append(" Lv");
                if (e.getMaxLevel() > 0 && e.getMaxLevel() != e.getLevel()) {
                    sb.append("s ").append(e.getLevel()).append("-").append(e.getMaxLevel());
                } else {
                    sb.append(e.getLevel());
                }
                String whitespaceFormat = romHandler.generationOfPokemon() == 7 ? "%-31s" : "%-25s";
                log.printf(whitespaceFormat, sb);
                StringBuilder sb2 = new StringBuilder();
                if (romHandler instanceof Gen1RomHandler) {
                    sb2.append(String.format("HP %-3d ATK %-3d DEF %-3d SPECIAL %-3d SPEED %-3d", e.getSpecies().getHp(), e.getSpecies().getAttack(), e.getSpecies().getDefense(), e.getSpecies().getSpecial(), e.getSpecies().getSpeed()));
                } else {
                    sb2.append(String.format("HP %-3d ATK %-3d DEF %-3d SPATK %-3d SPDEF %-3d SPEED %-3d", e.getSpecies().getHp(), e.getSpecies().getAttack(), e.getSpecies().getDefense(), e.getSpecies().getSpatk(), e.getSpecies().getSpdef(), e.getSpecies().getSpeed()));
                }
                log.print(sb2);
                log.println();
            }
            log.println();
        }
        log.println();
    }

    private void maybeLogTrainerChanges(final PrintStream log, List<String> originalTrainerNames, boolean trainerNamesChanged, boolean logTrainerMovesets) {
        log.println("--Trainers Pokemon--");
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer t : trainers) {
            log.print("#" + t.index + " ");
            String originalTrainerName = originalTrainerNames.get(t.index);
            String currentTrainerName = "";
            if (t.fullDisplayName != null) {
                currentTrainerName = t.fullDisplayName;
            } else if (t.name != null) {
                currentTrainerName = t.name;
            }
            if (!currentTrainerName.isEmpty()) {
                if (trainerNamesChanged) {
                    log.printf("(%s => %s)", originalTrainerName, currentTrainerName);
                } else {
                    log.printf("(%s)", currentTrainerName);
                }
            }
            if (t.offset != 0) {
                log.printf("@%X", t.offset);
            }

            String[] itemNames = romHandler.getItemNames();
            if (logTrainerMovesets) {
                log.println();
                for (TrainerPokemon tpk : t.pokemon) {
                    List<Move> moves = romHandler.getMoves();
                    log.printf(tpk.toString(), itemNames[tpk.heldItem]);
                    log.print(", Ability: " + romHandler.abilityName(romHandler.getAbilityForTrainerPokemon(tpk)));
                    log.print(" - ");
                    boolean first = true;
                    for (int move : tpk.moves) {
                        if (move != 0) {
                            if (!first) {
                                log.print(", ");
                            }
                            log.print(moves.get(move).name);
                            first = false;
                        }
                    }
                    log.println();
                }
            } else {
                log.print(" - ");
                boolean first = true;
                for (TrainerPokemon tpk : t.pokemon) {
                    if (!first) {
                        log.print(", ");
                    }
                    log.printf(tpk.toString(), itemNames[tpk.heldItem]);
                    first = false;
                }
            }
            log.println();
        }
        log.println();
    }

    private int logStaticPokemon(final PrintStream log, int checkValue, List<StaticEncounter> oldStatics) {

        List<StaticEncounter> newStatics = romHandler.getStaticPokemon();

        log.println("--Static Pokemon--");
        Map<String, Integer> seenPokemon = new TreeMap<>();
        for (int i = 0; i < oldStatics.size(); i++) {
            StaticEncounter oldP = oldStatics.get(i);
            StaticEncounter newP = newStatics.get(i);
            checkValue = addToCV(checkValue, newP.pkmn.getNumber());
            String oldStaticString = oldP.toString(settings.isStaticLevelModified());
            log.print(oldStaticString);
            if (seenPokemon.containsKey(oldStaticString)) {
                int amount = seenPokemon.get(oldStaticString);
                log.print("(" + (++amount) + ")");
                seenPokemon.put(oldStaticString, amount);
            } else {
                seenPokemon.put(oldStaticString, 1);
            }
            log.println(" => " + newP.toString(settings.isStaticLevelModified()));
        }
        log.println();

        return checkValue;
    }

    private int logTotemPokemon(final PrintStream log, int checkValue, List<TotemPokemon> oldTotems) {

        List<TotemPokemon> newTotems = romHandler.getTotemPokemon();

        String[] itemNames = romHandler.getItemNames();
        log.println("--Totem Pokemon--");
        for (int i = 0; i < oldTotems.size(); i++) {
            TotemPokemon oldP = oldTotems.get(i);
            TotemPokemon newP = newTotems.get(i);
            checkValue = addToCV(checkValue, newP.pkmn.getNumber());
            log.println(oldP.pkmn.fullName() + " =>");
            log.printf(newP.toString(), itemNames[newP.heldItem]);
        }
        log.println();

        return checkValue;
    }

    private void logMoveChanges(final PrintStream log) {

        log.println("--Move Data--");
        log.print("NUM|NAME           |TYPE    |POWER|ACC.|PP");
        if (romHandler.hasPhysicalSpecialSplit()) {
            log.print(" |CATEGORY");
        }
        log.println();
        List<Move> allMoves = romHandler.getMoves();
        for (Move mv : allMoves) {
            if (mv != null) {
                String mvType = (mv.type == null) ? "???" : mv.type.toString();
                log.printf("%3d|%-15s|%-8s|%5d|%4d|%3d", mv.internalId, mv.name, mvType, mv.power,
                        (int) mv.hitratio, mv.pp);
                if (romHandler.hasPhysicalSpecialSplit()) {
                    log.printf("| %s", mv.category.toString());
                }
                log.println();
            }
        }
        log.println();
    }

    private void logShops(final PrintStream log) {
        String[] itemNames = romHandler.getItemNames();
        log.println("--Shops--");
        Map<Integer, Shop> shopsDict = romHandler.getShopItems();
        for (int shopID : shopsDict.keySet()) {
            Shop shop = shopsDict.get(shopID);
            log.printf("%s", shop.name);
            log.println();
            List<Integer> shopItems = shop.items;
            for (int shopItemID : shopItems) {
                log.printf("- %5s", itemNames[shopItemID]);
                log.println();
            }

            log.println();
        }
        log.println();
    }

    private void logPickupItems(final PrintStream log) {
        List<PickupItem> pickupItems = romHandler.getPickupItems();
        String[] itemNames = romHandler.getItemNames();
        log.println("--Pickup Items--");
        for (int levelRange = 0; levelRange < 10; levelRange++) {
            int startingLevel = (levelRange * 10) + 1;
            int endingLevel = (levelRange + 1) * 10;
            log.printf("Level %s-%s", startingLevel, endingLevel);
            log.println();
            TreeMap<Integer, List<String>> itemListPerProbability = new TreeMap<>();
            for (PickupItem pickupItem : pickupItems) {
                int probability = pickupItem.probabilities[levelRange];
                if (itemListPerProbability.containsKey(probability)) {
                    itemListPerProbability.get(probability).add(itemNames[pickupItem.item]);
                } else if (probability > 0) {
                    List<String> itemList = new ArrayList<>();
                    itemList.add(itemNames[pickupItem.item]);
                    itemListPerProbability.put(probability, itemList);
                }
            }
            for (Map.Entry<Integer, List<String>> itemListPerProbabilityEntry : itemListPerProbability.descendingMap().entrySet()) {
                int probability = itemListPerProbabilityEntry.getKey();
                List<String> itemList = itemListPerProbabilityEntry.getValue();
                String itemsString = String.join(", ", itemList);
                log.printf("%d%%: %s", probability, itemsString);
                log.println();
            }
            log.println();
        }
        log.println();
    }

    private List<String> getTrainerNames() {
        List<String> trainerNames = new ArrayList<>();
        trainerNames.add(""); // for index 0
        List<Trainer> trainers = romHandler.getTrainers();
        for (Trainer t : trainers) {
            if (t.fullDisplayName != null) {
                trainerNames.add(t.fullDisplayName);
            } else if (t.name != null) {
                trainerNames.add(t.name);
            } else {
                trainerNames.add("");
            }
        }
        return trainerNames;
    }


    private static int addToCV(int checkValue, int... values) {
        for (int value : values) {
            checkValue = Integer.rotateLeft(checkValue, 3);
            checkValue ^= value;
        }
        return checkValue;
    }
}