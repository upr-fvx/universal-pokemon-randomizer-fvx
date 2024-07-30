package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.MegaEvolution;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.gamedata.SpeciesSet;
import com.dabomstew.pkrandom.gamedata.cueh.BasicSpeciesAction;
import com.dabomstew.pkrandom.gamedata.cueh.EvolvedSpeciesAction;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.List;
import java.util.Random;

public class PokemonTypeRandomizer extends Randomizer {

    // "Get Secondary Type Chance"s
    private static final double GSTC_NO_EVO = 0.5;
    private static final double GSTC_HAS_EVO = 0.35;
    private static final double GSTC_MIDDLE_EVO = 0.15;
    private static final double GSTC_FINAL_EVO = 0.25;

    public PokemonTypeRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizePokemonTypes() {
        boolean evolutionSanity = settings.getTypesMod() == Settings.TypesMod.RANDOM_FOLLOW_EVOLUTIONS;
        boolean megaEvolutionSanity = settings.isTypesFollowMegaEvolutions();
        boolean dualTypeOnly = settings.isDualTypeOnly();

        BasicSpeciesAction<Species> basicAction = pk -> {
            pk.setPrimaryType(typeService.randomType(random));
            pk.setSecondaryType(null);
            double chance = pk.getEvolutionsFrom().size() == 1 ? GSTC_HAS_EVO : GSTC_NO_EVO;
            assignRandomSecondaryType(pk, chance, dualTypeOnly);
        };
        EvolvedSpeciesAction<Species> evolvedAction = (evFrom, evTo, toMonIsFinalEvo) -> {
            evTo.setPrimaryType(evFrom.getPrimaryType(false));
            evTo.setSecondaryType(evFrom.getSecondaryType(false));

            if (evTo.getSecondaryType(false) == null) {
                double chance = toMonIsFinalEvo ? GSTC_FINAL_EVO : GSTC_MIDDLE_EVO;
                assignRandomSecondaryType(evTo, chance, dualTypeOnly);
            }
        };
        BasicSpeciesAction<Species> noEvoAction = pk -> {
            pk.setPrimaryType(typeService.randomType(random));
            pk.setSecondaryType(null);
            assignRandomSecondaryType(pk, GSTC_NO_EVO, dualTypeOnly);
        };

        copyUpEvolutionsHelper.apply(evolutionSanity, false, basicAction, evolvedAction,
                null, noEvoAction);

        carryTypesToAltFormes();

        carryTypesToMegas(megaEvolutionSanity);
        changesMade = true;
    }

    private void carryTypesToAltFormes() {
        SpeciesSet allPokes = romHandler.getSpeciesSetInclFormes();
        for (Species pk : allPokes) {
            if (pk != null && pk.isActuallyCosmetic()) {
                pk.setPrimaryType(pk.getBaseForme().getPrimaryType(false));
                pk.setSecondaryType(pk.getBaseForme().getSecondaryType(false));
            }
        }
    }

    private void carryTypesToMegas(boolean megaEvolutionSanity) {
        if (megaEvolutionSanity) {
            List<MegaEvolution> allMegaEvos = romHandler.getMegaEvolutions();
            for (MegaEvolution megaEvo: allMegaEvos) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1) continue;
                megaEvo.getTo().setPrimaryType(megaEvo.getFrom().getPrimaryType(false));
                megaEvo.getTo().setSecondaryType(megaEvo.getFrom().getSecondaryType(false));

                if (megaEvo.getTo().getSecondaryType(false) == null) {
                    if (random.nextDouble() < 0.25) {
                        megaEvo.getTo().setSecondaryType(typeService.randomType(random));
                        while (megaEvo.getTo().getSecondaryType(false) == megaEvo.getTo().getPrimaryType(false)) {
                            megaEvo.getTo().setSecondaryType(typeService.randomType(random));
                        }
                    }
                }
            }
        }
    }

    private void assignRandomSecondaryType(Species pk, double chance, boolean dualTypeOnly) {
        if (random.nextDouble() < chance || dualTypeOnly) {
            pk.setSecondaryType(typeService.randomType(random));
            while (pk.getSecondaryType(false) == pk.getPrimaryType(false)) {
                pk.setSecondaryType(typeService.randomType(random));
            }
        }
    }
}
