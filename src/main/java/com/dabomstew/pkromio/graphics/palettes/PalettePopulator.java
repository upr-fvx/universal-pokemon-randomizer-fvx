package com.dabomstew.pkromio.graphics.palettes;

import java.util.Arrays;
import java.util.Random;

/* PalettePopulator.java is a port of PokePalette.java from the Pokemon Emerald Randomizer (ER), by Artemis251, 
 * modified for the needs of the Universal Pokémon Randomizer (UPR).
 * 
 * Below is a section taken from codeReadMe.txt of the ER source code, 
 * which may constitute a license. Parts omitted with "[...]" discuss
 * other topics.
 */

/* ===========================================
 * Pokemon Emerald Randomizer Source Code
 * ===========================================
 * v.2.2 -- 13 April 2014
 * Original code by Artemis251
 * 
 * [...]
 * 
 * In any case, feel free to tweak the code to your heart's content. I do
 * have a few ideas of how to take the current program further, but I lack
 * the free time to really implement them. Feel free to do so on your own!
 * I'm not sure what the whole protocol is for open sourcin' code in terms
 * of gettin' a central repository (do people use some kinda globally-
 * accessible git/svn or somethin'?), but you're free to redistribute your
 * own changes and whatnot. It's all for the greater good of players! [...]
 */

/* Permission has also been given expressly to reuse code of the ER in the UPR.
 * Below is an abridged version of the Reddit conversation where permission
 * was given, with a fuller version seen in this screenshot: 
 * https://i.imgur.com/hEzrP4Z.png [taken 2022-04-10]
 * (the conversation goes on after that, but licensing is not discussed more) 
 * 
 * voliol:
 * "Your Readme for the Emerald Randomizer states that "you're free to 
 *  redistribute your own changes and whatnot". However, considering I would be 
 *  porting parts of it to another project altogether with a license of its own 
 *  (GNU General Public License v3.0) I believed I should still ask for 
 *  permission either way. So are you okay with it, me porting 
 *  [the palette randomization] to the UPR?"
 *  
 * Artemis251:
 * "I'd be happy to see the palette randomization added to the much-more-mature 
 *  UPR. Feel free to grab whatever you can from my existing code and reuse it. 
 *  I'm pretty sure most people use the UPR over my original attempts nowadays 
 *  anyway. It'd be nice to be credited somewhere at the least, mostly because 
 *  the Emerald palette thing was such a pain to accomplish."
 */

/**
 * Populates/fills/modifies existing {@link Palette}s, using
 * {@link PalettePartDescription}s as instructions.
 * <p>
 * This class is a port of the corresponding class (PokePalette.java) in
 * Artemis251's Emerald Randomizer.
 */
public class PalettePopulator {

	private final int VARIA = 10; // 3 of these fuel the variation for sibling palettes - artemis
	private final int VARIABASE = 3; // base forced change for sibling palettes - artemis
	private static final int TOTAL_CHANGE_THRESHOLD = 9; // threshold for color changes to meet before stopping their
															// seed - artemis

	private static int indexOf(int[] ar, int src) {
		for (int i = 0; i < ar.length; i++) {
			if (ar[i] == src)
				return i;
		}
		return -1;
	}

	private Random random;

	private Palette palette;
	private PalettePartDescription description;
	private Color baseColor;
	private LightDarkMode lightDarkMode;

	private double[] leftShift;
	private double[] rightShift;

	public PalettePopulator(Random random) {
		this.random = random;
	}

	public void populatePartFromBaseColor(Palette palette, PalettePartDescription description, Color baseColor,
			LightDarkMode lightDarkMode) {

		this.palette = palette;

		this.description = description;

		this.baseColor = new Color(baseColor);

		this.lightDarkMode = description.correctLightDarkMode(lightDarkMode);

		if (description.length() != 0) {
			Color[] shades = makeShades();
			fillWithShades(shades);
		}

		if (description.hasSibling() && description.siblingLength() != 0) {
			Color sharedColor = getSiblingColor(palette, baseColor);
			// TODO: refactor so makeSiblingShades and makeShades have similar parameters,
			// or are merged into one method
			Color[] siblingShades = makeSiblingShades(sharedColor, description.getSiblingSlots(),
					description.getSharedSlot());

			fillWithShades(siblingShades);
		}
	}

	private Color getSiblingColor(Palette palette, Color baseColor) {
		Color sharedColor = new Color(palette.get(description.getSharedSlot()));

		if (sharedColor.getComp(0) <= 3 && sharedColor.getComp(1) <= 3 && sharedColor.getComp(2) <= 3) {
			sharedColor.setComps((orig, i) -> (int) (baseColor.getComp(i) * 0.15));
		} else if (sharedColor.getComp(0) >= 252 && sharedColor.getComp(1) >= 252 && sharedColor.getComp(2) >= 252) {
			sharedColor.setComps((orig, i) -> (int) (baseColor.getComp(i) * 0.85));
		}
		return sharedColor;
	}

