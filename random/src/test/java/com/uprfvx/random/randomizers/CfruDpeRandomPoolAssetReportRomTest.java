package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.CfruDpeRandomPoolAssetIssue;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.MoveLearnt;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.romio.RomOpener;
import com.uprfvx.romio.romhandlers.Gen3RomHandler;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CfruDpeRandomPoolAssetReportRomTest {

    private static final String ROM_PATH_PROPERTY = "uprfvx.cfruDpePoolAssetReportRom";
    private static final String ROM_PATH_ENV = "UPRFVX_CFRU_DPE_POOL_ASSET_REPORT_ROM";
    private static final int EXAMPLE_LIMIT = 20;
    private static final String REPORT_FILE_NAME = "cfru-dpe-pool-asset-report.txt";

    @Test
    public void cfruDpePoolAssetReportOptIn() throws IOException {
        String romPath = configuredRomPath();
        assumeTrue(romPath != null && !romPath.isBlank(),
                "Set -D" + ROM_PATH_PROPERTY + "=<private-rom> to run the CFRU/DPE pool asset report.");

        Gen3RomHandler romHandler = loadGen3Rom(romPath);
        assumeTrue(romHandler.hasExtendedBpreHackSpeciesPool(),
                "Loaded ROM is not an extended BPRE hack species pool.");

        SpeciesSet candidates = new SpeciesSet(romHandler.getRestrictedSpeciesService()
                .getSpecies(false, false, false));
        Map<Integer, List<MoveLearnt>> movesets = romHandler.getMovesLearnt();
        CfruDpeRandomPoolAssetGuard.Summary summary = CfruDpeRandomPoolAssetGuard.summarize(candidates,
                species -> statusFor(romHandler, movesets, species), EXAMPLE_LIMIT);

        writeSummary(buildSummary(romHandler, movesets, summary));
        assertTrue(summary.candidateCountBeforeGuard() >= summary.acceptedCountAfterGuard());
    }

    private static String configuredRomPath() {
        String property = System.getProperty(ROM_PATH_PROPERTY);
        if (property != null && !property.isBlank()) {
            return property;
        }
        return System.getenv(ROM_PATH_ENV);
    }

    private static Gen3RomHandler loadGen3Rom(String romPath) {
        File romFile = new File(romPath);
        RomOpener.Results results = new RomOpener().openRomFile(romFile);
        assertTrue(results.wasOpeningSuccessful(), () -> "Configured ROM could not be opened through RomOpener; "
                + "failType=" + results.getFailType()
                + " " + sanitizedHeaderSummary(romFile));

        RomHandler loadedHandler = results.getRomHandler();
        assertTrue(loadedHandler instanceof Gen3RomHandler,
                () -> "Configured ROM opened with non-Gen3 handler; handler="
                        + loadedHandler.getClass().getSimpleName()
                        + " code=" + loadedHandler.getROMCode());
        Gen3RomHandler romHandler = (Gen3RomHandler) loadedHandler;
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());
        return romHandler;
    }

    private static String sanitizedHeaderSummary(File romFile) {
        try (RandomAccessFile raf = new RandomAccessFile(romFile, "r")) {
            if (raf.length() <= Gen3Constants.romVersionOffset) {
                return "header=<too-short>";
            }
            byte[] romCodeBytes = new byte[4];
            raf.seek(Gen3Constants.romCodeOffset);
            raf.readFully(romCodeBytes);
            raf.seek(Gen3Constants.romVersionOffset);
            int version = raf.readUnsignedByte();
            return "headerCode=" + sanitizeRomCode(new String(romCodeBytes, StandardCharsets.US_ASCII))
                    + " headerVersion=" + version
                    + " fileLengthBytes=" + raf.length();
        } catch (IOException ex) {
            return "header=<unreadable>";
        }
    }

    private static String sanitizeRomCode(String romCode) {
        StringBuilder sanitized = new StringBuilder();
        for (int i = 0; i < romCode.length(); i++) {
            char c = romCode.charAt(i);
            sanitized.append(Character.isLetterOrDigit(c) ? c : '?');
        }
        return sanitized.toString();
    }

    private static CfruDpeRandomPoolAssetGuard.AssetStatus statusFor(Gen3RomHandler romHandler,
                                                                     Map<Integer, List<MoveLearnt>> movesets,
                                                                     Species species) {
        EnumSet<CfruDpeRandomPoolAssetIssue> issues =
                romHandler.getCfruDpeRandomPoolSpeciesAssetIssues(species, movesets);
        int internalIdentity = romHandler.getCfruDpeRandomPoolInternalSpeciesId(species);
        String speciesName = species == null ? null : species.getName();
        return new CfruDpeRandomPoolAssetGuard.AssetStatus(internalIdentity, speciesName, issues);
    }

    private static String buildSummary(Gen3RomHandler romHandler, Map<Integer, List<MoveLearnt>> movesets,
                                       CfruDpeRandomPoolAssetGuard.Summary summary) {
        StringBuilder report = new StringBuilder();
        appendLine(report, "[CFRU-DPE-POOL-ASSET-REPORT]");
        appendLine(report, "ROM recognized: code=" + romHandler.getROMCode()
                + " version=" + romHandler.getRomVersionForDiagnostics()
                + " isRomHack=" + romHandler.isRomHackForDiagnostics());
        appendLine(report, "PokemonCount=" + romHandler.getCfruDpePokemonCountForDiagnostics()
                + " PokedexCount=" + romHandler.getCfruDpePokedexCountForDiagnostics()
                + " maxInternalSpeciesId=" + romHandler.getCfruDpeMaxInternalSpeciesIdForDiagnostics());
        appendLine(report, "candidate count before guard=" + summary.candidateCountBeforeGuard());
        appendLine(report, "accepted count after guard=" + summary.acceptedCountAfterGuard());
        appendLine(report, "excluded count=" + summary.excludedCount());
        for (CfruDpeRandomPoolAssetIssue reason : CfruDpeRandomPoolAssetIssue.values()) {
            appendLine(report, "excluded " + reason.getLabel() + "=" + summary.countFor(reason));
        }
        appendLine(report, "sanitized examples, max " + EXAMPLE_LIMIT + ":");
        for (CfruDpeRandomPoolAssetGuard.AssetStatus example : summary.examples()) {
            appendLine(report, "  " + example.sanitizedLine());
        }
        appendLine(report, "CFRU/DPE learnset table:");
        appendLine(report, "  " + romHandler.getCfruDpeLevelUpLearnsetsTableDiagnostics());
        appendOgerponStatuses(report, summary);
        appendOgerponLearnsetDiagnostics(report, romHandler, movesets);
        appendOgerponSpritePaletteDiagnostics(report, romHandler);
        appendLearnsetNeighborhoodDiagnostics(report, romHandler, movesets);
        appendSpritePaletteNeighborhoodDiagnostics(report, romHandler);
        return report.toString();
    }

    private static void writeSummary(String report) throws IOException {
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

    private static void appendOgerponStatuses(StringBuilder report, CfruDpeRandomPoolAssetGuard.Summary summary) {
        appendLine(report, "Ogerpon/Forme status:");
        appendNamedOgerponStatus(report, "base Ogerpon", summary, "ogerpon", true);
        appendNamedOgerponStatus(report, "Wellspring", summary, "wellspring", false);
        appendNamedOgerponStatus(report, "Hearthflame", summary, "hearthflame", false);
        appendNamedOgerponStatus(report, "Cornerstone", summary, "cornerstone", false);
        appendNamedOgerponStatus(report, "Terastal", summary, "terastal", false);
    }

    private static void appendNamedOgerponStatus(StringBuilder report, String label,
                                                 CfruDpeRandomPoolAssetGuard.Summary summary, String namePart,
                                                 boolean exactMatch) {
        List<CfruDpeRandomPoolAssetGuard.AssetStatus> matches = summary.ogerponStatuses().stream()
                .filter(status -> status.speciesName() != null)
                .filter(status -> {
                    String lowerName = status.speciesName().toLowerCase(Locale.ROOT);
                    return exactMatch ? lowerName.equals(namePart) : lowerName.contains(namePart);
                })
                .toList();
        if (matches.isEmpty()) {
            appendLine(report, "  " + label + ": <not loaded>");
            return;
        }
        for (CfruDpeRandomPoolAssetGuard.AssetStatus status : matches) {
            appendLine(report, "  " + label + ": " + status.sanitizedLine());
        }
    }

    private static void appendOgerponLearnsetDiagnostics(StringBuilder report, Gen3RomHandler romHandler,
                                                         Map<Integer, List<MoveLearnt>> movesets) {
        appendLine(report, "Ogerpon learnset diagnostics:");
        List<Species> matches = romHandler.getSpecies().stream()
                .filter(species -> species != null && species.getName() != null)
                .filter(species -> species.getName().toLowerCase(Locale.ROOT).contains("ogerpon"))
                .toList();
        if (matches.isEmpty()) {
            appendLine(report, "  <not loaded>");
            return;
        }
        for (Species species : matches) {
            appendLine(report, "  " + romHandler.getCfruDpeLearnsetDiagnostics(species, movesets, 8));
        }
    }

    private static void appendOgerponSpritePaletteDiagnostics(StringBuilder report, Gen3RomHandler romHandler) {
        appendLine(report, "Ogerpon front sprite/palette diagnostics:");
        List<Species> matches = romHandler.getSpecies().stream()
                .filter(species -> species != null && species.getName() != null)
                .filter(species -> species.getName().toLowerCase(Locale.ROOT).contains("ogerpon"))
                .toList();
        if (matches.isEmpty()) {
            appendLine(report, "  <not loaded>");
            return;
        }
        for (Species species : matches) {
            appendLine(report, "  " + romHandler.getCfruDpeSpritePaletteDiagnostics(species));
        }
    }

    private static void appendLearnsetNeighborhoodDiagnostics(StringBuilder report, Gen3RomHandler romHandler,
                                                              Map<Integer, List<MoveLearnt>> movesets) {
        appendLine(report, "CFRU/DPE learnset neighborhood 1418..1439:");
        for (String line : romHandler.getCfruDpeLearnsetNeighborhoodDiagnostics(1418, 1439, movesets, 4)) {
            appendLine(report, "  " + line);
        }
    }

    private static void appendSpritePaletteNeighborhoodDiagnostics(StringBuilder report, Gen3RomHandler romHandler) {
        appendLine(report, "CFRU/DPE front sprite/palette neighborhood 1418..1439:");
        for (String line : romHandler.getCfruDpeSpritePaletteNeighborhoodDiagnostics(1418, 1439)) {
            appendLine(report, "  " + line);
        }
    }

    private static void appendLine(StringBuilder report, String line) {
        report.append(line).append(System.lineSeparator());
    }
}
