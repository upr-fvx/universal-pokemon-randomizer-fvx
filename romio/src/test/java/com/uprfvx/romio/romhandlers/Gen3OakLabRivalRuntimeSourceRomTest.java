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

        List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> commands =
                romHandler.getFrlgOakLabTrainerBattleCommandsForDiagnostics();
        report.add("trainerbattle 9 commands near StarterPokemon:");
        for (Gen3RomHandler.FrlgOakLabTrainerBattleCommand command : commands) {
            report.add("  offset=" + hex(command.offset())
                    + " trainerId=" + command.trainerId()
                    + " helperFlags=" + command.helperFlags());
        }

        List<Integer> idsByPlayerSlot = romHandler.getFrlgOakLabRivalTrainerIdsByPlayerStarterSlot();
        report.add("candidate trainer ids by player starter slot=" + idsByPlayerSlot);
        report.add("candidate trainer parties:");
        for (int trainerId : candidateTrainerIds(idsByPlayerSlot, commands)) {
            Trainer trainer = findTrainer(romHandler.getTrainers(), trainerId);
            report.add("  trainerId=" + trainerId + " party=" + formatParty(trainer));
        }
    }

    private static Set<Integer> candidateTrainerIds(List<Integer> idsByPlayerSlot,
                                                    List<Gen3RomHandler.FrlgOakLabTrainerBattleCommand> commands) {
        Set<Integer> ids = new LinkedHashSet<>(idsByPlayerSlot);
        for (Gen3RomHandler.FrlgOakLabTrainerBattleCommand command : commands) {
            ids.add(command.trainerId());
        }
        ids.add(326);
        ids.add(327);
        ids.add(328);
        return ids;
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
}
