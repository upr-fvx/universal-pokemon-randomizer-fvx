package com.uprfvx.romio.services;

import com.uprfvx.romio.gamedata.GenRestrictions;
import com.uprfvx.romio.gamedata.Species;

public final class SpecialFormPredicates {

    private SpecialFormPredicates() {
    }

    public static boolean isSpeciesAllowed(Species species, GenRestrictions restrictions,
                                           SpecialFormExclusionOptions options) {
        SpecialFormExclusionOptions effectiveOptions = options == null ? SpecialFormExclusionOptions.defaults() : options;
        return hasUsableSpeciesIdentity(species)
                && isAllowedBySpecialFormOptions(species, effectiveOptions)
                && isAllowedByGeneration(species, restrictions, effectiveOptions);
    }

    public static boolean hasUsableSpeciesIdentity(Species species) {
        return species != null && species.getSpeciesSetIdentityNumber() > 0;
    }

    public static boolean isAllowedBySpecialFormOptions(Species species, SpecialFormExclusionOptions options) {
        if (species == null) {
            return false;
        }
        SpecialFormExclusionOptions effectiveOptions = options == null ? SpecialFormExclusionOptions.defaults() : options;
        if (species.isMegaForm() && !effectiveOptions.isIncludeMegaForms()) {
            return false;
        }
        if (species.isGigantamaxForm() && !effectiveOptions.isIncludeGigantamaxForms()) {
            return false;
        }
        if (species.isIrregularSpecialForm() && !effectiveOptions.isIncludeIrregularSpecialForms()) {
            return false;
        }
        return true;
    }

    public static boolean isAllowedByGeneration(Species species, GenRestrictions restrictions,
                                                SpecialFormExclusionOptions options) {
        if (species == null) {
            return false;
        }
        if (restrictions == null) {
            return true;
        }
        int generation = effectiveGenerationForDirectLimit(species, options);
        return generation > 0 && restrictions.isGenAllowed(generation);
    }

    public static boolean isAllowedAfterEvolutionaryRelativeExpansion(Species species, GenRestrictions restrictions,
                                                                      SpecialFormExclusionOptions options) {
        SpecialFormExclusionOptions effectiveOptions = options == null ? SpecialFormExclusionOptions.defaults() : options;
        if (!hasUsableSpeciesIdentity(species) || !isAllowedBySpecialFormOptions(species, effectiveOptions)) {
            return false;
        }
        return !species.dependsOnRegionalFormForEligibility()
                || isAllowedByGeneration(species, restrictions, effectiveOptions);
    }

    public static int effectiveGenerationForDirectLimit(Species species, SpecialFormExclusionOptions options) {
        if (species == null) {
            return -1;
        }
        SpecialFormExclusionOptions effectiveOptions = options == null ? SpecialFormExclusionOptions.defaults() : options;
        if (species.dependsOnRegionalFormForEligibility()) {
            if (effectiveOptions.isAllowRegionalFormsAcrossGenLimit()) {
                return species.getRegionalBaseFamilyGeneration();
            }
            return species.getRegionalFormGeneration();
        }
        return species.getGeneration();
    }
}
