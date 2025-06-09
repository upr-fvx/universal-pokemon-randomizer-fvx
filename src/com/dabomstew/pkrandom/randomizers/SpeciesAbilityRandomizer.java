package com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.constants.AbilityIDs;
import com.dabomstew.pkromio.constants.Gen3Constants;
import com.dabomstew.pkromio.constants.GlobalConstants;
import com.dabomstew.pkromio.gamedata.MegaEvolution;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpeciesAbilityRandomizer extends Randomizer {

    public SpeciesAbilityRandomizer(RomHandler romHandler, Settings settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeAbilities() {
        boolean evolutionSanity = settings.isAbilitiesFollowEvolutions();
        boolean allowWonderGuard = settings.isAllowWonderGuard();
        boolean banTrappingAbilities = settings.isBanTrappingAbilities();
        boolean banNegativeAbilities = settings.isBanNegativeAbilities();
        boolean banBadAbilities = settings.isBanBadAbilities();
        boolean megaEvolutionSanity = settings.isAbilitiesFollowMegaEvolutions();
        boolean weighDuplicatesTogether = settings.isWeighDuplicateAbilitiesTogether();
        boolean ensureTwoAbilities = settings.isEnsureTwoAbilities();
        boolean isMultiBattleOnly = settings.getBattleStyle().isOnlyMultiBattles();

        // Abilities don't exist in some games...
        if (romHandler.abilitiesPerSpecies() == 0) {
            return;
        }

        final boolean hasHiddenAbilities = (romHandler.abilitiesPerSpecies() == 3);

        final List<Integer> bannedAbilities = romHandler.getUselessAbilities();

        if (!allowWonderGuard) {
            bannedAbilities.add(AbilityIDs.wonderGuard);
        }

        if (banTrappingAbilities) {
            bannedAbilities.addAll(GlobalConstants.battleTrappingAbilities);
        }

        if (banNegativeAbilities) {
            bannedAbilities.addAll(GlobalConstants.negativeAbilities);
        }

        if (banBadAbilities) {
            bannedAbilities.addAll(GlobalConstants.badAbilities);
            if (!isMultiBattleOnly) {
                bannedAbilities.addAll(GlobalConstants.doubleBattleAbilities);
            }
        }

        if (weighDuplicatesTogether) {
            bannedAbilities.addAll(GlobalConstants.duplicateAbilities);
            if (romHandler.generationOfPokemon() == 3) {
                bannedAbilities.add(Gen3Constants.airLockIndex); // Special case for Air Lock in Gen 3
            }
        }

        final int maxAbility = romHandler.highestAbilityIndex();

        // copy abilities straight up evolution lines
        // still keep WG as an exception, though
        copyUpEvolutionsHelper.apply(evolutionSanity, false, pk -> {
            if (pk.getAbility1() != AbilityIDs.wonderGuard && pk.getAbility2() != AbilityIDs.wonderGuard
                    && pk.getAbility3() != AbilityIDs.wonderGuard) {
                // Pick first ability
                pk.setAbility1(pickRandomAbility(maxAbility, bannedAbilities, weighDuplicatesTogether));

                // Second ability?
                if (ensureTwoAbilities || random.nextDouble() < 0.5) {
                    // Yes, second ability
                    pk.setAbility2(pickRandomAbility(maxAbility, bannedAbilities, weighDuplicatesTogether,
                            pk.getAbility1()));
                } else {
                    // Nope
                    pk.setAbility2(0);
                }

                // Third ability?
                if (hasHiddenAbilities) {
                    pk.setAbility3(pickRandomAbility(maxAbility, bannedAbilities, weighDuplicatesTogether,
                            pk.getAbility1(), pk.getAbility2()));
                }
            }
        }, (evFrom, evTo, toMonIsFinalEvo) -> {
            if (evTo.getAbility1() != AbilityIDs.wonderGuard && evTo.getAbility2() != AbilityIDs.wonderGuard
                    && evTo.getAbility3() != AbilityIDs.wonderGuard) {
                evTo.setAbility1(evFrom.getAbility1());
                evTo.setAbility2(evFrom.getAbility2());
                evTo.setAbility3(evFrom.getAbility3());
            }
        });


        romHandler.getSpeciesSetInclFormes().filter(Species::isActuallyCosmetic)
                .forEach(pk -> pk.copyBaseFormeAbilities(pk.getBaseForme()));

        if (megaEvolutionSanity) {
            for (MegaEvolution megaEvo : romHandler.getMegaEvolutions()) {
                if (megaEvo.getFrom().getMegaEvolutionsFrom().size() > 1)
                    continue;
                megaEvo.getTo().setAbility1(megaEvo.getFrom().getAbility1());
                megaEvo.getTo().setAbility2(megaEvo.getFrom().getAbility2());
                megaEvo.getTo().setAbility3(megaEvo.getFrom().getAbility3());
            }
        }

        changesMade = true;
    }

    private int pickRandomAbilityVariation(int selectedAbility, int... alreadySetAbilities) {
        int newAbility = selectedAbility;

        while (true) {
            Map<Integer, List<Integer>> abilityVariations = romHandler.getAbilityVariations();
            for (int baseAbility: abilityVariations.keySet()) {
                if (selectedAbility == baseAbility) {
                    List<Integer> variationsForThisAbility = abilityVariations.get(selectedAbility);
                    newAbility = variationsForThisAbility.get(random.nextInt(variationsForThisAbility.size()));
                    break;
                }
            }

            boolean repeat = false;
            for (int alreadySetAbility : alreadySetAbilities) {
                if (alreadySetAbility == newAbility) {
                    repeat = true;
                    break;
                }
            }

            if (!repeat) {
                break;
            }
        }

        return newAbility;
    }

    private int pickRandomAbility(int maxAbility, List<Integer> bannedAbilities, boolean useVariations,
                                  int... alreadySetAbilities) {
        int newAbility;

        while (true) {
            newAbility = random.nextInt(maxAbility) + 1;

            if (bannedAbilities.contains(newAbility)) {
                continue;
            }

            boolean repeat = false;
            for (int alreadySetAbility : alreadySetAbilities) {
                if (alreadySetAbility == newAbility) {
                    repeat = true;
                    break;
                }
            }

            if (!repeat) {
                if (useVariations) {
                    newAbility = pickRandomAbilityVariation(newAbility, alreadySetAbilities);
                }
                break;
            }
        }

        return newAbility;
    }
}
