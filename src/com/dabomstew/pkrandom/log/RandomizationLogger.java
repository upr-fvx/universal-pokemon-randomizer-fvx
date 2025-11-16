package com.dabomstew.pkrandom.log;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.SysConstants;
import com.dabomstew.pkrandom.Version;
import com.dabomstew.pkrandom.random.RandomSource;
import com.dabomstew.pkrandom.randomizers.*;
import com.dabomstew.pkrandom.updaters.*;
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.romhandlers.Gen1RomHandler;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.io.PrintStream;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class RandomizationLogger {

    private static final int TM_COMP_ROW_WIDTH = 5;
    private static final int TYPE_NAME_LEN = 8;

    private final RandomSource randomSource;
    private final Settings settings;
    private final RomHandler romHandler;
    private final ResourceBundle bundle;

    private final List<Integer> originalMTMoves;
    private final List<String> originalTrainerNames;
    private final List<StaticEncounter> originalStatics;
    private final List<TotemPokemon> originalTotems;
    private final List<InGameTrade> originalTrades;

    // this huge list of shared attributes with GameRandomizer could maybe be shared in a better way?
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
        this.originalTrades = romHandler.getInGameTrades();

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
        log.printf(getBS("Log.settings"), Version.VERSION + settings.toString());
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
        log.println();
        if (shouldLogBaseStatUpdates())
            printContentsRow("bsu");
        if (shouldLogSpeciesTraits())
            printContentsRow("psta");
        if (shouldLogEvolutions())
            printContentsRow("pe");
        if (shouldLogEvolutionImprovements())
            printContentsRow("pei");
        if (shouldLogBaseStatUpdates() || shouldLogSpeciesTraits() || shouldLogEvolutions()
                || shouldLogEvolutionImprovements())
            log.println();
        if (shouldLogStarters())
            printContentsRow("sp");
        if (shouldLogStaticPokemon())
            printContentsRow("stp");
        if (shouldLogInGameTrades())
            printContentsRow("igt");
        if (shouldLogStarters() || shouldLogStaticPokemon() || shouldLogInGameTrades())
            log.println();
        if (shouldLogMoveUpdates())
            printContentsRow("mu");
        if (shouldLogMoveData())
            printContentsRow("md");
        if (shouldLogMovesets())
            printContentsRow("pms");
        if (shouldLogMoveUpdates() || shouldLogMoveData() || shouldLogMovesets())
            log.println();
        if (shouldLogTrainers())
            printContentsRow("tp");
        if (shouldLogTotemPokemon())
            printContentsRow("totp");
        if (shouldLogTrainers() || shouldLogTotemPokemon())
            log.println();
        if (shouldLogWildPokemon()) {
            printContentsRow("wp");
            log.println();
        }
        if (shouldLogTMMoves())
            printContentsRow("tm");
        if (shouldLogTMHMCompatibility())
            printContentsRow("tmc");
        if (shouldLogMoveTutorMoves())
            printContentsRow("mt");
        if (shouldLogMoveTutorCompatibility())
            printContentsRow("mtc");
        if (shouldLogTMMoves() || shouldLogTMHMCompatibility()
                || shouldLogMoveTutorMoves() || shouldLogMoveTutorCompatibility())
            log.println();
        if (shouldLogShopItems())
            printContentsRow("sh");
        if (shouldLogPickupItems())
            printContentsRow("pu");
        if (shouldLogShopItems() || shouldLogPickupItems())
            log.println();
        if (shouldLogTypeEffectivenessUpdates())
            printContentsRow("teu");
        if (shouldLogTypeEffectiveness())
            printContentsRow("te");
        if (shouldLogTypeEffectivenessUpdates() || shouldLogTypeEffectiveness())
            log.println();
    }

    private void logOverview() {
        printSectionTitle("overview");
        // The overview lines intentionally (mostly) map to panels in the UI, thus they share their bundle strings.
        logOverviewLine(getBS("GUI.pbsPanel.title"),
                speciesBSRandomizer.isChangesMade() || speciesBSUpdater.isUpdated(), true);
        logOverviewLine(getBS("GUI.ptPanel.title"), speciesTypeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.paPanel.title"), speciesAbilityRandomizer.isChangesMade(),
                romHandler.abilitiesPerSpecies() != 0);
        logOverviewLine(getBS("GUI.pePanel.title"), evoRandomizer.isChangesMade()
                || shouldLogEvolutionImprovements(), true);
        logOverviewLine(getBS("GUI.spPanel.title"), starterRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.stpPanel.title"), staticPokeRandomizer.isStaticChangesMade(),
                romHandler.canChangeStaticPokemon());
        logOverviewLine(getBS("GUI.igtPanel.title"), tradeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.mdPanel.title"),
                moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated(), true);
        logOverviewLine(getBS("GUI.pmsPanel.title"), speciesMovesetRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("GUI.tpPanel.title"), trainerPokeRandomizer.isChangesMade(), true);
        logOverviewLine(getBS("Log.overview.trainerMovesets"), trainerMovesetRandomizer.isChangesMade(),
                TrainerMovesetRandomizer.hasSupport(romHandler.generationOfPokemon()));
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
        logOverviewLine(getBS("GUI.shPanel.title"), itemRandomizer.isShopChangesMade(),
                romHandler.hasShopSupport());
        logOverviewLine(getBS("GUI.puPanel.title"), itemRandomizer.isPickupChangesMade(),
                romHandler.abilitiesPerSpecies() != 0);
        logOverviewLine(getBS("GUI.tePanel.title"),
                typeEffRandomizer.isChangesMade() || typeEffUpdater.isUpdated(),
                romHandler.hasTypeEffectivenessSupport());
        logOverviewLine(getBS("GUI.ppalPanel.title"), paletteRandomizer != null && paletteRandomizer.isChangesMade(),
                romHandler.hasPokemonPaletteSupport());
        log.println();

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
        log.println();

        if (introPokeRandomizer.isChangesMade()) {
            log.printf(getBS("Log.overview.introPokemon"), introPokeRandomizer.getIntroSpecies().getFullName());
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
        if (shouldLogBaseStatUpdates())
            logBaseStatsUpdates();
        if (shouldLogSpeciesTraits())
            logSpeciesTraits();
        if (shouldLogEvolutions())
            logEvolutions();
        if (shouldLogEvolutionImprovements())
            logEvolutionImprovements();

        if (shouldLogStarters())
            logStarters();
        if (shouldLogStaticPokemon())
            logStaticPokemon(originalStatics);
        if (shouldLogInGameTrades())
            logInGameTrades(originalTrades);

        if (shouldLogMoveUpdates())
            logMoveUpdates();
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

        if (shouldLogTypeEffectivenessUpdates())
            logTypeEffectivenessUpdates();
        if (shouldLogTypeEffectiveness())
            logTypeEffectiveness();
    }

    private boolean shouldLogTypeEffectiveness() {
        return typeEffUpdater.isUpdated() || typeEffRandomizer.isChangesMade();
    }

    private void logTypeEffectiveness() {
        printSectionTitle("te");

        String[] effSymbols = getBS("Log.te.effectivenessSymbols").split(",");
        String[] effNames = getBS("Log.te.effectivenessNames").split(",");

        log.print(romHandler.getTypeTable().toBigString(effSymbols));
        log.printf(getBS("Log.te.orientation"));
        log.printf(getBS("Log.te.legend"), effSymbols[2], effNames[2]);
        log.printf(getBS("Log.te.legend"), effSymbols[3], effNames[3]);
        log.printf(getBS("Log.te.legend"), effSymbols[1], effNames[1]);
        log.printf(getBS("Log.te.legend"), effSymbols[0], effNames[0]);

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

    private boolean shouldLogEvolutionImprovements() {
        return !romHandler.getPreImprovedEvolutions().isEmpty();
    }

    private void logEvolutionImprovements() {
        printSectionTitle("pei");

        log.printf(getBS("Log.pei.listHead"));
        if (settings.isChangeImpossibleEvolutions()) {
            log.printf(getBS("Log.pei.listImpossible"));
        }
        if (settings.isMakeEvolutionsEasier()) {
            log.printf(getBS("Log.pei.listEasier"));
        }
        if (settings.isRemoveTimeBasedEvolutions()) {
            log.printf(getBS("Log.pei.listTimeBased"));
        }
        log.println();
        if (settings.isMakeEvolutionsEasier() && romHandler.generationOfPokemon() != 1) {
            log.printf(getBS("Log.pei.happiness"));
        }
        log.println();

        TextTable table = new TextTable(4);
        table.addRow(Arrays.asList(
                getBS("Log.pe.from"), getBS("Log.pe.to"),
                getBS("Log.pei.oldMethod"), getBS("Log.pei.newMethod")
        ));

        // rather hefty code, for filling these table rows
        // Assumes improvements only adds Evolutions and/or Species (forms) to evolve into;
        // i.e. oldFoo.size() < newFoo.size().
        for (Map.Entry<Species, List<Evolution>> entry : romHandler.getPreImprovedEvolutions().entrySet()) {
            table.addCell(0, entry.getKey().getFullName());

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

            for (Map.Entry<Species, List<Evolution>> toEntry : newByTo.entrySet()) {
                table.addCell(1, toEntry.getKey().getFullName());

                List<Evolution> oldEvos = oldByTo.getOrDefault(toEntry.getKey(), Collections.emptyList());
                if (oldEvos.isEmpty()) {
                    table.addCell(2, "None");
                }
                List<Evolution> newEvos = toEntry.getValue();
                for (int i = 0; i < newEvos.size(); i++) {
                    table.addCell(3, evolutionMethodToString(newEvos.get(i)));
                    if (i < oldEvos.size()) {
                        table.addCell(2, evolutionMethodToString(oldEvos.get(i)));
                    }
                }
                table.evenOut();
            }
            table.evenOut();
        }

        log.print(table);

        printSectionSeparator();
    }

    private String evolutionMethodToString(Evolution evo) {
        StringBuilder sb = new StringBuilder();

        String evoTypeStr = getBS("Log.pe." + evo.getType());

        if (evo.getType().usesItem()) {
            String itemName = romHandler.getItems().get(evo.getExtraInfo()).getName();
            evoTypeStr = String.format(evoTypeStr, itemName);
        } else if (evo.getType().usesMove()) {
            String moveName = romHandler.getMoves().get(evo.getExtraInfo()).name;
            evoTypeStr = String.format(evoTypeStr, moveName);
        } else if (evo.getType().usesSpecies()) {
            String speciesName = romHandler.getSpecies().get(evo.getExtraInfo()).getFullName();
            evoTypeStr = String.format(evoTypeStr, speciesName);
        } else if (evo.getType().usesLocation()) {
            List<String> locationNames = romHandler.getLocationNamesForEvolution(evo.getType());
            if (locationNames.isEmpty()) {
                locationNames = Collections.singletonList(getBS("Log.pe.nowhere"));
            }
            evoTypeStr = String.format(evoTypeStr, String.join(getBS("Log.pe.separator"), locationNames));
        }

        sb.append(evoTypeStr);
        if (evo.getType().usesLevel()) {
            sb.append(String.format(getBS("Log.pe.usesLevel"), evo.getExtraInfo()));
        }

        return sb.toString();
    }

    private boolean shouldLogSpeciesTraits() {
        return (speciesBSUpdater.isUpdated() || speciesBSRandomizer.isChangesMade()
                || speciesTypeRandomizer.isChangesMade() || speciesAbilityRandomizer.isChangesMade()
                || encHeldItemRandomizer.isChangesMade());
    }

    private void logSpeciesTraits() {
        // Log base stats, types, abilities, and wild held items
        printSectionTitle("psta");

        List<Species> allSpecies = romHandler.getSpeciesInclFormes();

        // TODO: This puts the alt forms at the end. It would be nice to have them near their base forms.

        int numLen = Integer.toString(allSpecies.size()).length();
        int nameLen = getMaxSpeciesNameLength(allSpecies);
        int typeLen = TYPE_NAME_LEN * 2 + 1; // two types and a '/' between
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

            log.printf("%" + numLen + "d", pk.getBaseNumber());
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
                if (pk.getGuaranteedHeldItem() != null) {
                    itemStrings.add(pk.getGuaranteedHeldItem().getName() + getBS("Log.psta.itemGuaranteed"));
                } else {
                    if (pk.getCommonHeldItem() != null) {
                        itemStrings.add(pk.getCommonHeldItem().getName() + getBS("Log.psta.itemCommon"));
                    }
                    if (pk.getRareHeldItem() != null) {
                        itemStrings.add(pk.getRareHeldItem().getName() + getBS("Log.psta.itemRare"));
                    }
                    if (pk.getDarkGrassHeldItem() != null) {
                        itemStrings.add(pk.getDarkGrassHeldItem().getName() + getBS("Log.psta.itemDarkGrass"));
                    }
                }
                log.print(String.join(", ", itemStrings));
            }
            log.println();
        }

        printSectionSeparator();
    }

    private int getMaxSpeciesNameLength(Collection<Species> allSpecies) {
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
        printSectionTitle("sp");
        String mode = null;
        switch (settings.getStartersMod()) {
            case UNCHANGED:
                mode = getBS("Log.sp.unchanged"); // should never happen
                break;
            case CUSTOM:
                mode = getBS("Log.sp.custom");
                break;
            case COMPLETELY_RANDOM:
                mode = getBS("Log.sp.random");
                break;
            case RANDOM_BASIC:
                mode = getBS("Log.sp.randomBasic");
                break;
            case RANDOM_WITH_TWO_EVOLUTIONS:
                mode = getBS("Log.sp.random2Evolution");
        }
        log.printf(getBS("Log.sp.mode"), mode);

        List<Species> starters = romHandler.getStarters();
        List<Item> heldItems = romHandler.getStarterHeldItems();

        for (int i = 0; i < starters.size(); i++) {
            if (heldItems.size() == 1 && heldItems.get(0) != null) {
                log.printf(getBS("Log.sp.setWithItem"), i + 1, starters.get(i).getFullName(),
                        heldItems.get(0).getName());
            } else if (heldItems.size() == starters.size() && heldItems.get(i) != null) {
                log.printf(getBS("Log.sp.setWithItem"), i + 1, starters.get(i).getFullName(),
                        heldItems.get(i).getName());
            } else {
                log.printf(getBS("Log.sp.setNoItem"), i + 1, starters.get(i).getFullName());
            }
            if (!heldItems.isEmpty() && heldItems.size() != 1 && heldItems.size() != starters.size()) {
                log.println("Something went weird with the held items. Please report this as a GitHub issue.");
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogMoveData() {
        return (moveDataRandomizer.isChangesMade() || moveUpdater.isUpdated());
    }

    private void logMoveData() {
        printSectionTitle("md");

        int columns = 6;
        List<String> head = new ArrayList<>(Arrays.asList(
                getBS("Log.md.num"), getBS("Log.md.name"), getBS("Log.md.type"),
                getBS("Log.md.power"), getBS("Log.md.accuracy"), getBS("Log.md.pp")
        ));
        if (romHandler.hasPhysicalSpecialSplit()) {
            columns++;
            head.add(getBS("Log.md.category"));
        }
        TextTable table = new TextTable(columns);
        table.addRow(head);

        table.setColumnAlignments(TextTable.Alignment.RIGHT, 3, 4, 5); // power, acc., pp

        for (Move mv : romHandler.getMoves()) {
            if (mv == null) {
                continue;
            }
            List<String> row = new ArrayList<>(Arrays.asList(
                    mv.internalId + "", mv.name, (mv.type == null) ? "???" : mv.type.toString(),
                    mv.power + "", (int) mv.hitratio + "%", mv.pp + ""
            ));
            if (romHandler.hasPhysicalSpecialSplit()) {
                row.add(mv.category.toString());
            }
            table.addRow(row);
        }

        log.print(table);

        printSectionSeparator();
    }

    private boolean shouldLogMovesets() {
        return speciesMovesetRandomizer.isChangesMade() || settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY;
    }

    private void logMovesets() {
        printSectionTitle("pms");
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

            log.printf(String.format("%03d %s -> ", pk.getBaseNumber(), pk.getFullName()));

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
        printSectionTitle("tm");

        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.printf(getBS("Log.tm.metronomeMode"));
        } else {
            List<Integer> tmMoves = romHandler.getTMMoves();
            List<Move> moves = romHandler.getMoves();
            for (int i = 0; i < tmMoves.size(); i++) {
                log.printf("TM%02d %s%n", i + 1, moves.get(tmMoves.get(i)).name);
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogTMHMCompatibility() {
        return tmhmtCompRandomizer.isTMHMChangesMade();
    }

    private void logTMHMCompatibility() {
        printSectionTitle("tmc");
        if (settings.isFullHMCompat()) {
            log.printf(getBS("Log.tmc.fullHM"));
        }
        if (settings.getTmsHmsCompatibilityMod() == Settings.TMsHMsCompatibilityMod.FULL) {
            log.printf(getBS("Log.tmc.full"));
        } else if (settings.getTmsHmsCompatibilityMod() != Settings.TMsHMsCompatibilityMod.UNCHANGED) {
            Map<Species, boolean[]> compat = romHandler.getTMHMCompatibility();
            List<Move> tmHMs = getTMHMs();

            logCompatibility(compat, tmHMs, getBS("Log.tmc.byTMHM"), this::logCompTMHM);
        }

        printSectionSeparator();
    }

    private List<Move> getTMHMs() {
        List<Move> moveData = romHandler.getMoves();
        List<Move> tmHMs = romHandler.getTMMoves()
                .stream().map(moveData::get)
                .collect(Collectors.toList());
        if (!settings.isFullHMCompat()) {
            romHandler.getHMMoves()
                    .stream().map(moveData::get)
                    .forEach(tmHMs::add);
        }
        return tmHMs;
    }

    private void logCompatibility(Map<Species, boolean[]> compat, List<Move> moves,
                                  String byMoveString, BiConsumer<Integer, List<Move>> logCompMoveFun) {
        log.printf(getBS("Log.tmc.bySpecies"));
        for (Map.Entry<Species, boolean[]> entry : compat.entrySet()) {

            logCompSpecies(entry.getKey());

            int j = 0;
            for (int i = 0; i < moves.size(); i++) {
                if (entry.getValue()[i + 1]) {
                    if (j != 0) {
                        log.print(", ");
                    }
                    if (j % TM_COMP_ROW_WIDTH == 0) {
                        log.printf("%n\t");
                    }
                    logCompMoveFun.accept(i, moves);
                    j++;
                }
            }
            log.println();
        }

        log.println();
        log.printf(byMoveString);
        for (int i = 0; i < moves.size(); i++) {

            logCompMoveFun.accept(i, moves);

            int j = 0;
            for (Map.Entry<Species, boolean[]> entry : compat.entrySet()) {
                if (entry.getValue()[i + 1]) {
                    if (j != 0) {
                        log.print(", ");
                    }
                    if (j % TM_COMP_ROW_WIDTH == 0) {
                        log.printf("%n\t");
                    }
                    logCompSpecies(entry.getKey());
                    j++;
                }
            }
            log.println();
        }
    }

    private void logCompSpecies(Species pk) {
        log.printf("#%03d %s", pk.getBaseNumber(), pk.getFullName());
    }

    private void logCompTMHM(int i, List<Move> tmHMs) {
        int tmCount = romHandler.getTMCount();
        if (i < tmCount) {
            log.printf(getBS("Log.tmc.tm"), i + 1, tmHMs.get(i).name);
        } else {
            log.printf(getBS("Log.tmc.hm"), i + 1 - tmCount, tmHMs.get(i).name);
        }
    }

    private void logCompTutorMove(int i, List<Move> tutorMoves) {
        log.print(tutorMoves.get(i).name);
    }

    private boolean shouldLogMoveTutorMoves() {
        return romHandler.hasMoveTutors() && (tmtMoveRandomizer.isTutorChangesMade() ||
                settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY);
    }

    private void logMoveTutorMoves(List<Integer> oldMtMoves) {
        printSectionTitle("mt");

        if (settings.getMovesetsMod() == Settings.MovesetsMod.METRONOME_ONLY) {
            log.printf(getBS("Log.mt.metronomeMode"));
        } else {
            List<Integer> newMtMoves = romHandler.getMoveTutorMoves();
            List<Move> moves = romHandler.getMoves();
            for (int i = 0; i < newMtMoves.size(); i++) {
                log.printf("%-10s -> %-10s%n", moves.get(oldMtMoves.get(i)).name,
                        moves.get(newMtMoves.get(i)).name);
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogMoveTutorCompatibility() {
        return romHandler.hasMoveTutors() && tmhmtCompRandomizer.isTutorChangesMade();
    }

    private void logMoveTutorCompatibility() {
        printSectionTitle("mtc");
        if (settings.getMoveTutorsCompatibilityMod() == Settings.MoveTutorsCompatibilityMod.FULL) {
            log.printf(getBS("Log.mtc.full"));
        } else {
            Map<Species, boolean[]> compat = romHandler.getMoveTutorCompatibility();
            List<Move> moveData = romHandler.getMoves();
            List<Move> tutorMoves = romHandler.getMoveTutorMoves()
                    .stream().map(moveData::get)
                    .collect(Collectors.toList());

            logCompatibility(compat, tutorMoves, getBS("Log.mtc.byTutorMove"), this::logCompTutorMove);
        }
        printSectionSeparator();
    }

    private boolean shouldLogTrainers() {
        return trainerPokeRandomizer.isChangesMade() || trainerMovesetRandomizer.isChangesMade()
                || trainerNameRandomizer.isChangesMade();
    }

    private void logTrainers(List<String> originalTrainerNames) {
        printSectionTitle("tp");
        List<Trainer> trainers = romHandler.getTrainers();
        String[] battleStyleNames = getBS("Log.tp.battleStyleNames").split(",");
        for (Trainer t : trainers) {
            log.print("#" + t.getIndex() + " ");
            String originalTrainerName = originalTrainerNames.get(t.getIndex());
            String currentTrainerName = "";
            if (t.getFullDisplayName() != null) {
                currentTrainerName = t.getFullDisplayName();
            } else if (t.getName() != null) {
                currentTrainerName = t.getName();
            }
            if (!currentTrainerName.isEmpty()) {
                if (trainerNameRandomizer.isChangesMade()) {
                    log.printf("(%s => %s)", originalTrainerName, currentTrainerName);
                } else {
                    log.printf("(%s)", currentTrainerName);
                }
            }
            if (t.getOffset() != 0) {
                log.printf("@%X", t.getOffset());
            }

            if (trainerMovesetRandomizer.isChangesMade()) {
                log.println();
                for (TrainerPokemon tpk : t.getPokemon()) {
                    List<Move> moves = romHandler.getMoves();
                    log.print(tpk.toString());
                    log.print(", " + getBS("Log.tp.ability") + ": "
                            + romHandler.abilityName(romHandler.getAbilityForTrainerPokemon(tpk)));
                    log.print(" - ");
                    boolean first = true;
                    for (int move : tpk.getMoves()) {
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
                for (TrainerPokemon tpk : t.getPokemon()) {
                    if (!first) {
                        log.print(", ");
                    }
                    log.print(tpk.toString());
                    first = false;
                }
            }
            if (settings.getBattleStyle().isBattleStyleChanged()) {
                log.printf(" (Battle Style: %s)", battleStyleNames[t.getCurrBattleStyle().getStyle().ordinal()]);
            }
            log.println();
        }
        printSectionSeparator();
    }

    private boolean shouldLogStaticPokemon() {
        return romHandler.canChangeStaticPokemon() && staticPokeRandomizer.isStaticChangesMade();
    }

    private void logStaticPokemon(List<StaticEncounter> oldStatics) {
        printSectionTitle("stp");

        List<StaticEncounter> newStatics = romHandler.getStaticPokemon();
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
        printSectionTitle("totp");
        List<TotemPokemon> newTotems = romHandler.getTotemPokemon();

        for (int i = 0; i < oldTotems.size(); i++) {
            TotemPokemon oldP = oldTotems.get(i);
            TotemPokemon newP = newTotems.get(i);
            log.println(oldP.getSpecies().getFullName() + " =>");
            log.print(newP.toString());
        }
        printSectionSeparator();
    }

    private boolean shouldLogWildPokemon() {
        return wildEncounterRandomizer.isChangesMade();
    }

    private void logWildPokemon() {
        printSectionTitle("wp");

        boolean useTimeBasedEncounters = settings.isUseTimeBasedEncounters() ||
                (!settings.isRandomizeWildPokemon() && settings.isWildLevelsModified());
        List<EncounterArea> encounterAreas = romHandler.getSortedEncounters(useTimeBasedEncounters);

        int i = 0;
        for (EncounterArea area : encounterAreas) {
            if (area.getEncounterType() == EncounterType.UNUSED) {
                continue;
            }

            i++;

            if (area.getDisplayName() == null) {
                log.printf(getBS("Log.wp.areaNoDisplayName"), i, area.getRate());
            } else {
                log.printf(getBS("Log.wp.areaWithDisplayName"), i, area.getDisplayName(), area.getRate());
            }
            for (Encounter e : area) {
                if (e.isSOS()) {
                    log.printf(getBS("Log.wp.sos"), getSOSString(e));
                }
                if (e.getMaxLevel() > 0 && e.getMaxLevel() != e.getLevel()) {
                    log.printf(getBS("Log.wp.encMultiLevel"), e.getSpecies().getFullName(),
                            e.getLevel(), e.getMaxLevel());
                } else {
                    log.printf(getBS("Log.wp.encSingleLevel"), e.getSpecies().getFullName(), e.getLevel());
                }
            }
            log.println();
        }
        printSectionSeparator();
    }

    private String getSOSString(Encounter e) {
        switch (e.getSosType()) {
            case RAIN:
                return getBS("Log.wp.rainSOS");
            case HAIL:
                return getBS("Log.wp.hailSOS");
            case SAND:
                return getBS("Log.wp.sandSOS");
            case GENERIC:
                return getBS("Log.wp.genericSOS");
            default:
                return "THIS SHOULD NOT BE SEEN, PLEASE REPORT BUG";
        }
    }

    private boolean shouldLogInGameTrades() {
        return tradeRandomizer.isChangesMade();
    }

    private void logInGameTrades(List<InGameTrade> oldTrades) {
        printSectionTitle("igt");
        List<InGameTrade> newTrades = romHandler.getInGameTrades();

        TextTable table = new TextTable(6);
        table.addRow(
                getBS("Log.igt.oldRequested"), getBS("Log.igt.oldGiven"), getBS("Log.igt.oldNickname"),
                getBS("Log.igt.newRequested"), getBS("Log.igt.newGiven"), getBS("Log.igt.newNickname")
        );
        for (int i = 0; i < oldTrades.size(); i++) {
            InGameTrade oldT = oldTrades.get(i);
            InGameTrade newT = newTrades.get(i);
            table.addRow(
                    oldT.getRequestedSpecies() == null ? getBS("Log.igt.any") : oldT.getRequestedSpecies().getFullName(),
                    oldT.getGivenSpecies().getFullName(), oldT.getNickname(),
                    newT.getRequestedSpecies() == null ? getBS("Log.igt.any") : newT.getRequestedSpecies().getFullName(),
                    newT.getGivenSpecies().getFullName(), newT.getNickname()
            );
        }
        log.print(table);

        printSectionSeparator();
    }

    private boolean shouldLogShopItems() {
        return itemRandomizer.isShopChangesMade();
    }

    private void logShopItems() {
        printSectionTitle("sh");
        if (settings.isAddCheapRareCandiesToShops()) {
            log.printf(getBS("Log.sh.addedRareCandies"));
        }
        if (settings.getShopItemsMod() != Settings.ShopItemsMod.UNCHANGED) {
            log.printf(getBS("Log.sh.specialShops"));
            List<Shop> shops = romHandler.getShops();
            for (Shop shop : shops) {
                if (!shop.isSpecialShop()) {
                    continue;
                }
                log.printf("%s", shop.getName());
                log.println();
                List<Item> shopItems = shop.getItems();
                for (Item shopItem : shopItems) {
                    log.printf("- %5s", shopItem.getName());
                    log.println();
                }

                log.println();
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogPickupItems() {
        return itemRandomizer.isPickupChangesMade();
    }

    private void logPickupItems() {
        printSectionTitle("pu");
        List<PickupItem> pickupItems = romHandler.getPickupItems();
        for (int levelRange = 0; levelRange < 10; levelRange++) {
            int startingLevel = (levelRange * 10) + 1;
            int endingLevel = (levelRange + 1) * 10;
            log.printf(getBS("Log.pu.level"), startingLevel, endingLevel);
            TreeMap<Integer, List<String>> itemListPerProbability = new TreeMap<>();
            for (PickupItem pickupItem : pickupItems) {
                int probability = pickupItem.getProbabilities()[levelRange];
                if (itemListPerProbability.containsKey(probability)) {
                    itemListPerProbability.get(probability).add(pickupItem.getItem().getName());
                } else if (probability > 0) {
                    List<String> itemList = new ArrayList<>();
                    itemList.add(pickupItem.getItem().getName());
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
        printSectionTitle("mu");
        log.printf(getBS("Log.mu.description"), settings.getUpdateMovesToGeneration());

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
        printSectionSeparator();
    }

    private boolean shouldLogBaseStatUpdates() {
        return speciesBSUpdater.isUpdated();
    }

    private void logBaseStatsUpdates() {
        printSectionTitle("bsu");
        log.printf(getBS("Log.bsu.description"), settings.getUpdateBaseStatsToGeneration());

        Map<Species, Map<BSUpdateType, Update<Integer>>> updates = speciesBSUpdater.getUpdates();
        for (Map.Entry<Species, Map<BSUpdateType, Update<Integer>>> outer : updates.entrySet()) {
            log.println(outer.getKey().getFullName() + ":");
            for (Map.Entry<BSUpdateType, Update<Integer>> inner : outer.getValue().entrySet()) {
                log.printf("\t%-7s: %3d -> %3d%n",
                        inner.getKey(),
                        inner.getValue().getBefore(), inner.getValue().getAfter());
            }
        }
        printSectionSeparator();
    }

    private boolean shouldLogTypeEffectivenessUpdates() {
        return typeEffUpdater.isUpdated();
    }

    private void logTypeEffectivenessUpdates() {
        printSectionTitle("teu");
        log.printf(getBS("Log.teu.description"), romHandler.generationOfPokemon() == 1 ? 2 : 6);

        String[] effNames = getBS("Log.te.effectivenessNames").split(",");

        Map<Type, Map<Type, Update<Effectiveness>>> updates = typeEffUpdater.getUpdates();
        for (Map.Entry<Type, Map<Type, Update<Effectiveness>>> outer : updates.entrySet()) {
            log.printf(getBS("Log.teu.against"), outer.getKey());
            for (Map.Entry<Type, Update<Effectiveness>> inner : outer.getValue().entrySet()) {
                log.printf("\t%-8s:   %-18s -> %-18s%n",
                        inner.getKey(),
                        effNames[inner.getValue().getBefore().ordinal()],
                        effNames[inner.getValue().getAfter().ordinal()]);
            }
        }
        printSectionSeparator();
    }

    private List<String> getTrainerNames(List<Trainer> trainers) {
        List<String> trainerNames = new ArrayList<>();
        trainerNames.add(""); // for index 0
        for (Trainer t : trainers) {
            if (t.getFullDisplayName() != null) {
                trainerNames.add(t.getFullDisplayName());
            } else if (t.getName() != null) {
                trainerNames.add(t.getName());
            } else {
                trainerNames.add("");
            }
        }
        return trainerNames;
    }

}


