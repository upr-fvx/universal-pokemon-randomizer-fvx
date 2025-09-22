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

import com.dabomstew.pkromio.gamedata.Type;

import java.util.Map;
import java.util.Random;

/**
 * Contains methods for accessing TypeColor constants for Gen 3-5 games. The constants
 * are read from a .txt file.
 */
public class Gen3to5TypeColors {

	private static final Map<Type, TypeColor[]> TYPE_COLORS = TypeColor
			.readTypeColorMapFromFile("data/Gen3to5TypeColors.txt");
	private static final Color DEFAULT_COLOR = new Color(0xC0C0C0);

	public static TypeColor getRandomTypeColor(Type type, Random random) {
		TypeColor[] typeColors = TYPE_COLORS.get(type);
		TypeColor color = typeColors == null ? new TypeColor(DEFAULT_COLOR, type)
				: typeColors[random.nextInt(typeColors.length)];
		return color;
	}
	
	private static RandomColorSelector randomColorSelector = new RandomColorSelector(new Random(),
	RandomColorSelector.Mode.HSV, hsv -> {
		double w = hsv[1];
		if (20 <= hsv[0] && hsv[0] <= 70) {
			w *= 3;
		}
		return w;
	}, new double[] { 0, 0, 0.6 }, new double[] { 360, 1, 1 });

	// TODO: something about too similar colors being chosen for the same mon?
	public static TypeColor getRandomTypeColor(Random random) {
		randomColorSelector.setRandom(random);
		return new TypeColor(randomColorSelector.getRandomColor(), null);
	}

}
