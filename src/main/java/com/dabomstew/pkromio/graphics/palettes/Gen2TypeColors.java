package com.dabomstew.pkromio.graphics.palettes;

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

import com.dabomstew.pkrandom.randomizers.Gen2PaletteRandomizer;
import com.dabomstew.pkromio.gamedata.Type;

import java.util.Map;
import java.util.Random;

/**
 * Contains methods for accessing TypeColor constants for Gen 2 games (G/S/C).
 * The constants are read from .txt files.
 * <p>
 * See {@link Gen2PaletteRandomizer} for an explanation on "bright colors" and
 * "dark colors".
 */
public class Gen2TypeColors {

	private Gen2TypeColors() {
	}

	private static final Map<Type, TypeColor[]> BRIGHT_TYPE_COLORS = TypeColor
			.readTypeColorMapFromFile("data/Gen2BrightTypeColors.txt");
	private static final Map<Type, TypeColor[]> DARK_TYPE_COLORS = TypeColor
			.readTypeColorMapFromFile("data/Gen2DarkTypeColors.txt");
	private static final Color DEFAULT_BRIGHT_COLOR = new Color(0xC0C0C0);
	private static final Color DEFAULT_DARK_COLOR = new Color(0x808080);

	public static TypeColor getRandomBrightColor(Type type, Random random) {
		TypeColor[] typeColors = BRIGHT_TYPE_COLORS.get(type);
		TypeColor color = typeColors == null ? new TypeColor(DEFAULT_BRIGHT_COLOR, type)
				: typeColors[random.nextInt(typeColors.length)];
		return color;
	}

	public static TypeColor getRandomDarkColor(Type type, Random random) {
		TypeColor[] typeColors = DARK_TYPE_COLORS.get(type);
		TypeColor color = typeColors == null ? new TypeColor(DEFAULT_DARK_COLOR, type)
				: typeColors[random.nextInt(typeColors.length)];
		return color;
	}

	private static RandomColorSelector brightColorSelector = new RandomColorSelector(new Random(),
	RandomColorSelector.Mode.HSV, hsv -> {
		double w = hsv[1] + hsv[2] * 0.5;
		if (20 <= hsv[0] && hsv[0] <= 70) {
			w *= 3;
		}
		return w;
	}, new double[] { 0, 0, 0.6 }, new double[] { 360, 1, 1 });
	private static RandomColorSelector darkColorSelector = new RandomColorSelector(new Random(),
	RandomColorSelector.Mode.HSV, hsv -> hsv[1] / 2, new double[] { 0, 0, 0.5 }, new double[] { 360, 1, 0.8 });

	public static TypeColor getRandomBrightColor(Random random) {
		brightColorSelector.setRandom(random);
		return new TypeColor(brightColorSelector.getRandomColor(), null);
	}

	public static TypeColor getRandomDarkColor(Random random) {
		darkColorSelector.setRandom(random);
		return new TypeColor(darkColorSelector.getRandomColor(), null);
	}

}
