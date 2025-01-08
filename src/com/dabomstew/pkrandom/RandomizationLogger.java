package com.dabomstew.pkrandom;

import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.romhandlers.Gen1RomHandler;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.updaters.*;

import java.io.PrintStream;
import java.util.*;

public class RandomizationLogger {

    private static final String NEWLINE = System.lineSeparator();
    private static final String LOGO =
            "==========================================================%n" +
            "|   _   _        _                           _           |%n" +
            "|  | | | | _ _  (_)__ __ ___  _ _  ___ __ _ | |          |%n" +
            "|  | |_| || ' \\ | |\\ V // -_)| '_|(_-</ _` || |          |%n" +
            "|   \\___/ |_||_||_| \\_/ \\___||_|  /__/\\__,_||_|          |%n" +
            "|   ___       _     __                                   |%n" +
            "|  | _ \\ ___ | |__ /_/  _ __   ___  _ _                  |%n" +
            "|  |  _// _ \\| / // -_)| '  \\ / _ \\| ' \\                 |%n" +
            "|  |_|  \\___/|_\\_\\\\___||_|_|_|\\___/|_||_|                |%n" +
            "|   ___                 _              _                 |%n" +
            "|  | _ \\ __ _  _ _   __| | ___  _ __  (_) ___ ___  _ _   |%n" +
            "|  |   // _` || ' \\ / _` |/ _ \\| '  \\ | ||_ // -_)| '_|  |%n" +
            "|  |_|_\\\\__,_||_||_|\\__,_|\\___/|_|_|_||_|/__|\\___||_|    |%n" +
            "|   ___ __   ____  __                                    |%n" +
            "|  | __|\\ \\ / /\\ \\/ /                                    |%n" +
            "|  | _|  \\ V /  >  <                                     |%n" +
            "|  |_|    \\_/  /_/\\_\\  %-10s                        |%n" +
            "|                                                        |%n" +
            "==========================================================%n%n";

    private static final String SECTION_SEPARATOR =
            NEWLINE + "==========================================================" + NEWLINE;
    private static final String SECTION_TITLE = " ( %s {%s} )%n%n";

    // At some point, it makes sense to let RandomizationLogger use a ResourceBundle for all its strings.
    // Until then, this string array may live here. It's a statement of intent of sorts,
    // to keep pretty strings out of the data classes/enums.
    private static final String[] EFFECTIVENESS_NAMES = {
            "No Effect/Immune", "Not Very Effective", "Neutral", "Super Effective"
    };

    private final RandomSource randomSource;
    private final Settings settings;
    private final RomHandler romHandler;
    private final ResourceBundle bundle;

    private final List<Integer> originalMTMoves;
    private final List<String> originalTrainerNames;
    private final List<StaticEncounter> originalStatics;
    private final List<TotemPokemon> originalTotems;
    private final List<IngameTrade> originalTrades;

    // this huge list of shared attributes with GameRandomizer could probably be shared in a better way...
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

    private PrintStream log;

