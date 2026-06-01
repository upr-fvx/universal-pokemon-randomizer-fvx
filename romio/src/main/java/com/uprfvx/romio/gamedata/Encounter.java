package com.uprfvx.romio.gamedata;

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

/**
 * Contains one wild Pokemon slot. The {@link Species} and forme info is largely held by a {@link SpeciesHolder},
 * but can also be gotten through {@link #getSpecies()}.
 */
public class Encounter {

    private final SpeciesHolder speciesHolder;

    private int level;
    private int maxLevel;

    // Used only for Gen 7's SOS mechanic
    private boolean isSOS;
    private SOSType sosType;

    /**
     * Creates an Encounter with the given {@link Species} and level.
     * @param species The Species used for the Encounter. Must be a base forme.
     * @param level The level for the Encounter, or min level if a max level is given later. Must be non-negative.
     * @throws NullPointerException if species is null.
     * @throws IllegalArgumentException if species is not a base forme.
     * @throws IllegalArgumentException if level is negative.
     */
    public Encounter(Species species, int level) {
        if (level < 0) {
            throw new IllegalArgumentException("level must be non-negative");
        }
        this.speciesHolder = new SpeciesHolder(species);
        this.level = level;
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

    public boolean isSOS() {
        return isSOS;
    }

    public void setSOS(boolean SOS) {
        isSOS = SOS;
    }

    public SOSType getSosType() {
        return sosType;
    }

    public void setSosType(SOSType sosType) {
        this.sosType = sosType;
    }

    @Override
    public String toString() {
        if (maxLevel == 0) {
            return getSpecies().getFullName() + " Lv" + level;
        } else {
            return getSpecies().getFullName() + " Lvs " + level + "-" + maxLevel;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Encounter other) {
            return level == other.level && maxLevel == other.maxLevel && speciesHolder.equals(other.speciesHolder)
                    && isSOS == other.isSOS && sosType == other.sosType;
        }
        return false;
    }

}
