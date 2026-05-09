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
 * Contains one wild Pokemon slot
 */
public class Encounter {
    // TODO: internally some encounters can't be alt formes; enforce it at this level

    private Species baseSpecies;
    private int formeNumber;
    private int level;
    private int maxLevel;

    // Used only for Gen 7's SOS mechanic
    private boolean isSOS;
    private SOSType sosType;

    public Encounter (Species species, int level) {
        setSpecies(species);
        this.level = level;
    }

    public Species getSpecies() {
        return baseSpecies.getForme(formeNumber);
    }

    public void setSpecies(Species species) {
        this.baseSpecies = species.getBaseForme();
        this.formeNumber = species.getFormeNumber();
    }

    public Species getBaseSpecies() {
        return baseSpecies;
    }

    public int getFormeNumber() {
        return formeNumber;
    }

    /**
     * Sets the formeNumber.
     * @param formeNumber The forme number to set.
     * @throws IllegalArgumentException if formeNumber is not a valid forme for {@link #baseSpecies}.
     */
    public void setFormeNumber(int formeNumber) {
        if (!baseSpecies.isValidFormeNumber(formeNumber)) {
            throw new IllegalArgumentException("formeNumber=" + formeNumber + " is not valid for "
                    + baseSpecies.getNumberAndFullName());
        }
        this.formeNumber = formeNumber;
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
            return level == other.level && maxLevel == other.maxLevel && baseSpecies.equals(other.baseSpecies)
                    && formeNumber == other.formeNumber && isSOS == other.isSOS && sosType == other.sosType;
        }
        return false;
    }

}
