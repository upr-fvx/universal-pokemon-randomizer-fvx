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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a parsed description for a part of a palette, for a
 * {@link PalettePopulator} to use. These describe e.g. what slots/{@link Color}
 * indices of a given {@link Palette} should be grouped together, or when the
 * Color in one slot should be the average of some other.
 * <p>
 * For the syntax, see {@link #PalettePartDescription(String)}.
 */
public class PalettePartDescription {

	private enum CharType {
		DIGIT, LETTER, SIBLING_DELIMITER, IGNORE;

		public static CharType of(char c) {
			if (Character.isDigit(c)) {
				return DIGIT;
			} else if (Character.isLetter(c)) {
				return LETTER;
			} else if (c == ';') {
				return SIBLING_DELIMITER;
			} else {
				return IGNORE;
			}
		}
	}

	private enum LightDarkSuffix {
		ANY, LIGHT, DARK, BASE, NO_LIGHT, NO_DARK
	}

	private boolean isBlank;

	private List<Integer> slots = new ArrayList<>();

	private List<Integer> siblingSlots = new ArrayList<>();
	private int sharedSlot = -1;

	private List<Integer> averageSlots = new ArrayList<>();

	private LightDarkSuffix lightDarkSuffix = LightDarkSuffix.ANY;
	private boolean endDarkened;

	public static PalettePartDescription[] allFrom(PaletteDescription paletteDescription) {
		return allFrom(paletteDescription.getBody());
	}
	
	public static PalettePartDescription[] allFrom(String paletteDescriptionBody) {
		String[] unparsedPartDescriptions = paletteDescriptionBody.split("/");

		PalettePartDescription[] partDescriptions = new PalettePartDescription[unparsedPartDescriptions.length];
		for (int i = 0; i < partDescriptions.length; i++) {
			partDescriptions[i] = new PalettePartDescription(unparsedPartDescriptions[i]);
		}

		return partDescriptions;
	}

	/**
	 * Constructs a PalettePartDescription from a String
	 * @param unparsed The String containing the description pre-parsing. Its syntax
	 *                 is described below.
	 *                 <br><br>
	 *                 Color slots are listed as comma-separated integers,
	 *                 representing locations in a certain Palette.
	 *                 <br><br>
	 *                 Color slots range from 1 to the size of the Palette (normally
	 *                 16). For images that have a transparent background color,
	 *                 e.g. those of Pokémon, slot 1 is used for that color. Slots
	 *                 are stored internally and returned as starting from 0, each
	 *                 slot in the unparsed String being offset by -1.
	 *                 <br><br>
	 *                 Color slots are ordered from brightest to darkest.
	 *                 <br><br>
	 *                 If you use "0" as a color slot, a shade is generated but
	 *                 not used in the final palette. This can e.g. be used to prevent
	 *                 the first color slot to be a highlight by putting "0," before it,
	 *                 or otherwise ensure color slots are as bright/dark/contrasted
	 *                 as they should be. Very useful for Gen 5 palettes.
	 *                 <br><br>
	 *                 The following letters also have syntactic meaning. They
	 *                 should be placed at the end of unparsed strings; letters at
	 *                 the start are ignored to allow for notes e.g. what palette
	 *                 the description pertains.
	 *                 <br><br>
	 *                 <b>L</b> - Ensures the color is light.<br>
	 *                 <b>LN</b> - Ensures the color is not light (i.e. dark or
	 *                 default).<br>
	 *                 <b>D</b> - Ensures the color is dark.<br>
	 *                 <b>DN</b> - Ensures the color is not dark (i.e. light or
	 *                 default).<br>
	 *                 <b>B</b> - Ensures the color is default (i.e. neither light
	 *                 nor dark)<br>
	 *                 <b>E</b> - Darkens the last/darkest shade of a color
	 *                 further.<br>
	 *                 <b>A</b> - Marks an "average color" description, see below.
	 *                 <br><br>
	 *                 Sibling colors are started with a ";". The shared color
	 *                 should be repeated at the end preceded by a "-". E.g.
	 *                 "1,2,3,4;5,6,4-4". Keep in mind second sibling colors
	 *                 tend to be less saturated (more gray) than other colors.
	 *                 <br><br>
	 *                 Average color descriptions start with an "A", and mark that
	 *                 the color in the first given color slot should be the average
	 *                 of the colors in the following color slots. Average color
	 *                 descriptions are not compatible with sibling colors.
	 */
	public PalettePartDescription(String unparsed) {
		isBlank = unparsed.matches("\\s*");
		List<String> tokens = splitIntoTokens(unparsed);
		parseTokens(tokens);
	}

	private List<String> splitIntoTokens(String unparsed) {
		List<String> tokens = new ArrayList<>();

		StringBuilder sb = new StringBuilder();
		char last = ' ';
		char current;
		for (int i = 0; i < unparsed.length(); i++) {

			current = unparsed.charAt(i);
			CharType charType = CharType.of(current);

			if (charType != CharType.IGNORE) {
				if (charType != CharType.of(last)) {
					tokens.add(sb.toString());
					sb = new StringBuilder();
				}
				sb.append(current);
			}
			last = current;
		}
		tokens.add(sb.toString());

		return tokens;
	}

	private void parseTokens(List<String> tokens) {
		List<Integer> currentSlots = slots;
		for (String token : tokens) {

			if (token.matches("[0-9]+")) {
				currentSlots.add(Integer.parseInt(token) - 1);
			}

			else if (token.equals("L")) {
				lightDarkSuffix = LightDarkSuffix.LIGHT;
			}

			else if (token.equals("D")) {
				lightDarkSuffix = LightDarkSuffix.DARK;
			}

			else if (token.equals("LN")) {
				lightDarkSuffix = LightDarkSuffix.NO_LIGHT;
			}

			else if (token.equals("DN")) {
				lightDarkSuffix = LightDarkSuffix.NO_DARK;
			}

			else if (token.equals("B")) {
				lightDarkSuffix = LightDarkSuffix.BASE;
			}

			else if (token.equals("E")) {
				endDarkened = true;
			}

			else if (token.equals("A")) {
				currentSlots = averageSlots;
			}

			else if (token.equals(";")) {
				currentSlots = siblingSlots;
			}

		}
		if (currentSlots == siblingSlots && !siblingSlots.isEmpty()) {
			sharedSlot = siblingSlots.get(siblingSlots.size() - 1);
			siblingSlots.remove(siblingSlots.size() - 1);
		}
	}

	public boolean isBlank() {
		return isBlank;
	}

	public boolean isEndDarkened() {
		return endDarkened;
	}

	public boolean hasSibling() {
		return !siblingSlots.isEmpty();
	}

	public boolean isAverageDescription() {
		return !averageSlots.isEmpty();
	}

	public int length() {
		return slots.size();
	}

	public int siblingLength() {
		return siblingSlots.size();
	}

	public int[] getSlots() {
		int[] slotsArr = new int[slots.size()];
		for (int i = 0; i < slots.size(); i++) {
			slotsArr[i] = slots.get(i);
		}
		return slotsArr;
	}

	public int[] getSiblingSlots() {
		if (!hasSibling()) {
			throw new IllegalStateException("Can't get sibling slots when there is none.");
		}
		int[] slotsArr = new int[siblingSlots.size()];
		for (int i = 0; i < siblingSlots.size(); i++) {
			slotsArr[i] = siblingSlots.get(i);
		}
		return slotsArr;
	}

	public int getSharedSlot() {
		if (sharedSlot == -1) {
			throw new IllegalStateException("Shared color slot has not been set.");
		}
		return sharedSlot;
	}

	public int getAverageToSlot() {
		return averageSlots.get(0);
	}

	public int[] getAverageFromSlots() {
		int[] slotsArr = new int[averageSlots.size() - 1];
		for (int i = 1; i < averageSlots.size(); i++) {
			slotsArr[i - 1] = averageSlots.get(i);
		}
		return slotsArr;
	}

	/**
	 * Corrects a {@link LightDarkMode} to one permitted by this description. Dark
	 * is turned to Light and vice versa so the % of light or dark colors stays the
	 * same.
	 * 
	 * @param in A LightDarkMode that may or not be permitted by this description.
	 */
	public LightDarkMode correctLightDarkMode(LightDarkMode in) {
		switch (lightDarkSuffix) {
		case ANY:
			return in;
		case BASE:
			return LightDarkMode.DEFAULT;
		case DARK:
			return LightDarkMode.DARK;
		case LIGHT:
			return LightDarkMode.LIGHT;
		case NO_DARK:
			if (in == LightDarkMode.DARK) {
				return LightDarkMode.LIGHT;
			} else {
				return in;
			}
		case NO_LIGHT:
			if (in == LightDarkMode.LIGHT) {
				return LightDarkMode.DARK;
			} else {
				return in;
			}
		default:
			return in;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Desc(");
		if (isBlank()) {
			sb.append("BLANK");
		} else if (isAverageDescription()) {
			sb.append("AV");
		} else {
			sb.append(Arrays.toString(getSlots()));
			if (hasSibling()) {
				sb.append(" SIB:").append(Arrays.toString(getSiblingSlots()));
				sb.append(" SHARED:").append(getSharedSlot());
			}
			if (lightDarkSuffix != LightDarkSuffix.ANY) {
				sb.append(" ").append(lightDarkSuffix);
			}
			if (isEndDarkened()) {
				sb.append("END_DARKENED");
			}
		}
		sb.append(")");
		return sb.toString();

	}

}
