package test.randomizers;

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.PokemonImageGetter;
import com.dabomstew.pkrandom.romhandlers.RomHandler;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import com.dabomstew.pkrandom.services.TypeService;

import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.*;

/**
 * A special "dummy" romHandler which copies data from an existing romHandler.
 *
 */
public class TestRomHandler implements RomHandler {

    private final TypeTable originalTypeTable;
    private TypeTable testTypeTable = null;
    private final SpeciesSet originalSpeciesInclFormes;
    private SpeciesSet testSpeciesInclFormes = null;
    Map<Species, Species> originalToTest = null;
    private final List<EncounterArea> originalEncounters;
    List<EncounterArea> testEncounters = null;
    private final int generation;

    /**
     * Given a loaded RomHandler, creates a mockup TestRomHandler by extracting the data from it.
     * @param mockupOf A loaded RomHandler to create a mockup of.
     */
    public TestRomHandler(RomHandler mockupOf) {
        originalTypeTable = new TypeTable(mockupOf.getTypeTable());
        originalSpeciesInclFormes = SpeciesSet.unmodifiable(mockupOf.getSpeciesInclFormes());
        originalEncounters = mockupOf.getEncounters(true);
        generation = mockupOf.generationOfPokemon();
    }

    /**
     * Prepares for testing by copying all data that must be passed by reference,
     * and all data which references that data.
     */
    public void prepare() {
        testSpeciesInclFormes = deepCopySpeciesSet(originalSpeciesInclFormes);
    }

