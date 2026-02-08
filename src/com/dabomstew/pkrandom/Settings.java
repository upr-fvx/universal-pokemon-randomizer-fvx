package com.dabomstew.pkrandom;

/*----------------------------------------------------------------------------*/
/*--  Settings.java - encapsulates a configuration of settings used by the  --*/
/*--                  randomizer to determine how to randomize the          --*/
/*--                  target game.                                          --*/
/*--                                                                        --*/
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

import com.dabomstew.pkrandom.customnames.CustomNamesSet;
import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.zip.CRC32;

public class Settings {

    public static final int VERSION = Version.LATEST.id;

    public static final int LENGTH_OF_SETTINGS_DATA = 67;

    public static final int MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL = 40;

    private CustomNamesSet customNames;

    private String romName;
    private boolean updatedFromOldVersion = false;
    private GenRestrictions currentRestrictions = new GenRestrictions();
    private int currentMiscTweaks;

    private boolean changeImpossibleEvolutions;
    private boolean estimateLevelForEvolutionImprovements;
    private boolean makeEvolutionsEasier;
    private int makeEvolutionsEasierLvl = MAKE_EVOLUTIONS_EASIER_DEFAULT_LVL;
    private boolean removeTimeBasedEvolutions;
    private boolean raceMode;
    private boolean randomizeIntroMon;
    private boolean limitPokemon;
    private boolean banIrregularAltFormes;
    private boolean dualTypeOnly;

    public enum BaseStatisticsMod {
        UNCHANGED, SHUFFLE, RANDOM,
    }

    public enum ExpCurveMod {
        LEGENDARIES, STRONG_LEGENDARIES, ALL
    }

    private BaseStatisticsMod baseStatisticsMod = BaseStatisticsMod.UNCHANGED;
    private boolean baseStatsFollowEvolutions;
    private boolean baseStatsFollowMegaEvolutions;
    private boolean assignEvoStatsRandomly;
    private boolean updateBaseStats;
    private int updateBaseStatsToGeneration;
    private boolean standardizeEXPCurves;
    private ExpCurve selectedEXPCurve;
    private ExpCurveMod expCurveMod = ExpCurveMod.LEGENDARIES;

    public enum AbilitiesMod {
        UNCHANGED, RANDOMIZE
    }

    private AbilitiesMod abilitiesMod = AbilitiesMod.UNCHANGED;
    private boolean allowWonderGuard = true;
    private boolean abilitiesFollowEvolutions;
    private boolean abilitiesFollowMegaEvolutions;
    private boolean banTrappingAbilities;
    private boolean banNegativeAbilities;
    private boolean banBadAbilities;
    private boolean weighDuplicateAbilitiesTogether;
    private boolean ensureTwoAbilities;

    public enum StartersMod {
        UNCHANGED, CUSTOM, COMPLETELY_RANDOM, RANDOM_WITH_TWO_EVOLUTIONS, RANDOM_BASIC
    }

    private StartersMod startersMod = StartersMod.UNCHANGED;

    public enum StartersTypeMod {
        NONE, FIRE_WATER_GRASS, TRIANGLE, UNIQUE, SINGLE_TYPE
    }

    private StartersTypeMod startersTypeMod = StartersTypeMod.NONE;
    private Type startersSingleType = null;
    private boolean startersNoDualTypes;
    private boolean allowStarterAltFormes;
    private boolean startersNoLegendaries;

    // index in the rom's list of pokemon
    // offset from the dropdown index from RandomizerGUI by 1
    private int[] customStarters = new int[3];
    private boolean randomizeStartersHeldItems;
    private boolean limitMainGameLegendaries;
    private boolean limit600;
    private boolean banBadRandomStarterHeldItems;

    private int startersBSTMinimum, startersBSTMaximum;

    public enum SpeciesTypesMod {
        UNCHANGED, RANDOM_FOLLOW_EVOLUTIONS, COMPLETELY_RANDOM
    }

    private SpeciesTypesMod speciesTypesMod = SpeciesTypesMod.UNCHANGED;

    private boolean typesFollowMegaEvolutions;

    // Evolutions
    public enum EvolutionsMod {
        UNCHANGED, RANDOM, RANDOM_EVERY_LEVEL
    }

    private EvolutionsMod evolutionsMod = EvolutionsMod.UNCHANGED;
    private boolean evosSimilarStrength;
    private boolean evosSameTyping;
    private boolean evosMaxThreeStages;
    private boolean evosForceChange;
    private boolean evosAllowAltFormes;
    private boolean evosForceGrowth;
    private boolean evosNoConvergence;

    // Move data
    private boolean randomizeMovePowers;
    private boolean randomizeMoveAccuracies;
    private boolean randomizeMovePPs;
    private boolean randomizeMoveTypes;
    private boolean randomizeMoveCategory;
    private boolean updateMoves;
    private int updateMovesToGeneration;
    private boolean updateMovesLegacy;

    public enum MovesetsMod {
        UNCHANGED, RANDOM_PREFER_SAME_TYPE, COMPLETELY_RANDOM, METRONOME_ONLY
    }

    private MovesetsMod movesetsMod = MovesetsMod.UNCHANGED;
    private boolean startWithGuaranteedMoves;
    private int guaranteedMoveCount = 2;
    private boolean reorderDamagingMoves;
    private boolean movesetsForceGoodDamaging;
    private int movesetsGoodDamagingPercent = 0;
    private boolean blockBrokenMovesetMoves;
    private boolean evolutionMovesForAll;

    public enum TrainersMod {
        UNCHANGED, RANDOM, DISTRIBUTED, MAINPLAYTHROUGH, TYPE_THEMED,
        TYPE_THEMED_ELITE4_GYMS, KEEP_THEMED, KEEP_THEME_OR_PRIMARY
    }

    private TrainersMod trainersMod = TrainersMod.UNCHANGED;
    private boolean rivalCarriesStarterThroughout;
    private boolean trainersUsePokemonOfSimilarStrength;
    private boolean trainersDoNotGetPrematureEvos;
    private boolean trainersMatchTypingDistribution;
    private boolean trainersBlockLegendaries = true;
    private boolean trainersUseLocalPokemon;
    private boolean trainersBlockEarlyWonderGuard = true;
    private boolean trainersEnforceDistribution;
    private boolean trainersEnforceMainPlaythrough;
    private boolean randomizeTrainerNames;
    private boolean randomizeTrainerClassNames;
    private boolean trainersEvolveTheirPokemon;
    private int trainersEvolutionLevelModifier = 0; // -50 ~ 50
    private boolean trainersLevelModified;
    private int trainersLevelModifier = 0; // -50 ~ 50
    private int eliteFourUniquePokemonNumber = 0; // 0 ~ 2
    private boolean allowTrainerAlternateFormes;
    private boolean swapTrainerMegaEvos;
    private int additionalBossTrainerPokemon = 0;
    private int additionalImportantTrainerPokemon = 0;
    private int additionalRegularTrainerPokemon = 0;
    private boolean randomizeHeldItemsForBossTrainerPokemon;
    private boolean randomizeHeldItemsForImportantTrainerPokemon;
    private boolean randomizeHeldItemsForRegularTrainerPokemon;
    private boolean consumableItemsOnlyForTrainerPokemon;
    private boolean sensibleItemsOnlyForTrainerPokemon;
    private boolean highestLevelOnlyGetsItemsForTrainerPokemon;
    private boolean diverseTypesForBossTrainers;
    private boolean diverseTypesForImportantTrainers;
    private boolean diverseTypesForRegularTrainers;
    private BattleStyle settingBattleStyle = new BattleStyle();
    private boolean shinyChance;
    private boolean betterBossTrainerMovesets;
    private boolean betterImportantTrainerMovesets;
    private boolean betterRegularTrainerMovesets;
    private boolean randomizeWildPokemon;
    public enum WildPokemonZoneMod {
        NONE, ENCOUNTER_SET, MAP, NAMED_LOCATION, GAME
    }
    private WildPokemonZoneMod wildPokemonZoneMod = WildPokemonZoneMod.GAME;
    private boolean splitWildZoneByEncounterTypes;
    
    public enum WildPokemonTypeMod {
        NONE, RANDOM_THEMES, KEEP_PRIMARY
    }
    private WildPokemonTypeMod wildPokemonTypeMod = WildPokemonTypeMod.NONE;
    private boolean keepWildTypeThemes;

    public enum WildPokemonEvolutionMod {
        NONE, BASIC_ONLY, KEEP_STAGE
    }
    private WildPokemonEvolutionMod wildPokemonEvolutionMod = WildPokemonEvolutionMod.NONE;
    private boolean keepWildEvolutionFamilies;

    private boolean similarStrengthEncounters;
    private boolean catchEmAllEncounters;
    private boolean useTimeBasedEncounters;
    private boolean blockWildLegendaries = true;
    private boolean useMinimumCatchRate;
    private int minimumCatchRateLevel = 1;
    private boolean randomizeWildPokemonHeldItems;
    private boolean banBadRandomWildPokemonHeldItems;
    private boolean balanceShakingGrass;
    private boolean wildLevelsModified;
    private int wildLevelModifier = 0;
    private boolean allowWildAltFormes;

    public enum StaticPokemonMod {
        UNCHANGED, RANDOM_MATCHING, COMPLETELY_RANDOM, SIMILAR_STRENGTH
    }

    private StaticPokemonMod staticPokemonMod = StaticPokemonMod.UNCHANGED;

    private boolean allowStaticAltFormes;
    private boolean swapStaticMegaEvos;
    private boolean staticLevelModified;
    private int staticLevelModifier = 0; // -50 ~ 50
    private boolean correctStaticMusic;

    public enum TotemPokemonMod {
        UNCHANGED, RANDOM, SIMILAR_STRENGTH
    }

    public enum AllyPokemonMod {
        UNCHANGED, RANDOM, SIMILAR_STRENGTH
    }

    public enum AuraMod {
        UNCHANGED, RANDOM, SAME_STRENGTH
    }

    private TotemPokemonMod totemPokemonMod = TotemPokemonMod.UNCHANGED;
    private AllyPokemonMod allyPokemonMod = AllyPokemonMod.UNCHANGED;
    private AuraMod auraMod = AuraMod.UNCHANGED;
    private boolean randomizeTotemHeldItems;
    private boolean totemLevelsModified;
    private int totemLevelModifier = 0;
    private boolean allowTotemAltFormes;

    public enum TMsMod {
        UNCHANGED, RANDOM
    }

    private TMsMod tmsMod = TMsMod.UNCHANGED;
    private boolean tmLevelUpMoveSanity;
    private boolean keepFieldMoveTMs;
    private boolean fullHMCompat;
    private boolean tmsForceGoodDamaging;
    private int tmsGoodDamagingPercent = 0;
    private boolean blockBrokenTMMoves;
    private boolean tmsFollowEvolutions;

    public enum TMsHMsCompatibilityMod {
        UNCHANGED, RANDOM_PREFER_TYPE, COMPLETELY_RANDOM, FULL
    }

    private TMsHMsCompatibilityMod tmsHmsCompatibilityMod = TMsHMsCompatibilityMod.UNCHANGED;

    public enum MoveTutorMovesMod {
        UNCHANGED, RANDOM
    }

    private MoveTutorMovesMod moveTutorMovesMod = MoveTutorMovesMod.UNCHANGED;
    private boolean tutorLevelUpMoveSanity;
    private boolean keepFieldMoveTutors;
    private boolean tutorsForceGoodDamaging;
    private int tutorsGoodDamagingPercent = 0;
    private boolean blockBrokenTutorMoves;
    private boolean tutorFollowEvolutions;

    public enum MoveTutorsCompatibilityMod {
        UNCHANGED, RANDOM_PREFER_TYPE, COMPLETELY_RANDOM, FULL
    }

    private MoveTutorsCompatibilityMod moveTutorsCompatibilityMod = MoveTutorsCompatibilityMod.UNCHANGED;

    public enum InGameTradesMod {
        UNCHANGED, RANDOMIZE_GIVEN, RANDOMIZE_GIVEN_AND_REQUESTED
    }

    private InGameTradesMod inGameTradesMod = InGameTradesMod.UNCHANGED;
    private boolean randomizeInGameTradesNicknames;
    private boolean randomizeInGameTradesOTs;
    private boolean randomizeInGameTradesIVs;
    private boolean randomizeInGameTradesItems;

    public enum FieldItemsMod {
        UNCHANGED, SHUFFLE, RANDOM, RANDOM_EVEN
    }

    private FieldItemsMod fieldItemsMod = FieldItemsMod.UNCHANGED;
    private boolean banBadRandomFieldItems;

    public enum ShopItemsMod {
        UNCHANGED, SHUFFLE, RANDOM
    }

    private ShopItemsMod shopItemsMod = ShopItemsMod.UNCHANGED;
    private boolean banBadRandomShopItems;
    private boolean banRegularShopItems;
    private boolean banOPShopItems;
    private boolean guaranteeEvolutionItems;
    private boolean guaranteeXItems;

    private boolean balanceShopPrices;
    private boolean addCheapRareCandiesToShops;

    public enum PickupItemsMod {
        UNCHANGED, RANDOM
    }

    private PickupItemsMod pickupItemsMod = PickupItemsMod.UNCHANGED;
    private boolean banBadRandomPickupItems;

    public enum TypeEffectivenessMod {
        UNCHANGED, RANDOM, RANDOM_BALANCED, KEEP_IDENTITIES, INVERSE
    }

    private TypeEffectivenessMod typeEffectivenessMod = TypeEffectivenessMod.UNCHANGED;
    private boolean inverseTypesRandomImmunities;
    private boolean updateTypeEffectiveness;

    public enum PokemonPalettesMod {
        UNCHANGED, RANDOM
    }

