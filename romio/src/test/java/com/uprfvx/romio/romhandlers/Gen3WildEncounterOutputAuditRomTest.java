package com.uprfvx.romio.romhandlers;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class Gen3WildEncounterOutputAuditRomTest {

    private static final String BASE_ROM_PROPERTY = "uprfvx.wildEncounterAuditBaseRom";
    private static final String BASE_ROM_ENV = "UPRFVX_WILD_ENCOUNTER_AUDIT_BASE_ROM";
    private static final String OUTPUT_ROM_PROPERTY = "uprfvx.wildEncounterAuditOutputRom";
    private static final String OUTPUT_ROM_ENV = "UPRFVX_WILD_ENCOUNTER_AUDIT_OUTPUT_ROM";
    private static final String REPORT_FILE_NAME = "wild-encounter-output-audit.txt";

    @Test
    public void wildEncounterBaseVsOutputAuditReportOptIn() throws IOException {
        String baseRomPath = configuredValue(BASE_ROM_PROPERTY, BASE_ROM_ENV);
        String outputRomPath = configuredValue(OUTPUT_ROM_PROPERTY, OUTPUT_ROM_ENV);
        assumeTrue(baseRomPath != null && !baseRomPath.isBlank()
                        && outputRomPath != null && !outputRomPath.isBlank(),
                "Set -D" + BASE_ROM_PROPERTY + "=<private-base-rom> and -D" + OUTPUT_ROM_PROPERTY
                        + "=<private-output-rom> to run the Gen3 wild encounter output audit.");

        Gen3RomHandler baseRomHandler = loadGen3Rom(baseRomPath);
        Gen3RomHandler outputRomHandler = loadGen3Rom(outputRomPath);

        Gen3RomHandler.FrlgWildEncounterOutputAuditReport audit =
                baseRomHandler.getFrlgWildEncounterOutputAuditForDiagnostics(outputRomHandler);

        List<String> report = new ArrayList<>();
        report.add("Gen3 wild encounter base-vs-output audit");
        report.add("Base ROM path: <redacted>");
        report.add("Output ROM path: <redacted>");
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
                + " romType=" + romHandler.getRomEntry().getRomType());
    }

    private static void appendAudit(List<String> report,
                                    Gen3RomHandler.FrlgWildEncounterOutputAuditReport audit) {
        Gen3RomHandler.FrlgWildEncounterOutputAuditSummary summary = audit.summary();
        report.add("");
        report.add("[wild encounter base-vs-output summary]");
        report.add("totalEncounterSlots=" + summary.totalEncounterSlots()
                + " changedSlots=" + summary.changedSlots()
                + " unchangedSlots=" + summary.unchangedSlots()
                + " changedPercentage=" + String.format(Locale.ROOT, "%.2f", summary.changedPercentage()));

        report.add("");
        report.add("[wild encounter base-vs-output slots]");
        for (Gen3RomHandler.FrlgWildEncounterOutputAuditRow row : audit.rows()) {
            report.add("  areaIndex=" + row.areaIndex()
                    + " area=" + row.areaName()
                    + " mapIndex=" + row.mapIndex()
                    + " locationTag=" + missing(row.locationTag())
                    + " encounterType=" + row.encounterType()
                    + " rate=" + row.encounterRate()
                    + " slot=" + row.slotIndex()
                    + " level=" + formatLevel(row.level(), row.maxLevel())
                    + " baseSpecies=" + formatSpecies(row.baseSpeciesId(), row.baseSpeciesName())
                    + " outputSpecies=" + formatSpecies(row.outputSpeciesId(), row.outputSpeciesName())
                    + " changedFromBase=" + yesNo(row.changedFromBase()));
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

    private static String formatLevel(int level, int maxLevel) {
        return maxLevel == 0 ? Integer.toString(level) : level + "-" + maxLevel;
    }

    private static String formatSpecies(int speciesId, String speciesName) {
        return speciesId < 0 ? "<missing>" : speciesId + " " + speciesName;
    }

    private static String missing(String value) {
        return value == null || value.isBlank() ? "<missing>" : value;
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
