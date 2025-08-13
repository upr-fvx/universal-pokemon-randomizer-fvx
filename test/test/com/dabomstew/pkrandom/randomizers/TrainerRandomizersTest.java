package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.SpeciesTypeRandomizer;
import com.dabomstew.pkrandom.randomizers.StarterRandomizer;
import com.dabomstew.pkrandom.randomizers.TrainerMovesetRandomizer;
import com.dabomstew.pkrandom.randomizers.TrainerPokemonRandomizer;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.Gen7Constants;
import com.dabomstew.pkromio.gamedata.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class TrainerRandomizersTest extends RandomizerTest {

    private static final double UBIQUITOUS_MOVE_RATE = 0.20;

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersHaveAtLeastTwoPokemonAfterSettingDoubleBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.DOUBLE_BATTLE));
        new TrainerPokemonRandomizer(romHandler, settings, RND).modifyBattleStyle();
        for (Trainer trainer : romHandler.getTrainers()) {
            System.out.println(trainer);
            if (trainer.forcedDoubleBattle) {
                assertTrue(trainer.pokemon.size() >= 2);
            } else {
                System.out.println("Not a forced double battle.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersHaveAtLeastThreePokemonAfterSettingTripleBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 5 || getGenerationNumberOf(romName) == 6);
        activateRomHandler(romName);
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.TRIPLE_BATTLE));
        new TrainerPokemonRandomizer(romHandler, settings, RND).modifyBattleStyle();
        for (Trainer trainer : romHandler.getTrainers()) {
            System.out.println(trainer);
            if (trainer.forcedDoubleBattle) {
                assertTrue(trainer.pokemon.size() >= 3);
            } else {
                System.out.println("Not a forced triple battle.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersHaveAtLeastThreePokemonAfterSettingRotationBattleMode(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 5 || getGenerationNumberOf(romName) == 6);
        activateRomHandler(romName);
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.SINGLE_STYLE, BattleStyle.Style.ROTATION_BATTLE));
        new TrainerPokemonRandomizer(romHandler, settings, RND).modifyBattleStyle();
        for (Trainer trainer : romHandler.getTrainers()) {
            System.out.println(trainer);
            if (trainer.forcedDoubleBattle) {
                assertTrue(trainer.pokemon.size() >= 3);
            } else {
                System.out.println("Not a forced rotation battle.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void trainersHaveEnoughPokemonForBattleStyle(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);
        Settings settings = new Settings();
        settings.setBattleStyle(new BattleStyle(BattleStyle.Modification.RANDOM, BattleStyle.Style.SINGLE_BATTLE));
        new TrainerPokemonRandomizer(romHandler, settings, RND).modifyBattleStyle();
        for (Trainer trainer : romHandler.getTrainers()) {
            System.out.println(trainer);
            if (trainer.forcedDoubleBattle) {
                switch (trainer.currBattleStyle.getStyle()) {
                    case SINGLE_BATTLE:
                        assertFalse(trainer.pokemon.isEmpty());
                        break;
                    case DOUBLE_BATTLE:
                        assertTrue(trainer.pokemon.size() >= 2);
                        break;
                    case TRIPLE_BATTLE:
                    case ROTATION_BATTLE:
                        assertTrue(trainer.pokemon.size() >= 3);
                        break;
                }
            } else {
                System.out.println("Not a forced rotation battle.");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedWorks(String romName) {
        activateRomHandler(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.KEEP_THEMED);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedWorksWithForcedEvolutions(String romName) {
        activateRomHandler(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.KEEP_THEMED);
        s.setTrainersForceMiddleStage(true);
        s.setTrainersForceMiddleStageLevel(1);
        s.setTrainersForceFullyEvolved(true);
        s.setTrainersForceFullyEvolvedLevel(20);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers, false);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedOrPrimaryWorks(String romName) {
        activateRomHandler(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        Map<Trainer, List<Species>> nonTypeThemedTrainers;
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);
        nonTypeThemedTrainers = recordTrainerPokemon();
        typeThemedTrainers.keySet().forEach(nonTypeThemedTrainers::remove);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.KEEP_THEME_OR_PRIMARY);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemeOrPrimaryCheck(beforeTrainerStrings, typeThemedTrainers, nonTypeThemedTrainers, false);
    }

    /**
     * Checks that trainers which had themes before randomizing still have them.
     * If nonTypeThemedTrainers is not null, also checks if trainers that did not have themes
     * have preserved the primary type of their Pokemon.
     */
    private void keepTypeThemeOrPrimaryCheck(Map<Trainer, List<String>> beforeTrainerStrings,
                                             Map<Trainer, Type> typeThemedTrainers,
                                             Map<Trainer, List<Species>> nonTypeThemedTrainers,
                                             boolean checkOnlyAdded) {

        for (Trainer tr : romHandler.getTrainers()) {
            List<String> beforeStrings = beforeTrainerStrings.get(tr);
            System.out.println("Before: " + beforeStrings.get(0));
            for (int i = 1; i < beforeStrings.size(); i++) {
                System.out.println("\t" + beforeStrings.get(i));
            }

            if (typeThemedTrainers.containsKey(tr)) {
                Type theme = typeThemedTrainers.get(tr);
                System.out.println("Type Theme: " + theme);
                System.out.println("After: " + tr);
                for (TrainerPokemon tp : tr.pokemon) {
                    Species sp = romHandler.getAltFormeOfSpecies(tp.getSpecies(), tp.getForme());
                    System.out.println("\t" + sp);
                    boolean keepsTheme = sp.getPrimaryType(false) == theme || sp.getSecondaryType(false) == theme;
                    if (!keepsTheme) {
                        if (checkOnlyAdded && !tp.isAddedTeamMember()) {
                            System.out.println("\t" + sp.getFullName() + " is not a " + theme + "-type, " +
                                    "but it is fine since it is an original team member.");
                        } else {
                            fail(sp.getFullName() + " is not a " + theme + "-type.");
                        }
                    }
                }
            } else {
                System.out.println("Not Type Themed");
                if(nonTypeThemedTrainers != null) {
                    System.out.println("After: " + tr);

                    List<Species> before = nonTypeThemedTrainers.get(tr);
                    List<TrainerPokemon> after = tr.pokemon;

                    if (before.size() < after.size()) {
                        throw new IllegalStateException("Trainer removed Pokemon!");
                    }

                    for (int i = 0; i < before.size(); i++) {
                        Species beforePoke = before.get(i);
                        Species afterPoke = after.get(i).getSpecies();

                        System.out.println("\t\tBefore: " + beforePoke.getName() + "; Primary type: "
                                + beforePoke.getPrimaryType(true).name());
                        System.out.println("\t\tAfter: " + afterPoke.getName() + "; Types: "
                                + afterPoke.getPrimaryType(false).name()
                                + (afterPoke.hasSecondaryType(false) ?
                                ", " + afterPoke.getSecondaryType(false) : ""));

                        assertTrue(afterPoke.hasType(beforePoke.getPrimaryType(true), false));
                    }
                }
            }
            System.out.println();
        }
    }

    /**
     * Records the species (in order) of the Pokemon for every trainer in the romHandler.
     * @return A Map of every trainer to a list of their Pokemon's species.
     */
    private Map<Trainer, List<Species>> recordTrainerPokemon() {
        Map<Trainer, List<Species>> trainersWithPokemonTypes = new HashMap<>();
        for(Trainer trainer : romHandler.getTrainers()) {
            List<Species> speciesPrimaryTypes = new ArrayList<>();
            for (TrainerPokemon tp : trainer.pokemon) {
                speciesPrimaryTypes.add(romHandler.getAltFormeOfSpecies(tp.getSpecies(), tp.getForme()));
            }
            trainersWithPokemonTypes.put(trainer, speciesPrimaryTypes);
        }

        return trainersWithPokemonTypes;
    }

    /**
     * Records original species names of all trainer Pokemon for every trainer in the romHandler.
     * @return A Map of every trainer to a list of their Pokemon's species.
     */
    private Map<Trainer, List<String>> recordTrainerPokemonSpeciesNames() {
        Map<Trainer, List<String>> originalNames = new HashMap<>();

        for (Trainer tr : romHandler.getTrainers()) {
            List<String> namesBefore = new ArrayList<>();
            originalNames.put(tr, namesBefore);
            for (TrainerPokemon tp : tr.pokemon) {
                namesBefore.add(tp.getSpecies().getName());
            }
        }

        return originalNames;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void keepTypeThemedWorksWithRandomSpeciesTypes(String romName) {
        activateRomHandler(romName);

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        Settings s = new Settings();
        s.setSpeciesTypesMod(false, false, true);
        new SpeciesTypeRandomizer(romHandler, s, RND).randomizeSpeciesTypes();
        s.setTrainersMod(false, false, false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers, false);
    }

    private void recordTypeThemeBefore(Map<Trainer, List<String>> beforeTrainerStrings, Map<Trainer, Type> typeThemedTrainers) {
        Map <String, Type> gymTypeMap = romHandler.getGymAndEliteTypeThemes();

        for (Trainer tr : romHandler.getTrainers()) {
            List<String> beforeStrings = new ArrayList<>();
            beforeTrainerStrings.put(tr, beforeStrings);
            beforeStrings.add(tr.toString());
            for (TrainerPokemon tp : tr.pokemon) {
                beforeStrings.add(tp.getSpecies().toString());
            }

            String gymTag = tr.tag;
            if(gymTag != null) {
                gymTag = gymTag.split("-")[0];

                //special Giovanni case
                if(gymTag.contains("GIO")) {
                    gymTag = "GYM8";
                }
            }

            Type theme = getThemedTrainerType(tr);
            if(gymTypeMap.containsKey(gymTag)) {
                beforeStrings.add("Forced theme from " + gymTag);
                theme = gymTypeMap.get(gymTag);
            }
            if (theme != null) {
                typeThemedTrainers.put(tr, theme);
            }
        }
    }

    private Type getThemedTrainerType(Trainer tr) {
        Species first = tr.pokemon.get(0).getSpecies();
        Type primary = first.getPrimaryType(true);
        Type secondary = first.getSecondaryType(true);
        for (int i = 1; i < tr.pokemon.size(); i++) {
            Species pk = tr.pokemon.get(i).getSpecies();
            if (secondary != null) {
                if (secondary != pk.getPrimaryType(true) && secondary != pk.getSecondaryType(true)) {
                    secondary = null;
                }
            }
            if (primary != pk.getPrimaryType(true) && primary != pk.getSecondaryType(true)) {
                primary = secondary;
                secondary = null;
            }
            if (primary == null) {
                return null; //no type is shared, no need to look at the remaining pokémon
            }
        }

        //we have a type theme!
        if (primary == Type.NORMAL && secondary != null) {
            //Bird override
            //(Normal is less significant than other types, for example, Flying)
            return secondary;
        } else {
            return primary;
        }

    }

    private void keepTypeThemedCheck(Map<Trainer, List<String>> beforeTrainerStrings, Map<Trainer, Type> typeThemedTrainers,
                                     boolean checkOnlyAdded) {
        keepTypeThemeOrPrimaryCheck(beforeTrainerStrings, typeThemedTrainers, null, checkOnlyAdded);
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void useLocalPokemonGuaranteesLocalPokemonOnly(String romName) {
        activateRomHandler(romName);
        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setTrainersUseLocalPokemon(true);
        s.setUseTimeBasedEncounters(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        SpeciesSet localWithRelatives = romHandler.getMainGameWildPokemonSpecies(s.isUseTimeBasedEncounters())
                        .buildFullFamilies(false);
        SpeciesSet all = romHandler.getSpeciesSet();
        SpeciesSet nonLocal = new SpeciesSet(all);
        nonLocal.removeAll(localWithRelatives);

        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);

            for (TrainerPokemon tp : tr.pokemon) {
                System.out.println(tp.getSpecies());
                assertTrue(localWithRelatives.contains(tp.getSpecies()));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void useLocalPokemonAndE4UniquePokesGivesE4NonLocalPokemon(String romName) {
        int wantedNonLocal = 1; // you can play around with this value between 1-6 but what's important is it works for 1

        activateRomHandler(romName);
        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(wantedNonLocal);
        s.setUseTimeBasedEncounters(true); // should be at least 4 non-local Pokemon in each game
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        SpeciesSet localWithRelatives = romHandler.getMainGameWildPokemonSpecies(s.isUseTimeBasedEncounters())
                .buildFullFamilies(false);
        SpeciesSet all = romHandler.getSpeciesSet();
        SpeciesSet nonLocal = new SpeciesSet(all);
        nonLocal.removeAll(localWithRelatives);

        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(false);
        assumeTrue(!eliteFourIndices.isEmpty());
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println("\n" + tr);

            if (eliteFourIndices.contains(tr.index)) {
                System.out.println("-E4 Member-");
                System.out.println("Non-local: " + nonLocal.stream().map(Species::getName).collect(Collectors.toList()));
                System.out.println("Local: " + localWithRelatives.stream().map(Species::getName).collect(Collectors.toList()));
                int nonLocalCount = 0;
                for (TrainerPokemon tp : tr.pokemon) {
                    if (nonLocal.contains(tp.getSpecies())) {
                        nonLocalCount++;
                        System.out.println(tp.getSpecies().getName() + " is non-local");
                    }
                }
                assertTrue(nonLocalCount == wantedNonLocal || nonLocalCount == tr.pokemon.size());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainers(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(false, true, false, false, false);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainersWithUseLocal(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setUseTimeBasedEncounters(true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void uniqueElite4PokemonGivesUniquePokemonToSaidTrainersWithTypeThemes(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        //s.setTrainersUseLocalPokemon(true);
        s.setEliteFourUniquePokemonNumber(1);
        s.setTrainersMod(false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        elite4UniquePokemonCheck();
    }

    private void elite4UniquePokemonCheck() {
        // This sometimes fails for Gen 1, since the final rival battle(s)
        // are included in RomHandler.getEliteFourTrainers(), but are not tagged
        // as CHAMPION.
        // Just tagging them as such would be an easy fix, but causes other issues,
        // that require design choices. Related to the champion/rival being forced
        // to have a Type theme, and there being 3 whole champion trainers
        // (all other games only have 1).
        // TODO: make sense of these design choices, and fix this bug.

        List<Trainer> trainers = romHandler.getTrainers();
        int[] pokeCount = new int[romHandler.getSpecies().size()];
        for (Trainer tr : trainers) {
            System.out.println(tr);
            for (TrainerPokemon tp : tr.pokemon) {
                Species pk = tp.getSpecies();
                pokeCount[pk.getNumber()]++;
            }
        }

        List<Species> allPokes = romHandler.getSpecies();
        for (int i = 1; i < allPokes.size(); i++) {
            System.out.println(allPokes.get(i).getName() + " : " + pokeCount[i]);
        }

        List<Integer> eliteFourIndices = romHandler.getEliteFourTrainers(false);
        assumeTrue(!eliteFourIndices.isEmpty());
        for (Trainer tr : romHandler.getTrainers()) {
            if (eliteFourIndices.contains(tr.index)) {
                System.out.println(tr);
                int minCount = Integer.MAX_VALUE;
                for (TrainerPokemon tp : tr.pokemon) {
                    Species pk = tp.getSpecies();
                    System.out.println(pk.getName() + ":" + pokeCount[pk.getNumber()]);
                    minCount = Math.min(minCount, pokeCount[pk.getNumber()]);
                }
                assertEquals(1, minCount);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void elite4MembersWithMultipleBattlesGetSameTypeThemeForAll(String romName) {
        //with random type themes
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setTrainersMod(false, false, false, false, true);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        Map<String, List<Type>> e4Types = new HashMap<>();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.tag != null && tr.tag.contains("ELITE")) {
                String memberTag = tr.tag.split("-")[0];
                if (!e4Types.containsKey(memberTag)) {
                    e4Types.put(memberTag, new ArrayList<>());
                }
                e4Types.get(memberTag).add(getThemedTrainerType(tr));
                //TODO: add handling for double-theme battles (e.g. all pokemon are Fire-Fighting)
            }
        }

        System.out.println(e4Types);
        for (Map.Entry<String, List<Type>> entry : e4Types.entrySet()) {
            Set<Type> uniques = new HashSet<>(entry.getValue());
            System.out.println(entry.getKey() + " has " + uniques.size() + " type theme(s)");
            assertEquals(1, uniques.size());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomUsableZCrystalsDoesNotChangeWhichPokemonHaveZCrystals(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 7);
        activateRomHandler(romName);

        System.out.println("==== BEFORE: ====");
        Map<Integer, boolean[]> before = findZCrystals();
        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).randomUsableZCrystals();
        System.out.println("==== AFTER: ====");
        Map<Integer, boolean[]> after = findZCrystals();

        assertEquals(before.keySet(), after.keySet());
        for (Integer key : before.keySet()) {
            assertArrayEquals(before.get(key), after.get(key));
        }
    }

    private Map<Integer, boolean[]> findZCrystals() {

        Map<Integer, boolean[]> zCrystalsByTrainer = new HashMap<>();
        List<Trainer> trainersBefore = romHandler.getTrainers();
        for (int i = 0; i < trainersBefore.size(); i++) {
            Trainer tr = trainersBefore.get(i);
            System.out.println(tr);
            boolean[] zCrystals = new boolean[tr.pokemon.size()];
            boolean anyHasZCrystal = false;
            for (int pkNum = 0; pkNum < tr.pokemon.size(); pkNum++) {
                TrainerPokemon tp = tr.pokemon.get(pkNum);
                System.out.println(tp.getHeldItem());
                if (tp.getHeldItem() != null && Gen7Constants.heldZCrystalsByType.containsValue(tp.getHeldItem().getId())) {
                    zCrystals[pkNum] = true;
                    anyHasZCrystal = true;
                }
            }
            if (anyHasZCrystal) {
                zCrystalsByTrainer.put(i, zCrystals);
            }
        }
        return zCrystalsByTrainer;
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomUsableZCrystalsGivesZCrystalsOfUsableType(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 7);
        activateRomHandler(romName);

        new TrainerPokemonRandomizer(romHandler, new Settings(), RND).randomUsableZCrystals();
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);
            for (TrainerPokemon tp : tr.pokemon) {
                if (tp.getHeldItem() != null && Gen7Constants.heldZCrystalsByType.containsValue(tp.getHeldItem().getId())) {
                    System.out.println(tp.getSpecies().getName() + " holds " + tp.getHeldItem());

                    int[] pkMoves = tp.isResetMoves() ?
                            RomFunctions.getMovesAtLevel(tp.getSpecies().getNumber(), romHandler.getMovesLearnt(), tp.getLevel())
                            : Arrays.stream(tp.getMoves()).distinct().filter(mv -> mv != 0).toArray();
                    Set<Type> moveTypes = new HashSet<>();
                    for (int moveID : pkMoves) {
                        Move mv = romHandler.getMoves().get(moveID);
                        System.out.println(mv.name + " | " + mv.type);
                        moveTypes.add(mv.type);
                    }

                    boolean anyMoveTypeCorrect = false;
                    for (Type t : moveTypes) {
                        if (Gen7Constants.heldZCrystalsByType.get(t) == tp.getHeldItem().getId()) {
                            anyMoveTypeCorrect = true;
                        }
                    }
                    assertTrue(anyMoveTypeCorrect);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeDiverseTrainersWorks(String romName) {
        activateRomHandler(romName);

        Settings settings = new Settings();
        settings.setTrainersMod(Settings.TrainersMod.RANDOM);
        settings.setDiverseTypesForRegularTrainers(true);
        settings.setDiverseTypesForImportantTrainers(true);
        settings.setDiverseTypesForBossTrainers(true);

        new TrainerPokemonRandomizer(romHandler, settings, RND).randomizeTrainerPokes();

        checkTrainerTypesAreDiverse(romHandler.getTrainers());
    }

    private void checkTrainerTypesAreDiverse(List<Trainer> trainers) {
        for(Trainer trainer : trainers) {
            Set<Type> usedTypes = EnumSet.noneOf(Type.class);
            System.out.println(trainer.fullDisplayName);

            for(TrainerPokemon tp : trainer.pokemon) {
                Species sp = tp.getSpecies();
                if(tp.getForme() != 0) {
                    sp = romHandler.getAltFormeOfSpecies(sp, tp.getForme());
                }

                Type primaryType = sp.getPrimaryType(false);
                Type secondaryType = sp.getSecondaryType(false);

                System.out.println("\t" + sp.getFullName() + ": " + primaryType +
                        (secondaryType == null ? "" : "/" + secondaryType));

                if(usedTypes.contains(primaryType)) {
                    fail("Type " + primaryType + " already used by this trainer!");
                }
                usedTypes.add(primaryType);

                if(secondaryType != null) {
                    if (usedTypes.contains(secondaryType)) {
                        fail("Type " + secondaryType + " already used by this trainer!");
                    }
                    usedTypes.add(secondaryType);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addedPokemonKeepTypeTheme(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());

        Map<Trainer, List<String>> beforeTrainerStrings = new HashMap<>();
        Map<Trainer, Type> typeThemedTrainers = new HashMap<>();
        recordTypeThemeBefore(beforeTrainerStrings, typeThemedTrainers);

        addPossibleTrainerPokemon();

        Settings s = new Settings(); //TrainersMod == UNCHANGED
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        keepTypeThemedCheck(beforeTrainerStrings, typeThemedTrainers, true);
    }

    private void addPossibleTrainerPokemon() {
        Settings s = new Settings();
        if (romHandler.canAddPokemonToBossTrainers()) {
            s.setAdditionalBossTrainerPokemon(6);
        }
        if (romHandler.canAddPokemonToImportantTrainers()) {
            s.setAdditionalImportantTrainerPokemon(6);
        }
        if (romHandler.canAddPokemonToRegularTrainers()) {
            s.setAdditionalRegularTrainerPokemon(6);
        }
        new TrainerPokemonRandomizer(romHandler, s, RND).addTrainerPokemon();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void addedPokemonDoNotExceedLevelOfAce(String romName) {
        activateRomHandler(romName);
        assumeTrue(romHandler.canAddPokemonToBossTrainers());

        addPossibleTrainerPokemon();

        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println(tr);

            boolean duplicateHighestLevel = false;
            TrainerPokemon ace = new TrainerPokemon();
            ace.setLevel(0);

            for (TrainerPokemon tp : tr.pokemon) {
                System.out.println(tp + " added=" + tp.isAddedTeamMember());
                if (tp.getLevel() == ace.getLevel()) {
                    duplicateHighestLevel = true;
                } else if (tp.getLevel() > ace.getLevel()) {
                    ace = tp;
                    duplicateHighestLevel = false;
                }
            }

            assertTrue(duplicateHighestLevel || !ace.isAddedTeamMember());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void nonForcefullyEvolvedPokemonAreCorrect(String romName) {
        activateRomHandler(romName);

        // Record original species of all trainer Pokemon (for better console output only)
        Map<Trainer, List<String>> originalNames = recordTrainerPokemonSpeciesNames();

        // Randomize
        Settings s = new Settings();
        s.setTrainersForceMiddleStage(true);
        s.setTrainersForceMiddleStageLevel(1);
        s.setTrainersForceFullyEvolved(true);
        s.setTrainersForceFullyEvolvedLevel(20);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        // Test
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println("\n" + tr);
            for (int k = 0; k<tr.pokemon.size(); k++) {
                TrainerPokemon tp = tr.pokemon.get(k);
                System.out.println(originalNames.get(tr).get(k) + "-->" + tp.getSpecies().getName());
                if (tp.getLevel()<20) {
                    // Everything below level 20 cannot be a basic Pokemon with two evolution stages
                    assertFalse(tp.getSpecies().isBasicPokemonWithMoreThanTwoEvoStages(false));
                }
                else {
                    // Everything over level 20 has to be fully evolved
                    assertTrue(tp.getSpecies().getEvolvedSpecies(false).isEmpty());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomTrainerPokemon_TrainersTaggedRival1AndFriend1CanCarryOnlyCorrectStarter(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setRivalCarriesStarterThroughout(true);

        TrainerPokemonRandomizer tpr = new TrainerPokemonRandomizer(romHandler, s, RND);
        tpr.makeRivalCarryStarter();
        tpr.randomizeTrainerPokes();

        carryStarterCheck();
    }

    private void carryStarterCheck() {
        List<Species> starters = romHandler.getStarters();
        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.tag != null && (tr.tag.contains("RIVAL1-") || tr.tag.contains("FRIEND1-"))) {
                System.out.println(tr);

                int variant = Integer.parseInt(tr.tag.split("-")[1]);
                int offset = tr.tag.contains("RIVAL") ? 1 : 2;
                Species expected = starters.get((variant + offset) % 3);
                Species actual = tr.pokemon.get(0).getSpecies();

                assertEquals(1, tr.pokemon.size());
                assertEquals(expected, actual);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void unchangedTrainerPokemonRandomStarters_TrainersTaggedRivalAndFriendCanCarryStarter(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setStartersMod(Settings.StartersMod.RANDOM_BASIC);
        s.setRivalCarriesStarterThroughout(true);

        new StarterRandomizer(romHandler, s, RND).randomizeStarters();
        TrainerPokemonRandomizer tpr = new TrainerPokemonRandomizer(romHandler, s, RND);
        tpr.makeRivalCarryStarter();

        carryStarterFamiliesCheck();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomTrainerPokemon_TrainersTaggedRivalAndFriendCanCarryStarter(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setTrainersMod(Settings.TrainersMod.RANDOM);
        s.setRivalCarriesStarterThroughout(true);

        TrainerPokemonRandomizer tpr = new TrainerPokemonRandomizer(romHandler, s, RND);
        tpr.makeRivalCarryStarter();
        tpr.randomizeTrainerPokes();

        carryStarterFamiliesCheck();
    }

    private void carryStarterFamiliesCheck() {
        List<SpeciesSet> starterFamilies = romHandler.getStarters()
                .stream().map(sp -> sp.getFamily(false))
                .collect(Collectors.toList());

        for (Trainer tr : romHandler.getTrainers()) {
            if (tr.tag != null && (tr.tag.contains("RIVAL") || tr.tag.contains("FRIEND"))) {
                System.out.println(tr);
                int variant = Integer.parseInt(tr.tag.split("-")[1]);
                if (romHandler.isYellow()) {
                    // Yellow uses the variant syntax, to instead refer to alternate battles,
                    // with different evos of the original starter.
                    // Here it doesn't matter though, so clear out variant.
                    variant = 0;
                }

                int offset = tr.tag.contains("RIVAL") ? 1 : 2;
                SpeciesSet expectedFamily = starterFamilies.get((variant + offset) % 3);

                boolean carriesStarter = false;
                for (TrainerPokemon tp : tr.pokemon) {
                    if (expectedFamily.contains(tp.getSpecies())) {
                        carriesStarter = true;
                        break;
                    }
                }

                assertTrue(carriesStarter);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void forcedEvolutionsDoNotResetMoves(String romName) {
        activateRomHandler(romName);

        // Record original species of all trainer Pokemon (for better console output only)
        Map<Trainer, List<String>> originalNames = recordTrainerPokemonSpeciesNames();

        // Randomize
        Settings s = new Settings();
        s.setTrainersForceMiddleStage(true);
        s.setTrainersForceMiddleStageLevel(1);
        s.setTrainersForceFullyEvolved(true);
        s.setTrainersForceFullyEvolvedLevel(20);
        new TrainerPokemonRandomizer(romHandler, s, RND).randomizeTrainerPokes();

        // Test
        for (Trainer tr : romHandler.getTrainers()) {
            System.out.println("\n" + tr);
            for (int k = 0; k<tr.pokemon.size(); k++) {
                TrainerPokemon tp = tr.pokemon.get(k);
                System.out.println(originalNames.get(tr).get(k) + "-->" + tp.getSpecies().getName() +
                        ": resetMoves = " + tp.isResetMoves());
                assertFalse(tp.isResetMoves());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void betterMovesetsDoesNotCauseUbiquitousMove(String romName) {
        assumeTrue(getGenerationNumberOf(romName) >= 3);
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setBetterTrainerMovesets(true);
        new TrainerMovesetRandomizer(romHandler, s, RND).randomizeTrainerMovesets();

        Map<Integer, Integer> moveCounts = new TreeMap<>();
        int tpCount = 0;
        for (Trainer tr : romHandler.getTrainers()) {
            for (TrainerPokemon tp : tr.pokemon) {
                tpCount++;
                for (int moveID : tp.getMoves()) {
                    if (moveID != 0) {
                        moveCounts.put(moveID, moveCounts.getOrDefault(moveID, 0) + 1);
                    }
                }
            }
        }

        List<Move> allMoves = romHandler.getMoves();

        Map<Move, Double> ubiquitous = new HashMap<>();
        int finalTpCount = tpCount;
        moveCounts.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(
                e -> {
                    Move m = allMoves.get(e.getKey());
                    double rate = (double) e.getValue() / (double) finalTpCount;
                    System.out.printf("%.4f\t%s%n", rate, m.name);
                    if (rate >= UBIQUITOUS_MOVE_RATE) {
                        ubiquitous.put(m, rate);
                    }
                }
        );

        System.out.println(ubiquitous);
        assertTrue(ubiquitous.isEmpty());
    }
}
