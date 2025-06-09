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
import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.MiscTweak;
import com.dabomstew.pkromio.RomFunctions;
import com.dabomstew.pkromio.constants.*;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.*;
import com.dabomstew.pkromio.graphics.palettes.Palette;
import com.dabomstew.pkromio.newnds.NARCArchive;
import com.dabomstew.pkromio.romhandlers.romentries.DSStaticPokemon;
import com.dabomstew.pkromio.romhandlers.romentries.Gen5RomEntry;
import com.dabomstew.pkromio.romhandlers.romentries.InFileEntry;
import compressors.DSDecmp;
import pptxt.PPTxtHandler;

import javax.naming.OperationNotSupportedException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link RomHandler} for Black, White, Black 2, White 2.
 */
public class Gen5RomHandler extends AbstractDSRomHandler {

    public static class Factory extends RomHandler.Factory {

        @Override
        public Gen5RomHandler create() {
            return new Gen5RomHandler();
        }

        public boolean isLoadable(String filename) {
            return detectNDSRomInner(getROMCodeFromFile(filename), getVersionFromFile(filename));
        }
    }

    private static List<Gen5RomEntry> roms;

    static {
        loadROMInfo();
    }

    private static void loadROMInfo() {
        try {
            roms = Gen5RomEntry.READER.readEntriesFromFile("gen5_offsets.ini");
        } catch (IOException e) {
            throw new RuntimeException("Could not read Rom Entries.", e);
        }
    }

    // This ROM
    private Species[] pokes;
    private final Map<Integer, FormeInfo> formeMappings = new TreeMap<>();
    private final Map<Species, Integer> formeGraphicsIndices = new HashMap<>();
    private final Map<Species, Integer> graphicalFormeCounts = new TreeMap<>(); // temporary until the form rewrite
    private List<Species> speciesList;
    private List<Species> speciesListInclFormes;
    private Move[] moves;
    private List<Item> items;
    private Gen5RomEntry romEntry;
    private byte[] arm9;
    private List<String> abilityNames;
    private List<String> shopNames;
    private boolean loadedWildMapNames;
    private Map<Integer, String> wildMapNames;
    private int hiddenHollowCount = 0;
    private boolean hiddenHollowCounted = false;
    private final List<Integer> originalDoubleTrainers = new ArrayList<>();
    private int pickupItemsTableOffset;
    private TypeTable typeTable;
    private long actualArm9CRC32;
    private Map<Integer, Long> actualOverlayCRC32s;
    private Map<String, Long> actualFileCRC32s;
    
    private NARCArchive pokeNarc, moveNarc, stringsNarc, storyTextNarc, scriptNarc, shopNarc;

    @Override
    protected int getARM9Offset() {
        return Gen5Constants.arm9Offset;
    }

    @Override
    protected boolean detectNDSRom(String ndsCode, byte version) {
        return detectNDSRomInner(ndsCode, version);
    }

    private static boolean detectNDSRomInner(String ndsCode, byte version) {
        return entryFor(ndsCode, version) != null;
    }

    private static Gen5RomEntry entryFor(String ndsCode, byte version) {
        if (ndsCode == null) {
            return null;
        }

        for (Gen5RomEntry re : roms) {
            if (ndsCode.equals(re.getRomCode()) && re.getVersion() == version) {
                return re;
            }
        }
        return null;
    }