	private void fillWithShades(Color[] shades) {
		for (int i = 0; i < shades.length; i++) {
			if (shades[i] != null) {
				palette.set(i, shades[i]);
			}
		}
	}

	private Color[] makeShades() {
		Color[] sorted = new Color[description.length()];
		for (int i = 0; i < sorted.length; i++) {
			sorted[i] = new Color();
		}

		initializeLeftRightShift();

		makeBaseColorLightOrDark();

		makeMiddleShadeOrShades(sorted);
		makeLeftShades(sorted, sorted.length / 2 - (isSlotsEven() ? 2 : 1));
		makeRightShades(sorted, sorted.length / 2 + 1);

		if (description.isEndDarkened()) {
			makeEndShadeDarkened(sorted);
		}

		Color[] out = fitShadesInSlots(sorted, description.getSlots());
		return out;
	}

	private Color[] makeSiblingShades(Color sharedCol, int[] slots, int shared) {
		int sharedLoc = indexOf(slots, shared);
		Color[] sorted = new Color[slots.length];
		for (int i = 0; i < sorted.length; i++) {
			sorted[i] = new Color();
		}

		double[] variation = {
				(random.nextInt(VARIA) + random.nextInt(VARIA) + random.nextInt(VARIA) + VARIABASE) * 0.01
						* (Math.pow(-1, (random.nextInt(2)))),
				(random.nextInt(VARIA) + random.nextInt(VARIA) + random.nextInt(VARIA) + VARIABASE) * 0.01
						* (Math.pow(-1, (random.nextInt(2)))),
				(random.nextInt(VARIA) + random.nextInt(VARIA) + random.nextInt(VARIA) + VARIABASE) * 0.01
						* (Math.pow(-1, (random.nextInt(2)))) };
		leftShift = new double[] { getLeftColorChange(sharedLoc, sharedCol.getComp(0)),
				getLeftColorChange(sharedLoc, sharedCol.getComp(1)),
				getLeftColorChange(sharedLoc, sharedCol.getComp(2)) };
		rightShift = new double[] { getRightColorChange((slots.length - sharedLoc - 1), sharedCol.getComp(0)),
				getRightColorChange((slots.length - sharedLoc - 1), sharedCol.getComp(1)),
				getRightColorChange((slots.length - sharedLoc - 1), sharedCol.getComp(2)) };

		// place shared color first
		sorted[sharedLoc] = sharedCol;

		// place shade left and right of shared
		if (sharedLoc - 1 >= 0) {
			sorted[sharedLoc - 1]
					.setComps((orig, i) -> (int) (sorted[sharedLoc].getComp(i) + leftShift[i] * (1 + variation[i])));
		}
		if (sharedLoc + 1 < sorted.length) {
			sorted[sharedLoc + 1]
					.setComps((orig, i) -> (int) (sorted[sharedLoc].getComp(i) - rightShift[i] * (1 + variation[i])));
		}

		makeLeftShades(sorted, sharedLoc - 2);
		makeRightShades(sorted, sharedLoc + 2);

		Color[] out = fitShadesInSlots(sorted, description.getSiblingSlots());
		return out;
	}

	private void makeBaseColorLightOrDark() {
		boolean rndcha = false;
		// TODO: figure out what rndcha is - it seems if it is true, then only some are
		// darkened/lightened. Does it stand for "random chance"?
		// is it not redundant with/did it originally use LIGHTDARK_CHANCE ?
		switch (lightDarkMode) {
		case DARK:
			darkenBaseColor(rndcha);
			darkenLeftRightShift();
			break;
		case LIGHT:
			lightenBaseColor(rndcha);
			lightenLeftRightShift();
			break;
		case DEFAULT:
			break;
		}
	}

	private void darkenBaseColor(boolean rndcha) {
		double mod;
		if (rndcha) {
			mod = 0.2;
		} else {
			mod = 0.5;
		}
		baseColor.setComps((orig, i) -> (int) (orig.getComp(i) - rightShift[i] * (description.length() / 2 + 1) * mod));
	}

