package com.uprfvx.random.randomizers;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.constants.AbilityIDs;
import com.uprfvx.romio.constants.Gen3Constants;
import com.uprfvx.romio.constants.GlobalConstants;
import com.uprfvx.romio.gamedata.BattleStyle;
import com.uprfvx.romio.gamedata.cueh.BasicSpeciesAction;
import com.uprfvx.romio.gamedata.cueh.CopyUpEvolutionsHelper;
import com.uprfvx.romio.gamedata.cueh.EvolvedSpeciesAction;
import com.uprfvx.romio.romhandlers.RomHandler;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpeciesAbilityRandomizer extends Randomizer {

    public SpeciesAbilityRandomizer(RomHandler romHandler, SettingsManager settings, Random random) {
        super(romHandler, settings, random);
    }

    public void randomizeAbilities() {
        boolean evolutionSanity = settings.get(Settings.Name.SPECIES_ABILITIES_FOLLOW_EVOLUTIONS);
        boolean allowWonderGuard = !(boolean) settings.get(Settings.Name.SPECIES_ABILITIES_BAN_WONDER_GUARD);
        boolean banTrappingAbilities = settings.get(Settings.Name.SPECIES_ABILITIES_BAN_TRAPPING);
        boolean banNegativeAbilities = settings.get(Settings.Name.SPECIES_ABILITIES_BAN_NEGATIVE);
        boolean banBadAbilities = settings.get(Settings.Name.SPECIES_ABILITIES_BAN_MINOR);
        boolean megaEvolutionSanity = settings.get(Settings.Name.SPECIES_ABILITIES_FOLLOW_MEGA_EVOLUTIONS);
        boolean weighDuplicatesTogether = settings.get(Settings.Name.SPECIES_ABILITIES_COMBINE_DUPLICATES);
        boolean ensureTwoAbilities = settings.get(Settings.Name.SPECIES_ALWAYS_HAVE_TWO_ABILITIES);
        BattleStyle battleStyle = new BattleStyle(
                settings.get(Settings.Name.TRAINERS_RANDOMIZE_BATTLE_STYLE),
                settings.get(Settings.Name.TRAINERS_SINGLE_STYLE_SELECTION)
        );
        boolean isMultiBattleOnly = battleStyle.isOnlyMultiBattles();

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
        BasicSpeciesAction basicAction = pk -> {
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
        };
        EvolvedSpeciesAction evolvedAction = (evFrom, evTo, _) -> {
            if (evTo.getAbility1() != AbilityIDs.wonderGuard && evTo.getAbility2() != AbilityIDs.wonderGuard
                    && evTo.getAbility3() != AbilityIDs.wonderGuard) {
                evTo.copyBaseFormeAbilities(evFrom);
            }
        };

        CopyUpEvolutionsHelper.Options cuehOptions = new CopyUpEvolutionsHelper.Options
                .Builder(basicAction, evolvedAction)
                .cosmeticAction((baseForme, altForme) -> altForme.copyBaseFormeAbilities(baseForme))
                .evolutionSanity(evolutionSanity)
                .treatMegasAsEvos(megaEvolutionSanity)
                .build();
        copyUpEvolutionsHelper.apply(cuehOptions);

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
