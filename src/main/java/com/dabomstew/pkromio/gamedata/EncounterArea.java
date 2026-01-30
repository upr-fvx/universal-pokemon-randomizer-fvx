package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  EncounterArea.java - contains a group of wild Pokemon                 --*/
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

import java.util.*;

public class EncounterArea extends ArrayList<Encounter> {

    private int rate;
    private final Set<Species> bannedSpecies = new HashSet<>();
    private String displayName;

    //The index of the map this area is contained in, as determined by the RomHandler.
    //Note that not all RomHandlers currently set this variable.
    //However, in general, a particular index number will apply to exactly one map -
    //unless the number is negative, in which case it indicates the encounter area spans multiple maps.
    //(Or that the map cannot be determined.)
    //Negative map indices will still attempt to have logical groupings.
    private int mapIndex = -1;

    private String locationTag;

    //The type of encounter this area is, as determined by the RomHandler.
    private EncounterType encounterType;

    private boolean postGame;
    // In some games, areas have both main game and post game encounters, following each other,
    // e.g. the fishing encounters in Gen 2. This attribute indicates the index for where the post game encounters
    // start.
    private int partiallyPostGameCutoff = -1;

    // For areas that work like/is the Trophy Garden rotating Pokemon in DPPt, where the game
    // softlocks or otherwise has logical issues if all Encounters' species are the same.
    private boolean forceMultipleSpecies;

    public EncounterArea() {
    }

