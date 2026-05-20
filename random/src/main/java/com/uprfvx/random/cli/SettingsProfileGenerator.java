package com.uprfvx.random.cli;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.GenRestrictions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * No-ROM helper for deriving settings files from a saved .rnqs baseline.
 */
public final class SettingsProfileGenerator {

    private static final Map<String, String> UNSUPPORTED_OVERLAY_REASONS = new LinkedHashMap<>();
    private static final Map<String, Consumer<Settings>> FEATURE_OVERLAYS = buildFeatureOverlays();
    private static final Map<String, List<String>> PROFILE_OVERLAYS = buildProfileOverlays();

    private SettingsProfileGenerator() {
    }

    public static void main(String[] args) {
        System.exit(invoke(args));
    }

    public static int invoke(String[] args) {
        try {
            Arguments arguments = Arguments.parse(args);
            if (arguments.help) {
                printUsage();
                return 0;
            }
            if (arguments.listFeatures) {
                printFeatureOverlays();
                return 0;
            }
            if (arguments.listProfiles) {
                printProfileOverlays();
                return 0;
            }

            Settings settings = readSettings(arguments.baseSettings);
            for (String profileId : arguments.profiles) {
                applyProfile(settings, profileId);
            }
            for (String featureId : arguments.enabledFeatures) {
                applyFeature(settings, featureId);
            }
            writeSettings(settings, arguments.outputSettings);
            System.out.println("Settings profile written successfully.");
            return 0;
        } catch (IllegalArgumentException | IOException ex) {
            System.err.println(ex.getMessage());
            return 1;
        }
    }

