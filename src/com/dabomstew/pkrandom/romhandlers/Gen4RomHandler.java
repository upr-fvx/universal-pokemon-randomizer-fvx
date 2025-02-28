package com.dabomstew.pkrandom.romhandlers;

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

import com.dabomstew.pkrandom.*;
import com.dabomstew.pkrandom.constants.*;
import com.dabomstew.pkrandom.exceptions.RomIOException;
import com.dabomstew.pkrandom.graphics.palettes.Palette;
import com.dabomstew.pkrandom.newnds.NARCArchive;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.romentries.DSStaticPokemon;
import com.dabomstew.pkrandom.romhandlers.romentries.Gen4RomEntry;
import com.dabomstew.pkrandom.romhandlers.romentries.InFileEntry;
import thenewpoketext.PokeTextData;
import thenewpoketext.TextToPoke;

import javax.naming.OperationNotSupportedException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link RomHandler} for Diamond, Pearl, Platinum, HeartGold, SoulSilver.
 */
public class Gen4RomHandler extends AbstractDSRomHandler {

	public static class Factory extends RomHandler.Factory {

		@Override
		public Gen4RomHandler create() {
			return new Gen4RomHandler();
		}

		public boolean isLoadable(String filename) {
			return detectNDSRomInner(getROMCodeFromFile(filename), getVersionFromFile(filename));
		}
	}

	private static List<Gen4RomEntry> roms;

	static {
		loadROMInfo();

	}

	private static void loadROMInfo() {
		try {
			roms = Gen4RomEntry.READER.readEntriesFromFile("gen4_offsets.ini");
		} catch (IOException e) {
			throw new RuntimeException("Could not read Rom Entries.", e);
		}
	}

	// This rom
	private Species[] pokes;
	private List<Species> speciesListInclFormes;
	private List<Species> speciesList;
	private Move[] moves;
	private NARCArchive pokeNarc, moveNarc;
	private NARCArchive msgNarc;
	private NARCArchive scriptNarc;
	private NARCArchive eventNarc;
	private byte[] arm9;
	private List<String> abilityNames;
	private List<String> itemNames;
	private boolean loadedWildMapNames;
	private Map<Integer, String> wildMapNames, headbuttMapNames;
	private ItemList allowedItems, nonBadItems;
	private boolean roamerRandomizationEnabled;
	private int pickupItemsTableOffset, rarePickupItemsTableOffset;
	private TypeTable typeTable;
	private long actualArm9CRC32;
	private Map<Integer, Long> actualOverlayCRC32s;
	private Map<String, Long> actualFileCRC32s;

	private Gen4RomEntry romEntry;

	@Override
	protected boolean detectNDSRom(String ndsCode, byte version) {
		return detectNDSRomInner(ndsCode, version);
	}

	private static boolean detectNDSRomInner(String ndsCode, byte version) {
		return entryFor(ndsCode, version) != null;
	}