    private PokemonPalettesMod pokemonPalettesMod = PokemonPalettesMod.UNCHANGED;
    private boolean pokemonPalettesFollowTypes;
    private boolean pokemonPalettesFollowEvolutions;
    private boolean pokemonPalettesShinyFromNormal;

    public void writeToFileFormat(FileOutputStream out) throws IOException {
        byte[] settings = toStringWithoutVersion().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(settings.length + 8);
        buf.putInt(VERSION);
        buf.putInt(settings.length);
        buf.put(settings);
        out.write(buf.array());
    }

    public static Settings readFromFileFormat(FileInputStream in) throws IOException, UnsupportedOperationException {
        byte[] versionBytes = new byte[4];
        byte[] lengthBytes = new byte[4];
        int nread = in.read(versionBytes);
        if (nread < 4) {
            throw new UnsupportedOperationException("Error reading version number from settings string.");
        }
        int version = ByteBuffer.wrap(versionBytes).getInt();
        if (((version >> 24) & 0xFF) > 0 && ((version >> 24) & 0xFF) <= 172) {
            throw new UnsupportedOperationException("The settings file is too old to update and cannot be loaded.");
        }
        if (version > VERSION) {
            throw new UnsupportedOperationException("Cannot read settings from a newer version of the randomizer.");
        }
        nread = in.read(lengthBytes);
        if (nread < 4) {
            throw new UnsupportedOperationException("Error reading settings length from settings string.");
        }
        int length = ByteBuffer.wrap(lengthBytes).getInt();
        byte[] buffer = FileFunctions.readFullyIntoBuffer(in, length);
        String settings = new String(buffer, StandardCharsets.UTF_8);

        return fromStringAndVersion(settings, version);
    }

    /**
     * Creates a Settings object from a settings string WITH its first 3 chars being the version ID,
     * of the version the string was created in. Updates the Settings object if needed.
     */
    public static Settings fromString(String withVersion) {
        int version = Integer.parseInt(withVersion.substring(0, 3));
        String withoutVersion = withVersion.substring(3);
        return fromStringAndVersion(withoutVersion, version);
    }

    private static Settings fromStringAndVersion(String s, int version) {
        boolean updated = false;
        if (version < VERSION) {
            updated = true;
            s = new SettingsUpdater().update(version, s);
        }
        Settings settings = fromStringWithoutVersion(s);
        settings.setUpdatedFromOldVersion(updated);
        return settings;
    }

    @Override
    public String toString() {
        return VERSION + toStringWithoutVersion();
    }

    private String toStringWithoutVersion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 0: general options #1 + trainer/class names
        out.write(makeByteSelected(changeImpossibleEvolutions, updateMoves, updateMovesLegacy, randomizeTrainerNames,
                randomizeTrainerClassNames, makeEvolutionsEasier, removeTimeBasedEvolutions, estimateLevelForEvolutionImprovements));

        // 1: pokemon base stats
        out.write(makeByteSelected(baseStatsFollowEvolutions, baseStatisticsMod == BaseStatisticsMod.RANDOM,
                baseStatisticsMod == BaseStatisticsMod.SHUFFLE, baseStatisticsMod == BaseStatisticsMod.UNCHANGED,
                standardizeEXPCurves, updateBaseStats, baseStatsFollowMegaEvolutions, assignEvoStatsRandomly));

        // 2: pokemon types
        out.write(makeByteSelected(speciesTypesMod == SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS,
                speciesTypesMod == SpeciesTypesMod.COMPLETELY_RANDOM, speciesTypesMod == SpeciesTypesMod.UNCHANGED,
                false, false, false, typesFollowMegaEvolutions, dualTypeOnly));

        // 3: abilities
        out.write(makeByteSelected(abilitiesMod == AbilitiesMod.UNCHANGED, abilitiesMod == AbilitiesMod.RANDOMIZE,
                allowWonderGuard, abilitiesFollowEvolutions, banTrappingAbilities, banNegativeAbilities, banBadAbilities,
                abilitiesFollowMegaEvolutions));

        // 4: starter pokemon stuff
        out.write(makeByteSelected(startersMod == StartersMod.CUSTOM, startersMod == StartersMod.COMPLETELY_RANDOM,
                startersMod == StartersMod.UNCHANGED, startersMod == StartersMod.RANDOM_WITH_TWO_EVOLUTIONS,
                randomizeStartersHeldItems, banBadRandomStarterHeldItems, allowStarterAltFormes,
                startersMod == StartersMod.RANDOM_BASIC));

        // 5 - 10: dropdowns
        write2ByteIntBigEndian(out, customStarters[0]);
        write2ByteIntBigEndian(out, customStarters[1]);
        write2ByteIntBigEndian(out, customStarters[2]);

        // 11 movesets
        out.write(makeByteSelected(movesetsMod == MovesetsMod.COMPLETELY_RANDOM,
                movesetsMod == MovesetsMod.RANDOM_PREFER_SAME_TYPE, movesetsMod == MovesetsMod.UNCHANGED,
                movesetsMod == MovesetsMod.METRONOME_ONLY, startWithGuaranteedMoves, reorderDamagingMoves)
                | ((guaranteedMoveCount - 2) << 6));

        // 12 movesets good damaging
        out.write((movesetsForceGoodDamaging ? 0x80 : 0) | movesetsGoodDamagingPercent);

        // 13 trainer pokemon
        out.write(makeByteSelected(trainersMod == TrainersMod.UNCHANGED,
                trainersMod == TrainersMod.RANDOM,
                trainersMod == TrainersMod.DISTRIBUTED,
                trainersMod == TrainersMod.MAINPLAYTHROUGH,
                trainersMod == TrainersMod.TYPE_THEMED,
                trainersMod == TrainersMod.TYPE_THEMED_ELITE4_GYMS,
                trainersMod == TrainersMod.KEEP_THEMED,
                trainersMod == TrainersMod.KEEP_THEME_OR_PRIMARY));
        
        // 14 trainer pokemon evolution level modifier
        out.write(trainersEvolutionLevelModifier + 50);

        // 15 wild pokemon (areas)
        out.write(makeByteSelected(!randomizeWildPokemon,
                wildPokemonZoneMod == WildPokemonZoneMod.NONE,
                wildPokemonZoneMod == WildPokemonZoneMod.ENCOUNTER_SET,
                wildPokemonZoneMod == WildPokemonZoneMod.GAME,
                keepWildEvolutionFamilies,
                wildPokemonZoneMod == WildPokemonZoneMod.NAMED_LOCATION,
                wildPokemonZoneMod == WildPokemonZoneMod.MAP,
                splitWildZoneByEncounterTypes));

        // 16 wild pokemon (restriction)
        out.write(makeByteSelected(false,
                similarStrengthEncounters,
                catchEmAllEncounters,
                false, false, false, false, false));

        // 17 wild pokemon (types/evolutions)
        out.write(makeByteSelected(wildPokemonTypeMod == WildPokemonTypeMod.NONE,
                wildPokemonTypeMod == WildPokemonTypeMod.KEEP_PRIMARY,
                wildPokemonTypeMod == WildPokemonTypeMod.RANDOM_THEMES,
                keepWildTypeThemes,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.NONE,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.BASIC_ONLY,
                wildPokemonEvolutionMod == WildPokemonEvolutionMod.KEEP_STAGE,
                false));

        // 18 wild pokemon (various)
        out.write(makeByteSelected(useTimeBasedEncounters, useMinimumCatchRate,
                blockWildLegendaries, randomizeWildPokemonHeldItems,
                banBadRandomWildPokemonHeldItems, balanceShakingGrass,
                false, false));

        // 19 static pokemon
        out.write(makeByteSelected(staticPokemonMod == StaticPokemonMod.UNCHANGED,
                staticPokemonMod == StaticPokemonMod.RANDOM_MATCHING,
                staticPokemonMod == StaticPokemonMod.COMPLETELY_RANDOM,
                staticPokemonMod == StaticPokemonMod.SIMILAR_STRENGTH,
                limitMainGameLegendaries, limit600, allowStaticAltFormes, swapStaticMegaEvos));

        // 20 tm randomization
        out.write(makeByteSelected(tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.COMPLETELY_RANDOM,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.RANDOM_PREFER_TYPE,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.UNCHANGED, tmsMod == TMsMod.RANDOM,
                tmsMod == TMsMod.UNCHANGED, tmLevelUpMoveSanity, keepFieldMoveTMs,
                tmsHmsCompatibilityMod == TMsHMsCompatibilityMod.FULL));

        // 21 tms part 2
        out.write(makeByteSelected(fullHMCompat, tmsFollowEvolutions, tutorFollowEvolutions));

        // 22 tms good damaging
        out.write((tmsForceGoodDamaging ? 0x80 : 0) | tmsGoodDamagingPercent);

        // 23 move tutor randomization
        out.write(makeByteSelected(moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.COMPLETELY_RANDOM,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.RANDOM_PREFER_TYPE,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.UNCHANGED,
                moveTutorMovesMod == MoveTutorMovesMod.RANDOM, moveTutorMovesMod == MoveTutorMovesMod.UNCHANGED,
                tutorLevelUpMoveSanity, keepFieldMoveTutors,
                moveTutorsCompatibilityMod == MoveTutorsCompatibilityMod.FULL));

        // 24 tutors good damaging
        out.write((tutorsForceGoodDamaging ? 0x80 : 0) | tutorsGoodDamagingPercent);

        // 25 in game trades
        out.write(makeByteSelected(inGameTradesMod == InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED,
                inGameTradesMod == InGameTradesMod.RANDOMIZE_GIVEN, randomizeInGameTradesItems,
                randomizeInGameTradesIVs, randomizeInGameTradesNicknames, randomizeInGameTradesOTs,
                inGameTradesMod == InGameTradesMod.UNCHANGED));

        // 26 field items
        out.write(makeByteSelected(fieldItemsMod == FieldItemsMod.RANDOM, fieldItemsMod == FieldItemsMod.SHUFFLE,
                fieldItemsMod == FieldItemsMod.UNCHANGED, banBadRandomFieldItems, fieldItemsMod == FieldItemsMod.RANDOM_EVEN));

        // 27 move randomizers
        // + static music
        out.write(makeByteSelected(randomizeMovePowers, randomizeMoveAccuracies, randomizeMovePPs, randomizeMoveTypes,
                randomizeMoveCategory, correctStaticMusic));

        // 28 evolutions 1
        out.write(makeByteSelected(evolutionsMod == EvolutionsMod.UNCHANGED, evolutionsMod == EvolutionsMod.RANDOM,
                evosSimilarStrength, evosSameTyping, evosMaxThreeStages, evosForceChange, evosAllowAltFormes,
                evolutionsMod == EvolutionsMod.RANDOM_EVERY_LEVEL));
        
        // 29 pokemon trainer misc
        out.write(makeByteSelected(trainersUsePokemonOfSimilarStrength, 
                rivalCarriesStarterThroughout,
                trainersMatchTypingDistribution,
                trainersBlockLegendaries,
                trainersBlockEarlyWonderGuard,
                swapTrainerMegaEvos,
                shinyChance,
                false));

        // 30 - 33: pokemon restrictions
        try {
            if (currentRestrictions == null) {
                writeFullInt(out, -1);
            } else {
                writeFullInt(out, currentRestrictions.toInt());
            }
        } catch (IOException e) {
            e.printStackTrace(); // better than nothing
        }

        // 34 - 37: misc tweaks
        try {
            // TODO: make misc tweaks little endian. No one likes big endian.
            writeFullIntBigEndian(out, currentMiscTweaks);
        } catch (IOException e) {
            e.printStackTrace(); // better than nothing
        }

        // 38 trainer pokemon level modifier
        out.write((trainersLevelModified ? 0x80 : 0) | (trainersLevelModifier+50));

        // 39 shop items 1
        out.write(makeByteSelected(shopItemsMod == ShopItemsMod.RANDOM, shopItemsMod == ShopItemsMod.SHUFFLE,
                shopItemsMod == ShopItemsMod.UNCHANGED, banBadRandomShopItems, banRegularShopItems, banOPShopItems,
                false, guaranteeEvolutionItems));

        // 40 wild level modifier
        out.write((wildLevelsModified ? 0x80 : 0) | (wildLevelModifier+50));

        // 41 EXP curve mod, block broken moves, alt forme stuff
        out.write(makeByteSelected(
                expCurveMod == ExpCurveMod.LEGENDARIES,
                expCurveMod == ExpCurveMod.STRONG_LEGENDARIES,
                expCurveMod == ExpCurveMod.ALL,
                blockBrokenMovesetMoves,
                blockBrokenTMMoves,
                blockBrokenTutorMoves,
                allowTrainerAlternateFormes,
                allowWildAltFormes));

        // 42 (Legacy Double Battle Mode), Additional Boss/Important Trainer Pokemon, Weigh Duplicate Abilities
        out.write((0) |
                (additionalBossTrainerPokemon << 1) |
                (additionalImportantTrainerPokemon << 4) |
                (weighDuplicateAbilitiesTogether ? 0x80 : 0));

        // 43 Additional Regular Trainer Pokemon, Aura modification, evolution moves, guarantee X items
        out.write(additionalRegularTrainerPokemon |
                ((auraMod == AuraMod.UNCHANGED) ? 0x8 : 0) |
                ((auraMod == AuraMod.RANDOM) ? 0x10 : 0) |
                ((auraMod == AuraMod.SAME_STRENGTH) ? 0x20 : 0) |
                (evolutionMovesForAll ? 0x40 : 0) |
                (guaranteeXItems ? 0x80 : 0));