    private static Settings readSettings(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Missing required --base-settings path.");
        }
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Base settings file does not exist.");
        }
        try (FileInputStream in = new FileInputStream(path.toFile())) {
            return Settings.readFromFileFormat(in);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Base settings file is not a valid .rnqs file.", ex);
        }
    }

    private static void writeSettings(Settings settings, Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Missing required --output-settings path.");
        }
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (FileOutputStream out = new FileOutputStream(path.toFile())) {
            settings.writeToFileFormat(out);
        }
    }

    private static void applyProfile(Settings settings, String profileId) {
        List<String> featureIds = PROFILE_OVERLAYS.get(normalize(profileId));
        if (featureIds == null) {
            throw new IllegalArgumentException("Unknown settings profile: " + profileId);
        }
        for (String featureId : featureIds) {
            applyFeature(settings, featureId);
        }
    }

    private static void applyFeature(Settings settings, String featureId) {
        Consumer<Settings> overlay = FEATURE_OVERLAYS.get(normalize(featureId));
        if (overlay == null) {
            throw new IllegalArgumentException("Unknown settings feature overlay: " + featureId);
        }
        overlay.accept(settings);
    }

    private static Map<String, Consumer<Settings>> buildFeatureOverlays() {
        Map<String, Consumer<Settings>> overlays = new LinkedHashMap<>();

        overlays.put("MODE-FOE-RANDOM", s -> s.setTrainersMod(Settings.TrainersMod.RANDOM));
        overlays.put("MODE-FOE-EVEN-DISTRIBUTION", s -> s.setTrainersMod(Settings.TrainersMod.DISTRIBUTED));
        overlays.put("MODE-FOE-MAIN-PLAYTHROUGH", s -> s.setTrainersMod(Settings.TrainersMod.MAINPLAYTHROUGH));
        overlays.put("MODE-FOE-TYPE-THEMED", s -> s.setTrainersMod(Settings.TrainersMod.TYPE_THEMED));
        overlays.put("MODE-FOE-KEEP-THEMED", s -> s.setTrainersMod(Settings.TrainersMod.KEEP_THEMED));

        overlays.put("MODE-WILD-ENCOUNTER-SET", s -> setWildZoneMode(s, Settings.WildPokemonZoneMod.ENCOUNTER_SET));
        overlays.put("MODE-WILD-MAP", s -> setWildZoneMode(s, Settings.WildPokemonZoneMod.MAP));
        overlays.put("MODE-WILD-NAMED-LOCATION", s -> setWildZoneMode(s, Settings.WildPokemonZoneMod.NAMED_LOCATION));
        overlays.put("MODE-WILD-GAME", s -> setWildZoneMode(s, Settings.WildPokemonZoneMod.GAME));
        overlays.put("MODE-WILD-CATCH-EM-ALL", s -> {
            s.setRandomizeWildPokemon(true);
            s.setCatchEmAllEncounters(true);
        });

        overlays.put("MODE-TYPE-RANDOM", s -> s.setTypeEffectivenessMod(Settings.TypeEffectivenessMod.RANDOM));
        overlays.put("MODE-TYPE-RANDOM-BALANCED", s -> s.setTypeEffectivenessMod(Settings.TypeEffectivenessMod.RANDOM_BALANCED));
        overlays.put("MODE-TYPE-KEEP-IDENTITIES", s -> s.setTypeEffectivenessMod(Settings.TypeEffectivenessMod.KEEP_IDENTITIES));
        overlays.put("MODE-TYPE-INVERSE", s -> s.setTypeEffectivenessMod(Settings.TypeEffectivenessMod.INVERSE));

        overlays.put("MODE-GEN-LIMIT-1-9", s -> setGenLimit(s, true));
        overlays.put("MODE-GEN-LIMIT-1-9-NO-RELATIVES", s -> setGenLimit(s, false));
        overlays.put("MODE-GEN-LIMIT-1-9-NO-MEGAS", unsupported("MODE-GEN-LIMIT-1-9-NO-MEGAS",
                "Mega-specific pool exclusion has no dedicated Settings field."));
        overlays.put("MODE-GEN-LIMIT-1-9-NO-GMAX", unsupported("MODE-GEN-LIMIT-1-9-NO-GMAX",
                "Gigantamax-specific pool exclusion has no dedicated Settings field."));

        overlays.put("MODE-INTRO-RANDOM", s -> s.setRandomizeIntroMon(true));
        overlays.put("MODE-NO-RANDOM-INTRO", s -> s.setRandomizeIntroMon(false));

        overlays.put("FVX-GEN-001", s -> s.setLimitPokemon(true));
        overlays.put("FVX-GEN-002", s -> s.setBanPrematureEvos(true));
        overlays.put("FVX-GEN-003", s -> s.setRandomizeIntroMon(false));
        overlays.put("FVX-GEN-004", s -> s.setRaceMode(true));

        overlays.put("FVX-TRAIT-001", s -> s.setBaseStatisticsMod(Settings.BaseStatisticsMod.RANDOM));
        overlays.put("FVX-TRAIT-002", s -> s.setBaseStatsFollowEvolutions(true));
        overlays.put("FVX-TRAIT-003", s -> s.setAssignEvoStatsRandomly(true));
        overlays.put("FVX-TRAIT-004", s -> {
            s.setUpdateBaseStats(true);
            s.setUpdateBaseStatsToGeneration(9);
        });
        overlays.put("FVX-TRAIT-005", s -> s.setStandardizeEXPCurves(true));
        overlays.put("FVX-TRAIT-006", s -> s.setSpeciesTypesMod(Settings.SpeciesTypesMod.COMPLETELY_RANDOM));
        overlays.put("FVX-TRAIT-007", s -> s.setDualTypeOnly(true));
        overlays.put("FVX-TRAIT-008", s -> s.setAbilitiesMod(Settings.AbilitiesMod.RANDOMIZE));
        overlays.put("FVX-TRAIT-009", s -> s.setAbilitiesFollowEvolutions(true));
        overlays.put("FVX-TRAIT-010", s -> s.setAllowWonderGuard(true));
        overlays.put("FVX-TRAIT-011", s -> s.setWeighDuplicateAbilitiesTogether(true));
        overlays.put("FVX-TRAIT-012", s -> s.setEnsureTwoAbilities(true));
        overlays.put("FVX-TRAIT-013", s -> s.setBanTrappingAbilities(true));
        overlays.put("FVX-TRAIT-014", s -> s.setBanNegativeAbilities(true));
        overlays.put("FVX-TRAIT-015", s -> s.setBanBadAbilities(true));
        overlays.put("FVX-TRAIT-016", s -> s.setEvolutionsMod(Settings.EvolutionsMod.RANDOM));
        overlays.put("FVX-TRAIT-017", s -> s.setEvolutionsMod(Settings.EvolutionsMod.RANDOM_EVERY_LEVEL));
        overlays.put("FVX-TRAIT-018", s -> s.setEvosSimilarStrength(true));
        overlays.put("FVX-TRAIT-019", s -> s.setEvosSameTyping(true));
        overlays.put("FVX-TRAIT-020", s -> s.setEvosMaxThreeStages(true));
        overlays.put("FVX-TRAIT-021", s -> s.setEvosNoConvergence(true));
        overlays.put("FVX-TRAIT-022", s -> s.setEvosForceChange(true));
        overlays.put("FVX-TRAIT-023", s -> s.setEvosForceGrowth(true));
        overlays.put("FVX-TRAIT-024", s -> s.setChangeImpossibleEvolutions(true));
        overlays.put("FVX-TRAIT-025", s -> s.setMakeEvolutionsEasier(true));
        overlays.put("FVX-TRAIT-026", s -> s.setEstimateLevelForEvolutionImprovements(true));
        overlays.put("FVX-TRAIT-027", s -> s.setRemoveTimeBasedEvolutions(true));
        overlays.put("FVX-TRAIT-028", s -> s.setExpCurveMod(Settings.ExpCurveMod.STRONG_LEGENDARIES));

        overlays.put("FVX-SST-001", unsupported("FVX-SST-001", "Custom starters require ROM-specific species IDs."));
        overlays.put("FVX-SST-002", s -> s.setStartersMod(Settings.StartersMod.COMPLETELY_RANDOM));
        overlays.put("FVX-SST-003", s -> s.setStartersMod(Settings.StartersMod.RANDOM_WITH_TWO_EVOLUTIONS));
        overlays.put("FVX-SST-004", s -> s.setStartersMod(Settings.StartersMod.RANDOM_BASIC));
        overlays.put("FVX-SST-005", s -> s.setStartersTypeMod(Settings.StartersTypeMod.FIRE_WATER_GRASS));
        overlays.put("FVX-SST-006", s -> s.setStartersNoLegendaries(true));
        overlays.put("FVX-SST-007", s -> s.setRandomizeStartersHeldItems(true));
        overlays.put("FVX-SST-008", s -> s.setBanBadRandomStarterHeldItems(true));
        overlays.put("FVX-SST-009", s -> {
            s.setStartersBSTMinimum(300);
            s.setStartersBSTMaximum(600);
        });
        overlays.put("FVX-SST-010", s -> s.setStaticPokemonMod(Settings.StaticPokemonMod.RANDOM_MATCHING));
        overlays.put("FVX-SST-011", s -> s.setStaticPokemonMod(Settings.StaticPokemonMod.COMPLETELY_RANDOM));
        overlays.put("FVX-SST-012", s -> s.setStaticPokemonMod(Settings.StaticPokemonMod.SIMILAR_STRENGTH));
        overlays.put("FVX-SST-013", s -> {
            s.setStaticLevelModified(true);
            s.setStaticLevelModifier(0);
            s.setCorrectStaticMusic(true);
        });
        overlays.put("FVX-SST-014", s -> s.setInGameTradesMod(Settings.InGameTradesMod.RANDOMIZE_GIVEN_AND_REQUESTED));
        overlays.put("FVX-SST-015", s -> {
            s.setRandomizeInGameTradesNicknames(true);
            s.setRandomizeInGameTradesOTs(true);
            s.setRandomizeInGameTradesIVs(true);
            s.setRandomizeInGameTradesItems(true);
        });

        overlays.put("FVX-MOVE-001", s -> s.setRandomizeMovePowers(true));
        overlays.put("FVX-MOVE-002", s -> s.setRandomizeMoveAccuracies(true));
        overlays.put("FVX-MOVE-003", s -> s.setRandomizeMovePPs(true));
        overlays.put("FVX-MOVE-004", s -> s.setRandomizeMoveTypes(true));
        overlays.put("FVX-MOVE-005", s -> s.setRandomizeMoveNames(true));
        overlays.put("FVX-MOVE-006", unsupported("FVX-MOVE-006",
                "Update Moves to Generation is intentionally out of scope for CFRU/DPE Gen9 profiles."));
        overlays.put("FVX-MOVE-007", s -> s.setMovesetsMod(Settings.MovesetsMod.COMPLETELY_RANDOM));
        overlays.put("FVX-MOVE-008", s -> s.setStartWithGuaranteedMoves(true));
        overlays.put("FVX-MOVE-009", s -> s.setReorderDamagingMoves(true));
        overlays.put("FVX-MOVE-010", s -> s.setBlockBrokenMovesetMoves(true));
        overlays.put("FVX-MOVE-011", s -> {
            s.setMovesetsForceGoodDamaging(true);
            s.setMovesetsGoodDamagingPercent(50);
        });

        overlays.put("FVX-FOE-001", s -> s.setTrainersMod(Settings.TrainersMod.RANDOM));
        overlays.put("FVX-FOE-002", s -> s.setBetterBossTrainerMovesets(true));
        overlays.put("FVX-FOE-003", s -> s.setBetterImportantTrainerMovesets(true));
        overlays.put("FVX-FOE-004", s -> s.setBetterRegularTrainerMovesets(true));
        overlays.put("FVX-FOE-005", s -> s.setAdditionalBossTrainerPokemon(1));
        overlays.put("FVX-FOE-006", s -> s.setAdditionalImportantTrainerPokemon(1));
        overlays.put("FVX-FOE-007", s -> s.setAdditionalRegularTrainerPokemon(1));
        overlays.put("FVX-FOE-008", s -> {
            s.setRandomizeHeldItemsForBossTrainerPokemon(true);
            s.setRandomizeHeldItemsForImportantTrainerPokemon(true);
            s.setRandomizeHeldItemsForRegularTrainerPokemon(true);
        });
        overlays.put("FVX-FOE-009", s -> {
            s.setTrainersMatchTypingDistribution(true);
            s.setDiverseTypesForBossTrainers(true);
            s.setDiverseTypesForImportantTrainers(true);
            s.setDiverseTypesForRegularTrainers(true);
        });
        overlays.put("FVX-FOE-010", s -> s.setEliteFourUniquePokemonNumber(1));
        overlays.put("FVX-FOE-011", s -> s.setBattleStyle(new BattleStyle(BattleStyle.Modification.RANDOM, BattleStyle.Style.SINGLE_BATTLE)));
        overlays.put("FVX-FOE-012", s -> s.setRivalCarriesStarterThroughout(true));
        overlays.put("FVX-FOE-013", s -> {
            s.setRandomizeTrainerNames(true);
            s.setRandomizeTrainerClassNames(true);
        });
        overlays.put("MODE-TRAINER-CLASS-SPRITE-SYNC", s -> s.setRandomizeTrainerClassSprites(true));
        overlays.put("FVX-FOE-014", s -> {
            s.setTrainersEvolveTheirPokemon(true);
            s.setTrainersEvolutionLevelModifier(0);
        });
        overlays.put("FVX-FOE-SENSIBLE-HELD-ITEMS", s -> s.setSensibleItemsOnlyForTrainers(true));

        overlays.put("FVX-WILD-001", s -> s.setRandomizeWildPokemon(true));
        overlays.put("FVX-WILD-002", s -> s.setWildPokemonZoneMod(Settings.WildPokemonZoneMod.GAME));
        overlays.put("FVX-WILD-003", s -> s.setSplitWildZoneByEncounterTypes(true));
        overlays.put("FVX-WILD-004", s -> s.setWildPokemonTypeMod(Settings.WildPokemonTypeMod.KEEP_PRIMARY));
        overlays.put("FVX-WILD-005", s -> s.setWildPokemonEvolutionMod(Settings.WildPokemonEvolutionMod.KEEP_STAGE));
        overlays.put("FVX-WILD-006", s -> s.setBlockWildLegendaries(true));
        overlays.put("FVX-WILD-007", s -> {
            s.setUseMinimumCatchRate(true);
            s.setMinimumCatchRateLevel(45);
        });
        overlays.put("FVX-WILD-008", s -> s.setRandomizeWildPokemonHeldItems(true));
        overlays.put("FVX-WILD-009", s -> s.setBanBadRandomWildPokemonHeldItems(true));
        overlays.put("FVX-WILD-010", s -> s.setCatchEmAllEncounters(true));
        overlays.put("FVX-WILD-011", s -> s.setSimilarStrengthEncounters(true));
        overlays.put("FVX-WILD-012", s -> {
            s.setBalanceShakingGrass(true);
            s.setWildLevelsModified(true);
            s.setWildLevelModifier(0);
        });

        overlays.put("FVX-TM-001", s -> s.setTmsMod(Settings.TMsMod.RANDOM));
        overlays.put("FVX-TM-002", s -> s.setKeepFieldMoveTMs(true));
        overlays.put("FVX-TM-003", s -> s.setBlockBrokenTMMoves(true));
        overlays.put("FVX-TM-004", s -> {
            s.setTmsForceGoodDamaging(true);
            s.setTmsGoodDamagingPercent(50);
        });
        overlays.put("FVX-TM-005", s -> s.setTmsHmsCompatibilityMod(Settings.TMsHMsCompatibilityMod.COMPLETELY_RANDOM));
        overlays.put("FVX-TM-006", s -> s.setTmLevelUpMoveSanity(true));
        overlays.put("FVX-TM-007", s -> s.setTmsFollowEvolutions(true));
        overlays.put("FVX-TM-008", s -> s.setFullHMCompat(true));
        overlays.put("FVX-TM-009", s -> s.setMoveTutorMovesMod(Settings.MoveTutorMovesMod.RANDOM));
        overlays.put("FVX-TM-010", s -> s.setKeepFieldMoveTutors(true));
        overlays.put("FVX-TM-011", s -> s.setBlockBrokenTutorMoves(true));
        overlays.put("FVX-TM-012", s -> {
            s.setTutorsForceGoodDamaging(true);
            s.setTutorsGoodDamagingPercent(50);
        });
        overlays.put("FVX-TM-013", s -> s.setMoveTutorsCompatibilityMod(Settings.MoveTutorsCompatibilityMod.COMPLETELY_RANDOM));
        overlays.put("FVX-TM-014", s -> s.setTutorLevelUpMoveSanity(true));
        overlays.put("FVX-TM-015", s -> s.setTutorFollowEvolutions(true));

        overlays.put("FVX-ITEM-001", s -> s.setFieldItemsMod(Settings.FieldItemsMod.SHUFFLE));
        overlays.put("FVX-ITEM-002", s -> s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM));
        overlays.put("FVX-ITEM-003", s -> s.setFieldItemsMod(Settings.FieldItemsMod.RANDOM_EVEN));
        overlays.put("FVX-ITEM-004", s -> s.setBanBadRandomFieldItems(true));
        overlays.put("FVX-ITEM-005", s -> s.setShopItemsMod(Settings.ShopItemsMod.SHUFFLE));
        overlays.put("FVX-ITEM-006", s -> s.setShopItemsMod(Settings.ShopItemsMod.RANDOM));
        overlays.put("FVX-ITEM-007", s -> {
            s.setBanBadRandomShopItems(true);
            s.setBanRegularShopItems(true);
            s.setBanOPShopItems(true);
        });
        overlays.put("FVX-ITEM-008", s -> {
            s.setGuaranteeEvolutionItems(true);
            s.setGuaranteeXItems(true);
        });
        overlays.put("FVX-ITEM-009", s -> {
            s.setBalanceShopPrices(true);
            s.setAddCheapRareCandiesToShops(true);
        });
        overlays.put("FVX-ITEM-010", s -> {
            s.setPickupItemsMod(Settings.PickupItemsMod.RANDOM);
            s.setBanBadRandomPickupItems(true);
        });

        overlays.put("FVX-TYPE-001", s -> s.setTypeEffectivenessMod(Settings.TypeEffectivenessMod.RANDOM_BALANCED));
        overlays.put("FVX-TYPE-002", s -> s.setInverseTypesRandomImmunities(true));
        overlays.put("FVX-TYPE-003", s -> s.setUpdateTypeEffectiveness(true));

        overlays.put("FVX-GFX-001", SettingsProfileGenerator::enablePokemonPaletteRandomization);
        overlays.put("FVX-GFX-002", s -> {
            enablePokemonPaletteRandomization(s);
            s.setPokemonPalettesFollowTypes(true);
        });
        overlays.put("FVX-GFX-003", s -> {
            enablePokemonPaletteRandomization(s);
            s.setPokemonPalettesFollowEvolutions(true);
        });
        overlays.put("FVX-GFX-004", s -> {
            enablePokemonPaletteRandomization(s);
            s.setPokemonPalettesShinyFromNormal(true);
        });
        overlays.put("FVX-GFX-005", unsupported("FVX-GFX-005",
                "Custom Player Graphics are supplied through the randomizer CLI, not .rnqs settings."));
        overlays.put("FVX-GFX-006", unsupported("FVX-GFX-006",
                "Character replacement is part of Custom Player Graphics, not .rnqs settings."));

        overlays.put("FVX-MISC-001", s -> enableMiscTweak(s, MiscTweak.FASTEST_TEXT));
        overlays.put("FVX-MISC-002", s -> enableMiscTweak(s, MiscTweak.RUNNING_SHOES_INDOORS));
        overlays.put("FVX-MISC-003", s -> enableMiscTweak(s, MiscTweak.RANDOMIZE_PC_POTION));
        overlays.put("FVX-MISC-004", s -> enableMiscTweak(s, MiscTweak.NATIONAL_DEX_AT_START));
        overlays.put("FVX-MISC-005", s -> enableMiscTweak(s, MiscTweak.FAST_EGG_HATCHING));
        overlays.put("FVX-MISC-006", s -> enableMiscTweak(s, MiscTweak.LOWER_CASE_POKEMON_NAMES));
        overlays.put("FVX-MISC-007", s -> enableMiscTweak(s, MiscTweak.RANDOMIZE_CATCHING_TUTORIAL));
        overlays.put("FVX-MISC-008", s -> enableMiscTweak(s, MiscTweak.BAN_LUCKY_EGG));
        overlays.put("FVX-MISC-009", s -> enableMiscTweak(s, MiscTweak.BALANCE_STATIC_LEVELS));
        overlays.put("FVX-MISC-010", s -> enableMiscTweak(s, MiscTweak.RUN_WITHOUT_RUNNING_SHOES));
        overlays.put("FVX-MISC-011", s -> enableMiscTweak(s, MiscTweak.REUSABLE_TMS));
        overlays.put("FVX-MISC-012", s -> enableMiscTweak(s, MiscTweak.FORGETTABLE_HMS));

        overlays.put("FVX-SPECIAL-WILD-001", s -> s.setUseTimeBasedEncounters(true));
        return Collections.unmodifiableMap(overlays);
    }

    private static Map<String, List<String>> buildProfileOverlays() {
        Map<String, List<String>> profiles = new LinkedHashMap<>();
        profiles.put("00_BASELINE", Collections.emptyList());
        profiles.put("01_TRAITS_FULL", Arrays.asList(
                "FVX-TRAIT-001", "FVX-TRAIT-002", "FVX-TRAIT-003", "FVX-TRAIT-004",
                "FVX-TRAIT-005", "FVX-TRAIT-006", "FVX-TRAIT-007", "FVX-TRAIT-008",
                "FVX-TRAIT-009", "FVX-TRAIT-010", "FVX-TRAIT-011", "FVX-TRAIT-012",
                "FVX-TRAIT-013", "FVX-TRAIT-014", "FVX-TRAIT-015", "FVX-TRAIT-016",
                "FVX-TRAIT-018", "FVX-TRAIT-019", "FVX-TRAIT-020", "FVX-TRAIT-021",
                "FVX-TRAIT-022", "FVX-TRAIT-023", "FVX-TRAIT-024", "FVX-TRAIT-025",
                "FVX-TRAIT-026", "FVX-TRAIT-027", "FVX-TRAIT-028"));
        profiles.put("02_STARTERS_STATICS_TRADES_FULL", Arrays.asList(
                "FVX-SST-002", "FVX-SST-006", "FVX-SST-007", "FVX-SST-008",
                "FVX-SST-011", "FVX-SST-013", "FVX-SST-014", "FVX-SST-015"));
        profiles.put("03_MOVES_MOVESETS_FULL", Arrays.asList(
                "FVX-MOVE-001", "FVX-MOVE-002", "FVX-MOVE-003", "FVX-MOVE-004",
                "FVX-MOVE-005", "FVX-MOVE-007", "FVX-MOVE-008", "FVX-MOVE-009",
                "FVX-MOVE-010", "FVX-MOVE-011"));
        profiles.put("04_FOE_BASE", Arrays.asList(
                "FVX-FOE-001", "FVX-FOE-002", "FVX-FOE-003", "FVX-FOE-004",
                "FVX-FOE-013", "FVX-FOE-014"));
        profiles.put("04_FOE_HELD_ITEMS_BASIC", Arrays.asList("FVX-FOE-008"));
        profiles.put("04_FOE_HELD_ITEMS_SENSIBLE_EXPECTED_FAIL", Arrays.asList(
                "FVX-FOE-008", "FVX-FOE-SENSIBLE-HELD-ITEMS"));
        profiles.put("05_WILD_FULL", Arrays.asList(
                "FVX-WILD-001", "FVX-WILD-002", "FVX-WILD-003", "FVX-WILD-004",
                "FVX-WILD-005", "FVX-WILD-006", "FVX-WILD-007", "FVX-WILD-008",
                "FVX-WILD-009", "FVX-WILD-010", "FVX-WILD-011", "FVX-WILD-012"));
        profiles.put("06_TM_TUTOR_FULL", Arrays.asList(
                "FVX-TM-001", "FVX-TM-002", "FVX-TM-003", "FVX-TM-004",
                "FVX-TM-005", "FVX-TM-006", "FVX-TM-007", "FVX-TM-008",
                "FVX-TM-009", "FVX-TM-010", "FVX-TM-011", "FVX-TM-012",
                "FVX-TM-013", "FVX-TM-014", "FVX-TM-015"));
        profiles.put("07_ITEMS_FULL", Arrays.asList(
                "FVX-ITEM-002", "FVX-ITEM-004", "FVX-ITEM-006", "FVX-ITEM-007",
                "FVX-ITEM-008", "FVX-ITEM-009", "FVX-ITEM-010"));
        profiles.put("08_TYPES_FULL", Arrays.asList("FVX-TYPE-001", "FVX-TYPE-002", "FVX-TYPE-003"));
        profiles.put("09_GRAPHICS_PALETTES", Arrays.asList("FVX-GFX-001", "FVX-GFX-002", "FVX-GFX-003", "FVX-GFX-004"));
        profiles.put("RISK_GRAPHICS_PALETTES_VISUAL",
                Arrays.asList("FVX-GFX-001", "FVX-GFX-002", "FVX-GFX-003", "FVX-GFX-004"));
        profiles.put("10_MISC_TWEAKS", Arrays.asList(
                "FVX-MISC-001", "FVX-MISC-002", "FVX-MISC-003", "FVX-MISC-004",
                "FVX-MISC-005", "FVX-MISC-006", "FVX-MISC-007", "FVX-MISC-008",
                "FVX-MISC-009", "FVX-MISC-010", "FVX-MISC-011", "FVX-MISC-012"));
        profiles.put("11_SPECIAL_WILD", Collections.singletonList("FVX-SPECIAL-WILD-001"));
        profiles.put("FOE_MODE_VARIANTS", Collections.singletonList("MODE-FOE-RANDOM"));
        profiles.put("WILD_LOCATION_VARIANTS", Collections.singletonList("MODE-WILD-ENCOUNTER-SET"));
        profiles.put("GENERAL_RESTRICTIONS_VARIANTS", Arrays.asList("FVX-GEN-001", "MODE-NO-RANDOM-INTRO"));
        profiles.put("TYPE_EFFECTIVENESS_EXACT_VARIANTS", Collections.singletonList("MODE-TYPE-RANDOM"));
        return Collections.unmodifiableMap(profiles);
    }

    private static Consumer<Settings> unsupported(String overlayId, String reason) {
        UNSUPPORTED_OVERLAY_REASONS.put(normalize(overlayId), reason);
        return s -> {
            throw new IllegalArgumentException("Unsupported settings feature overlay " + overlayId + ": " + reason);
        };
    }

    private static void setWildZoneMode(Settings settings, Settings.WildPokemonZoneMod zoneMod) {
        settings.setRandomizeWildPokemon(true);
        settings.setWildPokemonZoneMod(zoneMod);
    }

    private static void setGenLimit(Settings settings, boolean allowEvolutionaryRelatives) {
        GenRestrictions restrictions = new GenRestrictions();
        restrictions.setAllowEvolutionaryRelatives(allowEvolutionaryRelatives);
        settings.setLimitPokemon(true);
        settings.setCurrentRestrictions(restrictions);
    }

    private static void enablePokemonPaletteRandomization(Settings settings) {
        settings.setPokemonPalettesMod(Settings.PokemonPalettesMod.RANDOM);
    }

    private static void enableMiscTweak(Settings settings, MiscTweak tweak) {
        settings.setCurrentMiscTweaks(settings.getCurrentMiscTweaks() | tweak.getValue());
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT);
    }

    private static void printUsage() {
        System.out.println("Usage: settings-profile [--help] [--list-features] [--list-profiles]");
        System.out.println("       settings-profile --base-settings <file.rnqs> --output-settings <file.rnqs> [--profile ID] [--enable OVERLAY_ID]");
        System.out.println();
        System.out.println("No ROM is loaded. Profiles and feature overlays modify only Settings API state.");
    }

    private static void printFeatureOverlays() {
        for (String overlayId : FEATURE_OVERLAYS.keySet()) {
            String unsupportedReason = UNSUPPORTED_OVERLAY_REASONS.get(overlayId);
            if (unsupportedReason == null) {
                System.out.println(overlayId + "\tsupported");
            } else {
                System.out.println(overlayId + "\tunsupported\t" + unsupportedReason);
            }
        }
    }

    private static void printProfileOverlays() {
        for (Map.Entry<String, List<String>> profile : PROFILE_OVERLAYS.entrySet()) {
            System.out.println(profile.getKey() + "\t" + String.join(",", profile.getValue()));
        }
    }

    private static final class Arguments {
        private Path baseSettings;
        private Path outputSettings;
        private final List<String> enabledFeatures = new ArrayList<>();
        private final List<String> profiles = new ArrayList<>();
        private boolean help;
        private boolean listFeatures;
        private boolean listProfiles;

        private static Arguments parse(String[] args) {
            Arguments parsed = new Arguments();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "-h":
                    case "--help":
                        parsed.help = true;
                        break;
                    case "--list-features":
                        parsed.listFeatures = true;
                        break;
                    case "--list-profiles":
                        parsed.listProfiles = true;
                        break;
                    case "--base-settings":
                        parsed.baseSettings = Paths.get(requireValue(args, ++i, arg));
                        break;
                    case "--output-settings":
                        parsed.outputSettings = Paths.get(requireValue(args, ++i, arg));
                        break;
                    case "--enable":
                        parsed.enabledFeatures.add(requireValue(args, ++i, arg));
                        break;
                    case "--profile":
                        parsed.profiles.add(requireValue(args, ++i, arg));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            if (!parsed.help && !parsed.listFeatures && !parsed.listProfiles
                    && parsed.enabledFeatures.isEmpty() && parsed.profiles.isEmpty()) {
                throw new IllegalArgumentException("No --enable or --profile overlays were provided.");
            }
            return parsed;
        }

        private static String requireValue(String[] args, int index, String argName) {
            if (index >= args.length || args[index].startsWith("-")) {
                throw new IllegalArgumentException("Missing value for " + argName + ".");
            }
            return args[index];
        }
    }
}