	private static Gen4RomEntry entryFor(String ndsCode, byte version) {
		for (Gen4RomEntry re : roms) {
			if (ndsCode.equals(re.getRomCode()) && version == re.getVersion()) {
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
			msgNarc = readNARC(romEntry.getFile("Text"));
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		try {
			scriptNarc = readNARC(romEntry.getFile("Scripts"));
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		try {
			eventNarc = readNARC(romEntry.getFile("Events"));
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		loadPokemonStats();
		speciesListInclFormes = Arrays.asList(pokes);
		speciesList = Arrays.asList(Arrays.copyOfRange(pokes, 0, Gen4Constants.pokemonCount + 1));
		loadMoves();
		loadPokemonPalettes();
		abilityNames = getStrings(romEntry.getIntValue("AbilityNamesTextOffset"));
		itemNames = getStrings(romEntry.getIntValue("ItemNamesTextOffset"));
		loadedWildMapNames = false;

		allowedItems = Gen4Constants.allowedItems.copy();
		nonBadItems = Gen4Constants.nonBadItems.copy();

		roamerRandomizationEnabled = (romEntry.getRomType() == Gen4Constants.Type_DP && !romEntry.getRoamingPokemon().isEmpty())
				|| (romEntry.getRomType() == Gen4Constants.Type_Plat
						&& romEntry.hasTweakFile("NewRoamerSubroutineTweak"))
				|| (romEntry.getRomType() == Gen4Constants.Type_HGSS
						&& romEntry.hasTweakFile("NewRoamerSubroutineTweak"));

		try {
			computeCRC32sForRom();
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		
		// We want to guarantee that the catching tutorial in HGSS has Ethan/Lyra's new
		// Pokemon. We also
		// want to allow the option of randomizing the enemy Pokemon too. Unfortunately,
		// the latter can
		// occur *before* the former, but there's no guarantee that it will even happen.
		// Since we *know*
		// we'll need to do this patch eventually, just expand the arm9 here to make
		// things easy.
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS
				&& romEntry.hasTweakFile("NewCatchingTutorialSubroutineTweak")) {
			int extendBy = romEntry.getIntValue("Arm9ExtensionSize");
			arm9 = extendARM9(arm9, extendBy, romEntry.getStringValue("TCMCopyingPrefix"), Gen4Constants.arm9Offset);
			genericIPSPatch(arm9, "NewCatchingTutorialSubroutineTweak");
		}
	}

	private void loadMoves() {
		try {
			moveNarc = this.readNARC(romEntry.getFile("MoveData"));
			moves = new Move[Gen4Constants.moveCount + 1];
			List<String> moveNames = getStrings(romEntry.getIntValue("MoveNamesTextOffset"));
			for (int i = 1; i <= Gen4Constants.moveCount; i++) {
				byte[] moveData = moveNarc.files.get(i);
				moves[i] = new Move();
				moves[i].name = moveNames.get(i);
				moves[i].number = i;
				moves[i].internalId = i;
				moves[i].effectIndex = readWord(moveData, 0);
				moves[i].hitratio = (moveData[5] & 0xFF);
				moves[i].power = moveData[3] & 0xFF;
				moves[i].pp = moveData[6] & 0xFF;
				moves[i].type = Gen4Constants.typeTable[moveData[4] & 0xFF];
				moves[i].target = readWord(moveData, 8);
				moves[i].category = Gen4Constants.moveCategoryIndices[moveData[2] & 0xFF];
				moves[i].priority = moveData[10];
				int flags = moveData[11] & 0xFF;
				moves[i].makesContact = (flags & 1) != 0;
				moves[i].isPunchMove = Gen4Constants.punchMoves.contains(moves[i].number);
				moves[i].isSoundMove = Gen4Constants.soundMoves.contains(moves[i].number);

				if (i == MoveIDs.swift) {
					perfectAccuracy = (int) moves[i].hitratio;
				}

				if (GlobalConstants.normalMultihitMoves.contains(i)) {
					moves[i].hitCount = 3;
				} else if (GlobalConstants.doubleHitMoves.contains(i)) {
					moves[i].hitCount = 2;
				} else if (i == MoveIDs.tripleKick) {
					moves[i].hitCount = 2.71; // this assumes the first hit lands
				}

				int secondaryEffectChance = moveData[7] & 0xFF;
				loadStatChangesFromEffect(moves[i], secondaryEffectChance);
				loadStatusFromEffect(moves[i], secondaryEffectChance);
				loadMiscMoveInfoFromEffect(moves[i], secondaryEffectChance);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private void loadStatChangesFromEffect(Move move, int secondaryEffectChance) {
		switch (move.effectIndex) {
		case Gen4Constants.noDamageAtkPlusOneEffect:
		case Gen4Constants.noDamageDefPlusOneEffect:
		case Gen4Constants.noDamageSpAtkPlusOneEffect:
		case Gen4Constants.noDamageEvasionPlusOneEffect:
		case Gen4Constants.noDamageAtkMinusOneEffect:
		case Gen4Constants.noDamageDefMinusOneEffect:
		case Gen4Constants.noDamageSpeMinusOneEffect:
		case Gen4Constants.noDamageAccuracyMinusOneEffect:
		case Gen4Constants.noDamageEvasionMinusOneEffect:
		case Gen4Constants.noDamageAtkPlusTwoEffect:
		case Gen4Constants.noDamageDefPlusTwoEffect:
		case Gen4Constants.noDamageSpePlusTwoEffect:
		case Gen4Constants.noDamageSpAtkPlusTwoEffect:
		case Gen4Constants.noDamageSpDefPlusTwoEffect:
		case Gen4Constants.noDamageAtkMinusTwoEffect:
		case Gen4Constants.noDamageDefMinusTwoEffect:
		case Gen4Constants.noDamageSpeMinusTwoEffect:
		case Gen4Constants.noDamageSpDefMinusTwoEffect:
		case Gen4Constants.minimizeEffect:
		case Gen4Constants.swaggerEffect:
		case Gen4Constants.defenseCurlEffect:
		case Gen4Constants.flatterEffect:
		case Gen4Constants.chargeEffect:
		case Gen4Constants.noDamageAtkAndDefMinusOneEffect:
		case Gen4Constants.noDamageDefAndSpDefPlusOneEffect:
		case Gen4Constants.noDamageAtkAndDefPlusOneEffect:
		case Gen4Constants.noDamageSpAtkAndSpDefPlusOneEffect:
		case Gen4Constants.noDamageAtkAndSpePlusOneEffect:
		case Gen4Constants.noDamageSpAtkMinusTwoEffect:
			if (move.target == 16) {
				move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_USER;
			} else {
				move.statChangeMoveType = StatChangeMoveType.NO_DAMAGE_TARGET;
			}
			break;

		case Gen4Constants.damageAtkMinusOneEffect:
		case Gen4Constants.damageDefMinusOneEffect:
		case Gen4Constants.damageSpeMinusOneEffect:
		case Gen4Constants.damageSpAtkMinusOneEffect:
		case Gen4Constants.damageSpDefMinusOneEffect:
		case Gen4Constants.damageAccuracyMinusOneEffect:
		case Gen4Constants.damageSpDefMinusTwoEffect:
			move.statChangeMoveType = StatChangeMoveType.DAMAGE_TARGET;
			break;

		case Gen4Constants.damageUserDefPlusOneEffect:
		case Gen4Constants.damageUserAtkPlusOneEffect:
		case Gen4Constants.damageUserAllPlusOneEffect:
		case Gen4Constants.damageUserAtkAndDefMinusOneEffect:
		case Gen4Constants.damageUserSpAtkMinusTwoEffect:
		case Gen4Constants.damageUserSpeMinusOneEffect:
		case Gen4Constants.damageUserDefAndSpDefMinusOneEffect:
		case Gen4Constants.damageUserSpAtkPlusOneEffect:
			move.statChangeMoveType = StatChangeMoveType.DAMAGE_USER;
			break;

		default:
			// Move does not have a stat-changing effect
			return;
		}

		switch (move.effectIndex) {
			case Gen4Constants.noDamageAtkPlusOneEffect:
			case Gen4Constants.damageUserAtkPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.noDamageDefPlusOneEffect:
			case Gen4Constants.damageUserDefPlusOneEffect:
			case Gen4Constants.defenseCurlEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.noDamageSpAtkPlusOneEffect:
			case Gen4Constants.flatterEffect:
			case Gen4Constants.damageUserSpAtkPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.noDamageEvasionPlusOneEffect:
			case Gen4Constants.minimizeEffect: {
				move.statChanges[0].type = StatChangeType.EVASION;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.noDamageAtkMinusOneEffect:
			case Gen4Constants.damageAtkMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.noDamageDefMinusOneEffect:
			case Gen4Constants.damageDefMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.noDamageSpeMinusOneEffect:
			case Gen4Constants.damageSpeMinusOneEffect:
			case Gen4Constants.damageUserSpeMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.SPEED;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.noDamageAccuracyMinusOneEffect:
			case Gen4Constants.damageAccuracyMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.ACCURACY;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.noDamageEvasionMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.EVASION;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.noDamageAtkPlusTwoEffect:
			case Gen4Constants.swaggerEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = 2;
				break;
			}
			case Gen4Constants.noDamageDefPlusTwoEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = 2;
				break;
			}
			case Gen4Constants.noDamageSpePlusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPEED;
				move.statChanges[0].stages = 2;
				break;
			}
			case Gen4Constants.noDamageSpAtkPlusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
				move.statChanges[0].stages = 2;
				break;
			}
			case Gen4Constants.noDamageSpDefPlusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[0].stages = 2;
				break;
			}
			case Gen4Constants.noDamageAtkMinusTwoEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = -2;
				break;
			}
			case Gen4Constants.noDamageDefMinusTwoEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = -2;
				break;
			}
			case Gen4Constants.noDamageSpeMinusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPEED;
				move.statChanges[0].stages = -2;
				break;
			}
			case Gen4Constants.noDamageSpDefMinusTwoEffect:
			case Gen4Constants.damageSpDefMinusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[0].stages = -2;
				break;
			}
			case Gen4Constants.damageSpAtkMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.damageSpDefMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[0].stages = -1;
				break;
			}
			case Gen4Constants.damageUserAllPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.ALL;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.chargeEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[0].stages = 1;
				break;
			}
			case Gen4Constants.damageUserAtkAndDefMinusOneEffect:
			case Gen4Constants.noDamageAtkAndDefMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = -1;
				move.statChanges[1].type = StatChangeType.DEFENSE;
				move.statChanges[1].stages = -1;
				break;
			}
			case Gen4Constants.damageUserSpAtkMinusTwoEffect:
			case Gen4Constants.noDamageSpAtkMinusTwoEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
				move.statChanges[0].stages = -2;
				break;
			}
			case Gen4Constants.noDamageDefAndSpDefPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = 1;
				move.statChanges[1].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[1].stages = 1;
				break;
			}
			case Gen4Constants.noDamageAtkAndDefPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = 1;
				move.statChanges[1].type = StatChangeType.DEFENSE;
				move.statChanges[1].stages = 1;
				break;
			}
			case Gen4Constants.noDamageSpAtkAndSpDefPlusOneEffect: {
				move.statChanges[0].type = StatChangeType.SPECIAL_ATTACK;
				move.statChanges[0].stages = 1;
				move.statChanges[1].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[1].stages = 1;
				break;
			}
			case Gen4Constants.noDamageAtkAndSpePlusOneEffect: {
				move.statChanges[0].type = StatChangeType.ATTACK;
				move.statChanges[0].stages = 1;
				move.statChanges[1].type = StatChangeType.SPEED;
				move.statChanges[1].stages = 1;
				break;
			}
			case Gen4Constants.damageUserDefAndSpDefMinusOneEffect: {
				move.statChanges[0].type = StatChangeType.DEFENSE;
				move.statChanges[0].stages = -1;
				move.statChanges[1].type = StatChangeType.SPECIAL_DEFENSE;
				move.statChanges[1].stages = -1;
				break;
			}
		}

		if (move.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET
				|| move.statChangeMoveType == StatChangeMoveType.DAMAGE_USER) {
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
		switch (move.effectIndex) {
		case Gen4Constants.noDamageSleepEffect:
		case Gen4Constants.toxicEffect:
		case Gen4Constants.noDamageConfusionEffect:
		case Gen4Constants.noDamagePoisonEffect:
		case Gen4Constants.noDamageParalyzeEffect:
		case Gen4Constants.noDamageBurnEffect:
		case Gen4Constants.swaggerEffect:
		case Gen4Constants.flatterEffect:
		case Gen4Constants.teeterDanceEffect:
			move.statusMoveType = StatusMoveType.NO_DAMAGE;
			break;

		case Gen4Constants.damagePoisonEffect:
		case Gen4Constants.damageBurnEffect:
		case Gen4Constants.damageFreezeEffect:
		case Gen4Constants.damageParalyzeEffect:
		case Gen4Constants.damageConfusionEffect:
		case Gen4Constants.twineedleEffect:
		case Gen4Constants.damageBurnAndThawUserEffect:
		case Gen4Constants.thunderEffect:
		case Gen4Constants.blazeKickEffect:
		case Gen4Constants.poisonFangEffect:
		case Gen4Constants.damagePoisonWithIncreasedCritEffect:
		case Gen4Constants.flareBlitzEffect:
		case Gen4Constants.blizzardEffect:
		case Gen4Constants.voltTackleEffect:
		case Gen4Constants.bounceEffect:
		case Gen4Constants.chatterEffect:
		case Gen4Constants.fireFangEffect:
		case Gen4Constants.iceFangEffect:
		case Gen4Constants.thunderFangEffect:
			move.statusMoveType = StatusMoveType.DAMAGE;
			break;

		default:
			// Move does not have a status effect
			return;
		}

		switch (move.effectIndex) {
		case Gen4Constants.noDamageSleepEffect:
			move.statusType = StatusType.SLEEP;
			break;
		case Gen4Constants.damagePoisonEffect:
		case Gen4Constants.noDamagePoisonEffect:
		case Gen4Constants.twineedleEffect:
		case Gen4Constants.damagePoisonWithIncreasedCritEffect:
			move.statusType = StatusType.POISON;
			break;
		case Gen4Constants.damageBurnEffect:
		case Gen4Constants.damageBurnAndThawUserEffect:
		case Gen4Constants.noDamageBurnEffect:
		case Gen4Constants.blazeKickEffect:
		case Gen4Constants.flareBlitzEffect:
		case Gen4Constants.fireFangEffect:
			move.statusType = StatusType.BURN;
			break;
		case Gen4Constants.damageFreezeEffect:
		case Gen4Constants.blizzardEffect:
		case Gen4Constants.iceFangEffect:
			move.statusType = StatusType.FREEZE;
			break;
		case Gen4Constants.damageParalyzeEffect:
		case Gen4Constants.noDamageParalyzeEffect:
		case Gen4Constants.thunderEffect:
		case Gen4Constants.voltTackleEffect:
		case Gen4Constants.bounceEffect:
		case Gen4Constants.thunderFangEffect:
			move.statusType = StatusType.PARALYZE;
			break;
		case Gen4Constants.toxicEffect:
		case Gen4Constants.poisonFangEffect:
			move.statusType = StatusType.TOXIC_POISON;
			break;
		case Gen4Constants.noDamageConfusionEffect:
		case Gen4Constants.damageConfusionEffect:
		case Gen4Constants.swaggerEffect:
		case Gen4Constants.flatterEffect:
		case Gen4Constants.teeterDanceEffect:
		case Gen4Constants.chatterEffect:
			move.statusType = StatusType.CONFUSION;
			break;
		}

		if (move.statusMoveType == StatusMoveType.DAMAGE) {
			move.statusPercentChance = secondaryEffectChance;
			if (move.statusPercentChance == 0.0) {
				if (move.number == MoveIDs.chatter) {
					move.statusPercentChance = 1.0;
				} else {
					move.statusPercentChance = 100.0;
				}
			}
		}
	}

	private void loadMiscMoveInfoFromEffect(Move move, int secondaryEffectChance) {
		switch (move.effectIndex) {
		case Gen4Constants.increasedCritEffect:
		case Gen4Constants.blazeKickEffect:
		case Gen4Constants.damagePoisonWithIncreasedCritEffect:
			move.criticalChance = CriticalChance.INCREASED;
			break;

		case Gen4Constants.futureSightAndDoomDesireEffect:
			move.criticalChance = CriticalChance.NONE;

		case Gen4Constants.flinchEffect:
		case Gen4Constants.snoreEffect:
		case Gen4Constants.twisterEffect:
		case Gen4Constants.stompEffect:
		case Gen4Constants.fakeOutEffect:
		case Gen4Constants.fireFangEffect:
		case Gen4Constants.iceFangEffect:
		case Gen4Constants.thunderFangEffect:
			move.flinchPercentChance = secondaryEffectChance;
			break;

		case Gen4Constants.damageAbsorbEffect:
		case Gen4Constants.dreamEaterEffect:
			move.absorbPercent = 50;
			break;

		case Gen4Constants.damageRecoil25PercentEffect:
			move.recoilPercent = 25;
			break;

		case Gen4Constants.damageRecoil33PercentEffect:
		case Gen4Constants.flareBlitzEffect:
		case Gen4Constants.voltTackleEffect:
			move.recoilPercent = 33;
			break;

		case Gen4Constants.damageRecoil50PercentEffect:
			move.recoilPercent = 50;
			break;

		case Gen4Constants.bindingEffect:
		case Gen4Constants.trappingEffect:
			move.isTrapMove = true;
			break;

		case Gen4Constants.skullBashEffect:
		case Gen4Constants.solarbeamEffect:
		case Gen4Constants.flyEffect:
		case Gen4Constants.diveEffect:
		case Gen4Constants.digEffect:
		case Gen4Constants.bounceEffect:
		case Gen4Constants.shadowForceEffect:
			move.isChargeMove = true;
			break;

		case Gen3Constants.rechargeEffect:
			move.isRechargeMove = true;
			break;

		case Gen4Constants.razorWindEffect:
			move.criticalChance = CriticalChance.INCREASED;
			move.isChargeMove = true;
			break;

		case Gen4Constants.skyAttackEffect:
			move.criticalChance = CriticalChance.INCREASED;
			move.flinchPercentChance = secondaryEffectChance;
			move.isChargeMove = true;
			break;
		}
	}

	private void loadPokemonStats() {
		try {
			String pstatsnarc = romEntry.getFile("PokemonStats");
			pokeNarc = this.readNARC(pstatsnarc);
			String[] pokeNames = readPokemonNames();
			int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
			pokes = new Species[Gen4Constants.pokemonCount + formeCount + 1];
			for (int i = 1; i <= Gen4Constants.pokemonCount; i++) {
				pokes[i] = new Species(i);
				loadBasicPokeStats(pokes[i], pokeNarc.files.get(i));
				pokes[i].setName(pokeNames[i]);
				pokes[i].setGeneration(generationOf(pokes[i]));
			}

			int i = Gen4Constants.pokemonCount + 1;
			for (int k : Gen4Constants.formeMappings.keySet()) {
				if (i >= pokes.length) {
					break;
				}
				pokes[i] = new Species(i);
				loadBasicPokeStats(pokes[i], pokeNarc.files.get(k));
				FormeInfo fi = Gen4Constants.formeMappings.get(k);
				pokes[i].setName(pokeNames[fi.baseForme]);
				pokes[i].setBaseForme(pokes[fi.baseForme]);
				pokes[i].setFormeNumber(fi.formeNumber);
				pokes[i].setFormeSuffix(Gen4Constants.getFormeSuffixByBaseForme(fi.baseForme, fi.formeNumber));
				pokes[i].setGeneration(generationOf(pokes[i]));
				i = i + 1;
			}

			populateEvolutions();
		} catch (IOException e) {
			throw new RomIOException(e);
		}

	}

	private int generationOf(Species pk) {
		if (pk.getBaseForme() != null) {
			return pk.getBaseForme().getGeneration();
		}
		if (pk.getNumber() >= SpeciesIDs.turtwig) {
			return 4;
		} else if (pk.getNumber() >= SpeciesIDs.treecko) {
			return 3;
		} else if (pk.getNumber() >= SpeciesIDs.chikorita) {
			return 2;
		}
		return 1;
	}

	private void loadBasicPokeStats(Species pkmn, byte[] stats) {
		pkmn.setHp(stats[Gen4Constants.bsHPOffset] & 0xFF);
		pkmn.setAttack(stats[Gen4Constants.bsAttackOffset] & 0xFF);
		pkmn.setDefense(stats[Gen4Constants.bsDefenseOffset] & 0xFF);
		pkmn.setSpeed(stats[Gen4Constants.bsSpeedOffset] & 0xFF);
		pkmn.setSpatk(stats[Gen4Constants.bsSpAtkOffset] & 0xFF);
		pkmn.setSpdef(stats[Gen4Constants.bsSpDefOffset] & 0xFF);
		// Type
		pkmn.setPrimaryType(Gen4Constants.typeTable[stats[Gen4Constants.bsPrimaryTypeOffset] & 0xFF]);
		pkmn.setSecondaryType(Gen4Constants.typeTable[stats[Gen4Constants.bsSecondaryTypeOffset] & 0xFF]);
		// Only one type?
		if (pkmn.getSecondaryType(false) == pkmn.getPrimaryType(false)) {
			pkmn.setSecondaryType(null);
		}
		pkmn.setCatchRate(stats[Gen4Constants.bsCatchRateOffset] & 0xFF);
		pkmn.setGrowthCurve(ExpCurve.fromByte(stats[Gen4Constants.bsGrowthCurveOffset]));

		// Abilities
		pkmn.setAbility1(stats[Gen4Constants.bsAbility1Offset] & 0xFF);
		pkmn.setAbility2(stats[Gen4Constants.bsAbility2Offset] & 0xFF);

		// Held Items?
		int item1 = readWord(stats, Gen4Constants.bsCommonHeldItemOffset);
		int item2 = readWord(stats, Gen4Constants.bsRareHeldItemOffset);

		if (item1 == item2) {
			// guaranteed
			pkmn.setGuaranteedHeldItem(item1);
			pkmn.setCommonHeldItem(0);
			pkmn.setRareHeldItem(0);
		} else {
			pkmn.setGuaranteedHeldItem(0);
			pkmn.setCommonHeldItem(item1);
			pkmn.setRareHeldItem(item2);
		}
		pkmn.setDarkGrassHeldItem(-1);

		pkmn.setGenderRatio(stats[Gen4Constants.bsGenderRatioOffset] & 0xFF);

		int cosmeticForms = Gen4Constants.cosmeticForms.getOrDefault(pkmn.getNumber(), 0);
		if (cosmeticForms > 0 && romEntry.getRomType() != Gen4Constants.Type_DP) {
			pkmn.setCosmeticForms(cosmeticForms);
		}
	}

	private String[] readPokemonNames() {
		String[] pokeNames = new String[Gen4Constants.pokemonCount + 1];
		List<String> nameList = getStrings(romEntry.getIntValue("PokemonNamesTextOffset"));
		for (int i = 1; i <= Gen4Constants.pokemonCount; i++) {
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
			writeNARC(romEntry.getFile("Text"), msgNarc);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		try {
			writeNARC(romEntry.getFile("Scripts"), scriptNarc);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		try {
			writeNARC(romEntry.getFile("Events"), eventNarc);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	@Override
	public void saveMoves() {
		for (int i = 1; i <= Gen4Constants.moveCount; i++) {
			byte[] data = moveNarc.files.get(i);
			writeWord(data, 0, moves[i].effectIndex);
			data[2] = Gen4Constants.moveCategoryToByte(moves[i].category);
			data[3] = (byte) moves[i].power;
			data[4] = Gen4Constants.typeToByte(moves[i].type);
			int hitratio = (int) Math.round(moves[i].hitratio);
			if (hitratio < 0) {
				hitratio = 0;
			}
			if (hitratio > 100) {
				hitratio = 100;
			}
			data[5] = (byte) hitratio;
			data[6] = (byte) moves[i].pp;
		}

		try {
			this.writeNARC(romEntry.getFile("MoveData"), moveNarc);
		} catch (IOException e) {
			throw new RomIOException(e);
		}

	}

	@Override
	public void savePokemonStats() {
		// Update the "a/an X" list too, if it exists
		List<String> namesList = getStrings(romEntry.getIntValue("PokemonNamesTextOffset"));
		int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
		if (romEntry.getStringValue("HasExtraPokemonNames").equalsIgnoreCase("Yes")) {
			List<String> namesList2 = getStrings(romEntry.getIntValue("PokemonNamesTextOffset") + 1);
			for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
				if (i > Gen4Constants.pokemonCount) {
					saveBasicPokeStats(pokes[i], pokeNarc.files.get(i + Gen4Constants.formeOffset));
					continue;
				}
				saveBasicPokeStats(pokes[i], pokeNarc.files.get(i));
				String oldName = namesList.get(i);
				namesList.set(i, pokes[i].getName());
				namesList2.set(i, namesList2.get(i).replace(oldName, pokes[i].getName()));
			}
			setStrings(romEntry.getIntValue("PokemonNamesTextOffset") + 1, namesList2, false);
		} else {
			for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
				if (i > Gen4Constants.pokemonCount) {
					saveBasicPokeStats(pokes[i], pokeNarc.files.get(i + Gen4Constants.formeOffset));
					continue;
				}
				saveBasicPokeStats(pokes[i], pokeNarc.files.get(i));
				namesList.set(i, pokes[i].getName());
			}
		}
		setStrings(romEntry.getIntValue("PokemonNamesTextOffset"), namesList, false);

		try {
			String pstatsnarc = romEntry.getFile("PokemonStats");
			this.writeNARC(pstatsnarc, pokeNarc);
		} catch (IOException e) {
			throw new RomIOException(e);
		}

		writeEvolutions();

	}

	private void saveBasicPokeStats(Species pkmn, byte[] stats) {
		stats[Gen4Constants.bsHPOffset] = (byte) pkmn.getHp();
		stats[Gen4Constants.bsAttackOffset] = (byte) pkmn.getAttack();
		stats[Gen4Constants.bsDefenseOffset] = (byte) pkmn.getDefense();
		stats[Gen4Constants.bsSpeedOffset] = (byte) pkmn.getSpeed();
		stats[Gen4Constants.bsSpAtkOffset] = (byte) pkmn.getSpatk();
		stats[Gen4Constants.bsSpDefOffset] = (byte) pkmn.getSpdef();
		stats[Gen4Constants.bsPrimaryTypeOffset] = Gen4Constants.typeToByte(pkmn.getPrimaryType(false));
		if (pkmn.getSecondaryType(false) == null) {
			stats[Gen4Constants.bsSecondaryTypeOffset] = stats[Gen4Constants.bsPrimaryTypeOffset];
		} else {
			stats[Gen4Constants.bsSecondaryTypeOffset] = Gen4Constants.typeToByte(pkmn.getSecondaryType(false));
		}
		stats[Gen4Constants.bsCatchRateOffset] = (byte) pkmn.getCatchRate();
		stats[Gen4Constants.bsGrowthCurveOffset] = pkmn.getGrowthCurve().toByte();

		stats[Gen4Constants.bsAbility1Offset] = (byte) pkmn.getAbility1();
		stats[Gen4Constants.bsAbility2Offset] = (byte) pkmn.getAbility2();

		// Held items
		if (pkmn.getGuaranteedHeldItem() > 0) {
			writeWord(stats, Gen4Constants.bsCommonHeldItemOffset, pkmn.getGuaranteedHeldItem());
			writeWord(stats, Gen4Constants.bsRareHeldItemOffset, pkmn.getGuaranteedHeldItem());
		} else {
			writeWord(stats, Gen4Constants.bsCommonHeldItemOffset, pkmn.getCommonHeldItem());
			writeWord(stats, Gen4Constants.bsRareHeldItemOffset, pkmn.getRareHeldItem());
		}
	}

	@Override
	public List<Species> getSpecies() {
		return speciesList;
	}

	@Override
	public List<Species> getSpeciesInclFormes() {
		return speciesListInclFormes; // No formes for now
	}

	@Override
	public SpeciesSet getAltFormes() {
		int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
		return new SpeciesSet(speciesListInclFormes.subList(Gen4Constants.pokemonCount + 1,
				Gen4Constants.pokemonCount + formeCount + 1));
	}

	@Override
	public List<MegaEvolution> getMegaEvolutions() {
		return new ArrayList<>();
	}

	@Override
	public Species getAltFormeOfSpecies(Species base, int forme) {
		int pokeNum = Gen4Constants.getAbsolutePokeNumByBaseForme(base.getNumber(), forme);
		return pokeNum != 0 ? pokes[pokeNum] : base;
	}

	@Override
	public SpeciesSet getIrregularFormes() {
		return new SpeciesSet();
	}

	@Override
	public boolean hasFunctionalFormes() {
		return romEntry.getRomType() != Gen4Constants.Type_DP;
	}

	@Override
	public List<Species> getStarters() {
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			List<Integer> tailOffsets = RomFunctions.search(arm9, Gen4Constants.hgssStarterCodeSuffix);
			if (tailOffsets.size() == 1) {
				// Found starters
				int starterOffset = tailOffsets.get(0) - 13;
				int poke1 = readWord(arm9, starterOffset);
				int poke2 = readWord(arm9, starterOffset + 4);
				int poke3 = readWord(arm9, starterOffset + 8);
				return Arrays.asList(pokes[poke1], pokes[poke2], pokes[poke3]);
			} else {
				return Arrays.asList(pokes[SpeciesIDs.chikorita], pokes[SpeciesIDs.cyndaquil], pokes[SpeciesIDs.totodile]);
			}
		} else {
			try {
				byte[] starterData = readOverlay(romEntry.getIntValue("StarterPokemonOvlNumber"));
				int poke1 = readWord(starterData, romEntry.getIntValue("StarterPokemonOffset"));
				int poke2 = readWord(starterData, romEntry.getIntValue("StarterPokemonOffset") + 4);
				int poke3 = readWord(starterData, romEntry.getIntValue("StarterPokemonOffset") + 8);
				return Arrays.asList(pokes[poke1], pokes[poke2], pokes[poke3]);
			} catch (IOException e) {
				throw new RomIOException(e);
			}
		}
	}

	@Override
	public boolean setStarters(List<Species> newStarters) {
		if (newStarters.size() != 3) {
			return false;
		}

		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			List<Integer> tailOffsets = RomFunctions.search(arm9, Gen4Constants.hgssStarterCodeSuffix);
			if (tailOffsets.size() == 1) {
				// Found starters
				int starterOffset = tailOffsets.get(0) - 13;
				writeWord(arm9, starterOffset, newStarters.get(0).getNumber());
				writeWord(arm9, starterOffset + 4, newStarters.get(1).getNumber());
				writeWord(arm9, starterOffset + 8, newStarters.get(2).getNumber());
				// Go fix the rival scripts, which rely on fixed pokemon numbers
				// The logic to be changed each time is roughly:
				// Set 0x800C = player starter
				// If(0x800C==152) { trainerbattle rival w/ cynda }
				// ElseIf(0x800C==155) { trainerbattle rival w/ totodile }
				// Else { trainerbattle rival w/ chiko }
				// So we basically have to adjust the 152 and the 155.
				int[] filesWithRivalScript = Gen4Constants.hgssFilesWithRivalScript;
				// below code represents a rival script for sure
				// it means: StoreStarter2 0x800C; If 0x800C 152; CheckLR B_!=
				// <offset to follow>
				byte[] magic = Gen4Constants.hgssRivalScriptMagic;
				NARCArchive scriptNARC = scriptNarc;
				for (int fileCheck : filesWithRivalScript) {
					byte[] file = scriptNARC.files.get(fileCheck);
					List<Integer> rivalOffsets = RomFunctions.search(file, magic);
					if (rivalOffsets.size() == 1) {
						// found, adjust
						int baseOffset = rivalOffsets.get(0);
						// Replace 152 (chiko) with first starter
						writeWord(file, baseOffset + 8, newStarters.get(0).getNumber());
						int jumpAmount = readLong(file, baseOffset + 13);
						int secondBase = jumpAmount + baseOffset + 17;
						// TODO find out what this constant 0x11 is and remove
						// it
						if (file[secondBase] != 0x11 || (file[secondBase + 4] & 0xFF) != SpeciesIDs.cyndaquil) {
							// This isn't what we were expecting...
						} else {
							// Replace 155 (cynda) with 2nd starter
							writeWord(file, secondBase + 4, newStarters.get(1).getNumber());
						}
					}
				}
				// Fix starter text
				List<String> spStrings = getStrings(romEntry.getIntValue("StarterScreenTextOffset"));
				String[] intros = new String[] { "So, you like", "You????�ll take", "Do you want" };
				for (int i = 0; i < 3; i++) {
					Species newStarter = newStarters.get(i);
					int color = (i == 0) ? 3 : i;
					String newStarterDesc = "Professor Elm: " + intros[i] + " \\vFF00\\z000" + color
							+ newStarter.getName() + "\\vFF00\\z0000,\\nthe " + newStarter.getPrimaryType(false).camelCase()
							+ "-type Pok????�mon?";
					spStrings.set(i + 1, newStarterDesc);
					String altStarterDesc = "\\vFF00\\z000" + color + newStarter.getName() + "\\vFF00\\z0000, the "
							+ newStarter.getPrimaryType(false).camelCase() + "-type Pok????�mon, is\\nin this Pok????� Ball!";
					spStrings.set(i + 4, altStarterDesc);
				}
				setStrings(romEntry.getIntValue("StarterScreenTextOffset"), spStrings);

				try {
					// Fix starter cries
					byte[] starterPokemonOverlay = readOverlay(romEntry.getIntValue("StarterPokemonOvlNumber"));
					String spCriesPrefix = Gen4Constants.starterCriesPrefix;
					int offset = find(starterPokemonOverlay, spCriesPrefix);
					if (offset > 0) {
						offset += spCriesPrefix.length() / 2; // because it was a prefix
						for (Species newStarter : newStarters) {
							writeLong(starterPokemonOverlay, offset, newStarter.getNumber());
							offset += 4;
						}
					}
					writeOverlay(romEntry.getIntValue("StarterPokemonOvlNumber"), starterPokemonOverlay);
				} catch (IOException e) {
					throw new RomIOException(e);
				}
				return true;
			} else {
				return false;
			}
		} else {
			try {
				byte[] starterData = readOverlay(romEntry.getIntValue("StarterPokemonOvlNumber"));
				writeWord(starterData, romEntry.getIntValue("StarterPokemonOffset"), newStarters.get(0).getNumber());
				writeWord(starterData, romEntry.getIntValue("StarterPokemonOffset") + 4, newStarters.get(1).getNumber());
				writeWord(starterData, romEntry.getIntValue("StarterPokemonOffset") + 8, newStarters.get(2).getNumber());

				if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
					String starterPokemonGraphicsPrefix = romEntry.getStringValue("StarterPokemonGraphicsPrefix");
					int offset = find(starterData, starterPokemonGraphicsPrefix);
					if (offset > 0) {

						// The original subroutine for handling the starter graphics is optimized by the
						// compiler to use
						// a value as a pointer offset and then adding to that value to get the
						// Pokemon's index.
						// We will keep this logic, but in order to make place for an extra instruction
						// that will let
						// us set the Pokemon index to any Gen 4 value we want, we change the base
						// address of the
						// pointer that the offset is used for; this also requires some changes to the
						// instructions
						// that utilize this pointer.
						offset += starterPokemonGraphicsPrefix.length() / 2;

						// Move down a section of instructions to make place for an add instruction that
						// modifies the
						// pointer. A PC-relative load and a BL have to be slightly modified to point to
						// the correct
						// thing.
						writeWord(starterData, offset + 0xC, readWord(starterData, offset + 0xA));
						if (offset % 4 == 0) {
							starterData[offset + 0xC] = (byte) (starterData[offset + 0xC] - 1);
						}
						writeWord(starterData, offset + 0xA, readWord(starterData, offset + 0x8));
						starterData[offset + 0xA] = (byte) (starterData[offset + 0xA] - 1);
						writeWord(starterData, offset + 0x8, readWord(starterData, offset + 0x6));
						writeWord(starterData, offset + 0x6, readWord(starterData, offset + 0x4));
						writeWord(starterData, offset + 0x4, readWord(starterData, offset + 0x2));
						// This instruction normally uses the value in r0 (0x200) as an offset for an
						// ldr that uses
						// the pointer as its base address; we change this to not use an offset at all
						// because we
						// change the instruction before it to add that 0x200 to the base address.
						writeWord(starterData, offset + 0x2, 0x6828);
						writeWord(starterData, offset, 0x182D);

						offset += 0x16;
						// Change another ldr to not use any offset since we changed the base address
						writeWord(starterData, offset, 0x6828);

						offset += 0xA;

						// This is where we write the actual starter numbers, as two adds/subs

						for (int i = 0; i < 3; i++) {
							// The offset that we want to use for the pointer is 4, then 8, then 0xC.
							// We take the difference of the Pokemon's index and the offset, because we want
							// to add
							// (or subtract) that to/from the offset to get the Pokemon's index later.
							int starterDiff = newStarters.get(i).getNumber() - (4 * (i + 1));

							// Prepare two "add r0, #0x0" instructions where we'll modify the immediate
							int instr1 = 0x3200;
							int instr2 = 0x3200;

							if (starterDiff < 0) {
								// Pokemon's index is below the offset, change to a sub instruction
								instr1 |= 0x800;
								starterDiff = Math.abs(starterDiff);
							} else if (starterDiff > 255) {
								// Pokemon's index is above (offset + 255), need to utilize the second add
								// instruction
								instr2 |= 0xFF;
								starterDiff -= 255;
							}

							// Modify the first add instruction's immediate value
							instr1 |= (starterDiff & 0xFF);

							// Change the original offset that's loaded, then move an instruction up one
							// step
							// and insert our add instructions
							starterData[offset] = (byte) (4 * (i + 1));
							writeWord(starterData, offset + 2, readWord(starterData, offset + 4));
							writeWord(starterData, offset + 4, instr1);
							writeWord(starterData, offset + 8, instr2);

							// Repeat for each starter
							offset += 0xE;
						}

						// Change a loaded value to be 1 instead of 0x81 because we changed the pointer
						starterData[offset] = 1;

						// Also need to change one usage of the pointer we changed, in the inner
						// function
						String starterPokemonGraphicsPrefixInner = romEntry
								.getStringValue("StarterPokemonGraphicsPrefixInner");
						offset = find(starterData, starterPokemonGraphicsPrefixInner);

						if (offset > 0) {
							offset += starterPokemonGraphicsPrefixInner.length() / 2;
							starterData[offset + 1] = 0x68;
						}
					}
				}

				writeOverlay(romEntry.getIntValue("StarterPokemonOvlNumber"), starterData);
				// Patch DPPt-style rival scripts
				// these have a series of IfJump commands
				// following pokemon IDs
				// the jumps either go to trainer battles, or a HoF times
				// checker, or the StarterBattle command (Pt only)
				// the HoF times checker case is for the Fight Area or Survival
				// Area (depending on version).
				// the StarterBattle case is for Route 201 in Pt.
				int[] filesWithRivalScript = (romEntry.getRomType() == Gen4Constants.Type_Plat)
						? Gen4Constants.ptFilesWithRivalScript
						: Gen4Constants.dpFilesWithRivalScript;
				byte[] magic = Gen4Constants.dpptRivalScriptMagic;
				NARCArchive scriptNARC = scriptNarc;
				for (int fileCheck : filesWithRivalScript) {
					byte[] file = scriptNARC.files.get(fileCheck);
					List<Integer> rivalOffsets = RomFunctions.search(file, magic);
					if (!rivalOffsets.isEmpty()) {
						for (int baseOffset : rivalOffsets) {
							// found, check for trainer battle or HoF
							// check at jump
							int jumpLoc = baseOffset + magic.length;
							int jumpTo = readLong(file, jumpLoc) + jumpLoc + 4;
							// TODO find out what these constants are and remove
							// them
							if (readWord(file, jumpTo) != 0xE5 && readWord(file, jumpTo) != 0x28F
									&& (readWord(file, jumpTo) != 0x125
											|| romEntry.getRomType() != Gen4Constants.Type_Plat)) {
								continue; // not a rival script
							}
							// Replace the two starter-words 387 and 390
							writeWord(file, baseOffset + 0x8, newStarters.get(0).getNumber());
							writeWord(file, baseOffset + 0x15, newStarters.get(1).getNumber());
						}
					}
				}
				// Tag battles with rival or friend
				// Have their own script magic
				// 2 for Lucas/Dawn (=4 occurrences), 1 or 2 for Barry
				byte[] tagBattleMagic = Gen4Constants.dpptTagBattleScriptMagic1;
				byte[] tagBattleMagic2 = Gen4Constants.dpptTagBattleScriptMagic2;
				int[] filesWithTagBattleScript = (romEntry.getRomType() == Gen4Constants.Type_Plat)
						? Gen4Constants.ptFilesWithTagScript
						: Gen4Constants.dpFilesWithTagScript;
				for (int fileCheck : filesWithTagBattleScript) {
					byte[] file = scriptNARC.files.get(fileCheck);
					List<Integer> tbOffsets = RomFunctions.search(file, tagBattleMagic);
					if (!tbOffsets.isEmpty()) {
						for (int baseOffset : tbOffsets) {
							// found, check for second part
							int secondPartStart = baseOffset + tagBattleMagic.length + 2;
							if (secondPartStart + tagBattleMagic2.length > file.length) {
								continue; // match failed
							}
							boolean valid = true;
							for (int spo = 0; spo < tagBattleMagic2.length; spo++) {
								if (file[secondPartStart + spo] != tagBattleMagic2[spo]) {
									valid = false;
									break;
								}
							}
							if (!valid) {
								continue;
							}
							// Make sure the jump following the second
							// part jumps to a <return> command
							int jumpLoc = secondPartStart + tagBattleMagic2.length;
							int jumpTo = readLong(file, jumpLoc) + jumpLoc + 4;
							// TODO find out what this constant is and remove it
							if (readWord(file, jumpTo) != 0x1B) {
								continue; // not a tag battle script
							}
							// Replace the two starter-words
							if (readWord(file, baseOffset + 0x21) == SpeciesIDs.turtwig) {
								// first starter
								writeWord(file, baseOffset + 0x21, newStarters.get(0).getNumber());
							} else {
								// third starter
								writeWord(file, baseOffset + 0x21, newStarters.get(2).getNumber());
							}
							// second starter
							writeWord(file, baseOffset + 0xE, newStarters.get(1).getNumber());
						}
					}
				}
				// Fix starter script text
				// The starter picking screen
				List<String> spStrings = getStrings(romEntry.getIntValue("StarterScreenTextOffset"));
				// Get pokedex info
				List<String> pokedexSpeciesStrings = getStrings(romEntry.getIntValue("PokedexSpeciesTextOffset"));
				for (int i = 0; i < 3; i++) {
					Species newStarter = newStarters.get(i);
					int color = (i == 0) ? 3 : i;
					String newStarterDesc = "\\vFF00\\z000" + color + pokedexSpeciesStrings.get(newStarter.getNumber())
							+ " " + newStarter.getName() + "\\vFF00\\z0000!\\nWill you take this Pok????�mon?";
					spStrings.set(i + 1, newStarterDesc);
				}
				// rewrite starter picking screen
				setStrings(romEntry.getIntValue("StarterScreenTextOffset"), spStrings);
				if (romEntry.getRomType() == Gen4Constants.Type_DP) {
					// what rival says after we get the Pokemon
					List<String> lakeStrings = getStrings(romEntry.getIntValue("StarterLocationTextOffset"));
					lakeStrings.set(Gen4Constants.dpStarterStringIndex,
							"\\v0103\\z0000: Fwaaah!\\nYour Pok????�mon totally rocked!\\pBut mine was way tougher\\nthan yours!\\p...They were other people????�s\\nPok????�mon, though...\\pBut we had to use them...\\nThey won????�t mind, will they?\\p");
					setStrings(romEntry.getIntValue("StarterLocationTextOffset"), lakeStrings);
				} else {
					// what rival says after we get the Pokemon
					List<String> r201Strings = getStrings(romEntry.getIntValue("StarterLocationTextOffset"));
					r201Strings.set(Gen4Constants.ptStarterStringIndex,
							"\\v0103\\z0000\\z0000: Then, I choose you!\\nI????�m picking this one!\\p");
					setStrings(romEntry.getIntValue("StarterLocationTextOffset"), r201Strings);
				}
			} catch (IOException e) {
				throw new RomIOException(e);
			}
			return true;
		}
	}

	@Override
	public boolean supportsStarterHeldItems() {
		return romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat;
	}

    @Override
    public List<Integer> getStarterHeldItems() {
        int starterScriptNumber = romEntry.getIntValue("StarterPokemonScriptOffset");
        int starterHeldItemOffset = romEntry.getIntValue("StarterPokemonHeldItemOffset");
        byte[] file = scriptNarc.files.get(starterScriptNumber);
        int item = FileFunctions.read2ByteInt(file, starterHeldItemOffset);
        return Collections.singletonList(item);
    }

    @Override
    public void setStarterHeldItems(List<Integer> items) {
        int starterScriptNumber = romEntry.getIntValue("StarterPokemonScriptOffset");
        int starterHeldItemOffset = romEntry.getIntValue("StarterPokemonHeldItemOffset");
        byte[] file = scriptNarc.files.get(starterScriptNumber);
        FileFunctions.write2ByteInt(file, starterHeldItemOffset, items.get(0));
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

		List<EncounterArea> encounterAreas;
		try {
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				encounterAreas = getEncountersHGSS(useTimeOfDay);
			} else {
				encounterAreas = getEncountersDPPt(useTimeOfDay);
			}
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}

		Gen4Constants.tagEncounterAreas(encounterAreas, romEntry.getRomType(), useTimeOfDay);
		return encounterAreas;
	}

	private List<EncounterArea> getEncountersDPPt(boolean useTimeOfDay) throws IOException {
		List<EncounterArea> encounterAreas = new ArrayList<>();

		readMainEncountersDPPt(encounterAreas, useTimeOfDay);
		readExtraEncountersDPPt(encounterAreas);

		return encounterAreas;
	}

	private void readMainEncountersDPPt(List<EncounterArea> encounterAreas, boolean useTimeOfDay) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);
		// Credit for
		// https://github.com/magical/pokemon-encounters/blob/master/nds/encounters-gen4-sinnoh.py
		// for the structure for this.
		int c = -1;
		for (byte[] b : encounterData.files) {
			c++;
			if (!wildMapNames.containsKey(c)) {
				wildMapNames.put(c, "? Unknown ?");
			}
			String mapName = wildMapNames.get(c);
			int walkingRate = readLong(b, 0);
			if (walkingRate != 0) {
				// up to 4
				EncounterArea walkingArea = new EncounterArea(readEncountersDPPt(b, 4, 12));
				walkingArea.setDisplayName(mapName + " Grass/Cave");
				walkingArea.setEncounterType(EncounterType.WALKING);
				walkingArea.setRate(walkingRate);
				walkingArea.setMapIndex(c);
				encounterAreas.add(walkingArea);

				// Time of day replacements?
				if (useTimeOfDay) {
					for (int i = 0; i < 4; i++) {
						int pknum = readLong(b, 108 + 4 * i);
						if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
							Species pk = pokes[pknum];
							Encounter enc = new Encounter();
							enc.setLevel(walkingArea.get(Gen4Constants.dpptAlternateSlots[i + 2]).getLevel());
							enc.setSpecies(pk);
							walkingArea.add(enc);
						}
					}
				}
				// (if useTimeOfDay is off, just override them later)

				// Other conditional replacements (swarm, radar, GBA)
				EncounterArea condsArea = new EncounterArea();
				condsArea.setDisplayName(mapName + " Swarm/Radar/GBA");
				condsArea.setEncounterType(EncounterType.SPECIAL);
				condsArea.setRate(walkingRate);
				condsArea.setMapIndex(c);
				for (int i = 0; i < 20; i++) {
					if (i >= 2 && i <= 5) {
						// Time of day slot, handled already
						continue;
					}
					int offs = 100 + i * 4 + (i >= 10 ? 24 : 0);
					int pknum = readLong(b, offs);
					if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
						Species pk = pokes[pknum];
						Encounter enc = new Encounter();
						enc.setLevel(walkingArea.get(Gen4Constants.dpptAlternateSlots[i]).getLevel());
						enc.setSpecies(pk);
						condsArea.add(enc);
					}
				}
				if (!condsArea.isEmpty()) {
					encounterAreas.add(condsArea);
				}
			}

			// up to 204, 5 sets of "sea" encounters to go
			int offset = 204;
			for (int i = 0; i < 5; i++) {
				int rate = readLong(b, offset);
				offset += 4;
				List<Encounter> seaEncounters = readSeaEncountersDPPt(b, offset, 5);
				offset += 40;
				if (rate == 0 || i == 1) {
					continue;
				}
				EncounterArea seaArea = new EncounterArea(seaEncounters);
				seaArea.setDisplayName(mapName + " " + Gen4Constants.dpptWaterSlotSetNames[i]);
				seaArea.setEncounterType(i == 0 ? EncounterType.SURFING : EncounterType.FISHING);
				seaArea.setMapIndex(c);
				seaArea.setRate(rate);
				encounterAreas.add(seaArea);
			}
		}
	}

	private void readExtraEncountersDPPt(List<EncounterArea> encounterAreas) throws IOException {
		String extraEncountersFile = romEntry.getFile("ExtraEncounters");
		NARCArchive extraEncounterData = readNARC(extraEncountersFile);
		byte[] encounterOverlay = readOverlay(romEntry.getIntValue("EncounterOvlNumber"));

		readFeebasTileEncounters(encounterAreas, extraEncounterData, encounterOverlay);
		readHoneyTreeEncounters(encounterAreas, extraEncounterData, encounterOverlay);
		readTrophyGardenRotatingEncounters(encounterAreas, extraEncounterData);
		readGreatMarshRotatingEncounters(encounterAreas, extraEncounterData);
	}

	private void readFeebasTileEncounters(List<EncounterArea> encounterAreas, NARCArchive extraEncounterData, byte[] encounterOverlay) {
		byte[] feebasData = extraEncounterData.files.get(0);
		EncounterArea area = readExtraEncounterAreaDPPt(feebasData, 0, 1);
		int offset = find(encounterOverlay, Gen4Constants.feebasLevelPrefixDPPt);
		if (offset > 0) {
			offset += Gen4Constants.feebasLevelPrefixDPPt.length() / 2; // because it was a prefix
			for (Encounter enc : area) {
				enc.setMaxLevel(encounterOverlay[offset]);
				enc.setLevel(encounterOverlay[offset + 4]);
			}
		}
		area.setIdentifiers("Mt. Coronet Feebas Tiles", Gen4Constants.mtCoronetFeebasLakeMapIndex,
				EncounterType.SPECIAL);
		encounterAreas.add(area);
	}

	private void readHoneyTreeEncounters(List<EncounterArea> encounterAreas, NARCArchive extraEncounterData, byte[] encounterOverlay) {
		int offset;
		int[] honeyTreeOffsets = romEntry.getArrayValue("HoneyTreeOffsets");
		for (int i = 0; i < honeyTreeOffsets.length; i++) {
			byte[] honeyTreeData = extraEncounterData.files.get(honeyTreeOffsets[i]);
			EncounterArea area = readExtraEncounterAreaDPPt(honeyTreeData, 0, 6);
			offset = find(encounterOverlay, Gen4Constants.honeyTreeLevelPrefixDPPt);
			if (offset > 0) {
				offset += Gen4Constants.honeyTreeLevelPrefixDPPt.length() / 2; // because it was a prefix

				// To make different min levels work, we rewrite some assembly code in
				// setEncountersDPPt, which has the side effect of making reading the min
				// level easier. In case the original code is still there, just hardcode
				// the min level used in the vanilla game, since extracting it is hard.
				byte level;
				if (encounterOverlay[offset + 46] == 0x0B && encounterOverlay[offset + 47] == 0x2E) {
					level = 5;
				} else {
					level = encounterOverlay[offset + 46];
				}
				for (Encounter enc : area) {
					enc.setMaxLevel(encounterOverlay[offset + 102]);
					enc.setLevel(level);
				}
			}
			area.setDisplayName("Honey Tree Group " + (i + 1));
			area.setEncounterType(EncounterType.INTERACT);
			encounterAreas.add(area);
		}
	}

	private void readTrophyGardenRotatingEncounters(List<EncounterArea> encounterAreas, NARCArchive extraEncounterData) {
		// Trophy Garden rotating Pokemon (Mr. Backlot)
		byte[] trophyGardenData = extraEncounterData.files.get(8);
		EncounterArea area = readExtraEncounterAreaDPPt(trophyGardenData, 0, 16);

		// Trophy Garden rotating Pokemon get their levels from the regular Trophy Garden grass encounters,
		// indices 6 and 7. To make the logs nice, read in these encounters for this area and set the level
		// and maxLevel for the rotating encounters appropriately.
		int trophyGardenGrassEncounterIndex = Gen4Constants.getTrophyGardenGrassEncounterIndex(romEntry.getRomType());
		EncounterArea trophyGardenGrassEncounterSet = encounterAreas.get(trophyGardenGrassEncounterIndex);
		int level1 = trophyGardenGrassEncounterSet.get(6).getLevel();
		int level2 = trophyGardenGrassEncounterSet.get(7).getLevel();
		for (Encounter enc : area) {
			enc.setLevel(Math.min(level1, level2));
			if (level1 != level2) {
				enc.setMaxLevel(Math.max(level1, level2));
			}
		}
		area.setIdentifiers("Trophy Garden Rotating Pokemon (via Mr. Backlot)",
				Gen4Constants.trophyGardenMapIndex, EncounterType.SPECIAL);
		area.setForceMultipleSpecies(true); // prevents a possible softlock

		encounterAreas.add(area);
	}

	private void readGreatMarshRotatingEncounters(List<EncounterArea> encounterAreas, NARCArchive extraEncounterData) {
		int[] greatMarshOffsets = new int[] { 9, 10 };
		for (int i = 0; i < greatMarshOffsets.length; i++) {
			byte[] greatMarshData = extraEncounterData.files.get(greatMarshOffsets[i]);
			EncounterArea area = readExtraEncounterAreaDPPt(greatMarshData, 0, 32);

			// Great Marsh rotating Pokemon get their levels from the regular Great Marsh grass encounters,
			// indices 6 and 7. To make the logs nice, read in these encounters for all areas and set the
			// level and maxLevel for the rotating encounters appropriately.
			int level = 100;
			int maxLevel = 0;
			List<Integer> marshGrassEncounterIndices = Gen4Constants.getMarshGrassEncounterIndices(romEntry.getRomType());
			for (Integer marshGrassEncounterIndex : marshGrassEncounterIndices) {
				EncounterArea marshGrassArea = encounterAreas.get(marshGrassEncounterIndex);
				int currentLevel = marshGrassArea.get(6).getLevel();
				if (currentLevel < level) {
					level = currentLevel;
				}
				if (currentLevel > maxLevel) {
					maxLevel = currentLevel;
				}
				currentLevel = marshGrassArea.get(7).getLevel();
				if (currentLevel < level) {
					level = currentLevel;
				}
				if (currentLevel > maxLevel) {
					maxLevel = currentLevel;
				}
			}
			for (Encounter enc : area) {
				enc.setLevel(level);
				enc.setMaxLevel(maxLevel);
			}
			String pokedexStatus = i == 0 ? "(Post-National Dex)" : "(Pre-National Dex)";
			area.setIdentifiers("Great Marsh Rotating Pokemon " + pokedexStatus, -2,
					EncounterType.SPECIAL);
			encounterAreas.add(area);
		}
	}

	private List<Encounter> readEncountersDPPt(byte[] data, int offset, int amount) {
		List<Encounter> encounters = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			int level = readLong(data, offset + i * 8);
			int pokemon = readLong(data, offset + 4 + i * 8);
			Encounter enc = new Encounter();
			enc.setLevel(level);
			enc.setSpecies(pokes[pokemon]);
			encounters.add(enc);
		}
		return encounters;
	}

	private List<Encounter> readSeaEncountersDPPt(byte[] data, int offset, int amount) {
		List<Encounter> encounters = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			int level = readLong(data, offset + i * 8);
			int pokemon = readLong(data, offset + 4 + i * 8);
			Encounter enc = new Encounter();
			enc.setLevel(level >> 8);
			enc.setMaxLevel(level & 0xFF);
			enc.setSpecies(pokes[pokemon]);
			encounters.add(enc);
		}
		return encounters;
	}

	private EncounterArea readExtraEncounterAreaDPPt(byte[] data, int offset, int amount) {
		EncounterArea area = new EncounterArea();
		area.setRate(1);
		for (int i = 0; i < amount; i++) {
			int pokemon = readLong(data, offset + i * 4);
			Encounter e = new Encounter();
			e.setLevel(1);
			e.setSpecies(pokes[pokemon]);
			area.add(e);
		}
		return area;
	}

	private List<EncounterArea> getEncountersHGSS(boolean useTimeOfDay) throws IOException {
		List<EncounterArea> encounterAreas = new ArrayList<>();

		readMainEncountersHGSS(encounterAreas, useTimeOfDay);
		readExtraEncountersHGSS(encounterAreas);

		return encounterAreas;
	}

	private void readMainEncountersHGSS(List<EncounterArea> encounterAreas, boolean useTimeOfDay) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);
		// Credit for
		// https://github.com/magical/pokemon-encounters/blob/master/nds/encounters-gen4-johto.py
		// for the structure for this.
		int[] amounts = new int[] { 0, 5, 2, 5, 5, 5 };
		int mapIndex = -1;
		for (byte[] b : encounterData.files) {
			mapIndex++;
			boolean badMapIndex = false;
			if(mapIndex == Gen4Constants.nationalParkBadMapIndex) {
				mapIndex = Gen4Constants.nationalParkMapIndex;
				badMapIndex = true; //this is. a terrible way to do this, but it works.
			}
			if (!wildMapNames.containsKey(mapIndex)) {
				wildMapNames.put(mapIndex, "? Unknown ?");
			}
			String mapName = wildMapNames.get(mapIndex);
			int[] rates = new int[6];
			rates[0] = b[0] & 0xFF;
			rates[1] = b[1] & 0xFF;
			rates[2] = b[2] & 0xFF;
			rates[3] = b[3] & 0xFF;
			rates[4] = b[4] & 0xFF;
			rates[5] = b[5] & 0xFF;
			// Up to 8 after the rates
			// Walking has to be handled on its own because the levels
			// are reused for every time of day
			int[] walkingLevels = new int[12];
			for (int i = 0; i < 12; i++) {
				walkingLevels[i] = b[8 + i] & 0xFF;
			}
			// Up to 20 now (12 for levels)
			Species[][] walkingPokes = new Species[3][12];
			walkingPokes[0] = readPokemonHGSS(b, 20, 12);
			walkingPokes[1] = readPokemonHGSS(b, 44, 12);
			walkingPokes[2] = readPokemonHGSS(b, 68, 12);
			// Up to 92 now (12*2*3 for pokemon)
			if (rates[0] != 0) {
				if (!useTimeOfDay) {
					// Just write "day" encounters
					EncounterArea walkingArea = new EncounterArea(stitchEncsToLevels(walkingPokes[1], walkingLevels));
					walkingArea.setRate(rates[0]);
					walkingArea.setIdentifiers(mapName + " Grass/Cave", mapIndex, EncounterType.WALKING);
					encounterAreas.add(walkingArea);
				} else {
					for (int i = 0; i < 3; i++) {
						EncounterArea walkingArea = new EncounterArea(stitchEncsToLevels(walkingPokes[i], walkingLevels));
						walkingArea.setRate(rates[0]);
						walkingArea.setIdentifiers(
								mapName + " " + Gen4Constants.hgssTimeOfDayNames[i] + " Grass/Cave",
								mapIndex, EncounterType.WALKING);
						encounterAreas.add(walkingArea);
					}
				}
			}

			// TODO: these are time dependent (only on wednesdays/thursdays),
			//  should they not be excluded when useTimeOfDay == false ?
			// Hoenn/Sinnoh Radio
			EncounterArea radioArea = readOptionalEncounterAreaHGSS(b, 92, 4);
			radioArea.setIdentifiers(mapName + " Hoenn/Sinnoh Radio", mapIndex, EncounterType.SPECIAL);
			if (!radioArea.isEmpty()) {
				encounterAreas.add(radioArea);
			}

			// Up to 100 now... 2*2*2 for radio pokemon
			// Time to handle Surfing, Rock Smash, Rods
			int offset = 100;
			for (int i = 1; i < 6; i++) {
				List<Encounter> seaEncounters = readSeaEncountersHGSS(b, offset, amounts[i]);
				offset += 4 * amounts[i];
				if (rates[i] != 0) {
					// Valid area.
					EncounterArea seaArea = new EncounterArea(seaEncounters);
					seaArea.setIdentifiers(mapName + " " + Gen4Constants.hgssNonWalkingAreaNames[i], mapIndex,
							Gen4Constants.hgssNonWalkingAreaTypes[i]);
					seaArea.setRate(rates[i]);
					encounterAreas.add(seaArea);
				}
			}

			// Swarms
			EncounterArea swarmArea = readOptionalEncounterAreaHGSS(b, offset, 2);
			swarmArea.setIdentifiers(mapName + " Swarms", mapIndex, EncounterType.SPECIAL);
			if (!swarmArea.isEmpty()) {
				encounterAreas.add(swarmArea);
			}

			// TODO: Disable these... somehow when useTimeOfDay == false. It's tricky since I don't know what
			//  encounters are being replaced in the usual fishing area/how it works
			EncounterArea nightFishingReplacementArea = readOptionalEncounterAreaHGSS(b, offset + 4, 1);
			nightFishingReplacementArea.setIdentifiers(mapName + " Night Fishing Replacement", mapIndex,
					EncounterType.FISHING);
			if (!nightFishingReplacementArea.isEmpty()) {
				encounterAreas.add(nightFishingReplacementArea);
			}
			EncounterArea fishingSwarmsArea = readOptionalEncounterAreaHGSS(b, offset + 6, 1);
			fishingSwarmsArea.setIdentifiers(mapName + " Fishing Swarm", mapIndex, EncounterType.SPECIAL);
			if (!fishingSwarmsArea.isEmpty()) {
				encounterAreas.add(fishingSwarmsArea);
			}

			if(badMapIndex) {
				mapIndex = Gen4Constants.nationalParkBadMapIndex;
			}
		}
	}

	private void readExtraEncountersHGSS(List<EncounterArea> encounterAreas) throws IOException {
		readHeadbuttEncounters(encounterAreas);
		readBugCatchingContestEncounters(encounterAreas);
	}

	private void readHeadbuttEncounters(List<EncounterArea> encounterAreas) throws IOException {

		String headbuttEncountersFile = romEntry.getFile("HeadbuttPokemon");
		NARCArchive headbuttEncounterData = readNARC(headbuttEncountersFile);
		int mapID = -1;
		int lastCreatedID = wildMapNames.size() - 1;
		for (byte[] b : headbuttEncounterData.files) {
			mapID++;

			// Each headbutt encounter file starts with four bytes, which I believe are used
			// to indicate the number of "normal" and "special" trees that are available in
			// this area. For areas that don't contain any headbutt encounters, these four
			// bytes constitute the only four bytes in the file, so we can stop looking at
			// this file in this case.
			if (b.length == 4) {
				continue;
			}

			String mapName = headbuttMapNames.get(mapID);
			EncounterArea area = readHeadbuttEncounterAreaHGSS(b, 4, 18);

			// Map 24 is an unused version of Route 16, but it still has valid headbutt
			// encounter data.
			// Avoid adding it to the list of encounters to prevent confusion.
			if (!area.isEmpty() && mapID != 24) {
				area.setDisplayName(mapName + " Headbutt");
				area.setEncounterType(EncounterType.INTERACT);

				//Headbutt encounters use a different set of map IDs than regular wild encounters
				//To make the map IDs line up, we need to check the other map
				for(Map.Entry<Integer, String> map : wildMapNames.entrySet()) {
					if(map.getValue().equals(mapName)) {
						area.setMapIndex(map.getKey());
						break;
					}
				}
				if(area.getMapIndex() == -1) {
					//if we don't find it, it remains at the default of -1
					//However, we don't want it to be negative, since that signifies shared maps
					//Therefore, we will fabricate an arbitrary new index for this map
					do {
						lastCreatedID++;
					} while(wildMapNames.containsKey(lastCreatedID));
					//lastCreatedID should now be an unused index
					area.setMapIndex(lastCreatedID);
				}

				encounterAreas.add(area);
			}



		}
	}

	private void readBugCatchingContestEncounters(List<EncounterArea> encounterAreas) throws IOException {
		String bccEncountersFile = romEntry.getFile("BCCWilds");
		byte[] bccEncountersData = readFile(bccEncountersFile);

		EncounterArea preNationalDexArea = readBCCEncounterAreaHGSS(bccEncountersData, 0, 10);
		preNationalDexArea.setIdentifiers("Bug Catching Contest (Pre-National Dex)",
				Gen4Constants.nationalParkMapIndex, EncounterType.SPECIAL);
		if (!preNationalDexArea.isEmpty()) {
			encounterAreas.add(preNationalDexArea);
		}
		EncounterArea postNationalDexTuesArea = readBCCEncounterAreaHGSS(bccEncountersData, 80, 10);
		postNationalDexTuesArea.setIdentifiers("Bug Catching Contest (Post-National Dex, Tuesdays)",
				Gen4Constants.nationalParkMapIndex, EncounterType.SPECIAL);
		if (!postNationalDexTuesArea.isEmpty()) {
			encounterAreas.add(postNationalDexTuesArea);
		}
		EncounterArea postNationalDexThursArea = readBCCEncounterAreaHGSS(bccEncountersData, 160, 10);
		postNationalDexThursArea.setIdentifiers("Bug Catching Contest (Post-National Dex, Thursdays)",
				Gen4Constants.nationalParkMapIndex, EncounterType.SPECIAL);
		if (!postNationalDexThursArea.isEmpty()) {
			encounterAreas.add(postNationalDexThursArea);
		}
		EncounterArea postNationalDexSatArea = readBCCEncounterAreaHGSS(bccEncountersData, 240, 10);
		postNationalDexSatArea.setIdentifiers("Bug Catching Contest (Post-National Dex, Saturdays)",
				Gen4Constants.nationalParkMapIndex, EncounterType.SPECIAL);
		if (!postNationalDexSatArea.isEmpty()) {
			encounterAreas.add(postNationalDexSatArea);
		}
	}

	private EncounterArea readOptionalEncounterAreaHGSS(byte[] data, int offset, int amount) {
		EncounterArea area = new EncounterArea();
		area.setRate(1);
		for (int i = 0; i < amount; i++) {
			int pokemon = readWord(data, offset + i * 2);
			if (pokemon != 0) {
				Encounter enc = new Encounter();
				enc.setLevel(1);
				enc.setSpecies(pokes[pokemon]);
				area.add(enc);
			}
		}
		return area;
	}

	private Species[] readPokemonHGSS(byte[] data, int offset, int amount) {
		Species[] pokesHere = new Species[amount];
		for (int i = 0; i < amount; i++) {
			pokesHere[i] = pokes[readWord(data, offset + i * 2)];
		}
		return pokesHere;
	}

	private List<Encounter> readSeaEncountersHGSS(byte[] data, int offset, int amount) {
		List<Encounter> encounters = new ArrayList<>();
		for (int i = 0; i < amount; i++) {
			int level = readWord(data, offset + i * 4);
			int pokemon = readWord(data, offset + 2 + i * 4);
			Encounter enc = new Encounter();
			enc.setLevel(level & 0xFF);
			enc.setMaxLevel(level >> 8);
			enc.setSpecies(pokes[pokemon]);
			encounters.add(enc);
		}
		return encounters;
	}

	private EncounterArea readHeadbuttEncounterAreaHGSS(byte[] data, int offset, int amount) {
		EncounterArea area = new EncounterArea();
		area.setRate(1);
		for (int i = 0; i < amount; i++) {
			int pokemon = readWord(data, offset + i * 4);
			if (pokemon != 0) {
				Encounter enc = new Encounter();
				enc.setLevel(data[offset + 2 + i * 4]);
				enc.setMaxLevel(data[offset + 3 + i * 4]);
				enc.setSpecies(pokes[pokemon]);
				area.add(enc);
			}
		}
		return area;
	}

	private EncounterArea readBCCEncounterAreaHGSS(byte[] data, int offset, int amount) {
		EncounterArea area = new EncounterArea();
		area.setRate(1);
		for (int i = 0; i < amount; i++) {
			int pokemon = readWord(data, offset + i * 8);
			if (pokemon != 0) {
				Encounter enc = new Encounter();
				enc.setLevel(data[offset + 2 + i * 8]);
				enc.setMaxLevel(data[offset + 3 + i * 8]);
				enc.setSpecies(pokes[pokemon]);
				area.add(enc);
			}
		}
		return area;
	}

	private List<EncounterArea> readTimeBasedRodEncounterAreasHGSS(byte[] data, int offset, Species replacement,
																   int replacementIndex) {
		List<EncounterArea> encounterAreas = new ArrayList<>();
		List<Encounter> rodMorningDayEncounters = readSeaEncountersHGSS(data, offset, 5);
		EncounterArea rodMorningDayArea = new EncounterArea(rodMorningDayEncounters);
		encounterAreas.add(rodMorningDayArea);

		List<Encounter> rodNightEncounters = new ArrayList<>(rodMorningDayEncounters);
		Encounter replacedEncounter = cloneEncounterAndReplacePokemon(rodMorningDayEncounters.get(replacementIndex),
				replacement);
		rodNightEncounters.set(replacementIndex, replacedEncounter);
		EncounterArea rodNight = new EncounterArea(rodNightEncounters);
		encounterAreas.add(rodNight);

		return encounterAreas;
	}

	private Encounter cloneEncounterAndReplacePokemon(Encounter enc, Species pkmn) {
		Encounter clone = new Encounter();
		clone.setLevel(enc.getLevel());
		clone.setMaxLevel(enc.getMaxLevel());
		clone.setSpecies(pkmn);
		return clone;
	}

	@Override
	public List<EncounterArea> getSortedEncounters(boolean useTimeOfDay) {
		List<String> locationTagsTraverseOrder = romEntry.getRomType() == Gen4Constants.Type_HGSS ?
				Gen4Constants.locationTagsTraverseOrderHGSS : Gen4Constants.locationTagsTraverseOrderDPPt;
		return getEncounters(useTimeOfDay).stream()
				.sorted(Comparator.comparingInt(a -> locationTagsTraverseOrder.indexOf(a.getLocationTag())))
				.collect(Collectors.toList());
	}

	@Override
	public void setEncounters(boolean useTimeOfDay, List<EncounterArea> encounterAreas) {
		try {
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				setEncountersHGSS(useTimeOfDay, encounterAreas);
				updatePokedexAreaDataHGSS(encounterAreas);
			} else {
				setEncountersDPPt(useTimeOfDay, encounterAreas);
				updatePokedexAreaDataDPPt(encounterAreas);
			}
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}
	}

	private void setEncountersDPPt(boolean useTimeOfDay, List<EncounterArea> encounterAreas) throws IOException {
		Iterator<EncounterArea> areaIterator = encounterAreas.iterator();

		writeMainEncountersDPPt(areaIterator, useTimeOfDay);
		writeExtraEncountersDPPt(areaIterator);
	}

	private void writeMainEncountersDPPt(Iterator<EncounterArea> areaIterator, boolean useTimeOfDay) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);
		// Credit for
		// https://github.com/magical/pokemon-encounters/blob/master/nds/encounters-gen4-sinnoh.py
		// for the structure for this.
		for (byte[] b : encounterData.files) {
			int walkingRate = readLong(b, 0);
			if (walkingRate != 0) {
				// walking encounters are a-go
				EncounterArea walkingArea = areaIterator.next();
				writeEncountersDPPt(b, 4, walkingArea, 12);

				// Time of day encounters?
				int todEncounterSlot = 12;
				for (int i = 0; i < 4; i++) {
					int pknum = readLong(b, 108 + 4 * i);
					if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
						// Valid time of day slot
						if (useTimeOfDay) {
							// Get custom randomized encounter
							Species pk = walkingArea.get(todEncounterSlot++).getSpecies();
							writeLong(b, 108 + 4 * i, pk.getNumber());
						} else {
							// Copy the original slot's randomized encounter
							Species pk = walkingArea.get(Gen4Constants.dpptAlternateSlots[i + 2]).getSpecies();
							writeLong(b, 108 + 4 * i, pk.getNumber());
						}
					}
				}

				// Other conditional encounters?
				Iterator<Encounter> condEncounterIterator = null;
				for (int i = 0; i < 20; i++) {
					if (i >= 2 && i <= 5) {
						// Time of day slot, handled already
						continue;
					}
					int offs = 100 + i * 4 + (i >= 10 ? 24 : 0);
					int pknum = readLong(b, offs);
					if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
						// This slot is used, grab a replacement.
						if (condEncounterIterator == null) {
							// Fetch the set of conditional encounters for this
							// area now that we know it's necessary and exists.
							condEncounterIterator = areaIterator.next().iterator();
						}
						Species pk = condEncounterIterator.next().getSpecies();
						writeLong(b, offs, pk.getNumber());
					}
				}
			}
			// up to 204, 5 special ones to go
			// This is for surf, filler, old rod, good rod, super rod
			// so we skip index 1 (filler)
			int offset = 204;
			for (int i = 0; i < 5; i++) {
				int rate = readLong(b, offset);
				offset += 4;
				if (rate == 0 || i == 1) {
					offset += 40;
					continue;
				}

				EncounterArea seaArea = areaIterator.next();
				writeSeaEncountersDPPt(b, offset, seaArea);
				offset += 40;
			}
		}

		writeNARC(encountersFile, encounterData);
	}

	private void writeExtraEncountersDPPt(Iterator<EncounterArea> areaIterator) throws IOException {
		String extraEncountersFile = romEntry.getFile("ExtraEncounters");
		NARCArchive extraEncounterData = readNARC(extraEncountersFile);
		byte[] encounterOverlay = readOverlay(romEntry.getIntValue("EncounterOvlNumber"));

		writeFeebasEncounters(areaIterator, extraEncounterData, encounterOverlay);
		writeHoneyTreeEncounters(areaIterator, extraEncounterData, encounterOverlay);
		writeTrophyGardenRotatingEncounters(areaIterator, extraEncounterData);
		writeGreatMarshRotatingEncounters(areaIterator, extraEncounterData);

		// Save
		writeOverlay(romEntry.getIntValue("EncounterOvlNumber"), encounterOverlay);
		writeNARC(extraEncountersFile, extraEncounterData);
	}

	private void writeFeebasEncounters(Iterator<EncounterArea> areaIterator, NARCArchive extraEncounterData, byte[] encounterOverlay) {
		// Feebas tiles
		byte[] feebasData = extraEncounterData.files.get(0);
		EncounterArea feebasArea = areaIterator.next();
		int offset = find(encounterOverlay, Gen4Constants.feebasLevelPrefixDPPt);
		if (offset > 0) {
			offset += Gen4Constants.feebasLevelPrefixDPPt.length() / 2; // because it was a prefix
			encounterOverlay[offset] = (byte) feebasArea.get(0).getMaxLevel();
			encounterOverlay[offset + 4] = (byte) feebasArea.get(0).getLevel();
		}
		writeExtraEncountersDPPt(feebasData, 0, feebasArea);
	}

	private void writeHoneyTreeEncounters(Iterator<EncounterArea> areaIterator, NARCArchive extraEncounterData, byte[] encounterOverlay) {
		int[] honeyTreeOffsets = romEntry.getArrayValue("HoneyTreeOffsets");
		for (int honeyTreeOffset : honeyTreeOffsets) {
			byte[] honeyTreeData = extraEncounterData.files.get(honeyTreeOffset);
			EncounterArea honeyTreeArea = areaIterator.next();
			int offset = find(encounterOverlay, Gen4Constants.honeyTreeLevelPrefixDPPt);
			if (offset > 0) {
				offset += Gen4Constants.honeyTreeLevelPrefixDPPt.length() / 2; // because it was a prefix
				int level = honeyTreeArea.get(0).getLevel();
				int maxLevel = honeyTreeArea.get(0).getMaxLevel();

				// The original code makes it impossible for certain min levels
				// from being used in the assembly, but there's also a hardcoded
				// check for the original level range that we don't want. So we
				// can use that space to just do "mov r0, level", nop out the rest
				// of the check, then change "mov r0, r6, #5" to "mov r0, r0, r6".
				encounterOverlay[offset + 46] = (byte) level;
				encounterOverlay[offset + 47] = 0x20;
				encounterOverlay[offset + 48] = 0x00;
				encounterOverlay[offset + 49] = 0x00;
				encounterOverlay[offset + 50] = 0x00;
				encounterOverlay[offset + 51] = 0x00;
				encounterOverlay[offset + 52] = 0x00;
				encounterOverlay[offset + 53] = 0x00;
				encounterOverlay[offset + 54] = (byte) 0x80;
				encounterOverlay[offset + 55] = 0x19;

				encounterOverlay[offset + 102] = (byte) maxLevel;

				// In the above comment, r6 is a random number between 0 and
				// (maxLevel - level). To calculate this number, the game rolls
				// a random number between 0 and 0xFFFF and then divides it by
				// 0x1746; this produces values between 0 and 10, the original
				// level range. We need to replace the 0x1746 with our own
				// constant that has the same effect.
				int newRange = maxLevel - level;
				int divisor = (0xFFFF / (newRange + 1)) + 1;
				FileFunctions.writeFullInt(encounterOverlay, offset + 148, divisor);
			}
			writeExtraEncountersDPPt(honeyTreeData, 0, honeyTreeArea);
		}
	}

	private void writeTrophyGardenRotatingEncounters(Iterator<EncounterArea> areaIterator, NARCArchive extraEncounterData) {
		// Trophy Garden rotating Pokemon (Mr. Backlot)
		byte[] trophyGardenData = extraEncounterData.files.get(8);
		EncounterArea trophyGardenArea = areaIterator.next();
		writeExtraEncountersDPPt(trophyGardenData, 0, trophyGardenArea);
	}

	private void writeGreatMarshRotatingEncounters(Iterator<EncounterArea> areaIterator, NARCArchive extraEncounterData) {
		int[] greatMarshOffsets = new int[] { 9, 10 };
		for (int greatMarshOffset : greatMarshOffsets) {
			byte[] greatMarshData = extraEncounterData.files.get(greatMarshOffset);
			EncounterArea greatMarshRotatingArea = areaIterator.next();
			writeExtraEncountersDPPt(greatMarshData, 0, greatMarshRotatingArea);
		}
	}

	private void writeEncountersDPPt(byte[] data, int offset, List<Encounter> encounters, int encLength) {
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			writeLong(data, offset + i * 8, enc.getLevel());
			writeLong(data, offset + i * 8 + 4, enc.getSpecies().getNumber());
		}
	}

	private void writeSeaEncountersDPPt(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			writeLong(data, offset + i * 8, (enc.getLevel() << 8) + enc.getMaxLevel());
			writeLong(data, offset + i * 8 + 4, enc.getSpecies().getNumber());
		}
	}

	private void writeExtraEncountersDPPt(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			writeLong(data, offset + i * 4, enc.getSpecies().getNumber());
		}
	}

	private void setEncountersHGSS(boolean useTimeOfDay, List<EncounterArea> encounterAreas) throws IOException {
		Iterator<EncounterArea> areaIterator = encounterAreas.iterator();

		writeMainEncountersHGSS(useTimeOfDay, areaIterator);
		writeExtraEncountersHGSS(areaIterator);
	}

	private void writeMainEncountersHGSS(boolean useTimeOfDay, Iterator<EncounterArea> areaIterator) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);
		// Credit for
		// https://github.com/magical/pokemon-encounters/blob/master/nds/encounters-gen4-johto.py
		// for the structure for this.
		int[] amounts = new int[] { 0, 5, 2, 5, 5, 5 };
		for (byte[] b : encounterData.files) {
			int[] rates = new int[6];
			rates[0] = b[0] & 0xFF;
			rates[1] = b[1] & 0xFF;
			rates[2] = b[2] & 0xFF;
			rates[3] = b[3] & 0xFF;
			rates[4] = b[4] & 0xFF;
			rates[5] = b[5] & 0xFF;
			// Up to 20 after the rates & levels
			// Walking has to be handled on its own because the levels
			// are reused for every time of day
			if (rates[0] != 0) {
				EncounterArea walkingArea = areaIterator.next();
				writeWalkingEncounterLevelsHGSS(b, 8, walkingArea);
				writePokemonHGSS(b, 20, walkingArea);
				// either use the same area x3 for day, morning, night or get new ones for the latter 2
				if (!useTimeOfDay) {
					writePokemonHGSS(b, 44, walkingArea);
					writePokemonHGSS(b, 68, walkingArea);
				} else {
					for (int i = 1; i < 3; i++) {
						walkingArea = areaIterator.next();
						writePokemonHGSS(b, 20 + i * 24, walkingArea);
					}
				}
			}

			// Write radio pokemon
			writeOptionalEncountersHGSS(b, 92, 4, areaIterator);

			// Up to 100 now... 2*2*2 for radio pokemon
			// Write surf, rock smash, and rods
			int offset = 100;
			for (int i = 1; i < 6; i++) {
				if (rates[i] != 0) {
					// Valid area.
					EncounterArea seaArea = areaIterator.next();
					writeSeaEncountersHGSS(b, offset, seaArea);
				}
				offset += 4 * amounts[i];
			}

			// Write swarm pokemon
			writeOptionalEncountersHGSS(b, offset, 2, areaIterator);
			writeOptionalEncountersHGSS(b, offset + 4, 1, areaIterator);
			writeOptionalEncountersHGSS(b, offset + 6, 1, areaIterator);
		}

		// Save
		writeNARC(encountersFile, encounterData);
	}

	private void writeExtraEncountersHGSS(Iterator<EncounterArea> areaIterator) throws IOException {
		writeHeadbuttEncounters(areaIterator);
		writeBugCatchContestEncounters(areaIterator);
	}

	private void writeHeadbuttEncounters(Iterator<EncounterArea> areaIterator) throws IOException {
		// Write Headbutt encounters
		String headbuttEncountersFile = romEntry.getFile("HeadbuttPokemon");
		NARCArchive headbuttEncounterData = readNARC(headbuttEncountersFile);
		int c = -1;
		for (byte[] b : headbuttEncounterData.files) {
			c++;

			// In getEncountersHGSS, we ignored maps with no headbutt encounter data,
			// and we also ignored map 24 for being unused. We need to ignore them
			// here as well to keep encounters.next() in sync with the correct file.
			if (b.length == 4 || c == 24) {
				continue;
			}

			EncounterArea headbuttArea = areaIterator.next();
			writeHeadbuttEncountersHGSS(b, 4, headbuttArea);
		}

		// Save
		writeNARC(headbuttEncountersFile, headbuttEncounterData);
	}

	private void writeBugCatchContestEncounters(Iterator<EncounterArea> areaIterator) throws IOException {
		// Write Bug Catching Contest encounters
		String bccEncountersFile = romEntry.getFile("BCCWilds");
		byte[] bccEncountersData = readFile(bccEncountersFile);
		EncounterArea bccPreNationalDexArea = areaIterator.next();
		writeBCCEncountersHGSS(bccEncountersData, 0, bccPreNationalDexArea);
		EncounterArea bccPostNationalDexTuesArea = areaIterator.next();
		writeBCCEncountersHGSS(bccEncountersData, 80, bccPostNationalDexTuesArea);
		EncounterArea bccPostNationalDexThursArea = areaIterator.next();
		writeBCCEncountersHGSS(bccEncountersData, 160, bccPostNationalDexThursArea);
		EncounterArea bccPostNationalDexSatArea = areaIterator.next();
		writeBCCEncountersHGSS(bccEncountersData, 240, bccPostNationalDexSatArea);

		// Save
		writeFile(bccEncountersFile, bccEncountersData);
	}

	private void writeOptionalEncountersHGSS(byte[] data, int offset, int amount, Iterator<EncounterArea> areaIterator) {
		Iterator<Encounter> encounterIterator = null;
		for (int i = 0; i < amount; i++) {
			int origPokemon = readWord(data, offset + i * 2);
			if (origPokemon != 0) {
				// Need an encounter set, yes.
				if (encounterIterator == null) {
					encounterIterator = areaIterator.next().iterator();
				}
				Encounter here = encounterIterator.next();
				writeWord(data, offset + i * 2, here.getSpecies().getNumber());
			}
		}
	}

	private void writeWalkingEncounterLevelsHGSS(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			data[offset + i] = (byte) encounters.get(i).getLevel();
		}
	}

	private void writePokemonHGSS(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			writeWord(data, offset + i * 2, encounters.get(i).getSpecies().getNumber());
		}
	}

	private void writeSeaEncountersHGSS(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			data[offset + i * 4] = (byte) enc.getLevel();
			data[offset + i * 4 + 1] = (byte) enc.getMaxLevel();
			writeWord(data, offset + i * 4 + 2, enc.getSpecies().getNumber());
		}

	}

	private void writeHeadbuttEncountersHGSS(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			writeWord(data, offset + i * 4, enc.getSpecies().getNumber());
			data[offset + 2 + i * 4] = (byte) enc.getLevel();
			data[offset + 3 + i * 4] = (byte) enc.getMaxLevel();
		}
	}

	private void writeBCCEncountersHGSS(byte[] data, int offset, List<Encounter> encounters) {
		int encLength = encounters.size();
		for (int i = 0; i < encLength; i++) {
			Encounter enc = encounters.get(i);
			writeWord(data, offset + i * 8, enc.getSpecies().getNumber());
			data[offset + 2 + i * 8] = (byte) enc.getLevel();
			data[offset + 3 + i * 8] = (byte) enc.getMaxLevel();
		}
	}

	private List<Encounter> stitchEncsToLevels(Species[] species, int[] levels) {
		List<Encounter> encounters = new ArrayList<>();
		for (int i = 0; i < species.length; i++) {
			Encounter enc = new Encounter();
			enc.setLevel(levels[i]);
			enc.setSpecies(species[i]);
			encounters.add(enc);
		}
		return encounters;
	}

	private void loadWildMapNames() {
		try {
			wildMapNames = new HashMap<>();
			headbuttMapNames = new HashMap<>();
			byte[] internalNames = this.readFile(romEntry.getFile("MapTableFile"));
			int numMapHeaders = internalNames.length / 16;
			int baseMHOffset = romEntry.getIntValue("MapTableARM9Offset");
			List<String> allMapNames = getStrings(romEntry.getIntValue("MapNamesTextOffset"));
			int mapNameIndexSize = romEntry.getIntValue("MapTableNameIndexSize");
			for (int map = 0; map < numMapHeaders; map++) {
				int baseOffset = baseMHOffset + map * 24;
				int mapNameIndex = (mapNameIndexSize == 2) ? readWord(arm9, baseOffset + 18)
						: (arm9[baseOffset + 18] & 0xFF);
				String mapName = allMapNames.get(mapNameIndex);
				if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
					int wildSet = arm9[baseOffset] & 0xFF;
					if (wildSet != 255) {
						wildMapNames.put(wildSet, mapName);
					}
					headbuttMapNames.put(map, mapName);
				} else {
					int wildSet = readWord(arm9, baseOffset + 14);
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

	private void updatePokedexAreaDataDPPt(List<EncounterArea> encounterAreas) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);

		// Initialize empty area data
		Set[][] dungeonAreaData = new Set[Gen4Constants.pokemonCount + 1][3];
		Set[] dungeonSpecialPreNationalData = new Set[Gen4Constants.pokemonCount + 1];
		Set[] dungeonSpecialPostNationalData = new Set[Gen4Constants.pokemonCount + 1];
		Set[][] overworldAreaData = new Set[Gen4Constants.pokemonCount + 1][3];
		Set[] overworldSpecialPreNationalData = new Set[Gen4Constants.pokemonCount + 1];
		Set[] overworldSpecialPostNationalData = new Set[Gen4Constants.pokemonCount + 1];

		for (int pk = 1; pk <= Gen4Constants.pokemonCount; pk++) {
			for (int time = 0; time < 3; time++) {
				dungeonAreaData[pk][time] = new TreeSet<>();
				overworldAreaData[pk][time] = new TreeSet<>();
			}
			dungeonSpecialPreNationalData[pk] = new TreeSet<>();
			dungeonSpecialPostNationalData[pk] = new TreeSet<>();
			overworldSpecialPreNationalData[pk] = new TreeSet<>();
			overworldSpecialPostNationalData[pk] = new TreeSet<>();
		}

		for (int c = 0; c < encounterData.files.size(); c++) {
			Set<Integer>[][] target;
			Set<Integer>[] specialTarget;
			int index;
			if (Gen4Constants.dpptOverworldDexMaps[c] != -1) {
				target = overworldAreaData;
				specialTarget = overworldSpecialPostNationalData;
				index = Gen4Constants.dpptOverworldDexMaps[c];
			} else if (Gen4Constants.dpptDungeonDexMaps[c] != -1) {
				target = dungeonAreaData;
				specialTarget = dungeonSpecialPostNationalData;
				index = Gen4Constants.dpptDungeonDexMaps[c];
			} else {
				continue;
			}

			byte[] b = encounterData.files.get(c);

			int grassRate = readLong(b, 0);
			if (grassRate != 0) {
				// up to 4
				List<Encounter> grassEncounters = readEncountersDPPt(b, 4, 12);

				for (int i = 0; i < 12; i++) {
					int pknum = grassEncounters.get(i).getSpecies().getNumber();
					if (i == 2 || i == 3) {
						// morning only - time of day data for day/night for
						// these slots
						target[pknum][0].add(index);
					} else {
						// all times of day
						target[pknum][0].add(index);
						target[pknum][1].add(index);
						target[pknum][2].add(index);
					}
				}

				// time of day data for slots 2 and 3 day/night
				for (int i = 0; i < 4; i++) {
					int pknum = readLong(b, 108 + 4 * i);
					if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
						target[pknum][i > 1 ? 2 : 1].add(index);
					}
				}

				// For Swarm/Radar/GBA encounters, only Poke Radar encounters appear in the dex
				for (int i = 6; i < 10; i++) {
					int offs = 100 + i * 4;
					int pknum = readLong(b, offs);
					if (pknum >= 1 && pknum <= Gen4Constants.pokemonCount) {
						specialTarget[pknum].add(index);
					}
				}
			}

			// up to 204, 5 sets of "sea" encounters to go
			int offset = 204;
			for (int i = 0; i < 5; i++) {
				int rate = readLong(b, offset);
				offset += 4;
				List<Encounter> seaEncounters = readSeaEncountersDPPt(b, offset, 5);
				offset += 40;
				if (rate == 0 || i == 1) {
					continue;
				}
				for (Encounter enc : seaEncounters) {
					target[enc.getSpecies().getNumber()][0].add(index);
					target[enc.getSpecies().getNumber()][1].add(index);
					target[enc.getSpecies().getNumber()][2].add(index);
				}
			}
		}

		// Handle the "special" encounters that aren't in the encounter GARC
		for (EncounterArea area : encounterAreas) {
			if (area.getDisplayName().contains("Mt. Coronet Feebas Tiles")) {
				for (Encounter enc : area) {
					dungeonSpecialPreNationalData[enc.getSpecies().getNumber()].add(Gen4Constants.dpptMtCoronetDexIndex);
					dungeonSpecialPostNationalData[enc.getSpecies().getNumber()].add(Gen4Constants.dpptMtCoronetDexIndex);
				}
			} else if (area.getDisplayName().contains("Honey Tree Group 1") || area.getDisplayName().contains("Honey Tree Group 2")) {
				for (Encounter enc : area) {
					dungeonSpecialPreNationalData[enc.getSpecies().getNumber()]
							.add(Gen4Constants.dpptFloaromaMeadowDexIndex);
					dungeonSpecialPostNationalData[enc.getSpecies().getNumber()]
							.add(Gen4Constants.dpptFloaromaMeadowDexIndex);
					overworldSpecialPreNationalData[enc.getSpecies().getNumber()]
							.addAll(Gen4Constants.dpptOverworldHoneyTreeDexIndicies);
					overworldSpecialPostNationalData[enc.getSpecies().getNumber()]
							.addAll(Gen4Constants.dpptOverworldHoneyTreeDexIndicies);
				}
			} else if (area.getDisplayName().contains("Trophy Garden Rotating Pokemon")) {
				for (Encounter enc : area) {
					dungeonSpecialPostNationalData[enc.getSpecies().getNumber()].add(Gen4Constants.dpptTrophyGardenDexIndex);
				}
			} else if (area.getDisplayName().contains("Great Marsh Rotating Pokemon (Post-National Dex)")) {
				for (Encounter enc : area) {
					dungeonSpecialPostNationalData[enc.getSpecies().getNumber()].add(Gen4Constants.dpptGreatMarshDexIndex);
				}
			} else if (area.getDisplayName().contains("Great Marsh Rotating Pokemon (Pre-National Dex)")) {
				for (Encounter enc : area) {
					dungeonSpecialPreNationalData[enc.getSpecies().getNumber()].add(Gen4Constants.dpptGreatMarshDexIndex);
				}
			}
		}

		// Write new area data to its file
		// Area data format credit to Ganix
		String pokedexAreaDataFile = romEntry.getFile("PokedexAreaData");
		NARCArchive pokedexAreaData = readNARC(pokedexAreaDataFile);
		int dungeonDataIndex = romEntry.getIntValue("PokedexAreaDataDungeonIndex");
		int dungeonSpecialPreNationalDataIndex = romEntry.getIntValue("PokedexAreaDataDungeonSpecialPreNationalIndex");
		int dungeonSpecialPostNationalDataIndex = romEntry.getIntValue("PokedexAreaDataDungeonSpecialPostNationalIndex");
		int overworldDataIndex = romEntry.getIntValue("PokedexAreaDataOverworldIndex");
		int overworldSpecialPreNationalDataIndex = romEntry.getIntValue("PokedexAreaDataOverworldSpecialPreNationalIndex");
		int overworldSpecialPostNationalDataIndex = romEntry.getIntValue("PokedexAreaDataOverworldSpecialPostNationalIndex");
		for (int pk = 1; pk <= Gen4Constants.pokemonCount; pk++) {
			for (int time = 0; time < 3; time++) {
				pokedexAreaData.files.set(dungeonDataIndex + pk + time * Gen4Constants.pokedexAreaDataSize,
						makePokedexAreaDataFile(dungeonAreaData[pk][time]));
				pokedexAreaData.files.set(overworldDataIndex + pk + time * Gen4Constants.pokedexAreaDataSize,
						makePokedexAreaDataFile(overworldAreaData[pk][time]));
			}
			pokedexAreaData.files.set(dungeonSpecialPreNationalDataIndex + pk,
					makePokedexAreaDataFile(dungeonSpecialPreNationalData[pk]));
			pokedexAreaData.files.set(dungeonSpecialPostNationalDataIndex + pk,
					makePokedexAreaDataFile(dungeonSpecialPostNationalData[pk]));
			pokedexAreaData.files.set(overworldSpecialPreNationalDataIndex + pk,
					makePokedexAreaDataFile(overworldSpecialPreNationalData[pk]));
			pokedexAreaData.files.set(overworldSpecialPostNationalDataIndex + pk,
					makePokedexAreaDataFile(overworldSpecialPostNationalData[pk]));
		}
		writeNARC(pokedexAreaDataFile, pokedexAreaData);
	}

	private void updatePokedexAreaDataHGSS(List<EncounterArea> encounterAreas) throws IOException {
		String encountersFile = romEntry.getFile("WildPokemon");
		NARCArchive encounterData = readNARC(encountersFile);

		// Initialize empty area data
		Set[][] dungeonAreaData = new Set[Gen4Constants.pokemonCount + 1][3];
		Set[][] overworldAreaData = new Set[Gen4Constants.pokemonCount + 1][3];
		Set[] dungeonSpecialData = new Set[Gen4Constants.pokemonCount + 1];
		Set[] overworldSpecialData = new Set[Gen4Constants.pokemonCount + 1];

		for (int pk = 1; pk <= Gen4Constants.pokemonCount; pk++) {
			for (int time = 0; time < 3; time++) {
				dungeonAreaData[pk][time] = new TreeSet<>();
				overworldAreaData[pk][time] = new TreeSet<>();
			}
			dungeonSpecialData[pk] = new TreeSet<>();
			overworldSpecialData[pk] = new TreeSet<>();
		}

		for (int c = 0; c < encounterData.files.size(); c++) {
			Set<Integer>[][] target;
			Set<Integer>[] specialTarget;
			int index;
			if (Gen4Constants.hgssOverworldDexMaps[c] != -1) {
				target = overworldAreaData;
				specialTarget = overworldSpecialData;
				index = Gen4Constants.hgssOverworldDexMaps[c];
			} else if (Gen4Constants.hgssDungeonDexMaps[c] != -1) {
				target = dungeonAreaData;
				specialTarget = dungeonSpecialData;
				index = Gen4Constants.hgssDungeonDexMaps[c];
			} else {
				continue;
			}

			byte[] b = encounterData.files.get(c);
			int[] amounts = new int[] { 0, 5, 2, 5, 5, 5 };
			int[] rates = new int[6];
			rates[0] = b[0] & 0xFF;
			rates[1] = b[1] & 0xFF;
			rates[2] = b[2] & 0xFF;
			rates[3] = b[3] & 0xFF;
			rates[4] = b[4] & 0xFF;
			rates[5] = b[5] & 0xFF;
			// Up to 20 now (12 for levels)
			if (rates[0] != 0) {
				for (int time = 0; time < 3; time++) {
					Species[] pokes = readPokemonHGSS(b, 20 + time * 24, 12);
					for (Species pk : pokes) {
						target[pk.getNumber()][time].add(index);
					}
				}
			}

			// Hoenn/Sinnoh Radio
			EncounterArea radioArea = readOptionalEncounterAreaHGSS(b, 92, 4);
			for (Encounter enc : radioArea) {
				specialTarget[enc.getSpecies().getNumber()].add(index);
			}

			// Up to 100 now... 2*2*2 for radio pokemon
			// Handle surf, rock smash, and old rod
			int offset = 100;
			for (int i = 1; i < 4; i++) {
				List<Encounter> seaEncounters = readSeaEncountersHGSS(b, offset, amounts[i]);
				offset += 4 * amounts[i];
				if (rates[i] != 0) {
					// Valid area.
					for (Encounter enc : seaEncounters) {
						target[enc.getSpecies().getNumber()][0].add(index);
						target[enc.getSpecies().getNumber()][1].add(index);
						target[enc.getSpecies().getNumber()][2].add(index);
					}
				}
			}

			// Handle good and super rod, because they can get an encounter slot replaced by
			// the night fishing replacement
			Species nightFishingReplacement = pokes[readWord(b, 192)];
			if (rates[4] != 0) {
				List<EncounterArea> goodRodAreas = readTimeBasedRodEncounterAreasHGSS(b, offset,
						nightFishingReplacement, Gen4Constants.hgssGoodRodReplacementIndex);
				for (Encounter enc : goodRodAreas.get(0)) {
					target[enc.getSpecies().getNumber()][0].add(index);
					target[enc.getSpecies().getNumber()][1].add(index);
				}
				for (Encounter enc : goodRodAreas.get(1)) {
					target[enc.getSpecies().getNumber()][2].add(index);
				}
			}
			if (rates[5] != 0) {
				List<EncounterArea> superRodAreas = readTimeBasedRodEncounterAreasHGSS(b, offset + 20,
						nightFishingReplacement, Gen4Constants.hgssSuperRodReplacementIndex);
				for (Encounter enc : superRodAreas.get(0)) {
					target[enc.getSpecies().getNumber()][0].add(index);
					target[enc.getSpecies().getNumber()][1].add(index);
				}
				for (Encounter enc : superRodAreas.get(1)) {
					target[enc.getSpecies().getNumber()][2].add(index);
				}
			}
		}

		// Handle headbutt encounters too (only doing it like this because reading the
		// encounters from the ROM is really annoying)
		EncounterArea firstHeadbuttArea = encounterAreas.stream()
				.filter(es -> es.getDisplayName().contains("Route 1 Headbutt")).findFirst().orElse(null);
		int startingHeadbuttOffset = encounterAreas.indexOf(firstHeadbuttArea);
		if (startingHeadbuttOffset != -1) {
			for (int i = 0; i < Gen4Constants.hgssHeadbuttOverworldDexMaps.length; i++) {
				EncounterArea area = encounterAreas.get(startingHeadbuttOffset + i);
				for (Encounter enc : area) {
					if (Gen4Constants.hgssHeadbuttOverworldDexMaps[i] != -1) {
						overworldSpecialData[enc.getSpecies().getNumber()]
								.add(Gen4Constants.hgssHeadbuttOverworldDexMaps[i]);
					} else if (Gen4Constants.hgssHeadbuttDungeonDexMaps[i] != -1) {
						dungeonSpecialData[enc.getSpecies().getNumber()].add(Gen4Constants.hgssHeadbuttDungeonDexMaps[i]);
					}
				}
			}
		}

		// Write new area data to its file
		// Area data format credit to Ganix
		String pokedexAreaDataFile = romEntry.getFile("PokedexAreaData");
		NARCArchive pokedexAreaData = readNARC(pokedexAreaDataFile);
		int dungeonDataIndex = romEntry.getIntValue("PokedexAreaDataDungeonIndex");
		int overworldDataIndex = romEntry.getIntValue("PokedexAreaDataOverworldIndex");
		int dungeonSpecialIndex = romEntry.getIntValue("PokedexAreaDataDungeonSpecialIndex");
		int overworldSpecialDataIndex = romEntry.getIntValue("PokedexAreaDataOverworldSpecialIndex");
		for (int pk = 1; pk <= Gen4Constants.pokemonCount; pk++) {
			for (int time = 0; time < 3; time++) {
				pokedexAreaData.files.set(dungeonDataIndex + pk + time * Gen4Constants.pokedexAreaDataSize,
						makePokedexAreaDataFile(dungeonAreaData[pk][time]));
				pokedexAreaData.files.set(overworldDataIndex + pk + time * Gen4Constants.pokedexAreaDataSize,
						makePokedexAreaDataFile(overworldAreaData[pk][time]));
			}
			pokedexAreaData.files.set(dungeonSpecialIndex + pk, makePokedexAreaDataFile(dungeonSpecialData[pk]));
			pokedexAreaData.files.set(overworldSpecialDataIndex + pk,
					makePokedexAreaDataFile(overworldSpecialData[pk]));
		}
		writeNARC(pokedexAreaDataFile, pokedexAreaData);
	}

	private byte[] makePokedexAreaDataFile(Set<Integer> data) {
		byte[] output = new byte[data.size() * 4 + 4];
		int idx = 0;
		for (Integer obj : data) {
			int areaIndex = obj;
			this.writeLong(output, idx, areaIndex);
			idx += 4;
		}
		return output;
	}

	@Override
	public List<Trainer> getTrainers() {
		List<Trainer> allTrainers = new ArrayList<>();
		try {
			NARCArchive trainers = this.readNARC(romEntry.getFile("TrainerData"));
			NARCArchive trpokes = this.readNARC(romEntry.getFile("TrainerPokemon"));
			List<String> tclasses = this.getTrainerClassNames();
			List<String> tnames = this.getTrainerNames();
			int trainernum = trainers.files.size();
			for (int i = 1; i < trainernum; i++) {
				// Trainer entries are 20 bytes
				// Team flags; 1 byte; 0x01 = custom moves, 0x02 = held item
				// Class; 1 byte
				// 1 byte not used
				// Number of pokemon in team; 1 byte
				// Items; 2 bytes each, 4 item slots
				// AI Flags; 2 byte
				// 2 bytes not used
				// Battle Mode; 1 byte; 0 means single, 1 means double.
				// 3 bytes not used
				byte[] trainer = trainers.files.get(i);
				byte[] trpoke = trpokes.files.get(i);
				Trainer tr = new Trainer();
				tr.poketype = trainer[0] & 0xFF;
				tr.trainerclass = trainer[1] & 0xFF;
				tr.index = i;
				int numPokes = trainer[3] & 0xFF;
				int pokeOffs = 0;
				tr.fullDisplayName = tclasses.get(tr.trainerclass) + " " + tnames.get(i - 1);
				for (int poke = 0; poke < numPokes; poke++) {
					// Structure is
					// IV SB LV LV SP SP FRM FRM
					// (HI HI)
					// (M1 M1 M2 M2 M3 M3 M4 M4)
					// where SB = 0 0 Ab Ab 0 0 G G
					// IV is a "difficulty" level between 0 and 255 to represent 0 to 31 IVs.
					// These IVs affect all attributes. For the vanilla games, the
					// vast majority of trainers have 0 IVs; Elite Four members will
					// have 30 IVs.
					// Ab Ab = ability number, 0 for first ability, 2 for second [HGSS only]
					// G G affect the gender somehow. 0 appears to mean "most common
					// gender for the species".
					int difficulty = trpoke[pokeOffs] & 0xFF;
					int level = trpoke[pokeOffs + 2] & 0xFF;
					int species = (trpoke[pokeOffs + 4] & 0xFF) + ((trpoke[pokeOffs + 5] & 0x01) << 8);
					int formnum = (trpoke[pokeOffs + 5] >> 2);
					TrainerPokemon tpk = new TrainerPokemon();
					tpk.level = level;
					tpk.species = pokes[species];
					tpk.IVs = (difficulty * 31) / 255;
					int abilitySlot = (trpoke[pokeOffs + 1] >>> 4) & 0xF;
					if (abilitySlot == 0) {
						// All Gen 4 games represent the first ability as ability 0.
						abilitySlot = 1;
					}
					tpk.abilitySlot = abilitySlot;
					tpk.forme = formnum;
					tpk.formeSuffix = Gen4Constants.getFormeSuffixByBaseForme(species, formnum);
					pokeOffs += 6;
					if (tr.pokemonHaveItems()) {
						tpk.heldItem = readWord(trpoke, pokeOffs);
						pokeOffs += 2;
					}
					if (tr.pokemonHaveCustomMoves()) {
						for (int move = 0; move < 4; move++) {
							tpk.moves[move] = readWord(trpoke, pokeOffs + (move * 2));
						}
						pokeOffs += 8;
					}
					// Plat/HGSS have another random pokeOffs +=2 here.
					if (romEntry.getRomType() != Gen4Constants.Type_DP) {
						pokeOffs += 2;
					}
					tr.pokemon.add(tpk);
				}
				allTrainers.add(tr);
			}
			if (romEntry.getRomType() == Gen4Constants.Type_DP) {
				Gen4Constants.tagTrainersDP(allTrainers);
				Gen4Constants.setMultiBattleStatusDP(allTrainers);
			} else if (romEntry.getRomType() == Gen4Constants.Type_Plat) {
				Gen4Constants.tagTrainersPt(allTrainers);
				Gen4Constants.setMultiBattleStatusPt(allTrainers);
			} else {
				Gen4Constants.tagTrainersHGSS(allTrainers);
				Gen4Constants.setMultiBattleStatusHGSS(allTrainers);
			}
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}
		return allTrainers;
	}

	@Override
	public List<Integer> getMainPlaythroughTrainers() {
		return new ArrayList<>(); // Not implemented
	}

	@Override
	public List<Integer> getEliteFourTrainers(boolean isChallengeMode) {
		return Arrays.stream(romEntry.getArrayValue("EliteFourIndices")).boxed().collect(Collectors.toList());
	}

	@Override
    public Map<String, Type> getGymAndEliteTypeThemes() {
		switch (romEntry.getRomType()) {
			case Gen4Constants.Type_DP:
				return Gen4Constants.gymAndEliteThemesDP;
			case Gen4Constants.Type_Plat:
				return Gen4Constants.gymAndEliteThemesPt;
			case Gen4Constants.Type_HGSS:
				return Gen4Constants.gymAndEliteThemesHGSS;
			default:
				return null;
		}
    }

    @Override
	public List<Integer> getEvolutionItems() {
		return Gen4Constants.evolutionItems;
	}

	@Override
	public void setTrainers(List<Trainer> trainerData) {
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			fixAbilitySlotValuesForHGSS(trainerData);
		}
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
				// preserve original poketype
				trainer[0] = (byte) tr.poketype;
				int numPokes = tr.pokemon.size();
				trainer[3] = (byte) numPokes;

				if (tr.forcedDoubleBattle) {
					// If we set this flag for partner trainers (e.g., Cheryl), then the double wild
					// battles will turn into trainer battles with glitchy trainers.
					boolean excludedPartnerTrainer = romEntry.getRomType() != Gen4Constants.Type_HGSS
							&& Gen4Constants.partnerTrainerIndices.contains(tr.index);
					if (trainer[16] == 0 && !excludedPartnerTrainer) {
						trainer[16] |= 3;
					}
				}

				int bytesNeeded = 6 * numPokes;
				if (romEntry.getRomType() != Gen4Constants.Type_DP) {
					bytesNeeded += 2 * numPokes;
				}
				if (tr.pokemonHaveCustomMoves()) {
					bytesNeeded += 8 * numPokes; // 2 bytes * 4 moves
				}
				if (tr.pokemonHaveItems()) {
					bytesNeeded += 2 * numPokes;
				}
				byte[] trpoke = new byte[bytesNeeded];
				int pokeOffs = 0;
				Iterator<TrainerPokemon> tpokes = tr.pokemon.iterator();
				for (int poke = 0; poke < numPokes; poke++) {
					TrainerPokemon tp = tpokes.next();
					int ability = tp.abilitySlot << 4;
					if (tp.abilitySlot == 1) {
						// All Gen 4 games represent the first ability as ability 0.
						ability = 0;
					}
					// Add 1 to offset integer division truncation
					int difficulty = Math.min(255, 1 + (tp.IVs * 255) / 31);
					writeWord(trpoke, pokeOffs, difficulty | ability << 8);
					writeWord(trpoke, pokeOffs + 2, tp.level);
					writeWord(trpoke, pokeOffs + 4, tp.species.getNumber());
					trpoke[pokeOffs + 5] |= (byte) (tp.forme << 2);
					pokeOffs += 6;
					if (tr.pokemonHaveItems()) {
						writeWord(trpoke, pokeOffs, tp.heldItem);
						pokeOffs += 2;
					}
					if (tr.pokemonHaveCustomMoves()) {
						if (tp.resetMoves) {
							int[] pokeMoves = RomFunctions.getMovesAtLevel(
									getAltFormeOfSpecies(tp.species, tp.forme).getNumber(), movesets, tp.level);
							for (int m = 0; m < 4; m++) {
								writeWord(trpoke, pokeOffs + m * 2, pokeMoves[m]);
							}
						} else {
							writeWord(trpoke, pokeOffs, tp.moves[0]);
							writeWord(trpoke, pokeOffs + 2, tp.moves[1]);
							writeWord(trpoke, pokeOffs + 4, tp.moves[2]);
							writeWord(trpoke, pokeOffs + 6, tp.moves[3]);
						}
						pokeOffs += 8;
					}
					// Plat/HGSS have another random pokeOffs +=2 here.
					if (romEntry.getRomType() != Gen4Constants.Type_DP) {
						pokeOffs += 2;
					}
				}
				trpokes.files.add(trpoke);
			}
			this.writeNARC(romEntry.getFile("TrainerData"), trainers);
			this.writeNARC(romEntry.getFile("TrainerPokemon"), trpokes);
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}
	}

	@Override
	public void makeDoubleBattleModePossible() {
		// In Gen 4, the game prioritizes showing the special double battle intro over almost any
		// other kind of intro. Since the trainer music is tied to the intro, this results in the
		// vast majority of "special" trainers losing their intro and music in double battle mode.
		// To fix this, the below code patches the executable to skip the case for the special
		// double battle intro (by changing a beq to an unconditional branch); this slightly breaks
		// battles that are double battles in the original game, but the trade-off is worth it.

		// Then, also patch various subroutines that control the "Trainer Eye" event and text boxes
		// related to this in order to make double battles work on all trainers
		try {
			String doubleBattleFixPrefix = Gen4Constants.getDoubleBattleFixPrefix(romEntry.getRomType());
			int offset = find(arm9, doubleBattleFixPrefix);
			if (offset > 0) {
				offset += doubleBattleFixPrefix.length() / 2; // because it was a prefix
				arm9[offset] = (byte) 0xE0;
			} else {
				throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
			}

			String doubleBattleFlagReturnPrefix = romEntry.getStringValue("DoubleBattleFlagReturnPrefix");
			String doubleBattleWalkingPrefix1 = romEntry.getStringValue("DoubleBattleWalkingPrefix1");
			String doubleBattleWalkingPrefix2 = romEntry.getStringValue("DoubleBattleWalkingPrefix2");
			String doubleBattleTextBoxPrefix = romEntry.getStringValue("DoubleBattleTextBoxPrefix");

			// After getting the double battle flag, return immediately instead of
			// converting it to a 1 for
			// non-zero values/0 for zero
			offset = find(arm9, doubleBattleFlagReturnPrefix);
			if (offset > 0) {
				offset += doubleBattleFlagReturnPrefix.length() / 2; // because it was a prefix
				writeWord(arm9, offset, 0xBD08);
			} else {
				throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
			}

			// Instead of doing "double trainer walk" for nonzero values, do it only for
			// value == 2
			offset = find(arm9, doubleBattleWalkingPrefix1);
			if (offset > 0) {
				offset += doubleBattleWalkingPrefix1.length() / 2; // because it was a prefix
				arm9[offset] = (byte) 0x2; // cmp r0, #0x2
				arm9[offset + 3] = (byte) 0xD0; // beq DOUBLE_TRAINER_WALK
			} else {
				throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
			}

			// Instead of checking if the value was exactly 1 after checking that it was
			// nonzero, check that it's
			// 2 again lol
			offset = find(arm9, doubleBattleWalkingPrefix2);
			if (offset > 0) {
				offset += doubleBattleWalkingPrefix2.length() / 2; // because it was a prefix
				arm9[offset] = (byte) 0x2;
			} else {
				throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
			}

			// Once again, compare a value to 2 instead of just checking that it's nonzero
			offset = find(arm9, doubleBattleTextBoxPrefix);
			if (offset > 0) {
				offset += doubleBattleTextBoxPrefix.length() / 2; // because it was a prefix
				writeWord(arm9, offset, 0x46C0);
				writeWord(arm9, offset + 2, 0x2802);
				arm9[offset + 5] = (byte) 0xD0;
			} else {
				throw new OperationNotSupportedException("Double Battle Mode not supported for this game");
			}

			// This NARC has some data that controls how text boxes are handled at the end
			// of a trainer battle.
			// Changing this byte from 4 -> 0 makes it check if the "double battle" flag is
			// exactly 2 instead of
			// checking "flag & 2", which makes the single trainer double battles use the
			// single battle
			// handling (since we set their flag to 3 instead of 2)
			NARCArchive battleSkillSubSeq = readNARC(romEntry.getFile("BattleSkillSubSeq"));
			byte[] trainerEndFile = battleSkillSubSeq.files.get(romEntry.getIntValue("TrainerEndFileNumber"));
			trainerEndFile[romEntry.getIntValue("TrainerEndTextBoxOffset")] = 0;
			writeNARC(romEntry.getFile("BattleSkillSubSeq"), battleSkillSubSeq);
		} catch (IOException e) {
			throw new RomIOException(e);
		} catch (OperationNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	// Note: This method is here to avoid bloating AbstractRomHandler with
	// special-case logic.
	// It only works here because nothing in AbstractRomHandler cares about the
	// abilitySlot at
	// the moment; if that changes, then this should be moved there instead.
	private void fixAbilitySlotValuesForHGSS(List<Trainer> trainers) {
		for (Trainer tr : trainers) {
			if (!tr.pokemon.isEmpty()) {
				TrainerPokemon lastPokemon = tr.pokemon.get(tr.pokemon.size() - 1);
				int lastAbilitySlot = lastPokemon.abilitySlot;
				for (int i = 0; i < tr.pokemon.size(); i++) {
					// HGSS has a nasty bug where if a single Pokemon with an abilitySlot of 2
					// appears on the trainer's team, then all Pokemon that appear after it in
					// the trpoke data will *also* use their second ability in-game, regardless
					// of what their abilitySlot is set to. This can mess with the rival's
					// starter carrying forward their ability, and can also cause sensible items
					// to behave incorrectly. To fix this, we just make sure everything on a
					// Trainer's team uses the same abilitySlot. The choice to copy the last
					// Pokemon's abilitySlot is arbitrary, but allows us to avoid any special-
					// casing involving the rival's starter, since it always appears last.
					tr.pokemon.get(i).abilitySlot = lastAbilitySlot;
				}
			}
		}
	}
	

    @Override
    public SpeciesSet getBannedForWildEncounters() {
        // Ban Unown in DPPt because you can't get certain letters outside of Solaceon Ruins.
        // Ban Unown in HGSS because they don't show up unless you complete a puzzle in the Ruins of Alph.
        return new SpeciesSet(Collections.singletonList(pokes[SpeciesIDs.unown]));
    }

	@Override
	public SpeciesSet getBannedFormesForTrainerPokemon() {
		SpeciesSet banned = new SpeciesSet();
		if (romEntry.getRomType() != Gen4Constants.Type_DP) {
			Species giratinaOrigin = this.getAltFormeOfSpecies(pokes[SpeciesIDs.giratina], 1);
			if (giratinaOrigin != null) {
				// Ban Giratina-O for trainers in Gen 4, since he just instantly transforms
				// back to Altered Forme if he's not holding the Griseous Orb.
				banned.add(giratinaOrigin);
			}
		}
		return banned;
	}

	@Override
	public Map<Integer, List<MoveLearnt>> getMovesLearnt() {
		Map<Integer, List<MoveLearnt>> movesets = new TreeMap<>();
		try {
			NARCArchive movesLearnt = this.readNARC(romEntry.getFile("PokemonMovesets"));
			int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
			for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
				Species pkmn = pokes[i];
				byte[] rom;
				if (i > Gen4Constants.pokemonCount) {
					rom = movesLearnt.files.get(i + Gen4Constants.formeOffset);
				} else {
					rom = movesLearnt.files.get(i);
				}
				int moveDataLoc = 0;
				List<MoveLearnt> learnt = new ArrayList<>();
				while ((rom[moveDataLoc] & 0xFF) != 0xFF || (rom[moveDataLoc + 1] & 0xFF) != 0xFF) {
					int move = (rom[moveDataLoc] & 0xFF);
					int level = (rom[moveDataLoc + 1] & 0xFE) >> 1;
					if ((rom[moveDataLoc + 1] & 0x01) == 0x01) {
						move += 256;
					}
					MoveLearnt ml = new MoveLearnt();
					ml.level = level;
					ml.move = move;
					learnt.add(ml);
					moveDataLoc += 2;
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
		// int[] extraLearnSets = new int[] { 7, 13, 13 };
		// Build up a new NARC
		NARCArchive movesLearnt = new NARCArchive();
		// The blank moveset
		byte[] blankSet = new byte[] { (byte) 0xFF, (byte) 0xFF, 0, 0 };
		movesLearnt.files.add(blankSet);
		int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
		for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
			if (i == Gen4Constants.pokemonCount + 1) {
				for (int j = 0; j < Gen4Constants.formeOffset; j++) {
					movesLearnt.files.add(blankSet);
				}
			}
			Species pkmn = pokes[i];
			List<MoveLearnt> learnt = movesets.get(pkmn.getNumber());
			int sizeNeeded = learnt.size() * 2 + 2;
			if ((sizeNeeded % 4) != 0) {
				sizeNeeded += 2;
			}
			byte[] moveset = new byte[sizeNeeded];
			int j = 0;
			for (; j < learnt.size(); j++) {
				MoveLearnt ml = learnt.get(j);
				moveset[j * 2] = (byte) (ml.move & 0xFF);
				int levelPart = (ml.level << 1) & 0xFE;
				if (ml.move > 255) {
					levelPart++;
				}
				moveset[j * 2 + 1] = (byte) levelPart;
			}
			moveset[j * 2] = (byte) 0xFF;
			moveset[j * 2 + 1] = (byte) 0xFF;
			movesLearnt.files.add(moveset);
		}
		// for (int j = 0; j < extraLearnSets[romEntry.getRomType()]; j++) {
		// movesLearnt.files.add(blankSet);
		// }
		// Save
		try {
			this.writeNARC(romEntry.getFile("PokemonMovesets"), movesLearnt);
		} catch (IOException e) {
			throw new RomIOException(e);
		}

	}

	@Override
	public Map<Integer, List<Integer>> getEggMoves() {
		Map<Integer, List<Integer>> eggMoves = new TreeMap<>();
		try {
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				NARCArchive eggMoveNARC = this.readNARC(romEntry.getFile("EggMoves"));
				byte[] eggMoveData = eggMoveNARC.files.get(0);
				eggMoves = readEggMoves(eggMoveData, 0);
			} else {
				byte[] fieldOvl = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
				int offset = find(fieldOvl, Gen4Constants.dpptEggMoveTablePrefix);
				if (offset > 0) {
					offset += Gen4Constants.dpptEggMoveTablePrefix.length() / 2; // because it was a prefix
					eggMoves = readEggMoves(fieldOvl, offset);
				}
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}

		return eggMoves;
	}

	/**
	 * Returns a Set of move IDs of all {@link Move}s a {@link Species} could possibly have through egg move breeding.
	 * I.e., all the Pokemon's and all of its prevos' egg moves.
	 */
	private Set<Integer> getEffectiveEggMoves(Species pk) {
		Map<Integer, List<Integer>> allEggMoves = getEggMoves();
		Set<Integer> eggMoves = new HashSet<>();

		Stack<Species> stack = new Stack<>();
		List<Species> visited = new ArrayList<>();
		stack.add(pk);
		while (!stack.isEmpty()) {
			Species curr = stack.pop();
			visited.add(curr);
			for (Evolution evo : curr.getEvolutionsTo()) {
				if (!visited.contains(evo.getFrom())) {
					stack.push(evo.getFrom());
				}
			}
			if (allEggMoves.containsKey(curr.getNumber())) {
				eggMoves.addAll(allEggMoves.get(curr.getNumber()));
			}
		}

		return eggMoves;
	}

	@Override
	public void setEggMoves(Map<Integer, List<Integer>> eggMoves) {
		try {
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				NARCArchive eggMoveNARC = this.readNARC(romEntry.getFile("EggMoves"));
				byte[] eggMoveData = eggMoveNARC.files.get(0);
				writeEggMoves(eggMoves, eggMoveData, 0);
				eggMoveNARC.files.set(0, eggMoveData);
				this.writeNARC(romEntry.getFile("EggMoves"), eggMoveNARC);
			} else {
				byte[] fieldOvl = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
				int offset = find(fieldOvl, Gen4Constants.dpptEggMoveTablePrefix);
				if (offset > 0) {
					offset += Gen4Constants.dpptEggMoveTablePrefix.length() / 2; // because it was a prefix
					writeEggMoves(eggMoves, fieldOvl, offset);
					this.writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOvl);
				}
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private Map<Integer, List<Integer>> readEggMoves(byte[] data, int startingOffset) {
		Map<Integer, List<Integer>> eggMoves = new TreeMap<>();
		int currentOffset = startingOffset;
		int currentSpecies = 0;
		List<Integer> currentMoves = new ArrayList<>();
		int val = FileFunctions.read2ByteInt(data, currentOffset);

		// Egg move data is stored exactly like in Gen 3, so check egg_moves.h in the
		// Gen 3 decomps for more info on how this algorithm works.
		while (val != 0xFFFF) {
			if (val > 20000) {
				int species = val - 20000;
				if (!currentMoves.isEmpty()) {
					eggMoves.put(currentSpecies, currentMoves);
				}
				currentSpecies = species;
				currentMoves = new ArrayList<>();
			} else {
				currentMoves.add(val);
			}
			currentOffset += 2;
			val = FileFunctions.read2ByteInt(data, currentOffset);
		}

		// Need to make sure the last entry gets recorded too
		if (!currentMoves.isEmpty()) {
			eggMoves.put(currentSpecies, currentMoves);
		}

		return eggMoves;
	}

	private void writeEggMoves(Map<Integer, List<Integer>> eggMoves, byte[] data, int startingOffset) {
		int currentOffset = startingOffset;
		for (int species : eggMoves.keySet()) {
			FileFunctions.write2ByteInt(data, currentOffset, species + 20000);
			currentOffset += 2;
			for (int move : eggMoves.get(species)) {
				FileFunctions.write2ByteInt(data, currentOffset, move);
				currentOffset += 2;
			}
		}
	}

	public static class TextEntry {
		private final int textIndex;
		private final int stringNumber;

		public TextEntry(int textIndex, int stringNumber) {
			this.textIndex = textIndex;
			this.stringNumber = stringNumber;
		}
	}

	public static class StaticPokemonGameCorner extends DSStaticPokemon {
		private final TextEntry[] textEntries;

		public StaticPokemonGameCorner(InFileEntry[] speciesEntries, InFileEntry[] levelEntries, TextEntry[] textEntries) {
			super(speciesEntries, new InFileEntry[0], levelEntries);
			this.textEntries = textEntries;
		}

		@Override
		public void setPokemon(AbstractDSRomHandler parent, NARCArchive scriptNARC, Species pkmn) {
			super.setPokemon(parent, scriptNARC, pkmn);
			for (TextEntry textEntry : textEntries) {
				List<String> strings = ((Gen4RomHandler) parent).getStrings(textEntry.textIndex);
				String originalString = strings.get(textEntry.stringNumber);
				// For JP, the first thing after the name is "\x0001". For non-JP, it's "\v0203"
				int postNameIndex = originalString.indexOf("\\");
				String newString = pkmn.getName().toUpperCase() + originalString.substring(postNameIndex);
				strings.set(textEntry.stringNumber, newString);
				((Gen4RomHandler) parent).setStrings(textEntry.textIndex, strings);
			}
		}
	}

	public static class RoamingPokemon {
		private final int[] speciesCodeOffsets;
		private final int[] levelCodeOffsets;
		private final InFileEntry[] speciesScriptOffsets;
		private final InFileEntry[] genderOffsets;

		public RoamingPokemon(int[] speciesCodeOffsets, int[] levelCodeOffsets, InFileEntry[] speciesScriptOffsets,
							  InFileEntry[] genderOffsets) {
			this.speciesCodeOffsets = speciesCodeOffsets;
			this.levelCodeOffsets = levelCodeOffsets;
			this.speciesScriptOffsets = speciesScriptOffsets;
			this.genderOffsets = genderOffsets;
		}

		public Species getPokemon(Gen4RomHandler parent) {
			int species = parent.readWord(parent.arm9, speciesCodeOffsets[0]);
			return parent.pokes[species];
		}

		public void setPokemon(Gen4RomHandler parent, NARCArchive scriptNARC, Species pkmn) {
			int value = pkmn.getNumber();
			for (int speciesCodeOffset : speciesCodeOffsets) {
				parent.writeWord(parent.arm9, speciesCodeOffset, value);
			}
			for (InFileEntry speciesScriptOffset : speciesScriptOffsets) {
				byte[] file = scriptNARC.files.get(speciesScriptOffset.getFile());
				parent.writeWord(file, speciesScriptOffset.getOffset(), value);
			}
			int gender = 0; // male (works for genderless Pokemon too)
			if (pkmn.getGenderRatio() == 0xFE) {
				gender = 1; // female
			}
			for (InFileEntry genderOffset : genderOffsets) {
				byte[] file = scriptNARC.files.get(genderOffset.getFile());
				parent.writeWord(file, genderOffset.getOffset(), gender);
			}
		}

		public int getLevel(Gen4RomHandler parent) {
			if (levelCodeOffsets.length == 0) {
				return 1;
			}
			return parent.arm9[levelCodeOffsets[0]];
		}

		public void setLevel(Gen4RomHandler parent, int level) {
			for (int levelCodeOffset : levelCodeOffsets) {
				parent.arm9[levelCodeOffset] = (byte) level;
			}
		}
	}

	@Override
	public List<StaticEncounter> getStaticPokemon() {
		List<StaticEncounter> sp = new ArrayList<>();
		if (!romEntry.hasStaticPokemonSupport()) {
			return sp;
		}
		try {
			int[] staticEggOffsets = romEntry.getArrayValue("StaticEggPokemonOffsets");
			NARCArchive scriptNARC = scriptNarc;
			for (int i = 0; i < romEntry.getStaticPokemon().size(); i++) {
				int currentOffset = i;
				DSStaticPokemon statP = romEntry.getStaticPokemon().get(i);
				StaticEncounter se = new StaticEncounter();
				Species newPK = statP.getPokemon(this, scriptNARC);
				newPK = getAltFormeOfSpecies(newPK, statP.getForme(scriptNARC));
				se.spec = newPK;
				se.level = statP.getLevel(scriptNARC, 0);
				se.isEgg = Arrays.stream(staticEggOffsets).anyMatch(x -> x == currentOffset);
				for (int levelEntry = 1; levelEntry < statP.getLevelCount(); levelEntry++) {
					StaticEncounter linkedStatic = new StaticEncounter();
					linkedStatic.spec = newPK;
					linkedStatic.level = statP.getLevel(scriptNARC, levelEntry);
					se.linkedEncounters.add(linkedStatic);
				}
				sp.add(se);
			}
			int[] trades = romEntry.getArrayValue("StaticPokemonTrades");
			if (trades.length > 0) {
				NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
				int[] scripts = romEntry.getArrayValue("StaticPokemonTradeScripts");
				int[] scriptOffsets = romEntry.getArrayValue("StaticPokemonTradeLevelOffsets");
				for (int i = 0; i < trades.length; i++) {
					int tradeNum = trades[i];
					byte[] scriptFile = scriptNARC.files.get(scripts[i]);
					int level = scriptFile[scriptOffsets[i]];
					StaticEncounter se = new StaticEncounter(pokes[readLong(tradeNARC.files.get(tradeNum), 0)]);
					se.level = level;
					sp.add(se);
				}
			}
			if (romEntry.getIntValue("MysteryEggOffset") > 0) {
				byte[] ovOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
				StaticEncounter se = isMysteryEggCommandImproved(ovOverlay) ?
						readImprovedMysteryEgg(ovOverlay) : readVanillaMysteryEgg(ovOverlay);
				sp.add(se);
			}
			if (romEntry.getIntValue("FossilTableOffset") > 0) {
				byte[] ftData = arm9;
				int baseOffset = romEntry.getIntValue("FossilTableOffset");
				int fossilLevelScriptNum = romEntry.getIntValue("FossilLevelScriptNumber");
				byte[] fossilLevelScript = scriptNARC.files.get(fossilLevelScriptNum);
				int level = fossilLevelScript[romEntry.getIntValue("FossilLevelOffset")];
				if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
					ftData = readOverlay(romEntry.getIntValue("FossilTableOvlNumber"));
				}
				// read the 7 Fossil Pokemon
				for (int f = 0; f < Gen4Constants.fossilCount; f++) {
					StaticEncounter se = new StaticEncounter(pokes[readWord(ftData, baseOffset + 2 + f * 4)]);
					se.level = level;
					sp.add(se);
				}
			}

			if (roamerRandomizationEnabled) {
				getRoamers(sp);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		return sp;
	}

	private boolean isMysteryEggCommandImproved(byte[] ovOverlay) {
		// the "identifier" is just the first byte that differs from the Vanilla command
		int offset = romEntry.getIntValue("MysteryEggCommandOffset");
		offset += Gen4Constants.mysteryEggImprovementIdentifierOffset;
		return ovOverlay[offset] == Gen4Constants.mysteryEggImprovementIdentifier;
	}

	private StaticEncounter readImprovedMysteryEgg(byte[] ovOverlay) {
		int offset = romEntry.getIntValue("MysteryEggCommandOffset") + Gen4Constants.mysteryEggCommandLength;
		StaticEncounter se = new StaticEncounter(pokes[readLong(ovOverlay, offset)]);
		se.isEgg = true;
		return se;
	}

	private StaticEncounter readVanillaMysteryEgg(byte[] ovOverlay) {
		StaticEncounter se = new StaticEncounter(pokes[ovOverlay[romEntry.getIntValue("MysteryEggOffset")] & 0xFF]);
		se.isEgg = true;
		return se;
	}

	@Override
	public boolean setStaticPokemon(List<StaticEncounter> staticPokemon) {
		if (!romEntry.hasStaticPokemonSupport()) {
			return false;
		}
		int sptsize = romEntry.getArrayValue("StaticPokemonTrades").length;
		int meggsize = romEntry.getIntValue("MysteryEggOffset") > 0 ? 1 : 0;
		int fossilsize = romEntry.getIntValue("FossilTableOffset") > 0 ? 7 : 0;
		if (staticPokemon.size() != romEntry.getStaticPokemon().size() + sptsize + meggsize + fossilsize
				+ romEntry.getRoamingPokemon().size()) {
			return false;
		}
		try {
			Iterator<StaticEncounter> statics = staticPokemon.iterator();
			NARCArchive scriptNARC = scriptNarc;
			for (DSStaticPokemon statP : romEntry.getStaticPokemon()) {
				StaticEncounter se = statics.next();
				statP.setPokemon(this, scriptNARC, se.spec);
				statP.setForme(scriptNARC, se.spec.getFormeNumber());
				statP.setLevel(scriptNARC, se.level, 0);
				for (int i = 0; i < se.linkedEncounters.size(); i++) {
					StaticEncounter linkedStatic = se.linkedEncounters.get(i);
					statP.setLevel(scriptNARC, linkedStatic.level, i + 1);
				}
			}
			int[] trades = romEntry.getArrayValue("StaticPokemonTrades");
			if (trades.length > 0) {
				NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
				int[] scripts = romEntry.getArrayValue("StaticPokemonTradeScripts");
				int[] scriptOffsets = romEntry.getArrayValue("StaticPokemonTradeLevelOffsets");
				for (int i = 0; i < trades.length; i++) {
					int tradeNum = trades[i];
					StaticEncounter se = statics.next();
					Species thisTrade = se.spec;

					// Write species and ability,
					// always ability1 out of simplicity even if some pokes got a second one.
					writeLong(tradeNARC.files.get(tradeNum), 0, thisTrade.getNumber());
					writeLong(tradeNARC.files.get(tradeNum), 0x1C, thisTrade.getAbility1());

					// Write level to script file
					byte[] scriptFile = scriptNARC.files.get(scripts[i]);
					scriptFile[scriptOffsets[i]] = (byte) se.level;

					// If it's Kenya, write new species name to text file
					if (i == 1) {
						Map<String, String> replacements = new TreeMap<>();
						replacements.put(pokes[SpeciesIDs.spearow].getName().toUpperCase(), se.spec.getName());
						replaceAllStringsInEntry(romEntry.getIntValue("KenyaTextOffset"), replacements);
					}
				}
				writeNARC(romEntry.getFile("InGameTrades"), tradeNARC);
			}
			if (romEntry.getIntValue("MysteryEggOffset") > 0) {
				setMysteryEgg(statics.next());
			}
			if (romEntry.getIntValue("FossilTableOffset") > 0) {
				int baseOffset = romEntry.getIntValue("FossilTableOffset");
				int fossilLevelScriptNum = romEntry.getIntValue("FossilLevelScriptNumber");
				byte[] fossilLevelScript = scriptNARC.files.get(fossilLevelScriptNum);
				if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
					byte[] ftData = readOverlay(romEntry.getIntValue("FossilTableOvlNumber"));
					for (int f = 0; f < Gen4Constants.fossilCount; f++) {
						StaticEncounter se = statics.next();
						int pokenum = se.spec.getNumber();
						writeWord(ftData, baseOffset + 2 + f * 4, pokenum);
						fossilLevelScript[romEntry.getIntValue("FossilLevelOffset")] = (byte) se.level;
					}
					writeOverlay(romEntry.getIntValue("FossilTableOvlNumber"), ftData);
				} else {
					// write to arm9
					for (int f = 0; f < Gen4Constants.fossilCount; f++) {
						StaticEncounter se = statics.next();
						int pokenum = se.spec.getNumber();
						writeWord(arm9, baseOffset + 2 + f * 4, pokenum);
						fossilLevelScript[romEntry.getIntValue("FossilLevelOffset")] = (byte) se.level;
					}
				}
			}
			if (roamerRandomizationEnabled) {
				setRoamers(statics);
			}
			if (romEntry.getRomType() == Gen4Constants.Type_Plat) {
				patchDistortionWorldGroundCheck();
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		return true;
	}

	private void setMysteryEgg(StaticEncounter se) throws IOException {
		// The Mystery Egg (HGSS Togepi) uses a different command than other eggs,
		// which is hardcoded in the field overlay. Normally this command uses a single
		// byte to represent Togepi's number, which clearly doesn't allow for any Species
		// with a number > 255 to replace it.
		// To fix this, we overwrite the command with an improved version by AdAstra
		// on the Kingdom of DS Hacking Discord server. This improved command allows
		// a full long for both the Species and the Move ID.
		// (The Move ID is an extra move the Mystery Egg mon gets. In Vanilla, it is Togepi's Extrasensory.)
		byte[] ovOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
		int offset = romEntry.getIntValue("MysteryEggCommandOffset");

		if (!isMysteryEggCommandImproved(ovOverlay)) {
			improveMysteryEggCommand(ovOverlay);
		}
		offset += Gen4Constants.mysteryEggCommandLength;

		writeLong(ovOverlay, offset, se.spec.getNumber());
		offset += 4;

		Move extraMove = getMysteryEggMove(se.spec);
		writeLong(ovOverlay, offset, extraMove == null ? 0 : extraMove.number);

		writeOverlay(romEntry.getIntValue("FieldOvlNumber"), ovOverlay);
	}

	private void improveMysteryEggCommand(byte[] ovOverlay) {
        int offset = romEntry.getIntValue("MysteryEggCommandOffset");
        byte[] bytesBefore = Arrays.copyOfRange(ovOverlay, offset, offset + Gen4Constants.mysteryEggCommandLength);

		// The vanilla command looks slightly different between different localizations; it contains
		// a number of relative branch instructions that differ only by a few bytes.
		// Otherwise, the command is identical.
		// When "improving" the command, we naturally scoot those branch instructions over, and must thus adjust them
		// to account for their new locations (remember, they are *relative* branch instructions).
		// Previous versions of the program simply contained the improved bytes as a constant, though only for HGSS (U).
		// We could do the same for other versions, manually transforming the bytes and then include them in the
		// Rom entries, but this is rather wasteful, ugly, and would be prone to human-made errors.
		// Thus, the usage of the ARMThumbCode class.

		// AdAstra wrote that original version for HGSS (U), which the below is essentially a translation of.

		// The code below doesn't say that much on its own; it is still byte-code manipulating,
		// but printing out the armTC object does help, along with external tools like this opcode map:
		// https://imrannazar.com/articles/arm-opcode-map, and online (dis)assembler: https://armconverter.com/.
		ARMThumbCode armTC = new ARMThumbCode(bytesBefore);
		// Operations start at high offsets just because it was easier to count instructions that way,
		// since the shifting done by the removal at offset 30 could be ignored.
		armTC.insertInstructions(216,
				(byte) 0x04, ARMThumbCode.ADDSP_imm7,
				(byte) 0xf8, ARMThumbCode.POP_pc,
				(byte) 0x00, ARMThumbCode.LSL_imm_0);
		armTC.setInstruction(108, (byte) 0x1D, ARMThumbCode.LDRPC_r1);
		armTC.setInstruction(96, (byte) 0x01, ARMThumbCode.ADD_i8r4);
		armTC.setInstruction(66, (byte) 0x26, ARMThumbCode.LDRPC_r1);
		armTC.removeInstructions(30, 4);
		armTC.insertInstructions(30, (byte) 0x57, ARMThumbCode.BGE);

		// The improved command is the about same length as the original one.
		// "About" since it is proceeded by 8 bytes that we overwrite with the new species/move ids.
		// Anyway, it is safe to overwrite in the same location.
		writeBytes(ovOverlay, offset, armTC.toBytes());
	}

	/**
	 * Returns an appropriate extra move for the Mystery Egg mon.<br>
	 * Egg moves have the highest priority, then Move Tutor moves, then TM moves. Within each of these groups,
	 * strong attacking moves are prioritized. If the Pokemon has neither Egg, Move Tutor, nor TM moves,
	 * <b>returns null</b>.
	 */
	private Move getMysteryEggMove(Species pk) {
		Set<Integer> moveIDs = getEffectiveEggMoves(pk);
		if (moveIDs.isEmpty()) {
			moveIDs = getCompatibleTutorMoves(pk);
		}
		if (moveIDs.isEmpty()) {
			moveIDs = getCompatibleTMMoves(pk);
		}
		if (moveIDs.isEmpty()) {
			return null;
		}

		List<Move> posMoves = moveIDs.stream().map(moveID -> moves[moveID])
				.distinct()
				.sorted(Comparator.comparingDouble(m -> calculateMoveStrength(m, pk)))
				.collect(Collectors.toList());
		return posMoves.get(posMoves.size() - 1);
	}

	private Set<Integer> getCompatibleTutorMoves(Species pk) {
		Set<Integer> moveIDs = new HashSet<>();
		List<Integer> tutorMoves = getMoveTutorMoves();
		boolean[] comp = getMoveTutorCompatibility().get(pk);
		for (int i = 0; i < tutorMoves.size(); i++) {
			if (comp[i + 1]) {
				moveIDs.add(tutorMoves.get(i));
			}
		}
		return moveIDs;
	}

	private Set<Integer> getCompatibleTMMoves(Species pk) {
		Set<Integer> moveIDs = new HashSet<>();
		List<Integer> tmMoves = getTMMoves();
		boolean[] comp = getTMHMCompatibility().get(pk);
		for (int i = 0; i < tmMoves.size(); i++) {
			if (comp[i + 1]) {
				moveIDs.add(tmMoves.get(i));
			}
		}
		return moveIDs;
	}

	/**
	 * Calculates the Move's expected damage output when used by a specific Pokemon,
	 * taking hit ratio, hit count, attacking stat, and STAB into account.
	 * Status moves always return 0.
	 */
	private double calculateMoveStrength(Move m, Species pk) {
		// TODO: this could be used in more places
		if (m.category == MoveCategory.STATUS) {
			return 0;
		}
		double strength = m.power * m.hitCount * m.hitratio;
		strength *= m.category == MoveCategory.PHYSICAL ? pk.getAttack() : pk.getSpatk();
		if (m.type == pk.getPrimaryType(false) || m.type == pk.getSecondaryType(false)) {
			strength *= Gen4Constants.stabMultiplier;
		}
		return strength;
	}

	private void getRoamers(List<StaticEncounter> statics) {
		if (romEntry.getRomType() == Gen4Constants.Type_DP) {
			int offset = romEntry.getIntValue("RoamingPokemonFunctionStartOffset");
			if (readWord(arm9, offset + 44) != 0) {
				// In the original code, the code at this offset would be performing a shift to
				// put
				// Cresselia's constant in r7. After applying the patch, this is now a nop,
				// since
				// we just pc-relative load it instead. So if a nop isn't here, apply the patch.
				applyDiamondPearlRoamerPatch();
			}
		} else if (romEntry.getRomType() == Gen4Constants.Type_Plat || romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			int firstSpeciesOffset = romEntry.getRoamingPokemon().get(0).speciesCodeOffsets[0];
			if (arm9.length < firstSpeciesOffset || readWord(arm9, firstSpeciesOffset) == 0) {
				// Either the arm9 hasn't been extended, or the patch hasn't been written
				int extendBy = romEntry.getIntValue("Arm9ExtensionSize");
				arm9 = extendARM9(arm9, extendBy, romEntry.getStringValue("TCMCopyingPrefix"), Gen4Constants.arm9Offset);
				genericIPSPatch(arm9, "NewRoamerSubroutineTweak");
			}
		}
		for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
			RoamingPokemon roamer = romEntry.getRoamingPokemon().get(i);
			StaticEncounter se = new StaticEncounter();
			se.spec = roamer.getPokemon(this);
			se.level = roamer.getLevel(this);
			statics.add(se);
		}
	}

	private void setRoamers(Iterator<StaticEncounter> statics) {
		for (int i = 0; i < romEntry.getRoamingPokemon().size(); i++) {
			RoamingPokemon roamer = romEntry.getRoamingPokemon().get(i);
			StaticEncounter roamerEncounter = statics.next();
			roamer.setPokemon(this, scriptNarc, roamerEncounter.spec);
			roamer.setLevel(this, roamerEncounter.level);
		}
	}

	private void applyDiamondPearlRoamerPatch() {
		int offset = romEntry.getIntValue("RoamingPokemonFunctionStartOffset");

		// The original code had an entry for Darkrai; its species ID is pc-relative
		// loaded. Since this
		// entry is clearly unused, just replace Darkrai's species ID constant with
		// Cresselia's, since
		// in the original code, her ID is computed as 0x7A << 0x2
		FileFunctions.writeFullInt(arm9, offset + 244, SpeciesIDs.cresselia);

		// Now write a pc-relative load to our new constant over where Cresselia's ID is
		// normally mov'd
		// into r7 and shifted.
		arm9[offset + 42] = 0x32;
		arm9[offset + 43] = 0x4F;
		arm9[offset + 44] = 0x00;
		arm9[offset + 45] = 0x00;
	}

	private void patchDistortionWorldGroundCheck() throws IOException {
		byte[] fieldOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
		int offset = find(fieldOverlay, Gen4Constants.distortionWorldGroundCheckPrefix);
		if (offset > 0) {
			offset += Gen4Constants.distortionWorldGroundCheckPrefix.length() / 2; // because it was a prefix

			// We're now looking at a jump table in the field overlay that determines which
			// intro graphic the game
			// should display when encountering a Pokemon that does *not* have a special
			// intro. The Giratina fight
			// in the Distortion World uses ground type 23, and that particular ground type
			// never initializes the
			// variable that determines which graphic to use. As a result, if Giratina is
			// replaced with a Pokemon
			// that lacks a special intro, the game will use an uninitialized value for the
			// intro graphic and crash.
			// The below code simply patches the jump table entry for ground type 23 to take
			// the same branch that
			// regular grass encounters take, ensuring the intro graphic variable is
			// initialized.
			fieldOverlay[offset + (2 * 23)] = 0x30;
			writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOverlay);
		}
	}

	@Override
	public List<Integer> getTMMoves() {
		String tmDataPrefix;
		if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
			tmDataPrefix = Gen4Constants.dpptTMDataPrefix;
		} else {
			tmDataPrefix = Gen4Constants.hgssTMDataPrefix;
		}
		int offset = find(arm9, tmDataPrefix);
		if (offset > 0) {
			offset += tmDataPrefix.length() / 2; // because it was a prefix
			List<Integer> tms = new ArrayList<>();
			for (int i = 0; i < Gen4Constants.tmCount; i++) {
				tms.add(readWord(arm9, offset + i * 2));
			}
			return tms;
		} else {
			return null;
		}
	}

	@Override
	public List<Integer> getHMMoves() {
		String tmDataPrefix;
		if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
			tmDataPrefix = Gen4Constants.dpptTMDataPrefix;
		} else {
			tmDataPrefix = Gen4Constants.hgssTMDataPrefix;
		}
		int offset = find(arm9, tmDataPrefix);
		if (offset > 0) {
			offset += tmDataPrefix.length() / 2; // because it was a prefix
			offset += Gen4Constants.tmCount * 2; // TM data
			List<Integer> hms = new ArrayList<>();
			for (int i = 0; i < Gen4Constants.hmCount; i++) {
				hms.add(readWord(arm9, offset + i * 2));
			}
			return hms;
		} else {
			return null;
		}
	}

	@Override
	public void setTMMoves(List<Integer> moveIndexes) {
		List<Integer> oldMoveIndexes = this.getTMMoves();
		String tmDataPrefix;
		if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
			tmDataPrefix = Gen4Constants.dpptTMDataPrefix;
		} else {
			tmDataPrefix = Gen4Constants.hgssTMDataPrefix;
		}
		int offset = find(arm9, tmDataPrefix);
		if (offset > 0) {
			offset += tmDataPrefix.length() / 2; // because it was a prefix
			for (int i = 0; i < Gen4Constants.tmCount; i++) {
				writeWord(arm9, offset + i * 2, moveIndexes.get(i));
			}

			// Update TM item descriptions
			List<String> itemDescriptions = getStrings(romEntry.getIntValue("ItemDescriptionsTextOffset"));
			List<String> moveDescriptions = getStrings(romEntry.getIntValue("MoveDescriptionsTextOffset"));
			int textCharsPerLine = Gen4Constants.getTextCharsPerLine(romEntry.getRomType());
			// TM01 is item 328 and so on
			for (int i = 0; i < Gen4Constants.tmCount; i++) {
				// Rewrite 5-line move descs into 3-line item descs
				itemDescriptions.set(i + Gen4Constants.tmItemOffset, RomFunctions.rewriteDescriptionForNewLineSize(
						moveDescriptions.get(moveIndexes.get(i)), "\\n", textCharsPerLine, ssd));
			}
			// Save the new item descriptions
			setStrings(romEntry.getIntValue("ItemDescriptionsTextOffset"), itemDescriptions);
			// Palettes update
			String baseOfPalettes = Gen4Constants.pthgssItemPalettesPrefix;
			if (romEntry.getRomType() == Gen4Constants.Type_DP) {
				baseOfPalettes = Gen4Constants.dpItemPalettesPrefix;
			}
			int offsPals = find(arm9, baseOfPalettes);
			if (offsPals > 0) {
				// Write pals
				for (int i = 0; i < Gen4Constants.tmCount; i++) {
					Move m = this.moves[moveIndexes.get(i)];
					int pal = this.typeTMPaletteNumber(m.type);
					writeWord(arm9, offsPals + i * 8 + 2, pal);
				}
			}
			// if we can't update the palettes, it's not a big deal...

			// Update TM Text
			for (int i = 0; i < Gen4Constants.tmCount; i++) {
				int oldMoveIndex = oldMoveIndexes.get(i);
				int newMoveIndex = moveIndexes.get(i);
				int tmNumber = i + 1;

				if (romEntry.getTMTexts().containsKey(tmNumber)) {
					List<TextEntry> textEntries = romEntry.getTMTexts().get(tmNumber);
					Set<Integer> textFiles = new HashSet<>();
					for (TextEntry textEntry : textEntries) {
						textFiles.add(textEntry.textIndex);
					}
					String oldMoveName = moves[oldMoveIndex].name;
					String newMoveName = moves[newMoveIndex].name;
					if (romEntry.getRomType() == Gen4Constants.Type_HGSS && oldMoveIndex == MoveIDs.roar) {
						// It's somewhat dumb to even be bothering with this, but it's too silly not to
						// do
						oldMoveName = oldMoveName.toUpperCase();
						newMoveName = newMoveName.toUpperCase();
					}
					Map<String, String> replacements = new TreeMap<>();
					replacements.put(oldMoveName, newMoveName);
					for (int textFile : textFiles) {
						replaceAllStringsInEntry(textFile, replacements);
					}
				}

				if (romEntry.getTmTextsGameCorner().containsKey(tmNumber)) {
					TextEntry textEntry = romEntry.getTmTextsGameCorner().get(tmNumber);
					setBottomScreenTMText(textEntry.textIndex, textEntry.stringNumber, newMoveIndex);
				}

				if (romEntry.getTMScriptOffsetsFrontier().containsKey(tmNumber)) {
					int scriptFile = romEntry.getIntValue("FrontierScriptNumber");
					byte[] frontierScript = scriptNarc.files.get(scriptFile);
					int scriptOffset = romEntry.getTMScriptOffsetsFrontier().get(tmNumber);
					writeWord(frontierScript, scriptOffset, newMoveIndex);
					scriptNarc.files.set(scriptFile, frontierScript);
				}

				if (romEntry.getTMTextsFrontier().containsKey(tmNumber)) {
					int textOffset = romEntry.getIntValue("MiscUITextOffset");
					int stringNumber = romEntry.getTMTextsFrontier().get(tmNumber);
					setBottomScreenTMText(textOffset, stringNumber, newMoveIndex);
				}
			}
		}
	}

	private void setBottomScreenTMText(int textOffset, int stringNumber, int newMoveIndex) {
		List<String> strings = getStrings(textOffset);
		String originalString = strings.get(stringNumber);

		// The first thing after the name is "\n".
		int postNameIndex = originalString.indexOf("\\");
		String originalName = originalString.substring(0, postNameIndex);

		// Some languages (like English) write the name in ALL CAPS, others don't.
		// Check if the original is ALL CAPS and then match it for consistency.
		boolean isAllCaps = originalName.equals(originalName.toUpperCase());
		String newName = moves[newMoveIndex].name;
		if (isAllCaps) {
			newName = newName.toUpperCase();
		}
		String newString = newName + originalString.substring(postNameIndex);
		strings.set(stringNumber, newString);
		setStrings(textOffset, strings);
	}

	private static RomFunctions.StringSizeDeterminer ssd = new RomFunctions.StringLengthSD();

	@Override
	public int getTMCount() {
		return Gen4Constants.tmCount;
	}

	@Override
	public int getHMCount() {
		return Gen4Constants.hmCount;
	}

	@Override
	public Map<Species, boolean[]> getTMHMCompatibility() {
		Map<Species, boolean[]> compat = new TreeMap<>();
		int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
		for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
			byte[] data;
			if (i > Gen4Constants.pokemonCount) {
				data = pokeNarc.files.get(i + Gen4Constants.formeOffset);
			} else {
				data = pokeNarc.files.get(i);
			}
			Species pkmn = pokes[i];
			boolean[] flags = new boolean[Gen4Constants.tmCount + Gen4Constants.hmCount + 1];
			for (int j = 0; j < 13; j++) {
				readByteIntoFlags(data, flags, j * 8 + 1, Gen4Constants.bsTMHMCompatOffset + j);
			}
			compat.put(pkmn, flags);
		}
		return compat;
	}

	@Override
	public void setTMHMCompatibility(Map<Species, boolean[]> compatData) {
		for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
			Species pkmn = compatEntry.getKey();
			boolean[] flags = compatEntry.getValue();
			byte[] data = pokeNarc.files.get(pkmn.getNumber());
			for (int j = 0; j < 13; j++) {
				data[Gen4Constants.bsTMHMCompatOffset + j] = getByteFromFlags(flags, j * 8 + 1);
			}
		}
	}

	@Override
	public boolean hasMoveTutors() {
		return romEntry.getRomType() != Gen4Constants.Type_DP;
	}

	@Override
	public List<Integer> getMoveTutorMoves() {
		if (!hasMoveTutors()) {
			return new ArrayList<>();
		}
		int baseOffset = romEntry.getIntValue("MoveTutorMovesOffset");
		int amount = romEntry.getIntValue("MoveTutorCount");
		int bytesPer = romEntry.getIntValue("MoveTutorBytesCount");
		List<Integer> mtMoves = new ArrayList<>();
		try {
			byte[] mtFile = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
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
		int baseOffset = romEntry.getIntValue("MoveTutorMovesOffset");
		int amount = romEntry.getIntValue("MoveTutorCount");
		int bytesPer = romEntry.getIntValue("MoveTutorBytesCount");
		if (moves.size() != amount) {
			return;
		}
		try {
			byte[] mtFile = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
			for (int i = 0; i < amount; i++) {
				writeWord(mtFile, baseOffset + i * bytesPer, moves.get(i));
			}
			writeOverlay(romEntry.getIntValue("FieldOvlNumber"), mtFile);

			// In HGSS, Headbutt is the last tutor move, but the tutor teaches it
			// to you via a hardcoded script rather than looking at this data
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				setHGSSHeadbuttTutor(moves.get(moves.size() - 1));
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private void setHGSSHeadbuttTutor(int headbuttReplacement) {
		byte[] ilexForestScripts = scriptNarc.files.get(Gen4Constants.ilexForestScriptFile);
		for (int offset : Gen4Constants.headbuttTutorScriptOffsets) {
			writeWord(ilexForestScripts, offset, headbuttReplacement);
		}

		String replacementName = moves[headbuttReplacement].name;
		Map<String, String> replacements = new TreeMap<>();
		replacements.put(moves[MoveIDs.headbutt].name, replacementName);
		replaceAllStringsInEntry(Gen4Constants.ilexForestStringsFile, replacements);
	}

	@Override
	public Map<Species, boolean[]> getMoveTutorCompatibility() {
		if (!hasMoveTutors()) {
			return new TreeMap<>();
		}
		Map<Species, boolean[]> compat = new TreeMap<>();
		int amount = romEntry.getIntValue("MoveTutorCount");
		int baseOffset = romEntry.getIntValue("MoveTutorCompatOffset");
		int bytesPer = romEntry.getIntValue("MoveTutorCompatBytesCount");
		try {
			byte[] mtcFile;
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				mtcFile = readFile(romEntry.getFile("MoveTutorCompat"));
			} else {
				mtcFile = readOverlay(romEntry.getIntValue("MoveTutorCompatOvlNumber"));
			}
			int formeCount = Gen4Constants.getFormeCount(romEntry.getRomType());
			for (int i = 1; i <= Gen4Constants.pokemonCount + formeCount; i++) {
				Species pkmn = pokes[i];
				boolean[] flags = new boolean[amount + 1];
				for (int j = 0; j < bytesPer; j++) {
					if (i > Gen4Constants.pokemonCount) {
						readByteIntoFlags(mtcFile, flags, j * 8 + 1, baseOffset + (i - 1) * bytesPer + j);
					} else {
						readByteIntoFlags(mtcFile, flags, j * 8 + 1, baseOffset + (i - 1) * bytesPer + j);
					}
				}
				compat.put(pkmn, flags);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		return compat;
	}

	@Override
	public void setMoveTutorCompatibility(Map<Species, boolean[]> compatData) {
		if (!hasMoveTutors()) {
			return;
		}
		int amount = romEntry.getIntValue("MoveTutorCount");
		int baseOffset = romEntry.getIntValue("MoveTutorCompatOffset");
		int bytesPer = romEntry.getIntValue("MoveTutorCompatBytesCount");
		try {
			byte[] mtcFile;
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				mtcFile = readFile(romEntry.getFile("MoveTutorCompat"));
			} else {
				mtcFile = readOverlay(romEntry.getIntValue("MoveTutorCompatOvlNumber"));
			}
			for (Map.Entry<Species, boolean[]> compatEntry : compatData.entrySet()) {
				Species pkmn = compatEntry.getKey();
				boolean[] flags = compatEntry.getValue();
				for (int j = 0; j < bytesPer; j++) {
					int offsHere = baseOffset + (pkmn.getNumber() - 1) * bytesPer + j;
					if (j * 8 + 8 <= amount) {
						// entirely new byte
						mtcFile[offsHere] = getByteFromFlags(flags, j * 8 + 1);
					} else if (j * 8 < amount) {
						// need some of the original byte
						int newByte = getByteFromFlags(flags, j * 8 + 1) & 0xFF;
						int oldByteParts = (mtcFile[offsHere] >>> (8 - amount + j * 8)) << (8 - amount + j * 8);
						mtcFile[offsHere] = (byte) (newByte | oldByteParts);
					}
					// else do nothing to the byte
				}
			}
			if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				writeFile(romEntry.getFile("MoveTutorCompat"), mtcFile);
			} else {
				writeOverlay(romEntry.getIntValue("MoveTutorCompatOvlNumber"), mtcFile);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private int find(byte[] data, String hexString) {
		List<Integer> found = RomFunctions.search(data, RomFunctions.hexToBytes(hexString));
		if (found.isEmpty()) {
			throw new RuntimeException("Not found");
		} else if (found.size() > 1) {
			throw new RuntimeException("Not unique");
		} else {
			return found.get(0);
		}
	}

	private boolean lastStringsCompressed = false;

	private List<String> getStrings(int index) {
		PokeTextData pt = new PokeTextData(msgNarc.files.get(index));
		pt.decrypt();
		lastStringsCompressed = pt.compressFlag;
		return new ArrayList<>(pt.strlist);
	}

	private void setStrings(int index, List<String> newStrings) {
		setStrings(index, newStrings, false);
	}

	private void setStrings(int index, List<String> newStrings, boolean compressed) {
		byte[] rawUnencrypted = TextToPoke.MakeFile(newStrings, compressed);

		// make new encrypted name set
		PokeTextData encrypt = new PokeTextData(rawUnencrypted);
		encrypt.SetKey(0xD00E);
		encrypt.encrypt();

		// rewrite
		msgNarc.files.set(index, encrypt.get());
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
		// dppt technically do but we ignore them completely
		return romEntry.getRomType() == Gen4Constants.Type_HGSS;
	}

	@Override
	public boolean hasWildAltFormes() {
		return false;
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
	public List<TotemPokemon> getTotemPokemon() {
		return new ArrayList<>();
	}

	@Override
	public void setTotemPokemon(List<TotemPokemon> totemPokemon) {

	}

	@Override
	public boolean hasStarterAltFormes() {
		return false;
	}

	@Override
	public int starterCount() {
		return 3;
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
			for (int i = 1; i <= Gen4Constants.pokemonCount; i++) {
				Species pk = pokes[i];
				byte[] evoEntry = evoNARC.files.get(i);
				for (int evo = 0; evo < 7; evo++) {
					int method = readWord(evoEntry, evo * 6);
					int species = readWord(evoEntry, evo * 6 + 4);
					if (method >= 1 && method <= Gen4Constants.evolutionMethodCount && species >= 1) {
						EvolutionType et = Gen4Constants.evolutionTypeFromIndex(method);
						int extraInfo = readWord(evoEntry, evo * 6 + 2);
						Evolution evol = new Evolution(pokes[i], pokes[species], et, extraInfo);
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
		try {
			NARCArchive evoNARC = readNARC(romEntry.getFile("PokemonEvolutions"));
			for (int i = 1; i <= Gen4Constants.pokemonCount; i++) {
				byte[] evoEntry = evoNARC.files.get(i);
				Species pk = pokes[i];
				if (pk.getNumber() == SpeciesIDs.nincada) {
					writeShedinjaEvolution();
				}
				int evosWritten = 0;
				for (Evolution evo : pk.getEvolutionsFrom()) {
					writeWord(evoEntry, evosWritten * 6, Gen4Constants.evolutionTypeToIndex(evo.getType()));
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
	}

	private void writeShedinjaEvolution() {
		Species nincada = pokes[SpeciesIDs.nincada];

		// When the "Limit Pokemon" setting is enabled and Gen 3 is disabled, or when
		// "Random Every Level" evolutions are selected, we end up clearing out
		// Nincada's
		// vanilla evolutions. In that case, there's no point in even worrying about
		// Shedinja, so just return.
		if (nincada.getEvolutionsFrom().size() < 2) {
			return;
		}
		Species extraEvolution = nincada.getEvolutionsFrom().get(1).getTo();

		// In all the Gen 4 games, the game is hardcoded to check for
		// the LEVEL_IS_EXTRA evolution method; if it the Pokemon has it,
		// then a harcoded Shedinja is generated after every evolution
		// by using the following instructions:
		// mov r0, #0x49
		// lsl r0, r0, #2
		// The below code tweaks this instruction to load the species ID of Nincada's
		// new extra evolution into r0 using an 8-bit addition. Since Gen 4 has fewer
		// than 510 species in it, this will always succeed.
		int offset = find(arm9, Gen4Constants.shedinjaSpeciesLocator);
		if (offset > 0) {
			int lowByte, highByte;
			if (extraEvolution.getNumber() < 256) {
				lowByte = extraEvolution.getNumber();
				highByte = 0;
			} else {
				lowByte = 255;
				highByte = extraEvolution.getNumber() - 255;
			}

			// mov r0, lowByte
			// add r0, r0, highByte
			arm9[offset] = (byte) lowByte;
			arm9[offset + 1] = 0x20;
			arm9[offset + 2] = (byte) highByte;
			arm9[offset + 3] = 0x30;
		}
	}

	@Override
	public void removeImpossibleEvolutions(Settings settings) {
		boolean changeMoveEvos = !(settings.getMovesetsMod() == Settings.MovesetsMod.UNCHANGED);

		Map<Integer, List<MoveLearnt>> movesets = this.getMovesLearnt();
		Set<Evolution> extraEvolutions = new HashSet<>();
		for (Species pkmn : pokes) {
			if (pkmn != null) {
				extraEvolutions.clear();
				for (Evolution evo : pkmn.getEvolutionsFrom()) {
					// new 160 other impossible evolutions:
					if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
						// beauty milotic
						if (evo.getType() == EvolutionType.LEVEL_HIGH_BEAUTY) {
							// Replace w/ level 35
							evo.setType(EvolutionType.LEVEL);
							evo.setExtraInfo(35);
							addEvoUpdateLevel(impossibleEvolutionUpdates, evo);
						}
						// mt.coronet (magnezone/probopass)
						if (evo.getType() == EvolutionType.LEVEL_ELECTRIFIED_AREA) {
							// Replace w/ level 40
							evo.setType(EvolutionType.LEVEL);
							evo.setExtraInfo(40);
							addEvoUpdateLevel(impossibleEvolutionUpdates, evo);
						}
						// moss rock (leafeon)
						if (evo.getType() == EvolutionType.LEVEL_MOSS_ROCK) {
							// Replace w/ leaf stone
							evo.setType(EvolutionType.STONE);
							evo.setExtraInfo(ItemIDs.leafStone);
							addEvoUpdateStone(impossibleEvolutionUpdates, evo, itemNames.get(evo.getExtraInfo()));
						}
						// icy rock (glaceon)
						if (evo.getType() == EvolutionType.LEVEL_ICY_ROCK) {
							// Replace w/ dawn stone
							evo.setType(EvolutionType.STONE);
							evo.setExtraInfo(ItemIDs.dawnStone);
							addEvoUpdateStone(impossibleEvolutionUpdates, evo, itemNames.get(evo.getExtraInfo()));
						}
					}
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
						evo.setType(EvolutionType.LEVEL);
						evo.setExtraInfo(levelLearntAt);
						addEvoUpdateLevel(impossibleEvolutionUpdates, evo);
					}
					// Pure Trade
					if (evo.getType() == EvolutionType.TRADE) {
						// Replace w/ level 37
						evo.setType(EvolutionType.LEVEL);
						evo.setExtraInfo(37);
						addEvoUpdateLevel(impossibleEvolutionUpdates, evo);
					}
					// Trade w/ Item
					if (evo.getType() == EvolutionType.TRADE_ITEM) {
						// Get the current item & evolution
						int item = evo.getExtraInfo();
						if (evo.getFrom().getNumber() == SpeciesIDs.slowpoke) {
							// Slowpoke is awkward - he already has a level evo
							// So we can't do Level up w/ Held Item for him
							// Put Water Stone instead
							evo.setType(EvolutionType.STONE);
							evo.setExtraInfo(ItemIDs.waterStone);
							addEvoUpdateStone(impossibleEvolutionUpdates, evo, itemNames.get(evo.getExtraInfo()));
						} else {
							addEvoUpdateHeldItem(impossibleEvolutionUpdates, evo, itemNames.get(item));
							// Replace, for this entry, w/
							// Level up w/ Held Item at Day
							evo.setType(EvolutionType.LEVEL_ITEM_DAY);
							// now add an extra evo for
							// Level up w/ Held Item at Night
							Evolution extraEntry = new Evolution(evo.getFrom(), evo.getTo(), EvolutionType.LEVEL_ITEM_NIGHT,
									item);
							extraEvolutions.add(extraEntry);
						}
					}
				}
				pkmn.getEvolutionsFrom().addAll(extraEvolutions);
				for (Evolution ev : extraEvolutions) {
					ev.getTo().getEvolutionsTo().add(ev);
				}
			}
		}

	}

	@Override
	public void makeEvolutionsEasier(Settings settings) {
		boolean wildsRandomized = settings.isRandomizeWildPokemon();

		// Reduce the amount of happiness required to evolve.
		int offset = find(arm9, Gen4Constants.friendshipValueForEvoLocator);
		if (offset > 0) {
			// Amount of required happiness for HAPPINESS evolutions.
			if (arm9[offset] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
				arm9[offset] = (byte) GlobalConstants.easierHappinessToEvolve;
			}
			// Amount of required happiness for HAPPINESS_DAY evolutions.
			if (arm9[offset + 22] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
				arm9[offset + 22] = (byte) GlobalConstants.easierHappinessToEvolve;
			}
			// Amount of required happiness for HAPPINESS_NIGHT evolutions.
			if (arm9[offset + 44] == (byte) GlobalConstants.vanillaHappinessToEvolve) {
				arm9[offset + 44] = (byte) GlobalConstants.easierHappinessToEvolve;
			}
		}

		if (wildsRandomized) {
			for (Species pkmn : pokes) {
				if (pkmn != null) {
					for (Evolution evo : pkmn.getEvolutionsFrom()) {
						if (evo.getType() == EvolutionType.LEVEL_WITH_OTHER) {
							// Replace w/ level 35
							evo.setType(EvolutionType.LEVEL);
							evo.setExtraInfo(35);
							addEvoUpdateCondensed(easierEvolutionUpdates, evo, false);
						}
					}
				}
			}
		}
	}

	@Override
	public void removeTimeBasedEvolutions() {
		Set<Evolution> extraEvolutions = new HashSet<>();
		for (Species pkmn : pokes) {
			if (pkmn != null) {
				extraEvolutions.clear();
				for (Evolution evo : pkmn.getEvolutionsFrom()) {
					if (evo.getType() == EvolutionType.HAPPINESS_DAY) {
						if (evo.getFrom().getNumber() == SpeciesIDs.eevee) {
							// We can't set Eevee to evolve into Espeon with happiness at night because
							// that's how
							// Umbreon works in the original game. Instead, make Eevee: == sun stone =>
							// Espeon
							evo.setType(EvolutionType.STONE);
							evo.setExtraInfo(ItemIDs.sunStone);
							addEvoUpdateStone(timeBasedEvolutionUpdates, evo, itemNames.get(evo.getExtraInfo()));
						} else {
							// Add an extra evo for Happiness at Night
							addEvoUpdateHappiness(timeBasedEvolutionUpdates, evo);
							Evolution extraEntry = new Evolution(evo.getFrom(), evo.getTo(), EvolutionType.HAPPINESS_NIGHT,
									0);
							extraEvolutions.add(extraEntry);
						}
					} else if (evo.getType() == EvolutionType.HAPPINESS_NIGHT) {
						if (evo.getFrom().getNumber() == SpeciesIDs.eevee) {
							// We can't set Eevee to evolve into Umbreon with happiness at day because
							// that's how
							// Espeon works in the original game. Instead, make Eevee: == moon stone =>
							// Umbreon
							evo.setType(EvolutionType.STONE);
							evo.setExtraInfo(ItemIDs.moonStone);
							addEvoUpdateStone(timeBasedEvolutionUpdates, evo, itemNames.get(evo.getExtraInfo()));
						} else {
							// Add an extra evo for Happiness at Day
							addEvoUpdateHappiness(timeBasedEvolutionUpdates, evo);
							Evolution extraEntry = new Evolution(evo.getFrom(), evo.getTo(), EvolutionType.HAPPINESS_DAY,
									0);
							extraEvolutions.add(extraEntry);
						}
					} else if (evo.getType() == EvolutionType.LEVEL_ITEM_DAY) {
						int item = evo.getExtraInfo();
						// Make sure we don't already have an evo for the same item at night (e.g., when
						// using Change Impossible Evos)
						if (evo.getFrom().getEvolutionsFrom().stream()
								.noneMatch(e -> e.getType() == EvolutionType.LEVEL_ITEM_NIGHT && e.getExtraInfo() == item)) {
							// Add an extra evo for Level w/ Item During Night
							addEvoUpdateHeldItem(timeBasedEvolutionUpdates, evo, itemNames.get(item));
							Evolution extraEntry = new Evolution(evo.getFrom(), evo.getTo(), EvolutionType.LEVEL_ITEM_NIGHT,
									item);
							extraEvolutions.add(extraEntry);
						}
					} else if (evo.getType() == EvolutionType.LEVEL_ITEM_NIGHT) {
						int item = evo.getExtraInfo();
						// Make sure we don't already have an evo for the same item at day (e.g., when
						// using Change Impossible Evos)
						if (evo.getFrom().getEvolutionsFrom().stream()
								.noneMatch(e -> e.getType() == EvolutionType.LEVEL_ITEM_DAY && e.getExtraInfo() == item)) {
							// Add an extra evo for Level w/ Item During Day
							addEvoUpdateHeldItem(timeBasedEvolutionUpdates, evo, itemNames.get(item));
							Evolution extraEntry = new Evolution(evo.getFrom(), evo.getTo(), EvolutionType.LEVEL_ITEM_DAY,
									item);
							extraEvolutions.add(extraEntry);
						}
					}
				}
				pkmn.getEvolutionsFrom().addAll(extraEvolutions);
				for (Evolution ev : extraEvolutions) {
					ev.getTo().getEvolutionsTo().add(ev);
				}
			}
		}

	}

	@Override
	public boolean hasShopSupport() {
		return true;
	}

	@Override
	public Map<Integer, Shop> getShopItems() {
		List<String> shopNames = Gen4Constants.getShopNames(romEntry.getRomType());
		List<Integer> mainGameShops = Arrays.stream(romEntry.getArrayValue("MainGameShops")).boxed().collect(Collectors.toList());
		List<Integer> skipShops = Arrays.stream(romEntry.getArrayValue("SkipShops")).boxed().collect(Collectors.toList());
		int shopCount = romEntry.getIntValue("ShopCount");
		Map<Integer, Shop> shopItemsMap = new TreeMap<>();
		String shopDataPrefix = romEntry.getStringValue("ShopDataPrefix");
		int offset = find(arm9, shopDataPrefix);
		offset += shopDataPrefix.length() / 2;

		for (int i = 0; i < shopCount; i++) {
			if (!skipShops.contains(i)) {
				List<Integer> items = new ArrayList<>();
				int val = (FileFunctions.read2ByteInt(arm9, offset));
				while ((val & 0xFFFF) != 0xFFFF) {
					if (val != 0) {
						items.add(val);
					}
					offset += 2;
					val = (FileFunctions.read2ByteInt(arm9, offset));
				}
				offset += 2;
				Shop shop = new Shop();
				shop.items = items;
				shop.name = shopNames.get(i);
				shop.isMainGame = mainGameShops.contains(i);
				shopItemsMap.put(i, shop);
			} else {
				while ((FileFunctions.read2ByteInt(arm9, offset) & 0xFFFF) != 0xFFFF) {
					offset += 2;
				}
				offset += 2;
			}
		}
		return shopItemsMap;
	}

	@Override
	public void setShopItems(Map<Integer, Shop> shopItems) {
		int shopCount = romEntry.getIntValue("ShopCount");
		String shopDataPrefix = romEntry.getStringValue("ShopDataPrefix");
		int offset = find(arm9, shopDataPrefix);
		offset += shopDataPrefix.length() / 2;

		for (int i = 0; i < shopCount; i++) {
			Shop thisShop = shopItems.get(i);
			if (thisShop == null || thisShop.items == null) {
				while ((FileFunctions.read2ByteInt(arm9, offset) & 0xFFFF) != 0xFFFF) {
					offset += 2;
				}
				offset += 2;
				continue;
			}
			Iterator<Integer> iterItems = thisShop.items.iterator();
			int val = (FileFunctions.read2ByteInt(arm9, offset));
			while ((val & 0xFFFF) != 0xFFFF) {
				if (val != 0) {
					FileFunctions.write2ByteInt(arm9, offset, iterItems.next());
				}
				offset += 2;
				val = (FileFunctions.read2ByteInt(arm9, offset));
			}
			offset += 2;
		}
	}

	@Override
	public void setBalancedShopPrices() {
		try {
			// In Diamond and Pearl, item IDs 112 through 134 are unused. In Platinum and
			// HGSS, item ID 112 is used for
			// the Griseous Orb. So we need to skip through the unused IDs at different
			// points depending on the game.
			int startOfUnusedIDs = romEntry.getRomType() == Gen4Constants.Type_DP ? 112 : 113;
			NARCArchive itemPriceNarc = this.readNARC(romEntry.getFile("ItemData"));
			int itemID = 1;
			for (int i = 1; i < itemPriceNarc.files.size(); i++) {
				writeWord(itemPriceNarc.files.get(i), 0, Gen4Constants.balancedItemPrices.get(itemID) * 10);
				itemID++;
				if (itemID == startOfUnusedIDs) {
					itemID = 135;
				}
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
			byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
			if (pickupItemsTableOffset == 0) {
				int offset = find(battleOverlay, Gen4Constants.pickupTableLocator);
				if (offset > 0) {
					pickupItemsTableOffset = offset;
				}
			}

			// If we haven't found the pickup table for this ROM already, find it.
			if (rarePickupItemsTableOffset == 0) {
				int offset = find(battleOverlay, Gen4Constants.rarePickupTableLocator);
				if (offset > 0) {
					rarePickupItemsTableOffset = offset;
				}
			}

			// Assuming we've found the pickup table, extract the items out of it.
			if (pickupItemsTableOffset > 0 && rarePickupItemsTableOffset > 0) {
				for (int i = 0; i < Gen4Constants.numberOfCommonPickupItems; i++) {
					int itemOffset = pickupItemsTableOffset + (2 * i);
					int item = FileFunctions.read2ByteInt(battleOverlay, itemOffset);
					PickupItem pickupItem = new PickupItem(item);
					pickupItems.add(pickupItem);
				}
				for (int i = 0; i < Gen4Constants.numberOfRarePickupItems; i++) {
					int itemOffset = rarePickupItemsTableOffset + (2 * i);
					int item = FileFunctions.read2ByteInt(battleOverlay, itemOffset);
					PickupItem pickupItem = new PickupItem(item);
					pickupItems.add(pickupItem);
				}
			}

			// Assuming we got the items from the last step, fill out the probabilities.
			if (!pickupItems.isEmpty()) {
				for (int levelRange = 0; levelRange < 10; levelRange++) {
					int startingCommonItemOffset = levelRange;
					int startingRareItemOffset = 18 + levelRange;
					pickupItems.get(startingCommonItemOffset).probabilities[levelRange] = 30;
					for (int i = 1; i < 7; i++) {
						pickupItems.get(startingCommonItemOffset + i).probabilities[levelRange] = 10;
					}
					pickupItems.get(startingCommonItemOffset + 7).probabilities[levelRange] = 4;
					pickupItems.get(startingCommonItemOffset + 8).probabilities[levelRange] = 4;
					pickupItems.get(startingRareItemOffset).probabilities[levelRange] = 1;
					pickupItems.get(startingRareItemOffset + 1).probabilities[levelRange] = 1;
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
			if (pickupItemsTableOffset > 0 && rarePickupItemsTableOffset > 0) {
				byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
				Iterator<PickupItem> itemIterator = pickupItems.iterator();
				for (int i = 0; i < Gen4Constants.numberOfCommonPickupItems; i++) {
					int itemOffset = pickupItemsTableOffset + (2 * i);
					int item = itemIterator.next().item;
					FileFunctions.write2ByteInt(battleOverlay, itemOffset, item);
				}
				for (int i = 0; i < Gen4Constants.numberOfRarePickupItems; i++) {
					int itemOffset = rarePickupItemsTableOffset + (2 * i);
					int item = itemIterator.next().item;
					FileFunctions.write2ByteInt(battleOverlay, itemOffset, item);
				}
				writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	@Override
	public boolean canChangeTrainerText() {
		return true;
	}

	@Override
	public List<String> getTrainerNames() {
		List<String> tnames = new ArrayList<>(getStrings(romEntry.getIntValue("TrainerNamesTextOffset")));
		tnames.remove(0); // blank one
		for (int i = 0; i < tnames.size(); i++) {
			if (tnames.get(i).contains("\\and")) {
				tnames.set(i, tnames.get(i).replace("\\and", "&"));
			}
		}
		return tnames;
	}

	@Override
	public int maxTrainerNameLength() {
		return 10;// based off the english ROMs fixed
	}

	@Override
	public void setTrainerNames(List<String> trainerNames) {
		List<String> oldTNames = getStrings(romEntry.getIntValue("TrainerNamesTextOffset"));
		List<String> newTNames = new ArrayList<>(trainerNames);
		for (int i = 0; i < newTNames.size(); i++) {
			if (newTNames.get(i).contains("&")) {
				newTNames.set(i, newTNames.get(i).replace("&", "\\and"));
			}
		}
		newTNames.add(0, oldTNames.get(0)); // the 0-entry, preserve it

		// rewrite, only compressed if they were compressed before
		setStrings(romEntry.getIntValue("TrainerNamesTextOffset"), newTNames, lastStringsCompressed);

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
		return getStrings(romEntry.getIntValue("TrainerClassesTextOffset"));
	}

	@Override
	public void setTrainerClassNames(List<String> trainerClassNames) {
		setStrings(romEntry.getIntValue("TrainerClassesTextOffset"), trainerClassNames);
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
		return 2;
	}

	@Override
	public int highestAbilityIndex() {
		return Gen4Constants.highestAbilityIndex;
	}

	@Override
	public int internalStringLength(String string) {
		return string.length();
	}

	@Override
	public boolean setIntroPokemon(Species pk) {
		try {
			if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
				// This is a female-only Pokemon. Gen 4 has an annoying quirk where female-only Pokemon *need*
				// to pass a special parameter into the function that loads Pokemon sprites; the game will
				// softlock on native hardware otherwise. The way the compiler has optimized the intro Pokemon
				// code makes it very hard to modify, so passing in this special parameter is difficult. Rather
				// than attempt to patch this code, just report back that this Pokemon won't do.
				if (pk.getGenderRatio() == 0xFE) {
					return false;
				}
				// Assume alt formes can't be used. I haven't actually tested this, but it seemed like the safer guess.
				if (pk.getBaseForme() != null) {
					return false;
				}
				byte[] introOverlay = readOverlay(romEntry.getIntValue("IntroOvlNumber"));
				for (String prefix : Gen4Constants.dpptIntroPrefixes) {
					int offset = find(introOverlay, prefix);
					if (offset > 0) {
						offset += prefix.length() / 2; // because it was a prefix
						writeWord(introOverlay, offset, pk.getNumber());
					}
				}
				writeOverlay(romEntry.getIntValue("IntroOvlNumber"), introOverlay);
			} else if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
				// In HGSS, Ethan/Lyra's Marill is changed instead of the intro Pokemon.
				// This is really cool, but there *is* an actual intro Pokemon to randomize,
				// and it is a Marill to boot too! Ideally, Ethan/Lyra's Marill should be
				// in addition to the intro one, not instead of.
				if (pk.getBaseForme() != null) {
					return false;
				}
				int spriteID = Gen4Constants.getOverworldSpriteIDOfSpecies(pk.getNumber());

				byte[] fieldOverlay = readOverlay(romEntry.getIntValue("FieldOvlNumber"));
				String prefix = Gen4Constants.lyraEthanMarillSpritePrefix;
				int offset = find(fieldOverlay, prefix);
				if (offset > 0) {
					offset += prefix.length() / 2; // because it was a prefix
					writeWord(fieldOverlay, offset, spriteID);
					if (Gen4Constants.hgssBigOverworldPokemon.contains(pk.getNumber())) {
						// Write the constant to indicate it's big (0x208 | (20 << 10))
						writeWord(fieldOverlay, offset + 2, 0x5208);
					} else {
						// Write the constant to indicate it's normal-sized (0x227 | (19 << 10))
						writeWord(fieldOverlay, offset + 2, 0x4E27);
					}
				}
				writeOverlay(romEntry.getIntValue("FieldOvlNumber"), fieldOverlay);

				// Now modify the Marill's cry in every script it appears in to ensure consistency
				for (InFileEntry entry : romEntry.getMarillCryScriptEntries()) {
					byte[] script = scriptNarc.files.get(entry.getFile());
					writeWord(script, entry.getOffset(), pk.getNumber());
					scriptNarc.files.set(entry.getFile(), script);
				}

				// Modify the text too for additional consistency
				int[] textOffsets = romEntry.getArrayValue("MarillTextFiles");
				String originalSpeciesString = pokes[SpeciesIDs.marill].getName().toUpperCase();
				String newSpeciesString = pk.getName();
				Map<String, String> replacements = new TreeMap<>();
				replacements.put(originalSpeciesString, newSpeciesString);
				for (int textOffset : textOffsets) {
					replaceAllStringsInEntry(textOffset, replacements);
				}

				// Lastly, modify the catching tutorial to use the new Pokemon if we're capable
				// of doing so
				if (romEntry.hasTweakFile("NewCatchingTutorialSubroutineTweak")) {
					String catchingTutorialMonTablePrefix = romEntry.getStringValue("CatchingTutorialMonTablePrefix");
					offset = find(arm9, catchingTutorialMonTablePrefix);
					if (offset > 0) {
						offset += catchingTutorialMonTablePrefix.length() / 2; // because it was a prefix

						// As part of our catching tutorial patch, the player Pokemon's ID is just
						// pc-relative
						// loaded, and offset is now pointing to it.
						writeWord(arm9, offset, pk.getNumber());
					}
				}
			}
		} catch (IOException e) {
			throw new RomIOException(e);
		}
		return true;
	}

	@Override
	public ItemList getAllowedItems() {
		return allowedItems;
	}

	@Override
	public ItemList getNonBadItems() {
		return nonBadItems;
	}

	@Override
	public List<Integer> getUniqueNoSellItems() {
		return new ArrayList<>();
	}

	@Override
	public List<Integer> getRegularShopItems() {
		return Gen4Constants.regularShopItems;
	}

	@Override
	public List<Integer> getOPShopItems() {
		return Gen4Constants.opShopItems;
	}

	@Override
	public String[] getItemNames() {
		return itemNames.toArray(new String[0]);
	}

	@Override
	public String abilityName(int number) {
		return abilityNames.get(number);
	}

	@Override
	public Map<Integer, List<Integer>> getAbilityVariations() {
		return Gen4Constants.abilityVariations;
	}

	@Override
	public List<Integer> getUselessAbilities() {
		return new ArrayList<>(Gen4Constants.uselessAbilities);
	}

	@Override
	public int getAbilityForTrainerPokemon(TrainerPokemon tp) {
		// In Gen 4, alt formes for Trainer Pokemon use the base forme's ability
		Species pkmn = tp.species;
		while (pkmn.getBaseForme() != null) {
			pkmn = pkmn.getBaseForme();
		}

		if (romEntry.getRomType() == Gen4Constants.Type_DP || romEntry.getRomType() == Gen4Constants.Type_Plat) {
			// In DPPt, Trainer Pokemon *always* use the first Ability, no matter what
			return pkmn.getAbility1();
		} else {
			// In HGSS, Trainer Pokemon can specify which ability they want to use.
			return tp.abilitySlot == 2 ? pkmn.getAbility2() : pkmn.getAbility1();
		}
	}

	@Override
	public boolean hasMegaEvolutions() {
		return false;
	}

	private List<Integer> getFieldItems() {
		List<Integer> fieldItems = new ArrayList<>();
		// normal items
		int scriptFile = romEntry.getIntValue("ItemBallsScriptOffset");
		byte[] itemScripts = scriptNarc.files.get(scriptFile);
		int offset = 0;
		int skipTableOffset = 0;
		int[] skipTable = romEntry.getArrayValue("ItemBallsSkip");
		int setVar = romEntry.getRomType() == Gen4Constants.Type_HGSS ? Gen4Constants.hgssSetVarScript
				: Gen4Constants.dpptSetVarScript;
		while (true) {
			int part1 = readWord(itemScripts, offset);
			if (part1 == Gen4Constants.scriptListTerminator) {
				// done
				break;
			}
			int offsetInFile = readRelativePointer(itemScripts, offset);
			offset += 4;
			if (skipTableOffset < skipTable.length && (skipTable[skipTableOffset] == (offset / 4) - 1)) {
				skipTableOffset++;
				continue;
			}
			int command = readWord(itemScripts, offsetInFile);
			int variable = readWord(itemScripts, offsetInFile + 2);
			if (command == setVar && variable == Gen4Constants.itemScriptVariable) {
				int item = readWord(itemScripts, offsetInFile + 4);
				fieldItems.add(item);
			}

		}

		// hidden items
		int hiTableOffset = romEntry.getIntValue("HiddenItemTableOffset");
		int hiTableLimit = romEntry.getIntValue("HiddenItemCount");
		for (int i = 0; i < hiTableLimit; i++) {
			int item = readWord(arm9, hiTableOffset + i * 8);
			fieldItems.add(item);
		}

		return fieldItems;
	}

	private void setFieldItems(List<Integer> fieldItems) {
		Iterator<Integer> iterItems = fieldItems.iterator();

		// normal items
		int scriptFile = romEntry.getIntValue("ItemBallsScriptOffset");
		byte[] itemScripts = scriptNarc.files.get(scriptFile);
		int offset = 0;
		int skipTableOffset = 0;
		int[] skipTable = romEntry.getArrayValue("ItemBallsSkip");
		int setVar = romEntry.getRomType() == Gen4Constants.Type_HGSS ? Gen4Constants.hgssSetVarScript
				: Gen4Constants.dpptSetVarScript;
		while (true) {
			int part1 = readWord(itemScripts, offset);
			if (part1 == Gen4Constants.scriptListTerminator) {
				// done
				break;
			}
			int offsetInFile = readRelativePointer(itemScripts, offset);
			offset += 4;
			if (skipTableOffset < skipTable.length && (skipTable[skipTableOffset] == (offset / 4) - 1)) {
				skipTableOffset++;
				continue;
			}
			int command = readWord(itemScripts, offsetInFile);
			int variable = readWord(itemScripts, offsetInFile + 2);
			if (command == setVar && variable == Gen4Constants.itemScriptVariable) {
				int item = iterItems.next();
				writeWord(itemScripts, offsetInFile + 4, item);
			}
		}

		// hidden items
		int hiTableOffset = romEntry.getIntValue("HiddenItemTableOffset");
		int hiTableLimit = romEntry.getIntValue("HiddenItemCount");
		for (int i = 0; i < hiTableLimit; i++) {
			int item = iterItems.next();
			writeWord(arm9, hiTableOffset + i * 8, item);
		}
	}

	@Override
	public List<Integer> getRequiredFieldTMs() {
		if (romEntry.getRomType() == Gen4Constants.Type_DP) {
			return Gen4Constants.dpRequiredFieldTMs;
		} else if (romEntry.getRomType() == Gen4Constants.Type_Plat) {
			// same as DP just we have to keep the weather TMs
			return Gen4Constants.ptRequiredFieldTMs;
		}
		return new ArrayList<>();
	}

	@Override
	public List<Integer> getCurrentFieldTMs() {
		List<Integer> fieldItems = this.getFieldItems();
		List<Integer> fieldTMs = new ArrayList<>();

		for (int item : fieldItems) {
			if (Gen4Constants.allowedItems.isTM(item)) {
				fieldTMs.add(item - Gen4Constants.tmItemOffset + 1);
			}
		}

		return fieldTMs;
	}

	@Override
	public void setFieldTMs(List<Integer> fieldTMs) {
		List<Integer> fieldItems = this.getFieldItems();
		int fiLength = fieldItems.size();
		Iterator<Integer> iterTMs = fieldTMs.iterator();

		for (int i = 0; i < fiLength; i++) {
			int oldItem = fieldItems.get(i);
			if (Gen4Constants.allowedItems.isTM(oldItem)) {
				int newItem = iterTMs.next() + Gen4Constants.tmItemOffset - 1;
				fieldItems.set(i, newItem);
			}
		}

		this.setFieldItems(fieldItems);
	}

	@Override
	public List<Integer> getRegularFieldItems() {
		List<Integer> fieldItems = this.getFieldItems();
		List<Integer> fieldRegItems = new ArrayList<>();

		for (int item : fieldItems) {
			if (Gen4Constants.allowedItems.isAllowed(item) && !(Gen4Constants.allowedItems.isTM(item))) {
				fieldRegItems.add(item);
			}
		}

		return fieldRegItems;
	}

	@Override
	public void setRegularFieldItems(List<Integer> items) {
		List<Integer> fieldItems = this.getFieldItems();
		int fiLength = fieldItems.size();
		Iterator<Integer> iterNewItems = items.iterator();

		for (int i = 0; i < fiLength; i++) {
			int oldItem = fieldItems.get(i);
			if (!(Gen4Constants.allowedItems.isTM(oldItem)) && Gen4Constants.allowedItems.isAllowed(oldItem)) {
				int newItem = iterNewItems.next();
				fieldItems.set(i, newItem);
			}
		}

		this.setFieldItems(fieldItems);
	}

	@Override
	public List<IngameTrade> getIngameTrades() {
		List<IngameTrade> trades = new ArrayList<>();
		try {
			NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
			int[] spTrades = romEntry.getArrayValue("StaticPokemonTrades");
			List<String> tradeStrings = getStrings(romEntry.getIntValue("IngameTradesTextOffset"));
			int tradeCount = tradeNARC.files.size();
			for (int i = 0; i < tradeCount; i++) {
				boolean isSP = false;
				for (int spTrade : spTrades) {
					if (spTrade == i) {
						isSP = true;
						break;
					}
				}
				if (isSP) {
					continue;
				}
				byte[] tfile = tradeNARC.files.get(i);
				IngameTrade trade = new IngameTrade();
				trade.nickname = tradeStrings.get(i);
				trade.givenSpecies = pokes[readLong(tfile, 0)];
				trade.ivs = new int[6];
				for (int iv = 0; iv < 6; iv++) {
					trade.ivs[iv] = readLong(tfile, 4 + iv * 4);
				}
				trade.otId = readWord(tfile, 0x20);
				trade.otName = tradeStrings.get(i + tradeCount);
				trade.item = readLong(tfile, 0x3C);
				trade.requestedSpecies = pokes[readLong(tfile, 0x4C)];
				trades.add(trade);
			}
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}
		return trades;
	}

	@Override
	public void setIngameTrades(List<IngameTrade> trades) {
		int tradeOffset = 0;
		List<IngameTrade> oldTrades = this.getIngameTrades();
		try {
			NARCArchive tradeNARC = this.readNARC(romEntry.getFile("InGameTrades"));
			int[] spTrades = romEntry.getArrayValue("StaticPokemonTrades");
			List<String> tradeStrings = getStrings(romEntry.getIntValue("IngameTradesTextOffset"));
			int tradeCount = tradeNARC.files.size();
			for (int i = 0; i < tradeCount; i++) {
				boolean isSP = false;
				for (int spTrade : spTrades) {
					if (spTrade == i) {
						isSP = true;
						break;
					}
				}
				if (isSP) {
					continue;
				}
				byte[] tfile = tradeNARC.files.get(i);
				IngameTrade trade = trades.get(tradeOffset++);
				tradeStrings.set(i, trade.nickname);
				tradeStrings.set(i + tradeCount, trade.otName);
				writeLong(tfile, 0, trade.givenSpecies.getNumber());
				for (int iv = 0; iv < 6; iv++) {
					writeLong(tfile, 4 + iv * 4, trade.ivs[iv]);
				}
				writeWord(tfile, 0x20, trade.otId);
				writeLong(tfile, 0x3C, trade.item);
				writeLong(tfile, 0x4C, trade.requestedSpecies.getNumber());
				if (tfile.length > 0x50) {
					writeLong(tfile, 0x50, 0); // disable gender
				}
			}
			this.writeNARC(romEntry.getFile("InGameTrades"), tradeNARC);
			this.setStrings(romEntry.getIntValue("IngameTradesTextOffset"), tradeStrings);
			// update what the people say when they talk to you
			int[] textOffsets = romEntry.getArrayValue("IngameTradePersonTextOffsets");
			for (int trade = 0; trade < textOffsets.length; trade++) {
				if (textOffsets[trade] > 0) {
					if (trade >= oldTrades.size() || trade >= trades.size()) {
						break;
					}
					IngameTrade oldTrade = oldTrades.get(trade);
					IngameTrade newTrade = trades.get(trade);
					Map<String, String> replacements = new TreeMap<>();
					replacements.put(oldTrade.givenSpecies.getName().toUpperCase(),
							newTrade.givenSpecies.getName());
					if (oldTrade.requestedSpecies != newTrade.requestedSpecies) {
						replacements.put(oldTrade.requestedSpecies.getName().toUpperCase(),
								newTrade.requestedSpecies.getName());
					}
					replaceAllStringsInEntry(textOffsets[trade], replacements);
					// hgss override for one set of strings that appears 2x
					if (romEntry.getRomType() == Gen4Constants.Type_HGSS && trade == 6) {
						replaceAllStringsInEntry(textOffsets[trade] + 1, replacements);
					}
				}
			}
		} catch (IOException ex) {
			throw new RomIOException(ex);
		}
	}

	private void replaceAllStringsInEntry(int entry, Map<String, String> replacements) {
		// This function currently only replaces move and Pokemon names, and we don't
		// want them
		// split across multiple lines if there is a space.
		replacements.replaceAll((key, oldValue) -> oldValue.replace(' ', '_'));
		int lineLength = Gen4Constants.getTextCharsPerLine(romEntry.getRomType());
		List<String> strings = this.getStrings(entry);
		for (int strNum = 0; strNum < strings.size(); strNum++) {
			String oldString = strings.get(strNum);
			boolean needsReplacement = false;
			for (Map.Entry<String, String> replacement : replacements.entrySet()) {
				if (oldString.contains(replacement.getKey())) {
					needsReplacement = true;
					break;
				}
			}
			if (needsReplacement) {
				String newString = RomFunctions.formatTextWithReplacements(oldString, replacements, "\\n", "\\l", "\\p",
						lineLength, ssd);
				newString = newString.replace('_', ' ');
				strings.set(strNum, newString);
			}
		}
		this.setStrings(entry, strings);
	}

	@Override
	public boolean hasDVs() {
		return false;
	}

	@Override
	public int generationOfPokemon() {
		return 4;
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
			byte[] babyPokes = readFile(romEntry.getFile("BabyPokemon"));
			// baby pokemon
			for (int i = 1; i <= Gen4Constants.pokemonCount; i++) {
				Species baby = pokes[i];
				while (!baby.getEvolutionsTo().isEmpty()) {
					// Grab the first "to evolution" even if there are multiple
					baby = baby.getEvolutionsTo().get(0).getFrom();
				}
				writeWord(babyPokes, i * 2, baby.getNumber());
			}
			// finish up
			writeFile(romEntry.getFile("BabyPokemon"), babyPokes);
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
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			return Gen4Constants.hgssFieldMoves;
		} else {
			return Gen4Constants.dpptFieldMoves;
		}
	}

	@Override
	public List<Integer> getEarlyRequiredHMMoves() {
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			return Gen4Constants.hgssEarlyRequiredHMMoves;
		} else {
			return Gen4Constants.dpptEarlyRequiredHMMoves;
		}
	}

    @Override
    public int miscTweaksAvailable() {
        int available = MiscTweak.LOWER_CASE_POKEMON_NAMES.getValue();
        available |= MiscTweak.RANDOMIZE_CATCHING_TUTORIAL.getValue();
        if (romEntry.hasTweakFile("FastestTextTweak")) {
            available |= MiscTweak.FASTEST_TEXT.getValue();
        }
        available |= MiscTweak.BAN_LUCKY_EGG.getValue();
        if (romEntry.hasTweakFile("NationalDexAtStartTweak")) {
            available |= MiscTweak.NATIONAL_DEX_AT_START.getValue();
        }
        available |= MiscTweak.RUN_WITHOUT_RUNNING_SHOES.getValue();
        available |= MiscTweak.FASTER_HP_AND_EXP_BARS.getValue();
        if (romEntry.hasTweakFile("FastDistortionWorldTweak")) {
            available |= MiscTweak.FAST_DISTORTION_WORLD.getValue();
        }
        if (romEntry.getRomType() == Gen4Constants.Type_Plat || romEntry.getRomType() == Gen4Constants.Type_HGSS) {
            available |= MiscTweak.UPDATE_ROTOM_FORME_TYPING.getValue();
        }
		if (romEntry.getIntValue("TMMovesReusableFunctionOffset") != 0) {
			available |= MiscTweak.REUSABLE_TMS.getValue();
		}
       // if (romEntry.getArrayValue("HMMovesReusableFunctionOffsets").length != 0) {
            available |= MiscTweak.FORGETTABLE_HMS.getValue();
        //}
        return available;
    }

    @Override
    public void applyMiscTweak(MiscTweak tweak) {
        if (tweak == MiscTweak.LOWER_CASE_POKEMON_NAMES) {
            applyCamelCaseNames();
        } else if (tweak == MiscTweak.FASTEST_TEXT) {
            applyFastestText();
        } else if (tweak == MiscTweak.BAN_LUCKY_EGG) {
            allowedItems.banSingles(ItemIDs.luckyEgg);
            nonBadItems.banSingles(ItemIDs.luckyEgg);
        } else if (tweak == MiscTweak.NATIONAL_DEX_AT_START) {
            patchForNationalDex();
        } else if (tweak == MiscTweak.RUN_WITHOUT_RUNNING_SHOES) {
            applyRunWithoutRunningShoesPatch();
        } else if (tweak == MiscTweak.FASTER_HP_AND_EXP_BARS) {
            patchFasterBars();
        } else if (tweak == MiscTweak.FAST_DISTORTION_WORLD) {
            applyFastDistortionWorld();
        } else if (tweak == MiscTweak.UPDATE_ROTOM_FORME_TYPING) {
            updateRotomFormeTyping();
        } else if (tweak == MiscTweak.REUSABLE_TMS) {
			applyReusableTMsPatch();
		} else if (tweak == MiscTweak.FORGETTABLE_HMS) {
            applyForgettableHMsPatch();
        }
    }

	private void applyFastestText() {
		genericIPSPatch(arm9, "FastestTextTweak");
	}

	private void patchForNationalDex() {
		byte[] pokedexScript = scriptNarc.files.get(romEntry.getIntValue("NationalDexScriptOffset"));

		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			// Our patcher breaks if the output file is larger than the input file. For
			// HGSS, we want
			// to expand the script by four bytes to add an instruction to enable the
			// national dex. Thus,
			// the IPS patch was created with us adding four 0x00 bytes to the end of the
			// script in mind.
			byte[] expandedPokedexScript = new byte[pokedexScript.length + 4];
			System.arraycopy(pokedexScript, 0, expandedPokedexScript, 0, pokedexScript.length);
			pokedexScript = expandedPokedexScript;
		}
		genericIPSPatch(pokedexScript, "NationalDexAtStartTweak");
		scriptNarc.files.set(romEntry.getIntValue("NationalDexScriptOffset"), pokedexScript);
	}

	private void applyRunWithoutRunningShoesPatch() {
		String prefix = Gen4Constants.getRunWithoutRunningShoesPrefix(romEntry.getRomType());
		int offset = find(arm9, prefix);
		if (offset != 0) {
			// The prefix starts 0xE bytes from what we want to patch because what comes
			// between is region and revision dependent. To start running, the game checks:
			// 1. That you're holding the B button
			// 2. That the FLAG_SYS_B_DASH flag is set (aka, you've acquired Running Shoes)
			// For #2, if the flag is unset, it jumps to a different part of the
			// code to make you walk instead. This simply nops out this jump so the
			// game stops caring about the FLAG_SYS_B_DASH flag entirely.
			writeWord(arm9, offset + 0xE, 0);
		}
	}

	private void patchFasterBars() {
		// To understand what this code is patching, take a look at the CalcNewBarValue
		// and MoveBattleBar functions in this file from the Emerald decompilation:
		// https://github.com/pret/pokeemerald/blob/master/src/battle_interface.c
		// The code in Gen 4 is almost identical outside of one single constant; the
		// reason the bars scroll slower is because Gen 4 runs at 30 FPS instead of 60.
		try {
			byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
			int offset = find(battleOverlay, Gen4Constants.hpBarSpeedPrefix);
			if (offset > 0) {
				offset += Gen4Constants.hpBarSpeedPrefix.length() / 2; // because it was a prefix
				// For the HP bar, the original game passes 1 for the toAdd parameter of
				// CalcNewBarValue.
				// We want to pass 2 instead, so we simply change the mov instruction at offset.
				battleOverlay[offset] = 0x02;
			}

			offset = find(battleOverlay, Gen4Constants.expBarSpeedPrefix);
			if (offset > 0) {
				offset += Gen4Constants.expBarSpeedPrefix.length() / 2; // because it was a prefix
				// For the EXP bar, the original game passes expFraction for the toAdd
				// parameter. The
				// game calculates expFraction by doing a division, and to do *that*, it has to
				// load
				// receivedValue into r0 so it can call the division function with it as the
				// first
				// parameter. It gets the value from r6 like so:
				// add r0, r6, #0
				// Since we ultimately want toAdd (and thus expFraction) to be doubled, we can
				// double
				// receivedValue when it gets loaded into r0 by tweaking the add to be:
				// add r0, r6, r6
				battleOverlay[offset] = (byte) 0xB0;
				battleOverlay[offset + 1] = 0x19;
			}

			offset = find(battleOverlay, Gen4Constants.bothBarsSpeedPrefix);
			if (offset > 0) {
				offset += Gen4Constants.bothBarsSpeedPrefix.length() / 2; // because it was a prefix
				// For both HP and EXP bars, a different set of logic is used when the maxValue
				// has
				// fewer pixels than the whole bar; this logic ignores the toAdd parameter
				// entirely and
				// calculates its *own* toAdd by doing maxValue << 8 / scale. If we instead do
				// maxValue << 9, the new toAdd becomes doubled as well.
				battleOverlay[offset] = 0x40;
			}

			writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);

		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	@Override
	public boolean setCatchingTutorial(Species opponent, Species player) {
		int opponentOffset = romEntry.getIntValue("CatchingTutorialOpponentMonOffset");

		if (romEntry.hasTweakFile("NewCatchingTutorialSubroutineTweak")) {
			String catchingTutorialMonTablePrefix = romEntry.getStringValue("CatchingTutorialMonTablePrefix");
			int offset = find(arm9, catchingTutorialMonTablePrefix);
			if (offset > 0) {
				offset += catchingTutorialMonTablePrefix.length() / 2; // because it was a prefix

				// The player's mon is randomized as part of randomizing Lyra/Ethan's Pokemon
				// (see randomizeIntroPokemon), so we just care about the enemy mon. As part of our
				// catching tutorial patch, the player and enemy species IDs are pc-relative loaded, with
				// the enemy ID occurring right after the player ID (which is what offset is pointing to).
				writeWord(arm9, offset + 4, opponent.getNumber());
			}
		} else if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			// For non-US HGSS, just handle it in the old-school way.
			// Can randomize both Pokemon, but both limited to 1-255.
			// Make sure to raise the level of Lyra/Ethan's Pokemon to 10 to prevent softlocks.
			int playerOffset = romEntry.getIntValue("CatchingTutorialPlayerMonOffset");
			int levelOffset = romEntry.getIntValue("CatchingTutorialPlayerLevelOffset");
			if (opponent.getNumber() > 255 || player.getNumber() > 255) {
				return false;
			}
			arm9[opponentOffset] = (byte) opponent.getNumber();
			arm9[playerOffset] = (byte) player.getNumber();
			arm9[levelOffset] = 10;
		} else {
			// DPPt only supports randomizing the opponent, but enough space for any mon
			if (opponent != null) {
				writeLong(arm9, opponentOffset, opponent.getNumber());
			}
		}

		return true;
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
			TypeTable typeTable = new TypeTable(Type.getAllTypes(4));

			byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
			int currentOffset = romEntry.getIntValue("TypeEffectivenessOffset");

			int attackingType = battleOverlay[currentOffset];
			while (attackingType != Gen4Constants.typeTableTerminator) {
				if (battleOverlay[currentOffset] != Gen4Constants.typeTableForesightTerminator) {
					int defendingType = battleOverlay[currentOffset + 1];
					int effectivenessInternal = battleOverlay[currentOffset + 2];
					Type attacking = Gen4Constants.typeTable[attackingType];
					Type defending = Gen4Constants.typeTable[defendingType];
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
				attackingType = battleOverlay[currentOffset];
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
		if (typeTable.nonNeutralEffectivenessCount() > Gen4Constants.nonNeutralEffectivenessCount) {
			throw new IllegalArgumentException("Too many non-neutral Effectiveness-es. Was "
					+ typeTable.nonNeutralEffectivenessCount() + ", has to be at most " +
					Gen4Constants.nonNeutralEffectivenessCount);
		}
		try {
			byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
			int tableOffset = romEntry.getIntValue("TypeEffectivenessOffset");

			ByteArrayOutputStream mainPart = new ByteArrayOutputStream();
			ByteArrayOutputStream ghostImmunities = new ByteArrayOutputStream();

			prepareTypeTableParts(typeTable, mainPart, ghostImmunities);
			writeTypeTableParts(battleOverlay, tableOffset, mainPart, ghostImmunities);

			writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);
		} catch (IOException e) {
			throw new RomIOException(e);
		}
	}

	private void prepareTypeTableParts(TypeTable typeTable, ByteArrayOutputStream mainPart, ByteArrayOutputStream ghostImmunities) {
		for (Type attacker : typeTable.getTypes()) {
			for (Type defender : typeTable.getTypes()) {
				Effectiveness eff = typeTable.getEffectiveness(attacker, defender);
				if (eff != Effectiveness.NEUTRAL) {
					ByteArrayOutputStream part = (defender == Type.GHOST && eff == Effectiveness.ZERO)
							? ghostImmunities : mainPart;
					byte effectivenessInternal;
					switch (eff) {
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
					part.write(Gen4Constants.typeToByte(attacker));
					part.write(Gen4Constants.typeToByte(defender));
					part.write(effectivenessInternal);
				}
			}
		}
	}

	private void writeTypeTableParts(byte[] battleOverlay, int tableOffset, ByteArrayOutputStream mainPart,
									 ByteArrayOutputStream ghostImmunities) {
		writeBytes(battleOverlay, tableOffset, mainPart.toByteArray());
		tableOffset += mainPart.size();

		writeBytes(battleOverlay, tableOffset, new byte[] {Gen4Constants.typeTableForesightTerminator,
				Gen4Constants.typeTableForesightTerminator, (byte) 0x00});
		tableOffset += 3;

		writeBytes(battleOverlay, tableOffset, ghostImmunities.toByteArray());
		tableOffset += ghostImmunities.size();

		writeBytes(battleOverlay, tableOffset, new byte[] {Gen4Constants.typeTableTerminator,
				Gen4Constants.typeTableTerminator, (byte) 0x00});
	}

	private void applyFastDistortionWorld() {
		byte[] spearPillarPortalScript = scriptNarc.files.get(Gen4Constants.ptSpearPillarPortalScriptFile);
		byte[] expandedSpearPillarPortalScript = new byte[spearPillarPortalScript.length + 12];
		System.arraycopy(spearPillarPortalScript, 0, expandedSpearPillarPortalScript, 0,
				spearPillarPortalScript.length);
		spearPillarPortalScript = expandedSpearPillarPortalScript;
		genericIPSPatch(spearPillarPortalScript, "FastDistortionWorldTweak");
		scriptNarc.files.set(Gen4Constants.ptSpearPillarPortalScriptFile, spearPillarPortalScript);
	}

    private void updateRotomFormeTyping() {
        pokes[SpeciesIDs.Gen4Formes.rotomH].setSecondaryType(Type.FIRE);
        pokes[SpeciesIDs.Gen4Formes.rotomW].setSecondaryType(Type.WATER);
        pokes[SpeciesIDs.Gen4Formes.rotomFr].setSecondaryType(Type.ICE);
        pokes[SpeciesIDs.Gen4Formes.rotomFa].setSecondaryType(Type.FLYING);
        pokes[SpeciesIDs.Gen4Formes.rotomM].setSecondaryType(Type.GRASS);
    }

	private void applyReusableTMsPatch() {
		// don't know exactly how this works, but it does
		// credits to Mikelan98 for finding the method/locations to change
		int offset = romEntry.getIntValue("TMMovesReusableFunctionOffset");
		if (offset == 0) {
			return;
		}
		if (arm9[offset] != Gen4Constants.tmsReusableByteBefore) {
			throw new RuntimeException("Expected 0x" + Integer.toHexString(Gen4Constants.tmsReusableByteBefore)
					+ ", was 0x" + Integer.toHexString(arm9[offset]) + ". Likely TMMovesReusableFunctionOffset is faulty.");
		}
		arm9[offset] = Gen4Constants.tmsReusableByteAfter;
	}

    private void applyForgettableHMsPatch() {
        int[] offsets = romEntry.getArrayValue("HMMovesForgettableFunctionOffsets");
        int expectedOffsetsLength = new int[]{2, 2, 3}[romEntry.getRomType()];
  //      if (offsets.length != expectedOffsetsLength) {
   //         throw new RuntimeException("Unexpected length of HMMovesForgettableFunctionOffsets array. Expected "
   //                 + expectedOffsetsLength + ", was " + offsets.length + ".");
  //      }
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			try {
				// TODO: test
				// Currently level-up moves can replace HMs, but not TMs
				// (do tutors need to be tested separately??)
				// TODO: generalize
				// In-battle:
				// Overlay 8
				// 01 F0 FB F9 01 28 -> C0 46 00 20 01 28
				byte[] ol = readOverlay(8);
				int olOffset = find(ol, "01 F0 FB F9 01 28");
				writeBytes(ol, olOffset, RomFunctions.hexToBytes("C0 46 00 20 01 28"));
				writeOverlay(8, ol);
				// Overworld:
				// ARM9
				// EE F7 BC FF 01 28 -> C0 46 00 20 01 28
				// EE F7 53 FF 01 28 -> C0 46 00 20 01 28
				byte[] arm = readARM9();
				int armOffset1 = find(arm, "EE F7 BC FF 01 28");
				writeBytes(arm, armOffset1, RomFunctions.hexToBytes("C0 46 00 20 01 28"));
				int armOffset2 = find(arm, "EE F7 53 FF 01 28");
				writeBytes(arm, armOffset2, RomFunctions.hexToBytes("C0 46 00 20 01 28"));
				writeARM9(arm);
			} catch (IOException e) {
				throw new RomIOException(e);
			}
		} else {
			try {
				// TODO: test
				// TODO: generalize
				// In-battle:
				// Overlay 13
				// 01 F0 35 FA 01 28 -> C0 46 00 20 01 28
				byte[] ol = readOverlay(13);
				int olOffset = find(ol, "01 F0 35 FA 01 28");
				writeBytes(ol, olOffset, RomFunctions.hexToBytes("C0 46 00 20 01 28"));
				writeOverlay(13, ol);
				// Overworld:
				// ARM9
				// F0 F7 5B FA 01 28 -> C0 46 00 20 01 28
				byte[] arm = readARM9();
				int armOffset = find(arm, "F0 F7 5B FA 01 28");
				writeBytes(arm, armOffset, RomFunctions.hexToBytes("C0 46 00 20 01 28"));
				writeARM9(arm);
			} catch (IOException e) {
				throw new RomIOException(e);
			}
		}
    }

	@Override
    public void enableGuaranteedPokemonCatching() {
        try {
            byte[] battleOverlay = readOverlay(romEntry.getIntValue("BattleOvlNumber"));
            int offset = find(battleOverlay, Gen4Constants.perfectOddsBranchLocator);
            if (offset > 0) {
                // In Cmd_handleballthrow (name taken from pokeemerald decomp), the middle of the function checks
                // if the odds of catching a Pokemon is greater than 254; if it is, then the Pokemon is automatically
                // caught. In ASM, this is represented by:
                // cmp r1, #0xFF
                // bcc oddsLessThanOrEqualTo254
                // The below code just nops these two instructions so that we *always* act like our odds are 255,
                // and Pokemon are automatically caught no matter what.
                battleOverlay[offset] = 0x00;
                battleOverlay[offset + 1] = 0x00;
                battleOverlay[offset + 2] = 0x00;
                battleOverlay[offset + 3] = 0x00;
                writeOverlay(romEntry.getIntValue("BattleOvlNumber"), battleOverlay);
            }
        } catch (IOException e) {
            throw new RomIOException(e);
        }
    }
	@Override
	public void applyCorrectStaticMusic(Map<Integer, Integer> specialMusicStaticChanges) {
		List<Integer> replaced = new ArrayList<>();
		String newIndexToMusicPrefix;
		int newIndexToMusicPoolOffset;
		if (romEntry.getRomType() == Gen4Constants.Type_HGSS) {
			newIndexToMusicPrefix = romEntry.getStringValue("IndexToMusicPrefix");
			newIndexToMusicPoolOffset = find(arm9, newIndexToMusicPrefix);
			if (newIndexToMusicPoolOffset > 0) {
				newIndexToMusicPoolOffset += newIndexToMusicPrefix.length() / 2;

				for (int oldStatic : specialMusicStaticChanges.keySet()) {
					int i = newIndexToMusicPoolOffset;
					int indexEtc = readWord(arm9, i);
					int index = indexEtc & 0x3FF;
					while (index != oldStatic || replaced.contains(i)) {
						i += 2;
						indexEtc = readWord(arm9, i);
						index = indexEtc & 0x3FF;
					}
					int newIndexEtc = specialMusicStaticChanges.get(oldStatic) | (indexEtc & 0xFC00);
					writeWord(arm9, i, newIndexEtc);
					replaced.add(i);
				}
			}
		} else {
			int extendBy = romEntry.getIntValue("Arm9ExtensionSize");
			arm9 = extendARM9(arm9, extendBy, romEntry.getStringValue("TCMCopyingPrefix"), Gen4Constants.arm9Offset);
			genericIPSPatch(arm9, "NewIndexToMusicTweak");
			newIndexToMusicPrefix = romEntry.getStringValue("NewIndexToMusicPrefix");
			newIndexToMusicPoolOffset = find(arm9, newIndexToMusicPrefix);
			newIndexToMusicPoolOffset += newIndexToMusicPrefix.length() / 2;
			for (int oldStatic : specialMusicStaticChanges.keySet()) {
				int i = newIndexToMusicPoolOffset;
				int index = readWord(arm9, i);
				while (index != oldStatic || replaced.contains(i)) {
					i += 4;
					index = readWord(arm9, i);
				}
				writeWord(arm9, i, specialMusicStaticChanges.get(oldStatic));
				replaced.add(i);
			}
		}
	}

	@Override
	public boolean hasStaticMusicFix() {
		return romEntry.hasTweakFile("NewIndexToMusicTweak") || romEntry.getRomType() == Gen4Constants.Type_HGSS;
	}

	private boolean genericIPSPatch(byte[] data, String ctName) {
		if (!romEntry.hasTweakFile(ctName)) {
			throw new RomIOException("IPS Patch " + ctName + " is not supported by this ROM (" +
					romEntry.getName() + ").");
		}
		String patchName = romEntry.getTweakFile(ctName);

		try {
			FileFunctions.applyPatch(data, patchName);
			return true;
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
	public List<Integer> getAllConsumableHeldItems() {
		return Gen4Constants.consumableHeldItems;
	}

	@Override
	public List<Integer> getAllHeldItems() {
		return Gen4Constants.allHeldItems;
	}

	@Override
	public List<Integer> getSensibleHeldItemsFor(TrainerPokemon tp, boolean consumableOnly, List<Move> moves,
			int[] pokeMoves) {
		List<Integer> items = new ArrayList<>(Gen4Constants.generalPurposeConsumableItems);
		int frequencyBoostCount = 6; // Make some very good items more common, but not too common
		if (!consumableOnly) {
			frequencyBoostCount = 8; // bigger to account for larger item pool.
			items.addAll(Gen4Constants.generalPurposeItems);
		}
		for (int moveIdx : pokeMoves) {
			Move move = moves.get(moveIdx);
			if (move == null) {
				continue;
			}
			if (move.category == MoveCategory.PHYSICAL) {
				items.add(ItemIDs.liechiBerry);
				if (!consumableOnly) {
					items.addAll(Gen4Constants.typeBoostingItems.get(move.type));
					items.add(ItemIDs.choiceBand);
					items.add(ItemIDs.muscleBand);
				}
			}
			if (move.category == MoveCategory.SPECIAL) {
				items.add(ItemIDs.petayaBerry);
				if (!consumableOnly) {
					items.addAll(Gen4Constants.typeBoostingItems.get(move.type));
					items.add(ItemIDs.wiseGlasses);
					items.add(ItemIDs.choiceSpecs);
				}
			}
			if (!consumableOnly && Gen4Constants.moveBoostingItems.containsKey(moveIdx)) {
				items.addAll(Gen4Constants.moveBoostingItems.get(moveIdx));
			}
		}
		Map<Type, Effectiveness> byType = getTypeTable().against(tp.species.getPrimaryType(false), tp.species.getSecondaryType(false));
		for (Map.Entry<Type, Effectiveness> entry : byType.entrySet()) {
			Integer berry = Gen4Constants.weaknessReducingBerries.get(entry.getKey());
			if (entry.getValue() == Effectiveness.DOUBLE) {
				items.add(berry);
			} else if (entry.getValue() == Effectiveness.QUADRUPLE) {
				for (int i = 0; i < frequencyBoostCount; i++) {
					items.add(berry);
				}
			}
		}
		if (byType.get(Type.NORMAL) == Effectiveness.NEUTRAL) {
			items.add(ItemIDs.chilanBerry);
		}

		int ability = this.getAbilityForTrainerPokemon(tp);
		if (ability == AbilityIDs.levitate) {
			// we have to cast when removing, otherwise it defaults to removing by index
			items.remove((Integer) ItemIDs.shucaBerry);
		}

		if (!consumableOnly) {
			if (Gen4Constants.abilityBoostingItems.containsKey(ability)) {
				items.addAll(Gen4Constants.abilityBoostingItems.get(ability));
			}
			if (tp.species.getPrimaryType(false) == Type.POISON || tp.species.getSecondaryType(false) == Type.POISON) {
				items.add(ItemIDs.blackSludge);
			}
			List<Integer> speciesItems = Gen4Constants.speciesBoostingItems.get(tp.species.getNumber());
			if (speciesItems != null) {
				for (int i = 0; i < frequencyBoostCount; i++) {
					items.addAll(speciesItems);
				}
			}
		}
		return items;
	}

	protected int calculatePokemonNormalPaletteIndex(int i) {
		return i * 6 + 4;
	}

	protected int calculatePokemonShinyPaletteIndex(int i) {
		return calculatePokemonNormalPaletteIndex(i) + 1;
	}

    protected Collection<Integer> getGraphicalFormePokes() {
        return Gen4Constants.otherPokemonGraphicsPalettes.keySet();
    }

    protected void loadGraphicalFormePokemonPalettes(Species pk) {
        String NARCpath = getRomEntry().getFile("OtherPokemonGraphics");
        NARCArchive NARC;
        try {
            NARC = readNARC(NARCpath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

		int[][] palettes = Gen4Constants.otherPokemonGraphicsPalettes.get(pk.getNumber());
		for (int palID = 0; palID < palettes[0].length; palID++) {
			// assumes there are as many normal and shiny palettes
			pk.setNormalPalette(palID, readPalette(NARC, palettes[0][palID]));
			pk.setShinyPalette(palID, readPalette(NARC, palettes[1][palID]));
		}
    }

    protected void saveGraphicalFormePokemonPalettes(Species pk) {
		String NARCpath = getRomEntry().getFile("OtherPokemonGraphics");
		NARCArchive NARC;
		try {
			NARC = readNARC(NARCpath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		int[][] palettes = Gen4Constants.otherPokemonGraphicsPalettes.get(pk.getNumber());
		for (int palID = 0; palID < palettes[0].length; palID++) {
			// assumes there are as many normal and shiny palettes
			writePalette(NARC, palettes[0][palID], pk.getNormalPalette(palID));
			writePalette(NARC, palettes[1][palID], pk.getShinyPalette(palID));
		}
    }

	public Gen4PokemonImageGetter createPokemonImageGetter(Species pk) {
		return new Gen4PokemonImageGetter(pk);
	}

	public class Gen4PokemonImageGetter extends DSPokemonImageGetter {

		protected NARCArchive otherPokeGraphicsNARC;

		public Gen4PokemonImageGetter(Species pk) {
			super(pk);
		}

		public DSPokemonImageGetter setOtherPokeGraphicsNARC(NARCArchive otherPokeGraphicsNARC) {
			this.otherPokeGraphicsNARC = otherPokeGraphicsNARC;
			return this;
		}

		protected void beforeGet() {
			super.beforeGet();
			if (otherPokeGraphicsNARC == null) {
				try {
					String NARCpath = getRomEntry().getFile("OtherPokemonGraphics");
					otherPokeGraphicsNARC = readNARC(NARCpath);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public int getGraphicalFormeAmount() {
			int[][] formeInfo = Gen4Constants.otherPokemonGraphicsImages.get(pk.getNumber());
			if (formeInfo == null) {
				return 1;
			} else {
				return formeInfo[0].length;
			}
		}

		@Override
		public BufferedImage get() {
			beforeGet();

			int imageIndex = getImageIndex();
			int[] imageData = readImageData(getGraphicalFormeAmount() > 1 ? otherPokeGraphicsNARC : pokeGraphicsNARC, imageIndex);

			Palette palette = getPalette();
			int[] convPalette = palette.toARGB();
			if (transparentBackground) {
				convPalette[0] = 0;
			}

			// Deliberately chop off the right half of the image while still
			// correctly indexing the array.
			int bpp = 4;
			BufferedImage bim = new BufferedImage(80, 80, BufferedImage.TYPE_BYTE_INDEXED,
					GFXFunctions.indexColorModelFromPalette(convPalette, bpp));
			for (int y = 0; y < 80; y++) {
				for (int x = 0; x < 80; x++) {
					int value = ((imageData[y * 40 + x / 4]) >> (x % 4) * 4) & 0x0F;
					bim.setRGB(x, y, convPalette[value]);
				}
			}

			if (includePalette) {
				for (int j = 0; j < 16; j++) {
					bim.setRGB(j, 0, convPalette[j]);
				}
			}

			return bim;
		}

		private int getImageIndex() {
			int imageIndex;
			if (getGraphicalFormeAmount() > 1) {
				imageIndex = Gen4Constants.otherPokemonGraphicsImages.get(pk.getNumber())[back ? 1 : 0][forme];
			} else {
				imageIndex = pk.getNumber() * 6 + 2;
				if (gender == MALE) {
					imageIndex++;
				}
				if (back) {
					imageIndex -= 2;
				}
			}
			return imageIndex;
		}

		private Palette getPalette() {
			Palette palette;
			// unown and deoxys have the same palette(s) for all their formes
			if (getGraphicalFormeAmount() > 1 && pk.getNumber() != SpeciesIDs.unown && pk.getNumber() != SpeciesIDs.deoxys) {
				palette = shiny ? pk.getShinyPalette(forme) : pk.getNormalPalette(forme);
			} else {
				palette = shiny ? pk.getShinyPalette() : pk.getNormalPalette();
			}
			return palette;
		}


		@Override
		public BufferedImage getFull() {
			System.out.println(pk);
			if (getGraphicalFormeAmount() > 1) {
				return withFormesGetFull();
			} else {
				return super.getFull();
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

		private int[] readImageData(NARCArchive graphicsNARC, int imageIndex) {
			// read sprite
			byte[] rawImage = graphicsNARC.files.get(imageIndex);
			if (rawImage.length == 0) {
				// Must use other gender form
				rawImage = graphicsNARC.files.get(imageIndex ^ 1);
			}
			int[] imageData = new int[3200];
			for (int i = 0; i < 3200; i++) {
				imageData[i] = readWord(rawImage, i * 2 + 48);
			}

			// Decrypt image (why does EVERYTHING use the RNG formula geez)
			if (romEntry.getRomType() != Gen4Constants.Type_DP && getGraphicalFormeAmount() == 1) {
				int key = imageData[0];
				for (int i = 0; i < 3200; i++) {
					imageData[i] ^= (key & 0xFFFF);
					key = key * 0x41C64E6D + 0x6073;
				}
			} else {
				// D/P images are encrypted *backwards*. Wut.
				int key = imageData[3199];
				for (int i = 3199; i >= 0; i--) {
					imageData[i] ^= (key & 0xFFFF);
					key = key * 0x41C64E6D + 0x6073;
				}
			}
			return imageData;
		}

		@Override
		public boolean hasGenderedImages() {
			beforeGet();
			int imageIndex = pk.getNumber() * 6 + 2;
			byte[] rawImageFemale = pokeGraphicsNARC.files.get(imageIndex);
			byte[] rawImageMale = pokeGraphicsNARC.files.get(imageIndex + 1);
			return rawImageFemale.length != 0 && rawImageMale.length != 0
					&& !Arrays.equals(rawImageFemale, rawImageMale);
		}
	}

	//TODO: remove
	@Override
	public BufferedImage ripOtherPoke(int i, NARCArchive narcArchive) {
		final int deoxysImages = 0, deoxysPals = 0;
		final int unownImages = 8, unownPals = 2;
		final int castformImages = 64, castformPals = 4;
		final int burmyWormadamImages = 72;
		final int shellosGastroImages = 84;
		final int cherrimImages = 92;
		final int arceusImages = 96;
		final int eggImage = 132, manaphyEggImage = 133;
		final int shayminImages = 134;
		final int rotomImages = 138;
		final int giratinaImages = 150;
		final int pichuImages = 154;

		int palStart;
		switch (romEntry.getRomType()) {
			case Gen4Constants.Type_DP : palStart = manaphyEggImage + 1;
			case Gen4Constants.Type_Plat : palStart = 0; // TODO how does it look?
			case Gen4Constants.Type_HGSS : palStart = pichuImages + 4;
			default : palStart = 0;
		}

		System.out.println(i);
		int[] spriteData = readSpriteData(narcArchive, i);

		int palIndex;
		if (deoxysImages <= i && i < unownImages) {
			palIndex = deoxysPals + i % 2;
		} else if (unownImages <= i && i < castformImages) {
			palIndex = unownPals + i % 2;
		} else if ((castformImages <= i && i < shellosGastroImages) || (cherrimImages <= i && i < palStart)) {
			palIndex = i + castformPals - castformImages;
		} else if (shellosGastroImages <= i && i < cherrimImages) {
			palIndex = deoxysPals + i % 2; // TODO
		} else if (i == 208 || i == 209){
			palIndex = 210; // substitute
		} else if (i == 211){
			palIndex = 212; // shadows ??
		} else {
			palIndex = 0;
		}

		Palette palette = readPalette(narcArchive, palStart + palIndex);
		System.out.println(palette);
		int[] convPalette = palette.toARGB();
		// Deliberately chop off the right half of the image while still
		// correctly indexing the array.
		int bpp = 4;
		BufferedImage bim = new BufferedImage(80, 80, BufferedImage.TYPE_BYTE_INDEXED,
				GFXFunctions.indexColorModelFromPalette(convPalette, bpp));
		for (int y = 0; y < 80; y++) {
			for (int x = 0; x < 80; x++) {
				int value = ((spriteData[y * 40 + x / 4]) >> (x % 4) * 4) & 0x0F;
				bim.setRGB(x, y, convPalette[value]);
			}
		}
		return bim;
	}

	// TODO: remove
	private int[] readSpriteData(NARCArchive graphicsNARC, int spriteIndex) {
		// read sprite
		byte[] rawSprite = graphicsNARC.files.get(spriteIndex);
		if (rawSprite.length == 0) {
			// Must use other gender form
			rawSprite = graphicsNARC.files.get(spriteIndex ^ 1);
		}
		int[] spriteData = new int[3200];
		for (int i = 0; i < 3200; i++) {
			spriteData[i] = readWord(rawSprite, i * 2 + 48);
		}

		// Decrypt sprite (why does EVERYTHING use the RNG formula geez)
		if (romEntry.getRomType() != Gen4Constants.Type_DP) {
			int key = spriteData[0];
			for (int i = 0; i < 3200; i++) {
				spriteData[i] ^= (key & 0xFFFF);
				key = key * 0x41C64E6D + 0x6073;
			}
		} else {
			// D/P sprites are encrypted *backwards*. Wut.
			int key = spriteData[3199];
			for (int i = 3199; i >= 0; i--) {
				spriteData[i] ^= (key & 0xFFFF);
				key = key * 0x41C64E6D + 0x6073;
			}
		}
		return spriteData;
	}

	public String getPaletteFilesID() {
		switch (romEntry.getRomType()) {
			case Gen4Constants.Type_DP:
				return "DP";
			case Gen4Constants.Type_Plat:
			case Gen4Constants.Type_HGSS:
				// TODO: check if this should be identical
				return "DP";
			default:
				return null;
		}
	}

	@Override
	public Gen4RomEntry getRomEntry() {
		return romEntry;
	}

}