        // 44 Totem Pokemon settings
        out.write(makeByteSelected(
                totemPokemonMod == TotemPokemonMod.UNCHANGED,
                totemPokemonMod == TotemPokemonMod.RANDOM,
                totemPokemonMod == TotemPokemonMod.SIMILAR_STRENGTH,
                allyPokemonMod == AllyPokemonMod.UNCHANGED,
                allyPokemonMod == AllyPokemonMod.RANDOM,
                allyPokemonMod == AllyPokemonMod.SIMILAR_STRENGTH,
                randomizeTotemHeldItems,
                allowTotemAltFormes));

        // 45 Totem level modifier
        out.write((totemLevelsModified ? 0x80 : 0) | (totemLevelModifier+50));

        // 46 - 47: These two get a byte each for future proofing
        out.write(updateBaseStatsToGeneration);
        out.write(updateMovesToGeneration);

        // 48 Selected EXP curve
        out.write(selectedEXPCurve.toByte());

        // 49 Static level modifier
        out.write((staticLevelModified ? 0x80 : 0) | (staticLevelModifier+50));

        // 50 trainer pokemon held items / pokemon ensure two abilities / trainers use local pokemon
        out.write(makeByteSelected(randomizeHeldItemsForBossTrainerPokemon,
                randomizeHeldItemsForImportantTrainerPokemon,
                randomizeHeldItemsForRegularTrainerPokemon,
                consumableItemsOnlyForTrainerPokemon,
                sensibleItemsOnlyForTrainerPokemon,
                highestLevelOnlyGetsItemsForTrainerPokemon,
                ensureTwoAbilities,
                trainersUseLocalPokemon));

        // 51 pickup item randomization
        out.write(makeByteSelected(pickupItemsMod == PickupItemsMod.RANDOM,
                pickupItemsMod == PickupItemsMod.UNCHANGED, banBadRandomPickupItems,
                banIrregularAltFormes));

        // 52 elite four unique pokemon (3 bits) + catch rate level (3 bits)
        out.write(eliteFourUniquePokemonNumber | ((minimumCatchRateLevel - 1) << 3));

        // 53 starter type mod / starter no legendaries / starter no dual type checkbox
        out.write(makeByteSelected(startersTypeMod == StartersTypeMod.NONE,
                startersTypeMod == StartersTypeMod.FIRE_WATER_GRASS, startersTypeMod == StartersTypeMod.TRIANGLE,
                startersTypeMod == StartersTypeMod.UNIQUE, startersTypeMod == StartersTypeMod.SINGLE_TYPE,
                false, startersNoLegendaries, startersNoDualTypes));

        // 54 starter single-type type choice (5 bits)
        if(startersSingleType != null) {
            out.write(startersSingleType.toInt() + 1);
        } else {
            out.write(0);
        }
        
        // 55 Pokémon palette randomization
        out.write(makeByteSelected(pokemonPalettesMod == PokemonPalettesMod.UNCHANGED,
                pokemonPalettesMod == PokemonPalettesMod.RANDOM,
                pokemonPalettesFollowTypes,
                pokemonPalettesFollowEvolutions,
                pokemonPalettesShinyFromNormal));

        // 56 Type effectiveness
        out.write(makeByteSelected(typeEffectivenessMod == TypeEffectivenessMod.UNCHANGED,
                typeEffectivenessMod == TypeEffectivenessMod.RANDOM,
                typeEffectivenessMod == TypeEffectivenessMod.RANDOM_BALANCED,
                typeEffectivenessMod == TypeEffectivenessMod.KEEP_IDENTITIES,
                typeEffectivenessMod == TypeEffectivenessMod.INVERSE,
                inverseTypesRandomImmunities, updateTypeEffectiveness));

        // 57 evolutions 2
        out.write(makeByteSelected(evosForceGrowth, evosNoConvergence));

        // 58-60 starter BST limits
        byte highEndByte = (byte)(((startersBSTMinimum >> 8) & 0x0F) + ((startersBSTMaximum >> 4) & 0xF0));
        out.write(highEndByte);
        out.write((byte) startersBSTMinimum);
        out.write((byte) startersBSTMaximum);

        // 61 trainer type diversity + better movesets
        out.write(makeByteSelected(diverseTypesForBossTrainers, diverseTypesForImportantTrainers,
                diverseTypesForRegularTrainers, betterBossTrainerMovesets, betterImportantTrainerMovesets,
                betterRegularTrainerMovesets, false, false));

        // 62 setting battle style: modification (3bits) + style (4bits)
        out.write(makeByteSelected(settingBattleStyle.getModification() == BattleStyle.Modification.UNCHANGED,
                settingBattleStyle.getModification() == BattleStyle.Modification.RANDOM,
                settingBattleStyle.getModification() == BattleStyle.Modification.SINGLE_STYLE) |
                (makeByteSelected(settingBattleStyle.getStyle() == BattleStyle.Style.SINGLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.DOUBLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.TRIPLE_BATTLE,
                        settingBattleStyle.getStyle() == BattleStyle.Style.ROTATION_BATTLE) << 3));

        // 63 trainer pokemon evolve
        out.write(makeByteSelected(trainersEvolveTheirPokemon, trainersDoNotGetPrematureEvos));

        // 64 shop items 2
        out.write(makeByteSelected(balanceShopPrices, addCheapRareCandiesToShops,
                false, false, false, false, false, false));

        // 65 general options #2
        out.write(makeByteSelected(randomizeIntroMon, raceMode, false, limitPokemon,
                false, false, false, false));

        // 66 'Make evolutions easier' level select slider
        out.write(makeEvolutionsEasierLvl);

        try {
            byte[] romName = this.romName.getBytes(StandardCharsets.US_ASCII);
            out.write(romName.length);
            out.write(romName);
        } catch (IOException e) {
            out.write(0);
        }

        byte[] current = out.toByteArray();
        CRC32 checksum = new CRC32();
        checksum.update(current);