    public EncounterArea(Collection<? extends Encounter> collection) {
        super(collection);
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     * Returns an unmodifiable set of the {@link Species} which should NOT have {@link Encounter}s in this area.
     */
    public SpeciesSet getBannedSpecies() {
        return SpeciesSet.unmodifiable(bannedSpecies);
    }

    public void banSpecies(Species toBan) {
        bannedSpecies.add(toBan);
    }

    public void banAllSpecies(Collection<? extends Species> toBan) {
        bannedSpecies.addAll(toBan);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getMapIndex() {
        return mapIndex;
    }

    public void setMapIndex(int mapIndex) {
        this.mapIndex = mapIndex;
    }

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType type) {
        this.encounterType = type;
    }

    /**
     * Sets several pieces of initial info about the area, used to identify it.
     * @param displayName The area's display name.
     * @param mapIndex The mapIndex for the area.
     * @param encounterType The area's encounter type.
     */
    public void setIdentifiers(String displayName, int mapIndex, EncounterType encounterType) {
        this.setDisplayName(displayName);
        this.setMapIndex(mapIndex);
        this.setEncounterType(encounterType);
    }

    /**
     * Sets several pieces of initial info about the area, used to identify it.
     * @param displayName The area's display name.
     * @param mapIndex The mapIndex for the area.
     * @param encounterType The area's encounter type.
     * @param locationTag The name of the Location this area is contained in.
     */
    public void setIdentifiers(String displayName, int mapIndex, EncounterType encounterType, String locationTag) {
        this.setDisplayName(displayName);
        this.setMapIndex(mapIndex);
        this.setEncounterType(encounterType);
        this.setLocationTag(locationTag);
    }

    public boolean isPostGame() {
        return postGame;
    }

    public void setPostGame(boolean postGame) {
        this.postGame = postGame;
    }

    public boolean isPartiallyPostGame() {
        return partiallyPostGameCutoff != -1;
    }

    public int getPartiallyPostGameCutoff() {
        return partiallyPostGameCutoff;
    }

    public void setPartiallyPostGameCutoff(int partiallyPostGameCutoff) {
        this.partiallyPostGameCutoff = partiallyPostGameCutoff;
    }

    public void setForceMultipleSpecies(boolean forceMultipleSpecies) {
        this.forceMultipleSpecies = forceMultipleSpecies;
    }

    public boolean isForceMultipleSpecies() {
        return forceMultipleSpecies;
    }

    @Override
    public String toString() {
        return "Encounters [Name = " + displayName + ", Rate = " + rate + ", EncounterType = " + encounterType +
                ", Encounters = " + super.toString() + "]";
    }

    //Helper functions

    /**
     * Creates a {@link SpeciesSet} with all Species that can be found in this area.
     * @return A {@link SpeciesSet} containing all Species that can be encountered in this area.
     */
    public SpeciesSet getSpeciesInArea() {
        SpeciesSet speciesSet = new SpeciesSet();
        for (Encounter enc : this) {
            speciesSet.add(enc.getSpecies());
        }
        return speciesSet;
    }

    /**
     * Given a List of EncounterAreas, groups those that have the same encounter type.
     * @param toGroup The set of EncounterAreas to group.
     * @return A Map of encounterTypes to EncounterAreas.
     */
    public static Map<EncounterType, List<EncounterArea>> groupAreasByEncounterType(List<EncounterArea> toGroup) {
        Map<EncounterType, List<EncounterArea>> grouped = new HashMap<>();
        for (EncounterArea area : toGroup) {
            EncounterType encType = area.getEncounterType();
            if (!grouped.containsKey(encType)) {
                grouped.put(encType, new ArrayList<>());
            }
            grouped.get(encType).add(area);
        }
        return grouped;
    }

    /**
     * Given a List of EncounterAreas, groups those that have the same map index.
     * @param toGroup The set of EncounterAreas to group.
     * @return A Map of mapIndexes to EncounterAreas.
     */
    public static Map<Integer, List<EncounterArea>> groupAreasByMapIndex(List<EncounterArea> toGroup) {
        Map<Integer, List<EncounterArea>> grouped = new HashMap<>();
        for (EncounterArea area : toGroup) {
            int index = area.getMapIndex();
            if (!grouped.containsKey(index)) {
                grouped.put(index, new ArrayList<>());
            }
            grouped.get(index).add(area);
        }
        return grouped;
    }

    /**
     * Given a List of EncounterAreas, groups those that have the same location tag.
     * @param toGroup The set of EncounterAreas to group.
     * @return A Map of locationTags to EncounterAreas.
     */
    public static Map<String, List<EncounterArea>> groupAreasByLocation(List<EncounterArea> toGroup) {
        Map<String, List<EncounterArea>> grouped = new HashMap<>();
        int untagged = 1;
        for (EncounterArea area : toGroup) {
            String tag = area.getLocationTag();
            if (tag == null) {
                tag = "UNTAGGED-" + untagged;
                untagged++;
            }
            if (!grouped.containsKey(tag)) {
                grouped.put(tag, new ArrayList<>());
            }
            grouped.get(tag).add(area);
        }
        return grouped;
    }

    /**
     * Given a List of EncounterAreas, merges those that have the same Location tag.
     * @param toFlatten The set of EncounterAreas to merge.
     * @return A List of EncounterAreas with the specified areas merged.
     */
    public static List<EncounterArea> flattenLocations(List<EncounterArea> toFlatten) {
        Map<String, List<EncounterArea>> grouped = groupAreasByLocation(toFlatten);
        List<EncounterArea> flattenedLocations = new ArrayList<>();
        for (Map.Entry<String, List<EncounterArea>> locEntry : grouped.entrySet()) {
            EncounterArea flattened = new EncounterArea();
            flattened.setDisplayName("All of location " + locEntry.getKey());
            for(EncounterArea area : locEntry.getValue()) {
                if(area.encounterType == EncounterType.UNUSED) {
                    continue;
                }
                flattened.addAll(area);
                flattened.banAllSpecies(area.getBannedSpecies());
            }
            flattenedLocations.add(flattened);
        }
        return flattenedLocations;
    }

    /**
     * Given a List of EncounterAreas, merges those that have the same map index AND encounter type.
     * @param toFlatten The set of EncounterAreas to merge.
     * @return A List of EncounterAreas with the specified areas merged.
     */
    public static List<EncounterArea> flattenEncounterTypesInMaps(List<EncounterArea> toFlatten) {
        Map<Integer, List<EncounterArea>> grouped = groupAreasByMapIndex(toFlatten);
        List<EncounterArea> flattenedEncounters = new ArrayList<>();
        int unnamed = 1;
        for (Map.Entry<Integer, List<EncounterArea>> mapEntry : grouped.entrySet()) {
            Map<EncounterType, List<EncounterArea>> mapGrouped =
                    groupAreasByEncounterType(mapEntry.getValue());
            String mapName = mapEntry.getValue().get(0).getLocationTag();
            if (mapName == null) {
                mapName = "Unknown Map " + unnamed;
                unnamed++;
            }
            for (Map.Entry<EncounterType, List<EncounterArea>> typeEntry : mapGrouped.entrySet()) {
                EncounterArea flattened = new EncounterArea();
                flattened.setDisplayName(mapName + "-" + typeEntry.getKey().name());
                flattened.setEncounterType(typeEntry.getKey());
                flattened.setMapIndex(mapEntry.getKey());
                for (EncounterArea area : typeEntry.getValue()) {
                    flattened.addAll(area);
                    flattened.banAllSpecies(area.getBannedSpecies());
                }
                flattenedEncounters.add(flattened);
            }
        }
        return flattenedEncounters;
    }

    //Note: should be two more flattens... probably. (Or, we might remove the need for them entirely.)
}
