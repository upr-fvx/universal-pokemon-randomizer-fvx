package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.constants.Gen4Constants;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.graphics.packs.CustomPlayerGraphics;
import com.dabomstew.pkromio.graphics.packs.GraphicsPack;
import com.dabomstew.pkromio.romhandlers.AbstractRomHandler;
import com.dabomstew.pkromio.romhandlers.PokemonImageGetter;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romhandlers.romentries.RomEntry;
import com.dabomstew.pkromio.services.RestrictedSpeciesService;
import com.dabomstew.pkromio.services.TypeService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

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
    private final boolean hasMoveTutors;
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

    //TMs/HMs
    private final boolean originalIsTMsReusable;
    private boolean testIsTMsReusable;
    private final boolean canTMsBeHeld;

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

    //Misc tweaks
    private final int miscTweaksAvailable;

    //Starters
    private final List<Species> originalStarters;
    private List<Species> testStarters = null;
    private final boolean hasStarterAltFormes;

    //Items
    private final List<Item> items;
    private final Set<Item> originalAllowedItems;
    private final Set<Item> evolutionItems;
    private final Set<Item> xItems;
    private final Set<Item> regularShopItems;
    private final Set<Item> opShopItems;
    private final Set<Item> uniqueNoSellItems;

    //Field Items
    private final Set<Item> requiredFieldTMs;
    private final List<Item> originalFieldItems;
    private List<Item> testFieldItems;

    //Pickup Items
    private final List<PickupItem> originalPickupItems;
    private List<PickupItem> testPickupItems;

    //Shops
    private final List<Shop> originalShops;
    private List<Shop> testShops;

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
    private final boolean canAddPokemonToBossTrainers;
    private final boolean canAddPokemonToImportantTrainers;
    private final boolean canAddPokemonToRegularTrainers;

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
        hasMoveTutors = mockupOf.hasMoveTutors();
        originalMoveTutorMoves = hasMoveTutors() ?
                Collections.unmodifiableList(mockupOf.getMoveTutorMoves()) :
                Collections.emptyList();
        originalTMHMCompatibility = Collections.unmodifiableMap(mockupOf.getTMHMCompatibility());
        originalMoveTutorCompatibility = hasMoveTutors ?
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

        originalIsTMsReusable = mockupOf.isTMsReusable();
        canTMsBeHeld = mockupOf.canTMsBeHeld();

        hasTypeEffectivenessSupport = mockupOf.hasTypeEffectivenessSupport();

        generation = mockupOf.generationOfPokemon();
        romType = mockupOf.getROMType();
        isYellow = mockupOf.isYellow();
        isORAS = mockupOf.isORAS();
        isUSUM = mockupOf.isUSUM();

        miscTweaksAvailable = mockupOf.miscTweaksAvailable();

        originalStarters = Collections.unmodifiableList(mockupOf.getStarters());
        hasStarterAltFormes = mockupOf.hasStarterAltFormes();

        items = Collections.unmodifiableList(mockupOf.getItems());
        originalAllowedItems = items.stream().filter(Objects::nonNull).filter(Item::isAllowed).collect(Collectors.toSet());
        evolutionItems = Collections.unmodifiableSet(mockupOf.getEvolutionItems());
        xItems = Collections.unmodifiableSet(mockupOf.getXItems());
        regularShopItems = Collections.unmodifiableSet(mockupOf.getRegularShopItems());
        opShopItems = Collections.unmodifiableSet(mockupOf.getOPShopItems());
        uniqueNoSellItems = Collections.unmodifiableSet(mockupOf.getMegaStones());

        requiredFieldTMs = Collections.unmodifiableSet(mockupOf.getRequiredFieldTMs());
        originalFieldItems = Collections.unmodifiableList(mockupOf.getFieldItems());

        originalPickupItems = Collections.unmodifiableList(mockupOf.getPickupItems());

        originalShops = Collections.unmodifiableList(mockupOf.getShops());

        originalBannedForTrainers = SpeciesSet.unmodifiable(mockupOf.getBannedFormesForTrainerPokemon());
        originalTrainers = Collections.unmodifiableList(mockupOf.getTrainers());
        mainPlaythroughTrainers = Collections.unmodifiableList(mockupOf.getMainPlaythroughTrainers());
        eliteFourTrainers = Collections.unmodifiableList(mockupOf.getEliteFourTrainers(false));
        challengeModeEliteFourTrainers = Collections.unmodifiableList(mockupOf.getEliteFourTrainers(true));
        gymAndEliteTypeThemes = Collections.unmodifiableMap(mockupOf.getGymAndEliteTypeThemes());
        trainerPokemonAlwaysUseAbility1 = mockupOf.isTrainerPokemonAlwaysUseAbility1();
        trainerPokemonUseBaseFormeAbilities = mockupOf.isTrainerPokemonUseBaseFormeAbilities();
        canAddPokemonToBossTrainers = mockupOf.canAddPokemonToBossTrainers();
        canAddPokemonToImportantTrainers = mockupOf.canAddPokemonToImportantTrainers();
        canAddPokemonToRegularTrainers = mockupOf.canAddPokemonToRegularTrainers();

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
     * Resets all test data, mostly by simply dropping it.
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

        testIsTMsReusable = originalIsTMsReusable;

        testTypeTable = null;

        testStarters = null;

        // Items are passed around by reference a lot, but as we only expect their "allowed" attribute
        // to change, we can (and do) just reset that.
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null) {
                items.get(i).setAllowed(originalAllowedItems.contains(items.get(i)));
            }
        }

        testFieldItems = null;

        testShops = null;

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
            MegaEvolution evoCopy = new MegaEvolution(copy, originalToCopies.get(evolution.getTo()),
                    evolution.isNeedsItem(), evolution.getItem());
            copy.getMegaEvolutionsFrom().add(evoCopy);
            evoCopy.getTo().getMegaEvolutionsTo().add(evoCopy);

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
            Species spec = originalToTest.get(orig.getSpecies());
            copy.setSpecies(spec);
            for(StaticEncounter linked : copy.getLinkedEncounters()) {
                linked.setSpecies(spec);
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
                tp.setSpecies(originalToTest.get(tp.getSpecies()));
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
    public void loadPokemonStats() {
        throw new UnsupportedOperationException();
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
            testSpeciesInOrder.add(0, null); // and this is less sleek haha, O(#numPokemon)
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
            testRSS.setRestrictions(new GenRestrictions());
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
    public List<Item> getStarterHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public void setStarterHeldItems(List<Item> items) {
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
        return canAddPokemonToBossTrainers;
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        return canAddPokemonToImportantTrainers;
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        return canAddPokemonToRegularTrainers;
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
    public List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        throw new NotImplementedException();
    }

    @Override
    public Set<Item> getAllConsumableHeldItems() {
        throw new NotImplementedException();
    }

    @Override
    public Set<Item> getAllHeldItems() {
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
    public boolean isTMsReusable() {
        return testIsTMsReusable;
    }

    @Override
    public boolean canTMsBeHeld() {
        return canTMsBeHeld;
    }

    @Override
    public boolean hasMoveTutors() {
        return hasMoveTutors;
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
    public Set<Item> getEvolutionItems() {
        return evolutionItems;
    }

    @Override
    public Set<Item> getXItems() {
        return xItems;
    }

    @Override
    public Set<Item> getMegaStones() {
        return uniqueNoSellItems;
    }

    @Override
    public Set<Item> getRegularShopItems() {
        return regularShopItems;
    }

    @Override
    public Set<Item> getOPShopItems() {
        return opShopItems;
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public Set<Item> getRequiredFieldTMs() {
        return requiredFieldTMs;
    }

    @Override
    public List<Item> getFieldItems() {
        if (testFieldItems == null) {
            testFieldItems = new ArrayList<>(originalFieldItems);
        }
        return testFieldItems;
    }

    @Override
    public void setFieldItems(List<Item> items) {
        for (int i = 0; i < originalFieldItems.size(); i++) {
            if (items.get(i).isTM() != originalFieldItems.get(i).isTM())
                throw new IllegalArgumentException("TM must replace TM and vice versa.");
        }
        testFieldItems = items;
    }

    @Override
    public boolean hasShopSupport() {
        return !originalShops.isEmpty();
    }

    @Override
    public List<Shop> getShops() {
        if (testShops == null) {
            testShops = new ArrayList<>();
            for (Shop shop : originalShops) {
                testShops.add(new Shop(shop));
            }
        }
        return testShops;
    }

    @Override
    public void setShops(List<Shop> shops) {
        testShops = shops;
    }

    @Override
    public List<Integer> getShopPrices() {
        throw new NotImplementedException();
    }

    @Override
    protected Map<Integer, Integer> getBalancedShopPrices() {
        throw new NotImplementedException();
    }

    @Override
    public void setShopPrices(List<Integer> prices) {
        throw new NotImplementedException();
    }

    @Override
    public List<PickupItem> getPickupItems() {
        if (testPickupItems == null) {
            testPickupItems = new ArrayList<>(originalPickupItems.size());
            for (PickupItem pi : originalPickupItems) {
                testPickupItems.add(new PickupItem(pi));
            }
        }
        return testPickupItems;
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        testPickupItems = pickupItems;
    }

    @Override
    public List<InGameTrade> getInGameTrades() {
        throw new NotImplementedException();
    }

    @Override
    public void setInGameTrades(List<InGameTrade> trades) {
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
    public void removeImpossibleEvolutions(boolean changeMoveEvos) {
        throw new NotImplementedException();
    }

    @Override
    public void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel) {
        throw new NotImplementedException();
    }

    @Override
    public void makeEvolutionsEasier(boolean changeWithOtherEvos) {
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
        return miscTweaksAvailable;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if ((miscTweaksAvailable & tweak.getValue()) > 0) {
            if (tweak == MiscTweak.REUSABLE_TMS) {
                testIsTMsReusable = true;
            } else {
                throw new UnsupportedOperationException("unimplemented");
            }
        }
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        throw new NotImplementedException();
    }

    @Override
    public void setPCPotionItem(Item item) {
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
    public void setCustomPlayerGraphics(CustomPlayerGraphics customPlayerGraphics) {
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