        try {
            writeFullIntBigEndian(out, (int) checksum.getValue());
            writeFullIntBigEndian(out, CustomNamesSet.getFileChecksum());
        } catch (IOException e) {
            e.printStackTrace(); // better than nothing
        }

        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private static Settings fromStringWithoutVersion(String settingsString) throws IllegalArgumentException {
        byte[] data = Base64.getDecoder().decode(settingsString);
        checkChecksum(data);

        Settings settings = new Settings();

        // Restore the actual controls
        settings.setChangeImpossibleEvolutions(restoreState(data[0], 0));
        settings.setUpdateMoves(restoreState(data[0], 1));
        settings.setUpdateMovesLegacy(restoreState(data[0], 2));
        settings.setRandomizeTrainerNames(restoreState(data[0], 3));
        settings.setRandomizeTrainerClassNames(restoreState(data[0], 4));
        settings.setMakeEvolutionsEasier(restoreState(data[0], 5));
        settings.setRemoveTimeBasedEvolutions(restoreState(data[0], 6));
        settings.setEstimateLevelForEvolutionImprovements(restoreState(data[0], 7));

        settings.setBaseStatisticsMod(restoreEnum(BaseStatisticsMod.class, data[1], 3, // UNCHANGED
                2, // SHUFFLE
                1 // RANDOM
        ));
        settings.setStandardizeEXPCurves(restoreState(data[1], 4));
        settings.setBaseStatsFollowEvolutions(restoreState(data[1], 0));
        settings.setUpdateBaseStats(restoreState(data[1], 5));
        settings.setBaseStatsFollowMegaEvolutions(restoreState(data[1],6));
        settings.setAssignEvoStatsRandomly(restoreState(data[1],7));

        settings.setSpeciesTypesMod(restoreEnum(SpeciesTypesMod.class, data[2], 2, // UNCHANGED
                0, // RANDOM_FOLLOW_EVOLUTIONS
                1 // COMPLETELY_RANDOM
        ));
        settings.setTypesFollowMegaEvolutions(restoreState(data[2],6));
        settings.setDualTypeOnly(restoreState(data[2], 7));

        settings.setAbilitiesMod(restoreEnum(AbilitiesMod.class, data[3], 0, // UNCHANGED
                1 // RANDOMIZE
        ));
        settings.setAllowWonderGuard(restoreState(data[3], 2));
        settings.setAbilitiesFollowEvolutions(restoreState(data[3], 3));
        settings.setBanTrappingAbilities(restoreState(data[3], 4));
        settings.setBanNegativeAbilities(restoreState(data[3], 5));
        settings.setBanBadAbilities(restoreState(data[3], 6));
        settings.setAbilitiesFollowMegaEvolutions(restoreState(data[3],7));

        settings.setStartersMod(restoreEnum(StartersMod.class, data[4], 2, // UNCHANGED
                0, // CUSTOM
                1, // COMPLETELY_RANDOM
                3, // RANDOM_WITH_TWO_EVOLUTIONS
                7  // RANDOM_BASIC
        ));
        settings.setRandomizeStartersHeldItems(restoreState(data[4], 4));
        settings.setBanBadRandomStarterHeldItems(restoreState(data[4], 5));
        settings.setAllowStarterAltFormes(restoreState(data[4],6));

        settings.setCustomStarters(new int[]{FileFunctions.read2ByteInt(data, 5),
                FileFunctions.read2ByteInt(data, 7), FileFunctions.read2ByteInt(data, 9)});

        settings.setMovesetsMod(restoreEnum(MovesetsMod.class, data[11], 2, // UNCHANGED
                1, // RANDOM_PREFER_SAME_TYPE
                0, // COMPLETELY_RANDOM
                3 // METRONOME_ONLY
        ));
        settings.setStartWithGuaranteedMoves(restoreState(data[11], 4));
        settings.setReorderDamagingMoves(restoreState(data[11], 5));
        settings.setGuaranteedMoveCount(((data[11] & 0xC0) >> 6) + 2);

        settings.setMovesetsForceGoodDamaging(restoreState(data[12], 7));
        settings.setMovesetsGoodDamagingPercent(data[12] & 0x7F);

        // changed 160
        settings.setTrainersMod(restoreEnum(TrainersMod.class, data[13], 0, // UNCHANGED
                1, // RANDOM
                2, // DISTRIBUTED
                3, // MAINPLAYTHROUGH 
                4, // TYPE_THEMED
                5, // TYPE_THEMED_ELITE4_GYMS
                6, // KEEP_THEMED
                7  // KEEP_THEME_OR_PRIMARY
        ));
        
        settings.setTrainersEvolutionLevelModifier((data[14] & 0x7F) - 50);

        settings.setRandomizeWildPokemon(!restoreState(data[15], 0));

        settings.setWildPokemonZoneMod(restoreEnum(WildPokemonZoneMod.class, data[15], 1, // RANDOM
                2, // AREA_MAPPING
                6, // MAP_MAPPING
                5, // LOCATION_MAPPING
                3 // GLOBAL_MAPPING
        ));

        settings.setKeepWildEvolutionFamilies(restoreState(data[15], 4));
        settings.setSplitWildZoneByEncounterTypes(restoreState(data[15], 7));

        settings.setSimilarStrengthEncounters(restoreState(data[16], 1));
        settings.setCatchEmAllEncounters(restoreState(data[16], 2));
        settings.setWildPokemonTypeMod(restoreEnum(WildPokemonTypeMod.class, data[17], 0, // NONE
                2, // THEMED_AREAS
                1 // KEEP_PRIMARY
        ));
        settings.setKeepWildTypeThemes(restoreState(data[17], 3));
        settings.setWildPokemonEvolutionMod(restoreEnum(WildPokemonEvolutionMod.class, data[17],
                4, //NONE
                5, //BASIC_ONLY
                6 //KEEP_STAGE
        ));
        
        settings.setUseTimeBasedEncounters(restoreState(data[18], 0));
        settings.setUseMinimumCatchRate(restoreState(data[18], 1));
        settings.setBlockWildLegendaries(restoreState(data[18], 2));
        settings.setRandomizeWildPokemonHeldItems(restoreState(data[18], 3));
        settings.setBanBadRandomWildPokemonHeldItems(restoreState(data[18], 4));
        settings.setBalanceShakingGrass(restoreState(data[18], 5));

        settings.setStaticPokemonMod(restoreEnum(StaticPokemonMod.class, data[19], 0, // UNCHANGED
                1, // RANDOM_MATCHING
                2, // COMPLETELY_RANDOM
                3  // SIMILAR_STRENGTH 
        ));
        
        settings.setLimitMainGameLegendaries(restoreState(data[19], 4));
        settings.setLimit600(restoreState(data[19], 5));
        settings.setAllowStaticAltFormes(restoreState(data[19], 6));
        settings.setSwapStaticMegaEvos(restoreState(data[19], 7));
        
        settings.setTmsMod(restoreEnum(TMsMod.class, data[20], 4, // UNCHANGED
                3 // RANDOM
        ));
        settings.setTmsHmsCompatibilityMod(restoreEnum(TMsHMsCompatibilityMod.class, data[20], 2, // UNCHANGED
                1, // RANDOM_PREFER_TYPE
                0, // COMPLETELY_RANDOM
                7 // FULL
        )); 
        settings.setTmLevelUpMoveSanity(restoreState(data[20], 5));
        settings.setKeepFieldMoveTMs(restoreState(data[20], 6));

        settings.setFullHMCompat(restoreState(data[21], 0));
        settings.setTmsFollowEvolutions(restoreState(data[21], 1));
        settings.setTutorFollowEvolutions(restoreState(data[21], 2));

        settings.setTmsForceGoodDamaging(restoreState(data[22], 7));
        settings.setTmsGoodDamagingPercent(data[22] & 0x7F);

        settings.setMoveTutorMovesMod(restoreEnum(MoveTutorMovesMod.class, data[23], 4, // UNCHANGED
                3 // RANDOM
        ));
        settings.setMoveTutorsCompatibilityMod(restoreEnum(MoveTutorsCompatibilityMod.class, data[23], 2, // UNCHANGED
                1, // RANDOM_PREFER_TYPE
                0, // COMPLETELY_RANDOM
                7 // FULL
        ));
        settings.setTutorLevelUpMoveSanity(restoreState(data[23], 5));
        settings.setKeepFieldMoveTutors(restoreState(data[23], 6));

        settings.setTutorsForceGoodDamaging(restoreState(data[24], 7));
        settings.setTutorsGoodDamagingPercent(data[24] & 0x7F);

        // new 150
        settings.setInGameTradesMod(restoreEnum(InGameTradesMod.class, data[25], 6, // UNCHANGED
                1, // RANDOMIZE_GIVEN
                0 // RANDOMIZE_GIVEN_AND_REQUESTED
        ));
        settings.setRandomizeInGameTradesItems(restoreState(data[25], 2));
        settings.setRandomizeInGameTradesIVs(restoreState(data[25], 3));
        settings.setRandomizeInGameTradesNicknames(restoreState(data[25], 4));
        settings.setRandomizeInGameTradesOTs(restoreState(data[25], 5));

        settings.setFieldItemsMod(restoreEnum(FieldItemsMod.class, data[26],
                2,  // UNCHANGED
                1,  // SHUFFLE
                0,  // RANDOM
                4   // RANDOM_EVEN
        ));
        settings.setBanBadRandomFieldItems(restoreState(data[26], 3));

        // new 170
        settings.setRandomizeMovePowers(restoreState(data[27], 0));
        settings.setRandomizeMoveAccuracies(restoreState(data[27], 1));
        settings.setRandomizeMovePPs(restoreState(data[27], 2));
        settings.setRandomizeMoveTypes(restoreState(data[27], 3));
        settings.setRandomizeMoveCategory(restoreState(data[27], 4));
        settings.setCorrectStaticMusic(restoreState(data[27], 5));

        settings.setEvolutionsMod(restoreEnum(EvolutionsMod.class, data[28], 0, // UNCHANGED
                1, // RANDOM
                7 // RANDOM_EVERY_LEVEL
        ));
        settings.setEvosSimilarStrength(restoreState(data[28], 2));
        settings.setEvosSameTyping(restoreState(data[28], 3));
        settings.setEvosMaxThreeStages(restoreState(data[28], 4));
        settings.setEvosForceChange(restoreState(data[28], 5));
        settings.setEvosAllowAltFormes(restoreState(data[28],6));

        // new pokemon trainer misc
        settings.setTrainersUsePokemonOfSimilarStrength(restoreState(data[29], 0));
        settings.setRivalCarriesStarterThroughout(restoreState(data[29], 1));
        settings.setTrainersMatchTypingDistribution(restoreState(data[29], 2));
        settings.setTrainersBlockLegendaries(restoreState(data[29], 3));
        settings.setTrainersBlockEarlyWonderGuard(restoreState(data[29], 4));
        settings.setSwapTrainerMegaEvos(restoreState(data[29], 5));
        settings.setShinyChance(restoreState(data[29], 6));

        // gen restrictions
        int genLimit = FileFunctions.readFullInt(data, 30);
        GenRestrictions restrictions = new GenRestrictions(genLimit);
        settings.setCurrentRestrictions(restrictions);

        int codeTweaks = FileFunctions.readFullIntBigEndian(data, 34);

        settings.setCurrentMiscTweaks(codeTweaks);

        settings.setTrainersLevelModified(restoreState(data[38], 7));
        settings.setTrainersLevelModifier((data[38] & 0x7F) - 50);
        settings.setShopItemsMod(restoreEnum(ShopItemsMod.class,data[39],
                2,
                1,
                0));
        settings.setBanBadRandomShopItems(restoreState(data[39],3));
        settings.setBanRegularShopItems(restoreState(data[39],4));
        settings.setBanOPShopItems(restoreState(data[39],5));
        settings.setGuaranteeEvolutionItems(restoreState(data[39],7));

        settings.setWildLevelsModified(restoreState(data[40],7));
        settings.setWildLevelModifier((data[40] & 0x7F) - 50);

        settings.setExpCurveMod(restoreEnum(ExpCurveMod.class,data[41],0,1,2));

        settings.setBlockBrokenMovesetMoves(restoreState(data[41],3));
        settings.setBlockBrokenTMMoves(restoreState(data[41],4));
        settings.setBlockBrokenTutorMoves(restoreState(data[41],5));

        settings.setAllowTrainerAlternateFormes(restoreState(data[41],6));
        settings.setAllowWildAltFormes(restoreState(data[41],7));

        // restoreState(data[42], 0))  Legacy setting. This bit used to be used for "Double Battle Only Mode"
        settings.setAdditionalBossTrainerPokemon((data[42] & 0xE) >> 1);
        settings.setAdditionalImportantTrainerPokemon((data[42] & 0x70) >> 4);
        settings.setWeighDuplicateAbilitiesTogether(restoreState(data[42], 7));

        settings.setAdditionalRegularTrainerPokemon((data[43] & 0x7));
        settings.setAuraMod(restoreEnum(AuraMod.class,data[43],3,4,5));
        settings.setEvolutionMovesForAll(restoreState(data[43],6));
        settings.setGuaranteeXItems(restoreState(data[43],7));

        settings.setTotemPokemonMod(restoreEnum(TotemPokemonMod.class,data[44],0,1,2));
        settings.setAllyPokemonMod(restoreEnum(AllyPokemonMod.class,data[44],3,4,5));
        settings.setRandomizeTotemHeldItems(restoreState(data[44],6));
        settings.setAllowTotemAltFormes(restoreState(data[44],7));
        settings.setTotemLevelsModified(restoreState(data[45],7));
        settings.setTotemLevelModifier((data[45] & 0x7F) - 50);

        settings.setUpdateBaseStatsToGeneration(data[46]);

        settings.setUpdateMovesToGeneration(data[47]);

        settings.setSelectedEXPCurve(ExpCurve.fromByte(data[48]));

        settings.setStaticLevelModified(restoreState(data[49],7));
        settings.setStaticLevelModifier((data[49] & 0x7F) - 50);

        settings.setRandomizeHeldItemsForBossTrainerPokemon(restoreState(data[50], 0));
        settings.setRandomizeHeldItemsForImportantTrainerPokemon(restoreState(data[50], 1));
        settings.setRandomizeHeldItemsForRegularTrainerPokemon(restoreState(data[50], 2));
        settings.setConsumableItemsOnlyForTrainers(restoreState(data[50], 3));
        settings.setSensibleItemsOnlyForTrainers(restoreState(data[50], 4));
        settings.setHighestLevelGetsItemsForTrainers(restoreState(data[50], 5));
        settings.setEnsureTwoAbilities(restoreState(data[50], 6));
        settings.setTrainersUseLocalPokemon(restoreState(data[50], 7));

        settings.setPickupItemsMod(restoreEnum(PickupItemsMod.class, data[51],
                1, // UNCHANGED
                0));       // RANDOMIZE
        settings.setBanBadRandomPickupItems(restoreState(data[51], 2));
        settings.setBanIrregularAltFormes(restoreState(data[51], 3));

        settings.setEliteFourUniquePokemonNumber(data[52] & 0x7);
        settings.setMinimumCatchRateLevel(((data[52] & 0x38) >> 3) + 1);

        settings.setStartersTypeMod(restoreEnum(StartersTypeMod.class, data[53], 0, //NONE
                1, //FIRE_WATER_GRASS
                2, //TRIANGLE
                3, //UNIQUE
                4  //SINGLE_TYPE
            ));

        settings.setStartersNoLegendaries(restoreState(data[53], 6));
        settings.setStartersNoDualTypes(restoreState(data[53], 7));

        if(data[54] == 0) {
            settings.setStartersSingleType(null);
        } else {
            settings.setStartersSingleType(Type.fromInt((data[54] & 0x1F) - 1));
        }

        settings.setPokemonPalettesMod(restoreEnum(PokemonPalettesMod.class, data[55], 0, // UNCHANGED
                1 // RANDOM
        ));
        settings.setPokemonPalettesFollowTypes(restoreState(data[55], 2));
        settings.setPokemonPalettesFollowEvolutions(restoreState(data[55], 3));
        settings.setPokemonPalettesShinyFromNormal(restoreState(data[55], 4));

        settings.setTypeEffectivenessMod(restoreEnum(TypeEffectivenessMod.class, data[56], 0, // UNCHANGED
                1, // RANDOM
                2, // RANDOM_BALANCED
                3, // KEEP_IDENTITIES
                4  // REVERSE
        ));
        settings.setInverseTypesRandomImmunities(restoreState(data[56], 5));
        settings.setUpdateTypeEffectiveness(restoreState(data[56], 6));

        settings.setEvosForceGrowth(restoreState(data[57], 0));
        settings.setEvosNoConvergence(restoreState(data[57], 1));

        settings.setStartersBSTMinimum(((Byte.toUnsignedInt(data[58]) & 0x0F) << 8) + Byte.toUnsignedInt(data[59]));
        settings.setStartersBSTMaximum(((Byte.toUnsignedInt(data[58]) & 0xF0) << 4) + Byte.toUnsignedInt(data[60]));

        settings.setDiverseTypesForBossTrainers(restoreState(data[61], 0));
        settings.setDiverseTypesForImportantTrainers(restoreState(data[61], 1));
        settings.setDiverseTypesForRegularTrainers(restoreState(data[61], 2));
        settings.setBetterBossTrainerMovesets(restoreState(data[61], 3));
        settings.setBetterImportantTrainerMovesets(restoreState(data[61], 4));
        settings.setBetterRegularTrainerMovesets(restoreState(data[61], 5));

        settings.settingBattleStyle.setModification(restoreEnum(BattleStyle.Modification.class, data[62], 0, 1, 2));
        settings.settingBattleStyle.setStyle(restoreEnum(BattleStyle.Style.class, data[62], 3, 4, 5, 6));

        settings.setTrainersEvolveTheirPokemon(restoreState(data[63], 0));
        settings.setTrainersDoNotGetPrematureEvos(restoreState(data[63], 1));

        settings.setBalanceShopPrices(restoreState(data[64],0));
        settings.setAddCheapRareCandiesToShops(restoreState(data[64], 1));

        settings.setRandomizeIntroMon(restoreState(data[65], 0));
        settings.setRaceMode(restoreState(data[65], 1));

        settings.setLimitPokemon(restoreState(data[65], 3));
        settings.setMakeEvolutionsEasierLvl(data[66] & 0x7F);

        int romNameLength = data[LENGTH_OF_SETTINGS_DATA] & 0xFF;
        String romName = new String(data, LENGTH_OF_SETTINGS_DATA + 1, romNameLength, StandardCharsets.US_ASCII);
        settings.setRomName(romName);

        return settings;
    }

    public static class TweakForROMFeedback {
        private boolean changedStarter;
        private boolean removedCodeTweaks;

        public boolean isChangedStarter() {
            return changedStarter;
        }

        public TweakForROMFeedback setChangedStarter(boolean changedStarter) {
            this.changedStarter = changedStarter;
            return this;
        }

        public boolean isRemovedCodeTweaks() {
            return removedCodeTweaks;
        }

        public TweakForROMFeedback setRemovedCodeTweaks(boolean removedCodeTweaks) {
            this.removedCodeTweaks = removedCodeTweaks;
            return this;
        }
    }

