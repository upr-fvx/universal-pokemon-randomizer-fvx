package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.cueh.AltFormeAction;
import com.uprfvx.romio.gamedata.cueh.BasicSpeciesAction;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.Random;

public class SpeciesTypeRandomizer extends Randomizer {

    // "Get Secondary Type Chance"s
    private static final double GSTC_NO_EVO = 0.5;
    private static final double GSTC_HAS_EVO = 0.35;
    private static final double GSTC_MIDDLE_EVO = 0.15;
    private static final double GSTC_FINAL_EVO = 0.25;
    private static final double GSTC_MEGA_EVO = 0.25;
    private static final double GSTC_ALT_FORME = 0.5;

    public SpeciesTypeRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeSpeciesTypes() {
        boolean evolutionSanity = settings.getSpeciesTypesMod() == Settings.SpeciesTypesMod.RANDOM_FOLLOW_EVOLUTIONS;
        boolean megaEvolutionSanity = settings.isTypesFollowMegaEvolutions();
        boolean dualTypeOnly = settings.isDualTypeOnly();

        BasicSpeciesAction basicAction = pk -> {
            pk.setPrimaryType(typeService.randomType(random));
            pk.setSecondaryType(null);
            double chance = pk.getEvolutionsFrom().size() == 1 ? GSTC_HAS_EVO : GSTC_NO_EVO;
            assignRandomSecondaryType(pk, chance, dualTypeOnly);
        };

        EvolvedSpeciesAction evolvedAction = (evFrom, evTo, toMonIsFinalEvo) -> {
            evTo.setPrimaryType(evFrom.getPrimaryType(false));
            evTo.setSecondaryType(evFrom.getSecondaryType(false));

            if (evTo.getSecondaryType(false) == null) {
                double chance = toMonIsFinalEvo ? GSTC_FINAL_EVO : GSTC_MIDDLE_EVO;
                if (evTo.isMegaEvolution()) {
                    chance = GSTC_MEGA_EVO;
                }
                assignRandomSecondaryType(evTo, chance, false);
            }
        };

        BasicSpeciesAction noEvoAction = pk -> {
            pk.setPrimaryType(typeService.randomType(random));
            pk.setSecondaryType(null);
            assignRandomSecondaryType(pk, GSTC_NO_EVO, dualTypeOnly);
        };

        AltFormeAction altFormeAction = (baseForme, altForme) -> {
            altForme.setPrimaryType(baseForme.getPrimaryType(false));
            altForme.setSecondaryType(baseForme.getSecondaryType(false));
            assignRandomSecondaryType(altForme, GSTC_ALT_FORME, false);
        };

        AltFormeAction cosmeticAction = (baseForme, altForme) -> {
            altForme.setPrimaryType(baseForme.getPrimaryType(false));
            altForme.setSecondaryType(baseForme.getSecondaryType(false));
        };

        CopyUpEvolutionsHelper.Options cuehOptions = new CopyUpEvolutionsHelper.Options
                .Builder(basicAction, evolvedAction)
                .noEvoAction(noEvoAction)
                .altFormeAction(altFormeAction)
                .cosmeticAction(cosmeticAction)
                .evolutionSanity(evolutionSanity)
                .treatMegasAsEvos(megaEvolutionSanity)
                .build();
        copyUpEvolutionsHelper.apply(cuehOptions);

        changesMade = true;
    }

    private void assignRandomSecondaryType(Species sp, double chance, boolean dualTypeOnly) {
        if (random.nextDouble() < chance || dualTypeOnly) {
            do {
                sp.setSecondaryType(typeService.randomType(random));
            } while (sp.getSecondaryType(false) == sp.getPrimaryType(false));
        }
    }
}
