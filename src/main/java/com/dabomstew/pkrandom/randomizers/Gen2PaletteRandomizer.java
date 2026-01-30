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
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gamedata.cueh.BasicSpeciesAction;
import com.dabomstew.pkromio.gamedata.cueh.EvolvedSpeciesAction;
import com.dabomstew.pkromio.graphics.palettes.Color;
import com.dabomstew.pkromio.graphics.palettes.Gen2TypeColors;
import com.dabomstew.pkromio.graphics.palettes.Palette;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.util.Random;

/**
 * A {@link PaletteRandomizer} for Gen 2 games (G/S/C).
 * <p>
 * Pokémon palettes in Gen 2 do de facto only have two colors, as their sprites can
 * only have four, and two color slots are always occupied by white respectively
 * black. The remaining two colors are here divided into "bright colors" and
 * "dark colors", as those descriptors generally correspond to each of the color
 * slots.
 */
public class Gen2PaletteRandomizer extends PaletteRandomizer {

	private boolean typeSanity;
	private boolean shinyFromNormal;

	public Gen2PaletteRandomizer(RomHandler romHandler, Settings settings, Random random) {
		super(romHandler, settings, random);
	}

	@Override
	public void randomizePokemonPalettes() {
		this.typeSanity = settings.isPokemonPalettesFollowTypes();
		this.shinyFromNormal = settings.isPokemonPalettesShinyFromNormal();
		boolean evolutionSanity = settings.isPokemonPalettesFollowEvolutions();

		copyUpEvolutionsHelper.apply(evolutionSanity, true, new BasicSpeciesPaletteAction(),
				new EvolvedSpeciesPaletteAction());
	}

	private Palette getRandom2ColorPalette() {
		Palette palette = new Palette(2);
		palette.set(0, Gen2TypeColors.getRandomBrightColor(random));
		palette.set(1, Gen2TypeColors.getRandomDarkColor(random));
		return palette;
	}

	private Palette getRandom2ColorPalette(Type primaryType, Type secondaryType) {
		Palette palette = new Palette(2);
		Color brightColor = Gen2TypeColors.getRandomBrightColor(primaryType, random);
		Color darkColor = Gen2TypeColors.getRandomDarkColor(secondaryType == null ? primaryType : secondaryType,
				random);
		palette.set(0, brightColor);
		palette.set(1, darkColor);
		return palette;
	}

	private class BasicSpeciesPaletteAction implements BasicSpeciesAction<Species> {

		@Override
		public void applyTo(Species pk) {
			if (shinyFromNormal) {
				setShinyPaletteFromNormal(pk);
			}
			setNormalPaletteRandom(pk);
		}

		private void setNormalPaletteRandom(Species pk) {
			pk.setNormalPalette(typeSanity ? getRandom2ColorPalette(pk.getPrimaryType(false), pk.getSecondaryType(false))
					: getRandom2ColorPalette());
		}

	}

	private class EvolvedSpeciesPaletteAction implements EvolvedSpeciesAction<Species> {

		@Override
		public void applyTo(Species evFrom, Species evTo, boolean toMonIsFinalEvo) {
			if (shinyFromNormal) {
				setShinyPaletteFromNormal(evTo);
			}
			setNormalPaletteFromPrevo(evFrom, evTo);
		}

		private void setNormalPaletteFromPrevo(Species evFrom, Species evTo) {
			Palette palette = new Palette(evFrom.getNormalPalette());

			if (typeSanity) {
				if (evTo.getPrimaryType(false) != evFrom.getPrimaryType(false)) {
					Color newBrightColor = Gen2TypeColors.getRandomBrightColor(evTo.getPrimaryType(false), random);
					palette.set(0, newBrightColor);

				} else if (evTo.getSecondaryType(false) != evFrom.getSecondaryType(false)) {
					Color newDarkColor = Gen2TypeColors.getRandomDarkColor(
							evTo.getSecondaryType(false) == null ? evTo.getPrimaryType(false) : evTo.getSecondaryType(false), random);
					palette.set(1, newDarkColor);
				}
			}

			evTo.setNormalPalette(palette);
		}

	}

}
