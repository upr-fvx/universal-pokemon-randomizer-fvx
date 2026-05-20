package com.uprfvx.romio.services;

public class SpecialFormExclusionOptions {

    private final boolean includeMegaForms;
    private final boolean includeGigantamaxForms;
    private final boolean allowRegionalFormsAcrossGenLimit;
    private final boolean includeIrregularSpecialForms;

    public SpecialFormExclusionOptions(boolean includeMegaForms, boolean includeGigantamaxForms,
                                       boolean allowRegionalFormsAcrossGenLimit) {
        this(includeMegaForms, includeGigantamaxForms, allowRegionalFormsAcrossGenLimit, false);
    }

    public SpecialFormExclusionOptions(boolean includeMegaForms, boolean includeGigantamaxForms,
                                       boolean allowRegionalFormsAcrossGenLimit,
                                       boolean includeIrregularSpecialForms) {
        this.includeMegaForms = includeMegaForms;
        this.includeGigantamaxForms = includeGigantamaxForms;
        this.allowRegionalFormsAcrossGenLimit = allowRegionalFormsAcrossGenLimit;
        this.includeIrregularSpecialForms = includeIrregularSpecialForms;
    }

    public static SpecialFormExclusionOptions defaults() {
        return new SpecialFormExclusionOptions(false, false, false);
    }

    public static SpecialFormExclusionOptions allowAllSpecialForms() {
        return new SpecialFormExclusionOptions(true, true, true, true);
    }

    public boolean isIncludeMegaForms() {
        return includeMegaForms;
    }

    public boolean isIncludeGigantamaxForms() {
        return includeGigantamaxForms;
    }

    public boolean isAllowRegionalFormsAcrossGenLimit() {
        return allowRegionalFormsAcrossGenLimit;
    }

    public boolean isIncludeIrregularSpecialForms() {
        return includeIrregularSpecialForms;
    }
}