    public TweakForROMFeedback tweakForRom(RomHandler rh) {

        TweakForROMFeedback feedback = new TweakForROMFeedback();

        if (!rh.canSetIntroPokemon()) {
            this.setRandomizeIntroMon(false);
        }

        // move update check
        if (this.isUpdateMovesLegacy() && rh instanceof Gen5RomHandler) {
            // don't actually update moves
            this.setUpdateMovesLegacy(false);
            this.setUpdateMoves(false);
        }

        // starters
        List<Species> romSpecies;
        if (rh.hasStarterAltFormes()) {
            romSpecies = rh.getSpeciesInclFormes();
        } else {
            romSpecies = rh.getSpecies();
        }
        List<Species> romStarters = rh.getStarters();
        for (int starter = 0; starter < 3; starter++) {
            if (this.customStarters[starter] < 0 || this.customStarters[starter] >= romSpecies.size()) {
                // invalid starter for this game
                feedback.setChangedStarter(true);
                if (starter >= romStarters.size()) {
                    this.customStarters[starter] = 1;
                } else {
                    this.customStarters[starter] = romSpecies.indexOf(romStarters.get(starter));
                }
            }
        }

        // trainers
        if (!rh.canGiveCustomMovesetsToBossTrainers()) {
            this.setBetterBossTrainerMovesets(false);
        }
        if (!rh.canGiveCustomMovesetsToImportantTrainers()) {
            this.setBetterImportantTrainerMovesets(false);
        }
        if (!rh.canGiveCustomMovesetsToRegularTrainers()) {
            this.setBetterRegularTrainerMovesets(false);
        }
        if (!rh.canAddPokemonToBossTrainers()) {
            this.setAdditionalBossTrainerPokemon(0);
        }
        if (!rh.canAddPokemonToImportantTrainers()) {
            this.setAdditionalImportantTrainerPokemon(0);
        }
        if (!rh.canAddPokemonToRegularTrainers()) {
            this.setAdditionalRegularTrainerPokemon(0);
        }
        if (!rh.canAddHeldItemsToBossTrainers()) {
            this.setRandomizeHeldItemsForBossTrainerPokemon(false);
        }
        if (!rh.canAddHeldItemsToImportantTrainers()) {
            this.setRandomizeHeldItemsForImportantTrainerPokemon(false);
        }
        if (!rh.canAddHeldItemsToRegularTrainers()) {
            this.setRandomizeHeldItemsForRegularTrainerPokemon(false);
        }

        // gen restrictions
        if (rh instanceof Gen1RomHandler || (rh instanceof Gen3RomHandler && !rh.isRomValid(null))) {
            this.currentRestrictions = null;
            this.setLimitPokemon(false);
        } else {
            this.currentRestrictions.limitToGen(rh.generationOfPokemon());
        }

        // gen 5 exclusive stuff
        if (rh.generationOfPokemon() != 5) {
            if (trainersMod == TrainersMod.MAINPLAYTHROUGH) {
                trainersMod = TrainersMod.RANDOM;
            }
        }

        // misc tweaks
        int oldMiscTweaks = this.currentMiscTweaks;
        this.currentMiscTweaks &= rh.miscTweaksAvailable();

        if (oldMiscTweaks != this.currentMiscTweaks) {
            feedback.setRemovedCodeTweaks(true);
        }

        if (rh.abilitiesPerSpecies() == 0) {
            this.setAbilitiesMod(AbilitiesMod.UNCHANGED);
            this.setAllowWonderGuard(false);
        }

        if (!rh.supportsStarterHeldItems()) {
            // starter held items don't exist
            this.setRandomizeStartersHeldItems(false);
            this.setBanBadRandomStarterHeldItems(false);
        }

        if (!rh.supportsFourStartingMoves()) {
            this.setStartWithGuaranteedMoves(false);
        }

        if (rh instanceof Gen1RomHandler || rh instanceof Gen2RomHandler) {
            this.setTrainersBlockEarlyWonderGuard(false);
        }

        if (!rh.hasTimeBasedEncounters()) {
            this.setUseTimeBasedEncounters(false);
        }

        if (rh instanceof Gen1RomHandler) {
            this.setRandomizeWildPokemonHeldItems(false);
            this.setBanBadRandomWildPokemonHeldItems(false);
        }

        if (!rh.canChangeStaticPokemon()) {
            this.setStaticPokemonMod(StaticPokemonMod.UNCHANGED);
        }

        if (!rh.hasMoveTutors()) {
            this.setMoveTutorMovesMod(MoveTutorMovesMod.UNCHANGED);
            this.setMoveTutorsCompatibilityMod(MoveTutorsCompatibilityMod.UNCHANGED);
            this.setTutorLevelUpMoveSanity(false);
            this.setKeepFieldMoveTutors(false);
        }

        if (rh instanceof Gen1RomHandler) {
            // missing some ingame trade fields
            this.setRandomizeInGameTradesItems(false);
            this.setRandomizeInGameTradesIVs(false);
            this.setRandomizeInGameTradesOTs(false);
        }

        if (!rh.hasPhysicalSpecialSplit()) {
            this.setRandomizeMoveCategory(false);
        }

        if (!rh.hasShopSupport()) {
            this.setShopItemsMod(ShopItemsMod.UNCHANGED);
            this.setBalanceShopPrices(false);
        }

        if (!rh.canChangeShopSizes()) {
            this.setAddCheapRareCandiesToShops(false);
        }

        // done
        return feedback;
    }

    // getters and setters

    public CustomNamesSet getCustomNames() {
        return customNames;
    }

    public Settings setCustomNames(CustomNamesSet customNames) {
        this.customNames = customNames;
        return this;
    }

    public String getRomName() {
        return romName;
    }

    public void setRomName(String romName) {
        this.romName = romName;
    }

    public boolean isUpdatedFromOldVersion() {
        return updatedFromOldVersion;
    }

    private void setUpdatedFromOldVersion(boolean updatedFromOldVersion) {
        this.updatedFromOldVersion = updatedFromOldVersion;
    }

    public GenRestrictions getCurrentRestrictions() {
        return currentRestrictions;
    }

    public void setCurrentRestrictions(GenRestrictions currentRestrictions) {
        this.currentRestrictions = currentRestrictions;
    }

    public int getCurrentMiscTweaks() {
        return currentMiscTweaks;
    }

    public void setCurrentMiscTweaks(int currentMiscTweaks) {
        this.currentMiscTweaks = currentMiscTweaks;
    }

    public boolean isUpdateMoves() {
        return updateMoves;
    }

    public void setUpdateMoves(boolean updateMoves) {
        this.updateMoves = updateMoves;
    }

    public boolean isUpdateMovesLegacy() {
        return updateMovesLegacy;
    }

    public void setUpdateMovesLegacy(boolean updateMovesLegacy) {
        this.updateMovesLegacy = updateMovesLegacy;
    }

    public int getUpdateMovesToGeneration() {
        return updateMovesToGeneration;
    }

    public void setUpdateMovesToGeneration(int generation) {
        updateMovesToGeneration = generation;
    }

    public boolean isChangeImpossibleEvolutions() {
        return changeImpossibleEvolutions;
    }

    public boolean useEstimatedLevelsForEvolutionImprovements() {
        return estimateLevelForEvolutionImprovements;
    }

    public boolean isDualTypeOnly(){
        return dualTypeOnly;
    }

    public void setDualTypeOnly(boolean dualTypeOnly){
        this.dualTypeOnly = dualTypeOnly;
    }

    public void setChangeImpossibleEvolutions(boolean changeImpossibleEvolutions) {
        this.changeImpossibleEvolutions = changeImpossibleEvolutions;
    }

    public void setEstimateLevelForEvolutionImprovements(boolean estimateLevelForEvolutionImprovements) {
        this.estimateLevelForEvolutionImprovements = estimateLevelForEvolutionImprovements;
    }

    public boolean isMakeEvolutionsEasier() {
        return makeEvolutionsEasier;
    }

    public void setMakeEvolutionsEasier(boolean makeEvolutionsEasier) {
        this.makeEvolutionsEasier = makeEvolutionsEasier;
    }

    public int getMakeEvolutionsEasierLvl() {
        return makeEvolutionsEasierLvl;
    }

    public void setMakeEvolutionsEasierLvl(int makeEvolutionsEasierLvl) {
        this.makeEvolutionsEasierLvl = makeEvolutionsEasierLvl;
    }

    public boolean isRemoveTimeBasedEvolutions() {
        return removeTimeBasedEvolutions;
    }

    public void setRemoveTimeBasedEvolutions(boolean removeTimeBasedEvolutions) {
        this.removeTimeBasedEvolutions = removeTimeBasedEvolutions;
    }

    public boolean isRaceMode() {
        return raceMode;
    }

    public void setRaceMode(boolean raceMode) {
        this.raceMode = raceMode;
    }

    public boolean isBanIrregularAltFormes() {
        return banIrregularAltFormes;
    }

    public void setBanIrregularAltFormes(boolean banIrregularAltFormes) {
        this.banIrregularAltFormes = banIrregularAltFormes;
    }

    public boolean isRandomizeIntroMon() {
        return randomizeIntroMon;
    }

    public void setRandomizeIntroMon(boolean randomizeIntroMon) {
        this.randomizeIntroMon = randomizeIntroMon;
    }

    public boolean isLimitPokemon() {
        return limitPokemon;
    }

    public void setLimitPokemon(boolean limitPokemon) {
        this.limitPokemon = limitPokemon;
    }

    public BaseStatisticsMod getBaseStatisticsMod() {
        return baseStatisticsMod;
    }

    public void setBaseStatisticsMod(boolean... bools) {
        setBaseStatisticsMod(getEnum(BaseStatisticsMod.class, bools));
    }

    public void setBaseStatisticsMod(BaseStatisticsMod baseStatisticsMod) {
        this.baseStatisticsMod = baseStatisticsMod;
    }

    public boolean isBaseStatsFollowEvolutions() {
        return baseStatsFollowEvolutions;
    }

    public void setBaseStatsFollowEvolutions(boolean baseStatsFollowEvolutions) {
        this.baseStatsFollowEvolutions = baseStatsFollowEvolutions;
    }

    public boolean isBaseStatsFollowMegaEvolutions() {
        return baseStatsFollowMegaEvolutions;
    }

    public void setBaseStatsFollowMegaEvolutions(boolean baseStatsFollowMegaEvolutions) {
        this.baseStatsFollowMegaEvolutions = baseStatsFollowMegaEvolutions;
    }

    public boolean isAssignEvoStatsRandomly() {
        return assignEvoStatsRandomly;
    }

    public void setAssignEvoStatsRandomly(boolean assignEvoStatsRandomly) {
        this.assignEvoStatsRandomly = assignEvoStatsRandomly;
    }


    public boolean isStandardizeEXPCurves() {
        return standardizeEXPCurves;
    }

    public void setStandardizeEXPCurves(boolean standardizeEXPCurves) {
        this.standardizeEXPCurves = standardizeEXPCurves;
    }

    public ExpCurveMod getExpCurveMod() {
        return expCurveMod;
    }

    public void setExpCurveMod(boolean... bools) {
        setExpCurveMod(getEnum(ExpCurveMod.class, bools));
    }

    public void setExpCurveMod(ExpCurveMod expCurveMod) {
        this.expCurveMod = expCurveMod;
    }

    public ExpCurve getSelectedEXPCurve() {
        return selectedEXPCurve;
    }

    public void setSelectedEXPCurve(ExpCurve expCurve) {
        this.selectedEXPCurve = expCurve;
    }

    public boolean isUpdateBaseStats() {
        return updateBaseStats;
    }

    public void setUpdateBaseStats(boolean updateBaseStats) {
        this.updateBaseStats = updateBaseStats;
    }

    public int getUpdateBaseStatsToGeneration() {
        return updateBaseStatsToGeneration;
    }

    public void setUpdateBaseStatsToGeneration(int generation) {
        this.updateBaseStatsToGeneration = generation;
    }

    public AbilitiesMod getAbilitiesMod() {
        return abilitiesMod;
    }

    public void setAbilitiesMod(boolean... bools) {
        setAbilitiesMod(getEnum(AbilitiesMod.class, bools));
    }

    public void setAbilitiesMod(AbilitiesMod abilitiesMod) {
        this.abilitiesMod = abilitiesMod;
    }

    public boolean isAllowWonderGuard() {
        return allowWonderGuard;
    }

    public void setAllowWonderGuard(boolean allowWonderGuard) {
        this.allowWonderGuard = allowWonderGuard;
    }

    public boolean isAbilitiesFollowEvolutions() {
        return abilitiesFollowEvolutions;
    }

    public void setAbilitiesFollowEvolutions(boolean abilitiesFollowEvolutions) {
        this.abilitiesFollowEvolutions = abilitiesFollowEvolutions;
    }

    public boolean isAbilitiesFollowMegaEvolutions() {
        return abilitiesFollowMegaEvolutions;
    }

    public void setAbilitiesFollowMegaEvolutions(boolean abilitiesFollowMegaEvolutions) {
        this.abilitiesFollowMegaEvolutions = abilitiesFollowMegaEvolutions;
    }

    public boolean isBanTrappingAbilities() {
        return banTrappingAbilities;
    }

    public void setBanTrappingAbilities(boolean banTrappingAbilities) {
        this.banTrappingAbilities = banTrappingAbilities;
    }

    public boolean isBanNegativeAbilities() {
        return banNegativeAbilities;
    }

    public void setBanNegativeAbilities(boolean banNegativeAbilities) {
        this.banNegativeAbilities = banNegativeAbilities;
    }

    public boolean isBanBadAbilities() {
        return banBadAbilities;
    }

    public void setBanBadAbilities(boolean banBadAbilities) {
        this.banBadAbilities = banBadAbilities;
    }

    public boolean isWeighDuplicateAbilitiesTogether() {
        return weighDuplicateAbilitiesTogether;
    }

    public void setWeighDuplicateAbilitiesTogether(boolean weighDuplicateAbilitiesTogether) {
        this.weighDuplicateAbilitiesTogether = weighDuplicateAbilitiesTogether;
    }

    public boolean isEnsureTwoAbilities() { return ensureTwoAbilities; }

    public void setEnsureTwoAbilities(boolean ensureTwoAbilities) {
        this.ensureTwoAbilities = ensureTwoAbilities;
    }

    public StartersMod getStartersMod() {
        return startersMod;
    }

    public void setStartersMod(boolean... bools) {
        setStartersMod(getEnum(StartersMod.class, bools));
    }

    public void setStartersMod(StartersMod startersMod) {
        this.startersMod = startersMod;
    }

    public StartersTypeMod getStartersTypeMod() {
        return startersTypeMod;
    }

    public void setStartersTypeMod(boolean... bools) {
        setStartersTypeMod(getEnum(StartersTypeMod.class, bools));
    }

    public void setStartersTypeMod(StartersTypeMod startersTypeMod) {
        this.startersTypeMod = startersTypeMod;
    }

    public boolean isStartersNoDualTypes() {
        return startersNoDualTypes;
    }

    public void setStartersNoDualTypes(boolean startersNoDualTypes) {
        this.startersNoDualTypes = startersNoDualTypes;
    }

    public boolean isStartersNoLegendaries() {
        return startersNoLegendaries;
    }

    public void setStartersNoLegendaries(boolean startersNoLegendaries) {
        this.startersNoLegendaries = startersNoLegendaries;
    }

    public Type getStartersSingleType() {
        return startersSingleType;
    }

    private void setStartersSingleType(Type type) {
        startersSingleType = type;
    }

    public void setStartersSingleType(int typeIndex) {
        if(typeIndex == 0) {
            startersSingleType = null;
        } else {
            startersSingleType = Type.fromInt(typeIndex - 1);
        }
    }

    public int[] getCustomStarters() {
        return customStarters;
    }

    public void setCustomStarters(int[] customStarters) {
        this.customStarters = customStarters;
    }

    public boolean isRandomizeStartersHeldItems() {
        return randomizeStartersHeldItems;
    }

    public void setRandomizeStartersHeldItems(boolean randomizeStartersHeldItems) {
        this.randomizeStartersHeldItems = randomizeStartersHeldItems;
    }

    public boolean isBanBadRandomStarterHeldItems() {
        return banBadRandomStarterHeldItems;
    }

