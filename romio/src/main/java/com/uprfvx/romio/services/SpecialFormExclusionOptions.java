package com.uprfvx.romio.services;

public class SpecialFormExclusionOptions {

    private final boolean includeMegaForms;
    private final boolean includeGigantamaxForms;
    private final boolean allowRegionalFormsAcrossGenLimit;

    public SpecialFormExclusionOptions(boolean includeMegaForms, boolean includeGigantamaxForms,
                                       boolean allowRegionalFormsAcrossGenLimit) {
        this.includeMegaForms = includeMegaForms;
        this.includeGigantamaxForms = includeGigantamaxForms;
        this.allowRegionalFormsAcrossGenLimit = allowRegionalFormsAcrossGenLimit;
    }

    public static SpecialFormExclusionOptions defaults() {
        return new SpecialFormExclusionOptions(false, false, false);
    }

    public static SpecialFormExclusionOptions allowAllSpecialForms() {
        return new SpecialFormExclusionOptions(true, true, true);
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
}
