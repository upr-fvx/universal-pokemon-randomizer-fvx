package test.randomizers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.romhandlers.AbstractRomHandler;
import com.dabomstew.pkrandom.romhandlers.PokemonImageGetter;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.romhandlers.romentries.RomEntry;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import com.dabomstew.pkrandom.services.TypeService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.*;

/**
 * A special "dummy" romHandler which copies data from an existing romHandler.
 *
 */
public class TestRomHandler extends AbstractRomHandler {
    //Species
    private final SpeciesSet originalSpeciesInclFormes;
    private SpeciesSet testSpeciesInclFormes = null;
    private SpeciesSet testSpeciesNoFormes = null;
    Map<Species, Species> originalToTest = null;
    private List<MegaEvolution> testMegaEvolutions = null;
    private SpeciesSet testAltFormes = null;
    private final SpeciesSet originalIrregularFormes;
    private SpeciesSet testIrregularFormes = null;
    Map<Species, Map<Integer, Species>> testAltFormesMap = null;
    private RestrictedSpeciesService testRSS = null;
    private List<Species> testSpeciesInOrder = null;

    //Abilities
    private final int abilitiesPerSpecies;

    //Moves
    private final List<Move> originalMoves;
    private List<Move> testMoves;
    private final Map<Integer, List<MoveLearnt>> originalMovesLearnt;
    private Map<Integer, List<MoveLearnt>> testMovesLearnt;
    private final Map<Integer, List<Integer>> originalEggMoves;
    private Map<Integer, List<Integer>> testEggMoves;

    // TMs/HMs/Tutors
    private final List<Integer> originalTMMoves;
    private List<Integer> testTMMoves;
    private final List<Integer> hmMoves;
    private final List<Integer> originalMoveTutorMoves;
    private List<Integer> testMoveTutorMoves;
    private final Map<Species, boolean[]> originalTMHMCompatibility;
    private Map<Species, boolean[]> testTMHMCompatibility;
    private final Map<Species, boolean[]> originalMoveTutorCompatibility;
    private Map<Species, boolean[]> testMoveTutorCompatibility;

    //Evolutions
    private final boolean altFormesCanHaveDifferentEvolutions;

    //Encounters (wild)
    private final List<EncounterArea> originalEncounters;
    List<EncounterArea> testEncounters = null;
    private final boolean hasTimeBasedEncounters;
    private final boolean hasWildAltFormes;
    private final SpeciesSet originalBannedForWild;

    //Statics
    private final List<StaticEncounter> originalStatics;
    private List<StaticEncounter> testStatics = null;
    private final boolean canChangeStaticPokemon;
    private final boolean hasStaticAltFormes;
    private final SpeciesSet originalBannedForStatics;
    private final boolean forceSwapStaticMegaEvos;
    private final List<Integer> mainGameLegendaries;

    //Types
    private final TypeTable originalTypeTable;
    private TypeTable testTypeTable = null;
    private final boolean hasTypeEffectivenessSupport;

    //ROM identifiers
    private final int generation;
    private final boolean isYellow;
    private final boolean isORAS;
    private final boolean isUSUM;
    private final int romType;

    //Starters
    private final List<Species> originalStarters;
    private List<Species> testStarters = null;
    private final boolean hasStarterAltFormes;

    //Items
    private final List<String> itemNames;

    //Trainers
    private final SpeciesSet originalBannedForTrainers;
    private SpeciesSet testBannedForTrainers = null;
    private final List<Trainer> originalTrainers;
    private List<Trainer> testTrainers = null;
    private final List<Integer> mainPlaythroughTrainers;
    private final List<Integer> eliteFourTrainers;
    private final List<Integer> challengeModeEliteFourTrainers;
    private final Map<String, Type> gymAndEliteTypeThemes;
    private final boolean trainerPokemonAlwaysUseAbility1;
    private final boolean trainerPokemonUseBaseFormeAbilities;