    public void setBanBadRandomStarterHeldItems(boolean banBadRandomStarterHeldItems) {
        this.banBadRandomStarterHeldItems = banBadRandomStarterHeldItems;
    }

    public boolean isAllowStarterAltFormes() {
        return allowStarterAltFormes;
    }

    public void setAllowStarterAltFormes(boolean allowStarterAltFormes) {
        this.allowStarterAltFormes = allowStarterAltFormes;
    }

    public int getStartersBSTMinimum() {
        return startersBSTMinimum;
    }

    public void setStartersBSTMinimum(int startersBSTMinimum) {
        this.startersBSTMinimum = startersBSTMinimum;
    }

    public int getStartersBSTMaximum() {
        return startersBSTMaximum;
    }

    public void setStartersBSTMaximum(int startersBSTMaximum) {
        this.startersBSTMaximum = startersBSTMaximum;
    }
    
    public SpeciesTypesMod getSpeciesTypesMod() {
        return speciesTypesMod;
    }

    public void setSpeciesTypesMod(boolean... bools) {
        setSpeciesTypesMod(getEnum(SpeciesTypesMod.class, bools));
    }

    public void setSpeciesTypesMod(SpeciesTypesMod speciesTypesMod) {
        this.speciesTypesMod = speciesTypesMod;
    }

    public boolean isTypesFollowMegaEvolutions() {
        return typesFollowMegaEvolutions;
    }

    public void setTypesFollowMegaEvolutions(boolean typesFollowMegaEvolutions) {
        this.typesFollowMegaEvolutions = typesFollowMegaEvolutions;
    }

    public EvolutionsMod getEvolutionsMod() {
        return evolutionsMod;
    }

    public void setEvolutionsMod(boolean... bools) {
        setEvolutionsMod(getEnum(EvolutionsMod.class, bools));
    }

    public void setEvolutionsMod(EvolutionsMod evolutionsMod) {
        this.evolutionsMod = evolutionsMod;
    }

    public boolean isEvosSimilarStrength() {
        return evosSimilarStrength;
    }

    public void setEvosSimilarStrength(boolean evosSimilarStrength) {
        this.evosSimilarStrength = evosSimilarStrength;
    }

    public boolean isEvosSameTyping() {
        return evosSameTyping;
    }

    public void setEvosSameTyping(boolean evosSameTyping) {
        this.evosSameTyping = evosSameTyping;
    }

    public boolean isEvosMaxThreeStages() {
        return evosMaxThreeStages;
    }

    public void setEvosMaxThreeStages(boolean evosMaxThreeStages) {
        this.evosMaxThreeStages = evosMaxThreeStages;
    }

    public boolean isEvosForceChange() {
        return evosForceChange;
    }

    public void setEvosForceChange(boolean evosForceChange) {
        this.evosForceChange = evosForceChange;
    }

    public boolean isEvosAllowAltFormes() {
        return evosAllowAltFormes;
    }

    public void setEvosAllowAltFormes(boolean evosAllowAltFormes) {
        this.evosAllowAltFormes = evosAllowAltFormes;
    }

    public boolean isEvosForceGrowth() {
        return evosForceGrowth;
    }

    public void setEvosForceGrowth(boolean evosForceGrowth) {
        this.evosForceGrowth = evosForceGrowth;
    }

    public boolean isEvosNoConvergence() {
        return evosNoConvergence;
    }

    public void setEvosNoConvergence(boolean evosNoConvergence) {
        this.evosNoConvergence = evosNoConvergence;
    }

    public boolean isRandomizeMovePowers() {
        return randomizeMovePowers;
    }

    public void setRandomizeMovePowers(boolean randomizeMovePowers) {
        this.randomizeMovePowers = randomizeMovePowers;
    }

    public boolean isRandomizeMoveAccuracies() {
        return randomizeMoveAccuracies;
    }

    public void setRandomizeMoveAccuracies(boolean randomizeMoveAccuracies) {
        this.randomizeMoveAccuracies = randomizeMoveAccuracies;
    }

    public boolean isRandomizeMovePPs() {
        return randomizeMovePPs;
    }

    public void setRandomizeMovePPs(boolean randomizeMovePPs) {
        this.randomizeMovePPs = randomizeMovePPs;
    }

    public boolean isRandomizeMoveTypes() {
        return randomizeMoveTypes;
    }

    public void setRandomizeMoveTypes(boolean randomizeMoveTypes) {
        this.randomizeMoveTypes = randomizeMoveTypes;
    }

    public boolean isRandomizeMoveCategory() {
        return randomizeMoveCategory;
    }

    public void setRandomizeMoveCategory(boolean randomizeMoveCategory) {
        this.randomizeMoveCategory = randomizeMoveCategory;
    }

    public MovesetsMod getMovesetsMod() {
        return movesetsMod;
    }

    public void setMovesetsMod(boolean... bools) {
        setMovesetsMod(getEnum(MovesetsMod.class, bools));
    }

    public void setMovesetsMod(MovesetsMod movesetsMod) {
        this.movesetsMod = movesetsMod;
    }

    public boolean isStartWithGuaranteedMoves() {
        return startWithGuaranteedMoves;
    }

    public void setStartWithGuaranteedMoves(boolean startWithGuaranteedMoves) {
        this.startWithGuaranteedMoves = startWithGuaranteedMoves;
    }

    public int getGuaranteedMoveCount() {
        return guaranteedMoveCount;
    }

    public void setGuaranteedMoveCount(int guaranteedMoveCount) {
        this.guaranteedMoveCount = guaranteedMoveCount;
    }

    public boolean isReorderDamagingMoves() {
        return reorderDamagingMoves;
    }

    public void setReorderDamagingMoves(boolean reorderDamagingMoves) {
        this.reorderDamagingMoves = reorderDamagingMoves;
    }

    public boolean isMovesetsForceGoodDamaging() {
        return movesetsForceGoodDamaging;
    }

    public void setMovesetsForceGoodDamaging(boolean movesetsForceGoodDamaging) {
        this.movesetsForceGoodDamaging = movesetsForceGoodDamaging;
    }

    public int getMovesetsGoodDamagingPercent() {
        return movesetsGoodDamagingPercent;
    }

    public void setMovesetsGoodDamagingPercent(int movesetsGoodDamagingPercent) {
        this.movesetsGoodDamagingPercent = movesetsGoodDamagingPercent;
    }

    public boolean isBlockBrokenMovesetMoves() {
        return blockBrokenMovesetMoves;
    }

    public void setBlockBrokenMovesetMoves(boolean blockBrokenMovesetMoves) {
        this.blockBrokenMovesetMoves = blockBrokenMovesetMoves;
    }

    public boolean isEvolutionMovesForAll() {
        return evolutionMovesForAll;
    }

    public void setEvolutionMovesForAll(boolean evolutionMovesForAll) {
        this.evolutionMovesForAll = evolutionMovesForAll;
    }

    public TrainersMod getTrainersMod() {
        return trainersMod;
    }

    public void setTrainersMod(boolean... bools) {
        setTrainersMod(getEnum(TrainersMod.class, bools));
    }

    public void setTrainersMod(TrainersMod trainersMod) {
        this.trainersMod = trainersMod;
    }

    public boolean isRivalCarriesStarterThroughout() {
        return rivalCarriesStarterThroughout;
    }

    public void setRivalCarriesStarterThroughout(boolean rivalCarriesStarterThroughout) {
        this.rivalCarriesStarterThroughout = rivalCarriesStarterThroughout;
    }

    public boolean isTrainersUsePokemonOfSimilarStrength() {
        return trainersUsePokemonOfSimilarStrength;
    }

    public void setTrainersUsePokemonOfSimilarStrength(boolean trainersUsePokemonOfSimilarStrength) {
        this.trainersUsePokemonOfSimilarStrength = trainersUsePokemonOfSimilarStrength;
    }

    public boolean isTrainersDoNotGetPrematureEvos() {
        return trainersDoNotGetPrematureEvos;
    }

    public void setTrainersDoNotGetPrematureEvos(boolean trainersDoNotGetPrematureEvos) {
        this.trainersDoNotGetPrematureEvos = trainersDoNotGetPrematureEvos;
    }

    public boolean isTrainersMatchTypingDistribution() {
        return trainersMatchTypingDistribution;
    }

    public void setTrainersMatchTypingDistribution(boolean trainersMatchTypingDistribution) {
        this.trainersMatchTypingDistribution = trainersMatchTypingDistribution;
    }

    public boolean isTrainersBlockLegendaries() {
        return trainersBlockLegendaries;
    }

    public void setTrainersBlockLegendaries(boolean trainersBlockLegendaries) {
        this.trainersBlockLegendaries = trainersBlockLegendaries;
    }

    public boolean isTrainersUseLocalPokemon() {
        return trainersUseLocalPokemon;
    }

    public void setTrainersUseLocalPokemon(boolean trainersUseLocalPokemon) {
        this.trainersUseLocalPokemon = trainersUseLocalPokemon;
    }

    public boolean isTrainersEnforceDistribution() {
        return trainersEnforceDistribution;
    }

    public Settings setTrainersEnforceDistribution(boolean trainersEnforceDistribution) {
        this.trainersEnforceDistribution = trainersEnforceDistribution;
        return this;
    }
    
    public boolean isTrainersEnforceMainPlaythrough() {
        return trainersEnforceMainPlaythrough;
    }

    public Settings setTrainersEnforceMainPlaythrough(boolean trainersEnforceMainPlaythrough) {
        this.trainersEnforceMainPlaythrough = trainersEnforceMainPlaythrough;
        return this;
    }

    
    public boolean isTrainersBlockEarlyWonderGuard() {
        return trainersBlockEarlyWonderGuard;
    }

    public void setTrainersBlockEarlyWonderGuard(boolean trainersBlockEarlyWonderGuard) {
        this.trainersBlockEarlyWonderGuard = trainersBlockEarlyWonderGuard;
    }

    public boolean isRandomizeTrainerNames() {
        return randomizeTrainerNames;
    }

    public void setRandomizeTrainerNames(boolean randomizeTrainerNames) {
        this.randomizeTrainerNames = randomizeTrainerNames;
    }

    public boolean isRandomizeTrainerClassNames() {
        return randomizeTrainerClassNames;
    }

    public void setRandomizeTrainerClassNames(boolean randomizeTrainerClassNames) {
        this.randomizeTrainerClassNames = randomizeTrainerClassNames;
    }

    public boolean isTrainersEvolveTheirPokemon() {
        return trainersEvolveTheirPokemon;
    }

    public void setTrainersEvolveTheirPokemon(boolean trainersEvolveTheirPokemon) {
        this.trainersEvolveTheirPokemon = trainersEvolveTheirPokemon;
    }

    public int getTrainersEvolutionLevelModifier() {
        return trainersEvolutionLevelModifier;
    }

    public void setTrainersEvolutionLevelModifier(int trainersEvolutionLevelModifier) {
        this.trainersEvolutionLevelModifier = trainersEvolutionLevelModifier;
    }

    public boolean isTrainersLevelModified() {
        return trainersLevelModified;
    }

    public void setTrainersLevelModified(boolean trainersLevelModified) {
        this.trainersLevelModified = trainersLevelModified;
    }

    public int getTrainersLevelModifier() {
        return trainersLevelModifier;
    }

    public void setTrainersLevelModifier(int trainersLevelModifier) {
        this.trainersLevelModifier = trainersLevelModifier;
    }

    public int getEliteFourUniquePokemonNumber() {
        return eliteFourUniquePokemonNumber;
    }

    public void setEliteFourUniquePokemonNumber(int eliteFourUniquePokemonNumber) {
        this.eliteFourUniquePokemonNumber = eliteFourUniquePokemonNumber;
    }


    public boolean isAllowTrainerAlternateFormes() {
        return allowTrainerAlternateFormes;
    }

    public void setAllowTrainerAlternateFormes(boolean allowTrainerAlternateFormes) {
        this.allowTrainerAlternateFormes = allowTrainerAlternateFormes;
    }

    public boolean isSwapTrainerMegaEvos() {
        return swapTrainerMegaEvos;
    }

    public void setSwapTrainerMegaEvos(boolean swapTrainerMegaEvos) {
        this.swapTrainerMegaEvos = swapTrainerMegaEvos;
    }

    public int getAdditionalBossTrainerPokemon() {
        return additionalBossTrainerPokemon;
    }

    public void setAdditionalBossTrainerPokemon(int additional) {
        this.additionalBossTrainerPokemon = additional;
    }

    public int getAdditionalImportantTrainerPokemon() {
        return additionalImportantTrainerPokemon;
    }

    public void setAdditionalImportantTrainerPokemon(int additional) {
        this.additionalImportantTrainerPokemon = additional;
    }

    public int getAdditionalRegularTrainerPokemon() {
        return additionalRegularTrainerPokemon;
    }

    public void setAdditionalRegularTrainerPokemon(int additional) {
        this.additionalRegularTrainerPokemon = additional;
    }

    public boolean isRandomizeHeldItemsForBossTrainerPokemon() {
        return randomizeHeldItemsForBossTrainerPokemon;
    }

    public void setRandomizeHeldItemsForBossTrainerPokemon(boolean bossTrainers) {
        this.randomizeHeldItemsForBossTrainerPokemon = bossTrainers;
    }

    public boolean isRandomizeHeldItemsForImportantTrainerPokemon() {
        return randomizeHeldItemsForImportantTrainerPokemon;
    }

    public void setRandomizeHeldItemsForImportantTrainerPokemon(boolean importantTrainers) {
        this.randomizeHeldItemsForImportantTrainerPokemon = importantTrainers;
    }

    public boolean isRandomizeHeldItemsForRegularTrainerPokemon() {
        return randomizeHeldItemsForRegularTrainerPokemon;
    }

