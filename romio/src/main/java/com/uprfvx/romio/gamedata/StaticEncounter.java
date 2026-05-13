package com.uprfvx.romio.gamedata;

/*----------------------------------------------------------------------------*/
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

/**
 * Represents a static encounter or a gift Pokémon. The {@link Species} and forme info is largely
 * held by a {@link SpeciesHolder}, but can also be gotten through {@link #getSpecies()}.
 */
public class StaticEncounter {
    // TODO: test cases

    private final SpeciesHolder speciesHolder;

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

    /**
     * Creates a StaticEncounter with the given species.
     * @throws NullPointerException if species is null
     */
    public StaticEncounter(Species species) {
        this.speciesHolder = new SpeciesHolder(species);
        this.linkedEncounters = new ArrayList<>();
    }

    /**
     * Deep copies the given encounter and all linked encounters.
     * Linked encounters are not stored in the main list, so this is safe for copying full sets.
     * @param original The StaticEncounter to copy.
     */
    public StaticEncounter(StaticEncounter original) {
        this.speciesHolder = new SpeciesHolder(original.speciesHolder);
        this.level = original.level;
        this.maxLevel = original.maxLevel;
        this.heldItem = original.heldItem;
        this.isEgg = original.isEgg;
        this.resetMoves = original.resetMoves;
        this.restrictedPool = original.restrictedPool;
        this.restrictedList = new ArrayList<>(original.restrictedList);
        this.linkedEncounters = new ArrayList<>(original.linkedEncounters.size());
        for (StaticEncounter oldLinked : original.linkedEncounters) {
            // linked encounters don't have their own linked encounters, so this shouldn't deadlock
            StaticEncounter newLinked = new StaticEncounter(oldLinked);
            this.linkedEncounters.add(newLinked);
        }
    }

    public SpeciesHolder getSpeciesHolder() {
        return speciesHolder;
    }

    /**
     * Short for {@link #getSpeciesHolder()}.{@link SpeciesHolder#getSpecies() getSpecies()}
     */
    public Species getSpecies() {
        return speciesHolder.getSpecies();
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
        // A bit unclear whether this should use getSpecies() or baseSpecies
        // though it does not matter in the Vanilla Gen 6-7 games since no mon
        // that can mega evolve has alt formes beyond the mega.
        // If you give base forme (Plant) Wormadam a mega, can its alt formes automatically
        // access the same mega, and mega evolve?
        // If you force the encounter to be Venusaur-Mega from the start and give it Venusaurite,
        // can it mega evolve? Into Venusaur-Mega again??
        // TODO: figure out
        for (MegaEvolution mega: getSpecies().getMegaEvolutionsFrom()) {
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

    // TODO: encapsulate linked encounters better
    //  Getting should retrieve an unmodifiable list, adding done via a separate method.
    //  Also, linked encounters should not have modifiable baseSpecies/formeNumber
    public List<StaticEncounter> getLinkedEncounters() {
        return linkedEncounters;
    }

    @Override
    public String toString() {
        return this.toString(true);
    }

    public String toString(boolean printLevel) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSpecies().getFullName());
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
        return Objects.hash(getSpecies(), level);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StaticEncounter other) {
            return other.level == level && other.speciesHolder.equals(speciesHolder)
                    && other.maxLevel == maxLevel && other.isEgg == isEgg && other.resetMoves == resetMoves
                    && other.restrictedPool == restrictedPool && Objects.equals(other.restrictedList, restrictedList)
                    && Objects.equals(other.linkedEncounters, linkedEncounters);
        }
        return false;
    }

}