    /**
     * Given a loaded RomHandler, creates a mockup TestRomHandler by extracting the data from it.
     * @param mockupOf A loaded RomHandler to create a mockup of.
     */
    public TestRomHandler(RomHandler mockupOf) {
        originalTypeTable = new TypeTable(mockupOf.getTypeTable());
        originalSpeciesInclFormes = SpeciesSet.unmodifiable(mockupOf.getSpeciesInclFormes());
        originalEncounters = Collections.unmodifiableList(mockupOf.getEncounters(true));
        originalIrregularFormes = SpeciesSet.unmodifiable(mockupOf.getIrregularFormes());

        abilitiesPerSpecies = mockupOf.abilitiesPerSpecies();

        originalMoves = Collections.unmodifiableList(mockupOf.getMoves());
        originalMovesLearnt = Collections.unmodifiableMap(mockupOf.getMovesLearnt());
        originalEggMoves = Collections.unmodifiableMap(mockupOf.getEggMoves());

        originalTMMoves = Collections.unmodifiableList(mockupOf.getTMMoves());
        hmMoves = Collections.unmodifiableList(mockupOf.getHMMoves());
        originalMoveTutorMoves = Collections.unmodifiableList(mockupOf.getMoveTutorMoves());
        originalTMHMCompatibility = Collections.unmodifiableMap(mockupOf.getTMHMCompatibility());
        originalMoveTutorCompatibility = mockupOf.hasMoveTutors() ?
                Collections.unmodifiableMap(mockupOf.getMoveTutorCompatibility()) :
                Collections.emptyMap();

        altFormesCanHaveDifferentEvolutions = mockupOf.altFormesCanHaveDifferentEvolutions();

        hasTimeBasedEncounters = mockupOf.hasTimeBasedEncounters();
        hasWildAltFormes = mockupOf.hasWildAltFormes();
        originalBannedForWild = SpeciesSet.unmodifiable(mockupOf.getBannedForWildEncounters());

        originalStatics = Collections.unmodifiableList(mockupOf.getStaticPokemon());
        canChangeStaticPokemon = mockupOf.canChangeStaticPokemon();
        hasStaticAltFormes = mockupOf.hasStaticAltFormes();
        originalBannedForStatics = SpeciesSet.unmodifiable(mockupOf.getBannedForStaticPokemon());
        forceSwapStaticMegaEvos = mockupOf.forceSwapStaticMegaEvos();
        if(mockupOf.hasMainGameLegendaries()) {
            mainGameLegendaries = Collections.unmodifiableList(mockupOf.getMainGameLegendaries());
        } else {
            mainGameLegendaries = Collections.unmodifiableList(new ArrayList<>());
        }

        hasTypeEffectivenessSupport = mockupOf.hasTypeEffectivenessSupport();

        generation = mockupOf.generationOfPokemon();
        romType = mockupOf.getROMType();
        isYellow = mockupOf.isYellow();
        isORAS = mockupOf.isORAS();
        isUSUM = mockupOf.isUSUM();

        originalStarters = Collections.unmodifiableList(mockupOf.getStarters());
        hasStarterAltFormes = mockupOf.hasStarterAltFormes();

        itemNames = Collections.unmodifiableList(Arrays.asList(mockupOf.getItemNames()));

        originalBannedForTrainers = SpeciesSet.unmodifiable(mockupOf.getBannedFormesForTrainerPokemon());
        originalTrainers = Collections.unmodifiableList(mockupOf.getTrainers());
        mainPlaythroughTrainers = Collections.unmodifiableList(mockupOf.getMainPlaythroughTrainers());
        eliteFourTrainers = Collections.unmodifiableList(mockupOf.getEliteFourTrainers(false));
        challengeModeEliteFourTrainers = Collections.unmodifiableList(mockupOf.getEliteFourTrainers(true));
        gymAndEliteTypeThemes = Collections.unmodifiableMap(mockupOf.getGymAndEliteTypeThemes());
        trainerPokemonAlwaysUseAbility1 = mockupOf.isTrainerPokemonAlwaysUseAbility1();
        trainerPokemonUseBaseFormeAbilities = mockupOf.isTrainerPokemonUseBaseFormeAbilities();

        perfectAccuracy = mockupOf.getPerfectAccuracy();
    }

    /**
     * Prepares for testing by making a deep copy of all Species,
     * which are passed by reference and therefore cannot otherwise be reset. <br>
     * Other test data is copied as needed.
     */
    public void prepare() {
        testSpeciesInclFormes = deepCopySpeciesSet(originalSpeciesInclFormes);
        testSpeciesInclFormes.forEach(Species::saveOriginalData);
    }

