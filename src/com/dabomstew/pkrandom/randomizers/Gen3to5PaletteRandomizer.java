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
import com.dabomstew.pkrandom.exceptions.RandomizationException;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.cueh.BasicSpeciesAction;
import com.dabomstew.pkromio.gamedata.cueh.EvolvedSpeciesAction;
import com.dabomstew.pkromio.graphics.palettes.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * A {@link PaletteRandomizer} for Gen 3, Gen 4, and Gen 5 games (R/S/E/FR/LG,
 * D/P/Pt/HG/SS, B/W/B2/W2).
 * <p>
 * All three generations use similar 16-color palettes, that are implemented
 * using the {@link Palette} class. These palettes are populated/filled/modified
 * by {@link PalettePopulator}, using {@link PalettePartDescription}s as
 * instructions.
 * <p>
 * When Pokémon palettes are randomized, each Pokémon is assigned a
 * {@link TypeBaseColorList}, which uses its types to come up with appropriate
 * base colors.
 */
public class Gen3to5PaletteRandomizer extends PaletteRandomizer {

	/**
	 * A setting to use when testing the palette description files.
	 * If false, the corresponding source file is read instead of the compiled resource.
	 */
	private final static boolean COMPILED = true;

	/**
	 * An identifier for the related resource files. ROMs that share a
	 * paletteFilesID also share all resources. If they shouldn't, different ROMs
	 * must be assigned separate IDs.
	 */
	private final String paletteFilesID;

	private boolean typeSanity;
	private boolean shinyFromNormal;
	private Map<Species, TypeBaseColorList> typeBaseColorLists;

	public Gen3to5PaletteRandomizer(RomHandler romHandler, Settings settings, Random random) {
		super(romHandler, settings, random);
		this.paletteFilesID = romHandler.getPaletteFilesID();
	}

	@Override
	public void randomizePokemonPalettes() {

		// TODO: Figure out what to do with forms, with different palettes and with the same.
		// TODO: figure out genders in gen V, if anything needs to be done at all

		this.typeSanity = settings.isPokemonPalettesFollowTypes();
		this.shinyFromNormal = settings.isPokemonPalettesShinyFromNormal();
		boolean evolutionSanity = settings.isPokemonPalettesFollowEvolutions();

		this.typeBaseColorLists = new HashMap<>();

		if (paletteFilesID == null) {
			// TODO: this is the kind of exception which the user could basically ignore, the ROM is still
			//  fully functional. Is there a better way of logging that?
			//  e.g. - You click "randomize"
			//  - Something throws an exception here in the PaletteRandomizer
			//  - The Randomizer catches it and prints it to a log, doesn't write the palettes.
			//  - The rest of the randomization continues
			//  - It finishes and the end-user gets the pop-up message
			//  "The randomization finished, but with some errors. See..."
			throw new RandomizationException("Could not randomize palettes, unrecognized romtype.");
		}

		copyUpEvolutionsHelper.apply(evolutionSanity, true, new BasicSpeciesPaletteAction(),
				new EvolvedSpeciesPaletteAction());
		List<PaletteDescription> paletteDescriptions = getPaletteDescriptions("pokePalettes");
		populatePokemonPalettes(paletteDescriptions);

	}

	private void populatePokemonPalettes(List<PaletteDescription> paletteDescriptions) {

		PalettePopulator pp = new PalettePopulator(random);

		for (Entry<Species, TypeBaseColorList> entry : typeBaseColorLists.entrySet()) {

			Species pk = entry.getKey();
			Palette palette = pk.getNormalPalette();
			TypeBaseColorList typeBaseColorList = entry.getValue();
			PalettePartDescription[] palettePartDescriptions = getPalettePartDescriptions(pk, paletteDescriptions);

			populatePalette(palette, pp, typeBaseColorList, palettePartDescriptions);

		}
	}

