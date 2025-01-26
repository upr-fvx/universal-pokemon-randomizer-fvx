package com.dabomstew.pkrandom;

import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.romhandlers.Gen1RomHandler;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.updaters.*;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Just a shorter alias of {@link ResourceBundle#getString(String) bundle.getString(String)},
     * since we're using that a lot.
     */
    private String getBS(String key) {
        return bundle.getString(key);
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

    private void printSectionTitle(String bundleSectionID) {
        log.printf(getBS("Log.sectionTitle"),
                getBS("Log." + bundleSectionID + ".title"),
                getBS("Log." + bundleSectionID + ".shortcut"));
    }

    private void printSectionSeparator() {
        log.printf(getBS("Log.sectionSeparator"));
    }

    private void logHead() {
        log.printf(getBS("Log.logo"), Version.VERSION_STRING);
        log.printf(getBS("Log.title"));
        String gameName = romHandler.getROMName();
        if (romHandler.hasGameUpdateLoaded()) {
            gameName = gameName + " (" + romHandler.getGameUpdateVersion() + ")";
        }
        log.printf(getBS("Log.baseGame"), gameName);
        log.printf(getBS("Log.version"), Version.LATEST_VERSION.branchName, Version.VERSION_STRING);
        log.printf(getBS("Log.seed"), randomSource.getSeed());
        log.printf(getBS("Log.settings"), settings.toString());
        log.println();
        log.printf(getBS("Log.problems"));
        log.println();
        log.printf(getBS("Log.wikiLink"), SysConstants.WIKI_URL);
        log.printf(getBS("Log.githubIssuesLink"), SysConstants.ISSUES_URL);
        printSectionSeparator();
    }

    private void logTableOfContents() {
        printSectionTitle("toc");
        log.printf(getBS("Log.toc.instruction"));
        printContentsRow("toc");
        printContentsRow("overview");
        printOptionalContentsRows();
        printContentsRow("stat");
        printContentsRow("dg");
        printSectionSeparator();
    }

    private void printContentsRow(String bundleSectionID) {
        String title = getBS("Log." + bundleSectionID + ".title");
        String shortcut = getBS("Log." + bundleSectionID + ".shortcut");
        printContentsRow(title, shortcut);
    }

    private void printContentsRow(String title, String shortcut) {
        log.print(" " + title);
        log.print(new String(new char[49 - title.length()]).replace('\0', '-'));
        log.println("{" + shortcut + "}");
    }

    private void printOptionalContentsRows() {
        // TODO: where to put updates + evolution improvements?
        log.println();
        if (shouldLogSpeciesTraits())
            printContentsRow("psta");
        if (shouldLogEvolutions())
            printContentsRow("pe");
        if (shouldLogSpeciesTraits() || shouldLogEvolutions())
            log.println();
        if (shouldLogStarters())
            printContentsRow("sp");
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
        printSectionTitle("overview");
        // The overview lines intentionally (mostly) map to panels in the UI, thus they share their bundle strings.
        logOverviewLine(getBS("GUI.pbsPanel.title"), speciesBSRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.ptPanel.title"), speciesTypeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.paPanel.title"), speciesAbilityRandomizer.isChangesMade(),
                romHandler.abilitiesPerSpecies() != 0);
        // TODO: what about evo improvements?
        logOverviewLine(getBS("GUI.pePanel.title"), evoRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.spPanel.title"), starterRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.stpPanel.title"), staticPokeRandomizer.isStaticChangesMade(),
                romHandler.canChangeStaticPokemon());
        logOverviewLine(getBS("GUI.igtPanel.title"), tradeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.mdPanel.title"), moveDataRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.pmsPanel.title"), speciesMovesetRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.tpPanel.title"), trainerPokeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("Log.overview.trainerMovesets"), trainerMovesetRandomizer.isChangesMade(),
                romHandler.generationOfPokemon() >= 3); // TODO: why this?
        logOverviewLine(getBS("Log.overview.trainerNames"), trainerNameRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.totpPanel.title"), staticPokeRandomizer.isTotemChangesMade(),
                romHandler.hasTotemPokemon());
        logOverviewLine(getBS("GUI.wpPanel.title"), wildEncounterRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.tmMovesPanel.title"), tmtMoveRandomizer.isTMChangesMade(), true);
        logOverviewLine(getBS("GUI.thcPanel.title"), tmhmtCompRandomizer.isTMHMChangesMade(), true);
        logOverviewLine(getBS("GUI.mtMovesPanel.title"), tmtMoveRandomizer.isTutorChangesMade(),
                romHandler.hasMoveTutors());
        logOverviewLine(getBS("GUI.mtcPanel.title"), tmhmtCompRandomizer.isTutorChangesMade(),
                romHandler.hasMoveTutors());
        // field items aren't logged properly, but still important to show *whether* they were randomized
        logOverviewLine(getBS("GUI.fiPanel.title"), itemRandomizer.isFieldChangesMade(), true);
        // TODO: okay what should the naming be here? "Special Shops"? "Shop Items"? "Shops"?
        //  Both here and in RandomizerGUI; they should be synced.
        logOverviewLine(getBS("GUI.shPanel.title"), itemRandomizer.isShopChangesMade(),
                romHandler.hasShopSupport());
        logOverviewLine(getBS("GUI.puPanel.title"), itemRandomizer.isPickupChangesMade(),
                romHandler.abilitiesPerSpecies() != 0);
        logOverviewLine(getBS("GUI.tePanel.title"), typeEffRandomizer.isChangesMade(),
                romHandler.hasTypeEffectivenessSupport());
        logOverviewLine(getBS("GUI.ppalPanel.title"), paletteRandomizer != null && paletteRandomizer.isChangesMade(),
                romHandler.hasPokemonPaletteSupport());

        if (miscTweakRandomizer.isChangesMade()) {
            log.printf(getBS("Log.overview.miscTweaks"));
            int miscTweaks = settings.getCurrentMiscTweaks();
            for (MiscTweak mt : MiscTweak.allTweaks) {
                if ((miscTweaks & mt.getValue()) != 0) {
                    log.println(mt.getTweakName());
                }
            }
        } else {
            log.printf(getBS("Log.overview.noMiscTweaks"));
        }
        printSectionSeparator();
    }

    private void logOverviewLine(String line, boolean changed, boolean relevant) {
        if (relevant) {
            log.print(line + ": ");
            log.println(changed ?
                    getBS("Log.overview.changed") :
                    getBS("Log.overview.unchanged"));
        }
    }

    private void logStatistics(long startTime) {
        printSectionTitle("stat");
        log.printf(getBS("Log.stat.time"), System.currentTimeMillis() - startTime);
        log.printf(getBS("Log.stat.callsNonCosmetic"), randomSource.callsSinceSeedNonCosmetic());
        log.printf(getBS("Log.stat.callsCosmetic"), randomSource.callsSinceSeedCosmetic());
        log.printf(getBS("Log.stat.callsTotal"), randomSource.callsSinceSeed());
        printSectionSeparator();
    }

    private void logDiagnostics() {
        printSectionTitle("dg");
        if (!romHandler.isRomValid(null)) {
            log.printf(getBS("Log.dg.invalidRomLoaded"));
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
        //if (shouldLogEvolutions())
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

    private boolean shouldLogTypeEffectiveness() {
        return typeEffUpdater.isUpdated() || typeEffRandomizer.isChangesMade();
    }

    private void logTypeEffectiveness() {
        log.printf(getBS("Log.sectionTitle"), "Type Effectiveness", "TPEF");
        log.println(romHandler.getTypeTable().toBigString() + NEWLINE);
        printSectionSeparator();
    }

    private boolean shouldLogEvolutions() {
        return evoRandomizer.isChangesMade();
    }

    private void logEvolutions() {
        printSectionTitle("pe");
        List<Species> allSpecies = romHandler.getSpeciesInclFormes();
        int nameLen = getMaxSpeciesNameLength(allSpecies);

        // Table head
        log.printf("%-" + nameLen + "s|%-" + nameLen + "s|%s%n",
                getBS("Log.pe.from"), getBS("Log.pe.to"), getBS("Log.pe.method"));

        // Table body
        for (Species pk : allSpecies) {
            if (pk == null || pk.isActuallyCosmetic() || pk.getEvolutionsFrom().isEmpty()) {
                continue;
            }

            for (int i = 0; i < pk.getEvolutionsFrom().size(); i++) {
                Evolution evo = pk.getEvolutionsFrom().get(i);
                String from = i == 0 ? pk.getFullName() : "";
                String to = evo.getTo().getFullName();
                String method = evolutionMethodToString(evo);
                log.printf("%-" + nameLen + "s|%-" + nameLen + "s|%s%n",
                        from, to, method);
            }

        }
        printSectionSeparator();
    }

    private void logEvolutionImprovements() {
        log.printf("Section!!!%n");

        log.printf(getBS("Log.pe.impListHead"));
        if (settings.isChangeImpossibleEvolutions()) {
            log.printf(getBS("Log.pe.impListImpossible"));
        }
        if (settings.isMakeEvolutionsEasier()) {
            log.printf(getBS("Log.pe.impListEasier"));
        }
        if (settings.isRemoveTimeBasedEvolutions()) {
            log.printf(getBS("Log.pe.impListTimeBased"));
        }
        log.println();
        if (settings.isMakeEvolutionsEasier() && romHandler.generationOfPokemon() != 1) {
            log.printf(getBS("Log.pe.impHappiness"));
        }
        log.println();

        List<String> fromNames = new ArrayList<>();
        List<String> toNames = new ArrayList<>();
        List<String> oldMethods = new ArrayList<>();
        List<String> newMethods = new ArrayList<>();

        // rather hefty code, for filling these tables
        // Assumes improvements only adds Evolutions and/or Species (forms) to evolve into;
        // i.e. oldFoo.size() < newFoo.size().
        for (Map.Entry<Species, List<Evolution>> entry : romHandler.getPreImprovedEvolutions().entrySet()) {
            fromNames.add(entry.getKey().getFullName());

            Map<Species, List<Evolution>> oldByTo = new HashMap<>();
            for (Evolution oldEvo : entry.getValue()) {
                oldByTo.putIfAbsent(oldEvo.getTo(), new ArrayList<>());
                oldByTo.get(oldEvo.getTo()).add(oldEvo);
            }
            Map<Species, List<Evolution>> newByTo = new HashMap<>();
            for (Evolution newEvo : entry.getKey().getEvolutionsFrom()) {
                newByTo.putIfAbsent(newEvo.getTo(), new ArrayList<>());
                newByTo.get(newEvo.getTo()).add(newEvo);
            }

            for (Map.Entry<Species, List<Evolution>> toEntry: newByTo.entrySet()) {
                toNames.add(toEntry.getKey().getFullName());

                List<Evolution> oldEvos = oldByTo.getOrDefault(toEntry.getKey(), Collections.emptyList());
                if (oldEvos.isEmpty()) {
                    oldMethods.add("None");
                }
                List<Evolution> newEvos = toEntry.getValue();
                for (int i = 0; i < newEvos.size(); i++) {
                    newMethods.add(evolutionMethodToString(newEvos.get(i)));
                    if (i < oldEvos.size()) {
                        oldMethods.add(evolutionMethodToString(oldEvos.get(i)));
                    }
                }
                padToEqualSize(oldMethods, newMethods);
                padToEqualSize(toNames, newMethods);
            }
            padToEqualSize(fromNames, newMethods);
        }

        int fromLength = maxElementLength(fromNames);
        int toLength = maxElementLength(toNames);
        int oldMethodsLength = maxElementLength(oldMethods);
        int newMethodsLength = maxElementLength(newMethods);

        // printing the tables like this is swift though
        log.printf("%-" + fromLength + "s|%-" + toLength + "s|%-" + oldMethodsLength + "s|%-" + newMethodsLength + "s%n",
                getBS("Log.pe.from"), getBS("Log.pe.to"), getBS("Log.pe.oldMethod"), getBS("Log.pe.newMethod"));
        for (int i = 0; i < fromNames.size(); i++) {
            log.printf("%-" + fromLength + "s|%-" + toLength + "s|%-" + oldMethodsLength + "s|%-" + newMethodsLength + "s%n",
                    fromNames.get(i), toNames.get(i), oldMethods.get(i), newMethods.get(i));
        }
    }

    private void padToEqualSize(List<String> shorter, List<String> longer) {
        while (shorter.size() < longer.size()) {
            shorter.add("");
        }
    }

    private int maxElementLength(List<String> list) {
        return list.stream().map(String::length).max(Integer::compareTo).orElseThrow(IllegalStateException::new);
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

    private String evolutionMethodToString(Evolution evo) {
        StringBuilder sb = new StringBuilder();

        String evoTypeStr = getBS("Log.pe." + evo.getType());

        if (evo.getType().usesItem()) {
            String itemName = romHandler.getItemNames()[evo.getExtraInfo()];
            evoTypeStr = String.format(evoTypeStr, itemName);
        } else if (evo.getType().usesMove()) {
            String moveName = romHandler.getMoves().get(evo.getExtraInfo()).name;
            evoTypeStr = String.format(evoTypeStr, moveName);
        } else if (evo.getType().usesSpecies()) {
            String speciesName = romHandler.getSpecies().get(evo.getExtraInfo()).getName();
            evoTypeStr = String.format(evoTypeStr, speciesName);
        } else if (evo.getType().usesLocation()) {
            String locationName = "foobar todo";
            evoTypeStr = String.format(evoTypeStr, locationName);
        }

        sb.append(evoTypeStr);
        if (evo.getType().usesLevel()) {
            sb.append(String.format(getBS("Log.pe.usesLevel"), evo.getExtraInfo()));
        }

        return sb.toString();
    }


    // - scrap the EvolutionUpdate class
    // (- somehow remember old evolutions)
    // - write evolution methods here



    private boolean shouldLogSpeciesTraits() {
        return (speciesBSUpdater.isUpdated() || speciesBSRandomizer.isChangesMade()
                || speciesTypeRandomizer.isChangesMade() || speciesAbilityRandomizer.isChangesMade()
                || encHeldItemRandomizer.isChangesMade());
    }

    private void logSpeciesTraits() {
        // Log base stats, types, abilities, and wild held items
        List<Species> allSpecies = romHandler.getSpeciesInclFormes();
        String[] itemNames = romHandler.getItemNames();
        printSectionTitle("psta");

        // TODO: This puts the alt forms at the end. It would be nice to have them near their base forms.

        int numLen = Integer.toString(allSpecies.size()).length();
        int nameLen = getMaxSpeciesNameLength(allSpecies);
        int typeLen = 8 * 2 + 1; // TODO: remove magic number
        int abilityLen = getAbilityNameLength();

        // Table head
        log.printf("%" + numLen + "s", getBS("Log.psta.num"));
        log.printf("|%-" + nameLen + "s", getBS("Log.psta.name"));
        log.printf("|%-" + typeLen + "s", getBS("Log.psta.type"));
        if (romHandler.generationOfPokemon() == 1) {
            log.printf("|%4s|%4s|%4s|%4s|%4s",
                    getBS("Log.psta.hp"), getBS("Log.psta.attack"),
                    getBS("Log.psta.defense"), getBS("Log.psta.speed"),
                    getBS("Log.psta.special"));
        } else {
            log.printf("|%4s|%4s|%4s|%4s|%4s|%4s",
                    getBS("Log.psta.hp"), getBS("Log.psta.attack"),
                    getBS("Log.psta.defense"), getBS("Log.psta.spatk"),
                    getBS("Log.psta.spdef"), getBS("Log.psta.speed"));
        }
        for (int i = 0; i < romHandler.abilitiesPerSpecies(); i++) {
            log.printf("|%-" + abilityLen + "s", getBS("Log.psta.ability" + (i + 1)));
        }
        if (romHandler.generationOfPokemon() != 1) {// i.e. wild pokes have held items
            log.print("|" + getBS("Log.psta.item"));
        }
        log.println();

        // Rows for each species
        for (Species pk : allSpecies) {
            if (pk == null || pk.isActuallyCosmetic()) {
                continue;
            }

            log.printf("%" + numLen + "d", pk.getNumber());
            log.printf("|%-" + nameLen + "s", pk.getFullName());
            log.printf("|%-" + typeLen + "s",
                    pk.getPrimaryType(false)
                            + (pk.hasSecondaryType(false) ? "/" + pk.getSecondaryType(false) : ""));
            if (romHandler.generationOfPokemon() == 1) {
                log.printf("|%4d|%4d|%4d|%4d|%4d",
                        pk.getHp(), pk.getAttack(),
                        pk.getDefense(), pk.getSpeed(),
                        pk.getSpecial());
            } else {
                log.printf("|%4s|%4s|%4s|%4s|%4s|%4s",
                        pk.getHp(), pk.getAttack(),
                        pk.getDefense(), pk.getSpatk(),
                        pk.getSpdef(), pk.getSpeed());
            }
            if (romHandler.abilitiesPerSpecies() >= 1) {
                log.printf("|%-" + abilityLen + "s", romHandler.abilityName(pk.getAbility1()));
            }
            if (romHandler.abilitiesPerSpecies() >= 2) {
                log.printf("|%-" + abilityLen + "s",
                        pk.getAbility2() == pk.getAbility1() ? "--" : romHandler.abilityName(pk.getAbility2()));
            }
            if (romHandler.abilitiesPerSpecies() >= 3) {
                log.printf("|%-" + abilityLen + "s",
                        pk.getAbility3() == pk.getAbility1() ? "--" : romHandler.abilityName(pk.getAbility3()));
            }
            if (romHandler.generationOfPokemon() != 1) {// i.e. wild pokes have held items
                log.print("|");
                List<String> itemStrings = new ArrayList<>();
                if (pk.getGuaranteedHeldItem() > 0) {
                    itemStrings.add(itemNames[pk.getGuaranteedHeldItem()] + getBS("Log.psta.itemGuaranteed"));
                } else {
                    if (pk.getCommonHeldItem() > 0) {
                        itemStrings.add(itemNames[pk.getCommonHeldItem()] + getBS("Log.psta.itemCommon"));
                    }
                    if (pk.getRareHeldItem() > 0) {
                        itemStrings.add(itemNames[pk.getRareHeldItem()] + getBS("Log.psta.itemRare"));
                    }
                    if (pk.getDarkGrassHeldItem() > 0) {
                        itemStrings.add(itemNames[pk.getDarkGrassHeldItem()] + getBS("Log.psta.itemDarkGrass"));
                    }
                }
                log.print(String.join(", ", itemStrings));
            }
            log.println();
        }

        printSectionSeparator();
    }

    private int getMaxSpeciesNameLength(List<Species> allSpecies) {
        return allSpecies.stream()
                .filter(Objects::nonNull)
                .map(Species::getFullName).map(String::length)
                .max(Integer::compareTo)
                .orElseThrow(RuntimeException::new); // should not happen
    }

    private int getAbilityNameLength() {
        int max = 0;
        for (int i = 0; i < romHandler.highestAbilityIndex(); i++) {
            max = Math.max(max, romHandler.abilityName(i).length());
        }
        return max;
    }

    private boolean shouldLogStarters() {
        return starterRandomizer.isChangesMade();
    }

    private void logStarters() {
        // TODO: log starter held items
        printSectionTitle("sp");
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
        printSectionSeparator();
    }

    private boolean shouldLogMoveData() {
        return (moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated());
    }

    private void logMoveData() {

        log.printf(getBS("Log.sectionTitle"), "Move Data", "MVDT");
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
        printSectionSeparator();
    }

    private boolean shouldLogMovesets() {
        return speciesMovesetRandomizer.isChangesMade() || settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY;
    }

    private void logMovesets() {
        log.printf(getBS("Log.sectionTitle"), "Pokémon Movesets", "PKMV");
        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("Metronome only mode - every Pokémon learns only Metronome.");
            printSectionSeparator();
            return;
        }

        Map<Integer, List<MoveLearnt>> moveData = romHandler.getMovesLearnt();
        Map<Integer, List<Integer>> eggMoves = romHandler.getEggMoves();
        List<Move> moves = romHandler.getMoves();
        for (Species pk : romHandler.getSpeciesInclFormes()) {
            if (pk == null || pk.isActuallyCosmetic()) {
                continue;
            }

            log.printf(String.format("%03d %s -> ", pk.getNumber(), pk.getFullName()));

            SpeciesSet evos = pk.getEvolvedSpecies(false);
            if (evos.isEmpty()) {
                log.println(" (no evolution)");
            } else {
                log.println(evos.stream().sorted().map(Species::getFullName).collect(Collectors.joining(", ")));
            }

            if (romHandler instanceof Gen1RomHandler) {
                log.printf("  HP| ATK| DEF|SPEC| SPD %n%4d|%4d|%4d|%4d|%4d %n",
                        pk.getHp(), pk.getAttack(), pk.getDefense(), pk.getSpecial(), pk.getSpeed());
            } else {
                log.printf("  HP| ATK| DEF|SATK|SDEF| SPD %n%4d|%4d|%4d|%4d|%4d|%4d %n",
                        pk.getHp(), pk.getAttack(), pk.getDefense(), pk.getSpatk(), pk.getSpdef(), pk.getSpeed());
            }

            List<MoveLearnt> data = moveData.get(pk.getNumber());
            for (MoveLearnt ml : data) {
                try {
                    if (ml.level == 0) {
                        log.print("Learnt upon evolution: ");
                    } else {
                        log.printf("Level %-2d: ", ml.level);
                    }
                    log.println(formatMovesetMove(moves.get(ml.move), pk));
                } catch (NullPointerException ex) {
                    log.printf("invalid move at level %-2d %n", ml.level);
                }
            }
            List<Integer> eggMove = eggMoves.get(pk.getNumber());
            if (eggMove != null && !eggMove.isEmpty()) {
                log.println("Egg Moves:");
                for (Integer move : eggMove) {
                    log.println(" - " + formatMovesetMove(moves.get(move), pk));
                }
            }
            log.println();
        }
        printSectionSeparator();
    }

    private String formatMovesetMove(Move mv, Species learner) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s| %-8s | %-8s | POW=%3s | PP=%2d | ACC=%3.0f%%",
                mv.name, mv.type, mv.category,
                mv.category == MoveCategory.STATUS ? "--" : String.format("%3d", mv.power),
                mv.pp, mv.hitratio));
        if (mv.category != MoveCategory.STATUS && learner.hasType(mv.type, false)) {
            sb.append(" (STAB)");
        }
        return sb.toString();
    }

    private boolean shouldLogTMMoves() {
        return tmtMoveRandomizer.isTMChangesMade() || settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY;
    }

    private void logTMMoves() {
        log.printf(getBS("Log.sectionTitle"), "TM Moves", "TMMV");

        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("Metronome only mode - every TM contains Metronome.");
        } else {
            List<Integer> tmMoves = romHandler.getTMMoves();
            List<Move> moves = romHandler.getMoves();
            for (int i = 0; i < tmMoves.size(); i++) {
                log.printf("TM%02d %s" + NEWLINE, i + 1, moves.get(tmMoves.get(i)).name);
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogTMHMCompatibility() {
        return tmhmtCompRandomizer.isTMHMChangesMade();
    }

    private void logTMHMCompatibility() {
        log.printf(getBS("Log.sectionTitle"), "TM Compatibility", "TMCM");
        Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
        List<Integer> tmHMs = new ArrayList<>(romHandler.getTMMoves());
        tmHMs.addAll(romHandler.getHMMoves());
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(compat, tmHMs, moveData, true);
        printSectionSeparator();
    }

    private boolean shouldLogMoveTutorMoves() {
        return romHandler.hasMoveTutors() && (tmtMoveRandomizer.isTutorChangesMade() ||
                settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY);
    }

    private void logMoveTutorMoves(List<Integer> oldMtMoves) {
        log.printf(getBS("Log.sectionTitle"), "Move Tutor Moves", "MTMV");

        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.println("Metronome only mode - every Move Tutor teaches Metronome.");
        } else {
            List<Integer> newMtMoves = romHandler.getMoveTutorMoves();
            List<Move> moves = romHandler.getMoves();
            for (int i = 0; i < newMtMoves.size(); i++) {
                log.printf("%-10s -> %-10s" + NEWLINE, moves.get(oldMtMoves.get(i)).name,
                        moves.get(newMtMoves.get(i)).name);
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogMoveTutorCompatibility() {
        return romHandler.hasMoveTutors() && tmhmtCompRandomizer.isTutorChangesMade();
    }

    private void logMoveTutorCompatibility() {
        log.printf(getBS("Log.sectionTitle"), "Move Tutor Compatibility", "MTCB");
        Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
        List<Integer> tutorMoves = romHandler.getMoveTutorMoves();
        List<Move> moveData = romHandler.getMoves();

        logCompatibility(compat, tutorMoves, moveData, false);
        printSectionSeparator();
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
        log.printf(getBS("Log.sectionTitle"), "Trainer Pokémon", "TRPK");
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
        printSectionSeparator();
    }

    private boolean shouldLogStaticPokemon() {
        return romHandler.canChangeStaticPokemon() && staticPokeRandomizer.isStaticChangesMade();
    }

    private void logStaticPokemon(List<StaticEncounter> oldStatics) {
        List<StaticEncounter> newStatics = romHandler.getStaticPokemon();

        log.printf(getBS("Log.sectionTitle"), "Static Pokémon", "STPK");
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
        printSectionSeparator();
    }

    private boolean shouldLogTotemPokemon() {
        return romHandler.hasTotemPokemon() && staticPokeRandomizer.isTotemChangesMade();
    }

    private void logTotemPokemon(List<TotemPokemon> oldTotems) {
        List<TotemPokemon> newTotems = romHandler.getTotemPokemon();

        String[] itemNames = romHandler.getItemNames();
        log.printf(getBS("Log.sectionTitle"), "Totem Pokémon", "TOPK");
        for (int i = 0; i < oldTotems.size(); i++) {
            TotemPokemon oldP = oldTotems.get(i);
            TotemPokemon newP = newTotems.get(i);
            log.println(oldP.spec.getFullName() + " =>");
            log.printf(newP.toString(), itemNames[newP.heldItem]);
        }
        printSectionSeparator();
    }

    private boolean shouldLogWildPokemon() {
        return wildEncounterRandomizer.isChangesMade();
    }

    private void logWildPokemon() {
        log.printf(getBS("Log.sectionTitle"), "Wild Pokémon", "WDPK");
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
        printSectionSeparator();
    }

    private boolean shouldLogInGameTrades() {
        return tradeRandomizer.isChangesMade();
    }

    private void logInGameTrades(List<IngameTrade> oldTrades) {
        log.printf(getBS("Log.sectionTitle"), "In-Game Trades", "IGTR");
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
        printSectionSeparator();
    }

    private boolean shouldLogShopItems() {
        return itemRandomizer.isShopChangesMade();
    }

    private void logShopItems() {
        String[] itemNames = romHandler.getItemNames();
        log.printf(getBS("Log.sectionTitle"), "Shop Items", "SHMS");
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
        printSectionSeparator();
    }

    private boolean shouldLogPickupItems() {
        return itemRandomizer.isPickupChangesMade();
    }

    private void logPickupItems() {
        List<PickupItem> pickupItems = romHandler.getPickupItems();
        String[] itemNames = romHandler.getItemNames();
        log.printf(getBS("Log.sectionTitle"), "Pickup Items", "PUMS");
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
        printSectionSeparator();
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

        String[] effNames = getBS("Log.te.effectivenessNames").split(",");

        Map<Type, Map<Type, Update<Effectiveness>>> updates = typeEffUpdater.getUpdates();
        for (Map.Entry<Type, Map<Type, Update<Effectiveness>>> outer : updates.entrySet()) {
            log.println(outer.getKey() + " when used against...");
            for (Map.Entry<Type, Update<Effectiveness>> inner : outer.getValue().entrySet()) {
                log.printf("\t%-8s:   %-18s -> %-18s%n",
                        inner.getKey(),
                        effNames[inner.getValue().getBefore().ordinal()],
                        effNames[inner.getValue().getAfter().ordinal()]);
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