	private void darkenLeftRightShift() {
		leftShift[0] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(0));
		leftShift[1] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(1));
		leftShift[2] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(2));
		rightShift[0] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(0));
		rightShift[1] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(1));
		rightShift[2] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(2));
	}

	private void lightenBaseColor(boolean rndcha) {
		double mod;
		if (rndcha) {
			mod = 0.2;
		} else {
			mod = 0.5;
		}
		baseColor.setComps((orig, i) -> (int) (orig.getComp(i) + leftShift[i] * (description.length() / 2 + 1) * mod));
	}

	private void lightenLeftRightShift() {
		leftShift[0] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(0));
		leftShift[1] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(1));
		leftShift[2] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(2));
		rightShift[0] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(0));
		rightShift[1] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(1));
		rightShift[2] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(2));

		double mod = lightDarkenCoeff();
		for (int i = 0; i < rightShift.length; i++) {
			rightShift[i] += mod * getLeftColorChange(getMiddleSlotID(), baseColor.getComp(i));
		}
	}

	private double lightDarkenCoeff() {
		int[] bc = baseColor.toInts();
		Arrays.sort(bc);
		double avg = (bc[1] + bc[2]) / 2.0;

		if (baseColor.getComp(1) >= 225 && baseColor.getComp(0) < 200 && baseColor.getComp(2) < 200)
			avg += 75; // 45
		if (baseColor.getComp(1) >= 225)
			avg += 75;
		if (avg > 255)
			avg = 255;
		if (avg < 192) {
			return 0.5;
		} else {
			return (0.0002 * Math.pow(avg - 192, 3.5) + 50) * 0.01;
		}
	}

	private void initializeLeftRightShift() {
		leftShift = new double[3];
		leftShift[0] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(0));
		leftShift[1] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(1));
		leftShift[2] = getLeftColorChange(getMiddleSlotID(), baseColor.getComp(2));
		rightShift = new double[3];
		rightShift[0] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(0));
		rightShift[1] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(1));
		rightShift[2] = getRightColorChange(getMiddleSlotID(), baseColor.getComp(2));
	}

	private int getMiddleSlotID() {
		return (description.length() - (isSlotsEven() ? 0 : 1)) / 2;
	}

	private Color[] fitShadesInSlots(Color[] sorted, int[] slots) {
		Color[] out = new Color[palette.size()];
		for (int i = 0; i < slots.length; i++) {
			if (slots[i] != -1) { // permits the "0"-trick in PalettePartDescriptions
				out[slots[i]] = sorted[i];
			}
		}
		return out;
	}

	private void makeMiddleShadeOrShades(Color[] sorted) {
		if (isSlotsEven()) {
			for (int totalChange = 0, r = 0; totalChange < TOTAL_CHANGE_THRESHOLD && r < 5; r++) {
				totalChange = 0;
				for (int p = 0; p < 3; p++) {

					int leftValue = (int) (baseColor.getComp(p) + leftShift[p] * 0.5);
					leftValue = Math.min(leftValue, 255);
					sorted[sorted.length / 2 - 1].setComp(p, leftValue);

					int rightValue = (int) (baseColor.getComp(p) - rightShift[p] * 0.5);
					rightValue = Math.max(rightValue, 0);
					sorted[sorted.length / 2].setComp(p, rightValue);

					totalChange += (sorted[sorted.length / 2].getComp(p) >> 2)
							- (sorted[sorted.length / 2 - 1].getComp(p) >> 2);
				}
			}
		} else {
			sorted[sorted.length / 2] = baseColor;
		}
	}

	private void makeLeftShades(Color[] sorted, int rightBound) {
		// make all colors left of middle
		for (int ptr = rightBound; ptr >= 0; ptr--) {
			int right = ptr + 1;
			sorted[ptr].setComps((orig, i) -> (int) (sorted[right].getComp(i) + leftShift[i]));
		}
	}

	private void makeRightShades(Color[] sorted, int leftBound) {
		// make all colors right of middle
		for (int ptr = leftBound; ptr < sorted.length; ptr++) {
			int left = ptr - 1;
			sorted[ptr].setComps((orig, i) -> (int) (sorted[left].getComp(i) - rightShift[i]));
		}
	}

	private void makeEndShadeDarkened(Color[] sorted) {
		int ptr = sorted.length - 1;
		for (int j = 0; j < 1; j++) { // can be adjusted to darken more
			sorted[ptr].setComps((orig, i) -> (int) (orig.getComp(i) - rightShift[i]));
		}
	}

	private double getLeftColorChange(int palLimit, int maxRGB) {
		if (palLimit == 0)
			return 0;
		double coeff = (random.nextInt(5) + random.nextInt(5) + random.nextInt(5) + 153) / (palLimit);
		return coeff - coeff / 255 * maxRGB + palLimit * palLimit;
	}

	private double getRightColorChange(int palLimit, int maxRGB) {
		if (palLimit == 0)
			return 0;
		double coeff = (random.nextInt(5) + random.nextInt(5) + random.nextInt(5) + 153) / (palLimit);
		return coeff - coeff / 255 * (255 - maxRGB) + palLimit * palLimit;
	}

	private boolean isSlotsEven() {
		return description.length() % 2 == 0;
	}

	public void populateAverageColor(Palette palette, PalettePartDescription description) {
		Color averageTo = palette.get(description.getAverageToSlot());
		int[] averageFromSlots = description.getAverageFromSlots();
		averageTo.setComps((orig, i) -> {
			int sum = 0;
			for (int slot : averageFromSlots) {
				sum += palette.get(slot).getComp(i);
			}
			return sum / averageFromSlots.length;
		});
	}

}
