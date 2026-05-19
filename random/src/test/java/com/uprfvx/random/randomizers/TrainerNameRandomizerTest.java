package com.uprfvx.random.randomizers;

import com.uprfvx.random.Settings;
import com.uprfvx.random.customnames.CustomNamesSet;
import com.uprfvx.romio.gamedata.Evolution;
import com.uprfvx.romio.gamedata.EvolutionType;
import com.uprfvx.romio.gamedata.Species;
import com.uprfvx.romio.gamedata.SpeciesSet;
import com.uprfvx.romio.gamedata.Trainer;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.services.RestrictedSpeciesService;
import com.uprfvx.romio.services.TypeService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Non-ROM decision coverage only for FVX-FOE-013 Trainer Names/Class Names.
 * These tests do not prove Gen3 writer, reload, or text-encoding safety.
 */
class TrainerNameRandomizerDecisions {

    @Test
    void trainerNameRandomizerNoOpWhenTrainerTextCannotChange() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.canChangeTrainerText = false;
        handler.trainerNames = List.of("ALICE");

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings(), new Random(1));
        randomizer.randomizeTrainerNames();
        randomizer.randomizeTrainerClassNames();

        assertFalse(handler.setTrainerNamesCalled);
        assertFalse(handler.setTrainerClassNamesCalled);
        assertFalse(randomizer.isChangesMade());
    }

    @Test
    void trainerNameRandomizerUsesSinglePoolForNormalNames() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "BOB");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(2)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertEquals(2, handler.writtenTrainerNames.size());
        assertTrue(handler.trainerNamePool.containsAll(handler.writtenTrainerNames));
    }

    @Test
    void trainerNameRandomizerChangesAtLeastOneNonEmptyTrainerNameWhenPoolDiffers() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE");

        new TrainerNameRandomizer(handler.proxy, settings(List.of("ANA"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertEquals(List.of("ANA"), handler.writtenTrainerNames);
        assertNotEquals(handler.trainerNames, handler.writtenTrainerNames);
    }

    @Test
    void trainerNameRandomizerUsesDoublesPoolForNamesWithAmpersand() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("AL & BOB", "CAL & DEE");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(3)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertEquals(2, handler.writtenTrainerNames.size());
        assertTrue(handler.doublesTrainerNamePool.containsAll(handler.writtenTrainerNames));
    }

    @Test
    void trainerNameRandomizerRepeatedNonSpecialNamesKeepSameTranslation() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "ALICE", "ALICE");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(4)).randomizeTrainerNames();

        assertEquals(handler.writtenTrainerNames.get(0), handler.writtenTrainerNames.get(1));
        assertEquals(handler.writtenTrainerNames.get(1), handler.writtenTrainerNames.get(2));
    }

    @Test
    void trainerNameRandomizerRepeatedSpecialNamesMayBeTranslatedSeparately() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("GRUNT", "GRUNT");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(0)).randomizeTrainerNames();

        assertEquals(2, handler.writtenTrainerNames.size());
        assertNotEquals(handler.writtenTrainerNames.get(0), handler.writtenTrainerNames.get(1));
    }

    @Test
    void trainerNameRandomizerRespectsMaxLength() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE", "BOB", "CARL");
        handler.trainerNamePool = List.of("TOOLONG", "ANA", "BEX");
        handler.maxTrainerNameLength = 3;

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(5)).randomizeTrainerNames();

        assertTrue(handler.writtenTrainerNames.stream().allMatch(name -> name.length() <= 3));
    }

    @Test
    void trainerNameRandomizerAllowsAsciiNameInsideInternalLimit() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE");
        handler.maxTrainerNameLength = 4;

        new TrainerNameRandomizer(handler.proxy, settings(List.of("ANA"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertEquals(List.of("ANA"), handler.writtenTrainerNames);
        assertTrue(handler.writtenTrainerNames.stream()
                .allMatch(name -> handler.internalLength(name) <= handler.maxTrainerNameLength));
    }

    @Test
    void trainerNameRandomizerAllowsNameExactlyAtInternalLimit() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE");
        handler.maxTrainerNameLength = 4;

        new TrainerNameRandomizer(handler.proxy, settings(List.of("BEXX"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertEquals(List.of("BEXX"), handler.writtenTrainerNames);
        assertEquals(handler.maxTrainerNameLength, handler.internalLength(handler.writtenTrainerNames.get(0)));
    }

    @Test
    void trainerNameRandomizerRejectsNameOverInternalLimit() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("AL");
        handler.maxTrainerNameLength = 3;
        handler.internalLengths.put("WIDE", 4);

        new TrainerNameRandomizer(handler.proxy, settings(List.of("WIDE"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertEquals(List.of("AL"), handler.writtenTrainerNames);
        assertTrue(handler.internalLength("WIDE") > handler.maxTrainerNameLength);
    }

    @Test
    void trainerNameRandomizerUsesInternalLengthWhenItDiffersFromJavaLength() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("AL");
        handler.maxTrainerNameLength = 5;
        handler.internalLengths.put("TOK", 6);

        new TrainerNameRandomizer(handler.proxy, settings(List.of("TOK"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertEquals(List.of("AL"), handler.writtenTrainerNames);
        assertTrue("TOK".length() <= handler.maxTrainerNameLength);
        assertTrue(handler.internalLength("TOK") > handler.maxTrainerNameLength);
    }

    @Test
    void trainerNameRandomizerAllowsEscapedTokenWhenInternalLengthFits() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("AL");
        handler.maxTrainerNameLength = 1;
        handler.internalLengths.put("\\x01", 1);

        new TrainerNameRandomizer(handler.proxy, settings(List.of("\\x01"), List.of("BOY"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertEquals(List.of("\\x01"), handler.writtenTrainerNames);
        assertTrue("\\x01".length() > handler.maxTrainerNameLength);
        assertEquals(handler.maxTrainerNameLength, handler.internalLength("\\x01"));
    }

    @Test
    void trainerNameRandomizerRespectsMaxLengthWithClass() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNameMode = RomHandler.TrainerNameMode.MAX_LENGTH_WITH_CLASS;
        handler.trainerNames = List.of("ALICE", "BOB");
        handler.trainerNamePool = List.of("TOOLONG", "ANA", "BEX");
        handler.maxTrainerNameLength = 8;
        handler.tcNameLengthsByTrainer = List.of(5, 5);

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(6)).randomizeTrainerNames();

        assertTrue(handler.writtenTrainerNames.stream().allMatch(name -> name.length() + 5 <= 8));
    }

    @Test
    void trainerNameRandomizerClassNamesShuffleExistingSingleAndDoublesClasses() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("LAD", "DUO", "LASS");
        handler.doublesTrainerClasses = List.of(1);

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(7)).randomizeTrainerClassNames();

        assertTrue(handler.setTrainerClassNamesCalled);
        assertTrue(List.of("LAD", "LASS").contains(handler.writtenTrainerClassNames.get(0)));
        assertEquals("DUO", handler.writtenTrainerClassNames.get(1));
        assertTrue(List.of("LAD", "LASS").contains(handler.writtenTrainerClassNames.get(2)));
        assertFalse(handler.trainerClassPool.contains(handler.writtenTrainerClassNames.get(0)));
    }

    @Test
    void trainerClassNameRandomizerIsSeparateFromTrainerNameRandomizer() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerNames = List.of("ALICE");
        handler.trainerClassNames = List.of("BURGLAR");

        new TrainerNameRandomizer(handler.proxy, settings(List.of("ANA"), List.of("ENGINEER"), List.of(), List.of()),
                new Random(1)).randomizeTrainerNames();

        assertTrue(handler.setTrainerNamesCalled);
        assertFalse(handler.setTrainerClassNamesCalled);
        assertEquals(List.of("ANA"), handler.writtenTrainerNames);
        assertEquals(List.of("BURGLAR"), handler.trainerClassNames);
    }

    @Test
    void trainerNameRandomizerFixedClassNamesLengthUsesSameInternalLength() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BOY", "GIRL");
        handler.trainerClassPool = List.of("AA", "DUO", "TEAM");
        handler.fixedTrainerClassNamesLength = true;

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(8)).randomizeTrainerClassNames();

        assertEquals(3, handler.writtenTrainerClassNames.get(0).length());
        assertEquals(4, handler.writtenTrainerClassNames.get(1).length());
    }

    @Test
    void trainerClassNameRandomizerUsesOnlyOriginalClassNames() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BURGLAR", "ENGINEER", "FISHERMAN", "CUE BALL");
        handler.trainerClassPool = List.of("Director");

        new TrainerNameRandomizer(handler.proxy, settings(List.of("ANA"), List.of("Director"), List.of(), List.of()),
                new Random(1)).randomizeTrainerClassNames();

        assertTrue(handler.trainerClassNames.containsAll(handler.writtenTrainerClassNames));
        assertFalse(handler.writtenTrainerClassNames.contains("Director"));
    }

    @Test
    void trainerClassNameRandomizerDoesNotCollapseToSingleCustomClassWhenExistingClassesAreAvailable() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BURGLAR", "ENGINEER", "FISHERMAN", "CUE BALL");
        handler.trainerClassPool = List.of("Director");

        new TrainerNameRandomizer(handler.proxy, settings(List.of("ANA"), List.of("Director"), List.of(), List.of()),
                new Random(1)).randomizeTrainerClassNames();

        assertTrue(handler.writtenTrainerClassNames.stream().distinct().count() > 1);
        assertFalse(handler.writtenTrainerClassNames.stream().allMatch("Director"::equals));
    }

    @Test
    void trainerClassNameRandomizerChangesAtLeastOneClassWhenPossible() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BURGLAR", "ENGINEER", "FISHERMAN", "CUE BALL");

        new TrainerNameRandomizer(handler.proxy, settings(), new Random(1)).randomizeTrainerClassNames();

        assertNotEquals(handler.trainerClassNames, handler.writtenTrainerClassNames);
    }

    @Test
    void trainerClassNameRandomizerAvoidsClassIdIdentityMappingsWhenAlternativesExist() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BUG CATCHER", "PSYCHIC", "ELITE 4", "COOLTRAINER");

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings(), new Random(1));
        randomizer.randomizeTrainerClassNames();

        Map<Integer, Integer> mapping = randomizer.getTrainerClassIdMapping();
        assertEquals(handler.trainerClassNames.size(), mapping.size());
        for (int classId = 0; classId < handler.trainerClassNames.size(); classId++) {
            assertNotEquals(classId, mapping.get(classId));
        }
        assertNotEquals("BUG CATCHER", handler.writtenTrainerClassNames.get(0));
    }

    @Test
    void trainerClassNameRandomizerRecordsPerTrainerAssignmentsForSpriteSync() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BUG CATCHER", "PSYCHIC", "ELITE 4", "BOARDER");
        handler.trainers = List.of(trainer(102, 0), trainer(103, 0));
        Settings settings = settings();
        settings.setRandomizeTrainerClassSprites(true);

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings,
                new CyclingRandom(0, 1));
        randomizer.randomizeTrainerClassNames();

        Map<Integer, Integer> assignments = randomizer.getTrainerClassIdAssignmentsByTrainerIndex();
        assertNotEquals(0, assignments.get(102));
        assertNotEquals(0, assignments.get(103));
        assertNotEquals(assignments.get(102), assignments.get(103));
    }

    @Test
    void trainerClassNameRandomizerKeepsIdentityWhenNoAlternativeExists() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("OLD");

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings(), new Random(1));
        randomizer.randomizeTrainerClassNames();

        assertEquals(List.of("OLD"), handler.writtenTrainerClassNames);
        assertEquals(Map.of(0, 0), randomizer.getTrainerClassIdMapping());
    }

    @Test
    void gen3StyleClassNamesUseMaxLengthNotSameLengthBuckets() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BURGLAR", "ENGINEER", "FISHERMAN");
        handler.maxTrainerClassNameLength = 8;

        Map<Integer, List<String>> validBySourceLength = validClassCandidatesBySourceLength(
                handler,
                List.of("Director", "Commander"),
                handler.trainerClassNames);

        assertFalse(handler.fixedTrainerClassNamesLength);
        assertEquals(List.of("Director"), validBySourceLength.get(7));
        assertEquals(List.of("Director"), validBySourceLength.get(8));
        assertEquals(List.of("Director"), validBySourceLength.get(9));

        new TrainerNameRandomizer(handler.proxy,
                settings(List.of("ANA"), List.of("Director", "Commander"), List.of(), List.of()),
                new Random(1)).randomizeTrainerClassNames();

        assertTrue(handler.trainerClassNames.containsAll(handler.writtenTrainerClassNames));
        assertFalse(handler.writtenTrainerClassNames.contains("Director"));
        assertTrue(handler.writtenTrainerClassNames.stream().distinct().count() > 1);
    }

    @Test
    void sameLengthModeWouldNotMapEveryCommonLengthToDirector() {
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.trainerClassNames = List.of("BURGLAR", "ENGINEER", "FISHERMAN");
        handler.fixedTrainerClassNamesLength = true;
        handler.maxTrainerClassNameLength = 9;

        Map<Integer, List<String>> validBySourceLength = validClassCandidatesBySourceLength(
                handler,
                List.of("Director", "Leader", "Commander"),
                handler.trainerClassNames);

        assertEquals(Collections.emptyList(), validBySourceLength.get(7));
        assertEquals(List.of("Director"), validBySourceLength.get(8));
        assertEquals(List.of("Commander"), validBySourceLength.get(9));

        new TrainerNameRandomizer(handler.proxy,
                settings(List.of("ANA"), List.of("Director", "Leader", "Commander"), List.of(), List.of()),
                new Random(1)).randomizeTrainerClassNames();

        assertEquals("BURGLAR", handler.writtenTrainerClassNames.get(0));
        assertEquals("ENGINEER", handler.writtenTrainerClassNames.get(1));
        assertEquals("FISHERMAN", handler.writtenTrainerClassNames.get(2));
    }

    @Test
    void trainerNameAndClassRandomizersDoNotAlterStarterEvolutionChain() {
        Species bulbasaur = species(1, "Bulbasaur");
        Species ivysaur = species(2, "Ivysaur");
        Species venusaur = species(3, "Venusaur");
        Species squirtle = species(7, "Squirtle");
        Species wartortle = species(8, "Wartortle");
        addEvolution(bulbasaur, ivysaur, 16);
        addEvolution(ivysaur, venusaur, 32);
        addEvolution(squirtle, wartortle, 16);
        List<Species> starterChain = List.of(bulbasaur, ivysaur, venusaur, squirtle, wartortle);
        Map<Integer, List<String>> evolutionsBefore = evolutionTargetsBySpecies(starterChain);
        TrainerNameTestRomHandler handler = TrainerNameTestRomHandler.create();
        handler.species = starterChain;

        TrainerNameRandomizer randomizer = new TrainerNameRandomizer(handler.proxy, settings(), new Random(9));
        randomizer.randomizeTrainerNames();
        randomizer.randomizeTrainerClassNames();

        assertTrue(randomizer.isChangesMade());
        assertEquals(evolutionsBefore, evolutionTargetsBySpecies(starterChain));
        assertEquals(List.of("Ivysaur:LEVEL:16"), evolutionTargetsBySpecies(List.of(bulbasaur)).get(1));
        assertEquals(List.of("Venusaur:LEVEL:32"), evolutionTargetsBySpecies(List.of(ivysaur)).get(2));
        assertEquals(List.of("Wartortle:LEVEL:16"), evolutionTargetsBySpecies(List.of(squirtle)).get(7));
        assertTrue(venusaur.getEvolutionsFrom().isEmpty());
    }

    private static Settings settings() {
        return settings(
                List.of("ANA", "BEX", "CODY", "DORA", "ELI"),
                List.of("BOY", "GIRL", "DUO", "TEAM"),
                List.of("ANA & BEX", "CODY & DORA"),
                List.of("PAIR", "TEAM"));
    }

    private static Settings settings(List<String> trainerNames, List<String> trainerClasses,
                                     List<String> doublesTrainerNames, List<String> doublesTrainerClasses) {
        Settings settings = new Settings();
        settings.setCustomNames(new CustomNamesSet(
                trainerNames,
                trainerClasses,
                doublesTrainerNames,
                doublesTrainerClasses,
                Collections.emptyList()));
        return settings;
    }

    private static Map<Integer, List<String>> validClassCandidatesBySourceLength(TrainerNameTestRomHandler handler,
                                                                                List<String> candidateClasses,
                                                                                List<String> sourceClasses) {
        Map<Integer, List<String>> validBySourceLength = new HashMap<>();
        for (String sourceClass : sourceClasses) {
            int sourceLength = handler.internalLength(sourceClass);
            List<String> validCandidates = candidateClasses.stream()
                    .filter(candidate -> handler.internalLength(candidate) <= handler.maxTrainerClassNameLength)
                    .filter(candidate -> !handler.fixedTrainerClassNamesLength
                            || handler.internalLength(candidate) == sourceLength)
                    .collect(Collectors.toList());
            validBySourceLength.put(sourceLength, validCandidates);
        }
        return validBySourceLength;
    }

    private static Species species(int number, String name) {
        Species species = new Species(number);
        species.setName(name);
        return species;
    }

    private static Trainer trainer(int index, int trainerClass) {
        Trainer trainer = new Trainer();
        trainer.setIndex(index);
        trainer.setTrainerclass(trainerClass);
        return trainer;
    }

    private static void addEvolution(Species from, Species to, int level) {
        Evolution evolution = new Evolution(from, to, EvolutionType.LEVEL, level);
        from.getEvolutionsFrom().add(evolution);
        to.getEvolutionsTo().add(evolution);
    }

    private static Map<Integer, List<String>> evolutionTargetsBySpecies(List<Species> species) {
        Map<Integer, List<String>> evolutions = new HashMap<>();
        for (Species current : species) {
            evolutions.put(current.getNumber(), current.getEvolutionsFrom().stream()
                    .map(evolution -> evolution.getTo().getName() + ":" + evolution.getType() + ":"
                            + evolution.getExtraInfo())
                    .collect(Collectors.toList()));
        }
        return evolutions;
    }

    private static class TrainerNameTestRomHandler implements InvocationHandler {
        private RomHandler proxy;
        private RestrictedSpeciesService restrictedSpeciesService;
        private TypeService typeService;
        private List<Species> species = Collections.emptyList();
        private boolean canChangeTrainerText = true;
        private List<String> trainerNames = List.of("ALICE");
        private List<String> trainerNamePool = List.of("ANA", "BEX", "CODY", "DORA", "ELI");
        private List<String> doublesTrainerNamePool = List.of("ANA & BEX", "CODY & DORA");
        private List<String> trainerClassNames = List.of("LAD");
        private List<String> trainerClassPool = List.of("BOY", "GIRL", "DUO", "TEAM");
        private List<String> doublesTrainerClassPool = List.of("PAIR", "TEAM");
        private List<Trainer> trainers = Collections.emptyList();
        private List<Integer> doublesTrainerClasses = Collections.emptyList();
        private List<Integer> tcNameLengthsByTrainer = Collections.emptyList();
        private RomHandler.TrainerNameMode trainerNameMode = RomHandler.TrainerNameMode.MAX_LENGTH;
        private int maxTrainerNameLength = 10;
        private int maxSumOfTrainerNameLengths = Integer.MAX_VALUE;
        private boolean fixedTrainerClassNamesLength;
        private int maxTrainerClassNameLength = 10;
        private final Map<String, Integer> internalLengths = new HashMap<>();
        private boolean setTrainerNamesCalled;
        private boolean setTrainerClassNamesCalled;
        private List<String> writtenTrainerNames = new ArrayList<>();
        private List<String> writtenTrainerClassNames = new ArrayList<>();

        private static TrainerNameTestRomHandler create() {
            TrainerNameTestRomHandler handler = new TrainerNameTestRomHandler();
            handler.proxy = (RomHandler) Proxy.newProxyInstance(
                    RomHandler.class.getClassLoader(), new Class<?>[] { RomHandler.class }, handler);
            handler.restrictedSpeciesService = new RestrictedSpeciesService(handler.proxy);
            handler.typeService = new TypeService(handler.proxy);
            return handler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "getRestrictedSpeciesService" -> restrictedSpeciesService;
                case "getTypeService" -> typeService;
                case "getSpeciesSetInclFormes", "getSpeciesSet" -> {
                    SpeciesSet speciesSet = new SpeciesSet();
                    speciesSet.addAll(species);
                    yield speciesSet;
                }
                case "getSpeciesInclFormes", "getSpecies" -> species;
                case "canChangeTrainerText" -> canChangeTrainerText;
                case "getTrainerNames" -> trainerNames;
                case "setTrainerNames" -> {
                    setTrainerNamesCalled = true;
                    writtenTrainerNames = new ArrayList<>(asStringList(args[0]));
                    yield null;
                }
                case "trainerNameMode" -> trainerNameMode;
                case "maxTrainerNameLength" -> maxTrainerNameLength;
                case "maxSumOfTrainerNameLengths" -> maxSumOfTrainerNameLengths;
                case "getTCNameLengthsByTrainer" -> tcNameLengthsByTrainer;
                case "getTrainerClassNames" -> trainerClassNames;
                case "setTrainerClassNames" -> {
                    setTrainerClassNamesCalled = true;
                    writtenTrainerClassNames = new ArrayList<>(asStringList(args[0]));
                    yield null;
                }
                case "fixedTrainerClassNamesLength" -> fixedTrainerClassNamesLength;
                case "maxTrainerClassNameLength" -> maxTrainerClassNameLength;
                case "getDoublesTrainerClasses" -> doublesTrainerClasses;
                case "getTrainers" -> trainers;
                case "internalStringLength" -> internalLength((String) args[0]);
                case "toString" -> "TrainerNameRandomizerTestRomHandler";
                case "hashCode" -> System.identityHashCode(this);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private int internalLength(String string) {
            return internalLengths.getOrDefault(string, string.length());
        }

        @SuppressWarnings("unchecked")
        private static List<String> asStringList(Object value) {
            return (List<String>) value;
        }
    }

    private static class CyclingRandom extends Random {
        private final int[] values;
        private int index;

        CyclingRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return values[index++ % values.length] % bound;
        }
    }
}
