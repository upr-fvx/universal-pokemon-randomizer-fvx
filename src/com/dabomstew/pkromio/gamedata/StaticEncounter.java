package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  StaticEncounter.java - stores a static encounter in Gen 6+            --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StaticEncounter {
    private Species species;
    private int forme = 0;
    private int level;
    private int maxLevel = 0;
    private Item heldItem;
    private boolean isEgg = false;
    private boolean resetMoves = false;
    private boolean restrictedPool = false;
    private List<Species> restrictedList = new ArrayList<>();

    // In the games, sometimes what is logically an encounter or set of encounters with one specific Pokemon
    // can actually consist of multiple encounters internally. This can happen because:
    // - The same Pokemon appears in multiple locations (e.g., Reshiram/Zekrom in BW1, Giratina in Pt)
    // - The same Pokemon appears at different levels depending on game progression (e.g., Volcarona in BW2)
    // - Rebattling a Pokemon actually is different encounter entirely (e.g., Xerneas/Yveltal in XY)
    // This list tracks encounters that should logically have the same species and forme, but *may* have
    // differences in other properties like level.
    private final List<StaticEncounter> linkedEncounters;

    public StaticEncounter() {
        this.linkedEncounters = new ArrayList<>();
    }

    public StaticEncounter(Species species) {
        this.species = species;
        this.linkedEncounters = new ArrayList<>();
    }

    /**
     * Deep copies the given encounter and all linked encounters.
     * Linked encounters are not stored in the main list, so this is safe for copying full sets.
     * @param original The StaticEncounter to copy.
     */
    public StaticEncounter(StaticEncounter original) {
        this.species = original.species;
        this.forme = original.forme;
        this.level = original.level;
        this.maxLevel = original.maxLevel;
        this.heldItem = original.heldItem;
        this.isEgg = original.isEgg;
        this.resetMoves = original.resetMoves;
        this.restrictedPool = original.restrictedPool;
        this.restrictedList = new ArrayList<>(original.restrictedList);
        this.linkedEncounters = new ArrayList<>(original.linkedEncounters.size());
        for (StaticEncounter oldLinked : original.linkedEncounters) {
            StaticEncounter newLinked = new StaticEncounter(); //is there a reason to not use the copy constructor here?
            newLinked.species = oldLinked.species;
            newLinked.forme = oldLinked.forme;
            newLinked.level = oldLinked.level;
            newLinked.maxLevel = oldLinked.maxLevel;
            newLinked.heldItem = oldLinked.heldItem;
            newLinked.isEgg = oldLinked.isEgg;
            newLinked.resetMoves = oldLinked.resetMoves;
            newLinked.restrictedPool = oldLinked.restrictedPool;
            newLinked.restrictedList = new ArrayList<>(oldLinked.restrictedList);
            this.linkedEncounters.add(newLinked);
        }
    }


    public Species getSpecies() {
        return species;
    }

    public void setSpecies(Species species) {
        this.species = species;
    }

    public int getForme() {
        return forme;
    }

    public void setForme(int forme) {
        this.forme = forme;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public Item getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(Item heldItem) {
        this.heldItem = heldItem;
    }

    public boolean canMegaEvolve() {
        for (MegaEvolution mega: species.getMegaEvolutionsFrom()) {
            if (mega.isNeedsItem() && mega.getItem().equals(heldItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEgg() {
        return isEgg;
    }

    public void setEgg(boolean egg) {
        isEgg = egg;
    }

    public boolean isResetMoves() {
        return resetMoves;
    }

    public void setResetMoves(boolean resetMoves) {
        this.resetMoves = resetMoves;
    }

    public boolean isRestrictedPool() {
        return restrictedPool;
    }

    public void setRestrictedPool(boolean restrictedPool) {
        this.restrictedPool = restrictedPool;
    }

    public List<Species> getRestrictedList() {
        return restrictedList;
    }

    public void setRestrictedList(List<Species> restrictedList) {
        this.restrictedList = restrictedList;
    }

    public List<StaticEncounter> getLinkedEncounters() {
        return linkedEncounters;
    }

    @Override
    public String toString() {
        return this.toString(true);
    }

    public String toString(boolean printLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(species == null ? null : species.getFullName());
        if (isEgg) {
            sb.append(" (egg)");
        } else if (printLevel) {
            StringBuilder levelStringBuilder = new StringBuilder("Lv" + level);
            if (maxLevel > 0) {
                levelStringBuilder.append("-").append(maxLevel);
            }
            boolean needToDisplayLinkedLevels = false;
            for (StaticEncounter linkedEncounter : linkedEncounters) {
                if (level != linkedEncounter.level) {
                    needToDisplayLinkedLevels = true;
                    break;
                }
            }
            if (needToDisplayLinkedLevels) {
                for (StaticEncounter linkedEncounter : linkedEncounters) {
                    levelStringBuilder.append(" / ").append("Lv").append(linkedEncounter.level);
                }
            }
            sb.append(" ");
            sb.append(levelStringBuilder);
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(species, level);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StaticEncounter) {
            StaticEncounter other = (StaticEncounter) o;
            return Objects.equals(other.species, species) && other.forme == forme && other.level == level
                    && other.maxLevel == maxLevel && other.isEgg == isEgg && other.resetMoves == resetMoves
                    && other.restrictedPool == restrictedPool && Objects.equals(other.restrictedList, restrictedList)
                    && Objects.equals(other.linkedEncounters, linkedEncounters);
        }
        return false;
    }

}
