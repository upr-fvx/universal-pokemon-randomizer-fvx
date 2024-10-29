package test.randomizers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.AbstractRomHandler;
import com.dabomstew.pkrandom.romhandlers.PokemonImageGetter;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.romhandlers.romentries.RomEntry;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import com.dabomstew.pkrandom.services.TypeService;
import java.lang.UnsupportedOperationException;

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

    //Encounters
    private final List<EncounterArea> originalEncounters;
    List<EncounterArea> testEncounters = null;
    private final boolean hasTimeBasedEncounters;
    private final boolean hasWildAltFormes;
    private final SpeciesSet originalBannedForWild;

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

    /**
     * Given a loaded RomHandler, creates a mockup TestRomHandler by extracting the data from it.
     * @param mockupOf A loaded RomHandler to create a mockup of.
     */
    public TestRomHandler(RomHandler mockupOf) {
        originalTypeTable = new TypeTable(mockupOf.getTypeTable());
        originalSpeciesInclFormes = SpeciesSet.unmodifiable(mockupOf.getSpeciesInclFormes());
        originalEncounters = mockupOf.getEncounters(true);
        generation = mockupOf.generationOfPokemon();
        hasTimeBasedEncounters = mockupOf.hasTimeBasedEncounters();
        hasWildAltFormes = mockupOf.hasWildAltFormes();
        originalBannedForWild = SpeciesSet.unmodifiable(mockupOf.getBannedForWildEncounters());
        originalIrregularFormes = SpeciesSet.unmodifiable(mockupOf.getIrregularFormes());

        romType = mockupOf.getROMType();
        isYellow = mockupOf.isYellow();
        isORAS = mockupOf.isORAS();
        isUSUM = mockupOf.isUSUM();

        hasTypeEffectivenessSupport = mockupOf.hasTypeEffectivenessSupport();

        originalStarters = Collections.unmodifiableList(mockupOf.getStarters());
        hasStarterAltFormes = mockupOf.hasStarterAltFormes();
    }

    /**
     * Prepares for testing by making a deep copy of all Species,
     * which are passed by reference and therefore cannot otherwise be reset.
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

        testEncounters = null;

        testTypeTable = null;

        testStarters = null;
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
        copy.setFormeSpriteIndex(original.getFormeSpriteIndex());
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
            evoCopy.setLevel(evolution.getLevel());
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getStarterHeldItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStarterHeldItems(List<Integer> items) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int abilitiesPerSpecies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int highestAbilityIndex() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String abilityName(int number) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getUselessAbilities() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAbilityForTrainerPokemon(TrainerPokemon tp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMegaEvolutions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        if(!useTimeOfDay) {
            throw new UnsupportedOperationException();
        }

        if(testEncounters == null) {
            testEncounters = deepCopyEncounters(originalEncounters);
        }

        return testEncounters;
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Trainer> getTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTrainers(List<Trainer> trainerData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getAllConsumableHeldItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getAllHeldItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasRivalFinalBattle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeDoubleBattleModePossible() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPhysicalSpecialSplit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Move> getMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPerfectAccuracy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsFourStartingMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canChangeStaticPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasStaticAltFormes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean forceSwapStaticMegaEvos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMainGameLegendaries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasStaticMusicFix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasTotemPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getTMMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getHMMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTMCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHMCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasMoveTutors() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canChangeTrainerText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getTrainerNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TrainerNameMode trainerNameMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxTrainerNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getTrainerClassNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxTrainerClassNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemList getAllowedItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemList getNonBadItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getEvolutionItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getXItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getUniqueNoSellItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getRegularShopItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getOPShopItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getItemNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getRequiredFieldTMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getCurrentFieldTMs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFieldTMs(List<Integer> fieldTMs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getRegularFieldItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRegularFieldItems(List<Integer> items) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasShopSupport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, Shop> getShopItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShopItems(Map<Integer, Shop> shopItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBalancedShopPrices() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PickupItem> getPickupItems() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IngameTrade> getIngameTrades() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIngameTrades(List<IngameTrade> trades) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasDVs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxTradeNicknameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int maxTradeOTNameLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeImpossibleEvolutions(Settings settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void makeEvolutionsEasier(Settings settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTimeBasedEvolutions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EvolutionUpdate> getImpossibleEvoUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EvolutionUpdate> getEasierEvoUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<EvolutionUpdate> getTimeBasedEvoUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean altFormesCanHaveDifferentEvolutions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getIllegalMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getFieldMoves() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public String getROMName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getROMCode() {
        throw new UnsupportedOperationException();
        //What even is this...?
    }

    @Override
    public int getROMType() {
        return romType;
    }

    @Override
    public String getSupportLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultExtension() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public int internalStringLength(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int generationOfPokemon() {
        return generation;
    }

    @Override
    public void writeCheckValueToROM(int value) {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public int miscTweaksAvailable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPCPotionItem(int itemID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasFunctionalFormes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpeciesSet getBannedFormesForTrainerPokemon() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPokemonPaletteSupport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pokemonPaletteSupportIsPartial() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack playerGraphics, Settings.PlayerCharacterMod toReplace) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPokemonImageGetter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PokemonImageGetter createPokemonImageGetter(Species pk) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPaletteFilesID() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BufferedImage> getAllPokemonImages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void savePokemonPalettes() {
        throw new UnsupportedOperationException();
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
