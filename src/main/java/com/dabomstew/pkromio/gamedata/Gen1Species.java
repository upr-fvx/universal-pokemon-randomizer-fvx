package com.dabomstew.pkromio.gamedata;

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

import com.dabomstew.pkromio.graphics.palettes.SGBPaletteID;

/**
 * Represents an individual Gen 1 Species.
 * Used to handle things related to stats because of the lack of the Special split in Gen 1.
 */
public class Gen1Species extends Species {

    public Gen1Species(int number) {
        super(number);
        setGeneration(1);
    }

    private int frontImagePointer;
    private int backImagePointer;
	
	private SGBPaletteID paletteID;

    @Override
    public int getBST() {
        return getHp() + getAttack() + getDefense() + getSpecial() + getSpeed();
    }

    @Override
    public int getBSTForPowerLevels() {
        return getBST(); // no Shedinja clause
    }

    @Override
    public double getAttackSpecialAttackRatio() {
        return (double) getAttack() / ((double) getAttack() + (double) getSpecial());
    }

    @Override
    public String toString() {
        return "Species [name=" + getName() + ", number=" + getNumber() + ", primaryType=" + getPrimaryType(false) + ", secondaryType="
                + getSecondaryType(false) + ", hp=" + getHp() + ", attack=" + getAttack() + ", defense=" + getDefense() + ", special=" + getSpecial()
                + ", speed=" + getSpeed() + "]";
    }

    public int getFrontImagePointer() {
        return frontImagePointer;
    }

    public void setFrontImagePointer(int frontImagePointer) {
        this.frontImagePointer = frontImagePointer;
    }

    public int getBackImagePointer() {
        return backImagePointer;
    }

    public void setBackImagePointer(int backImagePointer) {
        this.backImagePointer = backImagePointer;
    }

    public SGBPaletteID getPaletteID() {
        return paletteID;
    }

    public void setPaletteID(SGBPaletteID paletteID) {
        this.paletteID = paletteID;
    }
}