    /**
     * Drops all test data.
     */
    public void reset() {
        testSpeciesInclFormes = null;
        testSpeciesNoFormes = null;
        originalToTest = null;
        testMegaEvolutions = null;
        testAltFormes = null;
        testIrregularFormes = null;
        testAltFormesMap = null;
        testRSS = null;
        testSpeciesInOrder = null;

        testMoves = null;
        testMovesLearnt = null;
        testEggMoves = null;

        testTMMoves = null;
        testMoveTutorMoves = null;
        testTMHMCompatibility = null;
        testMoveTutorCompatibility = null;

        testEncounters = null;

        testStatics = null;

        testTypeTable = null;

        testStarters = null;

        testBannedForTrainers = null;
        testTrainers = null;
    }

    /**
     * Deep copies the set of all Species in the game.
     * When finished, all Species should contain the same data, but have no references to
     * the original set.
     * @param originalSet The set of Species to deep-copy.
     * @return A copy of each of the Species given, with no
     */
    private SpeciesSet deepCopySpeciesSet(SpeciesSet originalSet) {
        SpeciesSet newSet = new SpeciesSet();
        originalToTest = new HashMap<>();
        for(Species orig : originalSet) {
            Species copy = copySpeciesStaticTraits(orig);
            newSet.add(copy);
            originalToTest.put(orig, copy);
        }

        testMegaEvolutions = new ArrayList<>();
        testAltFormes = new SpeciesSet();
        testIrregularFormes = new SpeciesSet();
        testAltFormesMap = new HashMap<>();
        //now that they're all here, iterate again to copy relations
        for(Species orig : originalSet) {
            copySpeciesRelations(orig, originalToTest);
        }

        return newSet;
    }

    /**
     * Copies all data from one species to another, excepting data which contains references to other Species.
     * @param original The Species to copy.
     * @return A new Species with none of its fields having reference to the original Species.
     */
    private static Species copySpeciesStaticTraits(Species original) {
        boolean isGen1 = original instanceof Gen1Species;
        Species copy;
        if(isGen1) {
            copy = new Gen1Species(original.getNumber());
        } else {
            copy = new Species(original.getNumber());
        }
        copy.setName(original.getName());

        //formes
        copy.setFormeSuffix(original.getFormeSuffix());
        copy.setFormeNumber(original.getFormeNumber());
        copy.setCosmeticForms(original.getCosmeticForms());
        copy.setActuallyCosmetic(original.isActuallyCosmetic());

        copy.setRealCosmeticFormNumbers(new ArrayList<>(copy.getRealCosmeticFormNumbers()));
        //I don't know if that copy is necessary, but it shouldn't hurt?

        copy.setGeneration(original.getGeneration());

        //Types
        copy.setPrimaryType(original.getPrimaryType(true));
        copy.setSecondaryType(original.getSecondaryType(true));
        //using original or not shouldn't matter

        //base stats
        copy.setHp(original.getHp());
        copy.setAttack(original.getAttack());
        copy.setDefense(original.getDefense());
        copy.setSpeed(original.getSpeed());
        if(isGen1) {
            copy.setSpecial(original.getSpecial());
        } else {
            copy.setSpatk(original.getSpatk());
            copy.setSpdef(original.getSpdef());
        }

        //abilities
        copy.setAbility1(original.getAbility1());
        copy.setAbility2(original.getAbility2());
        copy.setAbility3(original.getAbility3());

        copy.setExpYield(original.getExpYield());

        //wild encounter related
        copy.setCatchRate(original.getCatchRate());
        copy.setCommonHeldItem(original.getCommonHeldItem());
        copy.setRareHeldItem(original.getRareHeldItem());
        copy.setDarkGrassHeldItem(original.getDarkGrassHeldItem());
        copy.setGenderRatio(original.getGenderRatio());

        //misc
        copy.setFrontImageDimensions(original.getFrontImageDimensions());
        copy.setCallRate(original.getCallRate());
        copy.setGrowthCurve(original.getGrowthCurve());

        return copy;
    }

