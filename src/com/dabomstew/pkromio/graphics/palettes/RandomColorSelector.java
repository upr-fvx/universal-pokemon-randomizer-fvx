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

import randompoint.RandomPointSelector;

import java.util.Random;
import java.util.function.Function;

/**
 * RandomColorSelector can select a random {@link Color} according to some weight function.
 */
public class RandomColorSelector {

	public enum Mode {
		RGB, HSV
	}

	public static double[] defaultLowerBounds() {
		return new double[] { 0, 0, 0 };
	}

	public static double[] defaultUpperBounds(Mode mode) {
		switch (mode) {
			case HSV:
				return new double[]{360, 1, 1};
			case RGB:
				return new double[]{255, 255, 255};
		}
		throw new RuntimeException("Should not get here");
	}

	private final RandomPointSelector randomPointSelector;
	private final Mode mode;

	public RandomColorSelector(Random random, Mode mode, Function<double[], Double> weightFunction) {
		this(random, mode, weightFunction, defaultLowerBounds(), defaultUpperBounds(mode));
	}

	public RandomColorSelector(Random random, Mode mode, Function<double[], Double> weightFunction,
			double[] lowerBounds, double[] upperBounds) {
		this.mode = mode;
		this.randomPointSelector = new RandomPointSelector(random, 3, lowerBounds, upperBounds, weightFunction);
	}

	public Color getRandomColor() {
		double[] point = randomPointSelector.getRandomPoint();
		switch (mode) {
			case RGB:
				return new Color((int) point[0], (int) point[1], (int) point[2]);
			case HSV:
				return Color.colorFromHSV(point[0], point[1], point[2]);
		}
		throw new RuntimeException("Should not get here");
	}
	
	public Random getRandom() {
		return randomPointSelector.getRandom();
	}
	
	public void setRandom(Random random) {
		randomPointSelector.setRandom(random);
	}

}
