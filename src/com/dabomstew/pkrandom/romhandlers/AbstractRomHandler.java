package com.dabomstew.pkrandom.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  AbstractRomHandler.java - a base class for all rom handlers which     --*/
/*--                            implements the majority of the actual       --*/
/*--                            randomizer logic by building on the base    --*/
/*--                            getters & setters provided by each concrete --*/
/*--                            handler.                                    --*/
/*--                                                                        --*/
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

import com.dabomstew.pkrandom.MiscTweak;
import com.dabomstew.pkrandom.RomFunctions;
import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.constants.ItemIDs;
import com.dabomstew.pkrandom.exceptions.RomIOException;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.graphics.packs.GraphicsPack;
import com.dabomstew.pkrandom.romhandlers.romentries.RomEntry;
import com.dabomstew.pkrandom.services.RestrictedSpeciesService;
import com.dabomstew.pkrandom.services.TypeService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An abstract base class for {@link RomHandler}s, with default implementations for many of the interface's methods.
 */
public abstract class AbstractRomHandler implements RomHandler {

    protected final RestrictedSpeciesService rPokeService = new RestrictedSpeciesService(this);
    protected final TypeService typeService = new TypeService(this);

    protected int perfectAccuracy = 100; // default

    /*
     * Public Methods, implemented here for all gens. Unlikely to be overridden.
     */

