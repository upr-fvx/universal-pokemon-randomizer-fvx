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

public class Gen3IntroMonVisualSourceRomTest {

    private static final String BASE_ROM_PROPERTY = "uprfvx.introMonVisualSourceBaseRom";
    private static final String BASE_ROM_ENV = "UPRFVX_INTRO_MON_VISUAL_SOURCE_BASE_ROM";
    private static final String OUTPUT_ROM_PROPERTY = "uprfvx.introMonVisualSourceOutputRom";
    private static final String OUTPUT_ROM_ENV = "UPRFVX_INTRO_MON_VISUAL_SOURCE_OUTPUT_ROM";
    private static final String REPORT_FILE_NAME = "intro-mon-visual-source-report.txt";

    @Test
    public void introMonVisualSourceReportOptIn() throws IOException {
        String baseRomPath = configuredValue(BASE_ROM_PROPERTY, BASE_ROM_ENV);
        assumeTrue(baseRomPath != null && !baseRomPath.isBlank(),
                "Set -D" + BASE_ROM_PROPERTY + "=<private-input-rom> to run the Intro Mon visual source report.");

        Gen3RomHandler baseRomHandler = loadGen3Rom(baseRomPath);
        String outputRomPath = configuredValue(OUTPUT_ROM_PROPERTY, OUTPUT_ROM_ENV);
        Gen3RomHandler outputRomHandler = outputRomPath == null || outputRomPath.isBlank()
                ? null
                : loadGen3Rom(outputRomPath);

        List<String> report = new ArrayList<>();
        report.add("Intro Mon visual source diagnostics");
        report.add("Base ROM path: <redacted>");
        report.add("Output ROM path: " + (outputRomHandler == null ? "<not configured>" : "<redacted>"));
        appendRomSummary(report, "base", baseRomHandler);
        appendDiagnostics(report, "base", baseRomHandler.getIntroMonVisualSourceDiagnosticsForDiagnostics());
        if (outputRomHandler != null) {
            appendRomSummary(report, "output", outputRomHandler);
            appendDiagnostics(report, "output", outputRomHandler.getIntroMonVisualSourceDiagnosticsForDiagnostics());
            appendComparison(report, Gen3RomHandler.compareIntroMonVisualSourceDiagnostics(
                    baseRomHandler.getIntroMonVisualSourceDiagnosticsForDiagnostics(),
                    outputRomHandler.getIntroMonVisualSourceDiagnosticsForDiagnostics()));
            appendCandidateSearch(report,
                    baseRomHandler.getIntroMonVisualSourceSearchCandidatesForDiagnostics(outputRomHandler));
        }

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

    private static void appendDiagnostics(List<String> report, String label,
                                          Gen3RomHandler.Gen3IntroMonVisualSourceDiagnostics diagnostics) {
        report.add("");
        report.add("[" + label + " intro visual sources]");
        report.add("cryOffset=" + hex(diagnostics.cryOffset())
                + " imageOffset=" + hex(diagnostics.imageOffset())
                + " paletteOffset=" + hex(diagnostics.paletteOffset())
                + " otherOffset=" + hex(diagnostics.otherOffset())
                + " imageTableOffset=" + hex(diagnostics.imageTableOffset())
                + " paletteTableOffset=" + hex(diagnostics.paletteTableOffset()));
        for (Gen3RomHandler.Gen3IntroMonVisualSourceCandidate candidate : diagnostics.candidates()) {
            report.add("  source=" + candidate.source()
                    + " offset=" + hex(candidate.offset())
                    + " rawSpeciesId=" + missing(candidate.rawSpeciesId())
                    + " decoded=" + candidate.decodedSpecies()
                    + " pointer=" + hexOrMissing(candidate.pointer())
                    + " expectedSpeciesId=" + missing(candidate.expectedSpeciesId()));
        }
    }

    private static void appendComparison(List<String> report,
                                         Gen3RomHandler.Gen3IntroMonVisualSourceComparison comparison) {
        report.add("");
        report.add("[base-vs-output intro visual source comparison]");
        for (Gen3RomHandler.Gen3IntroMonVisualSourceCandidateComparison candidate : comparison.candidates()) {
            report.add("  source=" + candidate.source()
                    + " offset=" + hex(candidate.offset())
                    + " baseSpeciesId=" + missing(candidate.baseSpeciesId())
                    + " baseDecoded=" + candidate.baseDecodedSpecies()
                    + " outputSpeciesId=" + missing(candidate.outputSpeciesId())
                    + " outputDecoded=" + candidate.outputDecodedSpecies()
                    + " changedFromBase=" + yesNo(candidate.changedFromBase()));
        }
    }

    private static void appendCandidateSearch(List<String> report,
                                              List<Gen3RomHandler.Gen3IntroMonVisualSourceSearchCandidate> candidates) {
        report.add("");
        report.add("[base-vs-output intro visual source candidate search]");
        for (Gen3RomHandler.Gen3IntroMonVisualSourceSearchCandidate candidate : candidates) {
            report.add("  candidateType=" + candidate.candidateType()
                    + " offset=" + hex(candidate.offset())
                    + " baseValue=" + candidate.baseValue()
                    + " outputValue=" + candidate.outputValue()
                    + " changedFromBase=" + yesNo(candidate.changedFromBase())
                    + " plausibilityReason=" + candidate.plausibilityReason());
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

    private static String hex(int value) {
        return String.format(Locale.ROOT, "0x%X", value);
    }

    private static String hexOrMissing(int value) {
        return value < 0 ? "<missing>" : hex(value);
    }

    private static String missing(int value) {
        return value < 0 ? "<missing>" : Integer.toString(value);
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
