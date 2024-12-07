package com.dabomstew.pkrandom;

import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.romhandlers.Gen1RomHandler;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.updaters.MoveUpdater;
import com.dabomstew.pkrandom.updaters.SpeciesBaseStatUpdater;
import com.dabomstew.pkrandom.updaters.TypeEffectivenessUpdater;
import com.dabomstew.pkrandom.updaters.Updater;

import java.io.PrintStream;
import java.util.*;

public class RandomizationLogger {

    private static final String NEWLINE = System.lineSeparator();

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

        logHead(log);

        // TODO: should this be reordered to match the GUI?

        maybeLogTypeEffectivenessUpdates(log);
        maybeLogTypeEffectiveness(log);

        maybeLogMoveUpdates(log);

        maybeLogEvolutions(log);

        maybeLogBaseStatUpdates(log);
        maybeLogSpeciesTraits(log);

        maybeLogEvolutionImprovements(log);

        maybeLogStarters(log);

        maybeLogMoveData(log);

        maybeLogMovesets(log);

        maybeLogTMMoves(log);
        maybeLogTMHMCompatibility(log);

        maybeLogMoveTutorMoves(log);
        maybeLogMoveTutorCompatibility(log);

        maybeLogTrainers(log);

        maybeLogStaticPokemon(log);
        maybeLogTotemPokemon(log);

        maybeLogWildPokemon(log);

        maybeLogInGameTrades(log);

        // TODO: log field items
        maybeLogShops(log);
        maybeLogPickupItems(log);