    /**
     * Given an original species and a copy of that species, as well as a map of all original species to copies of them,
     * copies all data which contains references to other Species, updating those references to the copies. <br>
     * For evolutions and mega evolutions, it only assigns those from this Species,
     * but also assigns it to the Species evolved to.
     * This should result in all evolutions being properly assigned if this function is called on all Species.
     * @param original The Species to copy relations from. They will be copied to its mapped value.
     * @param originalToCopies A Map of the original Species to their copies.
     */
    private void copySpeciesRelations(Species original, Map<Species, Species> originalToCopies) {
        Species copy = originalToCopies.get(original);
        if(!original.isBaseForme()) {
            Species copyBaseForme = originalToCopies.get(original.getBaseForme());
            copy.setBaseForme(copyBaseForme);
            testAltFormes.add(copy);

            if(originalIrregularFormes.contains(original)) {
                testIrregularFormes.add(copy);
            }
            if(!testAltFormesMap.containsKey(copyBaseForme)) {
                testAltFormesMap.put(copyBaseForme, new HashMap<>());
                testAltFormesMap.get(copyBaseForme).put(copyBaseForme.getFormeNumber(), copyBaseForme);
            }
            testAltFormesMap.get(copyBaseForme).put(copy.getFormeNumber(), copy);
        }

        for(Evolution evolution : original.getEvolutionsFrom()) {
            Evolution evoCopy = new Evolution(copy, originalToCopies.get(evolution.getTo()),
                    evolution.getType(), evolution.getExtraInfo());
            evoCopy.setForme(evolution.getForme());
            copy.getEvolutionsFrom().add(evoCopy);
            evoCopy.getTo().getEvolutionsTo().add(evoCopy);
        }

        for(MegaEvolution evolution : original.getMegaEvolutionsFrom()) {
            MegaEvolution evoCopy = new MegaEvolution(copy, originalToCopies.get(evolution.to),
                    evolution.method, evolution.argument);
            evoCopy.carryStats = evolution.carryStats;
            copy.getMegaEvolutionsFrom().add(evoCopy);
            evoCopy.to.getMegaEvolutionsTo().add(evoCopy);

            testMegaEvolutions.add(evoCopy);
        }
    }

    /**
     * Given a SpeciesSet, creates a new SpeciesSet containing the copy corresponding to each Species in the set.
     * @param original The SpeciesSet to copy.
     * @return A new SpeciesSet with the corresponding copy for each Species in the original.
     */
    private SpeciesSet copySpeciesSet(SpeciesSet original) {
        SpeciesSet copy = new SpeciesSet();
        for(Species origSpec : original) {
            copy.add(originalToTest.get(origSpec));
        }
        return copy;
    }

    /**
     * Given a List of EncounterAreas, copies them such that each Species in the original Encounters
     * is replaced by its copied test version.
     * @param originalEncounters The List of EncounterAreas to copy.
     * @return A new List of new EncounterAreas which are copies of the given ones.
     */
    private List<EncounterArea> deepCopyEncounters(List<EncounterArea> originalEncounters) {
        List<EncounterArea> copiedEncounters = new ArrayList<>();
        for(EncounterArea originalArea : originalEncounters) {
            EncounterArea copiedArea = new EncounterArea();
            copiedArea.setRate(originalArea.getRate());
            copiedArea.banAllSpecies(copySpeciesSet(originalArea.getBannedSpecies()));
            copiedArea.setIdentifiers(originalArea.getDisplayName(), originalArea.getMapIndex(),
                    originalArea.getEncounterType(), originalArea.getLocationTag());
            copiedArea.setPostGame(originalArea.isPostGame());
            copiedArea.setPartiallyPostGameCutoff(originalArea.getPartiallyPostGameCutoff());
            copiedArea.setForceMultipleSpecies(originalArea.isForceMultipleSpecies());

            for(Encounter origEnc : originalArea) {
                Encounter copyEnc = new Encounter();
                copyEnc.setLevel(origEnc.getLevel());
                copyEnc.setMaxLevel(origEnc.getMaxLevel());
                copyEnc.setSpecies(originalToTest.get(origEnc.getSpecies()));
                copyEnc.setFormeNumber(origEnc.getFormeNumber());
                copyEnc.setSOS(origEnc.isSOS());
                copyEnc.setSosType(origEnc.getSosType());

                copiedArea.add(copyEnc);
            }

            copiedEncounters.add(copiedArea);
        }

        return copiedEncounters;
    }