    public RestrictedSpeciesService getRestrictedSpeciesService() {
        return rPokeService;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    @Override
    public SpeciesSet getSpeciesSet() {
        return SpeciesSet.unmodifiable(getSpecies());
    }

    @Override
    public SpeciesSet getSpeciesSetInclFormes() {
        return SpeciesSet.unmodifiable(getSpeciesInclFormes());
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        return getEncounters(useTimeOfDay);
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        return new SpeciesSet();
    }

    @Override
    public boolean canAddPokemonToBossTrainers() {
        return true;
    }

    @Override
    public boolean canAddPokemonToImportantTrainers() {
        return true;
    }

    @Override
    public boolean canAddPokemonToRegularTrainers() {
        return true;
    }


    public SpeciesSet getMainGameWildPokemonSpecies(boolean useTimeOfDay) {
        SpeciesSet wildPokemon = new SpeciesSet();
        List<EncounterArea> areas = this.getEncounters(useTimeOfDay);

        for (EncounterArea area : areas) {
            if (area.isPartiallyPostGame()) {
                for (int i = area.getPartiallyPostGameCutoff(); i < area.size(); i++) {
                    wildPokemon.add(area.get(i).getSpecies());
                }
            } else if (!area.isPostGame()) {
                for (Encounter enc : area) {
                    wildPokemon.add(enc.getSpecies());
                }
            }
        }
        return wildPokemon;
    }

    @Override
    public boolean canAddHeldItemsToBossTrainers() {
        return true;
    }

    @Override
    public boolean canAddHeldItemsToImportantTrainers() {
        return true;
    }

    @Override
    public boolean canAddHeldItemsToRegularTrainers() {
        return true;
    }

    @Override
    public boolean hasRivalFinalBattle() {
        return false;
    }

    @Override
    public boolean hasStarterTypeTriangleSupport() {
        return (starterCount() % 3 == 0);
    }

    @Override
    public boolean canChangeStaticPokemon() {
        return getRomEntry().hasStaticPokemonSupport();
    }

    @Override
    public void condenseLevelEvolutions(int maxLevel, int maxIntermediateLevel) {
        // search for level evolutions
        for (Species pk : getSpeciesSet()) {
            if (pk != null) {
                for (Evolution checkEvo : pk.getEvolutionsFrom()) {
                    if (checkEvo.getType().usesLevel()) {
                        // If evo is intermediate and too high, bring it down
                        // Else if it's just too high, bring it down
                        if (checkEvo.getExtraInfo() > maxIntermediateLevel && !checkEvo.getTo().getEvolutionsFrom().isEmpty()) {
                            markImprovedEvolutions(pk);
                            checkEvo.setExtraInfo(maxIntermediateLevel);
                        } else if (checkEvo.getExtraInfo() > maxLevel) {
                            markImprovedEvolutions(pk);
                            checkEvo.setExtraInfo(maxLevel);
                        }
                    }
                    if (checkEvo.getType() == EvolutionType.LEVEL_UPSIDE_DOWN) {
                        markImprovedEvolutions(pk);
                        checkEvo.setType(EvolutionType.LEVEL);
                    }
                }
            }
        }

    }

    @Override
    public void removeTimeBasedEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            for (Evolution evo : pk.getEvolutionsFrom()) {
                EvolutionType et = evo.getType();

                if (et == EvolutionType.LEVEL_DUSK) {
                    markImprovedEvolutions(pk);
                    evo.setType(EvolutionType.STONE);
                    evo.setExtraInfo(ItemIDs.duskStone);
                } else if (et.usesTime()) {
                    markImprovedEvolutions(pk);
                    if (hadEvolutionOfType(pk, et.oppositeTime())) {
                        // Here we have just ascertained that this Species evolves by time,
                        // and that this evolution is paired; it has another similar evolution
                        // at the opposite time.
                        // E.g. Eevee -> Espeon/Umbreon, which is a HAPPINESS_DAY/HAPPINESS_NIGHT pair.
                        // In this case, we can't just remove the time-based-less,
                        // so instead we use Sun/Moon Stone.
                        evo.setType(EvolutionType.STONE);
                        int item = et.isDayType() ? ItemIDs.sunStone : ItemIDs.moonStone;
                        evo.setExtraInfo(item);
                    } else {
                        evo.setType(et.timeless());
                    }
                }
            }
        }
    }

    private boolean hadEvolutionOfType(Species pk, EvolutionType et) {
        List<Evolution> evos = preImprovedEvolutions.get(pk);
        if (evos == null) {
            throw new IllegalStateException("Species should always have been added to preImprovedEvolutions.");
        }
        return evos.stream().map(Evolution::getType).anyMatch(et2 -> et2 == et);
    }

    @Override
    public Map<Species, List<Evolution>> getPreImprovedEvolutions() {
        return preImprovedEvolutions;
    }

    /* Private methods/structs used internally by the above methods */

    private final Map<Species, List<Evolution>> preImprovedEvolutions = new TreeMap<>();

    /**
     * Marks that a {@link Species} is getting its {@link Evolution}s improved,
     * (and saves its original Evolutions) for logging purposes.
     */
    protected void markImprovedEvolutions(Species pk) {
        // We can't overwrite these entries, because then species with
        // multiple evos to change (e.g. HGSS Eevee) will store their partially-changed evos,
        // instead of the original ones.
        if (!preImprovedEvolutions.containsKey(pk)) {
            List<Evolution> evosCopy = new ArrayList<>(pk.getEvolutionsFrom().size());
            for (Evolution original : pk.getEvolutionsFrom()) {
                evosCopy.add(new Evolution(original));
            }
            preImprovedEvolutions.put(pk, evosCopy);
        }
    }

    /* Helper methods used by subclasses and/or this class */

    /**
     * Splits occurrences of {@link EvolutionType#LEVEL_ITEM} into
     * a {@link EvolutionType#LEVEL_ITEM_DAY} and a {@link EvolutionType#LEVEL_ITEM_NIGHT} part.<br>
     * Since LEVEL_ITEM is not used internally in any ROM, this must be done before writing Evolutions.<br>
     * Assumes each Species has at most one LEVEL_ITEM Evolution.
     */
    protected void splitLevelItemEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            Evolution levelItemEvo = null;
            for (Evolution evo : pk.getEvolutionsFrom()) {
                if (evo.getType() == EvolutionType.LEVEL_ITEM) {
                    levelItemEvo = evo;
                }
            }
            if (levelItemEvo != null) {
                levelItemEvo.setType(EvolutionType.LEVEL_ITEM_DAY);
                Evolution nightEvo = new Evolution(levelItemEvo);
                nightEvo.setType(EvolutionType.LEVEL_ITEM_NIGHT);
                nightEvo.getFrom().getEvolutionsFrom().add(nightEvo);
                nightEvo.getTo().getEvolutionsTo().add(nightEvo);
            }
        }
    }

    /**
     * Merge occurrences of otherwise identical {@link EvolutionType#LEVEL_ITEM_DAY} and
     * {@link EvolutionType#LEVEL_ITEM_NIGHT} {@link Evolution}s into a single one
     * using {@link EvolutionType#LEVEL_ITEM}.<br>
     * Assumes each Species has at most one pair of LEVEL_ITEM_DAY/NIGHT Evolutions.
     */
    protected void mergeLevelItemEvolutions() {
        for (Species pk : getSpecies()) {
            if (pk == null) {
                continue;
            }
            Evolution dayEvo = null;
            Evolution nightEvo = null;
            for (Evolution evo : pk.getEvolutionsFrom()) {
                if (evo.getType() == EvolutionType.LEVEL_ITEM_DAY) {
                    dayEvo = evo;
                } else if (evo.getType() == EvolutionType.LEVEL_ITEM_NIGHT) {
                    nightEvo = evo;
                }
            }
            if (dayEvo != null && nightEvo != null) {
                nightEvo.setType(EvolutionType.LEVEL_ITEM_DAY);
                if (nightEvo.equals(dayEvo)) {
                    dayEvo.setType(EvolutionType.LEVEL_ITEM);
                    nightEvo.getFrom().getEvolutionsFrom().remove(nightEvo);
                    nightEvo.getTo().getEvolutionsTo().remove(nightEvo);
                } else {
                    // If nightEvo differs from dayEvo in ways other than the EvolutionType,
                    // then we can not merge them.
                    nightEvo.setType(EvolutionType.LEVEL_ITEM_NIGHT);
                }
            }
        }
    }

    protected void applyCamelCaseNames() {
        getSpeciesSet().forEach(pk -> pk.setName(RomFunctions.camelCase(pk.getName())));
    }

    /* Default Implementations */
    /* Used when a subclass doesn't override */
    /*
     * The implication here is that these WILL be overridden by at least one
     * subclass.
     */

    @Override
    public List<String> getLocationNamesForEvolution(EvolutionType et) {
        throw new UnsupportedOperationException("This game has no location-based evolutions.");
    }

    @Override
    public boolean canSetIntroPokemon() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean hasTimeBasedEvolutions() {
        // DEFAULT: yes
        return true;
    }

    @Override
    public boolean hasTotemPokemon() {
        // DEFAULT: no
        return false;
    }

    @Override
    public void makeDoubleBattleModePossible() {
        // do nothing; this method is just needed by some ROMs
    }

    @Override
    public boolean hasTypeEffectivenessSupport() {
        // DEFAULT: no
        return false;
    }

    @Override
    public TypeTable getTypeTable() {
        // just returns some hard-coded tables if the subclass doesn't implement actually reading from ROM
        // obviously it is better if the type table can be actually read from ROM, so override this when possible
        if (generationOfPokemon() == 1) {
            return TypeTable.getVanillaGen1Table();
        } else if (generationOfPokemon() <= 5) {
            return TypeTable.getVanillaGen2To5Table();
        } else {
            return TypeTable.getVanillaGen6PlusTable();
        }
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        // do nothing
    }

    @Override
    public String abilityName(int number) {
        return "";
    }

    @Override
    public List<Integer> getUselessAbilities() {
        return new ArrayList<>();
    }

    @Override
    public int getAbilityForTrainerPokemon(TrainerPokemon tp) {
        return 0;
    }

    @Override
    public boolean hasTimeBasedEncounters() {
        // DEFAULT: no
        return false;
    }

    @Override
    public int getPerfectAccuracy() {
        return perfectAccuracy;
    }

    @Override
    public List<Integer> getMovesBannedFromLevelup() {
        return new ArrayList<>();
    }

    @Override
    public SpeciesSet getBannedForStaticPokemon() {
        return new SpeciesSet();
    }

    @Override
    public boolean forceSwapStaticMegaEvos() {
        return false;
    }

    @Override
    public List<String> getTrainerNames() {
        return getTrainers().stream().map(tr -> tr.name).collect(Collectors.toList());
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        for (int i = 0; i < trainerNames.size(); i++) {
            getTrainers().get(i).name = trainerNames.get(i);
        }
    }

    @Override
    public int maxTrainerNameLength() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxSumOfTrainerNameLengths() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxTrainerClassNameLength() {
        // default: no real limit
        return Integer.MAX_VALUE;
    }

    @Override
    public int maxTradeNicknameLength() {
        return 10;
    }

    @Override
    public int maxTradeOTNameLength() {
        return 7;
    }

    @Override
    public boolean altFormesCanHaveDifferentEvolutions() {
        return false;
    }

    @Override
    public List<Integer> getGameBreakingMoves() {
        // Sonicboom & Dragon Rage
        return Arrays.asList(49, 82);
    }

    @Override
    public List<Integer> getIllegalMoves() {
        return new ArrayList<>();
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
        return true;
    }

    @Override
    public int miscTweaksAvailable() {
        // default: none
        return 0;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        // default: do nothing
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
    public List<Integer> getXItems() {
        return GlobalConstants.xItems;
    }

    @Override
    public List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        return Collections.singletonList(0);
    }

    @Override
    public List<Integer> getAllConsumableHeldItems() {
        return Collections.singletonList(0);
    }

    /**
     * Returns a list of item IDs of all items that may have an effect for enemy trainers in battle.<br>
     * So e.g. Everstone is excluded, but also Metal Powder or other items that only have effects for
     * certain Pokémon species, since when picked for any other Pokémon they will do nothing.
     */
    @Override
    public List<Integer> getAllHeldItems() {
        return Collections.singletonList(0);
    }

    @Override
    public SpeciesSet getBannedFormesForTrainerPokemon() {
        return new SpeciesSet();
    }

    @Override
    public List<PickupItem> getPickupItems() {
        return new ArrayList<>();
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        // do nothing
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
    public boolean customPlayerGraphicsSupportDependsOnOS() {
        return false;
    }

    @Override
    public void setCustomPlayerGraphics(GraphicsPack playerGraphics, Settings.PlayerCharacterMod toReplace) {
        throw new UnsupportedOperationException("Custom player graphics not supported for this game.");
    }

    @Override
    public boolean hasPokemonImageGetter() {
        return false; // default: no
    }

    // just for testing
    public final void dumpAllPokemonImages() {
        List<BufferedImage> bims = getAllPokemonImages();

        for (int i = 0; i < bims.size(); i++) {
            String fileAdress = "Pokemon_image_dump/gen" + generationOfPokemon() + "/"
                    + String.format("%03d_d.png", i + 1);
            File outputfile = new File(fileAdress);
            try {
                ImageIO.write(bims.get(i), "png", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getPaletteFilesID() {
        throw new UnsupportedOperationException(); // default: assumes no resource files are needed
    }

    public abstract List<BufferedImage> getAllPokemonImages();

    public abstract void savePokemonPalettes();

    @Override
    public boolean saveRom(String filename, long seed, boolean saveAsDirectory) {
        try {
            prepareSaveRom();
            return saveAsDirectory ? saveRomDirectory(filename) : saveRomFile(filename, seed);
        } catch (RomIOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes the remaining things to the ROM, before it is written to file. When
     * overridden, this should be called as a superclass method.
     */
    protected void prepareSaveRom() {
        savePokemonStats();
        saveMoves();
        savePokemonPalettes();
    }

    public abstract void saveMoves();

    public abstract void savePokemonStats();

    protected abstract boolean saveRomFile(String filename, long seed);

    protected abstract boolean saveRomDirectory(String filename);

    protected abstract RomEntry getRomEntry();

    @Override
    public String getROMName() {
        return "Pokemon " + getRomEntry().getName();
    }

    @Override
    public String getROMCode() {
        return getRomEntry().getRomCode();
    }

    @Override
    public int getROMType() {
        return getRomEntry().getRomType();
    }

    @Override
    public String getSupportLevel() {
        return getRomEntry().hasStaticPokemonSupport() ? "Complete" : "No Static Pokemon";
    }

}