    @Override
    protected void loadedROM(String romCode, byte version) {
        this.romEntry = entryFor(romCode, version);
        try {
            arm9 = readARM9();
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        try {
            stringsNarc = readNARC(romEntry.getFile("TextStrings"));
            storyTextNarc = readNARC(romEntry.getFile("TextStory"));
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        try {
            scriptNarc = readNARC(romEntry.getFile("Scripts"));
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            try {
                shopNarc = readNARC(romEntry.getFile("ShopItems"));
            } catch (IOException e) {
                throw new RomIOException(e);
            }
        }
        loadItems();
        loadPokemonStats();
        speciesListInclFormes = Arrays.asList(pokes);
        speciesList = Arrays.asList(Arrays.copyOfRange(pokes,0,Gen5Constants.pokemonCount + 1));
        loadMoves();
        loadPokemonPalettes();

        abilityNames = getStrings(false, romEntry.getIntValue("AbilityNamesTextOffset"));
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            shopNames = Gen5Constants.bw1ShopNames;
        }
        else if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            shopNames = Gen5Constants.bw2ShopNames;
        }
        
        loadedWildMapNames = false;

        try {
            computeCRC32sForRom();
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        // If there are tweaks for expanding the ARM9, do it here to keep it simple.
        boolean shouldExtendARM9 = romEntry.hasTweakFile("ShedinjaEvolutionTweak") || romEntry.hasTweakFile("NewIndexToMusicTweak");
        if (shouldExtendARM9) {
            int extendBy = romEntry.getIntValue("Arm9ExtensionSize");
            byte[] prefix = RomFunctions.hexToBytes(romEntry.getStringValue("TCMCopyingPrefix"));
            arm9 = extendARM9(arm9, extendBy, prefix);
        }
    }

    private void loadItems() {
        items = new ArrayList<>();
        items.add(null);
        List<String> names = getStrings(false, romEntry.getIntValue("ItemNamesTextOffset"));
        for (int i = 1; i < names.size(); i++) {
            items.add(new Item(i, names.get(i)));
        }

        // TODO: would some other system be better here; e.g. something similar to "tagTrainers"
        for (int id : Gen5Constants.bannedItems) {
            if (id < items.size()) {
                items.get(id).setAllowed(false);
            }
        }
        for (int i = ItemIDs.tm01; i <= ItemIDs.tm92; i++) {
            items.get(i).setTM(true);
        }
        for (int i = ItemIDs.tm93; i <= ItemIDs.tm95; i++) {
            items.get(i).setTM(true);
        }
        for (int id : Gen5Constants.getBadItems(romEntry.getRomType())) {
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
    public void loadPokemonStats() {
        try {
            pokeNarc = this.readNARC(romEntry.getFile("PokemonStats"));
            String[] pokeNames = readPokemonNames();
            int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
            pokes = new Species[Gen5Constants.pokemonCount + formeCount + 1];
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                pokes[i] = new Species(i);
                loadBasicPokeStats(pokes[i], pokeNarc.files.get(i), formeMappings);
                pokes[i].setName(pokeNames[i]);
                pokes[i].setGeneration(generationOf(pokes[i]));
            }

            int i = Gen5Constants.pokemonCount + 1;
            for (int k: formeMappings.keySet()) {
                pokes[i] = new Species(i);
                loadBasicPokeStats(pokes[i], pokeNarc.files.get(k), formeMappings);
                FormeInfo fi = formeMappings.get(k);
                pokes[i].setName(pokeNames[fi.baseForme]);
                pokes[i].setBaseForme(pokes[fi.baseForme]);
                pokes[i].setFormeNumber(fi.formeNumber);
                pokes[i].setFormeSuffix(Gen5Constants.getFormeSuffixByBaseForme(fi.baseForme, fi.formeNumber));
                pokes[i].setGeneration(generationOf(pokes[i]));
                i = i + 1;
            }
            populateEvolutions();
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private int generationOf(Species pk) {
        if (!pk.isBaseForme()) {
            return pk.getBaseForme().getGeneration();
        }
        if (pk.getNumber() >= SpeciesIDs.victini) {
            return 5;
        } else if (pk.getNumber() >= SpeciesIDs.turtwig) {
            return 4;
        } else if (pk.getNumber() >= SpeciesIDs.treecko) {
            return 3;
        } else if (pk.getNumber() >= SpeciesIDs.chikorita) {
            return 2;
        }
        return 1;
    }

    private void loadMoves() {
        try {
            moveNarc = this.readNARC(romEntry.getFile("MoveData"));
            moves = new Move[Gen5Constants.moveCount + 1];
            List<String> moveNames = getStrings(false, romEntry.getIntValue("MoveNamesTextOffset"));
            for (int i = 1; i <= Gen5Constants.moveCount; i++) {
                byte[] moveData = moveNarc.files.get(i);
                moves[i] = new Move();
                moves[i].name = moveNames.get(i);
                moves[i].number = i;
                moves[i].internalId = i;
                moves[i].effectIndex = readWord(moveData, 16);
                moves[i].hitratio = (moveData[4] & 0xFF);
                moves[i].power = moveData[3] & 0xFF;
                moves[i].pp = moveData[5] & 0xFF;
                moves[i].type = Gen5Constants.typeTable[moveData[0] & 0xFF];
                moves[i].flinchPercentChance = moveData[15] & 0xFF;
                moves[i].target = moveData[20] & 0xFF;
                moves[i].category = Gen5Constants.moveCategoryIndices[moveData[2] & 0xFF];
                moves[i].priority = moveData[6];

                int critStages = moveData[14] & 0xFF;
                if (critStages == 6) {
                    moves[i].criticalChance = CriticalChance.GUARANTEED;
                } else if (critStages > 0) {
                    moves[i].criticalChance = CriticalChance.INCREASED;
                }

                int internalStatusType = readWord(moveData, 8);
                int flags = FileFunctions.readFullInt(moveData, 32);
                moves[i].makesContact = (flags & 0x001) != 0;
                moves[i].isChargeMove = (flags & 0x002) != 0;
                moves[i].isRechargeMove = (flags & 0x004) != 0;
                moves[i].isPunchMove = (flags & 0x080) != 0;
                moves[i].isSoundMove = (flags & 0x100) != 0;
                moves[i].isTrapMove = (moves[i].effectIndex == Gen5Constants.trappingEffect || internalStatusType == 8);
                int qualities = moveData[1];
                int recoilOrAbsorbPercent = moveData[18];
                if (qualities == Gen5Constants.damageAbsorbQuality) {
                    moves[i].absorbPercent = recoilOrAbsorbPercent;
                } else {
                    moves[i].recoilPercent = -recoilOrAbsorbPercent;
                }

                if (i == MoveIDs.swift) {
                    perfectAccuracy = (int)moves[i].hitratio;
                }

                if (GlobalConstants.normalMultihitMoves.contains(i)) {
                    moves[i].hitCount = 19 / 6.0;
                } else if (GlobalConstants.doubleHitMoves.contains(i)) {
                    moves[i].hitCount = 2;
                } else if (i == MoveIDs.tripleKick) {
                    moves[i].hitCount = 2.71; // this assumes the first hit lands
                }

                switch (qualities) {
                    case Gen5Constants.noDamageStatChangeQuality:
                    case Gen5Constants.noDamageStatusAndStatChangeQuality:
                        // All Allies or Self
                        if (moves[i].target == 6 || moves[i].target == 7) {
                            moves[i].statChangeMoveType = StatChangeMoveType.NO_DAMAGE_USER;
                        } else {
                            moves[i].statChangeMoveType = StatChangeMoveType.NO_DAMAGE_TARGET;
                        }
                        break;
                    case Gen5Constants.damageTargetDebuffQuality:
                        moves[i].statChangeMoveType = StatChangeMoveType.DAMAGE_TARGET;
                        break;
                    case Gen5Constants.damageUserBuffQuality:
                        moves[i].statChangeMoveType = StatChangeMoveType.DAMAGE_USER;
                        break;
                    default:
                        moves[i].statChangeMoveType = StatChangeMoveType.NONE_OR_UNKNOWN;
                        break;
                }

                for (int statChange = 0; statChange < 3; statChange++) {
                    moves[i].statChanges[statChange].type = StatChangeType.values()[moveData[21 + statChange]];
                    moves[i].statChanges[statChange].stages = moveData[24 + statChange];
                    moves[i].statChanges[statChange].percentChance = moveData[27 + statChange];
                }

                // Exclude status types that aren't in the StatusType enum.
                if (internalStatusType < 7) {
                    moves[i].statusType = StatusType.values()[internalStatusType];
                    if (moves[i].statusType == StatusType.POISON && (i == MoveIDs.toxic || i == MoveIDs.poisonFang)) {
                        moves[i].statusType = StatusType.TOXIC_POISON;
                    }
                    moves[i].statusPercentChance = moveData[10] & 0xFF;
                    if (moves[i].number == MoveIDs.chatter) {
                        moves[i].statusPercentChance = 1.0;
                    }
                    switch (qualities) {
                        case Gen5Constants.noDamageStatusQuality:
                        case Gen5Constants.noDamageStatusAndStatChangeQuality:
                            moves[i].statusMoveType = StatusMoveType.NO_DAMAGE;
                            break;
                        case Gen5Constants.damageStatusQuality:
                            moves[i].statusMoveType = StatusMoveType.DAMAGE;
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }

    }

    private void loadBasicPokeStats(Species pkmn, byte[] stats, Map<Integer,FormeInfo> altFormes) {
        pkmn.setHp(stats[Gen5Constants.bsHPOffset] & 0xFF);
        pkmn.setAttack(stats[Gen5Constants.bsAttackOffset] & 0xFF);
        pkmn.setDefense(stats[Gen5Constants.bsDefenseOffset] & 0xFF);
        pkmn.setSpeed(stats[Gen5Constants.bsSpeedOffset] & 0xFF);
        pkmn.setSpatk(stats[Gen5Constants.bsSpAtkOffset] & 0xFF);
        pkmn.setSpdef(stats[Gen5Constants.bsSpDefOffset] & 0xFF);
        // Type
        pkmn.setPrimaryType(Gen5Constants.typeTable[stats[Gen5Constants.bsPrimaryTypeOffset] & 0xFF]);
        pkmn.setSecondaryType(Gen5Constants.typeTable[stats[Gen5Constants.bsSecondaryTypeOffset] & 0xFF]);
        // Only one type?
        if (pkmn.getSecondaryType(false) == pkmn.getPrimaryType(false)) {
            pkmn.setSecondaryType(null);
        }
        pkmn.setCatchRate(stats[Gen5Constants.bsCatchRateOffset] & 0xFF);
        pkmn.setGrowthCurve(ExpCurve.fromByte(stats[Gen5Constants.bsGrowthCurveOffset]));

        pkmn.setAbility1(stats[Gen5Constants.bsAbility1Offset] & 0xFF);
        pkmn.setAbility2(stats[Gen5Constants.bsAbility2Offset] & 0xFF);
        pkmn.setAbility3(stats[Gen5Constants.bsAbility3Offset] & 0xFF);

        // Held Items?
        Item item1 = items.get(readWord(stats, Gen5Constants.bsCommonHeldItemOffset));
        Item item2 = items.get(readWord(stats, Gen5Constants.bsRareHeldItemOffset));

        if (Objects.equals(item1, item2)) {
            // guaranteed
            pkmn.setGuaranteedHeldItem(item1);
        } else {
            pkmn.setCommonHeldItem(item1);
            pkmn.setRareHeldItem(item2);
            Item dgItem = items.get(readWord(stats, Gen5Constants.bsDarkGrassHeldItemOffset));
            pkmn.setDarkGrassHeldItem(dgItem);
        }

        pkmn.setGenderRatio(stats[Gen5Constants.bsGenderRatioOffset] & 0xFF);

        int formeCount = stats[Gen5Constants.bsFormeCountOffset] & 0xFF;
        if (formeCount > 1) {
            graphicalFormeCounts.put(pkmn, formeCount);
            formeGraphicsIndices.put(pkmn, romEntry.getIntValue("FormeGraphicsOffset") + readWord(stats, Gen5Constants.bsFormeSpriteOffset));
            int firstFormeOffset = readWord(stats, Gen5Constants.bsFormeOffset);
            if (firstFormeOffset != 0) {
                for (int i = 1; i < formeCount; i++) {
                    altFormes.put(firstFormeOffset + i - 1,new FormeInfo(pkmn.getNumber(),i)); // Assumes that formes are in memory in the same order as their numbers
                    if (pkmn.getNumber() == SpeciesIDs.keldeo) {
                        pkmn.setCosmeticForms(formeCount);
                    }
                }
            } else {
                if (pkmn.getNumber() != SpeciesIDs.cherrim && pkmn.getNumber() != SpeciesIDs.arceus && pkmn.getNumber() != SpeciesIDs.deerling && pkmn.getNumber() != SpeciesIDs.sawsbuck && pkmn.getNumber() < SpeciesIDs.genesect) {
                    // Reason for exclusions:
                    // Cherrim/Arceus/Genesect: to avoid confusion
                    // Deerling/Sawsbuck: handled automatically in gen 5
                    pkmn.setCosmeticForms(formeCount);
                }
                if (pkmn.getNumber() == SpeciesIDs.Gen5Formes.keldeoCosmetic1) {
                    pkmn.setActuallyCosmetic(true);
                }
            }
        }
    }

    private String[] readPokemonNames() {
        String[] pokeNames = new String[Gen5Constants.pokemonCount + 1];
        List<String> nameList = getStrings(false, romEntry.getIntValue("PokemonNamesTextOffset"));
        for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
            pokeNames[i] = nameList.get(i);
        }
        return pokeNames;
    }

    @Override
    protected void prepareSaveRom() {
        super.prepareSaveRom();
        try {
            writeARM9(arm9);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        try {
            writeNARC(romEntry.getFile("TextStrings"), stringsNarc);
            writeNARC(romEntry.getFile("TextStory"), storyTextNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        try {
            writeNARC(romEntry.getFile("Scripts"), scriptNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public void saveMoves() {
        for (int i = 1; i <= Gen5Constants.moveCount; i++) {
            byte[] data = moveNarc.files.get(i);
            data[2] = Gen5Constants.moveCategoryToByte(moves[i].category);
            data[3] = (byte) moves[i].power;
            data[0] = Gen5Constants.typeToByte(moves[i].type);
            int hitratio = (int) Math.round(moves[i].hitratio);
            if (hitratio < 0) {
                hitratio = 0;
            }
            if (hitratio > 101) {
                hitratio = 100;
            }
            data[4] = (byte) hitratio;
            data[5] = (byte) moves[i].pp;
        }

        try {
            this.writeNARC(romEntry.getFile("MoveData"), moveNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }

    }

    @Override
    public void savePokemonStats() {
        List<String> nameList = getStrings(false, romEntry.getIntValue("PokemonNamesTextOffset"));

        int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
        int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
        for (int i = 1; i <= Gen5Constants.pokemonCount + formeCount; i++) {
            if (i > Gen5Constants.pokemonCount) {
                saveBasicPokeStats(pokes[i], pokeNarc.files.get(i + formeOffset));
                continue;
            }
            saveBasicPokeStats(pokes[i], pokeNarc.files.get(i));
            nameList.set(i, pokes[i].getName());
        }

        setStrings(false, romEntry.getIntValue("PokemonNamesTextOffset"), nameList);

        try {
            this.writeNARC(romEntry.getFile("PokemonStats"), pokeNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        writeEvolutions();
    }

    private void saveBasicPokeStats(Species pkmn, byte[] stats) {
        stats[Gen5Constants.bsHPOffset] = (byte) pkmn.getHp();
        stats[Gen5Constants.bsAttackOffset] = (byte) pkmn.getAttack();
        stats[Gen5Constants.bsDefenseOffset] = (byte) pkmn.getDefense();
        stats[Gen5Constants.bsSpeedOffset] = (byte) pkmn.getSpeed();
        stats[Gen5Constants.bsSpAtkOffset] = (byte) pkmn.getSpatk();
        stats[Gen5Constants.bsSpDefOffset] = (byte) pkmn.getSpdef();
        stats[Gen5Constants.bsPrimaryTypeOffset] = Gen5Constants.typeToByte(pkmn.getPrimaryType(false));
        if (pkmn.getSecondaryType(false) == null) {
            stats[Gen5Constants.bsSecondaryTypeOffset] = stats[Gen5Constants.bsPrimaryTypeOffset];
        } else {
            stats[Gen5Constants.bsSecondaryTypeOffset] = Gen5Constants.typeToByte(pkmn.getSecondaryType(false));
        }
        stats[Gen5Constants.bsCatchRateOffset] = (byte) pkmn.getCatchRate();
        stats[Gen5Constants.bsGrowthCurveOffset] = pkmn.getGrowthCurve().toByte();

        stats[Gen5Constants.bsAbility1Offset] = (byte) pkmn.getAbility1();
        stats[Gen5Constants.bsAbility2Offset] = (byte) pkmn.getAbility2();
        stats[Gen5Constants.bsAbility3Offset] = (byte) pkmn.getAbility3();

        // Held items
        if (pkmn.getGuaranteedHeldItem() != null) {
            writeWord(stats, Gen5Constants.bsCommonHeldItemOffset, pkmn.getGuaranteedHeldItem().getId());
            writeWord(stats, Gen5Constants.bsRareHeldItemOffset, pkmn.getGuaranteedHeldItem().getId());
            writeWord(stats, Gen5Constants.bsDarkGrassHeldItemOffset, 0);
        } else {
            writeWord(stats, Gen5Constants.bsCommonHeldItemOffset,
                    pkmn.getCommonHeldItem() == null ? 0 : pkmn.getCommonHeldItem().getId());
            writeWord(stats, Gen5Constants.bsRareHeldItemOffset,
                    pkmn.getRareHeldItem() == null ? 0 : pkmn.getRareHeldItem().getId());
            writeWord(stats, Gen5Constants.bsDarkGrassHeldItemOffset,
                    pkmn.getDarkGrassHeldItem() == null ? 0 : pkmn.getDarkGrassHeldItem().getId());
        }
    }

    @Override
    public List<Species> getSpecies() {
        return speciesList;
    }

    @Override
    public List<Species> getSpeciesInclFormes() {
        return speciesListInclFormes;
    }

	@Override
	public SpeciesSet getAltFormes() {
		int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
		return new SpeciesSet(speciesListInclFormes.subList(Gen5Constants.pokemonCount + 1,
				Gen5Constants.pokemonCount + formeCount + 1));
	}

    @Override
    public List<MegaEvolution> getMegaEvolutions() {
        return new ArrayList<>();
    }

    @Override
    public Species getAltFormeOfSpecies(Species base, int forme) {
        int pokeNum = Gen5Constants.getAbsolutePokeNumByBaseForme(base.getNumber(),forme);
        return pokeNum != 0 ? pokes[pokeNum] : base;
    }

	@Override
	public SpeciesSet getIrregularFormes() {
		return Gen5Constants.getIrregularFormes(romEntry.getRomType())
				.stream().map(i -> pokes[i])
				.collect(Collectors.toCollection(SpeciesSet::new));
	}

    @Override
    public boolean hasFunctionalFormes() {
        return true;
    }

    @Override
    public List<Species> getStarters() {
        NARCArchive scriptNARC = scriptNarc;
        List<Species> starters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            InFileEntry[] thisStarter = romEntry.getOffsetArrayEntry("StarterOffsets" + (i + 1));
            starters.add(pokes[readWord(scriptNARC.files.get(thisStarter[0].getFile()), thisStarter[0].getOffset())]);
        }
        return starters;
    }

    @Override
    public boolean setStarters(List<Species> newStarters) {
        if (newStarters.size() != 3) {
            return false;
        }

        // Fix up starter offsets
        try {
            NARCArchive scriptNARC = scriptNarc;
            for (int i = 0; i < 3; i++) {
                int starter = newStarters.get(i).getNumber();
                InFileEntry[] thisStarter = romEntry.getOffsetArrayEntry("StarterOffsets" + (i + 1));
                for (InFileEntry entry : thisStarter) {
                    writeWord(scriptNARC.files.get(entry.getFile()), entry.getOffset(), starter);
                }
            }
            // GIVE ME BACK MY PURRLOIN
            if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
                byte[] newScript = Gen5Constants.bw2NewStarterScript;
                byte[] oldFile = scriptNARC.files.get(romEntry.getIntValue("PokedexGivenFileOffset"));
                byte[] newFile = new byte[oldFile.length + newScript.length];
                int offset = find(oldFile, Gen5Constants.bw2StarterScriptMagic);
                if (offset > 0) {
                    System.arraycopy(oldFile, 0, newFile, 0, oldFile.length);
                    System.arraycopy(newScript, 0, newFile, oldFile.length, newScript.length);
                    if (romEntry.getRomCode().charAt(3) == 'J') {
                        newFile[oldFile.length + 0x6] -= 4;
                    }
                    newFile[offset++] = 0x1E;
                    newFile[offset++] = 0x0;
                    writeRelativePointer(newFile, offset, oldFile.length);
                    scriptNARC.files.set(romEntry.getIntValue("PokedexGivenFileOffset"), newFile);
                }
            } else {
                byte[] newScript = Gen5Constants.bw1NewStarterScript;

                byte[] oldFile = scriptNARC.files.get(romEntry.getIntValue("PokedexGivenFileOffset"));
                byte[] newFile = new byte[oldFile.length + newScript.length];
                int offset = find(oldFile, Gen5Constants.bw1StarterScriptMagic);
                if (offset > 0) {
                    System.arraycopy(oldFile, 0, newFile, 0, oldFile.length);
                    System.arraycopy(newScript, 0, newFile, oldFile.length, newScript.length);
                    if (romEntry.getRomCode().charAt(3) == 'J') {
                        newFile[oldFile.length + 0x4] -= 4;
                        newFile[oldFile.length + 0x8] -= 4;
                    }
                    newFile[offset++] = 0x04;
                    newFile[offset++] = 0x0;
                    writeRelativePointer(newFile, offset, oldFile.length);
                    scriptNARC.files.set(romEntry.getIntValue("PokedexGivenFileOffset"), newFile);
                }
            }

            // Starter sprites
            NARCArchive starterNARC = this.readNARC(romEntry.getFile("StarterGraphics"));
            NARCArchive pokespritesNARC = this.readNARC(romEntry.getFile("PokemonGraphics"));
            replaceStarterFiles(starterNARC, pokespritesNARC, 0, newStarters.get(0).getNumber());
            replaceStarterFiles(starterNARC, pokespritesNARC, 1, newStarters.get(1).getNumber());
            replaceStarterFiles(starterNARC, pokespritesNARC, 2, newStarters.get(2).getNumber());
            writeNARC(romEntry.getFile("StarterGraphics"), starterNARC);

            // Starter cries
            byte[] starterCryOverlay = this.readOverlay(romEntry.getIntValue("StarterCryOvlNumber"));
            String starterCryTablePrefix = romEntry.getStringValue("StarterCryTablePrefix");
            int offset = find(starterCryOverlay, starterCryTablePrefix);
            if (offset > 0) {
                offset += starterCryTablePrefix.length() / 2; // because it was a prefix
                for (Species newStarter : newStarters) {
                    writeWord(starterCryOverlay, offset, newStarter.getNumber());
                    offset += 2;
                }
                this.writeOverlay(romEntry.getIntValue("StarterCryOvlNumber"), starterCryOverlay);
            }
        } catch (IOException  ex) {
            throw new RomIOException(ex);
        }
        // Fix text depending on version
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            List<String> yourHouseStrings = getStrings(true, romEntry.getIntValue("StarterLocationTextOffset"));
            for (int i = 0; i < 3; i++) {
                yourHouseStrings.set(Gen5Constants.bw1StarterTextOffset - i,
                        "\\xF000\\xBD02\\x0000The " + newStarters.get(i).getPrimaryType(false).camelCase()
                                + "-type Pok\\x00E9mon\\xFFFE\\xF000\\xBD02\\x0000" + newStarters.get(i).getName());
            }
            // Update what the friends say
            yourHouseStrings
                    .set(Gen5Constants.bw1CherenText1Offset,
                            "Cheren: Hey, how come you get to pick\\xFFFEout my Pok\\x00E9mon?"
                                    + "\\xF000\\xBE01\\x0000\\xFFFEOh, never mind. I wanted this one\\xFFFEfrom the start, anyway."
                                    + "\\xF000\\xBE01\\x0000");
            yourHouseStrings.set(Gen5Constants.bw1CherenText2Offset,
                    "It's decided. You'll be my opponent...\\xFFFEin our first Pok\\x00E9mon battle!"
                            + "\\xF000\\xBE01\\x0000\\xFFFELet's see what you can do, \\xFFFEmy Pok\\x00E9mon!"
                            + "\\xF000\\xBE01\\x0000");

            // rewrite
            setStrings(true, romEntry.getIntValue("StarterLocationTextOffset"), yourHouseStrings);
        } else {
            List<String> starterTownStrings = getStrings(true, romEntry.getIntValue("StarterLocationTextOffset"));
            for (int i = 0; i < 3; i++) {
                starterTownStrings.set(Gen5Constants.bw2StarterTextOffset - i, "\\xF000\\xBD02\\x0000The "
                        + newStarters.get(i).getPrimaryType(false).camelCase()
                        + "-type Pok\\x00E9mon\\xFFFE\\xF000\\xBD02\\x0000" + newStarters.get(i).getName());
            }
            // Update what the rival says
            starterTownStrings.set(Gen5Constants.bw2RivalTextOffset,
                    "\\xF000\\x0100\\x0001\\x0001: Let's see how good\\xFFFEa Trainer you are!"
                            + "\\xF000\\xBE01\\x0000\\xFFFEI'll use my Pok\\x00E9mon"
                            + "\\xFFFEthat I raised from an Egg!\\xF000\\xBE01\\x0000");

            // rewrite
            setStrings(true, romEntry.getIntValue("StarterLocationTextOffset"), starterTownStrings);
        }
        return true;
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
        return false;
    }

    @Override
    public List<Item> getStarterHeldItems() {
        // do nothing
        return new ArrayList<>();
    }

    @Override
    public void setStarterHeldItems(List<Item> items) {
        // do nothing
    }

    private void replaceStarterFiles(NARCArchive starterNARC, NARCArchive pokespritesNARC, int starterIndex,
            int pokeNumber) {
        starterNARC.files.set(starterIndex * 2, pokespritesNARC.files.get(pokeNumber * 20 + 18));
        // Get the picture...
        byte[] compressedPic = pokespritesNARC.files.get(pokeNumber * 20);
        // Decompress it with JavaDSDecmp
        byte[] uncompressedPic = DSDecmp.Decompress(compressedPic);
        starterNARC.files.set(12 + starterIndex, uncompressedPic);
    }

    @Override
    public List<Move> getMoves() {
        return Arrays.asList(moves);
    }

    @Override
    public List<EncounterArea> getEncounters(boolean useTimeOfDay) {
        if (!loadedWildMapNames) {
            loadWildMapNames();
        }
        try {
            NARCArchive encounterNARC = readNARC(romEntry.getFile("WildPokemon"));
            List<EncounterArea> encounterAreas = new ArrayList<>();
            int idx = -1;
            for (byte[] entry : encounterNARC.files) {
                idx++;
                if (entry.length > Gen5Constants.perSeasonEncounterDataLength && useTimeOfDay) {
                    for (int i = 0; i < 4; i++) {
                        processEncounterEntry(encounterAreas, entry, i * Gen5Constants.perSeasonEncounterDataLength, idx);
                    }
                } else {
                    processEncounterEntry(encounterAreas, entry, 0, idx);
                }
            }

            Gen5Constants.tagEncounterAreas(encounterAreas, romEntry.getRomType(), useTimeOfDay);
            return encounterAreas;
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void processEncounterEntry(List<EncounterArea> encounterAreas, byte[] entry, int startOffset, int idx) {

        if (!wildMapNames.containsKey(idx)) {
            wildMapNames.put(idx, "? Unknown ?");
        }
        String mapName = wildMapNames.get(idx);

        int[] amounts = Gen5Constants.encountersOfEachType;

        int offset = 8;
        for (int i = 0; i < 7; i++) {
            int rate = entry[startOffset + i] & 0xFF;
            if (rate != 0) {
                List<Encounter> encounters = readEncounters(entry, startOffset + offset, amounts[i]);
                EncounterArea area = new EncounterArea(encounters);
                area.setRate(rate);
                area.setIdentifiers(mapName + " " + Gen5Constants.encounterTypeNames[i], idx,
                        Gen5Constants.encounterTypeValues[i]);
                encounterAreas.add(area);
            }
            offset += amounts[i] * 4;
        }

    }

    private List<Encounter> readEncounters(byte[] data, int offset, int number) {
        List<Encounter> encounters = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Encounter enc = new Encounter();
            int species = readWord(data, offset + i * 4) & 0x7FF;
            int forme = readWord(data, offset + i * 4) >> 11;
            Species baseForme = pokes[species];
            if (forme <= baseForme.getCosmeticForms() || forme == 30 || forme == 31) {
                enc.setSpecies(pokes[species]);
            } else {
                int speciesWithForme = Gen5Constants.getAbsolutePokeNumByBaseForme(species,forme);
                if (speciesWithForme == 0) {
                    enc.setSpecies(pokes[species]); // Failsafe
                } else {
                    enc.setSpecies(pokes[speciesWithForme]);
                }
            }
            enc.setFormeNumber(forme);
            enc.setLevel(data[offset + 2 + i * 4] & 0xFF);
            enc.setMaxLevel(data[offset + 3 + i * 4] & 0xFF);
            encounters.add(enc);
        }
        return encounters;
    }

    @Override
    public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
        List<String> locationTagsTraverseOrder = Gen5Constants.getLocationTagsTraverseOrder(getROMType());
        return getEncounters(useTimeOfDay).stream()
                .sorted(Comparator.comparingInt(a -> locationTagsTraverseOrder.indexOf(a.getLocationTag())))
                .collect(Collectors.toList());
    }

    @Override
    public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas) {
        try {
            NARCArchive encounterNARC = readNARC(romEntry.getFile("WildPokemon"));
            Iterator<EncounterArea> areaIterator = encounterAreas.iterator();
            for (byte[] entry : encounterNARC.files) {
                writeEncounterEntry(areaIterator, entry, 0);
                if (entry.length > 232) {
                    if (useTimeOfDay) {
                        for (int i = 1; i < 4; i++) {
                            writeEncounterEntry(areaIterator, entry, i * 232);
                        }
                    } else {
                        // copy for other 3 seasons
                        System.arraycopy(entry, 0, entry, 232, 232);
                        System.arraycopy(entry, 0, entry, 464, 232);
                        System.arraycopy(entry, 0, entry, 696, 232);
                    }
                }
            }

            // Save
            writeNARC(romEntry.getFile("WildPokemon"), encounterNARC);

            this.updatePokedexAreaData(encounterNARC);

            // Habitat List
            // disabled: habitat list changes cause a crash if too many entries for now.
//            if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
//                 NARCArchive habitatNARC = readNARC(romEntry.getFile("HabitatList"));
//                 for (int i = 0; i < habitatNARC.files.size(); i++) {
//                 byte[] oldEntry = habitatNARC.files.get(i);
//                 int[] encounterFiles = habitatListEntries[i];
//                 Map<Pokemon, byte[]> pokemonHere = new TreeMap<Pokemon, byte[]>();
//                 for (int encFile : encounterFiles) {
//                 byte[] encEntry = encounterNARC.files.get(encFile);
//                 if (encEntry.length > 232) {
//                 for (int s = 0; s < 4; s++) {
//                 addHabitats(encEntry, s * 232, pokemonHere, s);
//                 }
//                 } else {
//                 for (int s = 0; s < 4; s++) {
//                 addHabitats(encEntry, 0, pokemonHere, s);
//                 }
//                 }
//                 }
//                 // Make the new file
//                 byte[] habitatEntry = new byte[10 + pokemonHere.size() * 28];
//                 System.arraycopy(oldEntry, 0, habitatEntry, 0, 10);
//                 habitatEntry[8] = (byte) pokemonHere.size();
//                 // 28-byte entries for each pokemon
//                 int num = -1;
//                 for (Pokemon pkmn : pokemonHere.keySet()) {
//                 num++;
//                 writeWord(habitatEntry, 10 + num * 28, pkmn.getNumber());
//                 byte[] slots = pokemonHere.get(pkmn);
//                 System.arraycopy(slots, 0, habitatEntry, 12 + num * 28,
//                 12);
//                 }
//                 // Save
//                 habitatNARC.files.set(i, habitatEntry);
//                 }
//                 // Save habitat
//                 this.writeNARC(romEntry.getFile("HabitatList"),
//                 habitatNARC);
//            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }

    }

    private void updatePokedexAreaData(NARCArchive encounterNARC) throws IOException {
        NARCArchive areaNARC = this.readNARC(romEntry.getFile("PokedexAreaData"));
        int areaDataEntryLength = Gen5Constants.getAreaDataEntryLength(romEntry.getRomType());
        int encounterAreaCount = Gen5Constants.getEncounterAreaCount(romEntry.getRomType());
        List<byte[]> newFiles = new ArrayList<>();
        for (int i = 0; i < Gen5Constants.pokemonCount; i++) {
            byte[] nf = new byte[areaDataEntryLength];
            newFiles.add(nf);
        }
        // Get data now
        for (int i = 0; i < encounterNARC.files.size(); i++) {
            byte[] encEntry = encounterNARC.files.get(i);
            if (encEntry.length > Gen5Constants.perSeasonEncounterDataLength) {
                for (int season = 0; season < 4; season++) {
                    updateAreaDataFromEncounterEntry(encEntry, season * Gen5Constants.perSeasonEncounterDataLength, newFiles, season, i);
                }
            } else {
                for (int season = 0; season < 4; season++) {
                    updateAreaDataFromEncounterEntry(encEntry, 0, newFiles, season, i);
                }
            }
        }
        // Now update unobtainables, check for seasonal-dependent entries, & save
        for (int i = 0; i < Gen5Constants.pokemonCount; i++) {
            byte[] file = newFiles.get(i);
            for (int season = 0; season < 4; season++) {
                boolean unobtainable = true;
                for (int enc = 0; enc < encounterAreaCount; enc++) {
                    if (file[season * (encounterAreaCount + 1) + enc + 2] != 0) {
                        unobtainable = false;
                        break;
                    }
                }
                if (unobtainable) {
                    file[season * (encounterAreaCount + 1) + 1] = 1;
                }
            }
            boolean seasonalDependent = false;
            for (int enc = 0; enc < encounterAreaCount; enc++) {
                byte springEnc = file[enc + 2];
                byte summerEnc = file[(encounterAreaCount + 1) + enc + 2];
                byte autumnEnc = file[2 * (encounterAreaCount + 1) + enc + 2];
                byte winterEnc = file[3 * (encounterAreaCount + 1) + enc + 2];
                boolean allSeasonsAreTheSame = springEnc == summerEnc && springEnc == autumnEnc && springEnc == winterEnc;
                if (!allSeasonsAreTheSame) {
                    seasonalDependent = true;
                    break;
                }
            }
            if (!seasonalDependent) {
                file[0] = 1;
            }
            areaNARC.files.set(i, file);
        }
        // Save
        this.writeNARC(romEntry.getFile("PokedexAreaData"), areaNARC);
    }

    private void updateAreaDataFromEncounterEntry(byte[] entry, int startOffset, List<byte[]> areaData, int season, int fileNumber) {
        int[] amounts = Gen5Constants.encountersOfEachType;
        int encounterAreaCount = Gen5Constants.getEncounterAreaCount(romEntry.getRomType());
        int[] wildFileToAreaMap = Gen5Constants.getWildFileToAreaMap(romEntry.getRomType());

        int offset = 8;
        for (int i = 0; i < 7; i++) {
            int rate = entry[startOffset + i] & 0xFF;
            if (rate != 0) {
                for (int e = 0; e < amounts[i]; e++) {
                    Species pkmn = pokes[((entry[startOffset + offset + e * 4] & 0xFF) + ((entry[startOffset + offset
                            + 1 + e * 4] & 0x03) << 8))];
                    byte[] pokeFile = areaData.get(pkmn.getBaseNumber() - 1);
                    int areaIndex = wildFileToAreaMap[fileNumber];
                    // Route 4?
                    if (romEntry.getRomType() == Gen5Constants.Type_BW2 && areaIndex == Gen5Constants.bw2Route4AreaIndex) {
                        if ((fileNumber == Gen5Constants.b2Route4EncounterFile && romEntry.getRomCode().charAt(2) == 'D')
                                || (fileNumber == Gen5Constants.w2Route4EncounterFile && romEntry.getRomCode().charAt(2) == 'E')) {
                            areaIndex = -1; // wrong version
                        }
                    }
                    // Victory Road?
                    if (romEntry.getRomType() == Gen5Constants.Type_BW2 && areaIndex == Gen5Constants.bw2VictoryRoadAreaIndex) {
                        if (romEntry.getRomCode().charAt(2) == 'D') {
                            // White 2
                            if (fileNumber == Gen5Constants.b2VRExclusiveRoom1
                                    || fileNumber == Gen5Constants.b2VRExclusiveRoom2) {
                                areaIndex = -1; // wrong version
                            }
                        } else {
                            // Black 2
                            if (fileNumber == Gen5Constants.w2VRExclusiveRoom1
                                    || fileNumber == Gen5Constants.w2VRExclusiveRoom2) {
                                areaIndex = -1; // wrong version
                            }
                        }
                    }
                    // Reversal Mountain?
                    if (romEntry.getRomType() == Gen5Constants.Type_BW2 && areaIndex == Gen5Constants.bw2ReversalMountainAreaIndex) {
                        if (romEntry.getRomCode().charAt(2) == 'D') {
                            // White 2
                            if (fileNumber >= Gen5Constants.b2ReversalMountainStart
                                    && fileNumber <= Gen5Constants.b2ReversalMountainEnd) {
                                areaIndex = -1; // wrong version
                            }
                        } else {
                            // Black 2
                            if (fileNumber >= Gen5Constants.w2ReversalMountainStart
                                    && fileNumber <= Gen5Constants.w2ReversalMountainEnd) {
                                areaIndex = -1; // wrong version
                            }
                        }
                    }
                    // Skip stuff that isn't on the map or is wrong version
                    if (areaIndex != -1) {
                        pokeFile[season * (encounterAreaCount + 1) + 2 + areaIndex] |= (byte) (1 << i);
                    }
                }
            }
            offset += amounts[i] * 4;
        }
    }

    @SuppressWarnings("unused")
    private void addHabitats(byte[] entry, int startOffset, Map<Species, byte[]> pokemonHere, int season) {
        int[] amounts = Gen5Constants.encountersOfEachType;
        int[] type = Gen5Constants.habitatClassificationOfEachType;

        int offset = 8;
        for (int i = 0; i < 7; i++) {
            int rate = entry[startOffset + i] & 0xFF;
            if (rate != 0) {
                for (int e = 0; e < amounts[i]; e++) {
                    Species pkmn = pokes[((entry[startOffset + offset + e * 4] & 0xFF) + ((entry[startOffset + offset
                            + 1 + e * 4] & 0x03) << 8))];
                    if (pokemonHere.containsKey(pkmn)) {
                        pokemonHere.get(pkmn)[type[i] + season * 3] = 1;
                    } else {
                        byte[] locs = new byte[12];
                        locs[type[i] + season * 3] = 1;
                        pokemonHere.put(pkmn, locs);
                    }
                }
            }
            offset += amounts[i] * 4;
        }
    }

    private void writeEncounterEntry(Iterator<EncounterArea> areaIterator, byte[] entry, int startOffset) {
        int[] amounts = Gen5Constants.encountersOfEachType;

        int offset = 8;
        for (int i = 0; i < 7; i++) {
            int rate = entry[startOffset + i] & 0xFF;
            if (rate != 0) {
                Iterator<Encounter> encounterIterator = areaIterator.next().iterator();
                for (int j = 0; j < amounts[i]; j++) {
                    Encounter enc = encounterIterator.next();
                    int speciesAndFormeData = (enc.getFormeNumber() << 11) + enc.getSpecies().getBaseNumber();
                    writeWord(entry, startOffset + offset + j * 4, speciesAndFormeData);
                    entry[startOffset + offset + j * 4 + 2] = (byte) enc.getLevel();
                    entry[startOffset + offset + j * 4 + 3] = (byte) enc.getMaxLevel();
                }
            }
            offset += amounts[i] * 4;
        }
    }

    private void loadWildMapNames() {
        try {
            wildMapNames = new HashMap<>();
            byte[] mapHeaderData = this.readNARC(romEntry.getFile("MapTableFile")).files.get(0);
            int numMapHeaders = mapHeaderData.length / 48;
            List<String> allMapNames = getStrings(false, romEntry.getIntValue("MapNamesTextOffset"));
            for (int map = 0; map < numMapHeaders; map++) {
                int baseOffset = map * 48;
                int mapNameIndex = mapHeaderData[baseOffset + 26] & 0xFF;
                String mapName = allMapNames.get(mapNameIndex);
                if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
                    int wildSet = mapHeaderData[baseOffset + 20] & 0xFF;
                    if (wildSet != 255) {
                        wildMapNames.put(wildSet, mapName);
                    }
                } else {
                    int wildSet = readWord(mapHeaderData, baseOffset + 20);
                    if (wildSet != 65535) {
                        wildMapNames.put(wildSet, mapName);
                    }
                }
            }
            loadedWildMapNames = true;
        } catch (IOException e) {
            throw new RomIOException(e);
        }

    }

    @Override
    public List<Trainer> getTrainers() {
        List<Trainer> allTrainers = new ArrayList<>();
        try {
            NARCArchive trainers = this.readNARC(romEntry.getFile("TrainerData"));
            NARCArchive trpokes = this.readNARC(romEntry.getFile("TrainerPokemon"));
            int trainernum = trainers.files.size();
            List<String> tclasses = this.getTrainerClassNames();
            List<String> tnames = this.getTrainerNames();
            for (int i = 1; i < trainernum; i++) {
                // Trainer entries are 20 bytes
                // Team flags; 1 byte; 0x01 = custom moves, 0x02 = held item
                // Class; 1 byte
                // Battle Mode; 1 byte; 0=single, 1=double, 2=triple, 3=rotation
                // Number of pokemon in team; 1 byte
                // Items; 2 bytes each, 4 item slots
                // AI Flags; 2 byte
                // 2 bytes not used
                // Healer; 1 byte; 0x01 means they will heal player's pokes after defeat.
                // Victory Money; 1 byte; The money given out after defeat =
                //         4 * this value * highest level poke in party
                // Victory Item; 2 bytes; The item given out after defeat (e.g. berries)
                byte[] trainer = trainers.files.get(i);
                byte[] trpoke = trpokes.files.get(i);
                Trainer tr = new Trainer();
                tr.poketype = trainer[0] & 0xFF;
                tr.index = i;
                tr.trainerclass = trainer[1] & 0xFF;
                int numPokes = trainer[3] & 0xFF;
                int pokeOffs = 0;
                tr.fullDisplayName = tclasses.get(tr.trainerclass) + " " + tnames.get(i - 1);
                int battleType = trainer[2] & 0xFF;
                switch (battleType) {
                    case 0:
                        tr.currBattleStyle.setStyle(BattleStyle.Style.SINGLE_BATTLE);
                        break;
                    case 1:
                        tr.currBattleStyle.setStyle(BattleStyle.Style.DOUBLE_BATTLE);
                        originalDoubleTrainers.add(i);
                        break;
                    case 2:
                        tr.currBattleStyle.setStyle(BattleStyle.Style.TRIPLE_BATTLE);
                        break;
                    case 3:
                        tr.currBattleStyle.setStyle(BattleStyle.Style.ROTATION_BATTLE);
                        break;
                }
                for (int poke = 0; poke < numPokes; poke++) {
                    // Structure is
                    // IV SB LV LV SP SP FRM FRM
                    // (HI HI)
                    // (M1 M1 M2 M2 M3 M3 M4 M4)
                    // where SB = 0 0 Ab Ab 0 0 Fm Ml
                    // IV is a "difficulty" level between 0 and 255 to represent 0 to 31 IVs.
                    //     These IVs affect all attributes. For the vanilla games, the
                    //     vast majority of trainers have 0 IVs; Elite Four members will
                    //     have 30 IVs.
                    // Ab Ab = ability number, 0 for random
                    // Fm = 1 for forced female
                    // Ml = 1 for forced male
                    // There's also a trainer flag to force gender, but
                    // this allows fixed teams with mixed genders.

                    int difficulty = trpoke[pokeOffs] & 0xFF;
                    int level = readWord(trpoke, pokeOffs + 2);
                    int species = readWord(trpoke, pokeOffs + 4);
                    int formnum = readWord(trpoke, pokeOffs + 6);
                    TrainerPokemon tpk = new TrainerPokemon();
                    tpk.setLevel(level);
                    tpk.setSpecies(pokes[species]);
                    tpk.setIVs((difficulty) * 31 / 255);
                    int abilityAndFlag = trpoke[pokeOffs + 1];
                    tpk.setAbilitySlot((abilityAndFlag >>> 4) & 0xF);
                    tpk.setForcedGenderFlag((abilityAndFlag & 0xF));
                    tpk.setForme(formnum);
                    tpk.setFormeSuffix(Gen5Constants.getFormeSuffixByBaseForme(species,formnum));
                    pokeOffs += 8;
                    if (tr.pokemonHaveItems()) {
                        tpk.setHeldItem(items.get(readWord(trpoke, pokeOffs)));
                        pokeOffs += 2;
                    }
                    if (tr.pokemonHaveCustomMoves()) {
                        for (int move = 0; move < 4; move++) {
                            tpk.getMoves()[move] = readWord(trpoke, pokeOffs + (move*2));
                        }
                        pokeOffs += 8;
                    }
                    tr.pokemon.add(tpk);
                }
                allTrainers.add(tr);
            }
            if (romEntry.getRomType() == Gen5Constants.Type_BW) {
                Gen5Constants.tagTrainersBW(allTrainers);
                Gen5Constants.setMultiBattleStatusBW(allTrainers);
                Gen5Constants.setForcedRivalStarterPositionsBW(allTrainers);
            } else {
                if (!romEntry.getFile("DriftveilPokemon").isEmpty()) {
                    NARCArchive driftveil = this.readNARC(romEntry.getFile("DriftveilPokemon"));
                    int currentFile = 1;
                    for (int trno = 0; trno < 17; trno++) {
                        Trainer tr = new Trainer();
                        tr.index = allTrainers.size() + 1;
                        tr.poketype = 3; // have held items and custom moves
                        int nameAndClassIndex = Gen5Constants.bw2DriftveilTrainerOffsets.get(trno);
                        tr.fullDisplayName = tclasses.get(Gen5Constants.normalTrainerClassLength + nameAndClassIndex) + " " + tnames.get(Gen5Constants.normalTrainerNameLength + nameAndClassIndex);
                        tr.requiresUniqueHeldItems = true;
                        int pokemonNum = 6;
                        if (trno < 2) {
                            pokemonNum = 3;
                        }
                        for (int poke = 0; poke < pokemonNum; poke++) {
                            byte[] pkmndata = driftveil.files.get(currentFile);
                            int species = readWord(pkmndata, 0);
                            TrainerPokemon tpk = new TrainerPokemon();
                            tpk.setLevel(25);
                            tpk.setSpecies(pokes[species]);
                            tpk.setIVs(31);
                            tpk.setHeldItem(items.get(readWord(pkmndata, 12)));
                            for (int move = 0; move < 4; move++) {
                                tpk.getMoves()[move] = readWord(pkmndata, 2 + (move*2));
                            }
                            tr.pokemon.add(tpk);
                            currentFile++;
                        }
                        allTrainers.add(tr);
                    }
                }
                boolean isBlack2 = romEntry.getRomCode().startsWith("IRE");
                Gen5Constants.tagTrainersBW2(allTrainers);
                Gen5Constants.setMultiBattleStatusBW2(allTrainers, isBlack2);
                Gen5Constants.setForcedRivalStarterPositionsBW2(allTrainers);
            }
        } catch (IOException ex) {
            throw new RomIOException(ex);
        }
        return allTrainers;
    }

    @Override
    public List<Integer> getMainPlaythroughTrainers() {
        if (romEntry.getRomType() == Gen5Constants.Type_BW) { // BW1
            return Gen5Constants.bw1MainPlaythroughTrainers;
        }
        else if (romEntry.getRomType() == Gen5Constants.Type_BW2) { // BW2
            return Gen5Constants.bw2MainPlaythroughTrainers;
        }
        else {
            return Gen5Constants.emptyPlaythroughTrainers;
        }
    }

    @Override
    public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
        if (isChallengeMode) {
            return Arrays.stream(romEntry.getArrayValue("ChallengeModeEliteFourIndices")).boxed().collect(Collectors.toList());
        } else {
            return Arrays.stream(romEntry.getArrayValue("EliteFourIndices")).boxed().collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
        if(romEntry.getRomType() == Gen5Constants.Type_BW) {
            //This won't test correctly with TestRomHandler, but logically, it should be here.
            //(Another reason to split GameHandler off of RomHandler.)

            Map<String, Type> themes = new HashMap<>(Gen5Constants.gymAndEliteThemesBW);
            if(isTypeTriangleChanged()) {
                List<Type> triangle = getStarterTypeTriangle();
                themes.put("GYM1", triangle.get(0));
                themes.put("GYM9", triangle.get(1));
                themes.put("GYM10", triangle.get(2));
            }
            return themes;
        } else {
            return Gen5Constants.gymAndEliteThemesBW2;
        }
    }

    @Override
    public Set<Item> getEvolutionItems() {
        return itemIdsToSet(Gen5Constants.evolutionItems);
    }

    @Override
    public void setTrainers(List<Trainer> trainerData) {
        Iterator<Trainer> allTrainers = trainerData.iterator();
        try {
            NARCArchive trainers = this.readNARC(romEntry.getFile("TrainerData"));
            NARCArchive trpokes = new NARCArchive();
            // Get current movesets in case we need to reset them for certain
            // trainer mons.
            Map<Integer, List<MoveLearnt>> movesets = this.getMovesLearnt();
            // empty entry
            trpokes.files.add(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
            int trainernum = trainers.files.size();
            for (int i = 1; i < trainernum; i++) {
                byte[] trainer = trainers.files.get(i);
                Trainer tr = allTrainers.next();
                // preserve original poketype for held item & moves
                trainer[0] = (byte) tr.poketype;
                int numPokes = tr.pokemon.size();
                trainer[3] = (byte) numPokes;

                if (tr.forcedDoubleBattle) {
                    switch (tr.currBattleStyle.getStyle()) {
                        case SINGLE_BATTLE:
                            if (trainer[2] != 0) {
                                trainer[2] = 0;
                                trainer[12] &= 0x7F; // convert AI back to single battles
                            }
                            break;
                        case DOUBLE_BATTLE:
                            if (trainer[2] != 1) {
                                trainer[2] = 1;
                                trainer[12] |= 0x80; // Flag that needs to be set for trainers not to attack their own pokes
                            }
                            break;
                        case TRIPLE_BATTLE:
                            if (trainer[2] != 2) {
                                trainer[2] = 2;
                                trainer[12] |= 0x80; // Flag that needs to be set for trainers not to attack their own pokes
                            }
                            break;
                        case ROTATION_BATTLE:
                            if (trainer[2] != 3) {
                                trainer[2] = 3;
                                trainer[12] |= 0x7F; // Rotation Battles use Single Battle Logic
                            }
                            break;
                    }
                }

                int bytesNeeded = 8 * numPokes;
                if (tr.pokemonHaveCustomMoves()) {
                    bytesNeeded += 8 * numPokes;
                }
                if (tr.pokemonHaveItems()) {
                    bytesNeeded += 2 * numPokes;
                }
                byte[] trpoke = new byte[bytesNeeded];
                int pokeOffs = 0;
                Iterator<TrainerPokemon> tpokes = tr.pokemon.iterator();
                for (int poke = 0; poke < numPokes; poke++) {
                    TrainerPokemon tp = tpokes.next();
                    // Add 1 to offset integer division truncation
                    int difficulty = Math.min(255, 1 + (tp.getIVs() * 255) / 31);
                    byte abilityAndFlag = (byte)((tp.getAbilitySlot() << 4) | tp.getForcedGenderFlag());
                    writeWord(trpoke, pokeOffs, difficulty | abilityAndFlag << 8);
                    writeWord(trpoke, pokeOffs + 2, tp.getLevel());
                    writeWord(trpoke, pokeOffs + 4, tp.getSpecies().getNumber());
                    writeWord(trpoke, pokeOffs + 6, tp.getForme());
                    // no form info, so no byte 6/7
                    pokeOffs += 8;
                    if (tr.pokemonHaveItems()) {
                        int itemId = tp.getHeldItem() == null ? 0 : tp.getHeldItem().getId();
                        writeWord(trpoke, pokeOffs, itemId);
                        pokeOffs += 2;
                    }
                    if (tr.pokemonHaveCustomMoves()) {
                        if (tp.isResetMoves()) {
                            int[] pokeMoves = RomFunctions.getMovesAtLevel(getAltFormeOfSpecies(tp.getSpecies(), tp.getForme()).getNumber(), movesets, tp.getLevel());
                            for (int m = 0; m < 4; m++) {
                                writeWord(trpoke, pokeOffs + m * 2, pokeMoves[m]);
                            }
                        } else {
                            writeWord(trpoke, pokeOffs, tp.getMoves()[0]);
                            writeWord(trpoke, pokeOffs + 2, tp.getMoves()[1]);
                            writeWord(trpoke, pokeOffs + 4, tp.getMoves()[2]);
                            writeWord(trpoke, pokeOffs + 6, tp.getMoves()[3]);
                        }
                        pokeOffs += 8;
                    }
                }
                trpokes.files.add(trpoke);
            }
            this.writeNARC(romEntry.getFile("TrainerData"), trainers);
            this.writeNARC(romEntry.getFile("TrainerPokemon"), trpokes);

            // Deal with PWT
            if (romEntry.getRomType() == Gen5Constants.Type_BW2 && !romEntry.getFile("DriftveilPokemon").isEmpty()) {
                NARCArchive driftveil = this.readNARC(romEntry.getFile("DriftveilPokemon"));
                int currentFile = 1;
                for (int trno = 0; trno < 17; trno++) {
                    Trainer tr = allTrainers.next();
                    Iterator<TrainerPokemon> tpks = tr.pokemon.iterator();
                    int pokemonNum = 6;
                    if (trno < 2) {
                        pokemonNum = 3;
                    }
                    for (int poke = 0; poke < pokemonNum; poke++) {
                        byte[] pkmndata = driftveil.files.get(currentFile);
                        TrainerPokemon tp = tpks.next();
                        // pokemon and held item
                        writeWord(pkmndata, 0, tp.getSpecies().getNumber());
                        int itemId = tp.getHeldItem() == null ? 0 : tp.getHeldItem().getId();
                        writeWord(pkmndata, 12, itemId);
                        // handle moves
                        if (tp.isResetMoves()) {
                            int[] pokeMoves = RomFunctions.getMovesAtLevel(tp.getSpecies().getNumber(), movesets, tp.getLevel());
                            for (int m = 0; m < 4; m++) {
                                writeWord(pkmndata, 2 + m * 2, pokeMoves[m]);
                            }
                        } else {
                            writeWord(pkmndata, 2, tp.getMoves()[0]);
                            writeWord(pkmndata, 4, tp.getMoves()[1]);
                            writeWord(pkmndata, 6, tp.getMoves()[2]);
                            writeWord(pkmndata, 8, tp.getMoves()[3]);
                        }
                        currentFile++;
                    }
                }
                this.writeNARC(romEntry.getFile("DriftveilPokemon"), driftveil);
            }
        } catch (IOException ex) {
            throw new RomIOException(ex);
        }
    }

    @Override
    public void makeDoubleBattleModePossible() {
        try {
            NARCArchive trainerTextBoxes = readNARC(romEntry.getFile("TrainerTextBoxes"));
            byte[] data = trainerTextBoxes.files.get(0);
            for (int i = 0; i < data.length; i += 4) {
                int trainerIndex = readWord(data, i);
                if (originalDoubleTrainers.contains(trainerIndex)) {
                    int textBoxIndex = readWord(data, i+2);
                    if (textBoxIndex == 3) {
                        writeWord(data, i+2, 0);
                    } else if (textBoxIndex == 5) {
                        writeWord(data, i+2, 2);
                    } else if (textBoxIndex == 6) {
                        writeWord(data, i+2, 0x18);
                    }
                }
            }

            trainerTextBoxes.files.set(0, data);
            writeNARC(romEntry.getFile("TrainerTextBoxes"), trainerTextBoxes);


            try {
                byte[] fieldOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
                String trainerOverworldTextBoxPrefix = romEntry.getStringValue("TrainerOverworldTextBoxPrefix");
                int offset = find(fieldOverlay, trainerOverworldTextBoxPrefix);
                if (offset > 0) {
                    offset += trainerOverworldTextBoxPrefix.length() / 2; // because it was a prefix
                    // Overwrite text box values for trainer 1 in a doubles pair to use the same as a single trainer
                    fieldOverlay[offset-2] = 0;
                    fieldOverlay[offset] = 2;
                    fieldOverlay[offset+2] = 0x18;
                } else {
                    throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
                }

                String doubleBattleLimitPrefix = romEntry.getStringValue("DoubleBattleLimitPrefix");
                offset = find(fieldOverlay, doubleBattleLimitPrefix);
                if (offset > 0) {
                    offset += trainerOverworldTextBoxPrefix.length() / 2; // because it was a prefix
                    // No limit for doubles trainers, i.e. they will spot you even if you have a single Pokemon
                    writeWord(fieldOverlay, offset, 0x46C0);           // nop
                    writeWord(fieldOverlay, offset+2, 0x46C0);  // nop
                } else {
                    throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
                }

                String doubleBattleGetPointerPrefix = romEntry.getStringValue("DoubleBattleGetPointerPrefix");
                int beqToSingleTrainer = romEntry.getIntValue("BeqToSingleTrainerNumber");
                offset = find(fieldOverlay, doubleBattleGetPointerPrefix);
                if (offset > 0) {
                    offset += trainerOverworldTextBoxPrefix.length() / 2; // because it was a prefix
                    // Move some instructions up
                    writeWord(fieldOverlay, offset + 0x10, readWord(fieldOverlay, offset + 0xE));
                    writeWord(fieldOverlay, offset + 0xE, readWord(fieldOverlay, offset + 0xC));
                    writeWord(fieldOverlay, offset + 0xC, readWord(fieldOverlay, offset + 0xA));
                    // Add a beq and cmp to go to the "single trainer" case if a certain pointer is 0
                    writeWord(fieldOverlay, offset + 0xA, beqToSingleTrainer);
                    writeWord(fieldOverlay, offset + 8, 0x2800);
                } else {
                    throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
                }

                writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOverlay);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String textBoxChoicePrefix = romEntry.getStringValue("TextBoxChoicePrefix");
            int offset = find(arm9,textBoxChoicePrefix);

            if (offset > 0) {
                // Change a branch destination in order to only check the relevant trainer instead of checking
                // every trainer in the game (will result in incorrect text boxes when being spotted by doubles
                // pairs, but this is better than the game freezing for half a second and getting a blank text box)
                offset += textBoxChoicePrefix.length() / 2;
                arm9[offset-4] = 2;
            } else {
                throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
            }

        } catch (IOException ex) {
            throw new RomIOException(ex);
        } catch (OperationNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
        Map<Integer, List<MoveLearnt>> movesets = new TreeMap<>();
        try {
            NARCArchive movesLearnt = this.readNARC(romEntry.getFile("PokemonMovesets"));
            int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
            int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
            for (int i = 1; i <= Gen5Constants.pokemonCount + formeCount; i++) {
                Species pkmn = pokes[i];
                byte[] movedata;
                if (i > Gen5Constants.pokemonCount) {
                    movedata = movesLearnt.files.get(i + formeOffset);
                } else {
                    movedata = movesLearnt.files.get(i);
                }
                int moveDataLoc = 0;
                List<MoveLearnt> learnt = new ArrayList<>();
                while (readWord(movedata, moveDataLoc) != 0xFFFF || readWord(movedata, moveDataLoc + 2) != 0xFFFF) {
                    int move = readWord(movedata, moveDataLoc);
                    int level = readWord(movedata, moveDataLoc + 2);
                    learnt.add(new MoveLearnt(move, level));
                    moveDataLoc += 4;
                }
                movesets.put(pkmn.getNumber(), learnt);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return movesets;
    }

    @Override
    public void setMovesLearnt(Map<Integer, List<MoveLearnt>> movesets) {
        try {
            NARCArchive movesLearnt = readNARC(romEntry.getFile("PokemonMovesets"));
            int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
            int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
            for (int i = 1; i <= Gen5Constants.pokemonCount + formeCount; i++) {
                Species pkmn = pokes[i];
                List<MoveLearnt> learnt = movesets.get(pkmn.getNumber());
                int sizeNeeded = learnt.size() * 4 + 4;
                byte[] moveset = new byte[sizeNeeded];
                int j = 0;
                for (; j < learnt.size(); j++) {
                    MoveLearnt ml = learnt.get(j);
                    writeWord(moveset, j * 4, ml.move);
                    writeWord(moveset, j * 4 + 2, ml.level);
                }
                writeWord(moveset, j * 4, 0xFFFF);
                writeWord(moveset, j * 4 + 2, 0xFFFF);
                if (i > Gen5Constants.pokemonCount) {
                    movesLearnt.files.set(i + formeOffset, moveset);
                } else {
                    movesLearnt.files.set(i, moveset);
                }
            }
            // Save
            this.writeNARC(romEntry.getFile("PokemonMovesets"), movesLearnt);
        } catch (IOException e) {
            throw new RomIOException(e);
        }

    }

    @Override
    public Map<Integer, List<Integer>> getEggMoves() {
        Map<Integer, List<Integer>> eggMoves = new TreeMap<>();
        try {
            NARCArchive eggMovesNarc = this.readNARC(romEntry.getFile("EggMoves"));
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                Species pkmn = pokes[i];
                byte[] movedata = eggMovesNarc.files.get(i);
                int numberOfEggMoves = readWord(movedata, 0);
                List<Integer> moves = new ArrayList<>();
                for (int j = 0; j < numberOfEggMoves; j++) {
                    int move = readWord(movedata, 2 + (j * 2));
                    moves.add(move);
                }
                eggMoves.put(pkmn.getNumber(), moves);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return eggMoves;
    }

    @Override
    public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
        try {
            NARCArchive eggMovesNarc = this.readNARC(romEntry.getFile("EggMoves"));
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                Species pkmn = pokes[i];
                byte[] movedata = eggMovesNarc.files.get(i);
                List<Integer> moves = eggMoves.get(pkmn.getNumber());
                for (int j = 0; j < moves.size(); j++) {
                    writeWord(movedata, 2 + (j * 2), moves.get(j));
                }
            }
            // Save
            this.writeNARC(romEntry.getFile("EggMoves"), eggMovesNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    public static class RoamingPokemon {
        private int[] speciesOverlayOffsets;
        private int[] levelOverlayOffsets;
        private InFileEntry[] speciesScriptOffsets;

        public RoamingPokemon(int[] speciesOverlayOffsets, int[] levelOverlayOffsets, InFileEntry[] speciesScriptOffsets) {
            this.speciesOverlayOffsets = speciesOverlayOffsets;
            this.levelOverlayOffsets = levelOverlayOffsets;
            this.speciesScriptOffsets = speciesScriptOffsets;
        }

        public Species getPokemon(Gen5RomHandler parent) throws IOException {
            byte[] overlay = parent.readOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"));
            int species = parent.readWord(overlay, speciesOverlayOffsets[0]);
            return parent.pokes[species];
        }

        public void setPokemon(Gen5RomHandler parent, NARCArchive scriptNARC, Species pkmn) throws IOException {
            int value = pkmn.getNumber();
            byte[] overlay = parent.readOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"));
            for (int speciesOverlayOffset : speciesOverlayOffsets) {
                parent.writeWord(overlay, speciesOverlayOffset, value);
            }
            parent.writeOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"), overlay);
            for (InFileEntry speciesScriptOffset : speciesScriptOffsets) {
                byte[] file = scriptNARC.files.get(speciesScriptOffset.getFile());
                parent.writeWord(file, speciesScriptOffset.getOffset(), value);
            }
        }

        public int getLevel(Gen5RomHandler parent) throws IOException {
            if (levelOverlayOffsets.length == 0) {
                return 1;
            }
            byte[] overlay = parent.readOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"));
            return overlay[levelOverlayOffsets[0]];
        }

        public void setLevel(Gen5RomHandler parent, int level) throws IOException {
            byte[] overlay = parent.readOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"));
            for (int levelOverlayOffset : levelOverlayOffsets) {
                overlay[levelOverlayOffset] = (byte) level;
            }
            parent.writeOverlay(parent.romEntry.getIntValue("RoamerOvlNumber"), overlay);
        }
    }

    public static class TradeScript {
        private int fileNum;
        private int[] requestedOffsets;
        private int[] givenOffsets;

        public TradeScript(int fileNum, int[] requestedOffsets, int[] givenOffsets) {
            this.fileNum = fileNum;
            this.requestedOffsets = requestedOffsets;
            this.givenOffsets = givenOffsets;
        }

        public void setPokemon(Gen5RomHandler parent, NARCArchive scriptNARC, Species requested, Species given) {
            int req = requested.getNumber();
            int giv = given.getNumber();
            for (int i = 0; i < requestedOffsets.length; i++) {
                byte[] file = scriptNARC.files.get(fileNum);
                parent.writeWord(file, requestedOffsets[i], req);
                parent.writeWord(file, givenOffsets[i], giv);
            }
        }
    }

    @Override
    public boolean hasStaticAltFormes() {
        return false;
    }

    @Override
    public boolean hasMainGameLegendaries() {
        return true;
    }

    @Override
    public List<Integer> getMainGameLegendaries() {
        return Arrays.stream(romEntry.getArrayValue("MainGameLegendaries")).boxed().collect(Collectors.toList());
    }

    @Override
    public List<Integer> getSpecialMusicStatics() {
        return Arrays.stream(romEntry.getArrayValue("SpecialMusicStatics")).boxed().collect(Collectors.toList());
    }

    @Override
    public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {

        try {
            byte[] fieldOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
            genericIPSPatch(fieldOverlay, "NewIndexToMusicOvlTweak");
            writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOverlay);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Relies on arm9 already being extended, which it *should* have been in loadedROM
        genericIPSPatch(arm9, "NewIndexToMusicTweak");

        String newIndexToMusicPrefix = romEntry.getStringValue("NewIndexToMusicPrefix");
        int newIndexToMusicPoolOffset = find(arm9, newIndexToMusicPrefix);
        newIndexToMusicPoolOffset += newIndexToMusicPrefix.length() / 2;

        List<Integer> replaced = new ArrayList<>();
        int iMax = -1;

        switch(romEntry.getRomType()) {
            case Gen5Constants.Type_BW:
                for (int oldStatic: specialMusicStaticChanges.keySet()) {
                    int i = newIndexToMusicPoolOffset;
                    int index = readWord(arm9, i);
                    while (index != oldStatic || replaced.contains(i)) {
                        i += 4;
                        index = readWord(arm9, i);
                    }
                    writeWord(arm9, i, specialMusicStaticChanges.get(oldStatic));
                    replaced.add(i);
                    if (i > iMax) iMax = i;
                }
                break;
            case Gen5Constants.Type_BW2:
                for (int oldStatic: specialMusicStaticChanges.keySet()) {
                    int i = newIndexToMusicPoolOffset;
                    int index = readWord(arm9, i);
                    while (index != oldStatic || replaced.contains(i)) {
                        i += 4;
                        index = readWord(arm9, i);
                    }
                    // Special Kyurem-B/W handling
                    if (index > Gen5Constants.pokemonCount) {
                        writeWord(arm9, i - 0xFE, 0);
                        writeWord(arm9, i - 0xFC, 0);
                        writeWord(arm9, i - 0xFA, 0);
                        writeWord(arm9, i - 0xF8, 0x4290);
                    }
                    writeWord(arm9, i, specialMusicStaticChanges.get(oldStatic));
                    replaced.add(i);
                    if (i > iMax) iMax = i;
                }
                break;
        }

        List<Integer> specialMusicStatics = getSpecialMusicStatics();

        for (int i = newIndexToMusicPoolOffset; i <= iMax; i+= 4) {
            if (!replaced.contains(i)) {
                int pkID = readWord(arm9, i);

                // If a Pokemon is a "special music static" but the music hasn't been replaced, leave as is
                // Otherwise zero it out, because the original static encounter doesn't exist
                if (!specialMusicStatics.contains(pkID)) {
                    writeWord(arm9, i, 0);
                }
            }
        }

    }

    @Override
    public boolean hasStaticMusicFix() {
        return romEntry.getTweakFile("NewIndexToMusicTweak") != null;
    }

    @Override
    public List<TotemPokemon> getTotemPokemon() {
        return new ArrayList<>();
    }

    @Override
    public void setTotemPokemon(List<TotemPokemon> totemPokemon) {

    }

    @Override
    public List<StaticEncounter> getStaticPokemon() {
        List<StaticEncounter> sp = new ArrayList<>();
        if (!romEntry.hasStaticPokemonSupport()) {
            return sp;
        }
        int[] staticEggOffsets = romEntry.getArrayValue("StaticEggPokemonOffsets");

        // Regular static encounters
        NARCArchive scriptNARC = scriptNarc;
        for (int i = 0; i < romEntry.getStaticPokemon().size(); i++) {
            int currentOffset = i;
            DSStaticPokemon statP = romEntry.getStaticPokemon().get(i);
            StaticEncounter se = new StaticEncounter();
            Species newPK = statP.getPokemon(this, scriptNARC);
            newPK = getAltFormeOfSpecies(newPK, statP.getForme(scriptNARC));
            se.setSpecies(newPK);
            se.setLevel(statP.getLevel(scriptNARC, 0));
            se.setEgg(Arrays.stream(staticEggOffsets).anyMatch(x-> x == currentOffset));
            for (int levelEntry = 1; levelEntry < statP.getLevelCount(); levelEntry++) {
                StaticEncounter linkedStatic = new StaticEncounter();
                linkedStatic.setSpecies(newPK);
                linkedStatic.setLevel(statP.getLevel(scriptNARC, levelEntry));
                se.getLinkedEncounters().add(linkedStatic);
            }
            sp.add(se);
        }

        // Foongus/Amoongus fake ball encounters
        try {
            NARCArchive mapNARC = readNARC(romEntry.getFile("MapFiles"));
            for (int i = 0; i < romEntry.getStaticPokemonFakeBall().size(); i++) {
                DSStaticPokemon statP = romEntry.getStaticPokemonFakeBall().get(i);
                StaticEncounter se = new StaticEncounter();
                Species newPK = statP.getPokemon(this, scriptNARC);
                se.setSpecies(newPK);
                se.setLevel(statP.getLevel(mapNARC, 0));
                for (int levelEntry = 1; levelEntry < statP.getLevelCount(); levelEntry++) {
                    StaticEncounter linkedStatic = new StaticEncounter();
                    linkedStatic.setSpecies(newPK);
                    linkedStatic.setLevel(statP.getLevel(mapNARC, levelEntry));
                    se.getLinkedEncounters().add(linkedStatic);
                }
                sp.add(se);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        // BW2 hidden grotto encounters
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            List<Species> allowedHiddenHollowSpecies = new ArrayList<>();
            allowedHiddenHollowSpecies.addAll(Arrays.asList(Arrays.copyOfRange(pokes,1,494)));
            allowedHiddenHollowSpecies.addAll(
                    Gen5Constants.bw2HiddenHollowUnovaPokemon.stream().map(i -> pokes[i]).collect(Collectors.toList()));

            try {
                NARCArchive hhNARC = this.readNARC(romEntry.getFile("HiddenHollows"));
                for (byte[] hhEntry : hhNARC.files) {
                    for (int version = 0; version < 2; version++) {
                        if (version != romEntry.getIntValue("HiddenHollowIndex")) continue;
                        for (int raritySlot = 0; raritySlot < 3; raritySlot++) {
                            List<StaticEncounter> encountersInGroup = new ArrayList<>();
                            for (int group = 0; group < 4; group++) {
                                StaticEncounter se = new StaticEncounter();
                                Species newPK = pokes[readWord(hhEntry, version * 78 + raritySlot * 26 + group * 2)];
                                newPK = getAltFormeOfSpecies(newPK, hhEntry[version * 78 + raritySlot * 26 + 20 + group]);
                                se.setSpecies(newPK);
                                se.setLevel(hhEntry[version * 78 + raritySlot * 26 + 12 + group]);
                                se.setMaxLevel(hhEntry[version * 78 + raritySlot * 26 + 8 + group]);
                                se.setEgg(false);
                                se.setRestrictedPool(true);
                                se.setRestrictedList(allowedHiddenHollowSpecies);
                                boolean originalEncounter = true;
                                for (StaticEncounter encounterInGroup: encountersInGroup) {
                                    if (encounterInGroup.getSpecies().equals(se.getSpecies())) {
                                        encounterInGroup.getLinkedEncounters().add(se);
                                        originalEncounter = false;
                                        break;
                                    }
                                }
                                if (originalEncounter) {
                                    encountersInGroup.add(se);
                                    sp.add(se);
                                    if (!hiddenHollowCounted) {
                                        hiddenHollowCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RomIOException(e);
            }
        }
        hiddenHollowCounted = true;

        // Roaming encounters
        if (!romEntry.getRoamingPokemon().isEmpty()) {
            try {
                int firstSpeciesOffset = romEntry.getRoamingPokemon().get(0).speciesOverlayOffsets[0];
                byte[] overlay = readOverlay(romEntry.getIntValue("RoamerOvlNumber"));
                if (readWord(overlay, firstSpeciesOffset) > pokes.length) {
                    // In the original code, this is "mov r0, #0x2", which read as a word is
                    // 0x2002, much larger than the number of species in the game.
                    applyBlackWhiteRoamerPatch();
                }
                for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                    RoamingPokemon roamer = romEntry.getRoamingPokemon().get(i);
                    StaticEncounter se = new StaticEncounter();
                    se.setSpecies(roamer.getPokemon(this));
                    se.setLevel(roamer.getLevel(this));
                    sp.add(se);
                }
            } catch (Exception e) {
                throw new RomIOException(e);
            }
        }

        return sp;
    }

    @Override
    public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
        if (!romEntry.hasStaticPokemonSupport()) {
            return false;
        }
        if (staticPokemon.size() != (romEntry.getStaticPokemon().size() + romEntry.getStaticPokemonFakeBall().size() +
                hiddenHollowCount + romEntry.getRoamingPokemon().size())) {
            return false;
        }
        Iterator<StaticEncounter> statics = staticPokemon.iterator();

        // Regular static encounters
        NARCArchive scriptNARC = scriptNarc;
        for (DSStaticPokemon statP : romEntry.getStaticPokemon()) {
            StaticEncounter se = statics.next();
            statP.setPokemon(this, scriptNARC, se.getSpecies());
            statP.setForme(scriptNARC, se.getSpecies().getFormeNumber());
            statP.setLevel(scriptNARC, se.getLevel(), 0);
            for (int i = 0; i < se.getLinkedEncounters().size(); i++) {
                StaticEncounter linkedStatic = se.getLinkedEncounters().get(i);
                statP.setLevel(scriptNARC, linkedStatic.getLevel(), i + 1);
            }
        }

        // Foongus/Amoongus fake ball encounters
        try {
            NARCArchive mapNARC = readNARC(romEntry.getFile("MapFiles"));
            for (DSStaticPokemon statP : romEntry.getStaticPokemonFakeBall()) {
                StaticEncounter se = statics.next();
                statP.setPokemon(this, scriptNARC, se.getSpecies());
                statP.setLevel(mapNARC, se.getLevel(), 0);
                for (int i = 0; i < se.getLinkedEncounters().size(); i++) {
                    StaticEncounter linkedStatic = se.getLinkedEncounters().get(i);
                    statP.setLevel(mapNARC, linkedStatic.getLevel(), i + 1);
                }
            }
            this.writeNARC(romEntry.getFile("MapFiles"), mapNARC);
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        // BW2 hidden grotto encounters
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            try {
                NARCArchive hhNARC = this.readNARC(romEntry.getFile("HiddenHollows"));
                for (byte[] hhEntry : hhNARC.files) {
                    for (int version = 0; version < 2; version++) {
                        if (version != romEntry.getIntValue("HiddenHollowIndex")) continue;
                        for (int raritySlot = 0; raritySlot < 3; raritySlot++) {
                            for (int group = 0; group < 4; group++) {
                                StaticEncounter se = statics.next();
                                writeWord(hhEntry, version * 78 + raritySlot * 26 + group * 2, se.getSpecies().getNumber());
                                // genderRatio here is a percentage from 0-100;
                                // this value overrides the genderRatio of the species.
                                // The vanilla grottoes have some variance in genderRatios, but for simplicity's sake
                                // we just set all Pokémon to 30% female, unless they are always female/male/genderless.
                                int genderRatio;
                                switch (se.getSpecies().getGenderRatio()) {
                                    case 0xFE: // female
                                        genderRatio = 100;
                                        break;
                                    case 0x00: // male
                                    case 0xFF: // genderless
                                        genderRatio = 0;
                                        break;
                                    default:
                                        genderRatio = 30;
                                }
                                hhEntry[version * 78 + raritySlot * 26 + 16 + group] = (byte) genderRatio;
                                hhEntry[version * 78 + raritySlot * 26 + 20 + group] = (byte) se.getForme(); // forme
                                hhEntry[version * 78 + raritySlot * 26 + 12 + group] = (byte) se.getLevel();
                                hhEntry[version * 78 + raritySlot * 26 + 8 + group] = (byte) se.getMaxLevel();
                                for (int i = 0; i < se.getLinkedEncounters().size(); i++) {
                                    StaticEncounter linkedStatic = se.getLinkedEncounters().get(i);
                                    group++;
                                    writeWord(hhEntry, version * 78 + raritySlot * 26 + group * 2, linkedStatic.getSpecies().getNumber());
                                    hhEntry[version * 78 + raritySlot * 26 + 16 + group] = (byte) genderRatio;
                                    hhEntry[version * 78 + raritySlot * 26 + 20 + group] = (byte) linkedStatic.getForme(); // forme
                                    hhEntry[version * 78 + raritySlot * 26 + 12 + group] = (byte) linkedStatic.getLevel();
                                    hhEntry[version * 78 + raritySlot * 26 + 8 + group] = (byte) linkedStatic.getMaxLevel();
                                }
                            }
                        }
                    }
                }
                this.writeNARC(romEntry.getFile("HiddenHollows"), hhNARC);
            } catch (IOException e) {
                throw new RomIOException(e);
            }
        }

        // Roaming encounters
        try {
            for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
                RoamingPokemon roamer = romEntry.getRoamingPokemon().get(i);
                StaticEncounter roamerEncounter = statics.next();
                roamer.setPokemon(this, scriptNarc, roamerEncounter.getSpecies());
                roamer.setLevel(this, roamerEncounter.getLevel());
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }

        // In Black/White, the game has multiple hardcoded checks for Reshiram/Zekrom's species
        // ID in order to properly move it out of a box and into the first slot of the player's
        // party. We need to replace these checks with the species ID of whatever occupies
        // Reshiram/Zekrom's static encounter for the game to still function properly.
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            int boxLegendaryIndex = romEntry.getIntValue("BoxLegendaryOffset");
            try {
                int boxLegendarySpecies = staticPokemon.get(boxLegendaryIndex).getSpecies().getNumber();
                fixBoxLegendaryBW1(boxLegendarySpecies);
            } catch (IOException e) {
                throw new RomIOException(e);
            }
        }

        return true;
    }

    private void fixBoxLegendaryBW1(int boxLegendarySpecies) throws IOException {
        byte[] boxLegendaryOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
        if (romEntry.isBlack()) {
            // In Black, Reshiram's species ID is always retrieved via a pc-relative
            // load to some constant. All we need to is replace these constants with
            // the new species ID.
            int firstConstantOffset = find(boxLegendaryOverlay, Gen5Constants.blackBoxLegendaryCheckPrefix1);
            if (firstConstantOffset > 0) {
                firstConstantOffset += Gen5Constants.blackBoxLegendaryCheckPrefix1.length() / 2; // because it was a prefix
                FileFunctions.writeFullInt(boxLegendaryOverlay, firstConstantOffset, boxLegendarySpecies);
            }
            int secondConstantOffset = find(boxLegendaryOverlay, Gen5Constants.blackBoxLegendaryCheckPrefix2);
            if (secondConstantOffset > 0) {
                secondConstantOffset += Gen5Constants.blackBoxLegendaryCheckPrefix2.length() / 2; // because it was a prefix
                FileFunctions.writeFullInt(boxLegendaryOverlay, secondConstantOffset, boxLegendarySpecies);
            }
        } else {
            // In White, Zekrom's species ID is always loaded by loading 161 into a register
            // and then shifting left by 2. Thus, we need to be more clever with how we
            // modify code in order to set up some pc-relative loads.
            int firstFunctionOffset = find(boxLegendaryOverlay, Gen5Constants.whiteBoxLegendaryCheckPrefix1);
            if (firstFunctionOffset > 0) {
                firstFunctionOffset += Gen5Constants.whiteBoxLegendaryCheckPrefix1.length() / 2; // because it was a prefix

                // First, nop the instruction that loads a pointer to the string
                // "scrcmd_pokemon_fld.c" into a register; this has seemingly no
                // effect on the game and was probably used strictly for debugging.
                boxLegendaryOverlay[firstFunctionOffset + 66] = 0x00;
                boxLegendaryOverlay[firstFunctionOffset + 67] = 0x00;

                // In the space that used to hold the address of the "scrcmd_pokemon_fld.c"
                // string, we're going to instead store the species ID of the box legendary
                // so that we can do a pc-relative load to it.
                FileFunctions.writeFullInt(boxLegendaryOverlay, firstFunctionOffset + 320, boxLegendarySpecies);

                // Zekrom's species ID is originally loaded by doing a mov into r1 and then a shift
                // on that same register four instructions later. This nops out the first instruction
                // and replaces the left shift with a pc-relative load to the constant we stored above.
                boxLegendaryOverlay[firstFunctionOffset + 18] = 0x00;
                boxLegendaryOverlay[firstFunctionOffset + 19] = 0x00;
                boxLegendaryOverlay[firstFunctionOffset + 26] = 0x49;
                boxLegendaryOverlay[firstFunctionOffset + 27] = 0x49;
            }

            int secondFunctionOffset = find(boxLegendaryOverlay, Gen5Constants.whiteBoxLegendaryCheckPrefix2);
            if (secondFunctionOffset > 0) {
                secondFunctionOffset += Gen5Constants.whiteBoxLegendaryCheckPrefix2.length() / 2; // because it was a prefix

                // A completely unrelated function below this one decides to pc-relative load 0x00000000 into r4
                // instead of just doing a mov. We can replace it with a simple "mov r4, #0x0", but we have to be
                // careful about where we put it. The original code calls a function, performs an "add r6, r0, #0x0",
                // then does the load into r4. This means that whether or not the Z bit is set depends on the result
                // of the function call. If we naively replace the load with our mov, we'll be forcibly setting the Z
                // bit to 1, which will cause the subsequent beq to potentially take us to the wrong place. To get
                // around this, we reorder the code so the "mov r4, #0x0" occurs *before* the "add r6, r0, #0x0".
                boxLegendaryOverlay[secondFunctionOffset + 502] = 0x00;
                boxLegendaryOverlay[secondFunctionOffset + 503] = 0x24;
                boxLegendaryOverlay[secondFunctionOffset + 504] = 0x06;
                boxLegendaryOverlay[secondFunctionOffset + 505] = 0x1C;

                // Now replace the 0x00000000 constant with the species ID
                FileFunctions.writeFullInt(boxLegendaryOverlay, secondFunctionOffset + 556, boxLegendarySpecies);

                // Lastly, replace the mov and lsl that originally puts Zekrom's species ID into r1
                // with a pc-relative of the above constant and a nop.
                boxLegendaryOverlay[secondFunctionOffset + 78] = 0x77;
                boxLegendaryOverlay[secondFunctionOffset + 79] = 0x49;
                boxLegendaryOverlay[secondFunctionOffset + 80] = 0x00;
                boxLegendaryOverlay[secondFunctionOffset + 81] = 0x00;
            }
        }
        writeOverlay(romEntry.getIntValue("FieldOvlNumber"), boxLegendaryOverlay);
    }

    private void applyBlackWhiteRoamerPatch() throws IOException {
        int offset = romEntry.getIntValue("GetRoamerFlagOffsetStartOffset");
        byte[] overlay = readOverlay(romEntry.getIntValue("RoamerOvlNumber"));

        // This function returns 0 for Thundurus, 1 for Tornadus, and 2 for any other species.
        // In testing, this 2 case is never used, so we can use the space for it to pc-relative
        // load Thundurus's ID. The original code compares to Tornadus and Thundurus then does
        // "bne #0xA" to the default case. Change it to "bne #0x4", which will just make this
        // case immediately return.
        overlay[offset + 10] = 0x00;

        // Now in the space that used to do "mov r0, #0x2" and return, write Thundurus's ID
        FileFunctions.writeFullInt(overlay, offset + 20, SpeciesIDs.thundurus);

        // Lastly, instead of computing Thundurus's ID as TornadusID + 1, pc-relative load it
        // from what we wrote earlier.
        overlay[offset + 6] = 0x03;
        overlay[offset + 7] = 0x49;
        writeOverlay(romEntry.getIntValue("RoamerOvlNumber"), overlay);
    }

    @Override
    public int miscTweaksAvailable() {
        int available = 0;
        if (romEntry.hasTweakFile("FastestTextTweak")) {
            available |= MiscTweak.FASTEST_TEXT.getValue();
        }
        available |= MiscTweak.BAN_LUCKY_EGG.getValue();
        available |= MiscTweak.NO_FREE_LUCKY_EGG.getValue();
        available |= MiscTweak.BAN_BIG_MANIAC_ITEMS.getValue();
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            available |= MiscTweak.BALANCE_STATIC_LEVELS.getValue();
        }
        if (romEntry.hasTweakFile("NationalDexAtStartTweak")) {
            available |= MiscTweak.NATIONAL_DEX_AT_START.getValue();
        }
        available |= MiscTweak.RUN_WITHOUT_RUNNING_SHOES.getValue();
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            available |= MiscTweak.FORCE_CHALLENGE_MODE.getValue();
        }
        available |= MiscTweak.DISABLE_LOW_HP_MUSIC.getValue();
        if (romEntry.getIntValue("HMMovesForgettableFunctionOffset") != 0) {
            available |= MiscTweak.FORGETTABLE_HMS.getValue();
        }
        return available;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if (tweak == MiscTweak.FASTEST_TEXT) {
            applyFastestText();
        } else if (tweak == MiscTweak.BAN_LUCKY_EGG) {
            items.get(ItemIDs.luckyEgg).setAllowed(false);
        } else if (tweak == MiscTweak.NO_FREE_LUCKY_EGG) {
            removeFreeLuckyEgg();
        } else if (tweak == MiscTweak.BAN_BIG_MANIAC_ITEMS) {
            for (int i = 0; i < 4; i++) {
                // BalmMushroom, Big Nugget, Pearl String, Comet Shard
                items.get(ItemIDs.balmMushroom + i).setAllowed(false);
                // Relics
                items.get(ItemIDs.relicVase + i).setAllowed(false);
            }
            for (int i = 0; i < 7; i++) {
                // Rare berries
                items.get(ItemIDs.lansatBerry + i).setAllowed(false);
            }
        } else if (tweak == MiscTweak.BALANCE_STATIC_LEVELS) {
            byte[] fossilFile = scriptNarc.files.get(Gen5Constants.fossilPokemonFile);
            writeWord(fossilFile,Gen5Constants.fossilPokemonLevelOffset,20);
        } else if (tweak == MiscTweak.NATIONAL_DEX_AT_START) {
            patchForNationalDex();
        } else if (tweak == MiscTweak.RUN_WITHOUT_RUNNING_SHOES) {
            applyRunWithoutRunningShoesPatch();
        } else if (tweak == MiscTweak.FORCE_CHALLENGE_MODE) {
            forceChallengeMode();
        } else if (tweak == MiscTweak.DISABLE_LOW_HP_MUSIC) {
            disableLowHpMusic();
        } else if (tweak == MiscTweak.FORGETTABLE_HMS) {
            applyForgettableHMsPatch();
        }
    }

    // Removes the free lucky egg you receive from Professor Juniper and replaces it with a gooey mulch.
    private void removeFreeLuckyEgg() {
        int scriptFileGifts = romEntry.getIntValue("LuckyEggScriptOffset");
        int setVarGift = Gen5Constants.hiddenItemSetVarCommand;

        byte[] itemScripts = scriptNarc.files.get(scriptFileGifts);
        int offset = 0;
        int lookingForEggs = romEntry.getRomType() == Gen5Constants.Type_BW ? 1 : 2;
        while (lookingForEggs > 0) {
            int part1 = readWord(itemScripts, offset);
            if (part1 == Gen5Constants.scriptListTerminator) {
                // done
                break;
            }
            int offsetInFile = readRelativePointer(itemScripts, offset);
            offset += 4;
            if (offsetInFile > itemScripts.length) {
                break;
            }
            while (true) {
                offsetInFile++;
                // Gift items are not necessarily word aligned, so need to read one byte at a time
                int b = readByte(itemScripts, offsetInFile);
                if (b == setVarGift) {
                    int command = readWord(itemScripts, offsetInFile);
                    int variable = readWord(itemScripts,offsetInFile + 2);
                    int item = readWord(itemScripts, offsetInFile + 4);
                    if (command == setVarGift && variable == Gen5Constants.hiddenItemVarSet && item == ItemIDs.luckyEgg) {

                        writeWord(itemScripts, offsetInFile + 4, ItemIDs.gooeyMulch);
                        lookingForEggs--;
                    }
                }
                if (b == 0x2E) { // Beginning of a new block in the file
                    break;
                }
            }
        }
    }

    private void applyFastestText() {
        genericIPSPatch(arm9, "FastestTextTweak");
    }

    private void patchForNationalDex() {
        byte[] pokedexScript = scriptNarc.files.get(romEntry.getIntValue("NationalDexScriptOffset"));

        // Our patcher breaks if the output file is larger than the input file. In our case, we want
        // to expand the script by four bytes to add an instruction to enable the national dex. Thus,
        // the IPS patch was created with us adding four 0x00 bytes to the end of the script in mind.
        byte[] expandedPokedexScript = new byte[pokedexScript.length + 4];
        System.arraycopy(pokedexScript, 0, expandedPokedexScript, 0, pokedexScript.length);
        genericIPSPatch(expandedPokedexScript, "NationalDexAtStartTweak");
        scriptNarc.files.set(romEntry.getIntValue("NationalDexScriptOffset"), expandedPokedexScript);
    }

    private void applyRunWithoutRunningShoesPatch() {
        try {
            // In the overlay that handles field movement, there's a very simple function
            // that checks if the player has the Running Shoes by checking if flag 2403 is
            // set on the save file. If it isn't, the code branches to a separate code path
            // where the function returns 0. The below code simply nops this branch so that
            // this function always returns 1, regardless of the status of flag 2403.
            byte[] fieldOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
            String prefix = Gen5Constants.runningShoesPrefix;
            int offset = find(fieldOverlay, prefix);
            if (offset != 0) {
                writeWord(fieldOverlay, offset, 0);
                writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOverlay);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public TypeTable getTypeTable() {
        if (typeTable == null) {
            typeTable = readTypeTable();
        }
        return typeTable;
    }

    private TypeTable readTypeTable() {
        try {
            TypeTable typeTable = new TypeTable(Type.getAllTypes(5));
            byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
            int tableOffset = romEntry.getIntValue("TypeEffectivenessOffset");
            int tableWidth = typeTable.getTypes().size();

            for (Type attacker : typeTable.getTypes()) {
                for (Type defender : typeTable.getTypes()) {
                    int offset = tableOffset + (Gen5Constants.typeToByte(attacker) * tableWidth) + Gen5Constants.typeToByte(defender);
                    int effectivenessInternal = battleOverlay[offset];
                    Effectiveness effectiveness;
                    switch (effectivenessInternal) {
                        case 8:
                            effectiveness = Effectiveness.DOUBLE;
                            break;
                        case 4:
                            effectiveness = Effectiveness.NEUTRAL;
                            break;
                        case 2:
                            effectiveness = Effectiveness.HALF;
                            break;
                        case 0:
                            effectiveness = Effectiveness.ZERO;
                            break;
                        default:
                            effectiveness = null;
                            break;
                    }
                    typeTable.setEffectiveness(attacker, defender, effectiveness);
                }
            }

            return typeTable;
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public void setTypeTable(TypeTable typeTable) {
        this.typeTable = typeTable;
        writeTypeTable(typeTable);
    }

    private void writeTypeTable(TypeTable typeTable) {
        try {
            byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
            int tableOffset = romEntry.getIntValue("TypeEffectivenessOffset");
            int tableWidth = typeTable.getTypes().size();

            for (Type attacker : typeTable.getTypes()) {
                for (Type defender : typeTable.getTypes()) {
                    int offset = tableOffset + (Gen5Constants.typeToByte(attacker) * tableWidth) + Gen5Constants.typeToByte(defender);
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
                    battleOverlay[offset] = effectivenessInternal;
                }
            }
            writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void forceChallengeMode() {
        int offset = find(arm9, Gen5Constants.forceChallengeModeLocator);
        if (offset > 0) {
            // offset is now pointing at the start of sub_2010528, which is the function that
            // determines which difficulty the player currently has enabled. It returns 0 for
            // Easy Mode, 1 for Normal Mode, and 2 for Challenge Mode. Since we're just trying
            // to force Challenge Mode, all we need to do is:
            // mov r0, #0x2
            // bx lr
            arm9[offset] = 0x02;
            arm9[offset + 1] = 0x20;
            arm9[offset + 2] = 0x70;
            arm9[offset + 3] = 0x47;
        }
    }

    private void disableLowHpMusic() {
        try {
            byte[] lowHealthMusicOverlay = readOverlay(romEntry.getIntValue("LowHealthMusicOvlNumber"));
            int offset = find(lowHealthMusicOverlay, Gen5Constants.lowHealthMusicLocator);
            if (offset > 0) {
                // The game calls a function that returns 2 if the Pokemon has low HP. The ASM looks like this:
                // bl funcThatReturns2IfThePokemonHasLowHp
                // cmp r0, #0x2
                // bne pokemonDoesNotHaveLowHp
                // mov r7, #0x1
                // The offset variable is currently pointing at the bne instruction. If we change that bne to an unconditional
                // branch, the game will never think the player's Pokemon has low HP (for the purposes of changing the music).
                lowHealthMusicOverlay[offset + 1] = (byte)0xE0;
                writeOverlay(romEntry.getIntValue("LowHealthMusicOvlNumber"), lowHealthMusicOverlay);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void applyForgettableHMsPatch() {
        // thanks to totally_anonymous and drayano60
        int offset = romEntry.getIntValue("HMMovesForgettableFunctionOffset");
        if (offset == 0) {
            return;
        }

        byte[] bytesBefore = RomFunctions.hexToBytes(Gen5Constants.hmsForgettableBefore);
        for (int i = 0; i < bytesBefore.length; i++) {
            if (arm9[offset + i] != bytesBefore[i]) {
                throw new RuntimeException("Expected 0x" + Integer.toHexString(bytesBefore[i] & 0xFF) + ", was 0x"
                        + Integer.toHexString(arm9[offset + i] & 0xFF) + ". Likely HMMovesForgettableFunctionOffset is faulty.");
            }
        }
        writeBytes(arm9, offset, RomFunctions.hexToBytes(Gen5Constants.hmsForgettableAfter));
    }

    @Override
    public void enableGuaranteedPokemonCatching() {
        try {
            byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
            int offset = find(battleOverlay, Gen5Constants.perfectOddsBranchLocator);
            if (offset > 0) {
                // The game checks to see if your odds are greater then or equal to 255 using the following
                // code. Note that they compare to 0xFF000 instead of 0xFF; it looks like all catching code
                // probabilities are shifted like this?
                // mov r0, #0xFF
                // lsl r0, r0, #0xC
                // cmp r7, r0
                // blt oddsLessThanOrEqualTo254
                // The below code just nops the branch out so it always acts like our odds are 255, and
                // Pokemon are automatically caught no matter what.
                battleOverlay[offset] = 0x00;
                battleOverlay[offset + 1] = 0x00;
                writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private boolean genericIPSPatch(byte[] data, String ctName) {
        String patchName = romEntry.getTweakFile(ctName);
        if (patchName == null) {
            return false;
        }

        try {
            FileFunctions.applyPatch(data, patchName);
            return true;
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public List<Integer> getTMMoves() {
        String tmDataPrefix = Gen5Constants.tmDataPrefix;
        int offset = find(arm9, tmDataPrefix);
        if (offset > 0) {
            offset += Gen5Constants.tmDataPrefix.length() / 2; // because it was
                                                               // a prefix
            List<Integer> tms = new ArrayList<>();
            for (int i = 0; i < Gen5Constants.tmBlockOneCount; i++) {
                tms.add(readWord(arm9, offset + i * 2));
            }
            // Skip past first 92 TMs and 6 HMs
            offset += (Gen5Constants.tmBlockOneCount + Gen5Constants.hmCount) * 2;
            for (int i = 0; i < (Gen5Constants.tmCount - Gen5Constants.tmBlockOneCount); i++) {
                tms.add(readWord(arm9, offset + i * 2));
            }
            return tms;
        } else {
            return null;
        }
    }

    @Override
    public List<Integer> getHMMoves() {
        String tmDataPrefix = Gen5Constants.tmDataPrefix;
        int offset = find(arm9, tmDataPrefix);
        if (offset > 0) {
            offset += Gen5Constants.tmDataPrefix.length() / 2; // because it was
                                                               // a prefix
            offset += Gen5Constants.tmBlockOneCount * 2; // TM data
            List<Integer> hms = new ArrayList<>();
            for (int i = 0; i < Gen5Constants.hmCount; i++) {
                hms.add(readWord(arm9, offset + i * 2));
            }
            return hms;
        } else {
            return null;
        }
    }

    @Override
    public void setTMMoves(List<Integer> moveIndexes) {
        String tmDataPrefix = Gen5Constants.tmDataPrefix;
        int offset = find(arm9, tmDataPrefix);
        if (offset > 0) {
            offset += Gen5Constants.tmDataPrefix.length() / 2; // because it was
                                                               // a prefix
            for (int i = 0; i < Gen5Constants.tmBlockOneCount; i++) {
                writeWord(arm9, offset + i * 2, moveIndexes.get(i));
            }
            // Skip past those 92 TMs and 6 HMs
            offset += (Gen5Constants.tmBlockOneCount + Gen5Constants.hmCount) * 2;
            for (int i = 0; i < (Gen5Constants.tmCount - Gen5Constants.tmBlockOneCount); i++) {
                writeWord(arm9, offset + i * 2, moveIndexes.get(i + Gen5Constants.tmBlockOneCount));
            }

            // Update TM item descriptions
            List<String> itemDescriptions = getStrings(false, romEntry.getIntValue("ItemDescriptionsTextOffset"));
            List<String> moveDescriptions = getStrings(false, romEntry.getIntValue("MoveDescriptionsTextOffset"));
            // TM01 is item 328 and so on
            for (int i = 0; i < Gen5Constants.tmBlockOneCount; i++) {
                itemDescriptions.set(i + Gen5Constants.tmBlockOneOffset, moveDescriptions.get(moveIndexes.get(i)));
            }
            // TM93-95 are 618-620
            for (int i = 0; i < (Gen5Constants.tmCount - Gen5Constants.tmBlockOneCount); i++) {
                itemDescriptions.set(i + Gen5Constants.tmBlockTwoOffset,
                        moveDescriptions.get(moveIndexes.get(i + Gen5Constants.tmBlockOneCount)));
            }
            // Save the new item descriptions
            setStrings(false, romEntry.getIntValue("ItemDescriptionsTextOffset"), itemDescriptions);
            // Palettes
            String baseOfPalettes;
            if (romEntry.getRomType() == Gen5Constants.Type_BW) {
                baseOfPalettes = Gen5Constants.bw1ItemPalettesPrefix;
            } else {
                baseOfPalettes = Gen5Constants.bw2ItemPalettesPrefix;
            }
            int offsPals = find(arm9, baseOfPalettes);
            if (offsPals > 0) {
                // Write pals
                for (int i = 0; i < Gen5Constants.tmBlockOneCount; i++) {
                    int itmNum = Gen5Constants.tmBlockOneOffset + i;
                    Move m = this.moves[moveIndexes.get(i)];
                    int pal = this.typeTMPaletteNumber(m.type);
                    writeWord(arm9, offsPals + itmNum * 4 + 2, pal);
                }
                for (int i = 0; i < (Gen5Constants.tmCount - Gen5Constants.tmBlockOneCount); i++) {
                    int itmNum = Gen5Constants.tmBlockTwoOffset + i;
                    Move m = this.moves[moveIndexes.get(i + Gen5Constants.tmBlockOneCount)];
                    int pal = this.typeTMPaletteNumber(m.type);
                    writeWord(arm9, offsPals + itmNum * 4 + 2, pal);
                }
            }
        }
    }

    private static RomFunctions.StringSizeDeterminer ssd = encodedText -> {
        int offs = 0;
        int len = encodedText.length();
        while (encodedText.indexOf("\\x", offs) != -1) {
            len -= 5;
            offs = encodedText.indexOf("\\x", offs) + 1;
        }
        return len;
    };

    @Override
    public int getTMCount() {
        return Gen5Constants.tmCount;
    }

    @Override
    public int getHMCount() {
        return Gen5Constants.hmCount;
    }

    @Override
    public Map<Species, boolean[]> getTMHMCompatibility() {
        Map<Species, boolean[]> compat = new TreeMap<>();
        int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
        int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
        for (int i = 1; i <= Gen5Constants.pokemonCount + formeCount; i++) {
            byte[] data;
            if (i > Gen5Constants.pokemonCount) {
                data = pokeNarc.files.get(i + formeOffset);
            } else {
                data = pokeNarc.files.get(i);
            }
            Species pkmn = pokes[i];
            boolean[] flags = new boolean[Gen5Constants.tmCount + Gen5Constants.hmCount + 1];
            for (int j = 0; j < 13; j++) {
                readByteIntoFlags(data, flags, j * 8 + 1, Gen5Constants.bsTMHMCompatOffset + j);
            }
            compat.put(pkmn, flags);
        }
        return compat;
    }

    @Override
    public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
        int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int number = pkmn.getNumber();
            if (number > Gen5Constants.pokemonCount) {
                number += formeOffset;
            }
            byte[] data = pokeNarc.files.get(number);
            for (int j = 0; j < 13; j++) {
                data[Gen5Constants.bsTMHMCompatOffset + j] = getByteFromFlags(flags, j * 8 + 1);
            }
        }
    }

    @Override
    public boolean hasMoveTutors() {
        return romEntry.getRomType() == Gen5Constants.Type_BW2;
    }

    @Override
    public List<Integer> getMoveTutorMoves() {
        if (!hasMoveTutors()) {
            return new ArrayList<>();
        }
        int baseOffset = romEntry.getIntValue("MoveTutorDataOffset");
        int amount = Gen5Constants.bw2MoveTutorCount;
        int bytesPer = Gen5Constants.bw2MoveTutorBytesPerEntry;
        List<Integer> mtMoves = new ArrayList<>();
        try {
            byte[] mtFile = readOverlay(romEntry.getIntValue("MoveTutorOvlNumber"));
            for (int i = 0; i < amount; i++) {
                mtMoves.add(readWord(mtFile, baseOffset + i * bytesPer));
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return mtMoves;
    }

    @Override
    public void setMoveTutorMoves(List<Integer> moves) {
        if (!hasMoveTutors()) {
            return;
        }
        int baseOffset = romEntry.getIntValue("MoveTutorDataOffset");
        int amount = Gen5Constants.bw2MoveTutorCount;
        int bytesPer = Gen5Constants.bw2MoveTutorBytesPerEntry;
        if (moves.size() != amount) {
            return;
        }
        try {
            byte[] mtFile = readOverlay(romEntry.getIntValue("MoveTutorOvlNumber"));
            for (int i = 0; i < amount; i++) {
                writeWord(mtFile, baseOffset + i * bytesPer, moves.get(i));
            }
            writeOverlay(romEntry.getIntValue("MoveTutorOvlNumber"), mtFile);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public Map<Species, boolean[]> getMoveTutorCompatibility() {
        if (!hasMoveTutors()) {
            return new TreeMap<>();
        }
        Map<Species, boolean[]> compat = new TreeMap<>();
        int[] countsPersonalOrder = new int[] { 15, 17, 13, 15 };
        int[] countsMoveOrder = new int[] { 13, 15, 15, 17 };
        int[] personalToMoveOrder = new int[] { 1, 3, 0, 2 };
        int formeCount = Gen5Constants.getFormeCount(romEntry.getRomType());
        int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
        for (int i = 1; i <= Gen5Constants.pokemonCount + formeCount; i++) {
            byte[] data;
            if (i > Gen5Constants.pokemonCount) {
                data = pokeNarc.files.get(i + formeOffset);
            } else {
                data = pokeNarc.files.get(i);
            }
            Species pkmn = pokes[i];
            boolean[] flags = new boolean[Gen5Constants.bw2MoveTutorCount + 1];
            for (int mt = 0; mt < 4; mt++) {
                boolean[] mtflags = new boolean[countsPersonalOrder[mt] + 1];
                for (int j = 0; j < 4; j++) {
                    readByteIntoFlags(data, mtflags, j * 8 + 1, Gen5Constants.bsMTCompatOffset + mt * 4 + j);
                }
                int offsetOfThisData = 0;
                for (int cmoIndex = 0; cmoIndex < personalToMoveOrder[mt]; cmoIndex++) {
                    offsetOfThisData += countsMoveOrder[cmoIndex];
                }
                System.arraycopy(mtflags, 1, flags, offsetOfThisData + 1, countsPersonalOrder[mt]);
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
        int formeOffset = Gen5Constants.getFormeOffset(romEntry.getRomType());
        // BW2 move tutor flags aren't using the same order as the move tutor
        // move data.
        // We unscramble them from move data order to personal.narc flag order.
        int[] countsPersonalOrder = new int[] { 15, 17, 13, 15 };
        int[] countsMoveOrder = new int[] { 13, 15, 15, 17 };
        int[] personalToMoveOrder = new int[] { 1, 3, 0, 2 };
        for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
            Species pkmn = compatEntry.getKey();
            boolean[] flags = compatEntry.getValue();
            int number = pkmn.getNumber();
            if (number > Gen5Constants.pokemonCount) {
                number += formeOffset;
            }
            byte[] data = pokeNarc.files.get(number);
            for (int mt = 0; mt < 4; mt++) {
                int offsetOfThisData = 0;
                for (int cmoIndex = 0; cmoIndex < personalToMoveOrder[mt]; cmoIndex++) {
                    offsetOfThisData += countsMoveOrder[cmoIndex];
                }
                boolean[] mtflags = new boolean[countsPersonalOrder[mt] + 1];
                System.arraycopy(flags, offsetOfThisData + 1, mtflags, 1, countsPersonalOrder[mt]);
                for (int j = 0; j < 4; j++) {
                    data[Gen5Constants.bsMTCompatOffset + mt * 4 + j] = getByteFromFlags(mtflags, j * 8 + 1);
                }
            }
        }
    }

    private List<String> getStrings(boolean isStoryText, int index) {
        NARCArchive baseNARC = isStoryText ? storyTextNarc : stringsNarc;
        byte[] rawFile = baseNARC.files.get(index);
        return new ArrayList<>(PPTxtHandler.readTexts(rawFile));
    }

    private void setStrings(boolean isStoryText, int index, List<String> strings) {
        NARCArchive baseNARC = isStoryText ? storyTextNarc : stringsNarc;
        byte[] oldRawFile = baseNARC.files.get(index);
        byte[] newRawFile = PPTxtHandler.saveEntry(oldRawFile, strings);
        baseNARC.files.set(index, newRawFile);
    }

    @Override
    public boolean hasEncounterLocations() {
        return true;
    }

    @Override
    public boolean hasMapIndices() {
        return true;
    }

    @Override
    public boolean hasTimeBasedEncounters() {
        return true; // All BW/BW2 do [seasons]
    }

    @Override
    public boolean hasWildAltFormes() {
        return true;
    }

    @Override
    public boolean hasDarkGrassHeldItems() {
        return true;
    }

    private void populateEvolutions() {
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                pkmn.getEvolutionsFrom().clear();
                pkmn.getEvolutionsTo().clear();
            }
        }

        // Read NARC
        try {
            NARCArchive evoNARC = readNARC(romEntry.getFile("PokemonEvolutions"));
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                Species pk = pokes[i];
                byte[] evoEntry = evoNARC.files.get(i);
                for (int evo = 0; evo < 7; evo++) {
                    int method = readWord(evoEntry, evo * 6);
                    int species = readWord(evoEntry, evo * 6 + 4);
                    if (method >= 1 && method <= Gen5Constants.evolutionMethodCount && species >= 1) {
                        EvolutionType et = Gen5Constants.evolutionTypeFromIndex(method);
                        if (et.equals(EvolutionType.LEVEL_HIGH_BEAUTY)) continue; // Remove Feebas "split" evolution
                        int extraInfo = readWord(evoEntry, evo * 6 + 2);
                        Evolution evol = new Evolution(pk, pokes[species], et, extraInfo);
                        if (!pk.getEvolutionsFrom().contains(evol)) {
                            pk.getEvolutionsFrom().add(evol);
                            pokes[species].getEvolutionsTo().add(evol);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void writeEvolutions() {
        splitLevelItemEvolutions();
        try {
            NARCArchive evoNARC = readNARC(romEntry.getFile("PokemonEvolutions"));
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                byte[] evoEntry = evoNARC.files.get(i);
                Species pk = pokes[i];
                if (pk.getNumber() == SpeciesIDs.nincada && romEntry.hasTweakFile("ShedinjaEvolutionTweak")) {
                    writeShedinjaEvolution();
                }
                int evosWritten = 0;
                for (Evolution evo : pk.getEvolutionsFrom()) {
                    writeWord(evoEntry, evosWritten * 6, Gen5Constants.evolutionTypeToIndex(evo.getType()));
                    writeWord(evoEntry, evosWritten * 6 + 2, evo.getExtraInfo());
                    writeWord(evoEntry, evosWritten * 6 + 4, evo.getTo().getNumber());
                    evosWritten++;
                    if (evosWritten == 7) {
                        break;
                    }
                }
                while (evosWritten < 7) {
                    writeWord(evoEntry, evosWritten * 6, 0);
                    writeWord(evoEntry, evosWritten * 6 + 2, 0);
                    writeWord(evoEntry, evosWritten * 6 + 4, 0);
                    evosWritten++;
                }
            }
            writeNARC(romEntry.getFile("PokemonEvolutions"), evoNARC);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        mergeLevelItemEvolutions();
    }

    private void writeShedinjaEvolution() throws IOException {
        Species nincada = pokes[SpeciesIDs.nincada];

        // When the "Limit Pokemon" setting is enabled and Gen 3 is disabled, or when
        // "Random Every Level" evolutions are selected, we end up clearing out Nincada's
        // vanilla evolutions. In that case, there's no point in even worrying about
        // Shedinja, so just return.
        if (nincada.getEvolutionsFrom().size() < 2) {
            return;
        }
        Species extraEvolution = nincada.getEvolutionsFrom().get(1).getTo();

        // Update the evolution overlay to point towards our custom code in the expanded arm9.
        byte[] evolutionOverlay = readOverlay(romEntry.getIntValue("EvolutionOvlNumber"));
        genericIPSPatch(evolutionOverlay, "ShedinjaEvolutionOvlTweak");
        writeOverlay(romEntry.getIntValue("EvolutionOvlNumber"), evolutionOverlay);

        // Relies on arm9 already being extended, which it *should* have been in loadedROM
        genericIPSPatch(arm9, "ShedinjaEvolutionTweak");

        // After applying the tweak, Shedinja's ID is simply pc-relative loaded, so just
        // update the constant
        int offset = romEntry.getIntValue("ShedinjaSpeciesOffset");
        if (offset > 0) {
            FileFunctions.writeFullInt(arm9, offset, extraEvolution.getNumber());
        }
    }

    @Override
    public void removeImpossibleEvolutions(boolean changeMoveEvos) {

        Map<Integer, List<MoveLearnt>> movesets = this.getMovesLearnt();
        for (Species pkmn : pokes) {
            if (pkmn != null) {
                for (Evolution evo : pkmn.getEvolutionsFrom()) {
                    if (changeMoveEvos && evo.getType() == EvolutionType.LEVEL_WITH_MOVE) {
                        // read move
                        int move = evo.getExtraInfo();
                        int levelLearntAt = 1;
                        for (MoveLearnt ml : movesets.get(evo.getFrom().getNumber())) {
                            if (ml.move == move) {
                                levelLearntAt = ml.level;
                                break;
                            }
                        }
                        if (levelLearntAt == 1) {
                            // override for piloswine
                            levelLearntAt = 45;
                        }
                        // change to pure level evo
                        markImprovedEvolutions(pkmn);
                        evo.setType(EvolutionType.LEVEL);
                        evo.setExtraInfo(levelLearntAt);
                    }
                    // Pure Trade
                    if (evo.getType() == EvolutionType.TRADE) {
                        // Replace w/ level 37
                        markImprovedEvolutions(pkmn);
                        evo.setType(EvolutionType.LEVEL);
                        evo.setExtraInfo(37);
                    }
                    // Trade w/ Item
                    if (evo.getType() == EvolutionType.TRADE_ITEM) {
                        markImprovedEvolutions(pkmn);
                        if (evo.getFrom().getNumber() == SpeciesIDs.slowpoke) {
                            // Slowpoke is awkward - it already has a level evo
                            // So we can't do Level up w/ Held Item
                            // Put Water Stone instead
                            evo.setType(EvolutionType.STONE);
                            evo.setExtraInfo(ItemIDs.waterStone);
                        } else {
                            evo.setType(EvolutionType.LEVEL_ITEM);
                        }
                    }
                    if (evo.getType() == EvolutionType.TRADE_SPECIAL) {
                        // This is the karrablast <-> shelmet trade
                        // Replace it with Level up w/ Other Species in Party
                        // (22)
                        // Based on what species we're currently dealing with
                        markImprovedEvolutions(pkmn);
                        evo.setType(EvolutionType.LEVEL_WITH_OTHER);
                        evo.setExtraInfo((evo.getFrom().getNumber() == SpeciesIDs.karrablast ? SpeciesIDs.shelmet : SpeciesIDs.karrablast));
                    }
                }
            }
        }

    }

    @Override
    public void makeEvolutionsEasier(boolean changeWithOtherEvos) {

        // Reduce the amount of happiness required to evolve.
        int offset = find(arm9, Gen5Constants.friendshipValueForEvoLocator);
        if (offset > 0) {
            // Amount of required happiness for HAPPINESS evolutions.
            if (arm9[offset] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
                arm9[offset] = (byte) GlobalConstants.easierHappinessToEvolve;
            }
            // Amount of required happiness for HAPPINESS_DAY evolutions.
            if (arm9[offset + 20] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
                arm9[offset + 20] = (byte) GlobalConstants.easierHappinessToEvolve;
            }
            // Amount of required happiness for HAPPINESS_NIGHT evolutions.
            if (arm9[offset + 38] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
                arm9[offset + 38] = (byte) GlobalConstants.easierHappinessToEvolve;
            }
        }

        if (changeWithOtherEvos) {
            for (Species pkmn : pokes) {
                if (pkmn != null) {
                    for (Evolution evo : pkmn.getEvolutionsFrom()) {
                        if (evo.getType() == EvolutionType.LEVEL_WITH_OTHER) {
                            // Replace w/ level 35
                            markImprovedEvolutions(pkmn);
                            evo.setType(EvolutionType.LEVEL);
                            evo.setExtraInfo(35);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getLocationNamesForEvolution(EvolutionType et) {
        if (!et.usesLocation()) {
            throw new IllegalArgumentException(et + " is not a location-based EvolutionType.");
        }
        if (!loadedWildMapNames) {
            loadWildMapNames();
        }
        int mapIndex = Gen5Constants.getMapIndexForLocationEvolution(et, romEntry.getRomType());
        return Collections.singletonList(wildMapNames.get(mapIndex));
    }

    @Override
    public boolean hasShopSupport() {
        return true;
    }

    @Override
    public boolean canChangeShopSizes() {
        return romEntry.getRomType() == Gen5Constants.Type_BW2;
    }

    @Override
    public boolean canChangeTrainerText() {
        return true;
    }

    @Override
    public List<String> getTrainerNames() {
        List<String> tnames = getStrings(false, romEntry.getIntValue("TrainerNamesTextOffset"));
        tnames.remove(0); // blank one
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            List<String> pwtNames = getStrings(false, romEntry.getIntValue("PWTTrainerNamesTextOffset"));
            tnames.addAll(pwtNames);
        }
        // Tack the mugshot names on the end
        List<String> mnames = getStrings(false, romEntry.getIntValue("TrainerMugshotsTextOffset"));
        for (String mname : mnames) {
            if (!mname.isEmpty() && (mname.charAt(0) >= 'A' && mname.charAt(0) <= 'Z')) {
                tnames.add(mname);
            }
        }
        return tnames;
    }

    @Override
    public int maxTrainerNameLength() {
        return 10;// based off the english ROMs
    }

    @Override
    public void setTrainerNames(List<String> trainerNames) {
        List<String> tnames = getStrings(false, romEntry.getIntValue("TrainerNamesTextOffset"));
        // Grab the mugshot names off the back of the list of trainer names
        // we got back
        List<String> mnames = getStrings(false, romEntry.getIntValue("TrainerMugshotsTextOffset"));
        int trNamesSize = trainerNames.size();
        for (int i = mnames.size() - 1; i >= 0; i--) {
            String origMName = mnames.get(i);
            if (!origMName.isEmpty() && (origMName.charAt(0) >= 'A' && origMName.charAt(0) <= 'Z')) {
                // Grab replacement
                String replacement = trainerNames.remove(--trNamesSize);
                mnames.set(i, replacement);
            }
        }
        // Save back mugshot names
        setStrings(false, romEntry.getIntValue("TrainerMugshotsTextOffset"), mnames);

        // Now save the rest of trainer names
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            List<String> pwtNames = getStrings(false, romEntry.getIntValue("PWTTrainerNamesTextOffset"));
            List<String> newTNames = new ArrayList<>();
            List<String> newPWTNames = new ArrayList<>();
            newTNames.add(0, tnames.get(0)); // the 0-entry, preserve it
            for (int i = 1; i < tnames.size() + pwtNames.size(); i++) {
                if (i < tnames.size()) {
                    newTNames.add(trainerNames.get(i - 1));
                } else {
                    newPWTNames.add(trainerNames.get(i - 1));
                }
            }
            setStrings(false, romEntry.getIntValue("TrainerNamesTextOffset"), newTNames);
            setStrings(false, romEntry.getIntValue("PWTTrainerNamesTextOffset"), newPWTNames);
        } else {
            List<String> newTNames = new ArrayList<>(trainerNames);
            newTNames.add(0, tnames.get(0)); // the 0-entry, preserve it
            setStrings(false, romEntry.getIntValue("TrainerNamesTextOffset"), newTNames);
        }
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
    public List<String> getTrainerClassNames() {
        List<String> classNames = getStrings(false, romEntry.getIntValue("TrainerClassesTextOffset"));
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            classNames.addAll(getStrings(false, romEntry.getIntValue("PWTTrainerClassesTextOffset")));
        }
        return classNames;
    }

    @Override
    public void setTrainerClassNames(List<String> trainerClassNames) {
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            List<String> newTClasses = new ArrayList<>();
            List<String> newPWTClasses = new ArrayList<>();
            List<String> classNames = getStrings(false, romEntry.getIntValue("TrainerClassesTextOffset"));
            List<String> pwtClassNames = getStrings(false, romEntry.getIntValue("PWTTrainerClassesTextOffset"));
            for (int i = 0; i < classNames.size() + pwtClassNames.size(); i++) {
                if (i < classNames.size()) {
                    newTClasses.add(trainerClassNames.get(i));
                } else {
                    newPWTClasses.add(trainerClassNames.get(i));
                }
            }
            setStrings(false, romEntry.getIntValue("TrainerClassesTextOffset"), newTClasses);
            setStrings(false, romEntry.getIntValue("PWTTrainerClassesTextOffset"), newPWTClasses);
        } else {
            setStrings(false, romEntry.getIntValue("TrainerClassesTextOffset"), trainerClassNames);
        }
    }

    @Override
    public int maxTrainerClassNameLength() {
        return 12;// based off the english ROMs
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
    public String getDefaultExtension() {
        return "nds";
    }

    @Override
    public int abilitiesPerSpecies() {
        return 3;
    }

    @Override
    public int highestAbilityIndex() {
        return Gen5Constants.highestAbilityIndex;
    }

    @Override
    public int internalStringLength(String string) {
        return ssd.lengthFor(string);
    }

    @Override
    public boolean setIntroPokemon(Species pk) {
        try {
            int introPokemon = pk.getNumber();
            // Assume alt formes can't be used. I haven't actually tested this, but it seemed like the safer guess.
            if (!pk.isBaseForme()) {
                return false;
            }
            byte[] introGraphicOverlay = readOverlay(romEntry.getIntValue("IntroGraphicOvlNumber"));
            int offset = find(introGraphicOverlay, Gen5Constants.introGraphicPrefix);
            if (offset > 0) {
                offset += Gen5Constants.introGraphicPrefix.length() / 2; // because it was a prefix
                // offset is now pointing at the species constant that gets pc-relative
                // loaded to determine what sprite to load.
                writeWord(introGraphicOverlay, offset, introPokemon);
                writeOverlay(romEntry.getIntValue("IntroGraphicOvlNumber"), introGraphicOverlay);
            }

            if (romEntry.getRomType() == Gen5Constants.Type_BW) {
                byte[] introCryOverlay = readOverlay(romEntry.getIntValue("IntroCryOvlNumber"));
                offset = find(introCryOverlay, Gen5Constants.bw1IntroCryPrefix);
                if (offset > 0) {
                    offset += Gen5Constants.bw1IntroCryPrefix.length() / 2; // because it was a prefix
                    // The function starting from the offset looks like this:
                    // mov r0, #0x8f
                    // str r1, [sp, #local_94]
                    // lsl r0, r0, #0x2
                    // mov r2, #0x40
                    // mov r3, #0x0
                    // bl PlayCry
                    // [rest of the function...]
                    // pop { r3, r4, r5, r6, r7, pc }
                    // C0 46 (these are useless padding bytes)
                    // To make this more extensible, we want to pc-relative load a species ID into r0 instead.
                    // Start by moving everything below the left shift up by 2 bytes. We won't need the left
                    // shift later, and it will give us 4 bytes after the pop to use for the ID.
                    for (int i = offset + 6; i < offset + 40; i++) {
                        introCryOverlay[i - 2] = introCryOverlay[i];
                    }

                    // The call to PlayCry needs to be adjusted as well, since it got moved.
                    introCryOverlay[offset + 10]++;

                    // Now write the species ID in the 4 bytes of space now available at the bottom,
                    // and then write a pc-relative load to this species ID at the offset.
                    FileFunctions.writeFullInt(introCryOverlay, offset + 38, introPokemon);
                    introCryOverlay[offset] = 0x9;
                    introCryOverlay[offset + 1] = 0x48;
                    writeOverlay(romEntry.getIntValue("IntroCryOvlNumber"), introCryOverlay);
                }
            } else {
                byte[] introCryOverlay = readOverlay(romEntry.getIntValue("IntroCryOvlNumber"));
                offset = find(introCryOverlay, Gen5Constants.bw2IntroCryLocator);
                if (offset > 0) {
                    // offset is now pointing at the species constant that gets pc-relative
                    // loaded to determine what cry to play.
                    writeWord(introCryOverlay, offset, introPokemon);
                    writeOverlay(romEntry.getIntValue("IntroCryOvlNumber"), introCryOverlay);
                }
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return true;
    }

    @Override
    public Set<Item> getRegularShopItems() {
        return itemIdsToSet(Gen5Constants.regularShopItems);
    }

    @Override
    public Set<Item> getOPShopItems() {
        return itemIdsToSet(Gen5Constants.opShopItems);
    }

    @Override
    public String abilityName(int number) {
        return abilityNames.get(number);
    }

    @Override
    public Map<Integer, List<Integer>> getAbilityVariations() {
        return Gen5Constants.abilityVariations;
    }

    @Override
    public List<Integer> getUselessAbilities() {
        return new ArrayList<>(Gen5Constants.uselessAbilities);
    }

    @Override
    public boolean isTrainerPokemonUseBaseFormeAbilities() {
        return true;
    }

    @Override
    public boolean hasMegaEvolutions() {
        return false;
    }

    private List<Integer> getFieldItemIds() {
        List<Integer> fieldItems = new ArrayList<>();
        // normal items
        int scriptFileNormal = romEntry.getIntValue("ItemBallsScriptOffset");
        int scriptFileHidden = romEntry.getIntValue("HiddenItemsScriptOffset");
        int[] skipTable = romEntry.getArrayValue("ItemBallsSkip");
        int[] skipTableH = romEntry.getArrayValue("HiddenItemsSkip");
        int setVarNormal = Gen5Constants.normalItemSetVarCommand;
        int setVarHidden = Gen5Constants.hiddenItemSetVarCommand;

        byte[] itemScripts = scriptNarc.files.get(scriptFileNormal);
        int offset = 0;
        int skipTableOffset = 0;
        while (true) {
            int part1 = readWord(itemScripts, offset);
            if (part1 == Gen5Constants.scriptListTerminator) {
                // done
                break;
            }
            int offsetInFile = readRelativePointer(itemScripts, offset);
            offset += 4;
            if (offsetInFile > itemScripts.length) {
                break;
            }
            if (skipTableOffset < skipTable.length && (skipTable[skipTableOffset] == (offset / 4) - 1)) {
                skipTableOffset++;
                continue;
            }
            int command = readWord(itemScripts, offsetInFile + 2);
            int variable = readWord(itemScripts, offsetInFile + 4);
            if (command == setVarNormal && variable == Gen5Constants.normalItemVarSet) {
                int item = readWord(itemScripts, offsetInFile + 6);
                fieldItems.add(item);
            }

        }

        // hidden items
        byte[] hitemScripts = scriptNarc.files.get(scriptFileHidden);
        offset = 0;
        skipTableOffset = 0;
        while (true) {
            int part1 = readWord(hitemScripts, offset);
            if (part1 == Gen5Constants.scriptListTerminator) {
                // done
                break;
            }
            int offsetInFile = readRelativePointer(hitemScripts, offset);
            if (offsetInFile > hitemScripts.length) {
                break;
            }
            offset += 4;
            if (skipTableOffset < skipTable.length && (skipTableH[skipTableOffset] == (offset / 4) - 1)) {
                skipTableOffset++;
                continue;
            }
            int command = readWord(hitemScripts, offsetInFile + 2);
            int variable = readWord(hitemScripts, offsetInFile + 4);
            if (command == setVarHidden && variable == Gen5Constants.hiddenItemVarSet) {
                int item = readWord(hitemScripts, offsetInFile + 6);
                fieldItems.add(item);
            }

        }

        return fieldItems;
    }

    private void setFieldItemIds(List<Integer> fieldItems) {
        Iterator<Integer> iterItems = fieldItems.iterator();

        // normal items
        int scriptFileNormal = romEntry.getIntValue("ItemBallsScriptOffset");
        int scriptFileHidden = romEntry.getIntValue("HiddenItemsScriptOffset");
        int[] skipTable = romEntry.getArrayValue("ItemBallsSkip");
        int[] skipTableH = romEntry.getArrayValue("HiddenItemsSkip");
        int setVarNormal = Gen5Constants.normalItemSetVarCommand;
        int setVarHidden = Gen5Constants.hiddenItemSetVarCommand;

        byte[] itemScripts = scriptNarc.files.get(scriptFileNormal);
        int offset = 0;
        int skipTableOffset = 0;
        while (true) {
            int part1 = readWord(itemScripts, offset);
            if (part1 == Gen5Constants.scriptListTerminator) {
                // done
                break;
            }
            int offsetInFile = readRelativePointer(itemScripts, offset);
            offset += 4;
            if (offsetInFile > itemScripts.length) {
                break;
            }
            if (skipTableOffset < skipTable.length && (skipTable[skipTableOffset] == (offset / 4) - 1)) {
                skipTableOffset++;
                continue;
            }
            int command = readWord(itemScripts, offsetInFile + 2);
            int variable = readWord(itemScripts, offsetInFile + 4);
            if (command == setVarNormal && variable == Gen5Constants.normalItemVarSet) {
                int item = iterItems.next();
                writeWord(itemScripts, offsetInFile + 6, item);
            }

        }

        // hidden items
        byte[] hitemScripts = scriptNarc.files.get(scriptFileHidden);
        offset = 0;
        skipTableOffset = 0;
        while (true) {
            int part1 = readWord(hitemScripts, offset);
            if (part1 == Gen5Constants.scriptListTerminator) {
                // done
                break;
            }
            int offsetInFile = readRelativePointer(hitemScripts, offset);
            offset += 4;
            if (offsetInFile > hitemScripts.length) {
                break;
            }
            if (skipTableOffset < skipTable.length && (skipTableH[skipTableOffset] == (offset / 4) - 1)) {
                skipTableOffset++;
                continue;
            }
            int command = readWord(hitemScripts, offsetInFile + 2);
            int variable = readWord(hitemScripts, offsetInFile + 4);
            if (command == setVarHidden && variable == Gen5Constants.hiddenItemVarSet) {
                int item = iterItems.next();
                writeWord(hitemScripts, offsetInFile + 6, item);
            }

        }
    }

    @Override
    public List<Item> getFieldItems() {
        List<Integer> fieldItemsIds = getFieldItemIds();
        List<Item> fieldItems = new ArrayList<>();

        for (int id : fieldItemsIds) {
            Item item = items.get(id);
            if (item.isAllowed()) {
                fieldItems.add(item);
            }
        }

        return fieldItems;
    }

    @Override
    public void setFieldItems(List<Item> fieldItems) {
        checkFieldItemsTMsReplaceTMs(fieldItems);

        List<Integer> fieldItemsIds = getFieldItemIds();
        Iterator<Item> iterItems = fieldItems.iterator();

        for (int i = 0; i < fieldItemsIds.size(); i++) {
            Item current = items.get(fieldItemsIds.get(i));
            if (current.isAllowed()) {
                // Replace it
                fieldItemsIds.set(i, iterItems.next().getId());
            }
        }

        this.setFieldItemIds(fieldItemsIds);
    }

    @Override
    public Set<Item> getRequiredFieldTMs() {
        return itemIdsToSet(romEntry.getRomType() == Gen5Constants.Type_BW ?
                Gen5Constants.bw1RequiredFieldTMs : Gen5Constants.bw2RequiredFieldTMs);
    }

    @Override
    public List<InGameTrade> getInGameTrades() {
        List<InGameTrade> trades = new ArrayList<>();
        try {
            NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
            List<String> tradeStrings = getStrings(false, romEntry.getIntValue("IngameTradesTextOffset"));
            int[] unused = romEntry.getArrayValue("TradesUnused");
            int unusedOffset = 0;
            int tableSize = tradeNARC.files.size();

            for (int entry = 0; entry < tableSize; entry++) {
                if (unusedOffset < unused.length && unused[unusedOffset] == entry) {
                    unusedOffset++;
                    continue;
                }
                InGameTrade trade = new InGameTrade();
                byte[] tfile = tradeNARC.files.get(entry);
                trade.setNickname(tradeStrings.get(entry * 2));
                trade.setGivenSpecies(pokes[readLong(tfile, 4)]);
                trade.setIVs(new int[6]);
                for (int iv = 0; iv < 6; iv++) {
                    trade.getIVs()[iv] = readLong(tfile, 0x10 + iv * 4);
                }
                trade.setOtId(readWord(tfile, 0x34));
                trade.setHeldItem(items.get(readLong(tfile, 0x4C)));
                trade.setOtName(tradeStrings.get(entry * 2 + 1));
                trade.setRequestedSpecies(pokes[readLong(tfile, 0x5C)]);
                trades.add(trade);
            }
        } catch (Exception ex) {
            throw new RomIOException(ex);
        }

        return trades;

    }

    @Override
    public void setInGameTrades(List<InGameTrade> trades) {
        // info
        int tradeOffset = 0;
        List<InGameTrade> oldTrades = this.getInGameTrades();
        try {
            NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
            List<String> tradeStrings = getStrings(false, romEntry.getIntValue("IngameTradesTextOffset"));
            int tradeCount = tradeNARC.files.size();
            int[] unused = romEntry.getArrayValue("TradesUnused");
            int unusedOffset = 0;
            for (int i = 0; i < tradeCount; i++) {
                if (unusedOffset < unused.length && unused[unusedOffset] == i) {
                    unusedOffset++;
                    continue;
                }
                byte[] tfile = tradeNARC.files.get(i);
                InGameTrade trade = trades.get(tradeOffset++);
                tradeStrings.set(i * 2, trade.getNickname());
                tradeStrings.set(i * 2 + 1, trade.getOtName());
                writeLong(tfile, 4, trade.getGivenSpecies().getNumber());
                writeLong(tfile, 8, 0); // disable forme
                for (int iv = 0; iv < 6; iv++) {
                    writeLong(tfile, 0x10 + iv * 4, trade.getIVs()[iv]);
                }
                writeLong(tfile, 0x2C, 0xFF); // random nature
                writeWord(tfile, 0x34, trade.getOtId());
                writeLong(tfile, 0x4C, trade.getHeldItem() == null ? 0 : trade.getHeldItem().getId());
                writeLong(tfile, 0x5C, trade.getRequestedSpecies().getNumber());
                if (!romEntry.getTradeScripts().isEmpty()) {
                    romEntry.getTradeScripts().get(i - unusedOffset).setPokemon(this,scriptNarc, trade.getRequestedSpecies(), trade.getGivenSpecies());
                }
            }
            this.writeNARC(romEntry.getFile("InGameTrades"), tradeNARC);
            this.setStrings(false, romEntry.getIntValue("IngameTradesTextOffset"), tradeStrings);
            // update what the people say when they talk to you
            unusedOffset = 0;
            int[] textOffsets = romEntry.getArrayValue("IngameTradePersonTextOffsets");
            for (int tr = 0; tr < textOffsets.length; tr++) {
                if (unusedOffset < unused.length && unused[unusedOffset] == tr+24) {
                    unusedOffset++;
                    continue;
                }
                if (textOffsets[tr] > 0) {
                    if (tr+24 >= oldTrades.size() || tr+24 >= trades.size()) {
                        break;
                    }
                    InGameTrade oldTrade = oldTrades.get(tr+24);
                    InGameTrade newTrade = trades.get(tr+24);
                    Map<String, String> replacements = new TreeMap<>();
                    replacements.put(oldTrade.getGivenSpecies().getName(), newTrade.getGivenSpecies().getName());
                    if (oldTrade.getRequestedSpecies() != newTrade.getRequestedSpecies()) {
                        replacements.put(oldTrade.getRequestedSpecies().getName(), newTrade.getRequestedSpecies().getName());
                    }
                    replaceAllStringsInEntry(textOffsets[tr], replacements);
                }
            }
        } catch (IOException ex) {
            throw new RomIOException(ex);
        }
    }

    private void replaceAllStringsInEntry(int entry, Map<String, String> replacements) {
        List<String> thisTradeStrings = this.getStrings(true, entry);
        int ttsCount = thisTradeStrings.size();
        for (int strNum = 0; strNum < ttsCount; strNum++) {
            String newString = thisTradeStrings.get(strNum);
            for (String old: replacements.keySet()) {
                newString = newString.replaceAll(old,replacements.get(old));
            }
            thisTradeStrings.set(strNum, newString);
        }
        this.setStrings(true, entry, thisTradeStrings);
    }

    @Override
    public boolean hasDVs() {
        return false;
    }

    @Override
    public int generationOfPokemon() {
        return 5;
    }

    @Override
    public void removeEvosForPokemonPool() {
        // slightly more complicated than gen2/3
        // we have to update a "baby table" too
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

        try {
            NARCArchive babyNARC = readNARC(romEntry.getFile("BabyPokemon"));
            // baby pokemon
            for (int i = 1; i <= Gen5Constants.pokemonCount; i++) {
                Species baby = pokes[i];
                while (!baby.getEvolutionsTo().isEmpty()) {
                    // Grab the first "to evolution" even if there are multiple
                    baby = baby.getEvolutionsTo().get(0).getFrom();
                }
                writeWord(babyNARC.files.get(i), 0, baby.getNumber());
            }
            // finish up
            writeNARC(romEntry.getFile("BabyPokemon"), babyNARC);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public boolean supportsFourStartingMoves() {
        return true;
    }

    @Override
    public List<Integer> getFieldMoves() {
        // cut, fly, surf, strength, flash, dig, teleport, waterfall,
        // sweet scent, dive
        return Gen5Constants.fieldMoves;
    }

    @Override
    public List<Integer> getEarlyRequiredHMMoves() {
        // BW1: cut
        // BW2: none
        if (romEntry.getRomType() == Gen5Constants.Type_BW2) {
            return Gen5Constants.bw2EarlyRequiredHMMoves;
        } else {
            return Gen5Constants.bw1EarlyRequiredHMMoves;
        }
    }

    @Override
    public List<Shop> getShops() {
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            return getShopsBW();
        } else {
            return getShopsBW2();
        }
    }

    private List<Shop> getShopsBW() {
        int overlayNum = romEntry.getIntValue("ShopItemOvlNumber");
        int pointerTableOffset = romEntry.getIntValue("ShopPointerTableOffset");
        int sizeTableOffset = romEntry.getIntValue("ShopSizeTableOffset");
        int shopCount = romEntry.getIntValue("ShopCount");
        List<Shop> shops = new ArrayList<>();

        try {
            byte[] shopItemOverlay = readOverlay(overlayNum);

            for (int i = 0; i < shopCount; i++) {
                List<Item> shopItems = new ArrayList<>();

                int itemsOffset = readOverlayPointer(shopItemOverlay, overlayNum, pointerTableOffset + i * 4);
                int itemsSize = shopItemOverlay[sizeTableOffset + i];
                for (int j = 0; j < itemsSize; j++) {
                    int id = readWord(shopItemOverlay, itemsOffset + j * 2);
                    shopItems.add(items.get(id));
                }

                Shop shop = new Shop();
                shop.setItems(shopItems);
                shop.setName(shopNames.get(i));
                shop.setMainGame(Gen5Constants.getMainGameShops(romEntry.getRomType()).contains(i));
                shops.add(shop);
            }

            // TODO: re-mark which are the TM/regular shops
//            int[] tmShops = romEntry.getArrayValue("TMShops");
//            int[] regularShops = romEntry.getArrayValue("RegularShops");
//
//            Arrays.stream(tmShops).forEach(i -> shops.get(i).setSpecialShop(false));
//            Arrays.stream(regularShops).forEach(i -> shops.get(i).setSpecialShop(false));

            return shops;
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private List<Shop> getShopsBW2() {
        int shopCount = romEntry.getIntValue("ShopCount");
        List<Shop> shops = new ArrayList<>();

        for (int i = 0; i < shopCount; i++) {
            List<Item> shopItems = new ArrayList<>();
            byte[] shopData = shopNarc.files.get(i);
            for (int j = 0; j < shopData.length; j += 2) {
                int id = readWord(shopData, j);
                shopItems.add(items.get(id));
            }

            Shop shop = new Shop();
            shop.setItems(shopItems);
            shop.setName(shopNames.get(i));
            shop.setMainGame(Gen5Constants.getMainGameShops(romEntry.getRomType()).contains(i));
            shops.add(shop);
        }

        int[] tmShops = romEntry.getArrayValue("TMShops");
        int[] regularShops = romEntry.getArrayValue("RegularShops");

        Arrays.stream(tmShops).forEach(i -> shops.get(i).setSpecialShop(false));
        Arrays.stream(regularShops).forEach(i -> shops.get(i).setSpecialShop(false));

        return shops;
    }

    @Override
    public void setShops(List<Shop> shops) {
        if (romEntry.getRomType() == Gen5Constants.Type_BW) {
            setShopsBW(shops);
        } else {
            setShopsBW2(shops);
        }
    }

    private void setShopsBW(List<Shop> shops) {
        int overlayNum = romEntry.getIntValue("ShopItemOvlNumber");
        int pointerTableOffset = romEntry.getIntValue("ShopPointerTableOffset");
        int sizeTableOffset = romEntry.getIntValue("ShopSizeTableOffset");
        int shopCount = romEntry.getIntValue("ShopCount");
        if (shops.size() != shopCount) {
            throw new IllegalArgumentException("shops.size() must be: " + shopCount
                + ", is: " + shops.size());
        }

        try {
            // TODO: repoint the shops so we can have variable sizes, that's the whole point of this rewrite haha
            byte[] shopItemOverlay = readOverlay(overlayNum);

            for (int i = 0; i < shopCount; i++) {
                List<Item> shopItems = shops.get(i).getItems();

                int itemsOffset = readOverlayPointer(shopItemOverlay, overlayNum, pointerTableOffset + i * 4);
                int itemsSize = shopItemOverlay[sizeTableOffset + i];
                if (shopItems.size() != itemsSize) {
                    throw new IllegalArgumentException("Incorrect shop size.");
                }

                for (int j = 0; j < itemsSize; j++) {
                    int id = shopItems.get(j).getId();
                    writeWord(shopItemOverlay, itemsOffset + j * 2, id);
                }
            }
            writeOverlay(overlayNum, shopItemOverlay);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void setShopsBW2(List<Shop> shops) {
        int shopCount = romEntry.getIntValue("ShopCount");

        try {
            for (int i = 0; i < shopCount; i++) {
                List<Item> shopContents = shops.get(i).getItems();
                Iterator<Item> iterItems = shopContents.iterator();
                byte[] shop = new byte[shopContents.size() * 2];
                for (int j = 0; j < shop.length; j += 2) {
                    writeWord(shop, j, iterItems.next().getId());
                }
                shopNarc.files.set(i, shop);
            }
            writeNARC(romEntry.getFile("ShopItems"), shopNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public List<Integer> getShopPrices() {
        List<Integer> prices = new ArrayList<>();
        prices.add(0);
        try {
            NARCArchive itemPriceNarc = this.readNARC(romEntry.getFile("ItemData"));
            for (int i = 1; i < itemPriceNarc.files.size(); i++) {
                prices.add(readWord(itemPriceNarc.files.get(i), 0) * 10);
            }
            writeNARC(romEntry.getFile("ItemData"), itemPriceNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return prices;
    }

    @Override
    public void setBalancedShopPrices() {
        List<Integer> prices = getShopPrices();
        for (Map.Entry<Integer, Integer> entry : Gen5Constants.balancedItemPrices.entrySet()) {
            prices.set(entry.getKey(), entry.getValue());
        }
        setShopPrices(prices);
    }

    @Override
    // Internally, item prices are stored as multiples of 10,
    // so the last digit of each input price will be ignored.
    public void setShopPrices(List<Integer> prices) {
        try {
            NARCArchive itemPriceNarc = this.readNARC(romEntry.getFile("ItemData"));
            for (int i = 1; i < itemPriceNarc.files.size(); i++) {
                writeWord(itemPriceNarc.files.get(i), 0, prices.get(i) / 10);
            }
            writeNARC(romEntry.getFile("ItemData"), itemPriceNarc);
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public List<PickupItem> getPickupItems() {
        List<PickupItem> pickupItems = new ArrayList<>();
        try {
            byte[] battleOverlay = readOverlay(romEntry.getIntValue("PickupOvlNumber"));

            // If we haven't found the pickup table for this ROM already, find it.
            if (pickupItemsTableOffset == 0) {
                int offset = find(battleOverlay, Gen5Constants.pickupTableLocator);
                if (offset > 0) {
                    pickupItemsTableOffset = offset;
                }
            }

            // Assuming we've found the pickup table, extract the items out of it.
            if (pickupItemsTableOffset > 0) {
                for (int i = 0; i < Gen5Constants.numberOfPickupItems; i++) {
                    int itemOffset = pickupItemsTableOffset + (2 * i);
                    int id = FileFunctions.read2ByteInt(battleOverlay, itemOffset);
                    PickupItem pickupItem = new PickupItem(items.get(id));
                    pickupItems.add(pickupItem);
                }
            }

            // Assuming we got the items from the last step, fill out the probabilities.
            if (!pickupItems.isEmpty()) {
                for (int levelRange = 0; levelRange < 10; levelRange++) {
                    int startingRareItemOffset = levelRange;
                    int startingCommonItemOffset = 11 + levelRange;
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
        } catch (IOException e) {
            throw new RomIOException(e);
        }
        return pickupItems;
    }

    @Override
    public void setPickupItems(List<PickupItem> pickupItems) {
        try {
            if (pickupItemsTableOffset > 0) {
                byte[] battleOverlay = readOverlay(romEntry.getIntValue("PickupOvlNumber"));
                for (int i = 0; i < Gen5Constants.numberOfPickupItems; i++) {
                    int itemOffset = pickupItemsTableOffset + (2 * i);
                    int id = pickupItems.get(i).getItem().getId();
                    FileFunctions.write2ByteInt(battleOverlay, itemOffset, id);
                }
                writeOverlay(romEntry.getIntValue("PickupOvlNumber"), battleOverlay);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private void computeCRC32sForRom() throws IOException {
        this.actualOverlayCRC32s = new HashMap<>();
        this.actualFileCRC32s = new HashMap<>();
        this.actualArm9CRC32 = FileFunctions.getCRC32(arm9);
        for (int overlayNumber : romEntry.getOverlayExpectedCRC32Keys()) {
            byte[] overlay = readOverlay(overlayNumber);
            long crc32 = FileFunctions.getCRC32(overlay);
            this.actualOverlayCRC32s.put(overlayNumber, crc32);
        }
        for (String fileKey : romEntry.getFileKeys()) {
            byte[] file = readFile(romEntry.getFile(fileKey));
            long crc32 = FileFunctions.getCRC32(file);
            this.actualFileCRC32s.put(fileKey, crc32);
        }
    }

    @Override
    public boolean isRomValid(PrintStream logStream) {
        // identical to Gen 4 implementation, could be moved up to AbstractDSRomHandler
        if (logStream != null) {
            System.out.println("Checking CRC32 validities");
            System.out.println("ARM9 expected:\t" + Long.toHexString(romEntry.getArm9ExpectedCRC32()));
            System.out.println("ARM9 actual:  \t" + Long.toHexString(actualArm9CRC32));
        }
        if (romEntry.getArm9ExpectedCRC32() != actualArm9CRC32) {
            System.out.println(actualArm9CRC32);
            return false;
        }

        System.out.println("Overlays");
        for (int overlayNumber : romEntry.getOverlayExpectedCRC32Keys()) {
            long expectedCRC32 = romEntry.getOverlayExpectedCRC32(overlayNumber);
            long actualCRC32 = actualOverlayCRC32s.get(overlayNumber);
            if (logStream != null) {
                System.out.println("#" + overlayNumber + "\texpected:\t" + Long.toHexString(expectedCRC32));
                System.out.println("#" + overlayNumber + "\tactual:  \t" + Long.toHexString(actualCRC32));
            }
            if (expectedCRC32 != actualCRC32) {
                return false;
            }
        }

        System.out.println("Filekeys");
        for (String fileKey : romEntry.getFileKeys()) {
            long expectedCRC32 = romEntry.getFileExpectedCRC32(fileKey);
            long actualCRC32 = actualFileCRC32s.get(fileKey);
            if (logStream != null) {
                System.out.println(fileKey + "\texpected:\t" + Long.toHexString(expectedCRC32));
                System.out.println(fileKey + "\tactual:  \t" + Long.toHexString(actualCRC32));
            }
            if (expectedCRC32 != actualCRC32) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<Item> getAllHeldItems() {
        return itemIdsToSet(Gen5Constants.allHeldItems);
    }

    @Override
    public Set<Item> getAllConsumableHeldItems() {
        return itemIdsToSet(Gen5Constants.consumableHeldItems);
    }

    @Override
    public List<Item> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves, int[] pokeMoves) {
        List<Integer> ids = new ArrayList<>(Gen5Constants.generalPurposeConsumableItems);
        int frequencyBoostCount = 6; // Make some very good items more common, but not too common
        if (!consumableOnly) {
            frequencyBoostCount = 8; // bigger to account for larger item pool.
            ids.addAll(Gen5Constants.generalPurposeItems);
        }
        for (int moveIdx : pokeMoves) {
            Move move = moves.get(moveIdx);
            if (move == null) {
                continue;
            }
            if (move.category == MoveCategory.PHYSICAL) {
                ids.add(ItemIDs.liechiBerry);
                ids.add(Gen5Constants.consumableTypeBoostingItems.get(move.type));
                if (!consumableOnly) {
                    ids.addAll(Gen5Constants.typeBoostingItems.get(move.type));
                    ids.add(ItemIDs.choiceBand);
                    ids.add(ItemIDs.muscleBand);
                }
            }
            if (move.category == MoveCategory.SPECIAL) {
                ids.add(ItemIDs.petayaBerry);
                ids.add(Gen5Constants.consumableTypeBoostingItems.get(move.type));
                if (!consumableOnly) {
                    ids.addAll(Gen5Constants.typeBoostingItems.get(move.type));
                    ids.add(ItemIDs.wiseGlasses);
                    ids.add(ItemIDs.choiceSpecs);
                }
            }
            if (!consumableOnly && Gen5Constants.moveBoostingItems.containsKey(moveIdx)) {
                ids.addAll(Gen5Constants.moveBoostingItems.get(moveIdx));
            }
        }
        Map<Type, Effectiveness> byType = getTypeTable().against(tp.getSpecies().getPrimaryType(false), tp.getSpecies().getSecondaryType(false));
        for(Map.Entry<Type, Effectiveness> entry : byType.entrySet()) {
            Integer berry = Gen5Constants.weaknessReducingBerries.get(entry.getKey());
            if (entry.getValue() == Effectiveness.DOUBLE) {
                ids.add(berry);
            } else if (entry.getValue() == Effectiveness.QUADRUPLE) {
                for (int i = 0; i < frequencyBoostCount; i++) {
                    ids.add(berry);
                }
            }
        }
        if (byType.get(Type.NORMAL) == Effectiveness.NEUTRAL) {
            ids.add(ItemIDs.chilanBerry);
        }

        int ability = this.getAbilityForTrainerPokemon(tp);
        if (ability == AbilityIDs.levitate) {
            // we have to cast when removing, otherwise it defaults to removing by index
            ids.remove((Integer) ItemIDs.shucaBerry);
        } else if (byType.get(Type.GROUND) == Effectiveness.DOUBLE || byType.get(Type.GROUND) == Effectiveness.QUADRUPLE) {
            ids.add(ItemIDs.airBalloon);
        }

        if (!consumableOnly) {
            if (Gen5Constants.abilityBoostingItems.containsKey(ability)) {
                ids.addAll(Gen5Constants.abilityBoostingItems.get(ability));
            }
            if (tp.getSpecies().getPrimaryType(false) == Type.POISON || tp.getSpecies().getSecondaryType(false) == Type.POISON) {
                ids.add(ItemIDs.blackSludge);
            }
            List<Integer> speciesItems = Gen5Constants.speciesBoostingItems.get(tp.getSpecies().getNumber());
            if (speciesItems != null) {
                for (int i = 0; i < frequencyBoostCount; i++) {
                    ids.addAll(speciesItems);
                }
            }
            if (!tp.getSpecies().getEvolutionsFrom().isEmpty() && tp.getLevel() >= 20) {
                // eviolite can be too good for early game, so we gate it behind a minimum level.
                // We go with the same level as the option for "No early wonder guard".
                ids.add(ItemIDs.eviolite);
            }
        }
        return ids.stream().map(items::get).collect(Collectors.toList());
    }

    @Override
    protected void loadPokemonPalettes() {
        try {
            String NARCpath = getRomEntry().getFile("PokemonGraphics");
            NARCArchive pokeGraphicsNARC = readNARC(NARCpath);

            for (Species pk : getSpeciesSetInclFormes()) {
                int gfxIndex = getGraphicsIndex(pk);
                pk.setNormalPalette(readPalette(pokeGraphicsNARC, gfxIndex * 20 + 18));
                pk.setShinyPalette(readPalette(pokeGraphicsNARC, gfxIndex * 20 + 19));
            }

        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    @Override
    public void savePokemonPalettes() {
        try {
            String NARCpath = getRomEntry().getFile("PokemonGraphics");
            NARCArchive pokeGraphicsNARC = readNARC(NARCpath);

            for (Species pk : getSpeciesSetInclFormes()) {
                int gfxIndex = getGraphicsIndex(pk);
                writePalette(pokeGraphicsNARC, gfxIndex * 20 + 18, pk.getNormalPalette());
                writePalette(pokeGraphicsNARC, gfxIndex * 20 + 19, pk.getShinyPalette());
            }
            writeNARC(NARCpath, pokeGraphicsNARC);

        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }

    private int getGraphicsIndex(Species pk) {
        return pk.isBaseForme() ? pk.getNumber() : formeGraphicsIndices.get(pk.getBaseForme()) + pk.getFormeNumber() - 1;
    }

    @Override
    public Gen5PokemonImageGetter createPokemonImageGetter(Species pk) {
        return new Gen5PokemonImageGetter(pk);
    }

    public class Gen5PokemonImageGetter extends DSPokemonImageGetter {

        // TODO: getting the full animation sheets
        // These are 64x144 pixel images, stored 2 files after their respective non-animated image.
        // They are LZ11-compressed, and has what Tinke calls a "lineal" image pattern, as opposed to
        // the common "horizontal" one.
        // Methods for reading the "lineal" images are needed.

        public Gen5PokemonImageGetter(Species pk) {
            super(pk);
        }

        @Override
        public int getGraphicalFormeAmount() {
            return graphicalFormeCounts.getOrDefault(pk, 1) ;
        }

        @Override
        public BufferedImage get() {
            beforeGet();

            int imageIndex = getImageIndex();
            byte[] compressedPic = pokeGraphicsNARC.files.get(imageIndex);
            byte[] uncompressedPic = DSDecmp.Decompress(compressedPic);

            Palette palette = getPalette();
            int[] convPalette = palette.toARGB();
            if (transparentBackground) {
                convPalette[0] = 0;
            }

            // Output to 64x144 tiled image, then unscramble to a 96x96
            BufferedImage bim = GFXFunctions.drawTiledImage(uncompressedPic, convPalette, 48, 64, 144, 4);
            bim = unscramblePokemonSprite(bim);

            if (includePalette) {
                for (int j = 0; j < 16; j++) {
                    bim.setRGB(j, 0, convPalette[j]);
                }
            }

            return bim;
        }

        private int getImageIndex() {
            int gfxIndex;
            // Arceus doesn't have unique images for all of its forms, they are just palette swaps
            if (pk.isBaseForme() && forme != 0 && pk.getNumber() != SpeciesIDs.arceus) {
                gfxIndex = formeGraphicsIndices.get(pk) + forme - 1;
            } else {
                gfxIndex = getGraphicsIndex(pk);
            }

            int imageIndex = gfxIndex * 20;
            if (hasGenderedImages() && gender == FEMALE) {
                imageIndex++;
            }
            if (back) {
                imageIndex += 9;
            }
            return imageIndex;
        }

        private Palette getPalette() {
            // placeholder code, until the form rewrite comes along; then all palette reading will be centralized
            if (pk.isBaseForme() && forme != 0) {
                if (pk.getNumber() == SpeciesIDs.arceus) {
                    // Arceus doesn't have unique images for all of its forms, they are just palette swaps.
                    int palIndex = romEntry.getIntValue("ArceusPalettesOffset") + (forme - 1) * 2;
                    if (shiny) palIndex++;
                    return readPalette(pokeGraphicsNARC, palIndex);

                } else {
                    int gfxIndex = formeGraphicsIndices.get(pk) + forme - 1;
                    return readPalette(pokeGraphicsNARC, gfxIndex * 20 + (shiny ? 19 : 18));
                }
            } else {
                return shiny ? pk.getShinyPalette() : pk.getNormalPalette();
            }
        }

        private BufferedImage unscramblePokemonSprite(BufferedImage bim) {
            BufferedImage unscrambled = new BufferedImage(96, 96, BufferedImage.TYPE_BYTE_INDEXED,
                    (IndexColorModel) bim.getColorModel());
            Graphics g = unscrambled.getGraphics();
            g.drawImage(bim, 0, 0, 64, 64, 0, 0, 64, 64, null);
            g.drawImage(bim, 64, 0, 96, 8, 0, 64, 32, 72, null);
            g.drawImage(bim, 64, 8, 96, 16, 32, 64, 64, 72, null);
            g.drawImage(bim, 64, 16, 96, 24, 0, 72, 32, 80, null);
            g.drawImage(bim, 64, 24, 96, 32, 32, 72, 64, 80, null);
            g.drawImage(bim, 64, 32, 96, 40, 0, 80, 32, 88, null);
            g.drawImage(bim, 64, 40, 96, 48, 32, 80, 64, 88, null);
            g.drawImage(bim, 64, 48, 96, 56, 0, 88, 32, 96, null);
            g.drawImage(bim, 64, 56, 96, 64, 32, 88, 64, 96, null);
            g.drawImage(bim, 0, 64, 64, 96, 0, 96, 64, 128, null);
            g.drawImage(bim, 64, 64, 96, 72, 0, 128, 32, 136, null);
            g.drawImage(bim, 64, 72, 96, 80, 32, 128, 64, 136, null);
            g.drawImage(bim, 64, 80, 96, 88, 0, 136, 32, 144, null);
            g.drawImage(bim, 64, 88, 96, 96, 32, 136, 64, 144, null);
            return unscrambled;
        }

        @Override
        public boolean hasGenderedImages() {
            int imageIndex = pk.getNumber() * 20 + 1;
            byte[] imageData = pokeGraphicsNARC.files.get(imageIndex);
            return imageData.length != 0;
        }
    }
    
    public String getPaletteFilesID() {
        switch (romEntry.getRomType()) {
            case Gen5Constants.Type_BW:
                return "BW";
            case Gen5Constants.Type_BW2:
                // TODO: check if this should be identical
                return "BW";
            default:
                return null;
        }
    }

    @Override
    public Gen5RomEntry getRomEntry() {
        return romEntry;
    }
    
}