    /**
     * Given a List of {@link StaticEncounter}s, copies them such that each Species in the original
     * is replaced by its copied test version.
     * @param originalStatics The List of StaticEncounters to copy.
     * @return A new List of new StaticEncounters which are copies of the given ones.
     */
    private List<StaticEncounter> deepCopyStatics(List<StaticEncounter> originalStatics) {
        List<StaticEncounter> copiedStatics = new ArrayList<>();
        for(StaticEncounter orig : originalStatics) {
            StaticEncounter copy = new StaticEncounter(orig);
            Species spec = originalToTest.get(orig.spec);
            copy.spec = spec;
            for(StaticEncounter linked : copy.linkedEncounters) {
                linked.spec = spec;
            }
            copiedStatics.add(copy);
        }
        return copiedStatics;
    }

    /**
     * Given a List of {@link Trainer}s, copies them such that each Species in the original
     * is replaced by its copied test version.
     * @param originalTrainers The List of Trainers to copy.
     * @return A new List of Trainers which are copies of the given one.
     */
    private List<Trainer> deepCopyTrainers(List<Trainer> originalTrainers) {
        List<Trainer> copiedTrainers = new ArrayList<>();
        for(Trainer original : originalTrainers) {
            Trainer copy = new Trainer(original);
            for(TrainerPokemon tp : copy.pokemon) {
                tp.species = originalToTest.get(tp.species);
            }
            copiedTrainers.add(copy);
        }

        return copiedTrainers;
    }

    /**
     * Given a {@link List} of {@link Move}s, copies each.
     * @param originalMoves The List of Moves to copy.
     * @return A new List of Moves containing a copy of each move.
     */
    private List<Move> deepCopyMoves(List<Move> originalMoves) {
        List<Move> copiedMoves = new ArrayList<>();
        for(Move original : originalMoves) {
            if(original == null) {
                copiedMoves.add(null);
                //some games start the list with a null move so they start at 1 instead of zero
                //(weirdly, it's not all games)
            } else {
                Move copy = new Move(original);
                copiedMoves.add(copy);
            }
        }
        return copiedMoves;
    }

    @Override
    public boolean loadRom(String filename) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public boolean saveRom(String filename, long seed, boolean saveAsDirectory) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public void saveMoves() {

    }

    @Override
    public void savePokemonStats() {

    }

    @Override
    protected boolean saveRomFile(String filename, long seed) {
        return false;
    }

    @Override
    protected boolean saveRomDirectory(String filename) {
        return false;
    }

    @Override
    protected RomEntry getRomEntry() {
        return null;
    }

    @Override
    public String loadedFilename() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public boolean hasGameUpdateLoaded() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public boolean loadGameUpdate(String filename) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public void removeGameUpdate() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public String getGameUpdateVersion() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public void printRomDiagnostics(PrintStream logStream) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public boolean isRomValid(PrintStream logStream) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public List<Species> getSpecies() {
        if(testSpeciesInOrder == null) {
            testSpeciesInOrder = new ArrayList<>(getSpeciesSet());
            testSpeciesInOrder.sort(Comparator.comparingInt(Species::getNumber)); //ok that's some sleek syntax. gj java.
        }
        return testSpeciesInOrder;
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        throw new NotImplementedException();
    }

    @Override
    public SpeciesSet getAltFormes() {
        return testAltFormes;
    }

    @Override
    public SpeciesSet getSpeciesSet() {
        if(testSpeciesNoFormes == null) {
            testSpeciesNoFormes = testSpeciesInclFormes.filter(Species::isBaseForme);
        }

        return testSpeciesNoFormes;
    }

    @Override
    public SpeciesSet getSpeciesSetInclFormes() {
        return testSpeciesInclFormes;
    }

    @Override
    public List<MegaEvolution> getMegaEvolutions() {
        return testMegaEvolutions;
        //why does this even exist????
    }

    @Override
    public Species getAltFormeOfSpecies(Species base, int forme) {
        if(testAltFormesMap.get(base) == null) {
            return base;
        } else {
            return testAltFormesMap.get(base).get(forme);
        }
        //why is this even in RomHandler??
    }

    @Override
    public SpeciesSet getIrregularFormes() {
        return testIrregularFormes;
    }

    @Override
    public RestrictedSpeciesService getRestrictedSpeciesService() {
        if(testRSS == null) {
            testRSS = new RestrictedSpeciesService(this);
            testRSS.setRestrictions(new Settings());
        }
        return testRSS;
    }

