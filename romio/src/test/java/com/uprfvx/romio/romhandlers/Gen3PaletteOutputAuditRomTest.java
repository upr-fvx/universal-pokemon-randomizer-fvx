package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3PaletteOutputAuditRomTest {

    private static final String BASE_ROM_PROPERTY = "uprfvx.paletteAuditBaseRom";
    private static final String BASE_ROM_ENV = "UPRFVX_PALETTE_AUDIT_BASE_ROM";
    private static final String OUTPUT_ROM_PROPERTY = "uprfvx.paletteAuditOutputRom";
    private static final String OUTPUT_ROM_ENV = "UPRFVX_PALETTE_AUDIT_OUTPUT_ROM";
    private static final String SPECIES_IDS_PROPERTY = "uprfvx.paletteAuditSpeciesIds";
    private static final String SPECIES_IDS_ENV = "UPRFVX_PALETTE_AUDIT_SPECIES_IDS";
    private static final String REPORT_FILE_NAME = "pokemon-palette-output-audit.txt";

    @Test
    public void pokemonPaletteBaseVsOutputAuditReportOptIn() throws IOException {
        String baseRomPath = configuredValue(BASE_ROM_PROPERTY, BASE_ROM_ENV);
        String outputRomPath = configuredValue(OUTPUT_ROM_PROPERTY, OUTPUT_ROM_ENV);
        assumeTrue(baseRomPath != null && !baseRomPath.isBlank()
                        && outputRomPath != null && !outputRomPath.isBlank(),
                "Set -D" + BASE_ROM_PROPERTY + "=<private-base-rom> and -D" + OUTPUT_ROM_PROPERTY
                        + "=<private-output-rom> to run the Gen3 Pokemon palette output audit.");

        List<Integer> selectedSpeciesIds = parseSpeciesIds(configuredValue(SPECIES_IDS_PROPERTY, SPECIES_IDS_ENV));
        Gen3RomHandler baseRomHandler = loadGen3Rom(baseRomPath);
        Gen3RomHandler outputRomHandler = loadGen3Rom(outputRomPath);

        Gen3RomHandler.Gen3PaletteOutputAuditReport audit =
                baseRomHandler.getGen3PaletteOutputAuditForDiagnostics(outputRomHandler, selectedSpeciesIds);

        List<String> report = new ArrayList<>();
        report.add("Gen3 Pokemon palette base-vs-output audit");
        report.add("Base ROM path: <redacted>");
        report.add("Output ROM path: <redacted>");
        report.add("Configured species IDs: " + (selectedSpeciesIds.isEmpty() ? "<all modeled species>" : selectedSpeciesIds));
        appendRomSummary(report, "base", baseRomHandler);
        appendRomSummary(report, "output", outputRomHandler);
        appendAudit(report, audit);

        Path reportPath = Path.of("build").resolve("reports").resolve("diagnostics").resolve(REPORT_FILE_NAME);
        Files.createDirectories(reportPath.getParent());
        Files.write(reportPath, report);
    }

    private static void appendRomSummary(List<String> report, String label, Gen3RomHandler romHandler) {
        report.add("");
        report.add("[" + label + " ROM]");
        report.add("romCode=" + romHandler.getRomEntry().getRomCode()
                + " version=" + romHandler.getRomEntry().getVersion()
                + " romType=" + romHandler.getRomEntry().getRomType()
                + " extendedBpreHackSpeciesPool=" + yesNo(romHandler.hasExtendedBpreHackSpeciesPool()));
    }

    private static void appendAudit(List<String> report,
                                    Gen3RomHandler.Gen3PaletteOutputAuditReport audit) {
        Gen3RomHandler.Gen3PaletteOutputAuditSummary summary = audit.summary();
        report.add("");
        report.add("[pokemon palette base-vs-output summary]");
        report.add("sampledCount=" + summary.sampledCount()
                + " normalChangedCount=" + summary.normalChangedCount()
                + " shinyChangedCount=" + summary.shinyChangedCount()
                + " unchangedCount=" + summary.unchangedCount());

        report.add("");
        report.add("[pokemon palette base-vs-output species]");
        for (Gen3RomHandler.Gen3PaletteOutputAuditRow row : audit.rows()) {
            report.add("  speciesId=" + row.speciesId()
                    + " speciesIdentityNumber=" + row.speciesIdentityNumber()
                    + " decodedSpecies=" + row.decodedSpecies()
                    + " baseNormalPalettePointer=" + formatPointer(row.baseNormalPalettePointer())
                    + " outputNormalPalettePointer=" + formatPointer(row.outputNormalPalettePointer())
                    + " baseShinyPalettePointer=" + formatPointer(row.baseShinyPalettePointer())
                    + " outputShinyPalettePointer=" + formatPointer(row.outputShinyPalettePointer())
                    + " baseNormalPaletteDigest=" + row.baseNormalPaletteDigest()
                    + " outputNormalPaletteDigest=" + row.outputNormalPaletteDigest()
                    + " baseShinyPaletteDigest=" + row.baseShinyPaletteDigest()
                    + " outputShinyPaletteDigest=" + row.outputShinyPaletteDigest()
                    + " normalChangedFromBase=" + yesNo(row.normalChangedFromBase())
                    + " shinyChangedFromBase=" + yesNo(row.shinyChangedFromBase()));
        }
    }

    private static Gen3RomHandler loadGen3Rom(String romPath) {
        RomHandler.Factory factory = new Gen3RomHandler.Factory();
        assertTrue(factory.isLoadable(romPath), "Configured ROM is not loadable as a Gen3 ROM.");
        Gen3RomHandler romHandler = (Gen3RomHandler) factory.create();
        assertTrue(romHandler.loadRom(romPath), "Configured Gen3 ROM could not be loaded.");
        return romHandler;
    }

    private static String configuredValue(String propertyName, String environmentName) {
        String property = System.getProperty(propertyName);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(environmentName);
    }

    private static List<Integer> parseSpeciesIds(String rawSpeciesIds) {
        if (rawSpeciesIds == null || rawSpeciesIds.isBlank()) {
            return Collections.emptyList();
        }
        List<Integer> speciesIds = new ArrayList<>();
        for (String part : rawSpeciesIds.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                speciesIds.add(Integer.parseInt(trimmed));
            }
        }
        return speciesIds;
    }

    private static String formatPointer(int pointer) {
        return pointer < 0 ? "<missing>" : String.format(Locale.ROOT, "0x%X", pointer);
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
