package com.dabomstew.pkrandom.randomizers;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
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

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.graphics.palettes.Palette;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Random;

/**
 * A sub-randomizer for palette randomizing/modifications. Loading/writing palettes
 * should be done elsewhere, with implementations of this class only handling
 * changes done to said palettes.
 */
public abstract class PaletteRandomizer extends Randomizer {

	public PaletteRandomizer(RomHandler romHandler, Settings settings, Random random) {
		super(romHandler, settings, random);
	}

	public abstract void randomizePokemonPalettes();

	protected void setShinyPaletteFromNormal(Species pk) {
		pk.setShinyPalette(new Palette(pk.getNormalPalette()));
	}

}