    @Override
    public void removeEvosForPokemonPool() {
        throw new NotImplementedException();
        //Why is THIS in RomHandler, either???
    }

    @Override
    public List<Species> getStarters() {
        if(testStarters == null) {
            testStarters = new ArrayList<>();
            for(Species origStarter : originalStarters) {
                testStarters.add(originalToTest.get(origStarter));
            }
        }

        return testStarters;
    }

    @Override
    public boolean setStarters(List<Species> newStarters) {
        if(newStarters.size() != originalStarters.size()) {
            throw new IllegalArgumentException("Starter list is wrong size!");
        }

        testStarters = newStarters;
        return true;
    }

    @Override
    public boolean hasStarterAltFormes() {
        return hasStarterAltFormes;
    }

    @Override
    public int starterCount() {
        return originalStarters.size();
    }

    @Override
    public boolean hasStarterTypeTriangleSupport() {
        return originalStarters.size() % 3 == 0;
    }

    @Override
    public boolean supportsStarterHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getStarterHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public void setStarterHeldItems(List<Integer> items) {
        throw new NotImplementedException();
    }

    @Override
    public int abilitiesPerSpecies() {
        return abilitiesPerSpecies;
    }

    @Override
    public int highestAbilityIndex() {
        throw new NotImplementedException();
    }

