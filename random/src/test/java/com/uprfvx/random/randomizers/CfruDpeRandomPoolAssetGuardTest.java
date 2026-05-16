package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.CfruDpeRandomPoolAssetIssue;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CfruDpeRandomPoolAssetGuardTest {

    @Test
    public void unusableSpeciesReturnsOnlyCandidatesRejectedByAssetPredicate() {
        Species valid = species(1, "Valid");
        Species invalid = species(2, "Invalid");
        SpeciesSet candidates = new SpeciesSet();
        candidates.add(valid);
        candidates.add(invalid);

        SpeciesSet unusable = CfruDpeRandomPoolAssetGuard.unusableSpecies(candidates, species -> species != invalid);

        assertEquals(Set.of(invalid), unusable);
        assertEquals(Set.of(valid, invalid), candidates);
    }

    @Test
    public void summarizeAggregatesAcceptedExcludedReasonsAndSanitizedExamples() {
        Species valid = species(1, "Valid");
        Species noLearnset = species(2, "NoLearnset");
        Species noPalette = species(3, "NoPalette");
        Species ogerpon = species(1017, "Ogerpon");
        List<Species> candidates = List.of(valid, noLearnset, noPalette, ogerpon);

        CfruDpeRandomPoolAssetGuard.Summary summary = CfruDpeRandomPoolAssetGuard.summarize(candidates,
                species -> switch (species.getName()) {
                    case "Valid" -> status(species, 1);
                    case "NoLearnset" -> status(species, 2, CfruDpeRandomPoolAssetIssue.NO_USABLE_LEARNSET);
                    case "NoPalette" -> status(species, 3,
                            CfruDpeRandomPoolAssetIssue.INVALID_NORMAL_PALETTE_POINTER);
                    case "Ogerpon" -> status(species, 1017,
                            CfruDpeRandomPoolAssetIssue.NO_USABLE_LEARNSET,
                            CfruDpeRandomPoolAssetIssue.INVALID_FRONT_BATTLE_SPRITE_POINTER);
                    default -> throw new IllegalArgumentException(species.getName());
                }, 2);

        assertEquals(4, summary.candidateCountBeforeGuard());
        assertEquals(1, summary.acceptedCountAfterGuard());
        assertEquals(3, summary.excludedCount());
        assertEquals(2, summary.countFor(CfruDpeRandomPoolAssetIssue.NO_USABLE_LEARNSET));
        assertEquals(1, summary.countFor(CfruDpeRandomPoolAssetIssue.INVALID_FRONT_BATTLE_SPRITE_POINTER));
        assertEquals(1, summary.countFor(CfruDpeRandomPoolAssetIssue.INVALID_NORMAL_PALETTE_POINTER));
        assertEquals(2, summary.examples().size());
        assertEquals("internal=2 species=NoLearnset reason=no usable learnset",
                summary.examples().get(0).sanitizedLine());
        assertEquals(1, summary.ogerponStatuses().size());
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }

    private static CfruDpeRandomPoolAssetGuard.AssetStatus status(Species species, int internalIdentity,
                                                                  CfruDpeRandomPoolAssetIssue... issues) {
        EnumSet<CfruDpeRandomPoolAssetIssue> issueSet = issues.length == 0
                ? EnumSet.noneOf(CfruDpeRandomPoolAssetIssue.class)
                : EnumSet.copyOf(List.of(issues));
        return new CfruDpeRandomPoolAssetGuard.AssetStatus(internalIdentity, species.getName(), issueSet);
    }
}
