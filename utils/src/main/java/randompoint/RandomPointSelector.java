package randompoint;

/*----------------------------------------------------------------------------*/
/*--  RandomPointer.java  													--*/
/*--  Copyright (C) voliol 2022.                     						--*/
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

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

/**
 * Used to select random points in n-dimensional spaces, according to a weight
 * function. This is done by picking completely random points, until it finds
 * one "under" a n-dimensional curve created by the weight function. As such,
 * the time it takes depends on the weight function.
 *
 */
public class RandomPointSelector {

	private static final int MAX_VALUE_SAMPLE_NUM = 100000;
	private static final int MAX_ATTEMPTS = 200;

	private class Point {
		private final double[] coordinates;
		private final double value;

		public Point() {
			coordinates = randomCoordinatesWithinBounds();
			value = random.nextDouble() * getMaxValue();

		}

		@Override
		public String toString() {
			return Arrays.toString(coordinates) + "->" + value;
		}
	}

	private Random random;

	private int dimensions;
	private double[] lowerBounds;
	private double[] upperBounds;
	private double maxValue;

	private Function<double[], Double> weightFunction;

	private int tries;
	private int hits;

	/**
	 * Constructs a new RandomPointSelector.
	 * 
	 * @param random         A {@link Random} object.
	 * @param dimensions     The number of dimensions of the point to be selected.
	 * @param lowerBounds    The lower bound for each dimension, in order.
	 * @param upperBounds    The upper bound for each dimension, in order.
	 * @param weightFunction A function deciding the weight (chance of being
	 *                       selected) of each point.
	 */
	public RandomPointSelector(Random random, int dimensions, double lowerBounds[], double upperBounds[],
			Function<double[], Double> weightFunction) {
		this.random = random;
		this.dimensions = dimensions;

		if (lowerBounds.length != getDimensions()) {
			throw new IllegalArgumentException(
					"lowerBounds array must contain as many elements as there are dimensions");
		}
		this.lowerBounds = lowerBounds;
		if (upperBounds.length != getDimensions()) {
			throw new IllegalArgumentException(
					"upperBounds array must contain as many elements as there are dimensions");
		}
		this.upperBounds = upperBounds;

		this.weightFunction = weightFunction;
		this.maxValue = generateMaxValueBySample(MAX_VALUE_SAMPLE_NUM);
		System.out.println(maxValue);
	}

	private double generateMaxValueBySample(int sampleNum) {
		double max = 0;
		for (int i = 0; i < sampleNum; i++) {
			double value = curveValueAt(randomCoordinatesWithinBounds());
			max = Math.max(max, value);
		}
		return max;
	}

	private double[] randomCoordinatesWithinBounds() {
		double[] coordinates = new double[getDimensions()];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i] = random.nextDouble() * (upperBounds[i] - lowerBounds[i]) + lowerBounds[i];
		}
		return coordinates;
	}

	private int getDimensions() {
		return dimensions;
	}

	private double getMaxValue() {
		return maxValue;
	}

	public double[] getRandomPoint() {

		Point point = new Point();
		tries++;
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			if (underCurve(point)) {
				hits++;
				return point.coordinates;
			}
			point = new Point();
			tries++;
		}
		throw new RuntimeException("Picking point took too long, exceeded " + MAX_ATTEMPTS + " attempts.");
	}

	private boolean underCurve(Point point) {
		return (point.value < curveValueAt(point.coordinates));
	}

	private double curveValueAt(double[] coordinates) {
		return weightFunction.apply(coordinates);
	}

	/**
	 * Returns a number between 0-1, representing the relative "size" of the area
	 * under the n-dimensional curve. Basically the chance of each attempt at
	 * picking a point succeeding. <br>
	 * If this number is low, picking a random point may take longer time, or even
	 * time-out.<br>
	 * The number is procedurally updated as the RandomPointSelector is used; the
	 * more points selected, the more accurate it will be. If no points have been
	 * selected, the number will always be 0.
	 */
	public double getRelativeMeasureUnderCurve() {
		if (tries == 0) {
			return 0;
		}
		return ((double) hits) / ((double) tries);
	}

	public Random getRandom() {
		return random;
	}
	
	public void setRandom(Random random) {
		this.random = random;
	}

}
