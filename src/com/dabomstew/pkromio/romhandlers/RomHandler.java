package com.dabomstew.pkromio.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.graphics.packs.GraphicsPack;
import com.dabomstew.pkromio.services.RestrictedSpeciesService;
import com.dabomstew.pkromio.services.TypeService;

import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for direct handling a Rom/game file, and the data therein.
 * <br><br>
 * After a Rom has been loaded with {@link #loadRom(String)}, a number of data types reflecting the contents of the
 * Rom can be acquired through getters (e.g. {@link #getSpecies()}, {@link #getStarters()}, {@link #getTrainers()}).
 * Most of the corresponding data also have setters which update the contents of the Rom (e.g.
 * {@link #setStarters(List)}, {@link #setTrainers(List)}), but some (most notably the {@link Species} data from
 * {@link #getSpecies()}) are instead updated simply by editing the object returned by the setter.
 * An edited Rom can be saved with {@link #saveRom(String, long, boolean)}.
 * <br><br>
 * Some methods giving extra context to the main data are also provided (e.g. {@link #hasRivalFinalBattle()},
 * {@link #hasPhysicalSpecialSplit()}, {@link #abilitiesPerSpecies()}).
 * <br><br>
 * Though given a Rom, the RomHandler might not be able to get/set all kinds of data. Either because the Rom itself
 * does not support the data type (there are no Starter held items in Red), or because the RomHandler itself does not
 * implement it. For these non-universal data types, boolean methods are provided to report which ones are supported
 * (e.g. {@link #supportsStarterHeldItems()}, {@link #hasShopSupport()}, {@link #canChangeStaticPokemon()}).
 * <br><br>
 * Finally, the RomHandler is responsible for giving general info about the Rom (e.g. {@link #getROMName()},
 * {@link #getROMType()}, {@link #printRomDiagnostics(PrintStream)}), and the loading process (e.g.
 * {@link #loadedFilename()}, {@link #hasGameUpdateLoaded()}).
 */
public interface RomHandler {

    abstract class Factory {
        public abstract RomHandler create();

        public abstract boolean isLoadable(String filename);
    }

    // =======================
    // Basic load/save methods
    // =======================

    boolean loadRom(String filename);
    
    boolean saveRom(String filename, long seed, boolean saveAsDirectory);

    String loadedFilename();

    // =============================================================
    // Methods relating to game updates for the 3DS and Switch games
    // =============================================================

    boolean hasGameUpdateLoaded();

    boolean loadGameUpdate(String filename);

    void removeGameUpdate();

    String getGameUpdateVersion();

    // ===========
    // Log methods
    // ===========

    void printRomDiagnostics(PrintStream logStream);

    /**
     * Returns whether the Rom is valid/has correct checksums, and logs relevant information.
     * If logStream is null, nothing is logged.
     */
    boolean isRomValid(PrintStream logStream);

    // ======================================================
    // Methods for retrieving a list of Species objects.
    // Note that for many of these lists, index 0 is null.
    // Instead, you use index on the species' National Dex ID
    // ======================================================

    List<Species> getSpecies();

    List<Species> getSpeciesInclFormes();

    SpeciesSet getAltFormes();
    
    SpeciesSet getSpeciesSet();
    
    SpeciesSet getSpeciesSetInclFormes();

    List<MegaEvolution> getMegaEvolutions();

    Species getAltFormeOfSpecies(Species base, int forme);
    //TODO: move this to Species

    SpeciesSet getIrregularFormes();

    RestrictedSpeciesService getRestrictedSpeciesService();

    // ==================================
    // Methods to set up Gen Restrictions
    // ==================================

    /**
     * When using {@link RestrictedSpeciesService} to restrict which Pokémon
     * may appear, we want to prevent allowed Pokémon from evolving (or breeding)
     * into ones that are not. This does that.
     */
    void removeEvosForPokemonPool();

    // ===============
    // Starter Pokemon
    // ===============

    List<Species> getStarters();

    boolean setStarters(List<Species> newStarters);

    boolean hasStarterAltFormes();

    int starterCount();

    boolean hasStarterTypeTriangleSupport();

    boolean supportsStarterHeldItems();

    List<Item> getStarterHeldItems();

    void setStarterHeldItems(List<Item> items);

    /**
     * If the assigned starters have been chosen according to a new type triangle,
     * sets the types of this triangle so that other aspects of the game can be changed to match.
     * @param triangle The three types chosen, in the same order as the starters.
     */
    void setStarterTypeTriangle(List<Type> triangle);

    /**
     * Gets an unmodifiable copy of the starter type triangle in use.
     * If no type triangle has been set, returns FIRE_WATER_GRASS,
     * even if the starters have been changed from those types.
     * @return The three types chosen, in the same order as the starters.
     */
    List<Type> getStarterTypeTriangle();

    /**
     * Returns true if a custom type triangle has been set, false otherwise.
     */
    boolean isTypeTriangleChanged();

    /**
     * Returns the standard type triangle (Fire, Water, Grass) in the order used by this game.
     * @return The standard type triangle in the correct order.
     */
    List<Type> getStandardTypeTriangle();

    // =================
    // Pokemon Abilities
    // =================

    int abilitiesPerSpecies();

    int highestAbilityIndex();

    String abilityName(int number);

    Map<Integer,List<Integer>> getAbilityVariations();

    List<Integer> getUselessAbilities();

    int getAbilityForTrainerPokemon(TrainerPokemon tp);

    /**
     * Returns true if {@link TrainerPokemon} in this game always use ability 1.
     */
    boolean isTrainerPokemonAlwaysUseAbility1();

    /**
     * In some games, when alt formes are used as {@link TrainerPokemon},
     * they don't use their own abilities but those of their base forme.<br>
     * Returns true if that is the case for this game.
     */
    boolean isTrainerPokemonUseBaseFormeAbilities();

    boolean hasMegaEvolutions();

    // ============
    // Wild Pokemon
    // ============

    List<EncounterArea> getEncounters(boolean useTimeOfDay);

    /**
     * Returns a list identical to {@link #getEncounters(boolean)}, except it is sorted according to when in the game
     * the player is expected to go to the location of each {@link EncounterArea}.<br>
     * E.g. {@link EncounterArea}s at early routes come early, and victory road and post-game locations ones are at
     * the end.<br>
     * (if the order has been implemented; the default implementation does not sort)
     */
    List<EncounterArea> getSortedEncounters(boolean useTimeOfDay);

    /**
     *
     * @param useTimeOfDay
     * @return A new SpeciesSet containing all wild Species found in the main game.
     */
    SpeciesSet getMainGameWildPokemonSpecies(boolean useTimeOfDay);

    void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounters);

    default boolean hasEncounterLocations() {
        return false;
    }

    default boolean hasMapIndices() {
        return false;
    }

    boolean hasTimeBasedEncounters();

    boolean hasWildAltFormes();

    SpeciesSet getBannedForWildEncounters();

    /**
     * Returns whether the game supports wild encounters having guaranteed held items.
     */
    boolean hasGuaranteedWildHeldItems();

    /**
     * Returns whether the game supports wild encounters having (rare) held items in dark grass only.
     */
    boolean hasDarkGrassHeldItems();

    void enableGuaranteedPokemonCatching();

    // ===============
    // Trainer Pokemon
    // ===============

    List<Trainer> getTrainers();

    List<Integer> getMainPlaythroughTrainers();

    /**
     * Returns a list of the indices (in the main trainer list via {@link #getTrainers()}) of the trainers
     * consisting of the non-rematch Elite 4 challenge, including the Champion (or Ghetsis in BW1). <br>
     * If isChallengeMode is true, it returns the indexes for the Challenge Mode e4+champion (only in BW2).
     */
    List<Integer> getEliteFourTrainers(boolean isChallengeMode);

    Map<String, Type> getGymAndEliteTypeThemes();

    void setTrainers(List<Trainer> trainerData);

    boolean canAddPokemonToBossTrainers();

    boolean canAddPokemonToImportantTrainers();

    boolean canAddPokemonToRegularTrainers();

    boolean canAddHeldItemsToBossTrainers();

    boolean canAddHeldItemsToImportantTrainers();

    boolean canAddHeldItemsToRegularTrainers();

    /**
     * Returns a {@link List} of {@link Item}s that would be sensible/good for a {@link TrainerPokemon} to hold.
     * The List allows duplication of Items to make them more likely when chosen at random.
     */
    List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves);

    Set<Item> getAllConsumableHeldItems();

    Set<Item> getAllHeldItems();

    boolean hasRivalFinalBattle();

    void makeDoubleBattleModePossible();

    // =========
    // Move Data
    // =========

    boolean hasPhysicalSpecialSplit();

    // return all the moves valid in this game.
    List<Move> getMoves();

    int getPerfectAccuracy();

    // ================
    // Pokemon Movesets
    // ================

    Map<Integer, List<MoveLearnt>> getMovesLearnt();

    void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets);

    List<Integer> getMovesBannedFromLevelup();

    Map<Integer, List<Integer>> getEggMoves();

    void setEggMoves(Map<Integer, List<Integer>> eggMoves);

    boolean supportsFourStartingMoves();

    // ==============
    // Static Pokemon
    // ==============

    List<StaticEncounter> getStaticPokemon();

    boolean setStaticPokemon(List<StaticEncounter> staticPokemon);

    boolean canChangeStaticPokemon();

    boolean hasStaticAltFormes();

    SpeciesSet getBannedForStaticPokemon();

    boolean forceSwapStaticMegaEvos();

    boolean hasMainGameLegendaries();

    List<Integer> getMainGameLegendaries();

    List<Integer> getSpecialMusicStatics();

    void applyCorrectStaticMusic(Map<Integer,Integer> specialMusicStaticChanges);

    boolean hasStaticMusicFix();

    // =============
    // Totem Pokemon
    // =============

    boolean hasTotemPokemon();

    List<TotemPokemon> getTotemPokemon();

    void setTotemPokemon(List<TotemPokemon> totemPokemon);

    // =========
    // TMs & HMs
    // =========

    List<Integer> getTMMoves();

    List<Integer> getHMMoves();

    void setTMMoves(List<Integer> moveIndexes);

    int getTMCount();

    int getHMCount();

    boolean isTMsReusable();

    boolean canTMsBeHeld();

    /**
     * Get TM/HM compatibility data from this rom. The result should contain a
     * boolean array for each Species indexed as such:
     * <br>
     * 0: blank (false) / 1 - (getTMCount()) : TM compatibility /
     * (getTMCount()+1) - (getTMCount()+getHMCount()) - HM compatibility
     * 
     * @return Map of TM/HM compatibility
     */
    Map<Species, boolean[]> getTMHMCompatibility();

    void setTMHMCompatibility(Map<Species, boolean[]> compatData);

    // ===========
    // Move Tutors
    // ===========

    boolean hasMoveTutors();

    List<Integer> getMoveTutorMoves();

    void setMoveTutorMoves(List<Integer> moves);

    /**
     * Gets the Move Tutor compatibility for this game. 
     * If {@link #hasMoveTutors()}==false, there is no guarantee this method will work.
     */
    Map<Species, boolean[]> getMoveTutorCompatibility();

    void setMoveTutorCompatibility(Map<Species, boolean[]> compatData);

    // =============
    // Trainer Names
    // =============

    boolean canChangeTrainerText();

    List<String> getTrainerNames();

    void setTrainerNames(List<String> trainerNames);

    enum TrainerNameMode {
        SAME_LENGTH, MAX_LENGTH, MAX_LENGTH_WITH_CLASS
    }

    TrainerNameMode trainerNameMode();

    // Returns this with or without the class
    int maxTrainerNameLength();

    // Only relevant for gen2, which has fluid trainer name length but
    // only a certain amount of space in the ROM bank.
    int maxSumOfTrainerNameLengths();

    // Only needed if above mode is "MAX LENGTH WITH CLASS"
    List<Integer> getTCNameLengthsByTrainer();

    // ===============
    // Trainer Classes
    // ===============

    List<String> getTrainerClassNames();

    void setTrainerClassNames(List<String> trainerClassNames);

    boolean fixedTrainerClassNamesLength();

    int maxTrainerClassNameLength();

    List<Integer> getDoublesTrainerClasses();

    // =====
    // Items
    // =====

    /**
     * Returns a {@link List} of all {@link Item}s in the game, in order. Some of these may be unused/bug items.
     * The first element is always null, both to offset the other elements and to represent the "nothing" item.
     */
    List<Item> getItems();

    Set<Item> getAllowedItems();

    Set<Item> getNonBadItems();

    Set<Item> getEvolutionItems();

    Set<Item> getXItems();

    Set<Item> getMegaStones();

    Set<Item> getRegularShopItems();

    Set<Item> getOPShopItems();

    // ===========
    // Field Items
    // ===========

    // "Required" seems to mean that you can only get the TM as a field item in vanilla.
    // Thus, when randomizing field items they must be included, or otherwise become unavailable.
    Set<Item> getRequiredFieldTMs();

    List<Item> getFieldItems();

    /**
     * Sets field items. Due to an old requirement from the Randomizer (unclear where it comes from, might be a design
     * decision, might be necessary for some Gens to work), TMs must be replaced by TMs, and non-TMs by non-TMs.
     * This means the TM-ness at each given index, must be the same as in the returned value of {@link #getFieldItems()}.
     * If not followed, this will throw an {@link IllegalArgumentException}.
     */
    void setFieldItems(List<Item> items);

    // ============
    // Special Shops
    // =============

    boolean hasShopSupport();

    /**
     * Returns {@code true} if, relative to their contents gotten through {@link #getShops()},
     * the number of {@link Item Items} in {@link Shop Shops} may be changed before setting them with
     * {@link #setShops(List)}.<br>
     * More precisely, returns {@code true} if any amount of Items may be removed,
     * and up to 1 Item may be added per Shop.
     */
    boolean canChangeShopSizes();

    List<Shop> getShops();

    void setShops(List<Shop> shops);

    List<Integer> getShopPrices();

    void setShopPrices(List<Integer> prices);

    void setBalancedShopPrices();

    // ============
    // Pickup Items
    // ============

    List<PickupItem> getPickupItems();

    void setPickupItems(List<PickupItem> pickupItems);

    // ==============
    // In-Game Trades
    // ==============

    List<InGameTrade> getInGameTrades();

    void setInGameTrades(List<InGameTrade> trades);

    boolean hasDVs();

    int maxTradeNicknameLength();

    int maxTradeOTNameLength();

    // ==================
    // Pokemon Evolutions
    // ==================

    void removeImpossibleEvolutions(boolean changeMoveEvos);

    void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel);

    void makeEvolutionsEasier(boolean changeWithOtherEvos);

    boolean hasTimeBasedEvolutions();

    void removeTimeBasedEvolutions();

    /**
     * Some {@link EvolutionType}s only allow evolution in specific locations.
     * This method gets the name of said location(s), given an EvolutionType.
     */
    List<String> getLocationNamesForEvolution(EvolutionType et);

    /**
     * Returns a {@link Map} containing all Species whose
     * {@link Evolution}s were changed using {@link #removeImpossibleEvolutions(boolean)},
     * {@link #makeEvolutionsEasier(boolean)}, or {@link #removeTimeBasedEvolutions()},
     * and a {@link List} of all their Evolutions <b>pre-</b>change.<br>
     * If those methods have not been called, this Set is empty.
     */
    Map<Species, List<Evolution>> getPreImprovedEvolutions();

    // In the earlier games, alt formes use the same evolutions as the base forme.
    // In later games, this was changed so that alt formes can have unique evolutions
    // compared to the base forme.
    boolean altFormesCanHaveDifferentEvolutions();

    /**
     * Returns whether it is possible to give every {@link Species} <i>exactly</i>
     * one {@link Evolution} each, and successfully save the ROM.
     */
    boolean canGiveEverySpeciesOneEvolutionEach();

    // ==================================
    // (Mostly) unchanging lists of moves
    // ==================================

    List<Integer> getGameBreakingMoves();

    List<Integer> getIllegalMoves();

    // includes game or gen-specific moves like Secret Power
    // but NOT healing moves (Softboiled, Milk Drink)
    List<Integer> getFieldMoves();

    // any HMs required to obtain 4 badges
    // (excluding Gameshark codes or early drink in RBY)
    List<Integer> getEarlyRequiredHMMoves();

    // ====
    // Misc
    // ====

    ExpCurve[] getExpCurves();

    boolean isYellow();

    boolean isORAS();

    boolean isUSUM();

    boolean hasMultiplePlayerCharacters();

    String getROMName();

    String getROMCode();

    int getROMType();

    String getSupportLevel();

    String getDefaultExtension();

    int internalStringLength(String string);

    boolean canSetIntroPokemon();

    /**
     * Sets the {@link Species} shown in the intro. Returns false if pk is not a valid intro Species.
     * Throws {@link UnsupportedOperationException} if {@link #canSetIntroPokemon()} is not true.
     */
    boolean setIntroPokemon(Species pk);

    int generationOfPokemon();

    // ===========
    // code tweaks
    // ===========

    int miscTweaksAvailable();

    void applyMiscTweak(MiscTweak tweak);

    /**
     * Sets the Species shown in the catching tutorial. Returns false if the Species are not valid catching tutorial Species.
     */
    boolean setCatchingTutorial(Species opponent, Species player);

    void setPCPotionItem(Item item);

    // ==========================
    // Misc forme-related methods
    // ==========================

    boolean hasFunctionalFormes();

    SpeciesSet getBannedFormesForTrainerPokemon();
    
    // ========
    // Graphics
    // ========

    boolean hasPokemonPaletteSupport();

    boolean pokemonPaletteSupportIsPartial();

    boolean hasCustomPlayerGraphicsSupport();

    void setCustomPlayerGraphics(GraphicsPack playerGraphics, PlayerCharacterType toReplace);

    /**
     * Returns whether {@link #createPokemonImageGetter(Species)} is implemented or not.
     */
    boolean hasPokemonImageGetter();

    PokemonImageGetter createPokemonImageGetter(Species pk);

    // Kind of strange this is a responsibility for the romHandler, when the resources are so specific to the
    // randomizer parts, and the goal is to keep those separate. Still, it works for now.
    /**
     * Returns an identifier for resource files related to this ROM, used when randomizing palettes.
     */
    String getPaletteFilesID();

    void dumpAllPokemonImages();

    List<BufferedImage> getAllPokemonImages();

    // ======
    // Types
    // ======

    TypeService getTypeService();

    TypeTable getTypeTable();

    void setTypeTable(TypeTable typeTable);

    boolean hasTypeEffectivenessSupport();

}