    public RandomizationLogger(RandomSource randomSource, Settings settings, RomHandler romHandler, ResourceBundle bundle,
                               SpeciesBaseStatUpdater speciesBSUpdater, MoveUpdater moveUpdater,
                               TypeEffectivenessUpdater typeEffUpdater, IntroPokemonRandomizer introPokeRandomizer,
                               SpeciesBaseStatRandomizer speciesBSRandomizer, SpeciesTypeRandomizer speciesTypeRandomizer,
                               SpeciesAbilityRandomizer speciesAbilityRandomizer, EvolutionRandomizer evoRandomizer,
                               StarterRandomizer starterRandomizer, StaticPokemonRandomizer staticPokeRandomizer,
                               TradeRandomizer tradeRandomizer, MoveDataRandomizer moveDataRandomizer,
                               SpeciesMovesetRandomizer speciesMovesetRandomizer, TrainerPokemonRandomizer trainerPokeRandomizer,
                               TrainerMovesetRandomizer trainerMovesetRandomizer, TrainerNameRandomizer trainerNameRandomizer,
                               WildEncounterRandomizer wildEncounterRandomizer, EncounterHeldItemRandomizer encHeldItemRandomizer,
                               TMTutorMoveRandomizer tmtMoveRandomizer, TMHMTutorCompatibilityRandomizer tmhmtCompRandomizer,
                               ItemRandomizer itemRandomizer, TypeEffectivenessRandomizer typeEffRandomizer,
                               PaletteRandomizer paletteRandomizer, MiscTweakRandomizer miscTweakRandomizer) {
        this.randomSource = randomSource;
        this.settings = settings;
        this.romHandler = romHandler;
        this.bundle = bundle;

        this.originalMTMoves = romHandler.getMoveTutorMoves();
        this.originalTrainerNames = getTrainerNames(romHandler.getTrainers());
        this.originalStatics = romHandler.canChangeStaticPokemon() ? romHandler.getStaticPokemon() : null;
        this.originalTotems = romHandler.hasTotemPokemon() ? romHandler.getTotemPokemon() : null;
        this.originalTrades = romHandler.getIngameTrades();

        this.speciesBSUpdater = speciesBSUpdater;
        this.moveUpdater = moveUpdater;
        this.typeEffUpdater = typeEffUpdater;
        this.introPokeRandomizer = introPokeRandomizer;
        this.speciesBSRandomizer = speciesBSRandomizer;
        this.speciesTypeRandomizer = speciesTypeRandomizer;
        this.speciesAbilityRandomizer = speciesAbilityRandomizer;
        this.evoRandomizer = evoRandomizer;
        this.starterRandomizer = starterRandomizer;
        this.staticPokeRandomizer = staticPokeRandomizer;
        this.tradeRandomizer = tradeRandomizer;
        this.moveDataRandomizer = moveDataRandomizer;
        this.speciesMovesetRandomizer = speciesMovesetRandomizer;
        this.trainerPokeRandomizer = trainerPokeRandomizer;
        this.trainerMovesetRandomizer = trainerMovesetRandomizer;
        this.trainerNameRandomizer = trainerNameRandomizer;
        this.wildEncounterRandomizer = wildEncounterRandomizer;
        this.encHeldItemRandomizer = encHeldItemRandomizer;
        this.tmtMoveRandomizer = tmtMoveRandomizer;
        this.tmhmtCompRandomizer = tmhmtCompRandomizer;
        this.itemRandomizer = itemRandomizer;
        this.typeEffRandomizer = typeEffRandomizer;
        this.paletteRandomizer = paletteRandomizer;
        this.miscTweakRandomizer = miscTweakRandomizer;
    }

    public void logResults(PrintStream log, long startTime) {
        this.log = log;

        logHead();
        logTableOfContents();
        logOverview();
        logOptionalSections();
        logStatistics(startTime);
        logDiagnostics();
    }

    private void logHead() {
        // TODO: figure out how to fit the version into the Logo
        log.printf(LOGO, "v" + Version.VERSION_STRING);
        log.println(" [ Log for Randomized Game ]");
        log.println();
        String gameName = romHandler.getROMName();
        if (romHandler.hasGameUpdateLoaded()) {
            gameName = gameName + " (" + romHandler.getGameUpdateVersion() + ")";
        }
        log.println("Base Game: " + gameName);
        log.println("Randomizer Version: " + Version.LATEST_VERSION.branchName + " " + Version.VERSION_STRING);
        log.println("Random Seed: " + randomSource.getSeed());
        log.println("Settings String: " + Version.VERSION + settings.toString());
        log.println();
        log.println("If you are having problems using the Universal Pokémon Randomizer FVX,");
        log.println("please consult the wiki or leave an issue on the project's GitHub page.");
        log.println();
        log.println("Wiki link: "); // TODO: the link
        log.println("GitHub issues page link: "); // TODO: the link
        log.println(SECTION_SEPARATOR);
    }

    private void logTableOfContents() {
        log.printf(SECTION_TITLE, "Table of Contents", "TABL");
        log.println("Search (ctrl-f) for the strings in curly brackets {}");
        log.println("to find the below sections quickly.");
        printContentsRow("Table of Contents", "TABL");
        printContentsRow("Overview of Randomization", "OVRD");
        printOptionalContentsRows();
        printContentsRow("Randomization Statistics", "STAT");
        printContentsRow("Randomization/ROM Diagnostics", "DIAG");
        log.println(SECTION_SEPARATOR);
    }

