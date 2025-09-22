package com.dabomstew.pkromio.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
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

import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.Gen6Constants;
import com.dabomstew.pkromio.ctr.GARCArchive;
import com.dabomstew.pkromio.ctr.NCCH;
import com.dabomstew.pkromio.exceptions.CannotWriteToLocationException;
import com.dabomstew.pkromio.exceptions.EncryptedROMException;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.*;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * An abstract base class for 3DS {@link RomHandler}s, which standardises common 3DS functions.
 */
public abstract class Abstract3DSRomHandler extends AbstractRomHandler {

	private NCCH baseRom;
	private NCCH gameUpdate;
	private String loadedFN;

	@Override
	public boolean loadRom(String filename) {
		String productCode = getProductCodeFromFile(filename);
		String titleId = getTitleIdFromFile(filename);
		if (!this.detect3DSRom(productCode, titleId)) {
			return false;
		}
		// Load inner rom
		try {
			baseRom = new NCCH(filename, productCode, titleId);
			if (!baseRom.isDecrypted()) {
				throw new EncryptedROMException(filename);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		loadedFN = filename;
		this.loadedROM(productCode, titleId);
		return true;
	}

	protected abstract boolean detect3DSRom(String productCode, String titleId);

	@Override
	public String loadedFilename() {
		return loadedFN;
	}

	protected abstract void loadedROM(String productCode, String titleId);


	protected abstract String getGameAcronym();

	@Override
	public boolean saveRomFile(String filename, long seed) {
		try {
			baseRom.saveAsNCCH(filename, getGameAcronym(), seed);
		} catch (IOException | NoSuchAlgorithmException e) {
			if (e.getMessage().contains("Access is denied")) {
				throw new CannotWriteToLocationException("The randomizer cannot write to this location: " + filename);
			} else {
				throw new RomIOException(e);
			}
		}
		return true;
	}

	@Override
	public boolean saveRomDirectory(String filename) {
		try {
			baseRom.saveAsLayeredFS(filename);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		return true;
	}

	protected abstract boolean isGameUpdateSupported(int version);

	@Override
	public boolean hasGameUpdateLoaded() {
		return gameUpdate != null;
	}

	@Override
	public boolean loadGameUpdate(String filename) {
		String productCode = getProductCodeFromFile(filename);
		String titleId = getTitleIdFromFile(filename);
		try {
			gameUpdate = new NCCH(filename, productCode, titleId);
			if (!gameUpdate.isDecrypted()) {
				throw new EncryptedROMException(filename);
			}
			int version = gameUpdate.getVersion();
			if (!this.isGameUpdateSupported(version)) {
				System.out.println("Game Update: Supplied unexpected version " + version);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		this.loadedROM(baseRom.getProductCode(), baseRom.getTitleId());
		return true;
	}

	@Override
	public void removeGameUpdate() {
		gameUpdate = null;
		this.loadedROM(baseRom.getProductCode(), baseRom.getTitleId());
	}

	protected abstract String getGameVersion();

	@Override
	public String getGameUpdateVersion() {
		return getGameVersion();
	}

	@Override
	public void printRomDiagnostics(PrintStream logStream) {
		baseRom.printRomDiagnostics(logStream, gameUpdate);
	}

	public void closeInnerRom() throws IOException {
		baseRom.closeROM();
	}

	@Override
	public boolean hasPhysicalSpecialSplit() {
		// Default value for Gen4+.
		// Handlers can override again in case of ROM hacks etc.
		return true;
	}

	public void findTypeTable() {
		TypeTable typeTable = TypeTable.getVanillaGen6PlusTable();

		int tableWidth = typeTable.getTypes().size();
		byte[] tablebytes = new byte[tableWidth * tableWidth];

		for (Type attacker : typeTable.getTypes()) {
			for (Type defender : typeTable.getTypes()) {
				int offset = (Gen6Constants.typeToByte(attacker) * tableWidth) + Gen6Constants.typeToByte(defender);
				Effectiveness effectiveness = typeTable.getEffectiveness(attacker, defender);
				byte effectivenessInternal;
				switch (effectiveness) {
					case DOUBLE:
						effectivenessInternal = 8;
						break;
					case NEUTRAL:
						effectivenessInternal = 4;
						break;
					case HALF:
						effectivenessInternal = 2;
						break;
					case ZERO:
						effectivenessInternal = 0;
						break;
					default:
						effectivenessInternal = 0;
				}
				tablebytes[offset] = effectivenessInternal;
			}
		}

		System.out.println(bytesToHex(tablebytes));
		Map<String, List<Integer>> found = new TreeMap<>();

		for (String fileName : baseRom.getFileNames()) {
			System.out.println(fileName);
			if (fileName.startsWith("a"))
				continue;

			try {
				byte[] file = readFile(fileName);
				List<Integer> offsets = RomFunctions.search(file, tablebytes);
				if (!offsets.isEmpty()) {
					System.out.println(offsets);
					found.put(fileName, offsets);
				}
			} catch (Exception ignored) {
				System.out.println("could not read GARC");
			}
		}


		for (Map.Entry<String, List<Integer>> entry : found.entrySet()) {
			System.out.println(entry.getKey());
			for (Integer offset : entry.getValue()) {
				System.out.println("\t0x" + Integer.toHexString(offset));
			}
		}
	}

	protected byte[] readCode() throws IOException {
		if (gameUpdate != null) {
			return gameUpdate.getCode();
		}
		return baseRom.getCode();
	}

	protected void writeCode(byte[] data) throws IOException {
		baseRom.writeCode(data);
	}
	protected GARCArchive readGARC(String subpath, boolean skipDecompression) throws IOException {
		return new GARCArchive(readFile(subpath), skipDecompression);
	}

	protected GARCArchive readGARC(String subpath, List<Boolean> compressThese) throws IOException {
		return new GARCArchive(readFile(subpath), compressThese);
	}

	protected void writeGARC(String subpath, GARCArchive garc) throws IOException {
		this.writeFile(subpath, garc.getBytes());
	}

	protected byte[] readFile(String location) throws IOException {
		if (gameUpdate != null && gameUpdate.hasFile(location)) {
			return gameUpdate.getFile(location);
		}
		return baseRom.getFile(location);
	}

	protected void writeFile(String location, byte[] data) throws IOException {
		writeFile(location, data, 0, data.length);
	}

	protected void readByteIntoFlags(byte[] data, boolean[] flags, int offsetIntoFlags, int offsetIntoData) {
		int thisByte = data[offsetIntoData] & 0xFF;
		for (int i = 0; i < 8 && (i + offsetIntoFlags) < flags.length; i++) {
			flags[offsetIntoFlags + i] = ((thisByte >> i) & 0x01) == 0x01;
		}
	}

	protected byte getByteFromFlags(boolean[] flags, int offsetIntoFlags) {
		int thisByte = 0;
		for (int i = 0; i < 8 && (i + offsetIntoFlags) < flags.length; i++) {
			thisByte |= (flags[offsetIntoFlags + i] ? 1 : 0) << i;
		}
		return (byte) thisByte;
	}

	protected int readWord(byte[] data, int offset) {
		return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
	}

	protected void writeWord(byte[] data, int offset, int value) {
		data[offset] = (byte) (value & 0xFF);
		data[offset + 1] = (byte) ((value >> 8) & 0xFF);
	}

	protected int readLong(byte[] data, int offset) {
		return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16)
				| ((data[offset + 3] & 0xFF) << 24);
	}

	protected void writeLong(byte[] data, int offset, int value) {
		data[offset] = (byte) (value & 0xFF);
		data[offset + 1] = (byte) ((value >> 8) & 0xFF);
		data[offset + 2] = (byte) ((value >> 16) & 0xFF);
		data[offset + 3] = (byte) ((value >> 24) & 0xFF);
	}

	protected void writeFile(String location, byte[] data, int offset, int length) throws IOException {
		if (offset != 0 || length != data.length) {
			byte[] newData = new byte[length];
			System.arraycopy(data, offset, newData, 0, length);
			data = newData;
		}
		baseRom.writeFile(location, data);
		if (gameUpdate != null && gameUpdate.hasFile(location)) {
			gameUpdate.writeFile(location, data);
		}
	}

	public String getTitleIdFromLoadedROM() {
		return baseRom.getTitleId();
	}

	protected static String getProductCodeFromFile(String filename) {
		try {
			long ncchStartingOffset = NCCH.getCXIOffsetInFile(filename);
			if (ncchStartingOffset == -1) {
				return null;
			}
			FileInputStream fis = new FileInputStream(filename);
			fis.skip(ncchStartingOffset + 0x150);
			byte[] productCode = FileFunctions.readFullyIntoBuffer(fis, 0x10);
			fis.close();
			return new String(productCode, StandardCharsets.UTF_8).trim();
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	public static String getTitleIdFromFile(String filename) {
		try {
			long ncchStartingOffset = NCCH.getCXIOffsetInFile(filename);
			if (ncchStartingOffset == -1) {
				return null;
			}
			FileInputStream fis = new FileInputStream(filename);
			fis.skip(ncchStartingOffset + 0x118);
			byte[] programId = FileFunctions.readFullyIntoBuffer(fis, 0x8);
			fis.close();
			reverseArray(programId);
			return bytesToHex(programId);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private static void reverseArray(byte[] bytes) {
		for (int i = 0; i < bytes.length / 2; i++) {
			byte temp = bytes[i];
			bytes[i] = bytes[bytes.length - i - 1];
			bytes[bytes.length - i - 1] = temp;
		}
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int unsignedByte = bytes[i] & 0xFF;
			hexChars[i * 2] = HEX_ARRAY[unsignedByte >>> 4];
			hexChars[i * 2 + 1] = HEX_ARRAY[unsignedByte & 0x0F];
		}
		return new String(hexChars);
	}

	protected int typeTMPaletteNumber(Type t, boolean isGen7) {
		if (t == null) {
			return 322; // CURSE
		}
		switch (t) {
		case DARK:
			return 309;
		case DRAGON:
			return 310;
		case PSYCHIC:
			return 311;
		case NORMAL:
			return 312;
		case POISON:
			return 313;
		case ICE:
			return 314;
		case FIGHTING:
			return 315;
		case FIRE:
			return 316;
		case WATER:
			return 317;
		case FLYING:
			return 323;
		case GRASS:
			return 318;
		case ROCK:
			return 319;
		case ELECTRIC:
			return 320;
		case GROUND:
			return 321;
		case GHOST:
		default:
			return 322; // for CURSE
		case STEEL:
			return 324;
		case BUG:
			return 325;
		case FAIRY:
			return isGen7 ? 555 : 546;
		}
	}

	@Override
	public Set<Item> getMegaStones() {
		return itemIdsToSet(Gen6Constants.megaStones);
	}

	@Override
	public void savePokemonPalettes() {
		// not applicable
	}

	@Override
	public List<BufferedImage> getAllPokemonImages() {
		List<BufferedImage> bims = new ArrayList<>();

		String GARCPath = getGARCPath("PokemonGraphics");
		GARCArchive pokeGraphicsGARC;
		try {
			pokeGraphicsGARC = readGARC(GARCPath, false);
		} catch (IOException e) {
			throw new RomIOException(e);
		}

		for (int i = 1; i < pokeGraphicsGARC.files.size(); i++) {
			BufferedImage icon = getPokemonIcon(i, pokeGraphicsGARC, false, true);
			bims.add(icon);
		}
		return bims;
	}

	public int getIconGARCSize() {
		try {
			String GARCPath = getGARCPath("PokemonGraphics");
			GARCArchive pokeGraphicsGARC = readGARC(GARCPath, false);
			return pokeGraphicsGARC.files.size();
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	public BufferedImage getPokemonIcon(int iconIndex) {
		try {
			String GARCPath = getGARCPath("PokemonGraphics");
			GARCArchive pokeGraphicsGARC = readGARC(GARCPath, false);

			return getPokemonIcon(iconIndex, pokeGraphicsGARC, true, false);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	public abstract BufferedImage getPokemonIcon(int pkIndex, GARCArchive pokeGraphicsGARC,
												 boolean transparentBackground, boolean includePalette);


	public PokemonImageGetter createPokemonImageGetter(Species pk) {
		// No PokemonImageGetter for the 3DS games for now, in part because they can only get icons,
		// and in part because there's no code to get the icon(s) of a specific Pokemon.
		throw new UnsupportedOperationException();
	}

	// because RomEntry is an inner class it can't be accessed here 
	// (or can it in ZX?), so an abstract method is needed.
	// a refactoring might be better, but is outside of the scope for the changes
	// I'm making now
	// - voliol 2022-08-07
	public abstract String getGARCPath(String fileName);

}
