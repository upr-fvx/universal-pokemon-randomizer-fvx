package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.CfruDpeRandomPoolAssetIssue;
import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.MoveLearnt;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.romhandlers.Gen3RomHandler;
import com.uprfvx.romio.romhandlers.RomHandler;
import org.junit.jupiter.api.Test;

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

    @Test
    public void cfruDpePoolAssetReportOptIn() {
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

        printSummary(romHandler, summary);
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
        RomHandler.Factory factory = new Gen3RomHandler.Factory();
        assertTrue(factory.isLoadable(romPath), "Configured ROM is not loadable as a Gen3 ROM.");
        Gen3RomHandler romHandler = (Gen3RomHandler) factory.create();
        assertTrue(romHandler.loadRom(romPath), "Configured Gen3 ROM could not be loaded.");
        romHandler.getRestrictedSpeciesService().setRestrictions(new GenRestrictions());
        return romHandler;
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

    private static void printSummary(Gen3RomHandler romHandler, CfruDpeRandomPoolAssetGuard.Summary summary) {
        System.out.println("[CFRU-DPE-POOL-ASSET-REPORT]");
        System.out.println("ROM recognized: code=" + romHandler.getROMCode()
                + " version=" + romHandler.getRomVersionForDiagnostics()
                + " isRomHack=" + romHandler.isRomHackForDiagnostics());
        System.out.println("PokemonCount=" + romHandler.getCfruDpePokemonCountForDiagnostics()
                + " PokedexCount=" + romHandler.getCfruDpePokedexCountForDiagnostics()
                + " maxInternalSpeciesId=" + romHandler.getCfruDpeMaxInternalSpeciesIdForDiagnostics());
        System.out.println("candidate count before guard=" + summary.candidateCountBeforeGuard());
        System.out.println("accepted count after guard=" + summary.acceptedCountAfterGuard());
        System.out.println("excluded count=" + summary.excludedCount());
        for (CfruDpeRandomPoolAssetIssue reason : CfruDpeRandomPoolAssetIssue.values()) {
            System.out.println("excluded " + reason.getLabel() + "=" + summary.countFor(reason));
        }
        System.out.println("sanitized examples, max " + EXAMPLE_LIMIT + ":");
        for (CfruDpeRandomPoolAssetGuard.AssetStatus example : summary.examples()) {
            System.out.println("  " + example.sanitizedLine());
        }
        printOgerponStatuses(summary);
    }

    private static void printOgerponStatuses(CfruDpeRandomPoolAssetGuard.Summary summary) {
        System.out.println("Ogerpon/Forme status:");
        printNamedOgerponStatus("base Ogerpon", summary, "ogerpon", true);
        printNamedOgerponStatus("Wellspring", summary, "wellspring", false);
        printNamedOgerponStatus("Hearthflame", summary, "hearthflame", false);
        printNamedOgerponStatus("Cornerstone", summary, "cornerstone", false);
        printNamedOgerponStatus("Terastal", summary, "terastal", false);
    }

    private static void printNamedOgerponStatus(String label, CfruDpeRandomPoolAssetGuard.Summary summary,
                                                String namePart, boolean exactMatch) {
        List<CfruDpeRandomPoolAssetGuard.AssetStatus> matches = summary.ogerponStatuses().stream()
                .filter(status -> status.speciesName() != null)
                .filter(status -> {
                    String lowerName = status.speciesName().toLowerCase(Locale.ROOT);
                    return exactMatch ? lowerName.equals(namePart) : lowerName.contains(namePart);
                })
                .toList();
        if (matches.isEmpty()) {
            System.out.println("  " + label + ": <not loaded>");
            return;
        }
        for (CfruDpeRandomPoolAssetGuard.AssetStatus status : matches) {
            System.out.println("  " + label + ": " + status.sanitizedLine());
        }
    }
}