    private void printContentsRow(String name, String shortcut) {
        log.print(" " + name);
        log.print(new String(new char[49 - name.length()]).replace('\0', '-'));
        log.println("{" + shortcut + "}");
    }

    private void printOptionalContentsRows() {
        // TODO: where to put updates + evolution improvements?
        log.println();
        if (shouldLogSpeciesTraits())
            printContentsRow("Pokémon Base Statistics / Types / Abilities", "PKST");
        if (shouldLogEvolutions())
            printContentsRow("Pokémon Evolutions", "PKEV");
        if (shouldLogSpeciesTraits() || shouldLogEvolutions())
            log.println();
        if (shouldLogStarters())
            printContentsRow("Starter Pokémon", "SRPK");
        if (shouldLogStaticPokemon())
            printContentsRow("Static Pokémon", "STPK");
        if (shouldLogInGameTrades())
            printContentsRow("In-Game Trades", "IGTR");
        if (shouldLogStarters() || shouldLogStaticPokemon() || shouldLogInGameTrades())
            log.println();
        if (shouldLogMoveData())
            printContentsRow("Move Data", "MVDT");
        if (shouldLogMovesets())
            printContentsRow("Pokémon Movesets", "PKMV");
        if (shouldLogMoveData() || shouldLogMovesets())
            log.println();
        if (shouldLogTrainers())
            printContentsRow("Trainer Pokémon", "TRPK");
        if (shouldLogTotemPokemon())
            printContentsRow("Totem Pokémon", "TOPK");
        if (shouldLogTrainers() || shouldLogTotemPokemon())
            log.println();
        if (shouldLogWildPokemon()) {
            printContentsRow("Wild Pokémon", "WDPK");
            log.println();
        }
        if (shouldLogTMMoves())
            printContentsRow("TM Moves", "TMMV");
        if (shouldLogTMHMCompatibility())
            printContentsRow("TM/HM Compatibility", "TMCB");
        if (shouldLogMoveTutorMoves())
            printContentsRow("Move Tutor Moves", "MTMV");
        if (shouldLogMoveTutorCompatibility())
            printContentsRow("Move Tutor Compatibility", "MTCB");
        if (shouldLogTMMoves() || shouldLogTMHMCompatibility()
                || shouldLogMoveTutorMoves() || shouldLogMoveTutorCompatibility())
            log.println();
        if (shouldLogShopItems())
            printContentsRow("Shop Items", "SHMS");
        if (shouldLogPickupItems())
            printContentsRow("Pickup Items", "PUMS");
        if (shouldLogShopItems() || shouldLogPickupItems())
            log.println();
        if (shouldLogTypeEffectiveness()) {
            printContentsRow("Type Effectiveness", "TPEF");
            log.println();
        }
    }

    private void logOverview() {
        log.printf(SECTION_TITLE, "Overview of Randomization", "OVRD");
        // TODO list all randomized sections ( more atomic than the table of contents?? )
        // TODO list all *non*-randomized sections
        log.println("The following Misc. Tweaks were applied:");
        int miscTweaks = settings.getCurrentMiscTweaks();
        for (MiscTweak mt : MiscTweak.allTweaks) {
            if ((miscTweaks & mt.getValue()) != 0) {
                log.println(mt.getTweakName());
            }
        }
        log.print(SECTION_SEPARATOR);
    }