        logTail(log, startTime);
        logDiagnostics(log);
    }

    private void logHead(PrintStream log) {
        log.println("Randomizer Version: " + Version.VERSION_STRING);
        log.println("Random Seed: " + randomSource.getSeed());
        log.println("Settings String: " + Version.VERSION + settings.toString());
        log.println();
    }

    private void logTail(PrintStream log, long startTime) {
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
    }

    private void logDiagnostics(PrintStream log) {
        log.println("--ROM Diagnostics--");
        if (!romHandler.isRomValid(null)) {
            log.println(bundle.getString("Log.InvalidRomLoaded"));
        }
        romHandler.printRomDiagnostics(log);
    }

    private void maybeLogTypeEffectiveness(PrintStream log) {
        if (typeEffUpdater.isUpdated() || typeEffRandomizer.isChangesMade()) {
            log.println("--Type Effectiveness--");
            log.println(romHandler.getTypeTable().toBigString() + NEWLINE);
        }
    }

    private void maybeLogEvolutions(PrintStream log) {
        if (evoRandomizer.isChangesMade()) {
            logEvolutions(log);
        }
    }

    private void logEvolutions(PrintStream log) {
        log.println("--Randomized Evolutions--");
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

        log.println();
    }

    private void maybeLogSpeciesTraits(PrintStream log) {
        if (speciesBSUpdater.isUpdated() || speciesBSRandomizer.isChangesMade() || speciesTypeRandomizer.isChangesMade() ||
                speciesAbilityRandomizer.isChangesMade() || encHeldItemRandomizer.isChangesMade()) {
            logSpeciesTraits(log);
        } else {
            log.println("Pokemon base stats & type: unchanged" + NEWLINE);
        }
    }

    private void logSpeciesTraits(final PrintStream log) {
        List<Species> allPokes = romHandler.getSpeciesInclFormes();
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
        log.println();
    }

    private void maybeLogEvolutionImprovements(PrintStream log) {
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

    private void maybeLogStarters(PrintStream log) {
        if (starterRandomizer.isChangesMade()) {
            logStarters(log);
        }
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
            log.println("Set starter " + i + " to " + starter.getFullName());
            i++;
        }
        log.println();
    }

    private void maybeLogMoveData(PrintStream log) {
        if (moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated()) {
            logMoveData(log);
        } else {
            log.println("Move Data: Unchanged." + NEWLINE);
        }
    }

    private void logMoveData(final PrintStream log) {

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

    private void maybeLogMovesets(PrintStream log) {
        if (speciesMovesetRandomizer.isChangesMade()) {
            logMovesets(log);
        } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("Pokemon Movesets: Metronome Only." + NEWLINE);
        } else {
            log.println("Pokemon Movesets: Unchanged." + NEWLINE);
        }
    }

    private void logMovesets(PrintStream log) {
        log.println("--Pokemon Movesets--");
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
        log.println();
    }

    private void maybeLogTMMoves(PrintStream log) {
        if (tmtMoveRandomizer.isTMChangesMade()) {
            logTMMoves(log);
        } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("TM Moves: Metronome Only." + NEWLINE);
        } else {
            log.println("TM Moves: Unchanged." + NEWLINE);
        }
    }

    private void logTMMoves(PrintStream log) {
        log.println("--TM Moves--");
        List<Integer> tmMoves = romHandler.getTMMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < tmMoves.size(); i++) {
            log.printf("TM%02d %s" + NEWLINE, i + 1, moves.get(tmMoves.get(i)).name);
        }
        log.println();
    }

    private void maybeLogTMHMCompatibility(PrintStream log) {
        if (tmhmtCompRandomizer.isTMHMChangesMade()) {
            logTMHMCompatibility(log);
        }
    }

    private void logTMHMCompatibility(final PrintStream log) {
        log.println("--TM Compatibility--");
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        List<Integer> tmHMs = new ArrayList<>(romHandler.getTMMoves());
        tmHMs.addAll(romHandler.getHMMoves());
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(log, compat, tmHMs, moveData, true);
    }

    private void maybeLogMoveTutorMoves(PrintStream log) {
        if (romHandler.hasMoveTutors()) {
            if (tmtMoveRandomizer.isTutorChangesMade()) {
                logMoveTutorMoves(log, originalMTMoves);
            } else if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
                log.println("Move Tutor Moves: Metronome Only." + NEWLINE);
            } else {
                log.println("Move Tutor Moves: Unchanged." + NEWLINE);
            }
        }
    }

    private void logMoveTutorMoves(PrintStream log, List<Integer> oldMtMoves) {
        log.println("--Move Tutor Moves--");
        List<Integer> newMtMoves = romHandler.getMoveTutorMoves();
        List<Move> moves = romHandler.getMoves();
        for (int i = 0; i < newMtMoves.size(); i++) {
            log.printf("%-10s -> %-10s" + NEWLINE, moves.get(oldMtMoves.get(i)).name,
                    moves.get(newMtMoves.get(i)).name);
        }
        log.println();
    }

    private void maybeLogMoveTutorCompatibility(PrintStream log) {
        if (romHandler.hasMoveTutors() && tmhmtCompRandomizer.isTutorChangesMade()) {
            logTutorCompatibility(log);
        }
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

    private void maybeLogTrainers(PrintStream log) {
        if (trainerPokeRandomizer.isChangesMade() || trainerMovesetRandomizer.isChangesMade()
                || trainerNameRandomizer.isChangesMade()) {
            logTrainers(log, originalTrainerNames, trainerNameRandomizer.isChangesMade(),
                    trainerMovesetRandomizer.isChangesMade());
        } else {
            log.println("Trainers: Unchanged." + NEWLINE);
        }
    }

    private void logTrainers(final PrintStream log, List<String> originalTrainerNames, boolean trainerNamesChanged, boolean logTrainerMovesets) {
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

    private void maybeLogStaticPokemon(PrintStream log) {
        if (romHandler.canChangeStaticPokemon()) {
            if (staticPokeRandomizer.isStaticChangesMade()) {
                logStaticPokemon(log, originalStatics);
            } else {
                log.println("Static Pokemon: Unchanged." + NEWLINE);
            }
        }
    }

    private void logStaticPokemon(final PrintStream log, List<StaticEncounter> oldStatics) {
        List<StaticEncounter> newStatics = romHandler.getStaticPokemon();

        log.println("--Static Pokemon--");
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
        log.println();
    }

    private void maybeLogTotemPokemon(PrintStream log) {
        if (romHandler.hasTotemPokemon()) {
            if (staticPokeRandomizer.isTotemChangesMade()) {
                logTotemPokemon(log, originalTotems);
            } else {
                log.println("Totem Pokemon: Unchanged." + NEWLINE);
            }
        }
    }

    private void logTotemPokemon(final PrintStream log, List<TotemPokemon> oldTotems) {
        List<TotemPokemon> newTotems = romHandler.getTotemPokemon();

        String[] itemNames = romHandler.getItemNames();
        log.println("--Totem Pokemon--");
        for (int i = 0; i < oldTotems.size(); i++) {
            TotemPokemon oldP = oldTotems.get(i);
            TotemPokemon newP = newTotems.get(i);
            log.println(oldP.spec.getFullName() + " =>");
            log.printf(newP.toString(), itemNames[newP.heldItem]);
        }
        log.println();
    }

    private void maybeLogWildPokemon(PrintStream log) {
        if (wildEncounterRandomizer.isChangesMade()) {
            logWildPokemon(log);
        } else {
            log.println("Wild Pokemon: Unchanged." + NEWLINE);
        }
    }

    private void logWildPokemon(final PrintStream log) {

        log.println("--Wild Pokemon--");
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
        log.println();
    }

    private void maybeLogInGameTrades(PrintStream log) {
        if (tradeRandomizer.isChangesMade()) {
            logInGameTrades(log, originalTrades);
        }
    }

    private void logInGameTrades(PrintStream log, List<IngameTrade> oldTrades) {
        log.println("--In-Game Trades--");
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
        log.println();
    }

    private void maybeLogShops(PrintStream log) {
        if (itemRandomizer.isShopChangesMade()) {
            logShops(log);
        }
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

    private void maybeLogPickupItems(PrintStream log) {
        if (itemRandomizer.isPickupChangesMade()) {
            logPickupItems(log);
        }
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

    private void maybeLogMoveUpdates(PrintStream log) {
        if (settings.isUpdateMoves()) {
            logMoveUpdates(log);
        }
    }

    private void logMoveUpdates(PrintStream log) {
        log.println("--Move Updates--");
        log.print("The following moves have been updated, to be in line with Gen ");
        log.println(settings.getUpdateMovesToGeneration() + ".");
        log.println("Note that if you also randomized move data, these changes may be overwritten.");
        log.println();

        Map<Move, List<Updater.Update>> updates = moveUpdater.getUpdates();
        for (Map.Entry<Move, List<Updater.Update>> entry : updates.entrySet()) {
            log.println(entry.getKey().name);
            for (Updater.Update update : entry.getValue()) {
                log.println("\t" + update);
            }
        }
        log.println();
    }

    private void maybeLogBaseStatUpdates(PrintStream log) {
        if (settings.isUpdateBaseStats()) {
            logBaseStatsUpdates(log);
        }
    }

    private void logBaseStatsUpdates(PrintStream log) {
        log.println("--Pokémon Base Stat Updates--");
        log.print("The following base stats have been updated, to be in line with Gen ");
        log.println(settings.getUpdateBaseStatsToGeneration() + ".");
        log.println("Note that if you also randomized Pokémon base stats, these changes may be overwritten.");
        log.println();

        Map<Species, List<Updater.Update>> updates = speciesBSUpdater.getUpdates();
        for (Map.Entry<Species, List<Updater.Update>> entry : updates.entrySet()) {
            log.println(entry.getKey().getFullName());
            for (Updater.Update update : entry.getValue()) {
                log.println("\t" + update);
            }
        }
        log.println();
    }

    private void maybeLogTypeEffectivenessUpdates(PrintStream log) {
        if (settings.isUpdateTypeEffectiveness()) {
            logTypeEffectivenessUpdates(log);
        }
    }

    private void logTypeEffectivenessUpdates(PrintStream log) {
        log.println("--Type Effectiveness Updates--");
        log.print("The following parts of the Type effectiveness chart have been updated, to be in line with Gen ");
        log.println(romHandler.generationOfPokemon() == 1 ? 2 : 6 + ".");
        log.println("Note that if you also randomized Type effectiveness, these changes may be overwritten.");
        log.println();

        Map<Type, List<Updater.Update>> updates = typeEffUpdater.getUpdates();
        for (Map.Entry<Type, List<Updater.Update>> entry : updates.entrySet()) {
            log.println(entry.getKey() + " when used against...");
            for (Updater.Update update : entry.getValue()) {
                log.println("\t" + update);
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