    public void setRandomizeHeldItemsForRegularTrainerPokemon(boolean regularTrainers) {
        this.randomizeHeldItemsForRegularTrainerPokemon = regularTrainers;
    }

    public boolean isConsumableItemsOnlyForTrainers() {
        return consumableItemsOnlyForTrainerPokemon;
    }

    public void setConsumableItemsOnlyForTrainers(boolean consumableOnly) {
        this.consumableItemsOnlyForTrainerPokemon = consumableOnly;
    }

    public boolean isSensibleItemsOnlyForTrainers() {
        return sensibleItemsOnlyForTrainerPokemon;
    }

    public void setSensibleItemsOnlyForTrainers(boolean sensibleOnly) {
        this.sensibleItemsOnlyForTrainerPokemon = sensibleOnly;
    }

    public boolean isHighestLevelGetsItemsForTrainers() {
        return highestLevelOnlyGetsItemsForTrainerPokemon;
    }

    public void setHighestLevelGetsItemsForTrainers(boolean highestOnly) {
        this.highestLevelOnlyGetsItemsForTrainerPokemon = highestOnly;
    }

    public boolean isDiverseTypesForBossTrainers() {
        return diverseTypesForBossTrainers;
    }

    public void setDiverseTypesForBossTrainers(boolean isBossDiverse) {
        this.diverseTypesForBossTrainers = isBossDiverse;
    }

    public boolean isDiverseTypesForImportantTrainers() {
        return diverseTypesForImportantTrainers;
    }

    public void setDiverseTypesForImportantTrainers(boolean isImportantDiverse) {
        this.diverseTypesForImportantTrainers = isImportantDiverse;
    }

    public boolean isDiverseTypesForRegularTrainers() {
        return diverseTypesForRegularTrainers;
    }

    public void setDiverseTypesForRegularTrainers(boolean isRegularDiverse) {
        this.diverseTypesForRegularTrainers = isRegularDiverse;
    }

    public BattleStyle getBattleStyle() {
        return settingBattleStyle;
    }

    public void setBattleStyle(BattleStyle style) {
        settingBattleStyle = style;
    }

    public void setBattleStyleMod(boolean... bools) {
        settingBattleStyle.setModification(getEnum(BattleStyle.Modification.class, bools));
    }

    public void setSingleStyleSelection(boolean... bools) {
        settingBattleStyle.setStyle(getEnum(BattleStyle.Style.class, bools));
    }

    public boolean isShinyChance() {
        return shinyChance;
    }

    public void setShinyChance(boolean shinyChance) {
        this.shinyChance = shinyChance;
    }

    public boolean isBetterBossTrainerMovesets() {
        return betterBossTrainerMovesets;
    }

    public void setBetterBossTrainerMovesets(boolean betterBossTrainerMovesets) {
        this.betterBossTrainerMovesets = betterBossTrainerMovesets;
    }

    public boolean isBetterImportantTrainerMovesets() {
        return betterImportantTrainerMovesets;
    }

    public void setBetterImportantTrainerMovesets(boolean betterImportantTrainerMovesets) {
        this.betterImportantTrainerMovesets = betterImportantTrainerMovesets;
    }

    public boolean isBetterRegularTrainerMovesets() {
        return betterRegularTrainerMovesets;
    }

    public void setBetterRegularTrainerMovesets(boolean betterRegularTrainerMovesets) {
        this.betterRegularTrainerMovesets = betterRegularTrainerMovesets;
    }

    public boolean isRandomizeWildPokemon() {
        return randomizeWildPokemon;
    }

    public void setRandomizeWildPokemon(boolean randomizeWildPokemon) {
        this.randomizeWildPokemon = randomizeWildPokemon;
    }

    public WildPokemonZoneMod getWildPokemonZoneMod() {
        return wildPokemonZoneMod;
    }

    public void setWildPokemonZoneMod(boolean... bools) {
        setWildPokemonZoneMod(getEnum(WildPokemonZoneMod.class, bools));
    }

    public void setWildPokemonZoneMod(WildPokemonZoneMod wildPokemonMod) {
        this.wildPokemonZoneMod = wildPokemonMod;
    }

    public void setSplitWildZoneByEncounterTypes(boolean splitWildZoneByEncounterTypes) {
        this.splitWildZoneByEncounterTypes = splitWildZoneByEncounterTypes;
    }

    public boolean isSplitWildZoneByEncounterTypes() {
        return splitWildZoneByEncounterTypes;
    }

    public boolean isKeepWildTypeThemes() {
        return keepWildTypeThemes;
    }

    public void setKeepWildTypeThemes(boolean keepWildTypeThemes) {
        this.keepWildTypeThemes = keepWildTypeThemes;
    }

    public boolean isKeepWildEvolutionFamilies() {
        return keepWildEvolutionFamilies;
    }

    public void setKeepWildEvolutionFamilies(boolean keepWildEvolutionFamilies) {
        this.keepWildEvolutionFamilies = keepWildEvolutionFamilies;
    }

    public boolean isSimilarStrengthEncounters() {
        return similarStrengthEncounters;
    }

    public void setSimilarStrengthEncounters(boolean similarStrengthEncounters) {
        this.similarStrengthEncounters = similarStrengthEncounters;
    }

    public boolean isCatchEmAllEncounters() {
        return catchEmAllEncounters;
    }

    public void setCatchEmAllEncounters(boolean catchEmAllEncounters) {
        this.catchEmAllEncounters = catchEmAllEncounters;
    }

    public WildPokemonTypeMod getWildPokemonTypeMod() {
        return wildPokemonTypeMod;
    }

    public void setWildPokemonTypeMod(boolean... bools) {
        setWildPokemonTypeMod(getEnum(WildPokemonTypeMod.class, bools));
    }

    public void setWildPokemonTypeMod(WildPokemonTypeMod wildPokemonTypeMod) {
        this.wildPokemonTypeMod = wildPokemonTypeMod;
    }

    public WildPokemonEvolutionMod getWildPokemonEvolutionMod() {
        return wildPokemonEvolutionMod;
    }

    public void setWildPokemonEvolutionMod(boolean... bools) {
        setWildPokemonEvolutionMod(getEnum(WildPokemonEvolutionMod.class, bools));
    }

    public void setWildPokemonEvolutionMod(WildPokemonEvolutionMod wildPokemonEvolutionMod) {
        this.wildPokemonEvolutionMod = wildPokemonEvolutionMod;
    }

    public boolean isUseTimeBasedEncounters() {
        return useTimeBasedEncounters;
    }

    public void setUseTimeBasedEncounters(boolean useTimeBasedEncounters) {
        this.useTimeBasedEncounters = useTimeBasedEncounters;
    }

    public boolean isBlockWildLegendaries() {
        return blockWildLegendaries;
    }

    public void setBlockWildLegendaries(boolean blockWildLegendaries) {
        this.blockWildLegendaries = blockWildLegendaries;
    }

    public boolean isUseMinimumCatchRate() {
        return useMinimumCatchRate;
    }

    public void setUseMinimumCatchRate(boolean useMinimumCatchRate) {
        this.useMinimumCatchRate = useMinimumCatchRate;
    }

    public int getMinimumCatchRateLevel() {
        return minimumCatchRateLevel;
    }

    public void setMinimumCatchRateLevel(int minimumCatchRateLevel) {
        this.minimumCatchRateLevel = minimumCatchRateLevel;
    }

    public boolean isRandomizeWildPokemonHeldItems() {
        return randomizeWildPokemonHeldItems;
    }

    public void setRandomizeWildPokemonHeldItems(boolean randomizeWildPokemonHeldItems) {
        this.randomizeWildPokemonHeldItems = randomizeWildPokemonHeldItems;
    }

    public boolean isBanBadRandomWildPokemonHeldItems() {
        return banBadRandomWildPokemonHeldItems;
    }

    public void setBanBadRandomWildPokemonHeldItems(boolean banBadRandomWildPokemonHeldItems) {
        this.banBadRandomWildPokemonHeldItems = banBadRandomWildPokemonHeldItems;
    }

    public boolean isBalanceShakingGrass() {
        return balanceShakingGrass;
    }

    public void setBalanceShakingGrass(boolean balanceShakingGrass) {
        this.balanceShakingGrass = balanceShakingGrass;
    }

    public boolean isWildLevelsModified() {
        return wildLevelsModified;
    }

    public void setWildLevelsModified(boolean wildLevelsModified) {
        this.wildLevelsModified = wildLevelsModified;
    }

    public int getWildLevelModifier() {
        return wildLevelModifier;
    }

    public void setWildLevelModifier(int wildLevelModifier) {
        this.wildLevelModifier = wildLevelModifier;
    }

    public boolean isAllowWildAltFormes() {
        return allowWildAltFormes;
    }

    public void setAllowWildAltFormes(boolean allowWildAltFormes) {
        this.allowWildAltFormes = allowWildAltFormes;
    }

    public StaticPokemonMod getStaticPokemonMod() {
        return staticPokemonMod;
    }

    public void setStaticPokemonMod(boolean... bools) {
        setStaticPokemonMod(getEnum(StaticPokemonMod.class, bools));
    }

    public void setStaticPokemonMod(StaticPokemonMod staticPokemonMod) {
        this.staticPokemonMod = staticPokemonMod;
    }

    public boolean isLimitMainGameLegendaries() {
        return limitMainGameLegendaries;
    }

    public void setLimitMainGameLegendaries(boolean limitMainGameLegendaries) {
        this.limitMainGameLegendaries = limitMainGameLegendaries;
    }

    public boolean isLimit600() {
        return limit600;
    }

    public void setLimit600(boolean limit600) {
        this.limit600 = limit600;
    }

    public boolean isAllowStaticAltFormes() {
        return allowStaticAltFormes;
    }

    public void setAllowStaticAltFormes(boolean allowStaticAltFormes) {
        this.allowStaticAltFormes = allowStaticAltFormes;
    }

    public boolean isSwapStaticMegaEvos() {
        return swapStaticMegaEvos;
    }

    public void setSwapStaticMegaEvos(boolean swapStaticMegaEvos) {
        this.swapStaticMegaEvos = swapStaticMegaEvos;
    }

    public boolean isStaticLevelModified() {
        return staticLevelModified;
    }

    public void setStaticLevelModified(boolean staticLevelModified) {
        this.staticLevelModified = staticLevelModified;
    }

    public int getStaticLevelModifier() {
        return staticLevelModifier;
    }

    public void setStaticLevelModifier(int staticLevelModifier) {
        this.staticLevelModifier = staticLevelModifier;
    }

    public boolean isCorrectStaticMusic() {
        return correctStaticMusic;
    }

    public void setCorrectStaticMusic(boolean correctStaticMusic) {
        this.correctStaticMusic = correctStaticMusic;
    }


    public TotemPokemonMod getTotemPokemonMod() {
        return totemPokemonMod;
    }

    public void setTotemPokemonMod(boolean... bools) {
        setTotemPokemonMod(getEnum(TotemPokemonMod.class, bools));
    }

    public void setTotemPokemonMod(TotemPokemonMod totemPokemonMod) {
        this.totemPokemonMod = totemPokemonMod;
    }

    public AllyPokemonMod getAllyPokemonMod() {
        return allyPokemonMod;
    }

    public void setAllyPokemonMod(boolean... bools) {
        setAllyPokemonMod(getEnum(AllyPokemonMod.class, bools));
    }

    public void setAllyPokemonMod(AllyPokemonMod allyPokemonMod) {
        this.allyPokemonMod = allyPokemonMod;
    }

    public AuraMod getAuraMod() {
        return auraMod;
    }

    public void setAuraMod(boolean... bools) {
        setAuraMod(getEnum(AuraMod.class, bools));
    }

    public void setAuraMod(AuraMod auraMod) {
        this.auraMod = auraMod;
    }

    public boolean isRandomizeTotemHeldItems() {
        return randomizeTotemHeldItems;
    }

    public void setRandomizeTotemHeldItems(boolean randomizeTotemHeldItems) {
        this.randomizeTotemHeldItems = randomizeTotemHeldItems;
    }

    public boolean isTotemLevelsModified() {
        return totemLevelsModified;
    }

    public void setTotemLevelsModified(boolean totemLevelsModified) {
        this.totemLevelsModified = totemLevelsModified;
    }

    public int getTotemLevelModifier() {
        return totemLevelModifier;
    }

    public void setTotemLevelModifier(int totemLevelModifier) {
        this.totemLevelModifier = totemLevelModifier;
    }

    public boolean isAllowTotemAltFormes() {
        return allowTotemAltFormes;
    }

    public void setAllowTotemAltFormes(boolean allowTotemAltFormes) {
        this.allowTotemAltFormes = allowTotemAltFormes;
    }

    public TMsMod getTmsMod() {
        return tmsMod;
    }

    public void setTmsMod(boolean... bools) {
        setTmsMod(getEnum(TMsMod.class, bools));
    }

    public void setTmsMod(TMsMod tmsMod) {
        this.tmsMod = tmsMod;
    }

    public boolean isTmLevelUpMoveSanity() {
        return tmLevelUpMoveSanity;
    }

    public void setTmLevelUpMoveSanity(boolean tmLevelUpMoveSanity) {
        this.tmLevelUpMoveSanity = tmLevelUpMoveSanity;
    }

    public boolean isKeepFieldMoveTMs() {
        return keepFieldMoveTMs;
    }

    public void setKeepFieldMoveTMs(boolean keepFieldMoveTMs) {
        this.keepFieldMoveTMs = keepFieldMoveTMs;
    }

    public boolean isFullHMCompat() {
        return fullHMCompat;
    }

    public void setFullHMCompat(boolean fullHMCompat) {
        this.fullHMCompat = fullHMCompat;
    }

