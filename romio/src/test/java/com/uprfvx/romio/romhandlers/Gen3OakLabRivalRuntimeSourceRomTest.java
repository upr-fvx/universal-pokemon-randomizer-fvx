package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.gamedata.TrainerPokemon;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3OakLabRivalRuntimeSourceRomTest {

    private static final String ROM_PATH_PROPERTY = "uprfvx.oakLabRivalRuntimeSourceRom";
    private static final String ROM_PATH_ENV = "UPRFVX_OAK_LAB_RIVAL_RUNTIME_SOURCE_ROM";
    private static final String STARTERS_PROPERTY = "uprfvx.oakLabRivalRuntimeSourceStarters";
    private static final String STARTERS_ENV = "UPRFVX_OAK_LAB_RIVAL_RUNTIME_SOURCE_STARTERS";
    private static final String REPORT_FILE_NAME = "oak-lab-rival-runtime-source-report.txt";

    @Test
    public void oakLabRivalRuntimeSourceReportOptIn() throws IOException {
        String romPath = configuredRomPath();
        assumeTrue(romPath != null && !romPath.isBlank(),
                "Set -D" + ROM_PATH_PROPERTY + "=<private-rom> to run the Oak Lab Rival source report.");

        Gen3RomHandler romHandler = loadGen3Rom(romPath);
        List<String> report = new ArrayList<>();
        report.add("Oak Lab Rival runtime source report");
        report.add("ROM path: <redacted>");
        report.add("ROM code=" + romHandler.getRomEntry().getRomCode()
                + " version=" + romHandler.getRomEntry().getVersion()
                + " romType=" + romHandler.getRomEntry().getRomType());
        report.add("trainerDataOffset=" + hex(romHandler.getTrainerDataOffsetForDiagnostics())
                + " trainerEntrySize=" + romHandler.getTrainerEntrySizeForDiagnostics()
                + " loadedTrainerCount=" + romHandler.getTrainerCountForDiagnostics());
        report.add("trainerPokemonSpeciesWriteMode="
                + (romHandler.usesInternalSpeciesIdentityForTrainerPokemonDiagnostics()
                ? "internal SpeciesSet identity" : "vanilla internal species id"));
        appendState(report, "current", romHandler);

        List<Species> configuredStarters = configuredStarters(romHandler);
        if (!configuredStarters.isEmpty()) {
            romHandler.setStarters(configuredStarters);
            appendState(report, "after in-memory setStarters", romHandler);
        }

        Path reportPath = reportPath();
        Files.createDirectories(reportPath.getParent());
        Files.write(reportPath, report);
    }

    private static void appendState(List<String> report, String label, Gen3RomHandler romHandler) {
        report.add("");
        report.add("[" + label + "]");
        report.add("starter script slots:");
        for (Gen3RomHandler.FrlgOakLabStarterScriptSlot slot
                : romHandler.getFrlgOakLabStarterScriptSlotsForDiagnostics()) {
            report.add("  playerSlot=" + slot.playerSlot()
                    + " playerSpeciesOffset=" + hex(slot.playerSpeciesOffset())
                    + " playerRawId=" + slot.playerRawSpeciesId()
                    + " playerDecoded=" + romHandler.getFrlgOakLabSpeciesNameForDiagnostics(slot.playerRawSpeciesId())
                    + " rivalSpeciesOffset=" + hex(slot.rivalSpeciesOffset())
                    + " rivalRawId=" + slot.rivalRawSpeciesId()
                    + " rivalDecoded=" + romHandler.getFrlgOakLabSpeciesNameForDiagnostics(slot.rivalRawSpeciesId()));
        }

        report.add("script commands near Oak Lab starter flow:");
        appendScriptCommands(report, romHandler.getFrlgOakLabScriptCommandsNearStarterFlowForDiagnostics(), 120);

        List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> nearbyTrainerBattleCommands =
                romHandler.getFrlgOakLabTrainerBattleCommandsForDiagnostics();
        report.add("trainerbattle commands near StarterPokemon:");
        appendTrainerBattleCommands(report, nearbyTrainerBattleCommands, 80);

        List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> globalTrainerBattleCommands =
                romHandler.getFrlgTrainerBattleCommandsForDiagnostics();
        report.add("trainerbattle commands in whole ROM:");
        appendTrainerBattleCommands(report, globalTrainerBattleCommands, 200);

        report.add("Kanto starter raw species literal candidates:");
        appendLiteralCandidates(report, romHandler.getFrlgKantoStarterLiteralCandidatesForDiagnostics(), romHandler, 240);

        List<Integer> idsByPlayerSlot = romHandler.getFrlgOakLabRivalTrainerIdsByPlayerStarterSlot();
        report.add("candidate trainer ids by player starter slot=" + idsByPlayerSlot);
        Set<Integer> candidateIds = candidateTrainerIds(idsByPlayerSlot, nearbyTrainerBattleCommands,
                globalTrainerBattleCommands);
        report.add("loaded candidate trainer parties:");
        for (int trainerId : candidateIds) {
            Trainer trainer = findTrainer(romHandler.getTrainers(), trainerId);
            report.add("  trainerId=" + trainerId + " party=" + formatParty(trainer));
        }
        report.add("raw candidate trainer parties:");
        for (Gen3RomHandler.FrlgRawTrainerPartyDiagnostics trainer
                : romHandler.getRawTrainerPartyDiagnostics(new ArrayList<>(candidateIds))) {
            appendRawTrainerParty(report, trainer);
        }

        report.add("loaded trainer parties containing Bulbasaur/Charmander/Squirtle:");
        int loadedCount = 0;
        for (Trainer trainer : romHandler.getTrainers()) {
            if (containsKantoStarter(trainer)) {
                if (loadedCount++ < 120) {
                    report.add("  trainerId=" + trainer.getIndex() + " party=" + formatParty(trainer));
                }
            }
        }
        report.add("  total=" + loadedCount + " reported=" + Math.min(loadedCount, 120));

        report.add("raw trainer parties containing Bulbasaur/Charmander/Squirtle:");
        List<Gen3RomHandler.FrlgRawTrainerPartyDiagnostics> rawKantoStarterCandidates =
                romHandler.getRawTrainerKantoStarterPartyCandidatesForDiagnostics();
        int rawCount = 0;
        for (Gen3RomHandler.FrlgRawTrainerPartyDiagnostics trainer : rawKantoStarterCandidates) {
            if (rawCount++ >= 160) {
                break;
            }
            appendRawTrainerParty(report, trainer);
        }
        report.add("  total=" + rawKantoStarterCandidates.size()
                + " reported=" + Math.min(rawKantoStarterCandidates.size(), 160));
    }

    private static void appendRawTrainerParty(List<String> report,
                                              Gen3RomHandler.FrlgRawTrainerPartyDiagnostics trainer) {
            report.add("  trainerId=" + trainer.trainerId()
                    + " trainerOffset=" + hex(trainer.trainerOffset())
                    + " partyFlags=" + trainer.partyFlags()
                    + " partySize=" + trainer.partySize()
                    + " partyPointer=" + hexOrMissing(trainer.partyPointer())
                    + " partyPointerValid=" + trainer.partyPointerValid());
            for (Gen3RomHandler.FrlgRawTrainerPokemonDiagnostics pokemon : trainer.party()) {
                report.add("    partyIndex=" + pokemon.partyIndex()
                        + " offset=" + hex(pokemon.offset())
                        + " level=" + pokemon.level()
                        + " rawSpeciesId=" + pokemon.rawSpeciesId()
                        + " decoded=" + pokemon.decodedSpeciesName());
            }
    }

    private static Set<Integer> candidateTrainerIds(List<Integer> idsByPlayerSlot,
                                                    List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> nearbyCommands,
                                                    List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> globalCommands) {
        Set<Integer> ids = new LinkedHashSet<>(idsByPlayerSlot);
        for (Gen3RomHandler.FrlgOakLabTrainerBattleCommand command : nearbyCommands) {
            ids.add(command.trainerId());
        }
        for (Gen3RomHandler.FrlgOakLabTrainerBattleCommand command : globalCommands) {
            if (command.trainerId() >= 320 && command.trainerId() <= 340) {
                ids.add(command.trainerId());
            }
        }
        ids.add(326);
        ids.add(327);
        ids.add(328);
        return ids;
    }

    private static void appendScriptCommands(List<String> report,
                                             List<Gen3RomHandler.FrlgOakLabScriptCommand> commands,
                                             int limit) {
        int count = 0;
        for (Gen3RomHandler.FrlgOakLabScriptCommand command : commands) {
            if (count++ >= limit) {
                break;
            }
            report.add("  offset=" + hex(command.offset())
                    + " command=" + command.command()
                    + " arg1=" + hex(command.firstArgument())
                    + " arg2=" + signedHex(command.secondArgument()));
        }
        report.add("  total=" + commands.size() + " reported=" + Math.min(commands.size(), limit));
    }

    private static void appendTrainerBattleCommands(List<String> report,
                                                    List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> commands,
                                                    int limit) {
        int count = 0;
        for (Gen3RomHandler.FrlgOakLabTrainerBattleCommand command : commands) {
            if (count++ >= limit) {
                break;
            }
            report.add("  offset=" + hex(command.offset())
                    + " battleType=" + command.battleType()
                    + " trainerId=" + command.trainerId()
                    + " arg=" + command.argument());
        }
        report.add("  total=" + commands.size() + " reported=" + Math.min(commands.size(), limit));
    }

    private static void appendLiteralCandidates(List<String> report,
                                                List<Gen3RomHandler.FrlgKantoStarterLiteralCandidate> candidates,
                                                Gen3RomHandler romHandler,
                                                int limit) {
        int count = 0;
        for (Gen3RomHandler.FrlgKantoStarterLiteralCandidate candidate : candidates) {
            if (count++ >= limit) {
                break;
            }
            report.add("  offset=" + hex(candidate.offset())
                    + " rawSpeciesId=" + candidate.rawSpeciesId()
                    + " decoded=" + romHandler.getFrlgOakLabSpeciesNameForDiagnostics(candidate.rawSpeciesId())
                    + " context=" + candidate.context());
        }
        report.add("  total=" + candidates.size() + " reported=" + Math.min(candidates.size(), limit));
    }

    private static Trainer findTrainer(List<Trainer> trainers, int trainerId) {
        for (Trainer trainer : trainers) {
            if (trainer.getIndex() == trainerId) {
                return trainer;
            }
        }
        return null;
    }

    private static String formatParty(Trainer trainer) {
        if (trainer == null) {
            return "<not loaded>";
        }
        List<String> party = new ArrayList<>();
        for (TrainerPokemon pokemon : trainer.getPokemon()) {
            Species species = pokemon.getSpecies();
            party.add((species == null ? "?" : species.getFullName()) + " Lv" + pokemon.getLevel());
        }
        return party.toString();
    }

    private static boolean containsKantoStarter(Trainer trainer) {
        return trainer.getPokemon().stream()
                .map(TrainerPokemon::getSpecies)
                .filter(species -> species != null)
                .map(Species::getName)
                .map(Gen3OakLabRivalRuntimeSourceRomTest::normalize)
                .anyMatch(name -> name.equals("bulbasaur")
                        || name.equals("charmander")
                        || name.equals("squirtle"));
    }

    private static List<Species> configuredStarters(Gen3RomHandler romHandler) {
        String starterNames = configuredStarterNames();
        if (starterNames == null || starterNames.isBlank()) {
            return List.of();
        }
        String[] names = starterNames.split(",");
        assumeTrue(names.length == 3, "Configure exactly three comma-separated starter names.");
        List<Species> starters = new ArrayList<>();
        for (String name : names) {
            starters.add(findSpeciesByName(romHandler, name.trim()));
        }
        return starters;
    }

    private static Species findSpeciesByName(Gen3RomHandler romHandler, String name) {
        String normalized = normalize(name);
        for (Species species : romHandler.getSpecies()) {
            if (species != null
                    && (normalize(species.getName()).equals(normalized)
                    || normalize(species.getFullName()).equals(normalized))) {
                return species;
            }
        }
        throw new IllegalArgumentException("Configured starter species was not loaded: " + name);
    }

    private static Gen3RomHandler loadGen3Rom(String romPath) {
        RomHandler.Factory factory = new Gen3RomHandler.Factory();
        assertTrue(factory.isLoadable(romPath), "Configured ROM is not loadable as a Gen3 ROM.");
        Gen3RomHandler romHandler = (Gen3RomHandler) factory.create();
        assertTrue(romHandler.loadRom(romPath), "Configured Gen3 ROM could not be loaded.");
        return romHandler;
    }

    private static String configuredRomPath() {
        String property = System.getProperty(ROM_PATH_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(ROM_PATH_ENV);
    }

    private static String configuredStarterNames() {
        String property = System.getProperty(STARTERS_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(STARTERS_ENV);
    }

    private static Path reportPath() {
        return Path.of("build").resolve("reports").resolve("diagnostics").resolve(REPORT_FILE_NAME);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String hex(int value) {
        return String.format(Locale.ROOT, "0x%X", value);
    }

    private static String hexOrMissing(int value) {
        return value < 0 ? "<missing>" : hex(value);
    }

    private static String signedHex(int value) {
        return value < 0 ? "<none>" : hex(value);
    }
}
