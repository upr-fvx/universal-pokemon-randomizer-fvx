package com.uprfvx.random.randomizers;

import com.uprfvx.romio.gamedata.CfruDpeRandomPoolAssetIssue;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

final class CfruDpeRandomPoolAssetGuard {

    private CfruDpeRandomPoolAssetGuard() {
    }

    static SpeciesSet unusableSpecies(SpeciesSet candidates, Predicate<Species> usableSpecies) {
        SpeciesSet unusable = new SpeciesSet();
        for (Species species : candidates) {
            if (!usableSpecies.test(species)) {
                unusable.add(species);
            }
        }
        return unusable;
    }

    static Summary summarize(Collection<Species> candidates, AssetStatusProvider statusProvider, int exampleLimit) {
        int candidateCount = 0;
        int acceptedCount = 0;
        EnumMap<CfruDpeRandomPoolAssetIssue, Integer> excludedByReason =
                new EnumMap<>(CfruDpeRandomPoolAssetIssue.class);
        List<AssetStatus> examples = new ArrayList<>();
        List<AssetStatus> ogerponStatuses = new ArrayList<>();

        for (Species species : candidates) {
            candidateCount++;
            AssetStatus status = statusProvider.statusFor(species);
            if (status.accepted()) {
                acceptedCount++;
            } else {
                for (CfruDpeRandomPoolAssetIssue reason : status.issues()) {
                    excludedByReason.merge(reason, 1, Integer::sum);
                }
                if (examples.size() < exampleLimit) {
                    examples.add(status);
                }
            }
            if (isOgerponStatus(status)) {
                ogerponStatuses.add(status);
            }
        }

        return new Summary(candidateCount, acceptedCount, excludedByReason, examples, ogerponStatuses);
    }

    private static boolean isOgerponStatus(AssetStatus status) {
        return Optional.ofNullable(status.speciesName())
                .map(name -> name.toLowerCase(Locale.ROOT).contains("ogerpon"))
                .orElse(false);
    }

    interface AssetStatusProvider {
        AssetStatus statusFor(Species species);
    }

    record AssetStatus(int internalIdentity, String speciesName, EnumSet<CfruDpeRandomPoolAssetIssue> issues) {
        AssetStatus {
            issues = issues == null ? EnumSet.noneOf(CfruDpeRandomPoolAssetIssue.class) : EnumSet.copyOf(issues);
        }

        boolean accepted() {
            return issues.isEmpty();
        }

        String primaryReasonLabel() {
            return issues.stream()
                    .findFirst()
                    .map(CfruDpeRandomPoolAssetIssue::getLabel)
                    .orElse("accepted");
        }

        String sanitizedLine() {
            String safeName = Objects.requireNonNullElse(speciesName, "<null>");
            return "internal=" + internalIdentity + " species=" + safeName + " reason=" + primaryReasonLabel();
        }
    }

    record Summary(int candidateCountBeforeGuard,
                   int acceptedCountAfterGuard,
                   EnumMap<CfruDpeRandomPoolAssetIssue, Integer> excludedByReason,
                   List<AssetStatus> examples,
                   List<AssetStatus> ogerponStatuses) {
        int excludedCount() {
            return candidateCountBeforeGuard - acceptedCountAfterGuard;
        }

        int countFor(CfruDpeRandomPoolAssetIssue reason) {
            return excludedByReason.getOrDefault(reason, 0);
        }
    }
}
