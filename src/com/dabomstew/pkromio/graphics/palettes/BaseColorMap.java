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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Used to tweak {@link TypeColor}s, by mapping each to a {@link Color} derived
 * from it, and a {@link LightDarkMode}.
 */
public class BaseColorMap {

	private static final double TWEAK_RAND_MIN_COEFF = -0.032;
	private static final double TWEAK_RAND_MIN_ADDEND = 7.1;
	private static final double TWEAK_RAND_MAX_COEFF = -0.042;
	private static final double TWEAK_RAND_MAX_ADDEND = 17.19;

	private final Random random;

	private final Map<TypeColor, Color> baseColors = new HashMap<>();
	private final Map<TypeColor, LightDarkMode> lightDarkModes = new HashMap<>();

	public BaseColorMap(Random random) {
		this.random = random;
	}

	public Color getBaseColor(TypeColor key) {
		if (!baseColors.containsKey(key)) {
			add(key);
		}
		// unsure if this is where to put copy protection
		return new Color(baseColors.get(key));
	}

	public LightDarkMode getLightDarkMode(TypeColor key) {
		if (!baseColors.containsKey(key)) {
			add(key);
		}
		return lightDarkModes.get(key);
	}

	private void add(TypeColor key) {
		baseColors.put(key, randomlyTweakColor(key));
		lightDarkModes.put(key, LightDarkMode.randomLightDarkMode(random));
	}

	private Color randomlyTweakColor(Color color) {
		// based on Artemis251's Emerald Randomizer code
		Color tweakedColor = new Color();
		for (int i = 0; i < 3; i++) { // for each in R,G,B
			int randMin = (int) (TWEAK_RAND_MIN_COEFF * color.getComp(i) + TWEAK_RAND_MIN_ADDEND);
			int randMax = (int) (TWEAK_RAND_MAX_COEFF * color.getComp(i) + TWEAK_RAND_MAX_ADDEND);
			int change = random.nextInt(randMax - randMin) + random.nextInt(randMax);
			int value = color.getComp(i) + (int) (color.getComp(i) * change * 0.01 * Math.pow(-1, random.nextInt(2)));
			value = Math.min(value, 255);
			tweakedColor.setComp(i, value);
		}
		return tweakedColor;
	}

}