	public void populatePalette(Palette palette, PalettePopulator pp, TypeBaseColorList typeBaseColorList,
			PalettePartDescription[] palettePartDescriptions) {

		for (int i = 0; i < palettePartDescriptions.length; i++) {

			if (palettePartDescriptions[i].isAverageDescription()) {
				pp.populateAverageColor(palette, palettePartDescriptions[i]);

			} else if (!palettePartDescriptions[i].isBlank()) {
				Color baseColor = typeBaseColorList.getBaseColor(i);
				LightDarkMode lightDarkMode = typeBaseColorList.getLightDarkMode(i);
				pp.populatePartFromBaseColor(palette, palettePartDescriptions[i], baseColor, lightDarkMode);
			}

		}
	}

	public PalettePartDescription[] getPalettePartDescriptions(Species pk,
                                                               List<PaletteDescription> paletteDescriptions) {
		int paletteIndex = pk.getNumber() - 1;
		boolean validIndex = paletteIndex <= paletteDescriptions.size();
		return PalettePartDescription
				.allFrom(validIndex ? paletteDescriptions.get(paletteIndex) : PaletteDescription.BLANK);
	}

	/**
	 * Gets {@link PaletteDescription}s from a resource/file.
	 * 
	 * @param fileKey         The key to this particular kind of file, e.g.
	 *                        "pokePalettes".
	 */
	public List<PaletteDescription> getPaletteDescriptions(String fileKey) {
		List<PaletteDescription> paletteDescriptions = new ArrayList<>();

		Reader reader;
		if (COMPILED) {
			try {
				InputStream infi = getClass().getResourceAsStream(getResourceAddress(fileKey));
				reader = new InputStreamReader(infi);
			} catch (NullPointerException e) {
				throw new RuntimeException(new RuntimeException("Could not find resource " + getResourceAddress(fileKey), e));
			}
		} else {
			try {
				reader = new FileReader(getSourceFileAddress(fileKey));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		BufferedReader br = new BufferedReader(reader);

		String line;
		try {
			while ((line = br.readLine()) != null) {
				paletteDescriptions.add(new PaletteDescription(line));
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not read palette description file "
					+ (COMPILED ? getResourceAddress(fileKey) : getSourceFileAddress(fileKey)) + ".");
		}

		return paletteDescriptions;
	}

	public void savePaletteDescriptionSource(String fileKey, List<PaletteDescription> paletteDescriptions) {
		String fileAdress = getSourceFileAddress(fileKey);

		try (PrintWriter writer = new PrintWriter(new FileWriter(fileAdress))) {

			for (int i = 0; i < paletteDescriptions.size(); i++) {
				writer.print(paletteDescriptions.get(i).toFileFormattedString());
				if (i != paletteDescriptions.size() - 1) {
					writer.print("\n");
				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getFileName(String fileKey) {
		return fileKey + paletteFilesID + ".txt";
	}

	private String getResourceAddress(String fileKey) {
		return "/com/dabomstew/pkromio/graphics/resources/" + getFileName(fileKey);
	}

	private String getSourceFileAddress(String fileKey) {
		return "src/com/dabomstew/pkromio/graphics/resources/" + getFileName(fileKey);
	}

	private class BasicSpeciesPaletteAction implements BasicSpeciesAction<Species> {

		@Override
		public void applyTo(Species pk) {
			if (shinyFromNormal) {
				setShinyPaletteFromNormal(pk);
			}

			TypeBaseColorList typeBaseColorList = new TypeBaseColorList(pk, typeSanity, random);
			typeBaseColorLists.put(pk, typeBaseColorList);

		}

	}

	private class EvolvedSpeciesPaletteAction implements EvolvedSpeciesAction<Species> {

		@Override
		public void applyTo(Species evFrom, Species evTo, boolean toMonIsFinalEvo) {
			if (shinyFromNormal) {
				setShinyPaletteFromNormal(evTo);
			}
			TypeBaseColorList prevo = typeBaseColorLists.get(evFrom);
			TypeBaseColorList typeBaseColorList = new TypeBaseColorList(evTo, prevo, typeSanity, random);
			typeBaseColorLists.put(evTo, typeBaseColorList);

		}

	}

}