    @Override
    public String abilityName(int number) {
        throw new NotImplementedException();
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getUselessAbilities() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isTrainerPokemonAlwaysUseAbility1() {
        return trainerPokemonAlwaysUseAbility1;
    }

    @Override
    public boolean isTrainerPokemonUseBaseFormeAbilities() {
        return trainerPokemonUseBaseFormeAbilities;
    }

    @Override
    public boolean hasMegaEvolutions() {
        throw new NotImplementedException();
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        if(!useTimeOfDay) {
            throw new NotImplementedException();
        }

        if(testEncounters == null) {
            testEncounters = deepCopyEncounters(originalEncounters);
        }

        return testEncounters;
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        throw new NotImplementedException();
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounters) {
        testEncounters = encounters;
    }

    @Override
    public boolean hasTimeBasedEncounters() {
        return hasTimeBasedEncounters;
    }

    @Override
    public boolean hasWildAltFormes() {
        return hasWildAltFormes;
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        return originalBannedForWild;
        //TODO: ensure this works
    }

    @Override
    public void enableGuaranteedPokemonCatching() {
        throw new NotImplementedException();
    }

    @Override
    public List<Trainer> getTrainers() {
        if(testTrainers == null) {
            testTrainers = deepCopyTrainers(originalTrainers);
        }
        return testTrainers;
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        return mainPlaythroughTrainers;
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        if(isChallengeMode) {
            return challengeModeEliteFourTrainers;
        } else {
            return eliteFourTrainers;
        }
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        return gymAndEliteTypeThemes;
    }

    @Override
    public void setTrainers(List<Trainer> trainerData) {
        testTrainers = trainerData;
    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getAllConsumableHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getAllHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasRivalFinalBattle() {
        throw new NotImplementedException();
    }

    @Override
    public void makeDoubleBattleModePossible() {
        //do nothing
    }

    @Override
    public boolean hasPhysicalSpecialSplit() {
        throw new NotImplementedException();
    }

    @Override
    public List<Move> getMoves() {
        if(testMoves == null) {
            testMoves = deepCopyMoves(originalMoves);
        }
        return testMoves;
    }

    @Override
    public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
        if (testMovesLearnt == null) {
            testMovesLearnt = new HashMap<>();
            for (Integer pkNum : originalMovesLearnt.keySet()) {

                List<MoveLearnt> moveLearntsCopy = new ArrayList<>();
                for (MoveLearnt ml : originalMovesLearnt.get(pkNum)) {
                    moveLearntsCopy.add(new MoveLearnt(ml));
                }
                testMovesLearnt.put(pkNum, moveLearntsCopy);
            }
        }

        return testMovesLearnt;
    }

    @Override
    public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {
        testMovesLearnt = movesets;
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        throw new NotImplementedException();
    }

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        if (testEggMoves == null) {
            testEggMoves = new HashMap<>();
            for (Map.Entry<Integer, List<Integer>> entry : originalEggMoves.entrySet()) {
                testEggMoves.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }

        return testEggMoves;
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        testEggMoves = eggMoves;
    }

    @Override
    public boolean supportsFourStartingMoves() {
        throw new NotImplementedException();
    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        if(!canChangeStaticPokemon) {
            throw new UnsupportedOperationException("Base romHandler does not support changing statics!");
        }

        if(testStatics == null) {
            testStatics = deepCopyStatics(originalStatics);
        }
        return testStatics;
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        if(!canChangeStaticPokemon) {
            throw new UnsupportedOperationException("Base romHandler does not support changing statics!");
        }

        testStatics = staticPokemon;
        return true;
    }

    @Override
    public boolean canChangeStaticPokemon() {
        return canChangeStaticPokemon;
    }

    @Override
    public boolean hasStaticAltFormes() {
        return hasStaticAltFormes;
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        return originalBannedForStatics;
        //TODO: verify this works
    }

    @Override
    public boolean forceSwapStaticMegaEvos() {
        //what... does this mean
        return forceSwapStaticMegaEvos;
    }

    @Override
    public boolean hasMainGameLegendaries() {
        return !mainGameLegendaries.isEmpty();
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        return mainGameLegendaries;
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        throw new NotImplementedException();
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasStaticMusicFix() {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasTotemPokemon() {
        throw new NotImplementedException();
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        throw new NotImplementedException();
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getTMMoves() {
        if (testTMMoves == null) {
            testTMMoves = new ArrayList<>(originalTMMoves);
        }

        return testTMMoves;
    }

    @Override
    public List<Integer> getHMMoves() {
        return hmMoves;
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {
        testTMMoves = moveIndexes;
    }

    @Override
    public int getTMCount() {
        return originalTMMoves.size();
    }

    @Override
    public int getHMCount() {
        return hmMoves.size();
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        if (testTMHMCompatibility == null) {
            testTMHMCompatibility = new HashMap<>();
            for (Map.Entry<Species, boolean[]> entry : originalTMHMCompatibility.entrySet()) {
                testTMHMCompatibility.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }
        }

        return testTMHMCompatibility;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        testTMHMCompatibility = compatData;
    }

    @Override
    public boolean hasMoveTutors() {
        return !originalMoveTutorMoves.isEmpty();
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        if (testMoveTutorMoves == null) {
            testMoveTutorMoves = new ArrayList<>(originalMoveTutorMoves);
        }
        return testMoveTutorMoves;
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {
        testMoveTutorMoves = moves;
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        if (testMoveTutorCompatibility == null) {
            testMoveTutorCompatibility = new HashMap<>();
            for (Map.Entry<Species, boolean[]> entry : originalMoveTutorCompatibility.entrySet()) {
                testMoveTutorCompatibility.put(entry.getKey(), Arrays.copyOf(entry.getValue(), entry.getValue().length));
            }
        }

        return testMoveTutorCompatibility;
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
        testMoveTutorCompatibility = compatData;
    }

    @Override
    public boolean canChangeTrainerText() {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getTrainerNames() {
        throw new NotImplementedException();
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        throw new NotImplementedException();
    }

    @Override
    public TrainerNameMode trainerNameMode() {
        throw new NotImplementedException();
    }

    @Override
    public int maxTrainerNameLength() {
        throw new NotImplementedException();
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getTrainerClassNames() {
        throw new NotImplementedException();
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        throw new NotImplementedException();
    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        throw new NotImplementedException();
    }

    @Override
    public int maxTrainerClassNameLength() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        throw new NotImplementedException();
    }

    @Override
    public ItemList getAllowedItems() {
        throw new NotImplementedException();
    }

    @Override
    public ItemList getNonBadItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getEvolutionItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getXItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getUniqueNoSellItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getRegularShopItems() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getOPShopItems() {
        throw new NotImplementedException();
    }

    @Override
    public String[] getItemNames() {
        return itemNames.toArray(new String[0]);
    }

    @Override
    public List<Integer> getRequiredFieldTMs() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getCurrentFieldTMs() {
        throw new NotImplementedException();
    }

    @Override
    public void setFieldTMs(List<Integer> fieldTMs) {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getRegularFieldItems() {
        throw new NotImplementedException();
    }

    @Override
    public void setRegularFieldItems(List<Integer> items) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasShopSupport() {
        throw new NotImplementedException();
    }

    @Override
    public Map<Integer, Shop> getShopItems() {
        throw new NotImplementedException();
    }

    @Override
    public void setShopItems(Map<Integer, Shop> shopItems) {
        throw new NotImplementedException();
    }

    @Override
    public void setBalancedShopPrices() {
        throw new NotImplementedException();
    }

    @Override
    public List<PickupItem> getPickupItems() {
        throw new NotImplementedException();
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        throw new NotImplementedException();
    }

    @Override
    public List<IngameTrade> getIngameTrades() {
        throw new NotImplementedException();
    }

    @Override
    public void setIngameTrades(List<IngameTrade> trades) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasDVs() {
        throw new NotImplementedException();
    }

    @Override
    public int maxTradeNicknameLength() {
        throw new NotImplementedException();
    }

    @Override
    public int maxTradeOTNameLength() {
        throw new NotImplementedException();
    }

    @Override
    public void removeImpossibleEvolutions(Settings settings) {
        throw new NotImplementedException();
    }

    @Override
    public void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel) {
        throw new NotImplementedException();
    }

    @Override
    public void makeEvolutionsEasier(Settings settings) {
        throw new NotImplementedException();
    }

    @Override
    public Map<Species, List<Evolution>> getPreImprovedEvolutions() {
        throw new NotImplementedException();
    }

    @Override
    public boolean altFormesCanHaveDifferentEvolutions() {
        return altFormesCanHaveDifferentEvolutions;
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getIllegalMoves() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getFieldMoves() {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        throw new NotImplementedException();
    }

    @Override
    public boolean isYellow() {
        return isYellow;
    }

    @Override
    public boolean isORAS() {
        return isORAS;
    }

    @Override
    public boolean isUSUM() {
        return isUSUM;
    }

    @Override
    public boolean hasMultiplePlayerCharacters() {
        throw new NotImplementedException();
    }

    @Override
    public String getROMName() {
        throw new NotImplementedException();
    }

    @Override
    public String getROMCode() {
        throw new NotImplementedException();
        //What even is this...?
    }

    @Override
    public int getROMType() {
        return romType;
    }

    @Override
    public String getSupportLevel() {
        throw new NotImplementedException();
    }

    @Override
    public String getDefaultExtension() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public int internalStringLength(String string) {
        throw new NotImplementedException();
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        throw new NotImplementedException();
    }

    @Override
    public int generationOfPokemon() {
        return generation;
    }

    @Override
    public int miscTweaksAvailable() {
        throw new NotImplementedException();
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        throw new NotImplementedException();
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        throw new NotImplementedException();
    }

    @Override
    public void setPCPotionItem(int itemID) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasFunctionalFormes() {
        throw new NotImplementedException();
    }

    @Override
    public SpeciesSet getBannedFormesForTrainerPokemon() {
        if(testBannedForTrainers == null) {
            testBannedForTrainers = SpeciesSet.unmodifiable(copySpeciesSet(originalBannedForTrainers));
        }
        return testBannedForTrainers;
    }

    @Override
    public boolean hasPokemonPaletteSupport() {
        throw new NotImplementedException();
    }

    @Override
    public boolean pokemonPaletteSupportIsPartial() {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        throw new NotImplementedException();
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack playerGraphics, Settings.PlayerCharacterMod toReplace) {
        throw new NotImplementedException();
    }

    @Override
    public boolean hasPokemonImageGetter() {
        throw new NotImplementedException();
    }

    @Override
    public PokemonImageGetter createPokemonImageGetter(Species pk) {
        throw new NotImplementedException();
    }

    @Override
    public String getPaletteFilesID() {
        throw new NotImplementedException();
    }

    @Override
    public List<BufferedImage> getAllPokemonImages() {
        throw new NotImplementedException();
    }

    @Override
    public void savePokemonPalettes() {
        throw new NotImplementedException();
    }

    @Override
    public TypeService getTypeService() {
        return new TypeService(this);
    }

    @Override
    public TypeTable getTypeTable() {
        return new TypeTable(testTypeTable != null ? testTypeTable : originalTypeTable);
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        testTypeTable = typeTable;
    }

    @Override
    public boolean hasTypeEffectivenessSupport() {
        return hasTypeEffectivenessSupport;
    }
}
