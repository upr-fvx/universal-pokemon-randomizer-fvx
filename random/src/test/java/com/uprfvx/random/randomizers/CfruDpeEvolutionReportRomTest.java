package com.uprfvx.random.randomizers;

import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.romhandlers.Gen3RomHandler;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.romhandlers.romentries.Gen3RomEntry;
import com.uprfvx.romio.romio.RomOpener;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CfruDpeEvolutionReportRomTest {

    private static final String INPUT_ROM_PROPERTY = "uprfvx.cfruDpeEvolutionReportInputRom";
    private static final String OUTPUT_ROM_PROPERTY = "uprfvx.cfruDpeEvolutionReportOutputRom";
    private static final String INPUT_ROM_ENV = "UPRFVX_CFRU_DPE_EVOLUTION_REPORT_INPUT_ROM";
    private static final String OUTPUT_ROM_ENV = "UPRFVX_CFRU_DPE_EVOLUTION_REPORT_OUTPUT_ROM";
    private static final String REPORT_FILE_NAME = "cfru-dpe-evolution-report.txt";
    private static final int EVOLUTION_ENTRY_SIZE = 8;
    private static final List<String> TARGET_SPECIES = List.of(
            "Bulbasaur", "Ivysaur", "Venusaur",
            "Charmander", "Charmeleon", "Charizard",
            "Squirtle", "Wartortle", "Blastoise");

    @Test
    public void cfruDpeEvolutionReportOptIn() throws Exception {
        String inputRom = configuredPath(INPUT_ROM_PROPERTY, INPUT_ROM_ENV);
        String outputRom = configuredPath(OUTPUT_ROM_PROPERTY, OUTPUT_ROM_ENV);
        assumeTrue(hasText(inputRom) || hasText(outputRom),
                "Set -D" + INPUT_ROM_PROPERTY + "=<private-input-rom> and/or -D" + OUTPUT_ROM_PROPERTY
                        + "=<private-output-rom> to run the CFRU/DPE evolution report.");

        StringBuilder report = new StringBuilder();
        appendLine(report, "[CFRU-DPE-EVOLUTION-REPORT]");
        appendLine(report, "Sanitized report only: no ROM path, hash, or full log is written.");
        appendLine(report, "Targets: " + String.join(", ", TARGET_SPECIES));
        appendLine(report, "");

        if (hasText(inputRom)) {
            appendRomReport(report, "INPUT", inputRom);
        } else {
            appendLine(report, "INPUT: <not configured>");
            appendLine(report, "");
        }
        if (hasText(outputRom)) {
            appendRomReport(report, "OUTPUT", outputRom);
        } else {
            appendLine(report, "OUTPUT: <not configured>");
            appendLine(report, "");
        }

        writeReport(report.toString());
    }

    private static void appendRomReport(StringBuilder report, String label, String romPath) throws Exception {
        Gen3RomHandler romHandler = loadGen3Rom(romPath);
        assumeTrue(romHandler.hasExtendedBpreHackSpeciesPool(),
                label + " ROM is not an extended BPRE hack species pool.");

        Gen3RomEntry romEntry = fieldValue(romHandler, "romEntry", Gen3RomEntry.class);
        int evolutionBaseOffset = romEntry.getIntValue("PokemonEvolutions");
        appendLine(report, label + ":");
        appendLine(report, "  ROM recognized: code=" + romHandler.getROMCode()
                + " version=" + romHandler.getRomVersionForDiagnostics()
                + " isRomHack=" + romHandler.isRomHackForDiagnostics());
        appendLine(report, "  PokemonCount=" + romHandler.getCfruDpePokemonCountForDiagnostics()
                + " PokedexCount=" + romHandler.getCfruDpePokedexCountForDiagnostics()
                + " evolutionTableOffset=" + hex(evolutionBaseOffset));
        appendLine(report, "  evolutionSlotsPerSpecies="
                + romHandler.getEvolutionSlotsPerSpeciesForDiagnostics()
                + " evolutionRowSize=" + hex(romHandler.getEvolutionRowSizeForDiagnostics()));

        for (String speciesName : TARGET_SPECIES) {
            appendSpeciesReport(report, romHandler, speciesName);
        }
        appendLine(report, "");
    }

    private static Gen3RomHandler loadGen3Rom(String romPath) {
        File romFile = new File(romPath);
        RomOpener.Results results = new RomOpener().openRomFile(romFile);
        assertTrue(results.wasOpeningSuccessful(), () -> "Configured ROM could not be opened through RomOpener; "
                + "failType=" + results.getFailType());

        RomHandler loadedHandler = results.getRomHandler();
        assertTrue(loadedHandler instanceof Gen3RomHandler,
                () -> "Configured ROM opened with non-Gen3 handler; handler="
                        + loadedHandler.getClass().getSimpleName()
                        + " code=" + loadedHandler.getROMCode());
        Gen3RomHandler romHandler = (Gen3RomHandler) loadedHandler;
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());
        return romHandler;
    }

    private static void appendSpeciesReport(StringBuilder report, Gen3RomHandler romHandler,
                                            String speciesName) throws Exception {
        Species species = findSpecies(romHandler, speciesName);
        appendLine(report, "  " + speciesName + ":");
        if (species == null) {
            appendLine(report, "    <not loaded>");
            return;
        }

        int internalId = romHandler.getCfruDpeRandomPoolInternalSpeciesId(species);
        int identity = species.getSpeciesSetIdentityNumber();
        int speciesNumber = species.getNumber();
        appendLine(report, "    internal id=" + internalId
                + " identity=" + identity
                + " species number=" + speciesNumber);

        int rowOffset = romHandler.getEvolutionRowOffsetForDiagnostics(species);
        appendLine(report, "    raw evolution row offset=" + hex(rowOffset));
        for (int slot = 0; slot < romHandler.getEvolutionSlotsPerSpeciesForDiagnostics(); slot++) {
            int entryOffset = rowOffset + slot * EVOLUTION_ENTRY_SIZE;
            int method = readWord(romHandler, entryOffset);
            int parameter = readWord(romHandler, entryOffset + 2);
            int targetRawId = readWord(romHandler, entryOffset + 4);
            int padding = readWord(romHandler, entryOffset + 6);
            appendLine(report, "    slot " + slot
                    + ": raw=[" + method + "," + parameter + "," + targetRawId + "," + padding + "]"
                    + " method=" + methodName(method)
                    + " parameter/level=" + parameter
                    + " target raw id=" + targetRawId
                    + " decoded target species name=" + decodedTargetName(romHandler, targetRawId));
        }
    }

    private static Species findSpecies(Gen3RomHandler romHandler, String expectedName) {
        String normalizedExpected = normalize(expectedName);
        return romHandler.getSpeciesInclFormes().stream()
                .filter(species -> species != null && species.getName() != null)
                .filter(species -> normalize(species.getName()).equals(normalizedExpected))
                .findFirst()
                .orElse(null);
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static int readWord(Gen3RomHandler romHandler, int offset) throws Exception {
        Method readWord = Gen3RomHandler.class.getSuperclass().getDeclaredMethod("readWord", int.class);
        readWord.setAccessible(true);
        return (Integer) readWord.invoke(romHandler, offset);
    }

    private static String decodedTargetName(Gen3RomHandler romHandler, int targetRawId) throws Exception {
        if (targetRawId <= 0) {
            return "<none>";
        }
        Species[] pokesInternal = fieldValue(romHandler, "pokesInternal", Species[].class);
        if (targetRawId >= pokesInternal.length || pokesInternal[targetRawId] == null) {
            return "<invalid>";
        }
        return pokesInternal[targetRawId].getName();
    }

    private static String methodName(int method) {
        if (method == 0) {
            return "NONE";
        }
        if (method < 0 || method > Gen3Constants.evolutionMethodCount) {
            return "UNKNOWN(" + method + ")";
        }
        EvolutionType evolutionType = Gen3Constants.evolutionTypeFromIndex(method);
        return evolutionType == null ? "UNKNOWN(" + method + ")" : evolutionType.name();
    }

    private static String configuredPath(String propertyName, String envName) {
        String property = System.getProperty(propertyName);
        if (hasText(property)) {
            return property;
        }
        return System.getenv(envName);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void writeReport(String report) throws IOException {
        System.out.print(report);
        System.out.flush();

        Path reportPath = reportPath();
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, report, StandardCharsets.UTF_8);
        System.out.println("Report file: " + reportPath);
    }

    private static Path reportPath() {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path randomDir = userDir.getFileName() != null && userDir.getFileName().toString().equals("random")
                ? userDir
                : userDir.resolve("random");
        return randomDir.resolve("build").resolve("reports").resolve("diagnostics").resolve(REPORT_FILE_NAME);
    }

    private static String hex(int value) {
        return String.format(Locale.ROOT, "0x%X", value);
    }

    private static void appendLine(StringBuilder report, String line) {
        report.append(line).append(System.lineSeparator());
    }

    private static <T> T fieldValue(Object target, String name, Class<T> expectedType) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        Object value = field.get(target);
        assertNotNull(value, "Expected field to be initialized: " + name);
        return expectedType.cast(value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