    /**
     * Resets all data to the original data gotten from the RomHandler.
     */
    public void reset() {
        testTypeTable = null;
        testEncounters = null;
        testSpeciesInclFormes = null;
        originalToTest = null;
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
    private static void copySpeciesRelations(Species original, Map<Species, Species> originalToCopies) {
        Species copy = originalToCopies.get(original);
        if(!original.isBaseForme()) {
            copy.setBaseForme(originalToCopies.get(original.getBaseForme()));
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
    public boolean isRomValid() {
        throw new UnsupportedOperationException("File functions cannot be called in TestRomHandler");
    }

    @Override
    public List<Species> getSpecies() {
        return null;
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        return null;
    }

    @Override
    public SpeciesSet getAltFormes() {
        return null;
    }

    @Override
    public SpeciesSet getSpeciesSet() {
        return testSpeciesInclFormes;
    }

    @Override
    public SpeciesSet getSpeciesSetInclFormes() {
        return null;
    }

    @Override
    public List<MegaEvolution> getMegaEvolutions() {
        return null;
    }

    @Override
    public Species getAltFormeOfSpecies(Species pk, int forme) {
        return null;
    }

    @Override
    public SpeciesSet getIrregularFormes() {
        return null;
    }

    @Override
    public RestrictedSpeciesService getRestrictedSpeciesService() {
        return new RestrictedSpeciesService(this);
    }

    @Override
    public void removeEvosForPokemonPool() {

    }

    @Override
    public List<Species> getStarters() {
        return null;
    }

    @Override
    public boolean setStarters(List<Species> newStarters) {
        return false;
    }

    @Override
    public boolean hasStarterAltFormes() {
        return false;
    }

    @Override
    public int starterCount() {
        return 0;
    }

    @Override
    public boolean hasStarterTypeTriangleSupport() {
        return false;
    }

    @Override
    public boolean supportsStarterHeldItems() {
        return false;
    }

    @Override
    public List<Integer> getStarterHeldItems() {
        return null;
    }

    @Override
    public void setStarterHeldItems(List<Integer> items) {

    }

    @Override
    public int abilitiesPerSpecies() {
        return 0;
    }

    @Override
    public int highestAbilityIndex() {
        return 0;
    }

    @Override
    public String abilityName(int number) {
        return null;
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        return null;
    }

    @Override
    public List<Integer> getUselessAbilities() {
        return null;
    }

    @Override
    public int getAbilityForTrainerPokemon(TrainerPokemon tp) {
        return 0;
    }

    @Override
    public boolean hasMegaEvolutions() {
        return false;
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        return null;
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        return null;
    }

    @Override
    public SpeciesSet getMainGameWildPokemonSpecies(boolean useTimeOfDay) {
        return null;
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounters) {

    }

    @Override
    public boolean hasTimeBasedEncounters() {
        return false;
    }

    @Override
    public boolean hasWildAltFormes() {
        return false;
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        return null;
    }

    @Override
    public void enableGuaranteedPokemonCatching() {

    }

    @Override
    public List<Trainer> getTrainers() {
        return null;
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        return null;
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        return null;
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        return null;
    }

    @Override
    public void setTrainers(List<Trainer> trainerData) {

    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        return false;
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        return false;
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        return false;
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        return false;
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        return false;
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        return false;
    }

    @Override
    public List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        return null;
    }

    @Override
    public List<Integer> getAllConsumableHeldItems() {
        return null;
    }

    @Override
    public List<Integer> getAllHeldItems() {
        return null;
    }

    @Override
    public boolean hasRivalFinalBattle() {
        return false;
    }

    @Override
    public void makeDoubleBattleModePossible() {

    }

    @Override
    public boolean hasPhysicalSpecialSplit() {
        return false;
    }

    @Override
    public List<Move> getMoves() {
        return null;
    }

    @Override
    public int getPerfectAccuracy() {
        return 0;
    }

    @Override
    public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
        return null;
    }

    @Override
    public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {

    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        return null;
    }

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        return null;
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {

    }

    @Override
    public boolean supportsFourStartingMoves() {
        return false;
    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        return null;
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        return false;
    }

    @Override
    public boolean canChangeStaticPokemon() {
        return false;
    }

    @Override
    public boolean hasStaticAltFormes() {
        return false;
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        return null;
    }

    @Override
    public boolean forceSwapStaticMegaEvos() {
        return false;
    }

    @Override
    public boolean hasMainGameLegendaries() {
        return false;
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        return null;
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        return null;
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {

    }

    @Override
    public boolean hasStaticMusicFix() {
        return false;
    }

    @Override
    public boolean hasTotemPokemon() {
        return false;
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        return null;
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {

    }

    @Override
    public List<Integer> getTMMoves() {
        return null;
    }

    @Override
    public List<Integer> getHMMoves() {
        return null;
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {

    }

    @Override
    public int getTMCount() {
        return 0;
    }

    @Override
    public int getHMCount() {
        return 0;
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        return null;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {

    }

    @Override
    public boolean hasMoveTutors() {
        return false;
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        return null;
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {

    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        return null;
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {

    }

    @Override
    public boolean canChangeTrainerText() {
        return false;
    }

    @Override
    public List<String> getTrainerNames() {
        return null;
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {

    }

    @Override
    public TrainerNameMode trainerNameMode() {
        return null;
    }

    @Override
    public int maxTrainerNameLength() {
        return 0;
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        return 0;
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        return null;
    }

    @Override
    public List<String> getTrainerClassNames() {
        return null;
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {

    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        return false;
    }

    @Override
    public int maxTrainerClassNameLength() {
        return 0;
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        return null;
    }

    @Override
    public ItemList getAllowedItems() {
        return null;
    }

    @Override
    public ItemList getNonBadItems() {
        return null;
    }

    @Override
    public List<Integer> getEvolutionItems() {
        return null;
    }

    @Override
    public List<Integer> getXItems() {
        return null;
    }

    @Override
    public List<Integer> getUniqueNoSellItems() {
        return null;
    }

    @Override
    public List<Integer> getRegularShopItems() {
        return null;
    }

    @Override
    public List<Integer> getOPShopItems() {
        return null;
    }

    @Override
    public String[] getItemNames() {
        return new String[0];
    }

    @Override
    public List<Integer> getRequiredFieldTMs() {
        return null;
    }

    @Override
    public List<Integer> getCurrentFieldTMs() {
        return null;
    }

    @Override
    public void setFieldTMs(List<Integer> fieldTMs) {

    }

    @Override
    public List<Integer> getRegularFieldItems() {
        return null;
    }

    @Override
    public void setRegularFieldItems(List<Integer> items) {

    }

    @Override
    public boolean hasShopSupport() {
        return false;
    }

    @Override
    public Map<Integer, Shop> getShopItems() {
        return null;
    }

    @Override
    public void setShopItems(Map<Integer, Shop> shopItems) {

    }

    @Override
    public void setBalancedShopPrices() {

    }

    @Override
    public List<PickupItem> getPickupItems() {
        return null;
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {

    }

    @Override
    public List<IngameTrade> getIngameTrades() {
        return null;
    }

    @Override
    public void setIngameTrades(List<IngameTrade> trades) {

    }

    @Override
    public boolean hasDVs() {
        return false;
    }

    @Override
    public int maxTradeNicknameLength() {
        return 0;
    }

    @Override
    public int maxTradeOTNameLength() {
        return 0;
    }

    @Override
    public void removeImpossibleEvolutions(Settings settings) {

    }

    @Override
    public void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel) {

    }

    @Override
    public void makeEvolutionsEasier(Settings settings) {

    }

    @Override
    public void removeTimeBasedEvolutions() {

    }

    @Override
    public Set<EvolutionUpdate> getImpossibleEvoUpdates() {
        return null;
    }

    @Override
    public Set<EvolutionUpdate> getEasierEvoUpdates() {
        return null;
    }

    @Override
    public Set<EvolutionUpdate> getTimeBasedEvoUpdates() {
        return null;
    }

    @Override
    public boolean altFormesCanHaveDifferentEvolutions() {
        return false;
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        return null;
    }

    @Override
    public List<Integer> getIllegalMoves() {
        return null;
    }

    @Override
    public List<Integer> getFieldMoves() {
        return null;
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        return null;
    }

    @Override
    public boolean isYellow() {
        return false;
    }

    @Override
    public boolean isORAS() {
        return false;
    }

    @Override
    public boolean isUSUM() {
        return false;
    }

    @Override
    public boolean hasMultiplePlayerCharacters() {
        return false;
    }

    @Override
    public String getROMName() {
        return null;
    }

    @Override
    public String getROMCode() {
        return null;
    }

    @Override
    public int getROMType() {
        return 0;
    }

    @Override
    public String getSupportLevel() {
        return null;
    }

    @Override
    public String getDefaultExtension() {
        return null;
    }

    @Override
    public int internalStringLength(String string) {
        return 0;
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        return false;
    }

    @Override
    public int generationOfPokemon() {
        return 0;
    }

    @Override
    public void writeCheckValueToROM(int value) {

    }

    @Override
    public int miscTweaksAvailable() {
        return 0;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {

    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        return false;
    }

    @Override
    public void setPCPotionItem(int itemID) {

    }

    @Override
    public boolean hasFunctionalFormes() {
        return false;
    }

    @Override
    public SpeciesSet getBannedFormesForTrainerPokemon() {
        return null;
    }

    @Override
    public boolean hasPokemonPaletteSupport() {
        return false;
    }

    @Override
    public boolean pokemonPaletteSupportIsPartial() {
        return false;
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        return false;
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack playerGraphics, Settings.PlayerCharacterMod toReplace) {

    }

    @Override
    public boolean hasPokemonImageGetter() {
        return false;
    }

    @Override
    public PokemonImageGetter createPokemonImageGetter(Species pk) {
        return null;
    }

    @Override
    public String getPaletteFilesID() {
        return null;
    }

    @Override
    public void dumpAllPokemonImages() {

    }

    @Override
    public List<BufferedImage> getAllPokemonImages() {
        return null;
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
        return false;
    }
}
