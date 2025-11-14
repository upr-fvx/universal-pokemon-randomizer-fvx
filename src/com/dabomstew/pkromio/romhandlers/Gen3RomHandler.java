package com.dabomstew.pkromio.romhandlers;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
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
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.*;
import com.dabomstew.pkromio.constants.enctaggers.Gen3EncounterAreaTagger;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.gbspace.FreedSpace;
import com.dabomstew.pkromio.graphics.images.GBAImage;
import com.dabomstew.pkromio.graphics.packs.*;
import com.dabomstew.pkromio.graphics.palettes.Palette;
import com.dabomstew.pkromio.romhandlers.romentries.Gen3EventTextEntry;
import com.dabomstew.pkromio.romhandlers.romentries.Gen3RomEntry;
import com.dabomstew.pkromio.romhandlers.romentries.RomEntry;
import compressors.DSCmp;
import compressors.DSDecmp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@link RomHandler} for Ruby, Sapphire, Emerald, FireRed, LeafGreen.
 */
public class Gen3RomHandler extends AbstractGBRomHandler {

    public static class Factory extends RomHandler.Factory {

        @Override
        public Gen3RomHandler create() {
            return new Gen3RomHandler();
        }

        public boolean isLoadable(String filename) {
            long fileLength = new File(filename).length();
            if (fileLength > 32 * 1024 * 1024) {
                return false;
            }
            byte[] loaded = loadFilePartial(filename, 0x100000);
            // nope
            return loaded.length != 0 && detectRomInner(loaded, (int) fileLength);
        }
    }

    private static List<Gen3RomEntry> roms;

    static {
        loadROMInfo();
    }

    private static void loadROMInfo() {
        try {
            roms = Gen3RomEntry.READER.readEntriesFromFile("gen3_offsets.ini");
        } catch (IOException e) {
            throw new RuntimeException("Could not read Rom Entries.", e);
        }
    }

    private void loadTextTable(String filename) {
        try {
            Scanner sc = new Scanner(FileFunctions.openConfig(filename + ".tbl"), "UTF-8");
            while (sc.hasNextLine()) {
                String q = sc.nextLine();
                if (!q.trim().isEmpty()) {
                    String[] r = q.split("=", 2);
                    if (r[1].endsWith("\r\n")) {
                        r[1] = r[1].substring(0, r[1].length() - 2);
                    }
                    tb[Integer.parseInt(r[0], 16)] = r[1];
                    d.put(r[1], (byte) Integer.parseInt(r[0], 16));
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found!");
        }

    }

    // This ROM's data
    private Gen3RomEntry romEntry;
    private Species[] pokes, pokesInternal;
    private List<Species> speciesList;
    private int numRealPokemon;
    private List<Trainer> trainers;
    private List<Item> items;
    private Move[] moves;
    private boolean jamboMovesetHack;
    private boolean havePatchedObedience;
    private String[] tb;
    public Map<String, Byte> d;
    private String[] abilityNames;
    private boolean mapLoadingDone;
    private List<Integer> itemOffs;
    private String[][] mapNames;
    private boolean isRomHack;
    private int[] internalToPokedex, pokedexToInternal;
    private int pokedexCount;
    private String[] pokeNames;
    private int pickupItemsTableOffset;

    // Misc.
    private final FreedSpace freedSpace = new FreedSpace();

    @Override
    public boolean detectRom(byte[] rom) {
        return detectRomInner(rom, rom.length);
    }

    private static boolean detectRomInner(byte[] rom, int romSize) {
        if (romSize != Gen3Constants.size8M && romSize != Gen3Constants.size16M && romSize != Gen3Constants.size32M) {
            return false; // size check
        }
        // Special case for Emerald unofficial translation
        if (romName(rom, Gen3Constants.unofficialEmeraldROMName)) {
            // give it a rom code so it can be detected
            rom[Gen3Constants.romCodeOffset] = 'B';
            rom[Gen3Constants.romCodeOffset + 1] = 'P';
            rom[Gen3Constants.romCodeOffset + 2] = 'E';
            rom[Gen3Constants.romCodeOffset + 3] = 'T';
            rom[Gen3Constants.headerChecksumOffset] = 0x66;
        }
        for (Gen3RomEntry re : roms) {
            if (romCode(rom, re.getRomCode()) && (rom[Gen3Constants.romVersionOffset] & 0xFF) == re.getVersion()) {
                return true; // match
            }
        }
        return false; // GBA rom we don't support yet
    }

    @Override
    public void midLoadingSetUp() {
        super.midLoadingSetUp();
        isRomHack = false;
        jamboMovesetHack = false;
        if (romEntry.getRomCode().equals("BPRE") && romEntry.getVersion() == 0) {
            basicBPRE10HackSupport();
        }

        mapLoadingDone = false;
        determineMapBankSizes();
        preprocessMaps();
        mapLoadingDone = true;

        freeAllUnusedSpace();
    }

    @Override
    protected void loadGameData() {
        super.loadGameData();
        loadAbilityNames();
    }

    @Override
    protected void initRomEntry() {
        for (Gen3RomEntry re : roms) {
            if (romCode(rom, re.getRomCode()) && (rom[0xBC] & 0xFF) == re.getVersion()) {
                romEntry = new Gen3RomEntry(re); // clone so we can modify
                break;
            }
        }
        addPointerBlocksToRomEntry();
        addMoveTutorInfoToRomEntry();
        addTrainerGraphicsInfoToRomEntry();
    }

    private void addPointerBlocksToRomEntry() {
        if (romEntry.getIntValue("HasPointerBlock1") == 1) {
            addPointerBlock1ToRomEntry();
        } else {
            int baseNomOffset = find(rom, Gen3Constants.rsPokemonNamesPointerSuffix);
            romEntry.putIntValue("PokemonNames", readPointer(baseNomOffset - 4));
        }
        if (romEntry.getIntValue("HasPointerBlock2") == 1) {
            addPointerBlock2ToRomEntry();
        }
    }

    private void addPointerBlock1ToRomEntry() {
        romEntry.putIntValue("PokemonFrontImages", readPointer(Gen3Constants.pokemonFrontImagesPointer));
        romEntry.putIntValue("PokemonBackImages", readPointer(Gen3Constants.pokemonBackImagesPointer));
        romEntry.putIntValue("PokemonNormalPalettes", readPointer(Gen3Constants.pokemonNormalPalettesPointer));
        romEntry.putIntValue("PokemonShinyPalettes", readPointer(Gen3Constants.pokemonShinyPalettesPointer));
        romEntry.putIntValue("PokemonIconSprites", readPointer(Gen3Constants.pokemonIconSpritesPointer));
        romEntry.putIntValue("PokemonIconPalettes", readPointer(Gen3Constants.pokemonIconPalettesPointer));
        romEntry.putIntValue("PokemonNames", readPointer(Gen3Constants.pokemonNamesPointer));
        romEntry.putIntValue("MoveNames", readPointer(Gen3Constants.moveNamesPointer));
        romEntry.putIntValue("DecorationNames", readPointer(Gen3Constants.decorationNamesPointer));
    }

    private void addPointerBlock2ToRomEntry() {
        romEntry.putIntValue("PokemonStats", readPointer(Gen3Constants.pokemonStatsPointer));
        romEntry.putIntValue("AbilityNames", readPointer(Gen3Constants.abilityNamesPointer));
        romEntry.putIntValue("AbilityDescriptions", readPointer(Gen3Constants.abilityDescriptionsPointer));
        romEntry.putIntValue("ItemData", readPointer(Gen3Constants.itemDataPointer));
        romEntry.putIntValue("MoveData", readPointer(Gen3Constants.moveDataPointer));
        romEntry.putIntValue("BallSpritesPointer", readPointer(Gen3Constants.ballSpritesPointer));
        romEntry.putIntValue("BallPalettesPointer", readPointer(Gen3Constants.ballPalettesPointer));
    }

    private void addMoveTutorInfoToRomEntry() {
        if (romEntry.getRomType() == Gen3Constants.RomType_Em || romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            romEntry.putIntValue("MoveTutorCompatibility",
                    romEntry.getIntValue("MoveTutorData") + romEntry.getIntValue("MoveTutorMoves") * 2);
        }
    }

    private void addTrainerGraphicsInfoToRomEntry() {
        addTrainerFrontPalettesToRomEntry();
        addTrainerBackPalettesToRomEntry();
        addMapIconInfoToRomEntry();
    }

    private void addTrainerFrontPalettesToRomEntry() {
        int offset;
        switch (romEntry.getRomType()) {
            case Gen3Constants.RomType_Ruby:
            case Gen3Constants.RomType_Sapp:
                offset = Gen3Constants.rsTrainerFrontPalettesOffset;
                break;
            case Gen3Constants.RomType_Em:
                offset = Gen3Constants.emTrainerFrontPalettesOffset;
                break;
            case Gen3Constants.RomType_FRLG:
                offset = Gen3Constants.frlgTrainerFrontPalettesOffset;
                break;
            default:
                throw new RuntimeException("Invalid romType");
        }
        addRelativeOffsetToRomEntry("TrainerFrontPalettes", "TrainerFrontImages", offset);
    }

    private void addTrainerBackPalettesToRomEntry() {
        int offset;
        switch (romEntry.getRomType()) {
            case Gen3Constants.RomType_Ruby:
            case Gen3Constants.RomType_Sapp:
                offset = Gen3Constants.rsTrainerBackPalettesOffset;
                break;
            case Gen3Constants.RomType_Em:
                offset = Gen3Constants.emTrainerBackPalettesOffset;
                break;
            case Gen3Constants.RomType_FRLG:
                offset = Gen3Constants.frlgTrainerBackPalettesOffset;
                break;
            default:
                throw new RuntimeException("Invalid romType");
        }
        addRelativeOffsetToRomEntry("TrainerBackPalettes", "TrainerBackImages", offset);

    }

    private void addMapIconInfoToRomEntry() {
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            addRelativeOffsetToRomEntry("RedMapIconPalettePointer", "RedMapIconImagePointer",
                    Gen3Constants.redMapIconPalettePointerOffset);
            addRelativeOffsetToRomEntry("LeafMapIconImagePointer", "RedMapIconImagePointer",
                    Gen3Constants.leafMapIconImagePointerOffset);
            addRelativeOffsetToRomEntry("LeafMapIconPalettePointer", "RedMapIconImagePointer",
                    Gen3Constants.leafMapIconPalettePointerOffset);
        } else {
            addRelativeOffsetToRomEntry("BrendanMapIconPalette", "BrendanMapIconImage",
                    Gen3Constants.brendanMapIconPaletteOffset);
            addRelativeOffsetToRomEntry("MayMapIconImage", "BrendanMapIconImage",
                    Gen3Constants.mayMapIconImageOffset);
            addRelativeOffsetToRomEntry("MayMapIconPalette", "BrendanMapIconImage",
                    Gen3Constants.mayMapIconPaletteOffset);
        }
    }

    @Override
    protected void initTextTables() {
        tb = new String[256];
        d = new HashMap<>();
        loadTextTable(romEntry.getTableFile());
    }

    private void basicBPRE10HackSupport() {
        if (basicBPRE10HackDetection()) {
            this.isRomHack = true;
            // NUMBER OF POKEMON DETECTION

            // this is the most annoying bit
            // we'll try to get it from the pokemon names,
            // and sanity check it using other things
            // this of course means we can't support
            // any hack with extended length names

            int iPokemonCount = 0;
            int namesOffset = romEntry.getIntValue("PokemonNames");
            int nameLen = romEntry.getIntValue("PokemonNameLength");
            while (true) {
                int nameOffset = namesOffset + (iPokemonCount + 1) * nameLen;
                int nameStrLen = lengthOfStringAt(nameOffset);
                if (nameStrLen > 0 && nameStrLen <= nameLen && rom[nameOffset] != 0) {
                    iPokemonCount++;
                } else {
                    break;
                }
            }

            // Is there an unused egg slot at the end?
            String lastName = readVariableLengthString(namesOffset + iPokemonCount * nameLen);
            if (lastName.equals("?") || lastName.equals("-")) {
                iPokemonCount--;
            }

            // Jambo's Moves Learnt table hack?
            // need to check this before using moveset pointers
            int movesetsTable;
            if (readLong(0x3EB20) == 0x47084918) {
                // Hack applied, adjust accordingly
                int firstRoutinePtr = readPointer(0x3EB84);
                movesetsTable = readPointer(firstRoutinePtr + 75);
                jamboMovesetHack = true;
            } else {
                movesetsTable = readPointer(0x3EA7C);
                jamboMovesetHack = false;
            }

            // secondary check: moveset pointers
            // if a slot has an invalid moveset pointer, it's not a real slot
            // Before that, grab the moveset table from a known pointer to it.
            romEntry.putIntValue("PokemonMovesets", movesetsTable);
            while (iPokemonCount >= 0) {
                int movesetPtr = readPointer(movesetsTable + iPokemonCount * 4, true);
                if (movesetPtr == -1) {
                    iPokemonCount--;
                } else {
                    break;
                }
            }

            // sanity check: pokedex order
            // pokedex entries have to be within 0-1023
            // even after extending the dex
            // (at least with conventional methods)
            // so if we run into an invalid one
            // then we can cut off the count
            int pdOffset = romEntry.getIntValue("PokedexOrder");
            for (int i = 1; i <= iPokemonCount; i++) {
                int pdEntry = readWord(pdOffset + (i - 1) * 2);
                if (pdEntry > 1023) {
                    iPokemonCount = i - 1;
                    break;
                }
            }

            // write new pokemon count
            romEntry.putIntValue("PokemonCount", iPokemonCount);

            // update some key offsets from known pointers
            romEntry.putIntValue("PokemonTMHMCompat", readPointer(0x43C68));
            romEntry.putIntValue("PokemonEvolutions", readPointer(0x42F6C));
            romEntry.putIntValue("MoveTutorCompatibility", readPointer(0x120C30));
            int descsTable = readPointer(0xE5440);
            romEntry.putIntValue("MoveDescriptions", descsTable);
            int trainersTable = readPointer(0xFC00);
            romEntry.putIntValue("TrainerData", trainersTable);

            // try to detect number of moves using the descriptions
            int moveCount = 0;
            while (true) {
                int descPointer = readPointer(descsTable + (moveCount) * 4, true);
                if (descPointer != -1) {
                    int descStrLen = lengthOfStringAt(descPointer);
                    if (descStrLen > 0 && descStrLen < 100) {
                        // okay, this does seem fine
                        moveCount++;
                        continue;
                    }
                }
                break;
            }
            romEntry.putIntValue("MoveCount", moveCount);

            // attempt to detect number of trainers using various tells
            int trainerCount = 1;
            int tEntryLen = romEntry.getIntValue("TrainerEntrySize");
            int tNameLen = romEntry.getIntValue("TrainerNameLength");
            while (true) {
                int trOffset = trainersTable + tEntryLen * trainerCount;
                int pokeDataType = rom[trOffset] & 0xFF;
                if (pokeDataType >= 4) {
                    // only allowed 0-3
                    break;
                }
                int numPokes = rom[trOffset + (tEntryLen - 8)] & 0xFF;
                if (numPokes == 0 || numPokes > 6) {
                    break;
                }
                int pointerToPokes = readPointer(trOffset + (tEntryLen - 4), true);
                if (pointerToPokes == -1) {
                    break;
                }
                int nameLength = lengthOfStringAt(trOffset + 4) - 1;
                if (nameLength > tNameLen) {
                    break;
                }
                // found a valid trainer entry, recognize it
                trainerCount++;
            }
            romEntry.putIntValue("TrainerCount", trainerCount);
        }

    }

    private boolean basicBPRE10HackDetection() {
        if (rom.length != Gen3Constants.size16M) {
            return true;
        }
        long csum = FileFunctions.getCRC32(rom);
        return csum != 3716707868L;
    }

    private void loadPokedexOrder() {
        int pdOffset = romEntry.getIntValue("PokedexOrder");
        int numInternalPokes = romEntry.getIntValue("PokemonCount");
        int maxPokedex = 0;
        internalToPokedex = new int[numInternalPokes + 1];
        pokedexToInternal = new int[numInternalPokes + 1];
        for (int i = 1; i <= numInternalPokes; i++) {
            int dexEntry = readWord(rom, pdOffset + (i - 1) * 2);
            if (dexEntry != 0) {
                internalToPokedex[i] = dexEntry;
                // take the first pokemon only for each dex entry
                if (pokedexToInternal[dexEntry] == 0) {
                    pokedexToInternal[dexEntry] = i;
                }
                maxPokedex = Math.max(maxPokedex, dexEntry);
            }
        }
        if (maxPokedex == Gen3Constants.unhackedMaxPokedex) {
            // see if the slots between johto and hoenn are in use
            // old rom hacks use them instead of expanding pokes
            int offs = romEntry.getIntValue("PokemonStats");
            int usedSlots = 0;
            for (int i = 0; i < Gen3Constants.unhackedMaxPokedex - Gen3Constants.unhackedRealPokedex; i++) {
                int pokeSlot = Gen3Constants.hoennPokesStart + i;
                int pokeOffs = offs + pokeSlot * Gen3Constants.baseStatsEntrySize;
                String lowerName = pokeNames[pokeSlot].toLowerCase();
                if (!this.matches(rom, pokeOffs, Gen3Constants.emptyPokemonSig) && !lowerName.contains("unused")
                        && !lowerName.equals("?") && !lowerName.equals("-")) {
                    usedSlots++;
                    pokedexToInternal[Gen3Constants.unhackedRealPokedex + usedSlots] = pokeSlot;
                    internalToPokedex[pokeSlot] = Gen3Constants.unhackedRealPokedex + usedSlots;
                } else {
                    internalToPokedex[pokeSlot] = 0;
                }
            }
            // remove the fake extra slots
            for (int i = usedSlots + 1; i <= Gen3Constants.unhackedMaxPokedex - Gen3Constants.unhackedRealPokedex; i++) {
                pokedexToInternal[Gen3Constants.unhackedRealPokedex + i] = 0;
            }
            // if any slots were used at all, this is a rom hack
            if (usedSlots > 0) {
                this.isRomHack = true;
            }
            this.pokedexCount = Gen3Constants.unhackedRealPokedex + usedSlots;
        } else {
            this.isRomHack = true;
            this.pokedexCount = maxPokedex;
        }

    }

    private void constructPokemonList() {
        if (!this.isRomHack) {
            // simple behavior: all pokes in the dex are valid
            speciesList = Arrays.asList(pokes);
        } else {
            // only include "valid" pokes
            speciesList = new ArrayList<>();
            speciesList.add(null);
            for (int i = 1; i < pokes.length; i++) {
                Species pk = pokes[i];
                if (pk != null) {
                    String lowerName = pk.getName().toLowerCase();
                    if (!lowerName.contains("unused") && !lowerName.equals("?")) {
                        speciesList.add(pk);
                    }
                }
            }
        }
        numRealPokemon = speciesList.size() - 1;

    }

    @Override
    public void loadPokemonStats() {
        loadPokemonNames();
        loadPokedexOrder();

        pokes = new Species[this.pokedexCount + 1];
        int numInternalPokes = romEntry.getIntValue("PokemonCount");
        pokesInternal = new Species[numInternalPokes + 1];
        int offs = romEntry.getIntValue("PokemonStats");
        for (int i = 1; i <= numInternalPokes; i++) {
            int number = internalToPokedex[i];
            Species pk = new Species(number);
            pk.setName(pokeNames[i]);
            if (pk.getNumber() != 0) {
                pokes[pk.getNumber()] = pk;
            }
            pokesInternal[i] = pk;
            int pkoffs = offs + i * Gen3Constants.baseStatsEntrySize;
            loadBasicPokeStats(pk, pkoffs);
            pk.setGeneration(generationOf(pk));
        }

        // In these games, the alternate formes of Deoxys have hardcoded stats that are used 99% of the time;
        // the only times these hardcoded stats are ignored are during Link Battles. Since not many people
        // are using the randomizer to battle against others, let's just always use these stats.
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG || romEntry.getRomType() == Gen3Constants.RomType_Em) {
            String deoxysStatPrefix = romEntry.getStringValue("DeoxysStatPrefix");
            int offset = find(deoxysStatPrefix);
            if (offset > 0) {
                offset += deoxysStatPrefix.length() / 2; // because it was a prefix
                Species deoxys = pokes[SpeciesIDs.deoxys];
                deoxys.setHp(readWord(offset));
                deoxys.setAttack(readWord(offset + 2));
                deoxys.setDefense(readWord(offset + 4));
                deoxys.setSpeed(readWord(offset + 6));
                deoxys.setSpatk(readWord(offset + 8));
                deoxys.setSpdef(readWord(offset + 10));
            }
        }

        constructPokemonList();
    }

    private int generationOf(Species pk) {
        if (pk.getNumber() >= SpeciesIDs.treecko) {
            return 3;
        } else if (pk.getNumber() >= SpeciesIDs.chikorita) {
            return 2;
        }
        return 1;
    }

    @Override
    public void savePokemonStats() {
        // Write pokemon names & stats
        int offs = romEntry.getIntValue("PokemonNames");
        int nameLen = romEntry.getIntValue("PokemonNameLength");
        int offs2 = romEntry.getIntValue("PokemonStats");
        int numInternalPokes = romEntry.getIntValue("PokemonCount");
        for (int i = 1; i <= numInternalPokes; i++) {
            Species pk = pokesInternal[i];
            int stringOffset = offs + i * nameLen;
            writeFixedLengthString(pk.getName(), stringOffset, nameLen);
            saveBasicPokeStats(pk, offs2 + i * Gen3Constants.baseStatsEntrySize);
        }

        // Make sure to write to the hardcoded Deoxys stat location, since otherwise it will just have vanilla
        // stats no matter what settings the user selected.
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG || romEntry.getRomType() == Gen3Constants.RomType_Em) {
            String deoxysStatPrefix = romEntry.getStringValue("DeoxysStatPrefix");
            int offset = find(deoxysStatPrefix);
            if (offset > 0) {
                offset += deoxysStatPrefix.length() / 2; // because it was a prefix
                Species deoxys = pokes[SpeciesIDs.deoxys];
                writeWord(offset, deoxys.getHp());
                writeWord(offset + 2, deoxys.getAttack());
                writeWord(offset + 4, deoxys.getDefense());
                writeWord(offset + 6, deoxys.getSpeed());
                writeWord(offset + 8, deoxys.getSpatk());
                writeWord(offset + 10, deoxys.getSpdef());
            }
        }

