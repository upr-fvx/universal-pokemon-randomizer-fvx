package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  Encounter.java - contains one wild Pokemon slot                       --*/
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

public class Encounter {

    private int level;
    private int maxLevel;
    private Species species;
    private int formeNumber;

    // Used only for Gen 7's SOS mechanic
    private boolean isSOS;
    private SOSType sosType;

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

    public Species getSpecies() {
        return species;
    }
    //TODO: determine which uses of this need base forme, have those call Species.baseForme
    //(Thus allowing us to store the actually-used forme here,
    //solving some problems)

    public void setSpecies(Species species) {
        this.species = species;
    }

    public int getFormeNumber() {
        return formeNumber;
    }

    public void setFormeNumber(int formeNumber) {
        this.formeNumber = formeNumber;
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
        if (species == null) {
            return "ERROR";
        }
        if (maxLevel == 0) {
            return species.getName() + " Lv" + level;
        } else {
            return species.getName() + " Lvs " + level + "-" + maxLevel;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Encounter) {
            Encounter other = (Encounter) o;
            return level == other.level && maxLevel == other.maxLevel && species.equals(other.species)
                    && formeNumber == other.formeNumber && isSOS == other.isSOS && sosType == other.sosType;
        }
        return false;
    }

}
