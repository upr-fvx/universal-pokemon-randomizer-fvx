package com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.FileFunctions;
import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.exceptions.CannotWriteToLocationException;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.Type;
import com.dabomstew.pkromio.gbspace.FreedSpace;
import com.dabomstew.pkromio.graphics.palettes.Palette;
import com.dabomstew.pkromio.newnds.NARCArchive;
import com.dabomstew.pkromio.newnds.NDSRom;
import com.dabomstew.pkromio.romhandlers.romentries.AbstractDSRomEntry;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An abstract base class for DS {@link RomHandler}s, which standardises common DS functions.
 */
public abstract class AbstractDSRomHandler extends AbstractRomHandler {

    // ITCM is mirrored from 0x0000000 to 0x2000000, but it seems in practice only some of these mirrors are allowed.
    // Below is an arbitrary mirror which should work.
    private static final int ITCM_RAM_ADDRESS = 0x1000000;
    private static final int ITCM_END = 0x1FFFFFF;
    private static final int ITCM_LENGTH = 0x8000;

    private static final byte[] PALETTE_PREFIX_BYTES = { (byte) 0x52, (byte) 0x4C, (byte) 0x43, (byte) 0x4E,
            (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x01, (byte) 0x48, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x10, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x54, (byte) 0x54, (byte) 0x4C, (byte) 0x50,
            (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x0A, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    private NDSRom baseRom;
    private String loadedFN;

    protected byte[] arm9;
    private boolean arm9Extended = false;
    private int tcmCopyingPointersOffset = -1;
    protected final FreedSpace arm9FreedSpace = new FreedSpace();

    protected abstract boolean detectNDSRom(String ndsCode, byte version);

    @Override
    public boolean loadRom(String filename) {
        if (!this.detectNDSRom(getROMCodeFromFile(filename), getVersionFromFile(filename))) {
            return false;
        }
        // Load inner rom
        try {
            baseRom = new NDSRom(filename);
            arm9 = readARM9();
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        loadedFN = filename;
        loadedROM(baseRom.getCode(), baseRom.getVersion());
        return true;
    }

    @Override
    public String loadedFilename() {
        return loadedFN;
    }

    protected abstract void loadedROM(String romCode, byte version);

    @Override
    protected void prepareSaveRom() {
        super.prepareSaveRom();
        try {
            writeARM9(arm9);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public boolean saveRomFile(String filename, long seed) {
        try {
            baseRom.saveTo(filename);
        } catch (IOException e) {
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
        // do nothing. DS games do have the concept of a filesystem, but it's way more
        // convenient for users to use ROM files instead.
        return true;
    }

    @Override
    public boolean hasGameUpdateLoaded() {
        return false;
    }

    @Override
    public boolean loadGameUpdate(String filename) {
        // do nothing, as DS games don't have external game updates
        return true;
    }

    @Override
    public void removeGameUpdate() {
        // do nothing, as DS games don't have external game updates
    }

    @Override
    public String getGameUpdateVersion() {
        // do nothing, as DS games don't have external game updates
        return null;
    }

    @Override
    public void printRomDiagnostics(PrintStream logStream) {
        baseRom.printRomDiagnostics(logStream);
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

    public NARCArchive readNARC(String subpath) throws IOException {
        return new NARCArchive(readFile(subpath));
    }

    public void writeNARC(String subpath, NARCArchive narc) throws IOException {
        this.writeFile(subpath, narc.getBytes());
    }

    protected static String getROMCodeFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            fis.skip(0x0C);
            byte[] sig = FileFunctions.readFullyIntoBuffer(fis, 4);
            fis.close();
            return new String(sig, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    protected static byte getVersionFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            fis.skip(0x1E);
            byte[] version = FileFunctions.readFullyIntoBuffer(fis, 1);
            fis.close();
            return version[0];
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    protected int readByte(byte[] data, int offset) { return data[offset] & 0xFF; }

    protected final void writeBytes(byte[] data, int offset, byte[] values) {
        System.arraycopy(values, 0, data, offset, values.length);
    }

    public int readWord(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    protected int readLong(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    protected int readRelativePointer(byte[] data, int offset) {
        return readLong(data, offset) + offset + 4;
    }

    public void writeWord(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    protected void writeLong(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    protected void writeRelativePointer(byte[] data, int offset, int pointer) {
        int relPointer = pointer - (offset + 4);
        writeLong(data, offset, relPointer);
    }

    protected byte[] readFile(String location) throws IOException {
        return baseRom.getFile(location);
    }

    protected void writeFile(String location, byte[] data) throws IOException {
        writeFile(location, data, 0, data.length);
    }

    protected void writeFile(String location, byte[] data, int offset, int length) throws IOException {
        if (offset != 0 || length != data.length) {
            byte[] newData = new byte[length];
            System.arraycopy(data, offset, newData, 0, length);
            data = newData;
        }
        baseRom.writeFile(location, data);
    }

    private byte[] readARM9() throws IOException {
        return baseRom.getARM9();
    }

    private void writeARM9(byte[] data) throws IOException {
        baseRom.writeARM9(data);
    }

    protected byte[] readOverlay(int number) throws IOException {
        return baseRom.getOverlay(number);
    }

    protected void writeOverlay(int number, byte[] data) throws IOException {
        baseRom.writeOverlay(number, data);
    }

    protected int overlayAddress(int number) {
        return baseRom.getOverlayAddress(number);
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

    protected int typeTMPaletteNumber(Type t) {
        if (t == null) {
            return 411; // CURSE
        }
        switch (t) {
        case FIGHTING:
            return 398;
        case DRAGON:
            return 399;
        case WATER:
            return 400;
        case PSYCHIC:
            return 401;
        case NORMAL:
            return 402;
        case POISON:
            return 403;
        case ICE:
            return 404;
        case GRASS:
            return 405;
        case FIRE:
            return 406;
        case DARK:
            return 407;
        case STEEL:
            return 408;
        case ELECTRIC:
            return 409;
        case GROUND:
            return 410;
        case GHOST:
        default:
            return 411; // for CURSE
        case ROCK:
            return 412;
        case FLYING:
            return 413;
        case BUG:
            return 610;
        }
    }

    protected int find(byte[] data, String hexString) {
        // TODO: merge all the "find" methods, move to RomFunctions maybe??
        byte[] searchFor = RomFunctions.hexToBytes(hexString);
        List<Integer> found = RomFunctions.search(data, searchFor);
        if (found.isEmpty()) {
            return -1; // not found
        } else if (found.size() > 1) {
            return -2; // not unique
        } else {
            return found.get(0);
        }
    }

    /**
     * Extends the ARM9 file, allowing for extra data/code storage. The ARM9 can only be extended once,
     * subsequent times it throws an {@link IllegalStateException}.
     * <br><br>
     * This method works through extending the section of ARM9 which is copied to ITCM
     * (Instruction Tightly Coupled Memory) and comes with a few caveats.<br>
     * There is a limit to how much data may be copied to ITCM (<32 KiB). If {@code extendBy} exceeds this limit, this
     * method throws an {@link IllegalArgumentException}.<br>
     * Also, pointers to the extended parts of ARM9 must rather point to ITCM, since that is where the data ultimately
     * ends up in RAM.
     *
     * @param extendBy The number of bytes to extend by.
     * @param repointExtendBy The number of bytes, of the extended ones, which will be available for repointing data
     *                        via {@link #arm9FreedSpace}.
     * @param prefix Bytes that come right before the TCM pointer section. Used to find said section.
     */
    protected void extendARM9(int extendBy, int repointExtendBy, byte[] prefix) {
        /*
        Simply extending the ARM9 at the end doesn't work. Towards the end of the ARM9, the following sections exist:
        1. A section that is copied to ITCM (Instruction Tightly Coupled Memory)
        2. A section that is copied to DTCM (Data Tightly Coupled Memory)
        3. Pointers specifying to where these sections should be copied as well as their sizes

        All of these sections are later overwritten(!) and the area is used more or less like a regular RAM area.
        This means that if any new code is put after these sections, it will also be overwritten.
        Changing which area is overwritten is not viable. There are very many pointers to this area that would need to
        be re-indexed.

        Our solution is to extend the section that is to be copied to ITCM, so that any new code gets copied to
        ITCM and can be executed from there. This means we have to shift all the data that is after this in order to
        make space. Additionally, elsewhere in the ARM9, pointers are stored specifying from where the ITCM
        section should be copied, as well as some other data. They are supposedly part of some sort of NDS library
        functions and should work the same across games; look for "[SDK+NINTENDO:" in the ARM9 and these pointers should
        be slightly before that. They are as follows (each pointer = 4 bytes):
        1. Pointer specifying from where the destination pointers/sizes should be read (see point 3 above)
        2. Pointer specifying the end address of the ARM9.
        3. Pointer specifying from where data copying should start (since ITCM is first, this corresponds to the start
           of the section that should be copied to ITCM).
        4. Pointer specifying where data should start being overwritten. (should be identical to #3)
        5. Pointer specifying where data should stop being overwritten (should correspond to start of ovl table).
        6. ???

        Out of these, we want to change #1 (it will be moved because we have to shift the end of the ARM9 to make space
        for enlarging the "copy to ITCM" area) and #2 (since the ARM9 will be made larger). We also want to change the
        specified size for the ITCM area since we're enlarging it.
         */

        if (repointExtendBy > extendBy) {
            throw new IllegalArgumentException("repointExtendBy can't be larger than extendBy");
        }
        if (arm9Extended) {
            throw new IllegalStateException("Don't try to extend the ARM9 more than once.");
        }

        int arm9Offset = getARM9Offset();

        tcmCopyingPointersOffset = RomFunctions.searchForFirst(arm9, 0, prefix);
        tcmCopyingPointersOffset += prefix.length; // because it was a prefix

        int oldDestPointersOffset = FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset) - arm9Offset;
        int itcmSrcOffset =
                FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset + 8) - arm9Offset;
        int itcmSizeOffset = oldDestPointersOffset + 4;
        int oldITCMSize = FileFunctions.readFullInt(arm9, itcmSizeOffset);
        if (oldITCMSize + extendBy > ITCM_LENGTH) {
            throw new IllegalArgumentException("Can't extend the section which is copied to ITCM past 32 KiB.");
        }

        int oldDTCMOffset = itcmSrcOffset + oldITCMSize;

        byte[] newARM9 = Arrays.copyOf(arm9, arm9.length + extendBy);

        // Change:
        // 1. Pointer to destination pointers/sizes
        // 2. ARM9 size
        // 3. Size of the area copied to ITCM
        FileFunctions.writeFullInt(newARM9, tcmCopyingPointersOffset,
                oldDestPointersOffset + extendBy + arm9Offset);
        FileFunctions.writeFullInt(newARM9, tcmCopyingPointersOffset + 4,
                newARM9.length + arm9Offset);
        FileFunctions.writeFullInt(newARM9, itcmSizeOffset, oldITCMSize + extendBy);

        // Finally, shift everything
        System.arraycopy(newARM9, oldDTCMOffset, newARM9, oldDTCMOffset + extendBy,
                arm9.length - oldDTCMOffset);

        // And free some amount of the extended section
        if (repointExtendBy > 0) {
            arm9FreedSpace.free(itcmSrcOffset + oldITCMSize + extendBy - repointExtendBy, repointExtendBy);
        }

        arm9Extended = true;

        arm9 = newARM9;
    }

    private boolean isInCopyToITCMSection(int offset) {
        if (tcmCopyingPointersOffset == -1) {
            throw new IllegalStateException("tcmCopyingPointersOffset has not been initialized");
        }
        int itcmSizeOffset = FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset) - getARM9Offset() + 4;
        int itcmSize = FileFunctions.readFullInt(arm9, itcmSizeOffset);
        int itcmSrcOffset = getITCMSrcOffset();
        return offset >= itcmSrcOffset && offset < itcmSrcOffset + itcmSize;
    }

    protected abstract int getARM9Offset();

    /**
     * Returns whether a pointer at {@code offset} in {@code data} points
     * to the RAM locations of either ARM9 or ITCM - i.e.,
     * whether the corresponding data belongs in the ARM9.bin file.<br>
     * To be used in conjunction with {@link #readARM9Pointer(byte[], int)}.
     */
    protected boolean isARM9Pointer(byte[] data, int offset) {
        int pointer = readLong(data, offset);
        return pointer <= ITCM_END || (pointer >= getARM9Offset() && pointer < getARM9Offset() + arm9.length);
    }

    /**
     * Reads a pointer at {@code offset} in {@code data}.<br>
     * If the pointer points to the RAM locations of either ARM9 or ITCM,
     * returns the corresponding offset in the ARM9.bin file.<br>
     * If the pointer does not point to the RAM locations of ARM9 or ITCM,
     * throws a {@link RomIOException}.
     */
    protected int readARM9Pointer(byte[] data, int offset) {
        int pointer = readLong(data, offset);
        if (pointer <= ITCM_END) {
            return pointer % ITCM_LENGTH + getITCMSrcOffset();
        } else if (pointer >= getARM9Offset() && pointer < getARM9Offset() + arm9.length) {
            return pointer - getARM9Offset();
        } else {
            throw new RuntimeException(String.format(
                    "Invalid pointer! 0x%s is not a RAM location for either ARM9 (0x%s-0x%s) or ITCM (0x%s-0x%s)",
                    Integer.toHexString(pointer),
                    Integer.toHexString(getARM9Offset()), Integer.toHexString(getARM9Offset() + arm9.length - 1),
                    Integer.toHexString(0), Integer.toHexString(ITCM_END)));
        }
    }

    private int getITCMSrcOffset() {
        return FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset + 8) - getARM9Offset();
    }

    /**
     * Writes a pointer to {@code offset} in {@code data}, pointing at {@code pointer}.<br>
     * {@code pointer} is an offset in the ARM9.bin file, corresponding to a location of either ARM9 or ITCM in RAM.
     * The actual pointer written, corresponds to this RAM location.<br>
     * If {@code pointer} does not correspond to an ARM9/ITCM RAM location, throws a {@link RomIOException}.
     */
    protected void writeARM9Pointer(byte[] data, int offset, int pointer) {
        if (isInCopyToITCMSection(pointer)) {
            writeLong(data, offset, pointer - getITCMSrcOffset() + ITCM_RAM_ADDRESS);
        } else if (pointer < arm9.length) {
            // This is not a perfect check; a pointer to e.g., the DTCM section of ARM9.bin would give a false positive.
            // At least it forbids pointers totally out of bounds.
            writeLong(data, offset, pointer + getARM9Offset());
        } else {
            throw new RomIOException("Invalid pointer! 0x" + Integer.toHexString(pointer) + " is not an offset in the " +
                    "ARM9.bin file, which corresponds to an ARM9/ITCM RAM location.");
        }
    }

    /**
     * Reads a pointer located in an overlay.
     * Assumes the pointer points to somewhere else in the overlay.
     */
    protected int readOverlayPointer(byte[] overlay, int overlayNum, int offset) {
        return readLong(overlay, offset) - overlayAddress(overlayNum);
    }

    /**
     * Writes a pointer to "offset" located in an overlay, pointing at "pointer".
     * Assumes the pointer will point to somewhere else in the overlay.
     */
    protected void writeOverlayPointer(byte[] arm9, int overlayNum, int offset, int pointer) {
        writeLong(arm9, offset, pointer + overlayAddress(overlayNum));
    }
    
	private byte[] concatenate(byte[] a, byte[] b) {
	    byte[] sum = new byte[a.length + b.length];
	    System.arraycopy(a, 0, sum, 0, a.length);
	    System.arraycopy(b, 0, sum, a.length, b.length);
	    return sum;
	}

    @Override
    public boolean hasTypeEffectivenessSupport() {
        return true;
    }

    @Override
    public boolean hasPokemonPaletteSupport() {
        return true;
    }

    @Override
    public boolean pokemonPaletteSupportIsPartial() {
        return true;
    }

    // I dare not rewrite the load ROM structure, so for now loadPokemonPalettes()
	// is separate methods called in loadROM()/loadedRom() methods. Even though
	// one call in AbstractRomHandler should suffice.
    // TODO: move loadPokemonPalettes() up
	protected abstract void loadPokemonPalettes();

    protected final Palette readPalette(NARCArchive NARC, int index) {
        byte[] withPrefixBytes = NARC.files.get(index);
        byte[] paletteBytes = Arrays.copyOfRange(withPrefixBytes, PALETTE_PREFIX_BYTES.length, withPrefixBytes.length);
        return new Palette(paletteBytes);
    }
    
    protected final void writePalette(NARCArchive NARC, int index, Palette palette) {
        byte[] paletteBytes = palette.toBytes();
        paletteBytes = concatenate(PALETTE_PREFIX_BYTES, paletteBytes);
        NARC.files.set(index, paletteBytes);
    }

    @Override
    public List<BufferedImage> getAllPokemonImages() {
        List<BufferedImage> bims = new ArrayList<>();

		String NARCPath = getRomEntry().getFile("PokemonGraphics");
		NARCArchive pokeGraphicsNARC;
		try {
			pokeGraphicsNARC = readNARC(NARCPath);
		} catch (IOException e) {
			throw new RomIOException(e);
		}

        for (int i = 1; i < getSpecies().size(); i++) {
            Species pk = getSpecies().get(i);
            DSPokemonImageGetter pig = createPokemonImageGetter(pk).setPokeGraphicsNARC(pokeGraphicsNARC);
            bims.add(pig.getFull());
        }
        return bims;
    }

    @Override
    public boolean hasPokemonImageGetter() {
        return true;
    }

    @Override
    public abstract DSPokemonImageGetter createPokemonImageGetter(Species pk);

    public abstract class DSPokemonImageGetter extends PokemonImageGetter {
        public static final int MALE = 0;
        public static final int FEMALE = 1;

        protected NARCArchive pokeGraphicsNARC;
        protected int gender = FEMALE;

        public DSPokemonImageGetter(Species pk) {
            super(pk);
        }

        public DSPokemonImageGetter setPokeGraphicsNARC(NARCArchive pokeGraphicsNARC) {
            this.pokeGraphicsNARC = pokeGraphicsNARC;
            return this;
        }

        public DSPokemonImageGetter setGender(int gender) {
            if (gender != FEMALE && gender != MALE) {
                throw new IllegalArgumentException("invalid gender, must be 0(MALE) or 1(FEMALE)");
            }
            this.gender = gender;
            return this;
        }

        protected void beforeGet() {
            if (pokeGraphicsNARC == null) {
                try {
                    String NARCpath = getRomEntry().getFile("PokemonGraphics");
                    pokeGraphicsNARC = readNARC(NARCpath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public abstract boolean hasGenderedImages();

        @Override
        public BufferedImage getFull() {
            // TODO: Gen 5 technically allows for alt forms to have graphical gender differences.
            //  Combine the two methods below.
            if (getGraphicalFormeAmount() > 1) {
                return withFormesGetFull();
            } else {
                return withGendersGetFull();
            }
        }

        private BufferedImage withFormesGetFull() {
            setIncludePalette(true);

            BufferedImage[] normal = new BufferedImage[getGraphicalFormeAmount()*2];
            BufferedImage[] shiny = new BufferedImage[getGraphicalFormeAmount()*2];
            for (int i = 0; i < getGraphicalFormeAmount(); i++) {
                setGraphicalForme(i);

                normal[i*2] = get();
                normal[i*2 + 1] = setBack(true).get();
                shiny[i*2 + 1] = setShiny(true).get();
                shiny[i*2] = setBack(false).get();
                setShiny(false);
            }
            return GFXFunctions.stitchToGrid(new BufferedImage[][] { normal, shiny });
        }

        public BufferedImage withGendersGetFull() {
            setGender(DSPokemonImageGetter.MALE)
                    .setIncludePalette(true);

            BufferedImage frontNormalM = get();
            BufferedImage backNormalM = setBack(true).get();
            BufferedImage backShinyM = setShiny(true).get();
            BufferedImage frontShinyM = setBack(false).get();

            BufferedImage combined;
            if (hasGenderedImages()) {
                BufferedImage frontShinyF = setGender(DSPokemonImageGetter.FEMALE).get();
                BufferedImage backShinyF = setBack(true).get();
                BufferedImage backNormalF = setShiny(false).get();
                BufferedImage frontNormalF = setBack(false).get();
                combined = GFXFunctions
                        .stitchToGrid(new BufferedImage[][]{{frontNormalM, backNormalM, frontNormalF, backNormalF},
                                {frontShinyM, backShinyM, frontShinyF, backShinyF}});
            } else {
                combined = GFXFunctions
                        .stitchToGrid(new BufferedImage[][]{{frontNormalM, backNormalM}, {frontShinyM, backShinyM}});
            }
            return combined;
        }
    }

    @Override
    protected abstract AbstractDSRomEntry getRomEntry();

}