        writeEvolutions();
    }

    @Override
    public void loadMoves() {
        int moveCount = romEntry.getIntValue("MoveCount");
        moves = new Move[moveCount + 1];
        int offs = romEntry.getIntValue("MoveData");
        int nameoffs = romEntry.getIntValue("MoveNames");
        int namelen = romEntry.getIntValue("MoveNameLength");
        for (int i = 1; i <= moveCount; i++) {
            moves[i] = new Move();
            moves[i].name = readFixedLengthString(nameoffs + i * namelen, namelen);
            moves[i].number = i;
            moves[i].internalId = i;
            moves[i].effectIndex = rom[offs + i * 0xC] & 0xFF;
            moves[i].hitratio = ((rom[offs + i * 0xC + 3] & 0xFF));
            moves[i].power = rom[offs + i * 0xC + 1] & 0xFF;
            moves[i].pp = rom[offs + i * 0xC + 4] & 0xFF;
            moves[i].type = Gen3Constants.typeTable[rom[offs + i * 0xC + 2]];
            moves[i].target = rom[offs + i * 0xC + 6] & 0xFF;
            moves[i].category = GBConstants.physicalTypes.contains(moves[i].type) ? MoveCategory.PHYSICAL : MoveCategory.SPECIAL;
            if (moves[i].power == 0 && !GlobalConstants.noPowerNonStatusMoves.contains(i)) {
                moves[i].category = MoveCategory.STATUS;
            }
            moves[i].priority = rom[offs + i * 0xC + 7];
            int flags = rom[offs + i * 0xC + 8] & 0xFF;
            moves[i].makesContact = (flags & 1) != 0;
            moves[i].isSoundMove = Gen3Constants.soundMoves.contains(moves[i].number);

            if (i == MoveIDs.swift) {
                perfectAccuracy = (int)moves[i].hitratio;
            }

            if (GlobalConstants.normalMultihitMoves.contains(i)) {
                moves[i].hitCount = 3;
            } else if (GlobalConstants.doubleHitMoves.contains(i)) {
                moves[i].hitCount = 2;
            } else if (i == MoveIDs.tripleKick) {
                moves[i].hitCount = 2.71; // this assumes the first hit lands
            }

            int secondaryEffectChance = rom[offs + i * 0xC + 5] & 0xFF;
            loadStatChangesFromEffect(moves[i], secondaryEffectChance);
            loadStatusFromEffect(moves[i], secondaryEffectChance);
            loadMiscMoveInfoFromEffect(moves[i], secondaryEffectChance);
        }
    }

    private void loadStatChangesFromEffect(Move move, int secondaryEffectChance) {
        switch (move.effectIndex) {
            case Gen3Constants.noDamageAtkPlusOneEffect:
            case Gen3Constants.noDamageDefPlusOneEffect:
            case Gen3Constants.noDamageSpAtkPlusOneEffect:
            case Gen3Constants.noDamageEvasionPlusOneEffect:
            case Gen3Constants.noDamageAtkMinusOneEffect:
            case Gen3Constants.noDamageDefMinusOneEffect:
            case Gen3Constants.noDamageSpeMinusOneEffect:
            case Gen3Constants.noDamageAccuracyMinusOneEffect:
            case Gen3Constants.noDamageEvasionMinusOneEffect:
            case Gen3Constants.noDamageAtkPlusTwoEffect:
            case Gen3Constants.noDamageDefPlusTwoEffect:
            case Gen3Constants.noDamageSpePlusTwoEffect:
            case Gen3Constants.noDamageSpAtkPlusTwoEffect:
            case Gen3Constants.noDamageSpDefPlusTwoEffect:
            case Gen3Constants.noDamageAtkMinusTwoEffect:
            case Gen3Constants.noDamageDefMinusTwoEffect:
            case Gen3Constants.noDamageSpeMinusTwoEffect:
            case Gen3Constants.noDamageSpDefMinusTwoEffect:
            case Gen3Constants.minimizeEffect:
            case Gen3Constants.swaggerEffect:
            case Gen3Constants.defenseCurlEffect:
            case Gen3Constants.flatterEffect:
            case Gen3Constants.chargeEffect:
            case Gen3Constants.noDamageAtkAndDefMinusOneEffect:
            case Gen3Constants.noDamageDefAndSpDefPlusOneEffect:
            case Gen3Constants.noDamageAtkAndDefPlusOneEffect:
            case Gen3Constants.noDamageSpAtkAndSpDefPlusOneEffect:
            case Gen3Constants.noDamageAtkAndSpePlusOneEffect:
                if (move.target == 16) {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_USER;
                } else {
                    move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_TARGET;
                }
                break;

            case Gen3Constants.damageAtkMinusOneEffect:
            case Gen3Constants.damageDefMinusOneEffect:
            case Gen3Constants.damageSpeMinusOneEffect:
            case Gen3Constants.damageSpAtkMinusOneEffect:
            case Gen3Constants.damageSpDefMinusOneEffect:
            case Gen3Constants.damageAccuracyMinusOneEffect:
                move.statChangeMoveType = StatChangeMoveType.DAMAGE_TARGET;
                break;

            case Gen3Constants.damageUserDefPlusOneEffect:
            case Gen3Constants.damageUserAtkPlusOneEffect:
            case Gen3Constants.damageUserAllPlusOneEffect:
            case Gen3Constants.damageUserAtkAndDefMinusOneEffect:
            case Gen3Constants.damageUserSpAtkMinusTwoEffect:
                move.statChangeMoveType = StatChangeMoveType.DAMAGE_USER;
                break;

            default:
                // Move does not have a stat-changing effect
                return;
        }

        switch (move.effectIndex) {
            case Gen3Constants.noDamageAtkPlusOneEffect:
            case Gen3Constants.damageUserAtkPlusOneEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.noDamageDefPlusOneEffect:
            case Gen3Constants.damageUserDefPlusOneEffect:
            case Gen3Constants.defenseCurlEffect: {
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.noDamageSpAtkPlusOneEffect:
            case Gen3Constants.flatterEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.noDamageEvasionPlusOneEffect:
            case Gen3Constants.minimizeEffect: {
                move.statChanges[0].type = StatChangeType.EVASION;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.noDamageAtkMinusOneEffect:
            case Gen3Constants.damageAtkMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.noDamageDefMinusOneEffect:
            case Gen3Constants.damageDefMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.noDamageSpeMinusOneEffect:
            case Gen3Constants.damageSpeMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.noDamageAccuracyMinusOneEffect:
            case Gen3Constants.damageAccuracyMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.ACCURACY;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.noDamageEvasionMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.EVASION;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.noDamageAtkPlusTwoEffect:
            case Gen3Constants.swaggerEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 2;
                break;
            }
            case Gen3Constants.noDamageDefPlusTwoEffect: {
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 2;
                break;
            }
            case Gen3Constants.noDamageSpePlusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = 2;
                break;
            }
            case Gen3Constants.noDamageSpAtkPlusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = 2;
                break;
            }
            case Gen3Constants.noDamageSpDefPlusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = 2;
                break;
            }
            case Gen3Constants.noDamageAtkMinusTwoEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -2;
                break;
            }
            case Gen3Constants.noDamageDefMinusTwoEffect: {
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = -2;
                break;
            }
            case Gen3Constants.noDamageSpeMinusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPEED;
                move.statChanges[0].stages = -2;
                break;
            }
            case Gen3Constants.noDamageSpDefMinusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = -2;
                break;
            }
            case Gen3Constants.damageSpAtkMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.damageSpDefMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = -1;
                break;
            }
            case Gen3Constants.damageUserAllPlusOneEffect: {
                move.statChanges[0].type = StatChangeType.ALL;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.chargeEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[0].stages = 1;
                break;
            }
            case Gen3Constants.damageUserAtkAndDefMinusOneEffect:
            case Gen3Constants.noDamageAtkAndDefMinusOneEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = -1;
                move.statChanges[1].type = StatChangeType.DEFENSE;
                move.statChanges[1].stages = -1;
                break;
            }
            case Gen3Constants.damageUserSpAtkMinusTwoEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = -2;
                break;
            }
            case Gen3Constants.noDamageDefAndSpDefPlusOneEffect: {
                move.statChanges[0].type = StatChangeType.DEFENSE;
                move.statChanges[0].stages = 1;
                move.statChanges[1].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[1].stages = 1;
                break;
            }
            case Gen3Constants.noDamageAtkAndDefPlusOneEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 1;
                move.statChanges[1].type = StatChangeType.DEFENSE;
                move.statChanges[1].stages = 1;
                break;
            }
            case Gen3Constants.noDamageSpAtkAndSpDefPlusOneEffect: {
                move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
                move.statChanges[0].stages = 1;
                move.statChanges[1].type = StatChangeType.SPECIAL_DEFENSE;
                move.statChanges[1].stages = 1;
                break;
            }
            case Gen3Constants.noDamageAtkAndSpePlusOneEffect: {
                move.statChanges[0].type = StatChangeType.ATTACK;
                move.statChanges[0].stages = 1;
                move.statChanges[1].type = StatChangeType.SPEED;
                move.statChanges[1].stages = 1;
                break;
            }
        }

        if (move.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET || move.statChangeMoveType == StatChangeMoveType.DAMAGE_USER) {
            for (int i = 0; i < move.statChanges.length; i++) {
                if (move.statChanges[i].type != StatChangeType.NONE) {
                    move.statChanges[i].percentChance = secondaryEffectChance;
                    if (move.statChanges[i].percentChance == 0.0) {
                        move.statChanges[i].percentChance = 100.0;
                    }
                }
            }
        }
    }

    private void loadStatusFromEffect(Move move, int secondaryEffectChance) {
        if (move.number == MoveIDs.bounce) {
            // GF hardcoded this, so we have to as well
            move.statusMoveType = StatusMoveType.DAMAGE;
            move.statusType = StatusType.PARALYZE;
            move.statusPercentChance = secondaryEffectChance;
            return;
        }

        switch (move.effectIndex) {
            case Gen3Constants.noDamageSleepEffect:
            case Gen3Constants.toxicEffect:
            case Gen3Constants.noDamageConfusionEffect:
            case Gen3Constants.noDamagePoisonEffect:
            case Gen3Constants.noDamageParalyzeEffect:
            case Gen3Constants.noDamageBurnEffect:
            case Gen3Constants.swaggerEffect:
            case Gen3Constants.flatterEffect:
            case Gen3Constants.teeterDanceEffect:
                move.statusMoveType = StatusMoveType.NO_DAMAGE;
                break;

            case Gen3Constants.damagePoisonEffect:
            case Gen3Constants.damageBurnEffect:
            case Gen3Constants.damageFreezeEffect:
            case Gen3Constants.damageParalyzeEffect:
            case Gen3Constants.damageConfusionEffect:
            case Gen3Constants.twineedleEffect:
            case Gen3Constants.damageBurnAndThawUserEffect:
            case Gen3Constants.thunderEffect:
            case Gen3Constants.blazeKickEffect:
            case Gen3Constants.poisonFangEffect:
            case Gen3Constants.poisonTailEffect:
                move.statusMoveType = StatusMoveType.DAMAGE;
                break;

            default:
                // Move does not have a status effect
                return;
        }

        switch (move.effectIndex) {
            case Gen3Constants.noDamageSleepEffect:
                move.statusType = StatusType.SLEEP;
                break;
            case Gen3Constants.damagePoisonEffect:
            case Gen3Constants.noDamagePoisonEffect:
            case Gen3Constants.twineedleEffect:
            case Gen3Constants.poisonTailEffect:
                move.statusType = StatusType.POISON;
                break;
            case Gen3Constants.damageBurnEffect:
            case Gen3Constants.damageBurnAndThawUserEffect:
            case Gen3Constants.noDamageBurnEffect:
            case Gen3Constants.blazeKickEffect:
                move.statusType = StatusType.BURN;
                break;
            case Gen3Constants.damageFreezeEffect:
                move.statusType = StatusType.FREEZE;
                break;
            case Gen3Constants.damageParalyzeEffect:
            case Gen3Constants.noDamageParalyzeEffect:
            case Gen3Constants.thunderEffect:
                move.statusType = StatusType.PARALYZE;
                break;
            case Gen3Constants.toxicEffect:
            case Gen3Constants.poisonFangEffect:
                move.statusType = StatusType.TOXIC_POISON;
                break;
            case Gen3Constants.noDamageConfusionEffect:
            case Gen3Constants.damageConfusionEffect:
            case Gen3Constants.swaggerEffect:
            case Gen3Constants.flatterEffect:
            case Gen3Constants.teeterDanceEffect:
                move.statusType = StatusType.CONFUSION;
                break;
        }

        if (move.statusMoveType == StatusMoveType.DAMAGE) {
            move.statusPercentChance = secondaryEffectChance;
            if (move.statusPercentChance == 0.0) {
                move.statusPercentChance = 100.0;
            }
        }
    }

    private void loadMiscMoveInfoFromEffect(Move move, int secondaryEffectChance) {
        switch (move.effectIndex) {
            case Gen3Constants.increasedCritEffect:
            case Gen3Constants.blazeKickEffect:
            case Gen3Constants.poisonTailEffect:
                move.criticalChance = CriticalChance.INCREASED;
                break;

            case Gen3Constants.futureSightAndDoomDesireEffect:
            case Gen3Constants.spitUpEffect:
                move.criticalChance = CriticalChance.NONE;

            case Gen3Constants.flinchEffect:
            case Gen3Constants.snoreEffect:
            case Gen3Constants.twisterEffect:
            case Gen3Constants.flinchWithMinimizeBonusEffect:
            case Gen3Constants.fakeOutEffect:
                move.flinchPercentChance = secondaryEffectChance;
                break;

            case Gen3Constants.damageAbsorbEffect:
            case Gen3Constants.dreamEaterEffect:
                move.absorbPercent = 50;
                break;

            case Gen3Constants.damageRecoil25PercentEffect:
                move.recoilPercent = 25;
                break;

            case Gen3Constants.damageRecoil33PercentEffect:
                move.recoilPercent = 33;
                break;

            case Gen3Constants.bindingEffect:
            case Gen3Constants.trappingEffect:
                move.isTrapMove = true;
                break;

            case Gen3Constants.razorWindEffect:
            case Gen3Constants.skullBashEffect:
            case Gen3Constants.solarbeamEffect:
            case Gen3Constants.semiInvulnerableEffect:
                move.isChargeMove = true;
                break;

            case Gen3Constants.rechargeEffect:
                move.isRechargeMove = true;
                break;

            case Gen3Constants.skyAttackEffect:
                move.criticalChance = CriticalChance.INCREASED;
                move.flinchPercentChance = secondaryEffectChance;
                move.isChargeMove = true;
                break;
        }
    }

    @Override
    public void saveMoves() {
        int moveCount = romEntry.getIntValue("MoveCount");
        int offs = romEntry.getIntValue("MoveData");
        for (int i = 1; i <= moveCount; i++) {

            int hitratio = (int) Math.round(moves[i].hitratio);
            hitratio = Math.max(hitratio, 0);
            hitratio = Math.min(hitratio, 100);

            // TODO: where does this 0xC come from?
            writeBytes(offs + i * 0xC, new byte[] { (byte) moves[i].effectIndex,
                    (byte) moves[i].power, Gen3Constants.typeToByte(moves[i].type),
                    (byte) hitratio, (byte) moves[i].pp });
        }
    }

    public List<Move> getMoves() {
        return Arrays.asList(moves);
    }

    private void loadBasicPokeStats(Species pkmn, int offset) {
        pkmn.setHp(rom[offset + Gen3Constants.bsHPOffset] & 0xFF);
        pkmn.setAttack(rom[offset + Gen3Constants.bsAttackOffset] & 0xFF);
        pkmn.setDefense(rom[offset + Gen3Constants.bsDefenseOffset] & 0xFF);
        pkmn.setSpeed(rom[offset + Gen3Constants.bsSpeedOffset] & 0xFF);
        pkmn.setSpatk(rom[offset + Gen3Constants.bsSpAtkOffset] & 0xFF);
        pkmn.setSpdef(rom[offset + Gen3Constants.bsSpDefOffset] & 0xFF);
        // Type
        pkmn.setPrimaryType(Gen3Constants.typeTable[rom[offset + Gen3Constants.bsPrimaryTypeOffset] & 0xFF]);
        pkmn.setSecondaryType(Gen3Constants.typeTable[rom[offset + Gen3Constants.bsSecondaryTypeOffset] & 0xFF]);
        // Only one type?
        if (pkmn.getSecondaryType(false) == pkmn.getPrimaryType(false)) {
            pkmn.setSecondaryType(null);
        }
        pkmn.setCatchRate(rom[offset + Gen3Constants.bsCatchRateOffset] & 0xFF);
        pkmn.setGrowthCurve(ExpCurve.fromByte(rom[offset + Gen3Constants.bsGrowthCurveOffset]));
        // Abilities
        pkmn.setAbility1(rom[offset + Gen3Constants.bsAbility1Offset] & 0xFF);
        pkmn.setAbility2(rom[offset + Gen3Constants.bsAbility2Offset] & 0xFF);

        // Held Items?
        int item1ID = Gen3Constants.itemIDToStandard(readWord(offset + Gen3Constants.bsCommonHeldItemOffset));
        Item item1 = items.get(item1ID);
        int item2ID = Gen3Constants.itemIDToStandard(readWord(offset + Gen3Constants.bsRareHeldItemOffset));
        Item item2 = items.get(item2ID);

        if (Objects.equals(item1, item2)) {
            // guaranteed
            pkmn.setGuaranteedHeldItem(item1);
        } else {
            pkmn.setCommonHeldItem(item1);
            pkmn.setRareHeldItem(item2);
        }

        pkmn.setGenderRatio(rom[offset + Gen3Constants.bsGenderRatioOffset] & 0xFF);
    }

    private void saveBasicPokeStats(Species pkmn, int offset) {
        writeByte(offset + Gen3Constants.bsHPOffset, (byte) pkmn.getHp());
        writeByte(offset + Gen3Constants.bsAttackOffset, (byte) pkmn.getAttack());
        writeByte(offset + Gen3Constants.bsDefenseOffset, (byte) pkmn.getDefense());
        writeByte(offset + Gen3Constants.bsSpeedOffset, (byte) pkmn.getSpeed());
        writeByte(offset + Gen3Constants.bsSpAtkOffset, (byte) pkmn.getSpatk());
        writeByte(offset + Gen3Constants.bsSpDefOffset, (byte) pkmn.getSpdef());
        writeByte(offset + Gen3Constants.bsPrimaryTypeOffset, Gen3Constants.typeToByte(pkmn.getPrimaryType(false)));
        writeByte(offset + Gen3Constants.bsSecondaryTypeOffset, Gen3Constants.typeToByte(
                pkmn.getSecondaryType(false) == null ? pkmn.getPrimaryType(false) : pkmn.getSecondaryType(false)
        ));
        writeByte(offset + Gen3Constants.bsCatchRateOffset, (byte) pkmn.getCatchRate());
        writeByte(offset + Gen3Constants.bsGrowthCurveOffset, pkmn.getGrowthCurve().toByte());

        writeByte(offset + Gen3Constants.bsAbility1Offset, (byte) pkmn.getAbility1());
        writeByte(offset + Gen3Constants.bsAbility2Offset, (byte) (
                pkmn.getAbility2() == 0 ? pkmn.getAbility1() :
                        pkmn.getAbility2())); // required to not break evos with random ability

        // Held items
        if (pkmn.getGuaranteedHeldItem() != null) {
            int internalID = Gen3Constants.itemIDToInternal(pkmn.getGuaranteedHeldItem().getId());
            writeWord(offset + Gen3Constants.bsCommonHeldItemOffset, internalID);
            writeWord(offset + Gen3Constants.bsRareHeldItemOffset, internalID);
        } else {
            // assumes common/rareHeldItem to be non-null, if guaranteedHeldItem is.
            int commonInternalID = pkmn.getCommonHeldItem() == null ? 0
                    : Gen3Constants.itemIDToInternal(pkmn.getCommonHeldItem().getId());
            writeWord(offset + Gen3Constants.bsCommonHeldItemOffset, commonInternalID);
            int rareInternalID = pkmn.getRareHeldItem() == null ? 0
                    : Gen3Constants.itemIDToInternal(pkmn.getRareHeldItem().getId());
            writeWord(offset + Gen3Constants.bsRareHeldItemOffset, rareInternalID);
        }

        writeByte(offset + Gen3Constants.bsGenderRatioOffset, (byte) pkmn.getGenderRatio());
    }

    private void loadPokemonNames() {
        int offs = romEntry.getIntValue("PokemonNames");
        int nameLen = romEntry.getIntValue("PokemonNameLength");
        int numInternalPokes = romEntry.getIntValue("PokemonCount");
        pokeNames = new String[numInternalPokes + 1];
        for (int i = 1; i <= numInternalPokes; i++) {
            pokeNames[i] = readFixedLengthString(offs + i * nameLen, nameLen);
        }
    }

    private String readString(int offset, int maxLength) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < maxLength; i++) {
            byte currChar = rom[offset + i];
            String translated = tb[Byte.toUnsignedInt(currChar)];
            if (translated != null) {
                string.append(translated);
            } else {
                if (currChar == Gen3Constants.textTerminator) {
                    break;
                } else if (currChar == Gen3Constants.textVariable) {
                    int nextChar = rom[offset + i + 1] & 0xFF;
                    string.append("\\v").append(String.format("%02X", nextChar));
                    i++;
                } else {
                    string.append("\\x").append(String.format("%02X", currChar));
                }
            }
        }
        return string.toString();
    }

    private byte[] translateString(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (!text.isEmpty()) {
            int i = Math.max(0, 4 - text.length());
            if (text.charAt(0) == '\\' && text.charAt(1) == 'x') {
                baos.write((byte) Integer.parseInt(text.substring(2, 4), 16));
                text = text.substring(4);
            } else if (text.charAt(0) == '\\' && text.charAt(1) == 'v') {
                baos.write(Gen3Constants.textVariable);
                baos.write((byte) Integer.parseInt(text.substring(2, 4), 16));
                text = text.substring(4);
            } else {
                while (!(d.containsKey(text.substring(0, 4 - i)) || (i == 4))) {
                    i++;
                }
                if (i == 4) {
                    text = text.substring(1);
                } else {
                    baos.write(d.get(text.substring(0, 4 - i)));
                    text = text.substring(4 - i);
                }
            }
        }
        return baos.toByteArray();
    }

    private String readFixedLengthString(int offset, int length) {
        return readString(offset, length);
    }

    private String readVariableLengthString(int offset) {
        return readString(offset, Integer.MAX_VALUE);
    }

    private void writeFixedLengthString(String str, int offset, int length) {
        byte[] translated = translateString(str);
        int len = Math.min(translated.length, length);
        System.arraycopy(translated, 0, rom, offset, len);
        if (len < length) {
            writeByte(offset + len, Gen3Constants.textTerminator);
            len++;
        }
        while (len < length) {
            writeByte(offset + len, Gen3Constants.textPadding);
            len++;
        }
    }

    @Deprecated
    private void writeVariableLengthString(String str, int offset) {
        System.out.println("writeVariableLengthString() is deprecated in favor of rewriteVariableLengthString(). " +
                "A variable length string should always have a pointer to it, and rewriteVariableLengthString() " +
                "handles that better.");
        byte[] translated = translateString(str);
        System.arraycopy(translated, 0, rom, offset, translated.length);
        writeByte(offset + translated.length, Gen3Constants.textTerminator);
    }

    private int lengthOfStringAt(int offset) {
        return lengthOfDataWithTerminatorAt(offset, Gen3Constants.textTerminator);
    }

    private static boolean romName(byte[] rom, String name) {
        int sigOffset = Gen3Constants.romNameOffset;
        byte[] sigBytes = name.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < sigBytes.length; i++) {
            if (rom[sigOffset + i] != sigBytes[i]) {
                return false;
            }
        }
        return true;

    }

    private static boolean romCode(byte[] rom, String codeToCheck) {
        int sigOffset = Gen3Constants.romCodeOffset;
        byte[] sigBytes = codeToCheck.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < sigBytes.length; i++) {
            if (rom[sigOffset + i] != sigBytes[i]) {
                return false;
            }
        }
        return true;

    }

    @Override
    protected int readPointer(int offset) {
        return readPointer(offset, false);
    }

    protected int readPointer(int offset, boolean handleInvalidPointerExternally) {
        int pointer = readLong(offset) - 0x8000000;
        if (pointer < 0 || pointer > rom.length) {
            // has these two modes because you don't want to try/catch for expected behaviors;
            // in those cases it can check for -1 instead.
            if (handleInvalidPointerExternally) {
                return -1;
            } else {
                throw new IllegalArgumentException("No valid pointer at 0x" + Integer.toHexString(offset) + ".");
            }
        }
        return pointer;
    }

    private int readLong(int offset) {
        return (rom[offset] & 0xFF) + ((rom[offset + 1] & 0xFF) << 8) + ((rom[offset + 2] & 0xFF) << 16)
                + (((rom[offset + 3] & 0xFF)) << 24);
    }

    @Override
    protected void writePointer(int offset, int pointer) {
        writeLong(offset, pointer + 0x8000000);
    }

    private void writeLong(int offset, int value) {
        writeBytes(offset, new byte[] { (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF) });
    }

    @Override
    public List<Species> getStarters() {
        List<Species> starters = new ArrayList<>();
        int baseOffset = romEntry.getIntValue("StarterPokemon");
        Species starter1 = pokesInternal[readWord(baseOffset)];
        Species starter2;
        Species starter3;
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp
                || romEntry.getRomType() == Gen3Constants.RomType_Em) {
            // do something
            starter2 = pokesInternal[readWord(baseOffset + Gen3Constants.rseStarter2Offset)];
            starter3 = pokesInternal[readWord(baseOffset + Gen3Constants.rseStarter3Offset)];
        } else {
            // do something else
            starter2 = pokesInternal[readWord(baseOffset + Gen3Constants.frlgStarter2Offset)];
            starter3 = pokesInternal[readWord(baseOffset + Gen3Constants.frlgStarter3Offset)];
        }
        starters.add(starter1);
        starters.add(starter2);
        starters.add(starter3);
        return starters;
    }

    @Override
    public boolean setStarters(List<Species> starters) {
        if (starters.size() != 3) {
            throw new IllegalArgumentException("Wrong amount of starters, must be 3.");
        }

        // Support Deoxys/Mew starters in E/FR/LG
        attemptObedienceEvolutionPatches();
        writeStarterBytes(starters);
        writeStarterText(starters);
        return true;

    }

    private void writeStarterBytes(List<Species> starters) {
        int baseOffset = romEntry.getIntValue("StarterPokemon");
        int starter0 = pokedexToInternal[starters.get(0).getNumber()];
        int starter1 = pokedexToInternal[starters.get(1).getNumber()];
        int starter2 = pokedexToInternal[starters.get(2).getNumber()];
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp
                || romEntry.getRomType() == Gen3Constants.RomType_Em) {

            // US
            // order: 0, 1, 2
            writeWord(baseOffset, starter0);
            writeWord(baseOffset + Gen3Constants.rseStarter2Offset, starter1);
            writeWord(baseOffset + Gen3Constants.rseStarter3Offset, starter2);

        } else {
            // frlg:
            // order: 0, 1, 2
            writeWord(baseOffset, starter0);
            writeWord(baseOffset + Gen3Constants.frlgStarterRepeatOffset, starter1);

            writeWord(baseOffset + Gen3Constants.frlgStarter2Offset, starter1);
            writeWord(baseOffset + Gen3Constants.frlgStarter2Offset + Gen3Constants.frlgStarterRepeatOffset, starter2);

            writeWord(baseOffset + Gen3Constants.frlgStarter3Offset, starter2);
            writeWord(baseOffset + Gen3Constants.frlgStarter3Offset + Gen3Constants.frlgStarterRepeatOffset, starter0);
        }
    }

    private void writeStarterText(List<Species> starters) {
        writeEventText(romEntry.getStarterTexts(), id -> {
            Species starter = starters.get(id);
            Type t = starter.getPrimaryType(false);
            if (t == Type.NORMAL && starter.getSecondaryType(false) != null) {
                t = starter.getSecondaryType(false);
            }
            return new String[] {starter.getName(), t.toString()};
            }, new String[] {"[starter]", "[type]"}, "Starter");
    }

    @Override
    public boolean hasStarterAltFormes() {
        return false;
    }

    @Override
    public int starterCount() {
        return 3;
    }

    @Override
    public boolean supportsStarterHeldItems() {
        return true;
    }

    @Override
    public List<Item> getStarterHeldItems() {
        List<Item> sHeldItems = new ArrayList<>();
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            // offset from normal starter offset as a word
            int baseOffset = romEntry.getIntValue("StarterPokemon");
            int internalID = readWord(baseOffset + Gen3Constants.frlgStarterItemsOffset);
            sHeldItems.add(items.get(Gen3Constants.itemIDToStandard(internalID)));
        } else {
            int baseOffset = romEntry.getIntValue("StarterItems");
            int i1 = rom[baseOffset] & 0xFF;
            int i2 = rom[baseOffset + 2] & 0xFF;
            int internalID = i2 == 0 ? i1 : i2 + 0xFF;
            sHeldItems.add(items.get(Gen3Constants.itemIDToStandard(internalID)));
        }
        return sHeldItems;
    }

    @Override
    public void setStarterHeldItems(List<Item> items) {
        if (items.size() != 1) {
            return;
        }
        Item item = items.get(0);
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            // offset from normal starter offset as a word
            int baseOffset = romEntry.getIntValue("StarterPokemon");
            int internalID = item == null ? 0 : Gen3Constants.itemIDToInternal(item.getId());
            writeWord(baseOffset + Gen3Constants.frlgStarterItemsOffset, internalID);
        } else {
            int baseOffset = romEntry.getIntValue("StarterItems");
            int internalID = item == null ? 0 : Gen3Constants.itemIDToInternal(item.getId());
            if (internalID <= 0xFF) {
                rom[baseOffset] = (byte) (internalID & 0xFF);
                rom[baseOffset + 2] = 0;
            } else {
                rom[baseOffset] = (byte) 0xFF;
                rom[baseOffset + 2] = (byte) ((internalID - 0xFF) & 0xFF);
            }
            rom[baseOffset + 3] = Gen3Constants.gbaAddRxOpcode | Gen3Constants.gbaR2;
        }
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        if (!mapLoadingDone) {
            preprocessMaps();
            mapLoadingDone = true;
        }

        int startOffs = romEntry.getIntValue("WildPokemon");
        List<EncounterArea> encounterAreas = new ArrayList<>();
        Set<Integer> seenOffsets = new TreeSet<>();
        int offs = startOffs;
        while (true) {
            // Read pointers
            int bank = rom[offs] & 0xFF;
            int map = rom[offs + 1] & 0xFF;
            if (bank == 0xFF && map == 0xFF) {
                break;
            }

            String mapName = mapNames[bank][map];

            int walkingOffset = readPointer(offs + 4, true);
            int surfingOffset = readPointer(offs + 8, true);
            int rockSmashOffset = readPointer(offs + 12, true);
            int fishingOffset = readPointer(offs + 16, true);

            // Add pokemanz
            if (walkingOffset != -1 && rom[walkingOffset] != 0
                    && !seenOffsets.contains(readPointer(walkingOffset + 4))) {
                encounterAreas.add(readEncounterArea(walkingOffset, Gen3Constants.walkingSlots,
                        mapName + " Grass/Cave", EncounterType.WALKING));
                seenOffsets.add(readPointer(walkingOffset + 4));
            }
            if (surfingOffset != -1 && rom[surfingOffset] != 0
                    && !seenOffsets.contains(readPointer(surfingOffset + 4))) {
                encounterAreas.add(readEncounterArea(surfingOffset, Gen3Constants.surfingSlots,
                        mapName + " Surfing", EncounterType.SURFING));
                seenOffsets.add(readPointer(surfingOffset + 4));
            }
            if (rockSmashOffset != -1 && rom[rockSmashOffset] != 0
                    && !seenOffsets.contains(readPointer(rockSmashOffset + 4))) {
                encounterAreas.add(readEncounterArea(rockSmashOffset, Gen3Constants.rockSmashSlots,
                        mapName + " Rock Smash", EncounterType.INTERACT));
                seenOffsets.add(readPointer(rockSmashOffset + 4));
            }
            if (fishingOffset != -1 && rom[fishingOffset] != 0
                    && !seenOffsets.contains(readPointer(fishingOffset + 4))) {
                encounterAreas.add(readEncounterArea(fishingOffset, Gen3Constants.fishingSlots,
                        mapName + " Fishing", EncounterType.FISHING));
                seenOffsets.add(readPointer(fishingOffset + 4));
            }

            offs += 20;
        }
        int[] battleTrappersBannedAreas = romEntry.getArrayValue("BattleTrappersBanned");
        if (battleTrappersBannedAreas.length > 0) {
            // Some encounter areas aren't allowed to have Pokemon
            // with Arena Trap, Shadow Tag etc.
            Set<Species> battleTrappers = new HashSet<>();
            for (Species pk : getSpecies()) {
                if (hasBattleTrappingAbility(pk)) {
                    battleTrappers.add(pk);
                }
            }
            for (int areaIdx : battleTrappersBannedAreas) {
                encounterAreas.get(areaIdx).banAllSpecies(battleTrappers);
            }
        }

        new Gen3EncounterAreaTagger().tag(encounterAreas, romEntry.getRomType(), false);

        return encounterAreas;
    }

    private boolean hasBattleTrappingAbility(Species species) {
        return species != null
                && (GlobalConstants.battleTrappingAbilities.contains(species.getAbility1()) || GlobalConstants.battleTrappingAbilities
                        .contains(species.getAbility2()));
    }

    private EncounterArea readEncounterArea(int offset, int numOfEntries, String name, EncounterType encounterType) {
        EncounterArea area = new EncounterArea();
        area.setRate(rom[offset]);
        area.setDisplayName(name);
        area.setEncounterType(encounterType);
        // Grab the *real* pointer to data
        int dataOffset = readPointer(offset + 4);
        // Read the entries
        for (int i = 0; i < numOfEntries; i++) {
            // min, max, species, species
            Encounter enc = new Encounter();
            enc.setLevel(rom[dataOffset + i * 4]);
            enc.setMaxLevel(rom[dataOffset + i * 4 + 1]);
            enc.setSpecies(pokesInternal[readWord(dataOffset + i * 4 + 2)]);
            area.add(enc);
        }
        return area;
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        List<String> locationTagsTraverseOrder = Gen3Constants.getLocationTagsTraverseOrder(getROMType());
        return getEncounters(useTimeOfDay).stream()
                .sorted(Comparator.comparingInt(a -> locationTagsTraverseOrder.indexOf(a.getLocationTag())))
                .collect(Collectors.toList());
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas) {
        // Support Deoxys/Mew catches in E/FR/LG
        attemptObedienceEvolutionPatches();

        int startOffs = romEntry.getIntValue("WildPokemon");
        Iterator<EncounterArea> areaIterator = encounterAreas.iterator();
        Set<Integer> seenOffsets = new TreeSet<>();
        int offs = startOffs;
        while (true) {
            // Read pointers
            int bank = rom[offs] & 0xFF;
            int map = rom[offs + 1] & 0xFF;
            if (bank == 0xFF && map == 0xFF) {
                break;
            }

            int walkingOffset = readPointer(offs + 4, true);
            int surfingOffset = readPointer(offs + 8, true);
            int rockSmashOffset = readPointer(offs + 12, true);
            int fishingOffsets = readPointer(offs + 16, true);

            // Add pokemanz
            if (walkingOffset != -1 && rom[walkingOffset] != 0
                    && !seenOffsets.contains(readPointer(walkingOffset + 4))) {
                writeEncounterArea(walkingOffset, Gen3Constants.walkingSlots, areaIterator.next());
                seenOffsets.add(readPointer(walkingOffset + 4));
            }
            if (surfingOffset != -1 && rom[surfingOffset] != 0
                    && !seenOffsets.contains(readPointer(surfingOffset + 4))) {
                writeEncounterArea(surfingOffset, Gen3Constants.surfingSlots, areaIterator.next());
                seenOffsets.add(readPointer(surfingOffset + 4));
            }
            if (rockSmashOffset != -1 && rom[rockSmashOffset] != 0
                    && !seenOffsets.contains(readPointer(rockSmashOffset + 4))) {
                writeEncounterArea(rockSmashOffset, Gen3Constants.rockSmashSlots, areaIterator.next());
                seenOffsets.add(readPointer(rockSmashOffset + 4));
            }
            if (fishingOffsets != -1 && rom[fishingOffsets] != 0
                    && !seenOffsets.contains(readPointer(fishingOffsets + 4))) {
                writeEncounterArea(fishingOffsets, Gen3Constants.fishingSlots, areaIterator.next());
                seenOffsets.add(readPointer(fishingOffsets + 4));
            }

            offs += 20;
        }
    }

    private void writeEncounterArea(int offset, int numOfEntries, EncounterArea area) {
        // Grab the *real* pointer to data
        int dataOffset = readPointer(offset + 4);
        // Write the entries
        for (int i = 0; i < numOfEntries; i++) {
            Encounter enc = area.get(i);
            // min, max, species, species
            int levels = enc.getLevel() | (enc.getMaxLevel() << 8);
            writeWord(dataOffset + i * 4, levels);
            writeWord(dataOffset + i * 4 + 2, pokedexToInternal[enc.getSpecies().getNumber()]);
        }
    }

    @Override
    public boolean hasEncounterLocations() {
        return true;
    }

    @Override
    public boolean hasWildAltFormes() {
        return false;
    }

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        SpeciesSet banned = new SpeciesSet();
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            // Ban Unown in FRLG because the game crashes if it is encountered outside of Tanoby Ruins.
            // See GenerateWildMon in wild_encounter.c in pokefirered
            banned.add(pokes[SpeciesIDs.unown]);
        }
        return banned;
    }

    @Override
    public List<Trainer> getTrainers() {
        if (trainers == null) {
            throw new IllegalStateException("Trainers have not been loaded.");
        }
        return trainers;
    }

    @Override
    public void loadTrainers() {
        trainers = new ArrayList<>();
        int baseOffset = romEntry.getIntValue("TrainerData");
        int amount = romEntry.getIntValue("TrainerCount");
        int entryLen = romEntry.getIntValue("TrainerEntrySize");
        List<String> tcnames = this.getTrainerClassNames();
        for (int i = 1; i < amount; i++) {
            // Trainer entries are 40 bytes
            // Team flags; 1 byte; 0x01 = custom moves, 0x02 = held item
            // Class; 1 byte
            // Encounter Music and gender; 1 byte
            // Battle Sprite; 1 byte
            // Name; 12 bytes; 0xff terminated
            // Items; 2 bytes each, 4 item slots
            // Battle Mode; 1 byte; 0 means single, 1 means double.
            // 3 bytes not used
            // AI Flags; 1 byte
            // 3 bytes not used
            // Number of pokemon in team; 1 byte
            // 3 bytes not used
            // Pointer to pokemon; 4 bytes
            // https://github.com/pret/pokefirered/blob/3dce3407d5f9bca69d61b1cf1b314fb1e921d572/include/battle.h#L111
            int trOffset = baseOffset + i * entryLen;
            Trainer tr = new Trainer();
            tr.setOffset(trOffset);
            tr.setIndex(i);
            int trainerclass = rom[trOffset + 1] & 0xFF;
            tr.setTrainerclass((rom[trOffset + 2] & 0x80) > 0 ? 1 : 0);

            int pokeDataType = rom[trOffset] & 0xFF;
            if (rom[trOffset + (entryLen - 16)] == 0x01) {
                tr.getCurrBattleStyle().setStyle(BattleStyle.Style.DOUBLE_BATTLE);
            }
            int numPokes = rom[trOffset + (entryLen - 8)] & 0xFF;
            int pointerToPokes = readPointer(trOffset + (entryLen - 4));
            tr.setPoketype(pokeDataType);
            tr.setName(this.readVariableLengthString(trOffset + 4));
            tr.setFullDisplayName(tcnames.get(trainerclass) + " " + tr.getName());
            // Pokemon structure data is like
            // IV IV LV SP SP
            // (HI HI)
            // (M1 M1 M2 M2 M3 M3 M4 M4)
            // IV is a "difficulty" level between 0 and 255 to represent 0 to 31 IVs.
            //     These IVs affect all attributes. For the vanilla games, the majority
            //     of trainers have 0 IVs; Elite Four members will have 31 IVs.
            // https://github.com/pret/pokeemerald/blob/6c38837b266c0dd36ccdd04559199282daa7a8a0/include/data.h#L22
            if (pokeDataType == 0) {
                // blocks of 8 bytes
                for (int poke = 0; poke < numPokes; poke++) {
                    TrainerPokemon thisPoke = new TrainerPokemon();
                    thisPoke.setIVs(((readWord(pointerToPokes + poke * 8) & 0xFF) * 31) / 255);
                    thisPoke.setLevel(readWord(pointerToPokes + poke * 8 + 2));
                    thisPoke.setSpecies(pokesInternal[readWord(pointerToPokes + poke * 8 + 4)]);
                    // In Gen 3, Trainer Pokemon *always* use the first Ability, no matter what
                    thisPoke.setAbilitySlot(1);
                    tr.getPokemon().add(thisPoke);
                }
            } else if (pokeDataType == 2) {
                // blocks of 8 bytes
                for (int poke = 0; poke < numPokes; poke++) {
                    TrainerPokemon thisPoke = new TrainerPokemon();
                    thisPoke.setIVs(((readWord(pointerToPokes + poke * 8) & 0xFF) * 31) / 255);
                    thisPoke.setLevel(readWord(pointerToPokes + poke * 8 + 2));
                    thisPoke.setSpecies(pokesInternal[readWord(pointerToPokes + poke * 8 + 4)]);
                    int itemID = Gen3Constants.itemIDToStandard(readWord(pointerToPokes + poke * 8 + 6));
                    thisPoke.setHeldItem(items.get(itemID));
                    thisPoke.setAbilitySlot(1);
                    tr.getPokemon().add(thisPoke);
                }
            } else if (pokeDataType == 1) {
                // blocks of 16 bytes
                for (int poke = 0; poke < numPokes; poke++) {
                    TrainerPokemon thisPoke = new TrainerPokemon();
                    thisPoke.setIVs(((readWord(pointerToPokes + poke * 16) & 0xFF) * 31) / 255);
                    thisPoke.setLevel(readWord(pointerToPokes + poke * 16 + 2));
                    thisPoke.setSpecies(pokesInternal[readWord(pointerToPokes + poke * 16 + 4)]);
                    for (int move = 0; move < 4; move++) {
                        thisPoke.getMoves()[move] = readWord(pointerToPokes + poke * 16 + 6 + (move*2));
                    }
                    thisPoke.setAbilitySlot(1);
                    tr.getPokemon().add(thisPoke);
                }
            } else if (pokeDataType == 3) {
                // blocks of 16 bytes
                for (int poke = 0; poke < numPokes; poke++) {
                    TrainerPokemon thisPoke = new TrainerPokemon();
                    thisPoke.setIVs(((readWord(pointerToPokes + poke * 16) & 0xFF) * 31) / 255);
                    thisPoke.setLevel(readWord(pointerToPokes + poke * 16 + 2));
                    thisPoke.setSpecies(pokesInternal[readWord(pointerToPokes + poke * 16 + 4)]);
                    int itemID = Gen3Constants.itemIDToStandard(readWord(pointerToPokes + poke * 16 + 6));
                    thisPoke.setHeldItem(items.get(itemID));
                    for (int move = 0; move < 4; move++) {
                        thisPoke.getMoves()[move] = readWord(pointerToPokes + poke * 16 + 8 + (move*2));
                    }
                    thisPoke.setAbilitySlot(1);
                    tr.getPokemon().add(thisPoke);
                }
            }
            trainers.add(tr);
        }

        if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            readMossdeepStevenTrainer();
        }

        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            Gen3Constants.trainerTagsRS(trainers, romEntry.getRomType());
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            Gen3Constants.trainerTagsE(trainers);
            Gen3Constants.setMultiBattleStatusEm(trainers);
        } else {
            Gen3Constants.trainerTagsFRLG(trainers);
        }
    }

    /**
     * Reads the Mossdeep Steven battle/{@link Trainer} from ROM and adds it to the end of the trainers list.
     */
	private void readMossdeepStevenTrainer() {
		int mossdeepStevenOffset = romEntry.getIntValue("MossdeepStevenTeamOffset");
		Trainer mossdeepSteven = new Trainer();
		mossdeepSteven.setOffset(mossdeepStevenOffset);
		mossdeepSteven.setIndex(trainers.size() + 1);
		mossdeepSteven.setPoketype(1); // Custom moves, but no held items

		// This is literally how the game does it too, lol. Have to subtract one because
		// the trainers internally are one-indexed, but then trainers is zero-indexed.
		Trainer meteorFallsSteven = trainers.get(Gen3Constants.emMeteorFallsStevenIndex - 1);
		mossdeepSteven.setTrainerclass(meteorFallsSteven.getTrainerclass());
		mossdeepSteven.setName(meteorFallsSteven.getName());
		mossdeepSteven.setFullDisplayName(meteorFallsSteven.getFullDisplayName());

		for (int i = 0; i < 3; i++) {
			int currentOffset = mossdeepStevenOffset + (i * 20);
			TrainerPokemon tp = new TrainerPokemon();
			tp.setSpecies(pokesInternal[readWord(currentOffset)]);
			tp.setIVs(rom[currentOffset + 2]);
			tp.setLevel(rom[currentOffset + 3]);
			for (int move = 0; move < 4; move++) {
				tp.getMoves()[move] = readWord(currentOffset + 12 + (move * 2));
			}
            tp.setAbilitySlot(1);
			mossdeepSteven.getPokemon().add(tp);
		}

		trainers.add(mossdeepSteven);
	}

    @Override
    public Set<Item> getEvolutionItems() {
        return itemIdsToSet(Gen3Constants.evolutionItems);
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        return new ArrayList<>(); // TODO: Not implemented
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        return Arrays.stream(romEntry.getArrayValue("EliteFourIndices")).boxed().collect(Collectors.toList());
    }

	@Override
	public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
	}

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        if(romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            return Gen3Constants.gymAndEliteThemesFRLG;
        } else if(romEntry.getRomType() == Gen3Constants.RomType_Em) {
            return Gen3Constants.gymAndEliteThemesEm;
        } else {
            return Gen3Constants.gymAndEliteThemesRS;
        }
    }

    @Override
    public void saveTrainers() {
        if (trainers == null) {
            throw new IllegalStateException("Trainers are not loaded");
        }

        int baseOffset = romEntry.getIntValue("TrainerData");
        int amount = romEntry.getIntValue("TrainerCount");
        int entryLen = romEntry.getIntValue("TrainerEntrySize");
        int nameLen = romEntry.getIntValue("TrainerNameLength");
        Iterator<Trainer> trainerIterator = trainers.iterator();

        for (int i = 1; i < amount; i++) {
            int trOffset = baseOffset + i * entryLen;
            Trainer tr = trainerIterator.next();

            // When rewriting the Pokémon data (in particular the pointer),
            // it needs to use parts of the old trainer data - thus those are overwritten
            // after
            int pokemonPointerOffset = trOffset + (entryLen - 4);
            new DataRewriter<Trainer>().rewriteData(pokemonPointerOffset, tr, this::trainerPokemonToBytes,
                    (oldDataOffset) -> readTrainerPokemonDataLength(trOffset));

            writeByte(trOffset, (byte) tr.getPoketype());
            writeFixedLengthString(tr.getName(), trOffset + 4, nameLen);
            writeByte(trOffset + (entryLen - 8), (byte) tr.getPokemon().size());
            if (tr.isForcedDoubleBattle()) {
                if (tr.getCurrBattleStyle().getStyle() == BattleStyle.Style.DOUBLE_BATTLE)
                    writeByte(trOffset + (entryLen - 16), (byte) 0x01);
                else
                    writeByte(trOffset + (entryLen - 16), (byte) 0x00);
            }
        }

        if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            writeMossdeepStevenTrainer();
        }
    }

	private byte[] trainerPokemonToBytes(Trainer trainer) {
		int dataSize = trainer.getPokemon().size() * (trainer.pokemonHaveCustomMoves() ? 16 : 8);
		byte[] pokemonData = new byte[dataSize];

		// Get current movesets in case we need to reset them for certain
		// trainer mons.
		Map<Integer, List<MoveLearnt>> movesets = this.getMovesLearnt();

		if (trainer.pokemonHaveCustomMoves()) {
			// custom moves, blocks of 16 bytes
			for (int tpIndex = 0; tpIndex < trainer.getPokemon().size(); tpIndex++) {
				TrainerPokemon tp = trainer.getPokemon().get(tpIndex);
				// Add 1 to offset integer division truncation
				writeWord(pokemonData, tpIndex * 16, Math.min(255, 1 + (tp.getIVs() * 255) / 31));
				writeWord(pokemonData, tpIndex * 16 + 2, tp.getLevel());
				writeWord(pokemonData, tpIndex * 16 + 4, pokedexToInternal[tp.getSpecies().getNumber()]);
				int movesStart;
				if (trainer.pokemonHaveItems()) {
                    int itemInternalID = tp.getHeldItem() == null ? 0 :
                            Gen3Constants.itemIDToInternal(tp.getHeldItem().getId());
                    writeWord(pokemonData, tpIndex * 16 + 6, itemInternalID);
					movesStart = 8;
				} else {
					movesStart = 6;
					writeWord(pokemonData, tpIndex * 16 + 14, 0);
				}
				if (tp.isResetMoves()) {
					int[] pokeMoves = getMovesAtLevel(tp.getSpecies().getNumber(), movesets, tp.getLevel());
					for (int m = 0; m < 4; m++) {
						writeWord(pokemonData, tpIndex * 16 + movesStart + m * 2, pokeMoves[m]);
					}
				} else {
					writeWord(pokemonData, tpIndex * 16 + movesStart, tp.getMoves()[0]);
					writeWord(pokemonData, tpIndex * 16 + movesStart + 2, tp.getMoves()[1]);
					writeWord(pokemonData, tpIndex * 16 + movesStart + 4, tp.getMoves()[2]);
					writeWord(pokemonData, tpIndex * 16 + movesStart + 6, tp.getMoves()[3]);
				}
			}
		} else {
			// no moves, blocks of 8 bytes
			for (int tpIndex = 0; tpIndex < trainer.getPokemon().size(); tpIndex++) {
				TrainerPokemon tp = trainer.getPokemon().get(tpIndex);
				writeWord(pokemonData, tpIndex * 8, Math.min(255, 1 + (tp.getIVs() * 255) / 31));
				writeWord(pokemonData, tpIndex * 8 + 2, tp.getLevel());
				writeWord(pokemonData, tpIndex * 8 + 4, pokedexToInternal[tp.getSpecies().getNumber()]);
                int itemInternalID = !trainer.pokemonHaveItems() || tp.getHeldItem() == null ? 0
                        : Gen3Constants.itemIDToInternal(tp.getHeldItem().getId());
                writeWord(pokemonData, tpIndex * 8 + 6, itemInternalID);
			}
		}

		return pokemonData;
	}

	private int readTrainerPokemonDataLength(int trainerOffset) {
		int entryLen = romEntry.getIntValue("TrainerEntrySize");

		int pokeType = rom[trainerOffset] & 0xFF;
		int pokeCount = rom[trainerOffset + (entryLen - 8)] & 0xFF;
		return pokeCount * ((pokeType & 1) == 1 ? 16 : 8);
	}

    /**
     * Writes the Mossdeep Steven battle/{@link Trainer} to ROM. Assumes this is always the last trainer in
     * trainers list.
     */
    private void writeMossdeepStevenTrainer() {
        // The Mossdeep Steven trainer is special because it is *not* really a trainer to the game, just Pokemon data.
        // The randomizer surrounds this data with a Trainer object so it can be randomized.
		int mossdeepStevenOffset = romEntry.getIntValue("MossdeepStevenTeamOffset");
		Trainer mossdeepSteven = trainers.get(trainers.size() - 1);

        // The below code *could* be implemented using trainerPokemonToBytes(mossdeepSteven), but then extra
        // precautions would need to be taken so the mossdeepSteven Trainer's properties aren't changed.
        // Adding an extra Pokémon is normally fine with Trainers; but it would cause corruption with mossdeepSteven.
        // ...thus the custom implementation below.
		for (int i = 0; i < 3; i++) {
			int currentOffset = mossdeepStevenOffset + (i * 20);
			TrainerPokemon tp = mossdeepSteven.getPokemon().get(i);
			writeWord(currentOffset, pokedexToInternal[tp.getSpecies().getNumber()]);
			writeByte(currentOffset + 2, (byte) tp.getIVs());
			writeByte(currentOffset + 3, (byte) tp.getLevel());
			for (int move = 0; move < 4; move++) {
				writeWord(currentOffset + 12 + (move * 2), tp.getMoves()[move]);
			}
		}
	}

    @Override
    public List<Species> getSpecies() {
        return speciesList;
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        return speciesList; // No alt formes for now, should include Deoxys formes in the future
    }

    @Override
    public SpeciesSet getAltFormes() {
        return new SpeciesSet();
    }

    @Override
    public List<MegaEvolution> getMegaEvolutions() {
        return new ArrayList<>();
    }

    @Override
    public Species getAltFormeOfSpecies(Species base, int forme) {
        return base;
    }

    @Override
    public SpeciesSet getIrregularFormes() {
        return new SpeciesSet();
    }

    @Override
    public boolean hasFunctionalFormes() {
        return false;
    }

    @Override
	public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
		Map<Integer, List<MoveLearnt>> movesets = new TreeMap<>();
		int baseOffset = romEntry.getIntValue("PokemonMovesets");
		for (int i = 1; i <= numRealPokemon; i++) {
			Species pk = speciesList.get(i);
			int pointerOffset = baseOffset + (pokedexToInternal[pk.getNumber()]) * 4;
			int movesLearntOffset = readPointer(pointerOffset);
			List<MoveLearnt> moves = readMovesLearnt(movesLearntOffset);
			movesets.put(pk.getNumber(), moves);
		}
		return movesets;
	}

	private List<MoveLearnt> readMovesLearnt(int offset) {
		List<MoveLearnt> moves = new ArrayList<>();
		if (jamboMovesetHack) {
			while ((rom[offset] & 0xFF) != 0x00 || (rom[offset + 1] & 0xFF) != 0x00
					|| (rom[offset + 2] & 0xFF) != 0xFF) {
                int move = readWord(offset);
                int level = rom[offset + 2] & 0xFF;
				moves.add(new MoveLearnt(move, level));
				offset += 3;
			}
		} else {
			while ((rom[offset] & 0xFF) != 0xFF || (rom[offset + 1] & 0xFF) != 0xFF) {
				int move = (rom[offset] & 0xFF);
				int level = (rom[offset + 1] & 0xFE) >> 1;
				if ((rom[offset + 1] & 0x01) == 0x01) {
					move += 0x100;
				}
				moves.add(new MoveLearnt(move, level));
				offset += 2;
			}
		}
		return moves;
	}

	@Override
	public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {
		int baseOffset = romEntry.getIntValue("PokemonMovesets");
		for (int i = 1; i <= numRealPokemon; i++) {
			Species pk = speciesList.get(i);
			int pointerOffset = baseOffset + (pokedexToInternal[pk.getNumber()]) * 4;
			List<MoveLearnt> moves = movesets.get(pk.getNumber());
			new DataRewriter<List<MoveLearnt>>().rewriteData(pointerOffset, moves, this::movesLearntToBytes,
					this::lengthOfMovesLearntAt);
		}
	}

	private byte[] movesLearntToBytes(List<MoveLearnt> movesLearnt) {
		int entrySize = jamboMovesetHack ? 3 : 2;
		byte[] terminator = jamboMovesetHack ? Gen3Constants.jamboMovesLearntTerminator
				: Gen3Constants.vanillaMovesLearntTerminator;

		int bytesNeeded = entrySize * movesLearnt.size() + terminator.length;
		byte[] bytes = new byte[bytesNeeded];

		for (int i = 0; i < movesLearnt.size(); i++) {
			writeMoveLearnt(bytes, i * entrySize, movesLearnt.get(i));
		}
		writeBytes(bytes, bytesNeeded - terminator.length, terminator);

		return bytes;
	}

	private void writeMoveLearnt(byte[] data, int offset, MoveLearnt ml) {
		if (jamboMovesetHack) {
			writeWord(data, offset, ml.move);
			data[offset + 2] = (byte) ml.level;
		} else {
			data[offset] = (byte) (ml.move & 0xFF);
			int levelPart = (ml.level << 1) & 0xFE;
			if (ml.move > 255) {
				levelPart++;
			}
			data[offset + 1] = (byte) levelPart;
		}
	}

	/**
	 * Reads the length of a MoveLearnt-s entry in bytes, including the terminator
	 * bytes.
	 */
	private int lengthOfMovesLearntAt(int offset) {
		List<MoveLearnt> moves = readMovesLearnt(offset);
		return movesLearntToBytes(moves).length;
	}

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        Map<Integer, List<Integer>> eggMoves = new TreeMap<>();
        int offset = romEntry.getIntValue("EggMoves");
        int currentSpecies = 0;
        List<Integer> currentMoves = new ArrayList<>();
        int val = FileFunctions.read2ByteInt(rom, offset);

        // Check egg_moves.h in the Gen 3 decomps for more info on how this algorithm works.
        while (val != 0xFFFF) {
            if (val > 20000) {
                int species = val - 20000;
                if (!currentMoves.isEmpty()) {
                    eggMoves.put(internalToPokedex[currentSpecies], currentMoves);
                }
                currentSpecies = species;
                currentMoves = new ArrayList<>();
            } else {
                currentMoves.add(val);
            }
            offset += 2;
            val = FileFunctions.read2ByteInt(rom, offset);
        }

        // Need to make sure the last entry gets recorded too
        if (!currentMoves.isEmpty()) {
            eggMoves.put(internalToPokedex[currentSpecies], currentMoves);
        }
        return eggMoves;
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        int offset = romEntry.getIntValue("EggMoves");
        for (int species : eggMoves.keySet()) {
            FileFunctions.write2ByteInt(rom, offset, pokedexToInternal[species] + 20000);
            offset += 2;
            for (int move : eggMoves.get(species)) {
                FileFunctions.write2ByteInt(rom, offset, move);
                offset += 2;
            }
        }
    }

    public static class StaticPokemon {
        private final int[] speciesOffsets;
        private final int[] levelOffsets;

        public StaticPokemon(int[] speciesOffsets, int[] levelOffsets) {
            this.speciesOffsets = speciesOffsets;
            this.levelOffsets = levelOffsets;
        }

        public Species getPokemon(Gen3RomHandler parent) {
            return parent.pokesInternal[parent.readWord(speciesOffsets[0])];
        }

        public void setPokemon(Gen3RomHandler parent, Species pkmn) {
            int value = parent.pokedexToInternal[pkmn.getNumber()];
            for (int offset : speciesOffsets) {
                parent.writeWord(offset, value);
            }
        }

        public int getLevel(byte[] rom, int i) {
            if (levelOffsets.length <= i) {
                return 1;
            }
            return rom[levelOffsets[i]];
        }

        public void setLevel(byte[] rom, int level, int i) {
            if (levelOffsets.length > i) { // Might not have a level entry e.g., it's an egg
                rom[levelOffsets[i]] = (byte) level;
            }
        }
    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        List<StaticEncounter> statics = new ArrayList<>();
        List<StaticPokemon> staticsHere = romEntry.getStaticPokemon();
        int[] staticEggOffsets = romEntry.getArrayValue("StaticEggPokemonOffsets");
        for (int i = 0; i < staticsHere.size(); i++) {
            int currentOffset = i;
            StaticPokemon staticPK = staticsHere.get(i);
            StaticEncounter se = new StaticEncounter();
            se.setSpecies(staticPK.getPokemon(this));
            se.setLevel(staticPK.getLevel(rom, 0));
            se.setEgg(Arrays.stream(staticEggOffsets).anyMatch(x-> x == currentOffset));
            statics.add(se);
        }

        if (romEntry.hasTweakFile("StaticFirstBattleTweak")) {
            // Read in and randomize the static starting Poochyena/Zigzagoon fight in RSE
            int startingSpeciesOffset = romEntry.getIntValue("StaticFirstBattleSpeciesOffset");
            int species = readWord(startingSpeciesOffset);
            if (species == 0xFFFF) {
                // Patch hasn't been applied, so apply it first
                try {
                    FileFunctions.applyPatch(rom, romEntry.getTweakFile("StaticFirstBattleTweak"));
                    species = readWord(startingSpeciesOffset);
                } catch (IOException e) {
                    throw new RomIOException(e);
                }
            }
            Species pkmn = pokesInternal[species];
            int startingLevelOffset = romEntry.getIntValue("StaticFirstBattleLevelOffset");
            int level = rom[startingLevelOffset];
            StaticEncounter se = new StaticEncounter();
            se.setSpecies(pkmn);
            se.setLevel(level);
            statics.add(se);
        } else if (romEntry.hasTweakFile("GhostMarowakTweak")) {
            // Read in and randomize the static Ghost Marowak fight in FRLG
            int[] ghostMarowakOffsets = romEntry.getArrayValue("GhostMarowakSpeciesOffsets");
            int species = readWord(ghostMarowakOffsets[0]);
            if (species == 0xFFFF) {
                // Patch hasn't been applied, so apply it first
                try {
                    FileFunctions.applyPatch(rom, romEntry.getTweakFile("GhostMarowakTweak"));
                    species = readWord(ghostMarowakOffsets[0]);
                } catch (IOException e) {
                    throw new RomIOException(e);
                }
            }
            Species pkmn = pokesInternal[species];
            int[] startingLevelOffsets = romEntry.getArrayValue("GhostMarowakLevelOffsets");
            int level = rom[startingLevelOffsets[0]];
            StaticEncounter se = new StaticEncounter();
            se.setSpecies(pkmn);
            se.setLevel(level);
            statics.add(se);
        }

        try {
            getRoamers(statics);
        } catch (Exception e) {
            throw new RomIOException(e);
        }

        return statics;
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        // Support Deoxys/Mew gifts/catches in E/FR/LG
        attemptObedienceEvolutionPatches();

        List<StaticPokemon> staticsHere = romEntry.getStaticPokemon();
        int roamerSize = romEntry.getRoamingPokemon().size();
        if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            // Emerald roamers are set as linkedEncounters to their respective
            // Southern Island statics and thus don't count.
            roamerSize = 0;
        }
        int hardcodedStaticSize = 0;
        if (romEntry.hasTweakFile("StaticFirstBattleTweak") || romEntry.hasTweakFile("GhostMarowakTweak")) {
            hardcodedStaticSize = 1;
        }

        if (staticPokemon.size() != staticsHere.size() + hardcodedStaticSize + roamerSize) {
            return false;
        }

        for (int i = 0; i < staticsHere.size(); i++) {
            staticsHere.get(i).setPokemon(this, staticPokemon.get(i).getSpecies());
            staticsHere.get(i).setLevel(rom, staticPokemon.get(i).getLevel(), 0);
        }

        if (romEntry.hasTweakFile("StaticFirstBattleTweak")) {
            StaticEncounter startingFirstBattle = staticPokemon.get(romEntry.getIntValue("StaticFirstBattleOffset"));
            int startingSpeciesOffset = romEntry.getIntValue("StaticFirstBattleSpeciesOffset");
            writeWord(startingSpeciesOffset, pokedexToInternal[startingFirstBattle.getSpecies().getNumber()]);
            int startingLevelOffset = romEntry.getIntValue("StaticFirstBattleLevelOffset");
            writeByte(startingLevelOffset, (byte) startingFirstBattle.getLevel());
        } else if (romEntry.hasTweakFile("GhostMarowakTweak")) {
            StaticEncounter ghostMarowak = staticPokemon.get(romEntry.getIntValue("GhostMarowakOffset"));
            int[] ghostMarowakSpeciesOffsets = romEntry.getArrayValue("GhostMarowakSpeciesOffsets");
            for (int offset : ghostMarowakSpeciesOffsets) {
                writeWord(offset, pokedexToInternal[ghostMarowak.getSpecies().getNumber()]);
            }
            int[] ghostMarowakLevelOffsets = romEntry.getArrayValue("GhostMarowakLevelOffsets");
            for (int offset : ghostMarowakLevelOffsets) {
                writeByte(offset, (byte) ghostMarowak.getLevel());
            }

            // The code for creating Ghost Marowak tries to ensure the Pokemon is female. If the Pokemon
            // cannot be female (because they are always male or an indeterminate gender), then the game
            // will infinite loop trying and failing to make the Pokemon female. For Pokemon that cannot
            // be female, change the specified gender to something that actually works.
            int ghostMarowakGenderOffset = romEntry.getIntValue("GhostMarowakGenderOffset");
            if (ghostMarowak.getSpecies().getGenderRatio() == 0 || ghostMarowak.getSpecies().getGenderRatio() == 0xFF) {
                // 0x00 is 100% male, and 0xFF is indeterminate gender
                writeByte(ghostMarowakGenderOffset, (byte) ghostMarowak.getSpecies().getGenderRatio());
            }
        }

        setRoamers(staticPokemon);
        return true;
    }

    private void getRoamers(List<StaticEncounter> statics) throws IOException {
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby) {
            int firstSpecies = readWord(rom, romEntry.getRoamingPokemon().get(0).speciesOffsets[0]);
            if (firstSpecies == 0) {
                // Before applying the patch, the first species offset will be pointing to
                // the lower bytes of 0x2000000, so when it reads a word, it will be 0.
                applyRubyRoamerPatch();
            }
            StaticPokemon roamer = romEntry.getRoamingPokemon().get(0);
            StaticEncounter se = new StaticEncounter();
            se.setSpecies(roamer.getPokemon(this));
            se.setLevel(roamer.getLevel(rom, 0));
            statics.add(se);
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            StaticPokemon roamer = romEntry.getRoamingPokemon().get(0);
            StaticEncounter se = new StaticEncounter();
            se.setSpecies(roamer.getPokemon(this));
            se.setLevel(roamer.getLevel(rom, 0));
            statics.add(se);
        } else if (romEntry.getRomType() == Gen3Constants.RomType_FRLG && romEntry.hasTweakFile("RoamingPokemonTweak")) {
            int firstSpecies = readWord(rom, romEntry.getRoamingPokemon().get(0).speciesOffsets[0]);
            if (firstSpecies == 0xFFFF) {
                // This means that the IPS patch hasn't been applied yet, since the first species
                // ID location is free space.
                FileFunctions.applyPatch(rom, romEntry.getTweakFile("RoamingPokemonTweak"));
            }
            for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                StaticPokemon roamer = romEntry.getRoamingPokemon().get(i);
                StaticEncounter se = new StaticEncounter();
                se.setSpecies(roamer.getPokemon(this));
                se.setLevel(roamer.getLevel(rom, 0));
                statics.add(se);
            }
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            int firstSpecies = readWord(rom, romEntry.getRoamingPokemon().get(0).speciesOffsets[0]);
            if (firstSpecies >= pokesInternal.length) {
                // Before applying the patch, the first species offset is a pointer with a huge value.
                // Thus, this check is a good indicator that the patch needs to be applied.
                applyEmeraldRoamerPatch();
            }
            int[] southernIslandOffsets = romEntry.getArrayValue("StaticSouthernIslandOffsets");
            for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                StaticPokemon roamer = romEntry.getRoamingPokemon().get(i);
                StaticEncounter se = new StaticEncounter();
                se.setSpecies(roamer.getPokemon(this));
                se.setLevel(roamer.getLevel(rom, 0));

                // Link each roamer to their respective Southern Island static encounter so that
                // they randomize to the same species.
                StaticEncounter southernIslandEncounter = statics.get(southernIslandOffsets[i]);
                southernIslandEncounter.getLinkedEncounters().add(se);
            }
        }
    }

    private void setRoamers(List<StaticEncounter> statics) {
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            StaticEncounter roamerEncounter = statics.get(statics.size() - 1);
            StaticPokemon roamer = romEntry.getRoamingPokemon().get(0);
            roamer.setPokemon(this, roamerEncounter.getSpecies());
            for (int i = 0; i < roamer.levelOffsets.length; i++) {
                roamer.setLevel(rom, roamerEncounter.getLevel(), i);
            }
        } else if (romEntry.getRomType() == Gen3Constants.RomType_FRLG && romEntry.hasTweakFile("RoamingPokemonTweak")) {
            for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                int offsetInStaticList = statics.size() - 3 + i;
                StaticEncounter roamerEncounter = statics.get(offsetInStaticList);
                StaticPokemon roamer = romEntry.getRoamingPokemon().get(i);
                roamer.setPokemon(this, roamerEncounter.getSpecies());
                for (int j = 0; j < roamer.levelOffsets.length; j++) {
                    roamer.setLevel(rom, roamerEncounter.getLevel(), j);
                }
            }
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            int[] southernIslandOffsets = romEntry.getArrayValue("StaticSouthernIslandOffsets");
            for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                StaticEncounter southernIslandEncounter = statics.get(southernIslandOffsets[i]);
                StaticEncounter roamerEncounter = southernIslandEncounter.getLinkedEncounters().get(0);
                StaticPokemon roamer = romEntry.getRoamingPokemon().get(i);
                roamer.setPokemon(this, roamerEncounter.getSpecies());
                for (int j = 0; j < roamer.levelOffsets.length; j++) {
                    roamer.setLevel(rom, roamerEncounter.getLevel(), j);
                }
            }
        }
    }

    private void applyRubyRoamerPatch() {
        int offset = romEntry.getIntValue("FindMapsWithMonFunctionStartOffset");

        // The constant 0x2000000 is actually in the function twice, so we'll replace the first instance
        // with Latios's ID. First, change the "ldr r2, [pc, #0x68]" near the start of the function to
        // "ldr r2, [pc, #0x15C]" so it points to the second usage of 0x2000000
        writeByte(offset + 22, (byte) 0x57);

        // In the space formerly occupied by the first 0x2000000, write Latios's ID
        FileFunctions.writeFullInt(rom, offset + 128, pokedexToInternal[SpeciesIDs.latios]);

        // Where the original function computes Latios's ID by setting r0 to 0xCC << 1, just pc-relative
        // load our constant. We have four bytes of space to play with, and we need to make sure the offset
        // from the pc is 4-byte aligned; we need to nop for alignment and then perform the load.
        writeBytes(offset + 12, new byte[] { 0x00, 0x00, 0x1C, 0x48 });

        offset = romEntry.getIntValue("CreateInitialRoamerMonFunctionStartOffset");

        // At the very end of the function, the game pops the lr from the stack and stores it in r0, then
        // it does "bx r0" to jump back to the caller, and then it has two bytes of padding afterwards. For
        // some reason, Ruby very rarely does "pop { pc }" even though that seemingly works fine. By doing
        // that, we only need one instruction to return to the caller, giving us four bytes to write
        // Latios's species ID.
        writeBytes(offset + 182, new byte[] { 0x00, (byte) 0xBD});
        FileFunctions.writeFullInt(rom, offset + 184, pokedexToInternal[SpeciesIDs.latios]);

        // Now write a pc-relative load to this new species ID constant over the original move and lsl. Similar
        // to before, we need to write a nop first for alignment, then pc-relative load into r6.
        writeBytes(offset + 10, new byte[] { 0x00, 0x00, 0x2A, 0x4E });
    }

    private void applyEmeraldRoamerPatch() {
        int offset = romEntry.getIntValue("CreateInitialRoamerMonFunctionStartOffset");

        // Latias's species ID is already a pc-relative loaded constant, but Latios's isn't. We need to make
        // some room for it; the constant 0x03005D8C is actually in the function twice, so we'll replace the first
        // instance with Latios's ID. First, change the "ldr r0, [pc, #0xC]" at the start of the function to
        // "ldr r0, [pc, #0x104]", so it points to the second usage of 0x03005D8C
        writeByte(offset + 14, (byte) 0x41);

        // In the space formerly occupied by the first 0x03005D8C, write Latios's ID
        FileFunctions.writeFullInt(rom, offset + 28, pokedexToInternal[SpeciesIDs.latios]);

        // In the original function, we "lsl r0, r0, #0x10" then compare r0 to 0. The thing is, this left
        // shift doesn't actually matter, because 0 << 0x10 = 0, and [non-zero] << 0x10 = [non-zero].
        // Let's move the compare up to take its place and then load Latios's ID into r3 for use in another
        // branch later.
        writeBytes(offset + 8, new byte[] { 0x00, 0x28, 0x04, 0x4B });

        // Lastly, in the branch that normally does r2 = 0xCC << 0x1 to compute Latios's ID, just mov r3
        // into r2, since it was loaded with his ID with the above code.
        writeBytes(offset + 48, new byte[] { 0x1A, 0x46, 0x00, 0x00 });
    }

    @Override
    public List<Integer> getTMMoves() {
        List<Integer> tms = new ArrayList<>();
        int offset = romEntry.getIntValue("TmMoves");
        for (int i = 1; i <= Gen3Constants.tmCount; i++) {
            tms.add(readWord(offset + (i - 1) * 2));
        }
        return tms;
    }

    @Override
    public List<Integer> getHMMoves() {
        return Gen3Constants.hmMoves;
    }

	@Override
	public void setTMMoves(List<Integer> moveIndexes) {
		if (!mapLoadingDone) {
			preprocessMaps();
			mapLoadingDone = true;
		}
		writeTMMoves(moveIndexes);
		writeTMItemPalettes(moveIndexes);
		writeTMItemText(moveIndexes);
		writeTMText(moveIndexes);
	}

	private void writeTMMoves(List<Integer> moveIndexes) {
		int offset = romEntry.getIntValue("TmMoves");
		for (int i = 1; i <= Gen3Constants.tmCount; i++) {
			writeWord(offset + (i - 1) * 2, moveIndexes.get(i - 1));
		}
		int otherOffset = romEntry.getIntValue("TmMovesDuplicate");
		if (otherOffset > 0) {
			// Emerald/FR/LG have *two* TM tables
			System.arraycopy(rom, offset, rom, otherOffset, Gen3Constants.tmCount * 2);
		}
	}

	private void writeTMItemPalettes(List<Integer> moveIndexes) {
		int iiOffset = romEntry.getIntValue("ItemImages");
		if (iiOffset > 0) {
			int[] pals = romEntry.getArrayValue("TmPals");
			// Update the item image palettes
			// Gen3 TMs are 289-338
			for (int i = 0; i < Gen3Constants.tmCount; i++) {
				Move mv = moves[moveIndexes.get(i)];
				int typeID = Gen3Constants.typeToByte(mv.type);
                int itemID = Gen3Constants.itemIDToInternal(ItemIDs.tm01 + i);
				writePointer(iiOffset + itemID * 8 + 4, pals[typeID]);
			}
		}
	}

	private void writeTMItemText(List<Integer> moveIndexes) {
		// Item descriptions
		if (romEntry.getIntValue("MoveDescriptions") > 0) {
			// JP blocked for now - uses different item structure anyway
			int idOffset = romEntry.getIntValue("ItemData");
			int mdOffset = romEntry.getIntValue("MoveDescriptions");
			int entrySize = romEntry.getIntValue("ItemEntrySize");
			int limitPerLine = (romEntry.getRomType() == Gen3Constants.RomType_FRLG) ? Gen3Constants.frlgItemDescCharsPerLine
					: Gen3Constants.rseItemDescCharsPerLine;
			for (int i = 0; i < Gen3Constants.tmCount; i++) {
				int itemBaseOffset = idOffset + Gen3Constants.itemIDToInternal(ItemIDs.tm01 + i) * entrySize;
				int moveBaseOffset = mdOffset + (moveIndexes.get(i) - 1) * 4;
				int moveTextPointer = readPointer(moveBaseOffset);
				String moveDesc = readVariableLengthString(moveTextPointer);
				String newItemDesc = RomFunctions.rewriteDescriptionForNewLineSize(moveDesc, "\\n", limitPerLine, ssd);

				int itemDescPointerOffset = itemBaseOffset + Gen3Constants.itemDataDescriptionOffset;
				try {
					rewriteVariableLengthString(itemDescPointerOffset, newItemDesc);
				} catch (RomIOException e) {
                    // This used to be a simple logging, turned it into a full error because I don't *think* it
                    // should be too common? Plus the RomHandler arguably should not do logging.
					throw new RomIOException("Couldn't insert new item description. " + e.getMessage());
				}
			}
		}
    }

	private int[] searchForPointerCopies(int pointerOffset) {
		// Somewhat foolhardy, since other data around *could* coincidentally be
		// identical to the pointer, and would then be erroneously overwritten.
		byte[] searchNeedle = new byte[4];
		System.arraycopy(rom, pointerOffset, searchNeedle, 0, 4);
		// find copies within pointerSearchRadius bytes either way of actualOffset
		int minOffset = Math.max(0, pointerOffset - Gen3Constants.pointerSearchRadius);
		int maxOffset = Math.min(rom.length, pointerOffset + Gen3Constants.pointerSearchRadius);
		return RomFunctions.search(rom, minOffset, maxOffset, searchNeedle).stream().mapToInt(i -> i).toArray();
	}

    private RomFunctions.StringSizeDeterminer ssd = encodedText -> translateString(encodedText).length;

    @Override
    public int getTMCount() {
        return Gen3Constants.tmCount;
    }

    @Override
    public int getHMCount() {
        return Gen3Constants.hmCount;
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        Map<Species, boolean[]> compat = new TreeMap<>();
        int offset = romEntry.getIntValue("PokemonTMHMCompat");
        for (int i = 1; i <= numRealPokemon; i++) {
            Species pkmn = speciesList.get(i);
            int compatOffset = offset + (pokedexToInternal[pkmn.getNumber()]) * 8;
            boolean[] flags = new boolean[Gen3Constants.tmCount + Gen3Constants.hmCount + 1];
            for (int j = 0; j < 8; j++) {
                readByteIntoFlags(flags, j * 8 + 1, compatOffset + j);
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        int offset = romEntry.getIntValue("PokemonTMHMCompat");
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int compatOffset = offset + (pokedexToInternal[pkmn.getNumber()]) * 8;
            for (int j = 0; j < 8; j++) {
                writeByte(compatOffset + j, getByteFromFlags(flags, j * 8 + 1));
            }
        }
    }

    @Override
    public boolean hasMoveTutors() {
        return (romEntry.getRomType() == Gen3Constants.RomType_Em || romEntry.getRomType() == Gen3Constants.RomType_FRLG);
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        if (!hasMoveTutors()) {
            return new ArrayList<>();
        }
        List<Integer> mts = new ArrayList<>();
        int moveCount = romEntry.getIntValue("MoveTutorMoves");
        int offset = romEntry.getIntValue("MoveTutorData");
        for (int i = 0; i < moveCount; i++) {
            mts.add(readWord(offset + i * 2));
        }
        return mts;
    }

	@Override
	public void setMoveTutorMoves(List<Integer> moveIndexes) {
		if (!hasMoveTutors()) {
			return;
		}
        writeMoveTutorMoves(moveIndexes);
        writeMoveTutorText(moveIndexes);
	}

    private void writeMoveTutorMoves(List<Integer> moveIndexes) {
        int moveCount = romEntry.getIntValue("MoveTutorMoves");
        int offset = romEntry.getIntValue("MoveTutorData");
        if (moveCount != moveIndexes.size()) {
            throw new IllegalArgumentException("Wrong amount of move tutor moves.");
        }
        for (int i = 0; i < moveCount; i++) {
            writeWord(offset + i * 2, moveIndexes.get(i));
        }
    }

    private void writeEventText(List<Gen3EventTextEntry> eventTextEntries, Function<Integer, String[]> idToReplacers,
                                String[] targets, String description) {
        for (Gen3EventTextEntry ete : eventTextEntries) {
            // create the new text
            Map<String, String> replacements = new HashMap<>();
            String[] replacers = idToReplacers.apply(ete.getID());
            for (int i = 0; i < targets.length; i++) {
                // to prevent them being line-broken
                String spaceless = replacers[i].replace(' ', '_');
                replacements.put(targets[i], spaceless);
            }
            String newText = RomFunctions.formatTextWithReplacements(ete.getTemplate(), replacements, "\\n",
                    "\\l", "\\p", Gen3Constants.regularTextboxCharsPerLine, ssd);
            // get rid of the underscores
            for (String replacer : replacers) {
                newText = newText.replace(replacer.replace('_', ' '), replacer);
            }

            int pointerOffset = ete.getActualPointerOffset();
            int[] secondaryPointerOffsets = searchForPointerCopies(pointerOffset);
            try {
                rewriteVariableLengthString(pointerOffset, newText, secondaryPointerOffsets);
            } catch (RomIOException e) {
                // This used to be a simple logging, turned it into a full error because I don't *think* it
                // should be too common? Plus the RomHandler arguably should not do logging.
                throw new RomIOException("Couldn't insert new " + description + " text. " + e.getMessage());
            }
        }
    }

    /**
     * Takes a {@link Gen3EventTextEntry}, and reads the text in ROM it points to, creating a new template and
     * thereby a new .ini file "entry". Should be run on an un-randomized ROM for the best effect.
     */
    @SuppressWarnings("unused")
    private String eventEntryToIniText(String prefix, Gen3EventTextEntry ete, Function<Integer, String[]> idToReplacers,
                                       String[] targets) {
        String template = readVariableLengthString(readPointer(ete.getActualPointerOffset()));
        template = template.replace("\\n", " ");
        for (int i = 0; i < targets.length; i++) {
            template = template.replace(idToReplacers.apply(ete.getID())[i], targets[i]);
        }
        return String.format("%s[]=[%d,%d,%d,%d,%s,%s]\n", prefix, ete.getID(), ete.getMapBank(), ete.getMapNumber(),
                ete.getPersonNum(), ete.relativePointerOffsetsToString(), template);
    }

    private void writeTMText(List<Integer> moveIndexes) {
        writeEventText(romEntry.getTMTexts(), id -> {
            int moveIndex = moveIndexes.get(id - 1);
            return new String[]{moves[moveIndex].name};
        }, new String[]{"[move]"}, "TM");
    }

    private void writeMoveTutorText(List<Integer> moveIndexes) {
        writeEventText(romEntry.getMoveTutorTexts(), id -> {
            int moveIndex = moveIndexes.get(id);
            return new String[]{moves[moveIndex].name};
        }, new String[]{"[move]"}, "MoveTutor");
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        if (!hasMoveTutors()) {
            return new TreeMap<>();
        }
        Map<Species, boolean[]> compat = new TreeMap<>();
        int moveCount = romEntry.getIntValue("MoveTutorMoves");
        int offset = romEntry.getIntValue("MoveTutorCompatibility");
        int bytesRequired = ((moveCount + 7) & ~7) / 8;
        for (int i = 1; i <= numRealPokemon; i++) {
            Species pkmn = speciesList.get(i);
            int compatOffset = offset + pokedexToInternal[pkmn.getNumber()] * bytesRequired;
            boolean[] flags = new boolean[moveCount + 1];
            for (int j = 0; j < bytesRequired; j++) {
                readByteIntoFlags(flags, j * 8 + 1, compatOffset + j);
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
        if (!hasMoveTutors()) {
            return;
        }
        int moveCount = romEntry.getIntValue("MoveTutorMoves");
        int offset = romEntry.getIntValue("MoveTutorCompatibility");
        int bytesRequired = ((moveCount + 7) & ~7) / 8;
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int compatOffset = offset + pokedexToInternal[pkmn.getNumber()] * bytesRequired;
            for (int j = 0; j < bytesRequired; j++) {
                writeByte(compatOffset + j, getByteFromFlags(flags, j * 8 + 1));
            }
        }
    }

    // For dynamic offsets later

    /**
     * Finds the offset of a hexstring which appears only once in the rom.<br>
     * <b>WARNING:</b> this method runs very slowly, at {@code O(rom.length)}. Consider using something else,
     * such as manual offsets in a {@link RomEntry}/.ini file.
     */
    private int find(String hexString) {
        return find(rom, hexString);
    }

    /**
     * Finds the offset of a hexstring which appears only once in the "haystack".<br>
     * <b>WARNING:</b> this method runs very slowly, at {@code O(haystack.length)}. Consider using something else,
     * such as manual offsets in a {@link RomEntry}/.ini file.
     */
    private static int find(byte[] haystack, String hexString) {
        if (hexString.length() % 2 != 0) {
            return -3; // error
        }
        byte[] searchFor = new byte[hexString.length() / 2];
        for (int i = 0; i < searchFor.length; i++) {
            searchFor[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        List<Integer> found = RomFunctions.search(haystack, searchFor);
        if (found.isEmpty()) {
            return -1; // not found
        } else if (found.size() > 1) {
            return -2; // not unique
        } else {
            return found.get(0);
        }
    }

    /**
     * Finds all offsets of a hexstring in the rom.<br>
     * <b>WARNING:</b> this method runs very slowly, at {@code O(rom.length)}. Consider using something else,
     * such as manual offsets in a {@link RomEntry}/.ini file.
     */
    private List<Integer> findMultiple(String hexString) {
        return findMultiple(rom, hexString);
    }

    /**
     * Finds all offsets of a hexstring in the "haystack".<br>
     * <b>WARNING:</b> this method runs very slowly, at {@code O(haystack.length)}. Consider using something else,
     * such as manual offsets in a {@link RomEntry}/.ini file.
     */
    private static List<Integer> findMultiple(byte[] haystack, String hexString) {
        if (hexString.length() % 2 != 0) {
            return new ArrayList<>(); // error
        }
        byte[] searchFor = new byte[hexString.length() / 2];
        for (int i = 0; i < searchFor.length; i++) {
            searchFor[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        return RomFunctions.search(haystack, searchFor);
    }

    private void writeHexString(String hexString, int offset) {
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("hexString must have an even number of characters");
        }
        writeBytes(offset, RomFunctions.hexToBytes(hexString));
    }

    private void attemptObedienceEvolutionPatches() {
        if (havePatchedObedience) {
            return;
        }

        havePatchedObedience = true;
        // This routine *appears* to only exist in E/FR/LG...
        // Look for the deoxys part which is
        // MOVS R1, 0x19A
        // CMP R0, R1
        // BEQ <mew/deoxys case>
        // Hex is CD214900 8842 0FD0
        int deoxysObOffset = find(Gen3Constants.deoxysObeyCode);
        if (deoxysObOffset > 0) {
            // We found the deoxys check...
            // Replacing it with MOVS R1, 0x0 would work fine.
            // This would make it so species 0x0 (glitch only) would disobey.
            // But MOVS R1, 0x0 (the version I know) is 2-byte
            // So we just use it twice...
            // the equivalent of nop'ing the second time.
            writeBytes(deoxysObOffset, new byte[] { 0x00, Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1,
                    0x00, Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1});
            // Look for the mew check too... it's 0x16 ahead
            if (readWord(deoxysObOffset + Gen3Constants.mewObeyOffsetFromDeoxysObey) == (((Gen3Constants.gbaCmpRxOpcode | Gen3Constants.gbaR0) << 8) | (SpeciesIDs.mew))) {
                // Bingo, thats CMP R0, 0x97
                // change to CMP R0, 0x0
                writeWord(deoxysObOffset + Gen3Constants.mewObeyOffsetFromDeoxysObey,
                        (((Gen3Constants.gbaCmpRxOpcode | Gen3Constants.gbaR0) << 8) | (0)));
            }
        }

        // Look for evolutions too
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            int evoJumpOffset = find(Gen3Constants.levelEvoKantoDexCheckCode);
            if (evoJumpOffset > 0) {
                // This currently compares species to 0x97 and then allows
                // evolution if it's <= that.
                // Allow it regardless by using an unconditional jump instead
                writeWord(evoJumpOffset, Gen3Constants.gbaNopOpcode);
                writeWord(evoJumpOffset + 2,
                        ((Gen3Constants.gbaUnconditionalJumpOpcode << 8) | (Gen3Constants.levelEvoKantoDexJumpAmount)));
            }

            int stoneJumpOffset = find(Gen3Constants.stoneEvoKantoDexCheckCode);
            if (stoneJumpOffset > 0) {
                // same as the above, but for stone evos
                writeWord(stoneJumpOffset, Gen3Constants.gbaNopOpcode);
                writeWord(stoneJumpOffset + 2,
                        ((Gen3Constants.gbaUnconditionalJumpOpcode << 8) | (Gen3Constants.stoneEvoKantoDexJumpAmount)));
            }
        }
    }

    private void patchForNationalDex() {
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            // Find the original pokedex script
            int pkDexOffset = find(Gen3Constants.rsPokedexScriptIdentifier);
            if (pkDexOffset < 0) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. Could not find script.");
            }
            int textPointer = readPointer(pkDexOffset - 4);
            int realScriptLocation = pkDexOffset - 8;
            int pointerLocToScript = find(pointerToHexString(realScriptLocation));
            if (pointerLocToScript < 0) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. " +
                        "Could not find pointer to script.");
            }
            // Find free space for our new routine
            int writeSpace;
            try {
                writeSpace = findAndUnfreeSpace(Gen3Constants.rsNatDexScriptLength);
            } catch (RomIOException e) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. " + e.getMessage());
            }
            writePointer(pointerLocToScript, writeSpace);
            writeHexString(Gen3Constants.rsNatDexScriptPart1, writeSpace);
            writePointer(writeSpace + 4, textPointer);
            writeHexString(Gen3Constants.rsNatDexScriptPart2, writeSpace + 8);

        } else if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            // Find the original pokedex script
            int pkDexOffset = find(Gen3Constants.frlgPokedexScriptIdentifier);
            if (pkDexOffset < 0) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. Could not find script.");
            }
            // Find free space for our new routine
            int writeSpace;
            try {
                writeSpace = findAndUnfreeSpace(Gen3Constants.frlgNatDexScriptLength);
            } catch (RomIOException e) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. " + e.getMessage());
            }
            writeByte(pkDexOffset, (byte) 4); // TODO: "4" should be a constant
            writePointer(pkDexOffset + 1, writeSpace);
            writeByte(pkDexOffset + 5, (byte) 0); // NOP

            // Now write our new routine
            writeHexString(Gen3Constants.frlgNatDexScript, writeSpace);

            // Fix people using the national dex flag
            List<Integer> ndexChecks = findMultiple(Gen3Constants.frlgNatDexFlagChecker);
            for (int ndexCheckOffset : ndexChecks) {
                // change to a flag-check
                // 82C = "beaten e4/gary once"
                writeHexString(Gen3Constants.frlgE4FlagChecker, ndexCheckOffset);
            }

            // Fix oak in his lab
            int oakLabCheckOffs = find(Gen3Constants.frlgOaksLabKantoDexChecker);
            if (oakLabCheckOffs > 0) {
                // replace it
                writeHexString(Gen3Constants.frlgOaksLabFix, oakLabCheckOffs);
            }

            // Fix oak outside your house
            int oakHouseCheckOffs = find(Gen3Constants.frlgOakOutsideHouseCheck);
            if (oakHouseCheckOffs > 0) {
                // fix him to use ndex count
                writeHexString(Gen3Constants.frlgOakOutsideHouseFix, oakHouseCheckOffs);
            }

            // Fix Oak's aides so they look for your National Dex seen/caught,
            // not your Kanto Dex seen/caught
            int oakAideCheckOffs = find(Gen3Constants.frlgOakAideCheckPrefix);
            if (oakAideCheckOffs > 0) {
                oakAideCheckOffs += Gen3Constants.frlgOakAideCheckPrefix.length() / 2; // because it was a prefix
                // Change the bne instruction to an unconditional branch to always use National Dex
                writeByte(oakAideCheckOffs + 1, (byte) 0xE0);
            }
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
            // Find the original pokedex script
            int pkDexOffset = find(Gen3Constants.ePokedexScriptIdentifier);
            if (pkDexOffset < 0) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. Could not find script.");
            }
            int textPointer = readPointer(pkDexOffset - 4);
            int realScriptLocation = pkDexOffset - 8;
            int pointerLocToScript = find(pointerToHexString(realScriptLocation));
            if (pointerLocToScript < 0) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. " +
                        "Could not find pointer to script.");
            }
            // Find free space for our new routine
            int writeSpace;
            try {
                writeSpace = findAndUnfreeSpace(Gen3Constants.eNatDexScriptLength);
            } catch (RomIOException e) {
                throw new RuntimeException("Patch for National Dex at Start of Game unsuccessful. " + e.getMessage());
            }
            writePointer(pointerLocToScript, writeSpace);
            writeHexString(Gen3Constants.eNatDexScriptPart1, writeSpace);
            writePointer(writeSpace + 4, textPointer);
            writeHexString(Gen3Constants.eNatDexScriptPart2, writeSpace + 8);
        } else {
            throw new IllegalStateException("Invalid ROM Type: " + romEntry.getRomType());
        }
    }

    private String pointerToHexString(int pointer) {
        String hex = String.format("%08X", pointer + 0x08000000);
        return new String(new char[] { hex.charAt(6), hex.charAt(7), hex.charAt(4), hex.charAt(5), hex.charAt(2),
                hex.charAt(3), hex.charAt(0), hex.charAt(1) });
    }

    @Override
    public void loadEvolutions() {
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                pkmn.getEvolutionsFrom().clear();
                pkmn.getEvolutionsTo().clear();
            }
        }

        int baseOffset = romEntry.getIntValue("PokemonEvolutions");
        int numInternalPokes = romEntry.getIntValue("PokemonCount");
        for (int i = 1; i <= numRealPokemon; i++) {
            Species pk = speciesList.get(i);
            int idx = pokedexToInternal[pk.getNumber()];
            int evoOffset = baseOffset + (idx) * 0x28;
            for (int j = 0; j < 5; j++) {
                int method = readWord(evoOffset + j * 8);
                int evolvingTo = readWord(evoOffset + j * 8 + 4);
                if (method >= 1 && method <= Gen3Constants.evolutionMethodCount && evolvingTo >= 1
                        && evolvingTo <= numInternalPokes) {
                    EvolutionType et = Gen3Constants.evolutionTypeFromIndex(method);
                    int extraInfo = readWord(evoOffset + j * 8 + 2);
                    if (et.usesItem()) {
                        extraInfo = Gen3Constants.itemIDToStandard(extraInfo);
                    }
                    Evolution evo = new Evolution(pk, pokesInternal[evolvingTo], et, extraInfo);
                    if (!pk.getEvolutionsFrom().contains(evo)) {
                        pk.getEvolutionsFrom().add(evo);
                        pokesInternal[evolvingTo].getEvolutionsTo().add(evo);
                    }
                }
            }
        }
    }

    private void writeEvolutions() {
        int baseOffset = romEntry.getIntValue("PokemonEvolutions");
        for (int i = 1; i <= numRealPokemon; i++) {
            Species pk = speciesList.get(i);
            int idx = pokedexToInternal[pk.getNumber()];
            int evoOffset = baseOffset + (idx) * 0x28;
            int evosWritten = 0;
            for (Evolution evo : pk.getEvolutionsFrom()) {
                writeWord(evoOffset, Gen3Constants.evolutionTypeToIndex(evo.getType()));
                int extraInfo = evo.getExtraInfo();
                if (evo.getType().usesItem()) {
                    extraInfo = Gen3Constants.itemIDToInternal(extraInfo);
                }
                writeWord(evoOffset + 2, extraInfo);
                writeWord(evoOffset + 4, pokedexToInternal[evo.getTo().getNumber()]);
                writeWord(evoOffset + 6, 0);
                evoOffset += 8;
                evosWritten++;
                if (evosWritten == 5) {
                    break;
                }
            }
            while (evosWritten < 5) {
                writeWord(evoOffset, 0);
                writeWord(evoOffset + 2, 0);
                writeWord(evoOffset + 4, 0);
                writeWord(evoOffset + 6, 0);
                evoOffset += 8;
                evosWritten++;
            }
        }
    }

    @Override
    public void removeImpossibleEvolutions(boolean changeMoveEvos, boolean useEstimatedLevels) {
        attemptObedienceEvolutionPatches();

        // no move evos, so no need to check for those
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                for (Evolution evo : pkmn.getEvolutionsFrom()) {
                    // Not trades, but impossible without trading
                    if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
                        if (evo.getType() == EvolutionType.HAPPINESS_DAY) {
                            // happiness day change to Sun Stone
                            markImprovedEvolutions(pkmn);
                            evo.setType(EvolutionType.STONE);
                            evo.setExtraInfo(ItemIDs.sunStone);
                        }
                        if (evo.getType() == EvolutionType.HAPPINESS_NIGHT) {
                            // happiness night change to Moon Stone
                            markImprovedEvolutions(pkmn);
                            evo.setType(EvolutionType.STONE);
                            evo.setExtraInfo(ItemIDs.moonStone);
                        }
                        if (evo.getType() == EvolutionType.LEVEL_HIGH_BEAUTY) {
                            // beauty change to level 35 (or estimated level if useEstimatedLevels)
                            markImprovedEvolutions(pkmn);
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 35);
                        }
                    }
                    // Pure Trade
                    if (evo.getType() == EvolutionType.TRADE) {
                        // Haunter, Machoke, Kadabra, Graveler
                        // Make it into level 37 (or estimated level if useEstimatedLevels), we're done.
                        markImprovedEvolutions(pkmn);
                        evo.setType(EvolutionType.LEVEL);
                        evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 37);
                    }
                    // Trade w/ Held Item
                    if (evo.getType() == EvolutionType.TRADE_ITEM) {
                        markImprovedEvolutions(pkmn);
                        if (evo.getFrom().getNumber() == SpeciesIDs.poliwhirl) {
                            // Poliwhirl: Lv 37 (or estimated level if useEstimatedLevels)
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 37);
                        } else if (evo.getFrom().getNumber() == SpeciesIDs.slowpoke) {
                            // Slowpoke: Water Stone
                            evo.setType(EvolutionType.STONE);
                            evo.setExtraInfo(ItemIDs.waterStone);
                        } else if (evo.getFrom().getNumber() == SpeciesIDs.seadra) {
                            // Seadra: Lv 40 (or estimated level if useEstimatedLevels)
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 40);
                        } else if (evo.getFrom().getNumber() == SpeciesIDs.clamperl
                                && evo.getExtraInfo() == ItemIDs.deepSeaTooth) {
                            // Clamperl -> Huntail: Lv30 (or estimated level if useEstimatedLevels)
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 30);
                        } else if (evo.getFrom().getNumber() == SpeciesIDs.clamperl
                                && evo.getExtraInfo() == ItemIDs.deepSeaScale) {
                            // Clamperl -> Gorebyss: Water Stone
                            evo.setType(EvolutionType.STONE);
                            evo.setExtraInfo(ItemIDs.waterStone);
                        } else {
                            // Onix, Scyther or Porygon: Lv30 (or estimated level if useEstimatedLevels)
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(useEstimatedLevels ? evo.getEstimatedEvoLvl() : 30);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void makeEvolutionsEasier(boolean changeWithOtherEvos, boolean useEstimatedLevels) {
        // Reduce the amount of happiness required to evolve.
        int offset = find(rom, Gen3Constants.friendshipValueForEvoLocator);
        if (offset > 0) {
            // Amount of required happiness for HAPPINESS evolutions.
            if (rom[offset] == (byte) (GlobalConstants.vanillaHappinessToEvolve - 1)) {
                writeByte(offset, (byte) (GlobalConstants.easierHappinessToEvolve - 1));
            }
            // FRLG doesn't have code to handle time-based evolutions.
            if (romEntry.getRomType() != Gen3Constants.RomType_FRLG) {
                // Amount of required happiness for HAPPINESS_DAY evolutions.
                if (rom[offset + 38] == (byte) (GlobalConstants.vanillaHappinessToEvolve - 1)) {
                    writeByte(offset + 38, (byte) (GlobalConstants.easierHappinessToEvolve - 1));
                }
                // Amount of required happiness for HAPPINESS_NIGHT evolutions.
                if (rom[offset + 66] == (byte) (GlobalConstants.vanillaHappinessToEvolve - 1)) {
                    writeByte(offset + 66, (byte) (GlobalConstants.easierHappinessToEvolve - 1));
                }
            }
        }
    }

    @Override
    public boolean hasShopSupport() {
        return true;
    }

    @Override
    public boolean canChangeShopSizes() {
        return true;
    }

    @Override
    public List<Shop> getShops() {
        List<String> shopNames = Gen3Constants.getShopNames(romEntry.getRomType());
        List<Integer> mainGameShops = Arrays.stream(romEntry.getArrayValue("MainGameShops")).boxed().collect(Collectors.toList());
        List<Integer> skipShops = Arrays.stream(romEntry.getArrayValue("SkipShops")).boxed().collect(Collectors.toList());

        List<Shop> shops = new ArrayList<>();
        int[] shopPointerOffsets = romEntry.getArrayValue("ShopPointerOffsets");
        for (int i = 0; i < shopPointerOffsets.length; i++) {
            int offset = readPointer(shopPointerOffsets[i]);
            List<Item> shopItems = new ArrayList<>();
            int val = FileFunctions.read2ByteInt(rom, offset);
            while (val != 0x0000) {
                shopItems.add(items.get(Gen3Constants.itemIDToStandard(val)));
                offset += 2;
                val = FileFunctions.read2ByteInt(rom, offset);
            }
            Shop shop = new Shop();
            shop.setItems(shopItems);
            shop.setName(shopNames.get(i));
            shop.setMainGame(mainGameShops.contains(i));
            shop.setSpecialShop(!skipShops.contains(i));
            shops.add(shop);
        }
        return shops;
    }

    @Override
    public void setShops(List<Shop> shops) {
        int[] pointerOffsets = romEntry.getArrayValue("ShopPointerOffsets");
        if (shops.size() != pointerOffsets.length) {
            throw new RomIOException("Wrong amount of shops. Should be " + pointerOffsets.length
                    + "; is " + shops.size());
        }

        DataRewriter<Shop> dataRewriter = new DataRewriter<>();
        for (int i = 0; i < shops.size(); i++) {
            dataRewriter.rewriteData(pointerOffsets[i], shops.get(i), this::shopToBytes,
                    oldOffset -> lengthOfDataWithTerminatorAt(oldOffset, Gen3Constants.shopTerminator));
        }
    }

    private byte[] shopToBytes(Shop shop) {
        byte[] data = new byte[shop.getItems().size() * 2 + Gen3Constants.shopTerminator.length];
        int offset = 0;
        for (Item item : shop.getItems()) {
            writeWord(data, offset, Gen3Constants.itemIDToInternal(item.getId()));
            offset += 2;
        }
        writeBytes(data, offset, Gen3Constants.shopTerminator);
        return data;
    }

    public List<Integer> getShopPrices() {
        int itemDataOffset = romEntry.getIntValue("ItemData");
        int entrySize = romEntry.getIntValue("ItemEntrySize");
        int internalItemCount = romEntry.getIntValue("ItemCount");

        List<Integer> prices = new ArrayList<>(Collections.nCopies(items.size(), 0));

        for (int internal = 1; internal < internalItemCount; internal++) {
            int offset = itemDataOffset + (internal * entrySize) + 16;
            int id = Gen3Constants.itemIDToStandard(internal);
            prices.set(id, readWord(offset));
        }
        return prices;
    }

    @Override
    protected Map<Integer, Integer> getBalancedShopPrices() {
        return Gen3Constants.balancedItemPrices;
    }

    public void setShopPrices(List<Integer> prices) {
        int itemDataOffset = romEntry.getIntValue("ItemData");
        int entrySize = romEntry.getIntValue("ItemEntrySize");
        int internalItemCount = romEntry.getIntValue("ItemCount");
        if (prices.size() != items.size()) {
            throw new IllegalArgumentException("prices.size() must equals items.size(). " +
                    "Was:" + prices.size() + ", expected:" + items.size());
        }

        for (int internal = 1; internal < internalItemCount; internal++) {
            int offset = itemDataOffset + (internal * entrySize) + 16;
            int id = Gen3Constants.itemIDToStandard(internal);
            FileFunctions.write2ByteInt(rom, offset, prices.get(id));
        }
    }

    @Override
    public List<PickupItem> getPickupItems() {
        List<PickupItem> pickupItems = new ArrayList<>();
        int pickupItemCount = romEntry.getIntValue("PickupItemCount");
        int sizeOfPickupEntry = romEntry.getRomType() == Gen3Constants.RomType_Em ? 2 : 4;

        // If we haven't found the pickup table for this ROM already, find it.
        if (pickupItemsTableOffset == 0) {
            String pickupTableStartLocator = romEntry.getStringValue("PickupTableStartLocator");
            int offset = find(pickupTableStartLocator);
            if (offset > 0) {
                pickupItemsTableOffset = offset;
            }
        }

        // Assuming we've found the pickup table, extract the items out of it.
        if (pickupItemsTableOffset > 0) {
            for (int i = 0; i < pickupItemCount; i++) {
                int itemOffset = pickupItemsTableOffset + (sizeOfPickupEntry * i);
                int id = Gen3Constants.itemIDToStandard(FileFunctions.read2ByteInt(rom, itemOffset));
                PickupItem pickupItem = new PickupItem(items.get(id));
                pickupItems.add(pickupItem);
            }
        }

        // Assuming we got the items from the last step, fill out the probabilities based on the game.
        if (!pickupItems.isEmpty()) {
            if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
                for (int levelRange = 0; levelRange < 10; levelRange++) {
                    pickupItems.get(0).getProbabilities()[levelRange] = 30;
                    pickupItems.get(7).getProbabilities()[levelRange] = 5;
                    pickupItems.get(8).getProbabilities()[levelRange] = 4;
                    pickupItems.get(9).getProbabilities()[levelRange] = 1;
                    for (int i = 1; i < 7; i++) {
                        pickupItems.get(i).getProbabilities()[levelRange] = 10;
                    }
                }
            } else if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
                for (int levelRange = 0; levelRange < 10; levelRange++) {
                    pickupItems.get(0).getProbabilities()[levelRange] = 15;
                    for (int i = 1; i < 7; i++) {
                        pickupItems.get(i).getProbabilities()[levelRange] = 10;
                    }
                    for (int i = 7; i < 11; i++) {
                        pickupItems.get(i).getProbabilities()[levelRange] = 5;
                    }
                    for (int i = 11; i < 16; i++) {
                        pickupItems.get(i).getProbabilities()[levelRange] = 1;
                    }
                }
            } else {
                for (int levelRange = 0; levelRange < 10; levelRange++) {
                    int startingCommonItemOffset = levelRange;
                    int startingRareItemOffset = 18 + levelRange;
                    pickupItems.get(startingCommonItemOffset).getProbabilities()[levelRange] = 30;
                    for (int i = 1; i < 7; i++) {
                        pickupItems.get(startingCommonItemOffset + i).getProbabilities()[levelRange] = 10;
                    }
                    pickupItems.get(startingCommonItemOffset + 7).getProbabilities()[levelRange] = 4;
                    pickupItems.get(startingCommonItemOffset + 8).getProbabilities()[levelRange] = 4;
                    pickupItems.get(startingRareItemOffset).getProbabilities()[levelRange] = 1;
                    pickupItems.get(startingRareItemOffset + 1).getProbabilities()[levelRange] = 1;
                }
            }
        }
        return pickupItems;
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        int sizeOfPickupEntry = romEntry.getRomType() == Gen3Constants.RomType_Em ? 2 : 4;
        if (pickupItemsTableOffset > 0) {
            for (int i = 0; i < pickupItems.size(); i++) {
                int itemOffset = pickupItemsTableOffset + (sizeOfPickupEntry * i);
                int itemInternalID = Gen3Constants.itemIDToInternal(pickupItems.get(i).getItem().getId());
                FileFunctions.write2ByteInt(rom, itemOffset, itemInternalID);
            }
        }
    }

    @Override
    public boolean canChangeTrainerText() {
        return true;
    }

    @Override
    public TrainerNameMode trainerNameMode() {
        return TrainerNameMode.MAX_LENGTH;
    }

    @Override
    public List<Integer> getTCNameLengthsByTrainer() {
        // not needed
        return new ArrayList<>();
    }

    @Override
    public int maxTrainerNameLength() {
        return romEntry.getIntValue("TrainerNameLength") - 1;
    }

    @Override
    public List<String> getTrainerClassNames() {
        int baseOffset = romEntry.getIntValue("TrainerClassNames");
        int amount = romEntry.getIntValue("TrainerClassCount");
        int length = romEntry.getIntValue("TrainerClassNameLength");
        List<String> trainerClassNames = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            trainerClassNames.add(readVariableLengthString(baseOffset + i * length));
        }
        return trainerClassNames;
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        int baseOffset = romEntry.getIntValue("TrainerClassNames");
        int amount = romEntry.getIntValue("TrainerClassCount");
        int length = romEntry.getIntValue("TrainerClassNameLength");
        Iterator<String> trainerClassNamesIterator = trainerClassNames.iterator();
        for (int i = 0; i < amount; i++) {
            writeFixedLengthString(trainerClassNamesIterator.next(), baseOffset + i * length, length);
        }
    }

    @Override
    public int maxTrainerClassNameLength() {
        return romEntry.getIntValue("TrainerClassNameLength") - 1;
    }

    @Override
    public boolean fixedTrainerClassNamesLength() {
        return false;
    }

    @Override
    public List<Integer> getDoublesTrainerClasses() {
        int[] doublesClasses = romEntry.getArrayValue("DoublesTrainerClasses");
        List<Integer> doubles = new ArrayList<>();
        for (int tClass : doublesClasses) {
            doubles.add(tClass);
        }
        return doubles;
    }

    @Override
    public boolean hasStaticAltFormes() {
        return false;
    }

    @Override
    public boolean hasMainGameLegendaries() {
        return romEntry.getArrayValue("MainGameLegendaries") != null;
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        if (this.hasMainGameLegendaries()) {
            return Arrays.stream(romEntry.getArrayValue("MainGameLegendaries")).boxed().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        return Arrays.stream(romEntry.getArrayValue("SpecialMusicStatics")).boxed().collect(Collectors.toList());
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {
        List<Integer> replaced = new ArrayList<>();
        int newIndexToMusicPoolOffset;

        if (romEntry.hasTweakFile("NewIndexToMusicTweak")) {
            try {
                FileFunctions.applyPatch(rom, romEntry.getTweakFile("NewIndexToMusicTweak"));
            } catch (IOException e) {
                throw new RomIOException(e);
            }

            newIndexToMusicPoolOffset  = romEntry.getIntValue("NewIndexToMusicPoolOffset");

            if (newIndexToMusicPoolOffset > 0) {

                for (int oldStatic: specialMusicStaticChanges.keySet()) {
                    int i = newIndexToMusicPoolOffset;
                    int index = internalToPokedex[readWord(rom, i)];
                    while (index != oldStatic || replaced.contains(i)) {
                        i += 4;
                        index = internalToPokedex[readWord(rom, i)];
                    }
                    writeWord(rom, i, pokedexToInternal[specialMusicStaticChanges.get(oldStatic)]);
                    replaced.add(i);
                }
            }
        }
    }

    @Override
    public boolean hasStaticMusicFix() {
        return romEntry.hasTweakFile("NewIndexToMusicTweak");
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        return new ArrayList<>();
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {

    }

    @Override
    public String getDefaultExtension() {
        return "gba";
    }

    @Override
    public int abilitiesPerSpecies() {
        return 2;
    }

    @Override
    public int highestAbilityIndex() {
        return Gen3Constants.highestAbilityIndex;
    }

    private void loadAbilityNames() {
        int nameoffs = romEntry.getIntValue("AbilityNames");
        int namelen = romEntry.getIntValue("AbilityNameLength");
        abilityNames = new String[Gen3Constants.highestAbilityIndex + 1];
        for (int i = 0; i <= Gen3Constants.highestAbilityIndex; i++) {
            abilityNames[i] = readFixedLengthString(nameoffs + namelen * i, namelen);
        }
    }

    @Override
    public String abilityName(int number) {
        return abilityNames[number];
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        return Gen3Constants.abilityVariations;
    }

    @Override
    public List<Integer> getUselessAbilities() {
        return new ArrayList<>(Gen3Constants.uselessAbilities);
    }

    @Override
    public boolean isTrainerPokemonAlwaysUseAbility1() {
        return true;
    }

    @Override
    public boolean hasMegaEvolutions() {
        return false;
    }

    @Override
    public int internalStringLength(String string) {
        return translateString(string).length;
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        int imageTableOffset = romEntry.getIntValue("PokemonFrontImages");
        int paletteTableOffset = romEntry.getIntValue("PokemonNormalPalettes");
        int cryOffset = romEntry.getIntValue("IntroCryOffset");
        int imageOffset = romEntry.getIntValue("IntroImageOffset");
        int paletteOffset = romEntry.getIntValue("IntroPaletteOffset");
        int otherOffset = romEntry.getIntValue("IntroOtherOffset");

        // FRLG
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            // first 255 only due to size
            if (pokedexToInternal[pk.getNumber()] > 255) {
                return false;
            }
            int introPokemon = pokedexToInternal[pk.getNumber()];

            writeByte(cryOffset, (byte) introPokemon);
            writeByte(otherOffset, (byte) introPokemon);

            writePointer(imageOffset, imageTableOffset + introPokemon * 8);
            writePointer(imageOffset + 4, paletteTableOffset + introPokemon * 8);

        } else if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            // any pokemon in the range 0-510 except bulbasaur
            int introPokemon = pokedexToInternal[pk.getNumber()];
            if (introPokemon == 1 || introPokemon > 510) {
                return false;
            }

            if (introPokemon > 255) { // TODO: this pattern is recurring, maybe extractable into a method?
                rom[cryOffset] = (byte) 0xFF;
                rom[cryOffset + 1] = Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR0;

                rom[cryOffset + 2] = (byte) (introPokemon - 0xFF);
                rom[cryOffset + 3] = Gen3Constants.gbaAddRxOpcode | Gen3Constants.gbaR0;

                rom[otherOffset] = (byte) 0xFF;
                rom[otherOffset + 1] = Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR4;

                rom[otherOffset + 2] = (byte) (introPokemon - 0xFF);
                rom[otherOffset + 3] = Gen3Constants.gbaAddRxOpcode | Gen3Constants.gbaR4;
            } else {
                rom[cryOffset] = (byte) introPokemon;
                rom[cryOffset + 1] = Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR0;

                writeWord(cryOffset + 2, Gen3Constants.gbaNopOpcode);

                rom[otherOffset] = (byte) introPokemon;
                rom[otherOffset + 1] = Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR4;

                writeWord(otherOffset + 2, Gen3Constants.gbaNopOpcode);
            }

            writePointer(imageOffset, imageTableOffset + introPokemon * 8);
            writePointer(paletteOffset, paletteTableOffset + introPokemon * 8);
        } else {
            // Emerald: any Pokemon.
            int introPokemon = pokedexToInternal[pk.getNumber()];
            writeWord(imageOffset, introPokemon);
            writeWord(cryOffset, introPokemon);
        }
        return true;
    }

    private void determineMapBankSizes() {
        int mbpsOffset = romEntry.getIntValue("MapHeaders");
        List<Integer> mapBankOffsets = new ArrayList<>();

        int offset = mbpsOffset;

        // find map banks
        while (true) {
            boolean valid = true;
            for (int mbOffset : mapBankOffsets) {
                if (mbpsOffset < mbOffset && offset >= mbOffset) {
                    valid = false;
                    break;
                }
            }
            if (!valid) {
                break;
            }
            int newMBOffset = readPointer(offset, true);
            if (newMBOffset == -1) {
                break;
            }
            mapBankOffsets.add(newMBOffset);
            offset += 4;
        }
        int bankCount = mapBankOffsets.size();
        int[] bankMapCounts = new int[bankCount];
        for (int bank = 0; bank < bankCount; bank++) {
            int baseBankOffset = mapBankOffsets.get(bank);
            int count = 0;
            offset = baseBankOffset;
            while (true) {
                boolean valid = true;
                for (int mbOffset : mapBankOffsets) {
                    if (baseBankOffset < mbOffset && offset >= mbOffset) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) {
                    break;
                }
                if (baseBankOffset < mbpsOffset && offset >= mbpsOffset) {
                    break;
                }
                int newMapOffset = readPointer(offset, true);
                if (newMapOffset == -1) {
                    break;
                }
                count++;
                offset += 4;
            }
            bankMapCounts[bank] = count;
        }

        romEntry.putIntValue("MapBankCount", bankCount);
        romEntry.putArrayValue("MapBankSizes", bankMapCounts);
    }

    private void preprocessMaps() {
        itemOffs = new ArrayList<>();
        int bankCount = romEntry.getIntValue("MapBankCount");
        int[] bankMapCounts = romEntry.getArrayValue("MapBankSizes");
        int itemBall = romEntry.getIntValue("ItemBallPic");
        mapNames = new String[bankCount][];
        int mbpsOffset = romEntry.getIntValue("MapHeaders");
        int mapLabels = romEntry.getIntValue("MapLabels");
        Map<Integer, String> mapLabelsM = new HashMap<>();

        List<List<List<Gen3EventTextEntry>>> eventTextEntriesByBankAndMap =
                prepareEventTextEntriesByBankAndMap(bankCount, bankMapCounts);

        for (int bank = 0; bank < bankCount; bank++) {
            int bankOffset = readPointer(mbpsOffset + bank * 4);
            mapNames[bank] = new String[bankMapCounts[bank]];
            for (int map = 0; map < bankMapCounts[bank]; map++) {
                int mhOffset = readPointer(bankOffset + map * 4);

                // map name
                int mapLabel = rom[mhOffset + 0x14] & 0xFF;
                if (mapLabelsM.containsKey(mapLabel)) {
                    mapNames[bank][map] = mapLabelsM.get(mapLabel);
                } else {
                    if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
                        mapNames[bank][map] = readVariableLengthString(readPointer(mapLabels
                                + (mapLabel - Gen3Constants.frlgMapLabelsStart) * 4));
                    } else {
                        mapNames[bank][map] = readVariableLengthString(readPointer(mapLabels + mapLabel * 8 + 4));
                    }
                    mapLabelsM.put(mapLabel, mapNames[bank][map]);
                }

                // events
                int eventOffset = readPointer(mhOffset + 4, true);
                if (eventOffset != -1) {

                    int pCount = rom[eventOffset] & 0xFF;
                    int spCount = rom[eventOffset + 3] & 0xFF;

                    if (pCount > 0) {
                        int peopleOffset = readPointer(eventOffset + 4);
                        for (int p = 0; p < pCount; p++) {
                            int pSprite = rom[peopleOffset + p * 24 + 1];
                            int pointerOffset = peopleOffset + p * 24 + 16;
                            if (pSprite == itemBall && readPointer(pointerOffset, true) != -1) {
                                // Get script and look inside
                                int scriptOffset = readPointer(pointerOffset);
                                if (rom[scriptOffset] == 0x1A && rom[scriptOffset + 1] == 0x00
                                        && (rom[scriptOffset + 2] & 0xFF) == 0x80 && rom[scriptOffset + 5] == 0x1A
                                        && rom[scriptOffset + 6] == 0x01 && (rom[scriptOffset + 7] & 0xFF) == 0x80
                                        && rom[scriptOffset + 10] == 0x09
                                        && (rom[scriptOffset + 11] == 0x00 || rom[scriptOffset + 11] == 0x01)) {
                                    // item ball script
                                    itemOffs.add(scriptOffset + 3);
                                }
                            }
                        }

                        for (Gen3EventTextEntry ete : eventTextEntriesByBankAndMap.get(bank).get(map)) {
                            int scriptOffset = readPointer(peopleOffset + (ete.getPersonNum() - 1) * 24 + 16);
                            int[] relPointerOffsets = ete.getRelativePointerOffsets();
                            for (int i = 0; i < relPointerOffsets.length - 1; i++) {
                                scriptOffset = readPointer(scriptOffset + relPointerOffsets[i]);
                            }
                            ete.setActualPointerOffset(scriptOffset + relPointerOffsets[relPointerOffsets.length - 1]);

                        }
                    }

                    if (spCount > 0) {
                        int signpostsOffset = readPointer(eventOffset + 16);
                        for (int sp = 0; sp < spCount; sp++) {
                            int spType = rom[signpostsOffset + sp * 12 + 5];
                            if (spType >= 5 && spType <= 7) {
                                // hidden item
                                int itemHere = readWord(signpostsOffset + sp * 12 + 8);
                                if (itemHere != 0) {
                                    // itemid 0 is coins
                                    itemOffs.add(signpostsOffset + sp * 12 + 8);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<List<List<Gen3EventTextEntry>>> prepareEventTextEntriesByBankAndMap(int bankCount, int[] bankMapsCount) {
        List<List<List<Gen3EventTextEntry>>> byBankAndMap = new ArrayList<>(bankCount);
        for (int mapCount : bankMapsCount) {
            List<List<Gen3EventTextEntry>> byMap = new ArrayList<>(mapCount);
            byBankAndMap.add(byMap);
            for (int i = 0; i < mapCount; i++) {
                byMap.add(new ArrayList<>());
            }
        }
        romEntry.getStarterTexts().forEach(ete -> byBankAndMap.get(ete.getMapBank()).get(ete.getMapNumber()).add(ete));
        romEntry.getTMTexts().forEach(ete -> byBankAndMap.get(ete.getMapBank()).get(ete.getMapNumber()).add(ete));
        romEntry.getMoveTutorTexts().forEach(ete -> byBankAndMap.get(ete.getMapBank()).get(ete.getMapNumber()).add(ete));
        return byBankAndMap;
    }

    @Override
    public Set<Item> getOPShopItems() {
        return itemIdsToSet(Gen3Constants.opShopItems);
    }

    @Override
    public void loadItems() {
        int nameOffs = romEntry.getIntValue("ItemData");
        int structLen = romEntry.getIntValue("ItemEntrySize");
        int internalCount = romEntry.getIntValue("ItemCount");

        int lastItemID = Gen3Constants.getLastItemID(romEntry.getRomType());
        items = new ArrayList<>(Collections.nCopies(lastItemID + 1, null));

        for (int internal = 1; internal <= internalCount; internal++) {
            int id = Gen3Constants.itemIDToStandard(internal);
            String name = readVariableLengthString(nameOffs + structLen * internal);
            items.set(id, new Item(id, name));
        }

        Gen3Constants.bannedItems.stream().filter(id -> id < items.size())
                .map(items::get).filter(Objects::nonNull)
                .forEach(item -> item.setAllowed(false));
        for (int i = ItemIDs.tm01; i < ItemIDs.tm01 + Gen3Constants.tmCount; i++) {
            items.get(i).setTM(true);
        }
        for (int id : Gen3Constants.getBadItems(getROMType())) {
            if (id < items.size()) {
                items.get(id).setBad(true);
            }
        }
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public Set<Item> getRequiredFieldTMs() {
        List<Integer> ids;
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            ids = Gen3Constants.frlgRequiredFieldTMs;
        } else if (romEntry.getRomType() == Gen3Constants.RomType_Ruby || romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            ids = Gen3Constants.rsRequiredFieldTMs;
        } else {
            // emerald has a few TMs from pickup
            ids = Gen3Constants.eRequiredFieldTMs;
        }
        return itemIdsToSet(ids);
    }

    @Override
    public List<Item> getFieldItems() {
        if (!mapLoadingDone) {
            preprocessMaps();
            mapLoadingDone = true;
        }
        List<Item> fieldItems = new ArrayList<>();

        for (int offset : itemOffs) {
            Item item = items.get(Gen3Constants.itemIDToStandard(readWord(offset)));
            if (item.isAllowed()) {
                fieldItems.add(item);
            }
        }
        return fieldItems;
    }

    @Override
    public void setFieldItems(List<Item> fieldItems) {
        checkFieldItemsTMsReplaceTMs(fieldItems);

        if (!mapLoadingDone) {
            preprocessMaps();
            mapLoadingDone = true;
        }
        Iterator<Item> iterItems = fieldItems.iterator();

        for (int offset : itemOffs) {
            Item current = items.get(Gen3Constants.itemIDToStandard(readWord(offset)));
            if (current.isAllowed()) {
                // Replace it
                writeWord(offset, Gen3Constants.itemIDToInternal(iterItems.next().getId()));
            }
        }

    }

    @Override
    public List<InGameTrade> getInGameTrades() {
        List<InGameTrade> trades = new ArrayList<>();

        // info
        int tableOffset = romEntry.getIntValue("TradeTableOffset");
        int tableSize = romEntry.getIntValue("TradeTableSize");
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedOffset = 0;
        int entryLength = 60;

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                unusedOffset++;
                continue;
            }
            InGameTrade trade = new InGameTrade();
            int entryOffset = tableOffset + entry * entryLength;
            trade.setNickname(readVariableLengthString(entryOffset));
            trade.setGivenSpecies(pokesInternal[readWord(entryOffset + 12)]);
            trade.setIVs(new int[6]);
            for (int i = 0; i < 6; i++) {
                trade.getIVs()[i] = rom[entryOffset + 14 + i] & 0xFF;
            }
            trade.setOtId(readWord(entryOffset + 24));
            int heldItemID = Gen3Constants.itemIDToStandard(readWord(entryOffset + 40));
            trade.setHeldItem(items.get(heldItemID));
            trade.setOtName(readVariableLengthString(entryOffset + 43));
            trade.setRequestedSpecies(pokesInternal[readWord(entryOffset + 56)]);
            trades.add(trade);
        }

        return trades;

    }

    @Override
    public void setInGameTrades(List<InGameTrade> trades) {
        // info
        int tableOffset = romEntry.getIntValue("TradeTableOffset");
        int tableSize = romEntry.getIntValue("TradeTableSize");
        int[] unused = romEntry.getArrayValue("TradesUnused");
        int unusedOffset = 0;
        int entryLength = 60;
        int tradeOffset = 0;

        for (int entry = 0; entry < tableSize; entry++) {
            if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                unusedOffset++;
                continue;
            }
            InGameTrade trade = trades.get(tradeOffset++);
            int entryOffset = tableOffset + entry * entryLength;
            writeFixedLengthString(trade.getNickname(), entryOffset, 12);
            writeWord(entryOffset + 12, pokedexToInternal[trade.getGivenSpecies().getNumber()]);
            for (int i = 0; i < 6; i++) {
                writeByte(entryOffset + 14 + i, (byte) trade.getIVs()[i]);
            }
            writeWord(entryOffset + 24, trade.getOtId());
            int heldItemInternalID = trade.getHeldItem() == null ? 0
                    : Gen3Constants.itemIDToInternal(trade.getHeldItem().getId());
            writeWord(entryOffset + 40, heldItemInternalID);
            writeFixedLengthString(trade.getOtName(), entryOffset + 43, 11);
            writeWord(entryOffset + 56, pokedexToInternal[trade.getRequestedSpecies().getNumber()]);
        }
    }

    @Override
    public boolean hasDVs() {
        return false;
    }

    @Override
    public int generationOfPokemon() {
        return 3;
    }

    @Override
    //TODO: this is identical to the Gen 2 implementation => merge (?)
    public void removeEvosForPokemonPool() {
        SpeciesSet pokemonIncluded = rPokeService.getAll(false);
        Set<Evolution> keepEvos = new HashSet<>();
        for (Species pk : pokes) {
            if (pk != null) {
                keepEvos.clear();
                for (Evolution evol : pk.getEvolutionsFrom()) {
                    if (pokemonIncluded.contains(evol.getFrom()) && pokemonIncluded.contains(evol.getTo())) {
                        keepEvos.add(evol);
                    } else {
                        evol.getTo().getEvolutionsTo().remove(evol);
                    }
                }
                pk.getEvolutionsFrom().retainAll(keepEvos);
            }
        }
    }

    @Override
    public boolean supportsFourStartingMoves() {
        return true;
    }

    @Override
    public List<Integer> getFieldMoves() {
        // cut, fly, surf, strength, flash,
        // dig, teleport, waterfall,
        // rock smash, sweet scent
        // not softboiled or milk drink
        // dive and secret power in RSE only
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            return Gen3Constants.frlgFieldMoves;
        } else {
            return Gen3Constants.rseFieldMoves;
        }
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        // RSE: rock smash
        // FRLG: cut
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            return Gen3Constants.frlgEarlyRequiredHMMoves;
        } else {
            return Gen3Constants.rseEarlyRequiredHMMoves;
        }
    }

    @Override
    public int miscTweaksAvailable() {
        int available = MiscTweak.LOWER_CASE_POKEMON_NAMES.getValue();
        if (romEntry.getIntValue("NationalDexTweakPossible") != 0) {
            available |= MiscTweak.NATIONAL_DEX_AT_START.getValue();
        }
        if (romEntry.getIntValue("RunIndoorsTweakOffset") > 0) {
            available |= MiscTweak.RUNNING_SHOES_INDOORS.getValue();
        }
        if (romEntry.getIntValue("TextSpeedValuesOffset") > 0 || romEntry.hasTweakFile("InstantTextTweak")) {
            available |= MiscTweak.FASTEST_TEXT.getValue();
        }
        if (romEntry.getIntValue("CatchingTutorialOpponentMonOffset") > 0
                || romEntry.getIntValue("CatchingTutorialPlayerMonOffset") > 0) {
            available |= MiscTweak.RANDOMIZE_CATCHING_TUTORIAL.getValue();
        }
        if (romEntry.getIntValue("PCPotionOffset") != 0) {
            available |= MiscTweak.RANDOMIZE_PC_POTION.getValue();
        }
        available |= MiscTweak.BAN_LUCKY_EGG.getValue();
        available |= MiscTweak.RUN_WITHOUT_RUNNING_SHOES.getValue();
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            available |= MiscTweak.BALANCE_STATIC_LEVELS.getValue();
        }
        if (romEntry.getArrayValue("TMMovesReusableFunctionOffsets").length != 0) {
            available |= MiscTweak.REUSABLE_TMS.getValue();
        }
        if (romEntry.getArrayValue("HMMovesForgettableFunctionOffsets").length != 0) {
            available |= MiscTweak.FORGETTABLE_HMS.getValue();
        }
        return available;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if (tweak == MiscTweak.RUNNING_SHOES_INDOORS) {
            applyRunningShoesIndoorsPatch();
        } else if (tweak == MiscTweak.FASTEST_TEXT) {
            applyFastestTextPatch();
        } else if (tweak == MiscTweak.LOWER_CASE_POKEMON_NAMES) {
            applyCamelCaseNames();
        } else if (tweak == MiscTweak.NATIONAL_DEX_AT_START) {
            patchForNationalDex();
        } else if (tweak == MiscTweak.BAN_LUCKY_EGG) {
            items.get(ItemIDs.luckyEgg).setAllowed(false);
        } else if (tweak == MiscTweak.RUN_WITHOUT_RUNNING_SHOES) {
            applyRunWithoutRunningShoesPatch();
        } else if (tweak == MiscTweak.BALANCE_STATIC_LEVELS) {
            int[] fossilLevelOffsets = romEntry.getArrayValue("FossilLevelOffsets");
            for (int fossilLevelOffset : fossilLevelOffsets) {
                writeWord(rom, fossilLevelOffset, 30);
            }
        } else if (tweak == MiscTweak.REUSABLE_TMS) {
            applyReusableTMsPatch();
        } else if (tweak == MiscTweak.FORGETTABLE_HMS) {
            applyForgettableHMsPatch();
        }
    }

    private void applyRunningShoesIndoorsPatch() {
        if (romEntry.getIntValue("RunIndoorsTweakOffset") != 0) {
            writeByte(romEntry.getIntValue("RunIndoorsTweakOffset"), (byte) 0x00);
        }
    }

    private void applyFastestTextPatch() {
        if(romEntry.hasTweakFile("InstantTextTweak")) {
            try {
                FileFunctions.applyPatch(rom, romEntry.getTweakFile("InstantTextTweak"));
            } catch (IOException e) {
                throw new RomIOException(e);
            }
        } else if (romEntry.getIntValue("TextSpeedValuesOffset") > 0) {
            int tsvOffset = romEntry.getIntValue("TextSpeedValuesOffset");
            byte[] newTextSpeedValues = new byte[] {4, // slow = medium
                    1, // medium = fast
                    0}; // fast = instant
            writeBytes(tsvOffset, newTextSpeedValues);
        }
    }

    private void applyRunWithoutRunningShoesPatch() {
        String prefix = Gen3Constants.getRunningShoesCheckPrefix(romEntry.getRomType());
        int offset = find(prefix);
        if (offset != 0) {
            // The prefix starts 0x12 bytes from what we want to patch because what comes
            // between is region and revision dependent. To start running, the game checks:
            // 1. That you're not underwater (RSE only)
            // 2. That you're holding the B button
            // 3. That the FLAG_SYS_B_DASH flag is set (aka, you've acquired Running Shoes)
            // 4. That you're allowed to run in this location
            // For #3, if the flag is unset, it jumps to a different part of the
            // code to make you walk instead. This simply nops out this jump so the
            // game stops caring about the FLAG_SYS_B_DASH flag entirely.
            writeWord(offset + 0x12, 0);
        }
    }

    private void applyReusableTMsPatch() {
        // When a TM/HM has just been used, the game compares its item ID to HM01_Cut's,
        // and only removes the item if the ID is smaller.
        // To make all TMs reusable, change this from HM01_Cut => 0.
        // FRLG has multiple comparisons like this to change, so we deal with offsets instead of singular offset.
        int[] offsets = romEntry.getArrayValue("TMMovesReusableFunctionOffsets");
        byte hmCompareVal = (byte) (Gen3Constants.itemIDToInternal(ItemIDs.hm01) / 2);
        for (int offset : offsets) {
            if (rom[offset] != hmCompareVal) {
                throw new RuntimeException("Expected 0x" + Integer.toHexString(hmCompareVal) + ", was 0x"
                        + Integer.toHexString(rom[offset]) + ". Likely TMMovesReusableFunctionOffsets is faulty.");
            }
            writeByte(offset, (byte) 0);
        }
        tmsReusable = true;
    }

    private void applyForgettableHMsPatch() {
        // There are multiple locations where the game checks whether a Move is in a
        // "banned from forgetting" list. If this was always the same list we could blank out that,
        // but it is not. Instead, we force the checks themselves to misreport, to return "0" rather
        // than "1" when an HM is found.
        int[] offsets = romEntry.getArrayValue("HMMovesForgettableFunctionOffsets");
        for (int offset : offsets) {
            if (rom[offset] != 1) {
                throw new RuntimeException("Expected 0x01, was 0x"
                        + Integer.toHexString(rom[offset]) + ". Likely HMMovesForgettableFunctionOffsets is faulty.");
            }
            writeByte(offset, (byte) 0);
        }
    }

    @Override
    public boolean setCatchingTutorial(Species opponent, Species player) {
        if (romEntry.getIntValue("CatchingTutorialOpponentMonOffset") > 0) {
            // only Pokemon that can be males are allowed (not sure why, taken from uncommented older code)
            if (opponent.getGenderRatio() > 0xFD) {
                return false;
            }
            int oppOffset = romEntry.getIntValue("CatchingTutorialOpponentMonOffset");
            if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
                if (opponent.getNumber() > 255) {
                    return false;
                }

                int oppValue = pokedexToInternal[opponent.getNumber()];
                writeBytes(oppOffset, new byte[] {(byte) oppValue,
                        Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1});

            } else {
                if (opponent.getNumber() > 510) {
                    return false;
                }
                int oppValue = pokedexToInternal[opponent.getNumber()];
                if (oppValue > 255) {
                    writeBytes(oppOffset, new byte[] {(byte) 0xFF,
                            Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1,
                            (byte) (oppValue - 0xFF),
                            Gen3Constants.gbaAddRxOpcode | Gen3Constants.gbaR1});
                } else {
                    writeBytes(oppOffset, new byte[] {(byte) oppValue,
                            Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1});

                    writeWord(oppOffset + 2, Gen3Constants.gbaNopOpcode);
                }
            }
        }

        if (romEntry.getIntValue("CatchingTutorialPlayerMonOffset") > 0) {
            int playerOffset = romEntry.getIntValue("CatchingTutorialPlayerMonOffset");
            if (player.getNumber() > 510) {
                return false;
            }

            int plyValue = pokedexToInternal[player.getNumber()];
            if (plyValue > 255) {
                writeBytes(playerOffset, new byte[] {(byte) 0xFF,
                        Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1,
                        (byte) (plyValue - 0xFF),
                        Gen3Constants.gbaAddRxOpcode | Gen3Constants.gbaR1});
            } else {
                writeBytes(playerOffset, new byte[] {(byte) plyValue,
                        Gen3Constants.gbaSetRxOpcode | Gen3Constants.gbaR1});
                writeWord(playerOffset + 2, Gen3Constants.gbaNopOpcode);
            }
        }
        return true;
    }

    @Override
    public void setPCPotionItem(Item item) {
        if (romEntry.getIntValue("PCPotionOffset") != 0) {
            if (!item.isAllowed()) {
                throw new IllegalArgumentException("item not allowed for PC Potion: " + item.getName());
            }
            writeWord(romEntry.getIntValue("PCPotionOffset"), Gen3Constants.itemIDToInternal(item.getId()));
        }
    }

    @Override
    public TypeTable getTypeTable() {
        return readTypeTable();
    }

    private TypeTable readTypeTable() {
        TypeTable typeTable = new TypeTable(Type.getAllTypes(3));
        int currentOffset = romEntry.getIntValue("TypeEffectivenessOffset");
        int attackingType = rom[currentOffset];
        while (attackingType != GBConstants.typeTableTerminator) {
            if (rom[currentOffset] != GBConstants.typeTableForesightTerminator) {
                int defendingType = rom[currentOffset + 1];
                int effectivenessInternal = rom[currentOffset + 2];
                Type attacking = Gen3Constants.typeTable[attackingType];
                Type defending = Gen3Constants.typeTable[defendingType];
                Effectiveness effectiveness;
                switch (effectivenessInternal) {
                    case 20:
                        effectiveness = Effectiveness.DOUBLE;
                        break;
                    case 10:
                        effectiveness = Effectiveness.NEUTRAL;
                        break;
                    case 5:
                        effectiveness = Effectiveness.HALF;
                        break;
                    case 0:
                        effectiveness = Effectiveness.ZERO;
                        break;
                    default:
                        effectiveness = null;
                        break;
                }
                if (effectiveness != null) {
                    typeTable.setEffectiveness(attacking, defending, effectiveness);
                }
            }
            currentOffset += 3;
            attackingType = rom[currentOffset];
        }
        return typeTable;
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        writeTypeTable(typeTable);
    }

    private void writeTypeTable(TypeTable typeTable) {
        if (typeTable.nonNeutralEffectivenessCount() > Gen3Constants.nonNeutralEffectivenessCount) {
            throw new IllegalArgumentException("Too many non-neutral Effectiveness-es. Was "
                    + typeTable.nonNeutralEffectivenessCount() + ", has to be at most " +
                    Gen3Constants.nonNeutralEffectivenessCount);
        }
        int tableOffset = romEntry.getIntValue("TypeEffectivenessOffset");

        ByteArrayOutputStream mainPart = new ByteArrayOutputStream();
        ByteArrayOutputStream ghostImmunities = new ByteArrayOutputStream();

        prepareTypeTableParts(typeTable, mainPart, ghostImmunities);
        writeTypeTableParts(tableOffset, mainPart, ghostImmunities);
    }

    private void prepareTypeTableParts(TypeTable typeTable, ByteArrayOutputStream mainPart, ByteArrayOutputStream ghostImmunities) {
        for (Type attacker : typeTable.getTypes()) {
            for (Type defender : typeTable.getTypes()) {
                Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
                if (eff != Effectiveness.NEUTRAL) {
                    ByteArrayOutputStream part = (defender == Type.GHOST && eff == Effectiveness.ZERO)
                            ? ghostImmunities : mainPart;
                    byte effectivenessInternal ;switch (eff) {
                        case DOUBLE:
                            effectivenessInternal = 20;
                            break;
                        case HALF:
                            effectivenessInternal = 5;
                            break;
                        default:
                            effectivenessInternal = 0;
                            break;
                    }
                    part.write(Gen3Constants.typeToByte(attacker));
                    part.write(Gen3Constants.typeToByte(defender));
                    part.write(effectivenessInternal);
                }
            }
        }
    }

    private void writeTypeTableParts(int tableOffset, ByteArrayOutputStream mainPart, ByteArrayOutputStream ghostImmunities) {
        writeBytes(tableOffset, mainPart.toByteArray());
        tableOffset += mainPart.size();

        writeBytes(tableOffset, new byte[] {GBConstants.typeTableForesightTerminator,
                GBConstants.typeTableForesightTerminator, (byte) 0x00});
        tableOffset += 3;

        writeBytes(tableOffset, ghostImmunities.toByteArray());
        tableOffset += ghostImmunities.size();

        writeBytes(tableOffset, new byte[] {GBConstants.typeTableTerminator,
                GBConstants.typeTableTerminator, (byte) 0x00});
    }

    @Override
	public Set<Item> getAllHeldItems() {
		return itemIdsToSet(Gen3Constants.allHeldItems);
	}

	@Override
	public boolean hasRivalFinalBattle() {
        return romEntry.getRomType() == Gen3Constants.RomType_FRLG;
    }

    public void enableGuaranteedPokemonCatching() {
        int offset = find(rom, Gen3Constants.perfectOddsBranchLocator);
        if (offset > 0) {
            // In Cmd_handleballthrow, the middle of the function checks if the odds of catching a Pokemon
            // is greater than 254; if it is, then the Pokemon is automatically caught. In ASM, this is
            // represented by:
            // cmp r6, #0xFE
            // bls oddsLessThanOrEqualTo254
            // The below code just nops these two instructions so that we *always* act like our odds are 255,
            // and Pokemon are automatically caught no matter what.
            writeBytes(offset, new byte[] {0x00, 0x00, 0x00, 0x00});
        }
    }

    @Override
    public void loadPokemonPalettes() {
        int normalPaletteTableOffset = romEntry.getIntValue("PokemonNormalPalettes");
        int shinyPaletteTableOffset = romEntry.getIntValue("PokemonShinyPalettes");
        for (Species pk : getSpeciesSet()) {
            int pokeNumber = pokedexToInternal[pk.getNumber()];

            int normalPalOffset = readPointer(normalPaletteTableOffset + pokeNumber * 8);
            pk.setNormalPalette(readPalette(normalPalOffset));

            int shinyPalOffset = readPointer(shinyPaletteTableOffset + pokeNumber * 8);
            pk.setShinyPalette(readPalette(shinyPalOffset));
        }
    }

    private Palette readPalette(int palOffset) {
        byte[] paletteBytes = DSDecmp.Decompress(rom, palOffset);
        return new Palette(paletteBytes);
    }

    @Override
    public void savePokemonPalettes() {
        int normalPaletteTableOffset = romEntry.getIntValue("PokemonNormalPalettes");
        int shinyPaletteTableOffset = romEntry.getIntValue("PokemonShinyPalettes");
        for (Species pk : getSpeciesSet()) {
            int pokeNumber = pokedexToInternal[pk.getNumber()];
            int normalPalPointerOffset = normalPaletteTableOffset + pokeNumber * 8;
            int shinyPalPointerOffset = shinyPaletteTableOffset + pokeNumber * 8;

            if (pk.getNumber() == SpeciesIDs.unown) {
                int[] altFormeNormalPointerOffsets = IntStream.range(0, Gen3Constants.unownFormeCount - 1)
                        .map(i -> normalPaletteTableOffset + (Gen3Constants.unownBIndex + i) * 8)
                        .toArray();
                int[] altFormeShinyPointerOffsets = IntStream.range(0, Gen3Constants.unownFormeCount - 1)
                        .map(i -> shinyPaletteTableOffset + (Gen3Constants.unownBIndex + i) * 8)
                        .toArray();
                rewriteCompressedData(normalPalPointerOffset, pk.getNormalPalette().toBytes(),
                        altFormeNormalPointerOffsets);
                rewriteCompressedData(shinyPalPointerOffset, pk.getShinyPalette().toBytes(),
                        altFormeShinyPointerOffsets);

            } else {
                rewriteCompressedPalette(normalPalPointerOffset, pk.getNormalPalette());
                rewriteCompressedPalette(shinyPalPointerOffset, pk.getShinyPalette());
            }
        }
    }

    @Override
    public boolean hasCustomPlayerGraphicsSupport() {
        return true;
    }

    @Override
    public void setCustomPlayerGraphics(CustomPlayerGraphics customPlayerGraphics) {
        GraphicsPack unchecked = customPlayerGraphics.getGraphicsPack();
        PlayerCharacterType toReplace = customPlayerGraphics.getTypeToReplace();

        if (!(unchecked instanceof Gen3PlayerCharacterGraphics)) {
            throw new IllegalArgumentException("Invalid playerGraphics");
        }
        Gen3PlayerCharacterGraphics playerGraphics = (Gen3PlayerCharacterGraphics) unchecked;

        if (romEntry.getRomType() != Gen3Constants.RomType_FRLG) {
            separateFrontAndBackPlayerPalettes();
        }
        if (playerGraphics.hasFrontImage()) {
            int trainerNum = toReplace.ordinal();
            if (romEntry.getRomType() == Gen3Constants.RomType_Em) {
                trainerNum += Gen3Constants.emBrendanFrontImageIndex;
            } else if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
                trainerNum += Gen3Constants.frlgRedFrontImageIndex;
            }
            writeTrainerImage(trainerNum, playerGraphics.getFrontImage());
        }
        if (playerGraphics.hasBackImage()) {
            writeTrainerBackImage(toReplace.ordinal(), playerGraphics.getBackImage());
        }
        if (playerGraphics.hasSpritePalettes()) {
            String name = romEntry.getRomType() == Gen3Constants.RomType_FRLG ?
                    Gen3Constants.frlgGetName(toReplace) : Gen3Constants.rseGetName(toReplace);
            int normalIndex = romEntry.getIntValue(name + "NormalPalette");
            writeOverworldPalette(normalIndex, playerGraphics.getNormalSpritePalette());
            int reflectionIndex = romEntry.getIntValue(name + "ReflectionPalette");
            writeOverworldPalette(reflectionIndex, playerGraphics.getReflectionSpritePalette());
        }
        if (playerGraphics.hasWalkSprite()) {
            writePlayerSprite(playerGraphics.getWalkSprite(), toReplace, "WalkImage");
            writePlayerSprite(playerGraphics.getRunSprite(), toReplace, "RunImage");
        }
        if (playerGraphics.hasBikeSprite()) {
            writePlayerSprite(playerGraphics.getBikeSprite(), toReplace, "BikeImage");
        }
        if (playerGraphics.hasFishSprite()) {
            writePlayerSprite(playerGraphics.getFishSprite(), toReplace, "FishImage");
        }
        if (playerGraphics.hasSitSprite()) {
            writePlayerSprite(playerGraphics.getSitSprite(), toReplace, "SitImage");
        }
        if (playerGraphics.hasSurfBlobSprite()) {
            writePlayerSurfBlobSprite(playerGraphics.getSurfBlobSprite());
        }
        if (playerGraphics.hasBirdSprite()) {
            writePlayerBirdSprite(playerGraphics.getBirdSprite(), toReplace);
        }
        if (playerGraphics.hasMapIcon()) {
            writePlayerMapIcon(playerGraphics.getMapIcon(), toReplace);
        }
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            setFRLGCustomPlayerGraphics(playerGraphics, toReplace);
        } else {
            setRSECustomPlayerGraphics(playerGraphics, toReplace);
        }
    }

    private void setRSECustomPlayerGraphics(Gen3PlayerCharacterGraphics unchecked, PlayerCharacterType toReplace) {
        if (!(unchecked instanceof RSEPlayerCharacterGraphics)) {
            throw new IllegalArgumentException("Invalid playerGraphics");
        }
        RSEPlayerCharacterGraphics playerGraphics = (RSEPlayerCharacterGraphics) unchecked;

        if (playerGraphics.hasSitJumpSprite()) {
            writePlayerSprite(playerGraphics.getSitJumpSprite(), toReplace, "SitJumpImage");
        }
        if (playerGraphics.hasAcroBikeSprite()) {
            writePlayerSprite(playerGraphics.getAcroBikeSprite(), toReplace, "AcroBikeImage");
        }
        if (playerGraphics.hasUnderwaterSprite()) {
            writePlayerSprite(playerGraphics.getUnderwaterSprite(), toReplace, "UnderwaterImage");
            int underwaterIndex = romEntry.getIntValue("UnderwaterPalette");
            writeOverworldPalette(underwaterIndex, playerGraphics.getUnderwaterPalette());
        }
        if (playerGraphics.hasWateringCanSprite()) {
            writePlayerWateringCanSprite(playerGraphics.getWateringCanSprite(), toReplace);
        }
        if (playerGraphics.hasDecorateSprite()) {
            writePlayerSprite(playerGraphics.getDecorateSprite(), toReplace, "DecorateImage");
        }
        if (playerGraphics.hasFieldMoveSprite()) {
            writePlayerSprite(playerGraphics.getFieldMoveSprite(), toReplace, "FieldMoveImage");
        }
    }

    private void setFRLGCustomPlayerGraphics(Gen3PlayerCharacterGraphics unchecked, PlayerCharacterType toReplace) {
        if (!(unchecked instanceof FRLGPlayerCharacterGraphics)) {
            throw new IllegalArgumentException("Invalid playerGraphics");
        }
        FRLGPlayerCharacterGraphics playerGraphics = (FRLGPlayerCharacterGraphics) unchecked;

        if (playerGraphics.hasItemSprite()) {
            writePlayerSprite(playerGraphics.getItemSprite(), toReplace, "ItemImage");
        }
        if (playerGraphics.hasItemBikeSprite()) {
            writePlayerSprite(playerGraphics.getItemBikeSprite(), toReplace, "ItemBikeImage");
        }
    }

    private void separateFrontAndBackPlayerPalettes() {
        int frontTableOffset = romEntry.getIntValue("TrainerFrontPalettes");

        for (int i = 0; i <= 1; i++) {
            int trainerImageNum = romEntry.getRomType() ==
                    Gen3Constants.RomType_Em ? i + Gen3Constants.emBrendanFrontImageIndex : i;
            int frontPointerOffset = frontTableOffset + trainerImageNum * 8;
            Palette palette = readPalette(readPointer(frontPointerOffset));
            byte[] paletteBytes = DSCmp.compressLZ10(palette.toBytes());
            int newOffset = findAndUnfreeSpace(paletteBytes.length);
            writeBytes(newOffset, paletteBytes);
            writePointer(frontPointerOffset, newOffset);
        }
    }

    private void writeTrainerImage(int trainerNumber, GBAImage image) {
        int imageTableOffset = romEntry.getIntValue("TrainerFrontImages");
        int paletteTableOffset = romEntry.getIntValue("TrainerFrontPalettes");

        int imagePointerOffset = imageTableOffset + trainerNumber * 8;
        rewriteCompressedImage(imagePointerOffset, image);

        int palettePointerOffset = paletteTableOffset + trainerNumber * 8;
        rewriteCompressedPalette(palettePointerOffset, image.getPalette());
    }

    private void writeTrainerBackImage(int trainerNumber, GBAImage image) {
        int imageTableOffset = romEntry.getIntValue("TrainerBackImages");
        int paletteTableOffset = romEntry.getIntValue("TrainerBackPalettes");

        int imagePointerOffset = imageTableOffset + trainerNumber * 8;
        if (romEntry.getRomType() == Gen3Constants.RomType_Ruby ||
                romEntry.getRomType() == Gen3Constants.RomType_Sapp) {
            rewriteCompressedImage(imagePointerOffset, image);
        } else {
            writeBytes(readPointer(imagePointerOffset), image.toBytes());
        }

        int palettePointerOffset = paletteTableOffset + trainerNumber * 8;
        rewriteCompressedPalette(palettePointerOffset, image.getPalette());
    }

    private void writePlayerSprite(GBAImage sprite, PlayerCharacterType toReplace, String key) {
        for (int i = 0; i < sprite.getFrameAmount(); i++) {
            GBAImage frame = sprite.getSubimageFromFrame(i);
            String name = romEntry.getRomType() == Gen3Constants.RomType_FRLG ?
                    Gen3Constants.frlgGetName(toReplace) : Gen3Constants.rseGetName(toReplace);
            int imageNum = romEntry.getIntValue(name + key) + i;
            writeOverworldImage(imageNum, frame);
        }
    }

    private void writePlayerWateringCanSprite(GBAImage sprite, PlayerCharacterType toReplace) {
        int imageNum = romEntry.getIntValue(Gen3Constants.rseGetName(toReplace) + "WateringCanImage");
        writeOverworldImage(imageNum, sprite.getSubimageFromFrame(0));
        writeOverworldImage(imageNum + 1, sprite.getSubimageFromFrame(2));
        writeOverworldImage(imageNum + 2, sprite.getSubimageFromFrame(4));
        writeOverworldImage(imageNum + 3, sprite.getSubimageFromFrame(1));
        writeOverworldImage(imageNum + 5, sprite.getSubimageFromFrame(3));
        writeOverworldImage(imageNum + 7, sprite.getSubimageFromFrame(5));
    }

    private void writePlayerSurfBlobSprite(GBAImage sprite) {
        int imageTableOffset = romEntry.getIntValue("OverworldSurfBlobImages");

        for (int frame = 0; frame < sprite.getFrameAmount(); frame++) {
            writeOverworldImage(frame, sprite.getSubimageFromFrame(frame), imageTableOffset);
        }
    }

    private void writePlayerBirdSprite(GBAImage sprite, PlayerCharacterType toReplace) {
        int imageTableOffset = romEntry.getIntValue("OverworldBirdImages");

        writeOverworldImage(0, sprite.getSubimageFromFrame(0), imageTableOffset);

        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            writeOverworldImage(toReplace == PlayerCharacterType.PC1 ? 1 : 3,
                    sprite.getSubimageFromFrame(1), imageTableOffset);
            writeOverworldImage(toReplace == PlayerCharacterType.PC1 ? 2 : 4,
                    sprite.getSubimageFromFrame(2), imageTableOffset);
        }
    }

    /**
     * Overwrites an entry in the main overworld image table, with a given {@link BufferedImage}.
     * The given image must be as large as the one it is overwriting.
     */
    private void writeOverworldImage(int index, GBAImage image) {
        int imageTableOffset = romEntry.getIntValue("OverworldSpriteImages");
        writeOverworldImage(index, image, imageTableOffset);
    }

    /**
     * Overwrites an entry in an overworld image table, with a given {@link BufferedImage}.
     * The given image must be as large as the one it is overwriting.
     */
    private void writeOverworldImage(int index, GBAImage image, int imageTableOffset) {
        int imagePointerOffset = imageTableOffset + index * 8;
        int imageOffset = readPointer(imagePointerOffset);
        int imageLength = readWord(imagePointerOffset + 4);

        byte[] imageData = image.toBytes();
        if (imageData.length != imageLength) {
            throw new IllegalArgumentException("Wrong image size. Expected " + imageLength + " bytes, was "
                    + imageData.length + ".");
        }
        writeBytes(imageOffset, imageData);
    }

    private void writeOverworldPalette(int index, Palette palette) {
        if (palette.size() != 16) {
            throw new IllegalArgumentException("Palette must have exactly 16 colors");
        }
        int paletteTableOffset = romEntry.getIntValue("OverworldPalettes");
        int paletteOffset = readPointer(paletteTableOffset + index * 8);
        writeBytes(paletteOffset, palette.toBytes());
    }

    private void writePlayerMapIcon(GBAImage mapIcon, PlayerCharacterType toReplace) {
        if (romEntry.getRomType() == Gen3Constants.RomType_FRLG) {
            int imagePointerOffset = romEntry.getIntValue(Gen3Constants.frlgGetName(toReplace)
                    + "MapIconImagePointer");
            rewriteCompressedImage(imagePointerOffset, mapIcon);
            int paletteOffset = readPointer(romEntry.getIntValue(Gen3Constants.frlgGetName(toReplace)
                    + "MapIconPalettePointer"));
            writeBytes(paletteOffset, mapIcon.getPalette().toBytes());

        } else {
            int imageOffset = romEntry.getIntValue(Gen3Constants.rseGetName(toReplace) + "MapIconImage");
            int paletteOffset = romEntry.getIntValue(Gen3Constants.rseGetName(toReplace) + "MapIconPalette");
            writeBytes(imageOffset, mapIcon.toBytes());
            writeBytes(paletteOffset, mapIcon.getPalette().toBytes());
        }
    }

    private void rewriteCompressedPalette(int pointerOffset, Palette palette) {
        rewriteCompressedData(pointerOffset, palette.toBytes());
    }

    private void rewriteCompressedImage(int pointerOffset, GBAImage image) {
        rewriteCompressedData(pointerOffset, image.toBytes());
    }

    /*
     * Assumes there is only one pointer to the compressed data. If there are more,
     * use rewriteCompressedData(int, byte[], int[]) directly.
     */
    private void rewriteCompressedData(int pointerOffset, byte[] uncompressed) {
        rewriteCompressedData(pointerOffset, uncompressed, new int[0]);
    }

    private void rewriteCompressedData(int pointerOffset, byte[] uncompressed, int[] secondaryPointerOffsets) {
        new DataRewriter<byte[]>().rewriteData(pointerOffset, uncompressed, secondaryPointerOffsets,
                DSCmp::compressLZ10, this::lengthOfCompressedDataAt);
    }

    /*
     * Returns the length in bytes of the compressed data at the pointer. NOT the
     * length of the uncompressed data, but the length of it when compressed.
     */
    private int lengthOfCompressedDataAt(int pointerOffset) {
        // Yes, the "easiest" way to check how long compressed data is to uncompress it
        // and then compress it back.
        // A better solution would require understanding the inner workings of the LZ10.
        byte[] uncompressed = DSDecmp.Decompress(rom, pointerOffset);
        byte[] compressed = DSCmp.compressLZ10(uncompressed);
        return compressed.length;
    }

	private void rewriteVariableLengthString(int pointerOffset, String string) {
		rewriteVariableLengthString(pointerOffset, string, new int[0]);
	}

	private void rewriteVariableLengthString(int pointerOffset, String string, int[] secondaryPointerOffsets) {
		new DataRewriter<String>().rewriteData(pointerOffset, string, secondaryPointerOffsets,
				this::variableLengthStringToBytes, this::lengthOfStringAt);
	}

	private byte[] variableLengthStringToBytes(String string) {
		byte[] translated = translateString(string);
		byte[] newData = Arrays.copyOf(translated, translated.length + 1);
		newData[newData.length - 1] = (byte) 0xFF;
		return newData;
	}

    @Override
    protected FreedSpace getFreedSpace() {
        return freedSpace;
    }

	@Override
	protected byte getFreeSpaceByte() {
		return Gen3Constants.freeSpaceByte;
	}

	/**
	 * Finds and "frees" space that was unused in the unrandomized ROM, i.e. marks
	 * it as available for writing to.<br>
	 * "Unused" in this context refers only to blank data of many "FF"s in a row,
	 * NOT stuff like unused graphics, maps, and music. <br>
	 * <br>
	 * The finding is automatic instead of decided wholly by manual offsets. This
	 * presents a risk in theory, of marking "unused" space which isn't, but there
	 * is a guiding manual offset and other precautions, so this risk should be
	 * slim.
	 */
	private void freeAllUnusedSpace() {
		int unusedSpaceStartOffset = romEntry.getIntValue("FreeSpace");
		int unusedSpaceOffset = unusedSpaceStartOffset;
		int chunkLength = Gen3Constants.unusedSpaceChunkLength;
		int frontMargin = Gen3Constants.unusedSpaceFrontMargin;

		boolean freedAllUnused = false;

		while (!freedAllUnused) {
			byte[] searchNeedle = new byte[chunkLength];
			for (int i = 0; i < chunkLength; i++) {
				searchNeedle[i] = getFreeSpaceByte();
			}
			int foundOffset = RomFunctions.searchForFirst(rom, unusedSpaceOffset, searchNeedle);

			if (foundOffset < unusedSpaceStartOffset) {
				freedAllUnused = true;
			} else {

				if (foundOffset > unusedSpaceOffset + chunkLength || foundOffset == unusedSpaceStartOffset) {
					freeSpace(foundOffset + frontMargin, chunkLength - frontMargin);
				} else {
					freeSpace(foundOffset, chunkLength);
				}
				unusedSpaceOffset = foundOffset + chunkLength;
			}
		}

	}

    @Override
    public Gen3PokemonImageGetter createPokemonImageGetter(Species pk) {
        return new Gen3PokemonImageGetter(pk);
    }

    public class Gen3PokemonImageGetter extends GBPokemonImageGetter {
        // No support for Unown/Castform formes (nor Deoxys, but kind of unsure if that's even possible)

        public Gen3PokemonImageGetter(Species pk) {
            super(pk);
        }

        @Override
        public Gen3PokemonImageGetter setGraphicalForme(int forme) {
            throw new UnsupportedOperationException("Graphical forme support not implemented");
        }

        @Override
        public BufferedImage get() {
            // TODO: what's up with shiny lileep in one of the international games (forgot which)? is the ROM bad?

            int num = pokedexToInternal[pk.getNumber()];
            int tableOffset = back ? romEntry.getIntValue("PokemonBackImages") : romEntry.getIntValue("PokemonFrontImages");

            int imageOffset = readPointer(tableOffset + num * 8);
            byte[] data = DSDecmp.Decompress(rom, imageOffset);
            // Uses the 0-index missingno sprite if the data failed to read, for debugging
            // purposes
            if (data == null) {
                imageOffset = readPointer(tableOffset);
                data = DSDecmp.Decompress(rom, imageOffset);
            }

            Palette palette = shiny ? pk.getShinyPalette() : pk.getNormalPalette();
            // Castform has a 64-color palette, 16 colors for each form.
            if (pk.getNumber() == SpeciesIDs.castform) {
                palette = new Palette(Arrays.copyOf(palette.toARGB(), 16));
            }

            // Make image, 4bpp
            GBAImage bim = new GBAImage.Builder(8, 8, palette, data)
                    .transparent(transparentBackground)
                    .build();
            if (includePalette) {
                for (int i = 0; i < palette.size(); i++) {
                    bim.setColor(i, 0, i);
                }
            }

            return bim;
        }
    }

    public String getPaletteFilesID() {
        switch (romEntry.getRomType()) {
            case Gen3Constants.RomType_Ruby:
            case Gen3Constants.RomType_Sapp:
                // TODO: look at Blastoise, Caterpie, Kadabra, Deoxys.
                // otherwise all palettes are pretty much identical (in use).
                return "E";
            case Gen3Constants.RomType_Em:
                return "E";
            case Gen3Constants.RomType_FRLG:
                return "FRLG";
            default:
                return null;
        }
    }

    @Override
    public Set<Item> getAllConsumableHeldItems() {
        return itemIdsToSet(Gen3Constants.consumableHeldItems);
    }

    @Override
    public List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        List<Integer> ids = new ArrayList<>(Gen3Constants.generalPurposeConsumableItems);
        if (!consumableOnly) {
            ids.addAll(Gen3Constants.generalPurposeItems);
        }
        for (int moveIdx : pokeMoves) {
            Move move = moves.get(moveIdx);
            if (move == null) {
                continue;
            }
            if (GBConstants.physicalTypes.contains(move.type) && move.power > 0) {
                ids.add(ItemIDs.liechiBerry);
                if (!consumableOnly) {
                    ids.addAll(Gen3Constants.typeBoostingItems.get(move.type));
                    ids.add(ItemIDs.choiceBand);
                }
            }
            if (!GBConstants.physicalTypes.contains(move.type) && move.power > 0) {
                ids.add(ItemIDs.petayaBerry);
                if (!consumableOnly) {
                    ids.addAll(Gen3Constants.typeBoostingItems.get(move.type));
                }
            }
        }
        if (!consumableOnly) {
            List<Integer> speciesItems = Gen3Constants.speciesBoostingItems.get(tp.getSpecies().getNumber());
            if (speciesItems != null) {
                for (int i = 0; i < 6; i++) {  // Increase the likelihood of using species specific items.
                    ids.addAll(speciesItems);
                }
            }
        }
        return ids.stream().map(items::get).collect(Collectors.toList());
    }

    @Override
    public Gen3RomEntry getRomEntry() {
        return romEntry;
    }
}