    private void logStatistics(long startTime) {
        log.printf(SECTION_TITLE, "Randomization Statistics", "STAT");
        // TODO: is this correct? should not it just measure the randomization, sans logging?
        log.println("Time elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
        log.println("RNG calls (non-cosmetic): " + randomSource.callsSinceSeedNonCosmetic());
        log.println("RNG calls (cosmetic)    : " + randomSource.callsSinceSeedCosmetic());
        log.println("RNG calls (total)       : " + randomSource.callsSinceSeed());
        log.println(SECTION_SEPARATOR);
    }

    private void logDiagnostics() {
        log.printf(SECTION_TITLE, "ROM Diagnostics", "DIAG");
        if (!romHandler.isRomValid(null)) {
            log.println(bundle.getString("Log.InvalidRomLoaded"));
        }
        romHandler.printRomDiagnostics(log);
    }

    /**
     * The meat of the logging; logs all sections about the randomization results themselves,
     * other than the overview. They are optional because e.g. the Trainer Pokémon section
     * won't show up if they weren't randomized.
     */
    private void logOptionalSections() {
        if (shouldLogSpeciesTraits())
            logSpeciesTraits();
        if (shouldLogEvolutions())
            logEvolutions();

        if (shouldLogStarters())
            logStarters();
        if (shouldLogStaticPokemon())
            logStaticPokemon(originalStatics);
        if (shouldLogInGameTrades())
            logInGameTrades(originalTrades);

        if (shouldLogMoveData())
            logMoveData();
        if (shouldLogMovesets())
            logMovesets();

        if (shouldLogTrainers())
            logTrainers(originalTrainerNames);
        if (shouldLogTotemPokemon())
            logTotemPokemon(originalTotems);

        if (shouldLogWildPokemon())
            logWildPokemon();

        if (shouldLogTMMoves())
            logTMMoves();
        if (shouldLogTMHMCompatibility())
            logTMHMCompatibility();
        if (shouldLogMoveTutorMoves())
            logMoveTutorMoves(originalMTMoves);
        if (shouldLogMoveTutorCompatibility())
            logMoveTutorCompatibility();

        // TODO: log field items
        if (shouldLogShopItems())
            logShopItems();
        if (shouldLogPickupItems())
            logPickupItems();

        if (shouldLogTypeEffectiveness())
            logTypeEffectiveness();

        // TODO: where to fit these
        logBaseStatsUpdates();
        logEvolutionImprovements();
        logMoveUpdates();
        logTypeEffectivenessUpdates();
    }

    // TODO: refactor the below so they are formatted alike/like "sections"

    private boolean shouldLogTypeEffectiveness() {
        return typeEffUpdater.isUpdated() || typeEffRandomizer.isChangesMade();
    }

    private void logTypeEffectiveness() {
        log.printf(SECTION_TITLE, "Type Effectiveness", "TPEF");
        log.println(romHandler.getTypeTable().toBigString() + NEWLINE);
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogEvolutions() {
        return evoRandomizer.isChangesMade();
    }

    private void logEvolutions() {
        log.printf(SECTION_TITLE, "Pokémon Evolutions", "PKEV");
        List<Species> allPokes = romHandler.getSpeciesInclFormes();
        for (Species pk : allPokes) {
            if (pk != null && !pk.isActuallyCosmetic()) {
                int numEvos = pk.getEvolutionsFrom().size();
                if (numEvos > 0) {
                    StringBuilder evoStr = new StringBuilder(pk.getEvolutionsFrom().get(0).getTo().getFullName());
                    for (int i = 1; i < numEvos; i++) {
                        if (i == numEvos - 1) {
                            evoStr.append(" and ").append(pk.getEvolutionsFrom().get(i).getTo().getFullName());
                        } else {
                            evoStr.append(", ").append(pk.getEvolutionsFrom().get(i).getTo().getFullName());
                        }
                    }
                    log.printf("%-15s -> %-15s" + NEWLINE, pk.getFullName(), evoStr);
                }
            }
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogSpeciesTraits() {
        return (speciesBSUpdater.isUpdated() || speciesBSRandomizer.isChangesMade()
                || speciesTypeRandomizer.isChangesMade() || speciesAbilityRandomizer.isChangesMade()
                || encHeldItemRandomizer.isChangesMade()) ;
    }

    private void logSpeciesTraits() {
        List<Species> allPokes = romHandler.getSpeciesInclFormes();
        String[] itemNames = romHandler.getItemNames();
        // Log base stats & types
        log.printf(SECTION_TITLE, "Pokémon Base Statistics / Types / Abilities", "PKST");
        if (romHandler instanceof Gen1RomHandler) {
            log.println("NUM|NAME      |TYPE             |  HP| ATK| DEF| SPE|SPEC");
            for (Species pkmn : allPokes) {
                if (pkmn != null) {
                    String typeString = pkmn.getPrimaryType(false) == null ? "???" : pkmn.getPrimaryType(false).toString();
                    if (pkmn.getSecondaryType(false) != null) {
                        typeString += "/" + pkmn.getSecondaryType(false).toString();
                    }
                    log.printf("%3d|%-10s|%-17s|%4d|%4d|%4d|%4d|%4d" + NEWLINE, pkmn.getNumber(), pkmn.getFullName(), typeString,
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
            int abils = romHandler.abilitiesPerSpecies();
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
                    log.printf("%3d|" + nameSpFormat + "|%-17s|%4d|%4d|%4d|%4d|%4d|%4d", i, pkmn.getFullName(), typeString,
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
        log.println(SECTION_SEPARATOR);
    }

    private void logEvolutionImprovements() {
        // TODO
        if (settings.isChangeImpossibleEvolutions()) {
            log.println("--Removing Impossible Evolutions--");
            logUpdatedEvolutions(romHandler.getImpossibleEvoUpdates(), romHandler.getEasierEvoUpdates());
        }
        if (settings.isMakeEvolutionsEasier()) {
            log.println("--Making Evolutions Easier--");
            if (!(romHandler instanceof Gen1RomHandler)) {
                log.println("Friendship evolutions now take 160 happiness (was 220).");
            }
            logUpdatedEvolutions(romHandler.getEasierEvoUpdates(), null);
        }
        if (settings.isRemoveTimeBasedEvolutions()) {
            log.println("--Removing Timed-Based Evolutions--");
            logUpdatedEvolutions(romHandler.getTimeBasedEvoUpdates(), null);
        }
    }

    private void logUpdatedEvolutions(Set<EvolutionUpdate> updatedEvolutions,
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

    private boolean shouldLogStarters() {
        return starterRandomizer.isChangesMade();
    }

    private void logStarters() {

        // TODO: log starter held items
        log.printf(SECTION_TITLE, "Starter Pokémon", "SRPK");
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
            log.println("Set starter " + i + " to " + starter.getFullName());
            i++;
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogMoveData() {
        return (moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated());
    }

    private void logMoveData() {

        log.printf(SECTION_TITLE, "Move Data", "MVDT");
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogMovesets() {
        // TODO: how to mark metronome mode??
        return speciesMovesetRandomizer.isChangesMade();
    }

    private void logMovesets() {
        log.printf(SECTION_TITLE, "Pokémon Movesets", "PKMV");
        List<String> movesets = new ArrayList<>();
        Map<Integer, List<MoveLearnt>> moveData = romHandler.getMovesLearnt();
        Map<Integer, List<Integer>> eggMoves = romHandler.getEggMoves();
        List<Move> moves = romHandler.getMoves();
        List<Species> pkmnList = romHandler.getSpeciesInclFormes();
        int i = 1;
        for (Species pkmn : pkmnList) {
            if (pkmn == null || pkmn.isActuallyCosmetic()) {
                continue;
            }
            StringBuilder evoStr = new StringBuilder();
            try {
                evoStr.append(" -> ").append(pkmn.getEvolutionsFrom().get(0).getTo().getFullName());
            } catch (Exception e) {
                evoStr.append(" (no evolution)");
            }

            StringBuilder sb = new StringBuilder();

            if (romHandler instanceof Gen1RomHandler) {
                sb.append(String.format("%03d %s", i, pkmn.getFullName()))
                        .append(evoStr).append(NEWLINE)
                        .append(String.format("HP   %-3d", pkmn.getHp())).append(NEWLINE)
                        .append(String.format("ATK  %-3d", pkmn.getAttack())).append(NEWLINE)
                        .append(String.format("DEF  %-3d", pkmn.getDefense())).append(NEWLINE)
                        .append(String.format("SPEC %-3d", pkmn.getSpecial())).append(NEWLINE)
                        .append(String.format("SPE  %-3d", pkmn.getSpeed())).append(NEWLINE);
            } else {
                sb.append(String.format("%03d %s", i, pkmn.getFullName()))
                        .append(evoStr).append(NEWLINE)
                        .append(String.format("HP  %-3d", pkmn.getHp())).append(NEWLINE)
                        .append(String.format("ATK %-3d", pkmn.getAttack())).append(NEWLINE)
                        .append(String.format("DEF %-3d", pkmn.getDefense())).append(NEWLINE)
                        .append(String.format("SPA %-3d", pkmn.getSpatk())).append(NEWLINE)
                        .append(String.format("SPD %-3d", pkmn.getSpdef())).append(NEWLINE)
                        .append(String.format("SPE %-3d", pkmn.getSpeed())).append(NEWLINE);
            }

            i++;

            List<MoveLearnt> data = moveData.get(pkmn.getNumber());
            for (MoveLearnt ml : data) {
                try {
                    if (ml.level == 0) {
                        sb.append("Learned upon evolution: ")
                                .append(moves.get(ml.move).name).append(NEWLINE);
                    } else {
                        sb.append("Level ")
                                .append(String.format("%-2d", ml.level))
                                .append(": ")
                                .append(moves.get(ml.move).name).append(NEWLINE);
                    }
                } catch (NullPointerException ex) {
                    sb.append("invalid move at level").append(ml.level);
                }
            }
            List<Integer> eggMove = eggMoves.get(pkmn.getNumber());
            if (eggMove != null && !eggMove.isEmpty()) {
                sb.append("Egg Moves:").append(NEWLINE);
                for (Integer move : eggMove) {
                    sb.append(" - ").append(moves.get(move).name).append(NEWLINE);
                }
            }

            movesets.add(sb.toString());
        }
        Collections.sort(movesets);
        for (String moveset : movesets) {
            log.println(moveset);
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogTMMoves() {
        // TODO: again, how to deal with metronome mode
        return tmtMoveRandomizer.isTMChangesMade();
    }

    private void logTMMoves() {
        log.printf(SECTION_TITLE, "TM Moves", "TMMV");
        List<Integer> tmMoves = romHandler.getTMMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < tmMoves.size(); i++) {
            log.printf("TM%02d %s" + NEWLINE, i + 1, moves.get(tmMoves.get(i)).name);
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogTMHMCompatibility() {
        return tmhmtCompRandomizer.isTMHMChangesMade();
    }

    private void logTMHMCompatibility() {
        log.printf(SECTION_TITLE, "TM Compatibility", "TMCM");
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        List<Integer> tmHMs = new ArrayList<>(romHandler.getTMMoves());
        tmHMs.addAll(romHandler.getHMMoves());
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(compat, tmHMs, moveData, true);
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogMoveTutorMoves() {
        return romHandler.hasMoveTutors() && tmtMoveRandomizer.isTutorChangesMade();
    }

    private void logMoveTutorMoves(List<Integer> oldMtMoves) {
        log.printf(SECTION_TITLE, "Move Tutor Moves", "MTMV");
        List<Integer> newMtMoves = romHandler.getMoveTutorMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < newMtMoves.size(); i++) {
            log.printf("%-10s -> %-10s" + NEWLINE, moves.get(oldMtMoves.get(i)).name,
                    moves.get(newMtMoves.get(i)).name);
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogMoveTutorCompatibility() {
        return romHandler.hasMoveTutors() && tmhmtCompRandomizer.isTutorChangesMade();
    }

    private void logMoveTutorCompatibility() {
        log.printf(SECTION_TITLE, "Move Tutor Compatibility", "MTCB");
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        List<Integer> tutorMoves = romHandler.getMoveTutorMoves();
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(compat, tutorMoves, moveData, false);
        log.println(SECTION_SEPARATOR);
    }

    private void logCompatibility(Map<Species, boolean[]> compat, List<Integer> moveList,
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
            log.printf("%3d " + nameSpFormat, pkmn.getNumber(), pkmn.getFullName() + " ");

            for (int i = 1; i < flags.length; i++) {
                String moveName = moveData.get(moveList.get(i - 1)).name;
                if (moveName.isEmpty()) {
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

    private boolean shouldLogTrainers() {
        return trainerPokeRandomizer.isChangesMade() || trainerMovesetRandomizer.isChangesMade()
                || trainerNameRandomizer.isChangesMade();
    }

    private void logTrainers(List<String> originalTrainerNames) {
        log.printf(SECTION_TITLE, "Trainer Pokémon", "TRPK");
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
                if (trainerNameRandomizer.isChangesMade()) {
                    log.printf("(%s => %s)", originalTrainerName, currentTrainerName);
                } else {
                    log.printf("(%s)", currentTrainerName);
                }
            }
            if (t.offset != 0) {
                log.printf("@%X", t.offset);
            }

            String[] itemNames = romHandler.getItemNames();
            if (trainerMovesetRandomizer.isChangesMade()) {
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogStaticPokemon() {
        return romHandler.canChangeStaticPokemon() && staticPokeRandomizer.isStaticChangesMade();
    }

    private void logStaticPokemon(List<StaticEncounter> oldStatics) {
        List<StaticEncounter> newStatics = romHandler.getStaticPokemon();

        log.printf(SECTION_TITLE, "Static Pokémon", "STPK");
        Map<String, Integer> seenPokemon = new TreeMap<>();
        for (int i = 0; i < oldStatics.size(); i++) {
            StaticEncounter oldP = oldStatics.get(i);
            StaticEncounter newP = newStatics.get(i);
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogTotemPokemon() {
        return romHandler.hasTotemPokemon() && staticPokeRandomizer.isTotemChangesMade();
    }

    private void logTotemPokemon(List<TotemPokemon> oldTotems) {
        List<TotemPokemon> newTotems = romHandler.getTotemPokemon();

        String[] itemNames = romHandler.getItemNames();
        log.printf(SECTION_TITLE, "Totem Pokémon", "TOPK");
        for (int i = 0; i < oldTotems.size(); i++) {
            TotemPokemon oldP = oldTotems.get(i);
            TotemPokemon newP = newTotems.get(i);
            log.println(oldP.spec.getFullName() + " =>");
            log.printf(newP.toString(), itemNames[newP.heldItem]);
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogWildPokemon() {
        return wildEncounterRandomizer.isChangesMade();
    }

    private void logWildPokemon() {
        log.printf(SECTION_TITLE, "Wild Pokémon", "WDPK");
        boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                (!settings.isRandomizeWildPokemon() && settings.isWildLevelsModified());
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
                sb.append(e.getSpecies().getFullName()).append(" Lv");
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogInGameTrades() {
        return tradeRandomizer.isChangesMade();
    }

    private void logInGameTrades(List<IngameTrade> oldTrades) {
        log.printf(SECTION_TITLE, "In-Game Trades", "IGTR");
        List<IngameTrade> newTrades = romHandler.getIngameTrades();
        int size = oldTrades.size();
        for (int i = 0; i < size; i++) {
            IngameTrade oldT = oldTrades.get(i);
            IngameTrade newT = newTrades.get(i);
            log.printf("Trade %-11s -> %-11s the %-11s        ->      %-11s -> %-15s the %s" + NEWLINE,
                    oldT.requestedSpecies != null ? oldT.requestedSpecies.getFullName() : "Any",
                    oldT.nickname, oldT.givenSpecies.getFullName(),
                    newT.requestedSpecies != null ? newT.requestedSpecies.getFullName() : "Any",
                    newT.nickname, newT.givenSpecies.getFullName());
        }
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogShopItems() {
        return itemRandomizer.isShopChangesMade();
    }

    private void logShopItems() {
        String[] itemNames = romHandler.getItemNames();
        log.printf(SECTION_TITLE, "Shop Items", "SHMS");
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogPickupItems() {
        return itemRandomizer.isPickupChangesMade();
    }

    private void logPickupItems() {
        List<PickupItem> pickupItems = romHandler.getPickupItems();
        String[] itemNames = romHandler.getItemNames();
        log.printf(SECTION_TITLE, "Pickup Items", "PUMS");
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
        log.println(SECTION_SEPARATOR);
    }

    private boolean shouldLogMoveUpdates() {
        return moveUpdater.isUpdated();
    }
    
    private void logMoveUpdates() {
        // TODO
        log.println("--Move Updates--");
        log.print("The following moves have been updated, to be in line with Generation ");
        log.println(settings.getUpdateMovesToGeneration() + ".");
        log.println("Note that if you also randomized move data, these changes may be overwritten.");
        log.println();

        Map<Move, Map<MoveUpdateType, Update<Object>>> updates = moveUpdater.getUpdates();
        for (Map.Entry<Move, Map<MoveUpdateType, Update<Object>>> outer : updates.entrySet()) {
            log.println(outer.getKey().name + ":");
            for (Map.Entry<MoveUpdateType, Update<Object>> inner : outer.getValue().entrySet()) {

                log.printf("\t%-8s: ", inner.getKey());
                switch (inner.getKey()) {
                    case POWER:
                    case PP:
                        log.printf("%4d -> %4d%n",
                                (Integer) inner.getValue().getBefore(),
                                (Integer) inner.getValue().getAfter());
                        break;
                    case ACCURACY:
                        log.printf("%3.0f%% -> %3.0f%%%n",
                                (Double) inner.getValue().getBefore(),
                                (Double) inner.getValue().getAfter());
                        break;
                    case TYPE:
                    case CATEGORY:
                        Object before = inner.getValue().getBefore() == null ? "???" : inner.getValue().getBefore();
                        log.printf(" %s -> %s%n", before, inner.getValue().getAfter());
                        break;
                }
            }
        }
        log.println();
    }

    private boolean shouldLogBaseStatUpdates() {
        return speciesBSUpdater.isUpdated();
    }

    private void logBaseStatsUpdates() {
        // TODO
        log.println("--Pokémon Base Stat Updates--");
        log.print("The following base stats have been updated, to be in line with Generation ");
        log.println(settings.getUpdateBaseStatsToGeneration() + ".");
        log.println("Note that if you also randomized Pokémon base stats, these changes may be overwritten.");
        log.println();

        Map<Species, Map<BSUpdateType, Update<Integer>>> updates = speciesBSUpdater.getUpdates();
        for (Map.Entry<Species, Map<BSUpdateType, Update<Integer>>> outer : updates.entrySet()) {
            log.println(outer.getKey().getFullName() + ":");
            for (Map.Entry<BSUpdateType, Update<Integer>> inner : outer.getValue().entrySet()) {
                log.printf("\t%-7s: %3d -> %3d%n",
                        inner.getKey(),
                        inner.getValue().getBefore(), inner.getValue().getAfter());
            }
        }
        log.println();
    }

    private boolean shouldLogTypeEffectivenessUpdates() {
        return typeEffUpdater.isUpdated();
    }

    private void logTypeEffectivenessUpdates() {
        log.println("--Type Effectiveness Updates--");
        log.print("The following parts of the Type effectiveness chart have been updated, to be in line with Generation ");
        log.println(romHandler.generationOfPokemon() == 1 ? 2 : 6 + ".");
        log.println("Note that if you also randomized Type effectiveness, these changes may be overwritten.");
        log.println();

        Map<Type, Map<Type, Update<Effectiveness>>> updates = typeEffUpdater.getUpdates();
        for (Map.Entry<Type, Map<Type, Update<Effectiveness>>> outer : updates.entrySet()) {
            log.println(outer.getKey() + " when used against...");
            for (Map.Entry<Type, Update<Effectiveness>> inner : outer.getValue().entrySet()) {
                log.printf("\t%-8s:   %-18s -> %-18s%n",
                        inner.getKey(),
                        EFFECTIVENESS_NAMES[inner.getValue().getBefore().ordinal()],
                        EFFECTIVENESS_NAMES[inner.getValue().getAfter().ordinal()]);
            }
        }
        log.println();
    }

    private List<String> getTrainerNames(List<Trainer> trainers) {
        List<String> trainerNames = new ArrayList<>();
        trainerNames.add(""); // for index 0
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

}