    public boolean isTmsForceGoodDamaging() {
        return tmsForceGoodDamaging;
    }

    public void setTmsForceGoodDamaging(boolean tmsForceGoodDamaging) {
        this.tmsForceGoodDamaging = tmsForceGoodDamaging;
    }

    public int getTmsGoodDamagingPercent() {
        return tmsGoodDamagingPercent;
    }

    public void setTmsGoodDamagingPercent(int tmsGoodDamagingPercent) {
        this.tmsGoodDamagingPercent = tmsGoodDamagingPercent;
    }

    public boolean isBlockBrokenTMMoves() {
        return blockBrokenTMMoves;
    }

    public void setBlockBrokenTMMoves(boolean blockBrokenTMMoves) {
        this.blockBrokenTMMoves = blockBrokenTMMoves;
    }

    public TMsHMsCompatibilityMod getTmsHmsCompatibilityMod() {
        return tmsHmsCompatibilityMod;
    }

    public void setTmsHmsCompatibilityMod(boolean... bools) {
        setTmsHmsCompatibilityMod(getEnum(TMsHMsCompatibilityMod.class, bools));
    }

    public void setTmsHmsCompatibilityMod(TMsHMsCompatibilityMod tmsHmsCompatibilityMod) {
        this.tmsHmsCompatibilityMod = tmsHmsCompatibilityMod;
    }

    public boolean isTmsFollowEvolutions() {
        return tmsFollowEvolutions;
    }

    public void setTmsFollowEvolutions(boolean tmsFollowEvolutions) {
        this.tmsFollowEvolutions = tmsFollowEvolutions;
    }

    public MoveTutorMovesMod getMoveTutorMovesMod() {
        return moveTutorMovesMod;
    }

    public void setMoveTutorMovesMod(boolean... bools) {
        setMoveTutorMovesMod(getEnum(MoveTutorMovesMod.class, bools));
    }

    public void setMoveTutorMovesMod(MoveTutorMovesMod moveTutorMovesMod) {
        this.moveTutorMovesMod = moveTutorMovesMod;
    }

    public boolean isTutorLevelUpMoveSanity() {
        return tutorLevelUpMoveSanity;
    }

    public void setTutorLevelUpMoveSanity(boolean tutorLevelUpMoveSanity) {
        this.tutorLevelUpMoveSanity = tutorLevelUpMoveSanity;
    }

    public boolean isKeepFieldMoveTutors() {
        return keepFieldMoveTutors;
    }

    public void setKeepFieldMoveTutors(boolean keepFieldMoveTutors) {
        this.keepFieldMoveTutors = keepFieldMoveTutors;
    }

    public boolean isTutorsForceGoodDamaging() {
        return tutorsForceGoodDamaging;
    }

    public void setTutorsForceGoodDamaging(boolean tutorsForceGoodDamaging) {
        this.tutorsForceGoodDamaging = tutorsForceGoodDamaging;
    }

    public int getTutorsGoodDamagingPercent() {
        return tutorsGoodDamagingPercent;
    }

    public void setTutorsGoodDamagingPercent(int tutorsGoodDamagingPercent) {
        this.tutorsGoodDamagingPercent = tutorsGoodDamagingPercent;
    }

    public boolean isBlockBrokenTutorMoves() {
        return blockBrokenTutorMoves;
    }

    public void setBlockBrokenTutorMoves(boolean blockBrokenTutorMoves) {
        this.blockBrokenTutorMoves = blockBrokenTutorMoves;
    }

    public MoveTutorsCompatibilityMod getMoveTutorsCompatibilityMod() {
        return moveTutorsCompatibilityMod;
    }

    public void setMoveTutorsCompatibilityMod(boolean... bools) {
        setMoveTutorsCompatibilityMod(getEnum(MoveTutorsCompatibilityMod.class, bools));
    }

    public void setMoveTutorsCompatibilityMod(MoveTutorsCompatibilityMod moveTutorsCompatibilityMod) {
        this.moveTutorsCompatibilityMod = moveTutorsCompatibilityMod;
    }

    public boolean isTutorFollowEvolutions() {
        return tutorFollowEvolutions;
    }

    public void setTutorFollowEvolutions(boolean tutorFollowEvolutions) {
        this.tutorFollowEvolutions = tutorFollowEvolutions;
    }

    public InGameTradesMod getInGameTradesMod() {
        return inGameTradesMod;
    }

    public void setInGameTradesMod(boolean... bools) {
        setInGameTradesMod(getEnum(InGameTradesMod.class, bools));
    }

    public void setInGameTradesMod(InGameTradesMod inGameTradesMod) {
        this.inGameTradesMod = inGameTradesMod;
    }

    public boolean isRandomizeInGameTradesNicknames() {
        return randomizeInGameTradesNicknames;
    }

    public void setRandomizeInGameTradesNicknames(boolean randomizeInGameTradesNicknames) {
        this.randomizeInGameTradesNicknames = randomizeInGameTradesNicknames;
    }

    public boolean isRandomizeInGameTradesOTs() {
        return randomizeInGameTradesOTs;
    }

    public void setRandomizeInGameTradesOTs(boolean randomizeInGameTradesOTs) {
        this.randomizeInGameTradesOTs = randomizeInGameTradesOTs;
    }

    public boolean isRandomizeInGameTradesIVs() {
        return randomizeInGameTradesIVs;
    }

    public void setRandomizeInGameTradesIVs(boolean randomizeInGameTradesIVs) {
        this.randomizeInGameTradesIVs = randomizeInGameTradesIVs;
    }

    public boolean isRandomizeInGameTradesItems() {
        return randomizeInGameTradesItems;
    }

    public void setRandomizeInGameTradesItems(boolean randomizeInGameTradesItems) {
        this.randomizeInGameTradesItems = randomizeInGameTradesItems;
    }

    public FieldItemsMod getFieldItemsMod() {
        return fieldItemsMod;
    }

    public void setFieldItemsMod(boolean... bools) {
        setFieldItemsMod(getEnum(FieldItemsMod.class, bools));
    }

    public void setFieldItemsMod(FieldItemsMod fieldItemsMod) {
        this.fieldItemsMod = fieldItemsMod;
    }

    public boolean isBanBadRandomFieldItems() {
        return banBadRandomFieldItems;
    }


    public void setBanBadRandomFieldItems(boolean banBadRandomFieldItems) {
        this.banBadRandomFieldItems = banBadRandomFieldItems;
    }

    public ShopItemsMod getShopItemsMod() {
        return shopItemsMod;
    }

    public void setShopItemsMod(boolean... bools) {
        setShopItemsMod(getEnum(ShopItemsMod.class, bools));
    }

    public void setShopItemsMod(ShopItemsMod shopItemsMod) {
        this.shopItemsMod = shopItemsMod;
    }

    public boolean isBanBadRandomShopItems() {
        return banBadRandomShopItems;
    }

    public void setBanBadRandomShopItems(boolean banBadRandomShopItems) {
        this.banBadRandomShopItems = banBadRandomShopItems;
    }

    public boolean isBanRegularShopItems() {
        return banRegularShopItems;
    }

    public void setBanRegularShopItems(boolean banRegularShopItems) {
        this.banRegularShopItems = banRegularShopItems;
    }

    public boolean isBanOPShopItems() {
        return banOPShopItems;
    }

    public void setBanOPShopItems(boolean banOPShopItems) {
        this.banOPShopItems = banOPShopItems;
    }

    public boolean isGuaranteeEvolutionItems() {
        return guaranteeEvolutionItems;
    }

    public void setGuaranteeEvolutionItems(boolean guaranteeEvolutionItems) {
        this.guaranteeEvolutionItems = guaranteeEvolutionItems;
    }

    public boolean isGuaranteeXItems() {
        return guaranteeXItems;
    }

    public void setGuaranteeXItems(boolean guaranteeXItems) {
        this.guaranteeXItems = guaranteeXItems;
    }

    public boolean isBalanceShopPrices() {
        return balanceShopPrices;
    }

    public void setBalanceShopPrices(boolean balanceShopPrices) {
        this.balanceShopPrices = balanceShopPrices;
    }

    public boolean isAddCheapRareCandiesToShops() {
        return addCheapRareCandiesToShops;
    }

    public void setAddCheapRareCandiesToShops(boolean addCheapRareCandiesToShops) {
        this.addCheapRareCandiesToShops = addCheapRareCandiesToShops;
    }

    public PickupItemsMod getPickupItemsMod() {
        return pickupItemsMod;
    }

    public void setPickupItemsMod(boolean... bools) {
        setPickupItemsMod(getEnum(PickupItemsMod.class, bools));
    }

    public void setPickupItemsMod(PickupItemsMod pickupItemsMod) {
        this.pickupItemsMod = pickupItemsMod;
    }

    public boolean isBanBadRandomPickupItems() {
        return banBadRandomPickupItems;
    }

    public void setBanBadRandomPickupItems(boolean banBadRandomPickupItems) {
        this.banBadRandomPickupItems = banBadRandomPickupItems;
    }

    public TypeEffectivenessMod getTypeEffectivenessMod() {
        return typeEffectivenessMod;
    }

    public void setTypeEffectivenessMod(boolean... bools) {
        setTypeEffectivenessMod(getEnum(TypeEffectivenessMod.class, bools));
    }

    public void setTypeEffectivenessMod(TypeEffectivenessMod typeEffectivenessMod) {
        this.typeEffectivenessMod = typeEffectivenessMod;
    }

    public boolean isInverseTypesRandomImmunities() {
        return inverseTypesRandomImmunities;
    }

    public void setInverseTypesRandomImmunities(boolean inverseTypesRandomImmunities) {
        this.inverseTypesRandomImmunities = inverseTypesRandomImmunities;
    }

    public boolean isUpdateTypeEffectiveness() {
        return updateTypeEffectiveness;
    }

    public void setUpdateTypeEffectiveness(boolean updateTypeEffectiveness) {
        this.updateTypeEffectiveness = updateTypeEffectiveness;
    }

    public PokemonPalettesMod getPokemonPalettesMod() {
    	return pokemonPalettesMod;
    }

    public void setPokemonPalettesMod(boolean... bools) {
        setPokemonPalettesMod(getEnum(PokemonPalettesMod.class, bools));
    }
    
    public void setPokemonPalettesMod(PokemonPalettesMod pokemonPalettesMod) {
    	this.pokemonPalettesMod = pokemonPalettesMod;
    }

    public boolean isPokemonPalettesFollowTypes() {
		return pokemonPalettesFollowTypes;
	}

	public void setPokemonPalettesFollowTypes(boolean pokemonPalettesFollowTypes) {
		this.pokemonPalettesFollowTypes = pokemonPalettesFollowTypes;
	}

	public boolean isPokemonPalettesFollowEvolutions() {
		return pokemonPalettesFollowEvolutions;
	}

	public void setPokemonPalettesFollowEvolutions(boolean pokemonPalettesFollowEvolutions) {
		this.pokemonPalettesFollowEvolutions = pokemonPalettesFollowEvolutions;
	}

	public boolean isPokemonPalettesShinyFromNormal() {
		return pokemonPalettesShinyFromNormal;
	}

	public void setPokemonPalettesShinyFromNormal(boolean pokemonPalettesShinyFromNormal) {
		this.pokemonPalettesShinyFromNormal = pokemonPalettesShinyFromNormal;
	}

	private static int makeByteSelected(boolean... bools) {
        if (bools.length > 8) {
            throw new IllegalArgumentException("Can't set more than 8 bits in a byte!");
        }

        int initial = 0;
        int state = 1;
        for (boolean b : bools) {
            initial |= b ? state : 0;
            state *= 2;
        }
        return initial;
    }

    private static boolean restoreState(byte b, int index) {
        if (index >= 8) {
            throw new IllegalArgumentException("Can't read more than 8 bits from a byte!");
        }

        int value = b & 0xFF;
        return ((value >> index) & 0x01) == 0x01;
    }

    private static void writeFullInt(ByteArrayOutputStream out, int value) throws IOException {
        byte[] crc = new byte[4];
        FileFunctions.writeFullInt(crc, 0, value);
        out.write(crc);
    }

    private static void writeFullIntBigEndian(ByteArrayOutputStream out, int value) throws IOException {
        byte[] crc = ByteBuffer.allocate(4).putInt(value).array();
        out.write(crc);
    }

    private static void write2ByteIntBigEndian(ByteArrayOutputStream out, int value) {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

    private static <E extends Enum<E>> E restoreEnum(Class<E> clazz, byte b, int... indices) {
        boolean[] bools = new boolean[indices.length];
        int i = 0;
        for (int idx : indices) {
            bools[i] = restoreState(b, idx);
            i++;
        }
        return getEnum(clazz, bools);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E getEnum(Class<E> clazz, boolean... bools) {
        int index = getSetEnum(clazz.getSimpleName(), bools);
        try {
            return ((E[]) clazz.getMethod("values").invoke(null))[index];
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Unable to parse enum of type %s", clazz.getSimpleName()),
                    e);
        }
    }

    private static int getSetEnum(String type, boolean... bools) {
        int index = -1;
        for (int i = 0; i < bools.length; i++) {
            if (bools[i]) {
                if (index >= 0) {
                    throw new IllegalStateException(String.format("Only one value for %s may be chosen!", type));
                }
                index = i;
            }
        }
        // We have to return something, so return the default
        return index >= 0 ? index : 0;
    }

    private static void checkChecksum(byte[] data) {
        // Check the checksum
        ByteBuffer buf = ByteBuffer.allocate(4).put(data, data.length - 8, 4);
        buf.rewind();
        int crc = buf.getInt();

        CRC32 checksum = new CRC32();
        checksum.update(data, 0, data.length - 8);

        if ((int) checksum.getValue() != crc) {
            throw new IllegalArgumentException("Malformed input string");
        }
    }

}
