package com.dabomstew.pkromio.constants;

/*----------------------------------------------------------------------------*/
/*--  GlobalConstants.java - constants that are relevant for multiple games --*/
/*--                         in the Pokemon series                          --*/
/*--                                                                        --*/
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GlobalConstants {

    public static final boolean[] bannedRandomMoves = new boolean[827], bannedForDamagingMove = new boolean[827];
    static {
        bannedRandomMoves[MoveIDs.struggle] = true; //  self explanatory

        bannedForDamagingMove[MoveIDs.selfDestruct] = true;
        bannedForDamagingMove[MoveIDs.dreamEater] = true;
        bannedForDamagingMove[MoveIDs.explosion] = true;
        bannedForDamagingMove[MoveIDs.snore] = true;
        bannedForDamagingMove[MoveIDs.falseSwipe] = true;
        bannedForDamagingMove[MoveIDs.futureSight] = true;
        bannedForDamagingMove[MoveIDs.fakeOut] = true;
        bannedForDamagingMove[MoveIDs.focusPunch] = true;
        bannedForDamagingMove[MoveIDs.doomDesire] = true;
        bannedForDamagingMove[MoveIDs.feint] = true;
        bannedForDamagingMove[MoveIDs.lastResort] = true;
        bannedForDamagingMove[MoveIDs.suckerPunch] = true;
        bannedForDamagingMove[MoveIDs.constrict] = true; // overly weak
        bannedForDamagingMove[MoveIDs.rage] = true; // lock-in in gen1
        bannedForDamagingMove[MoveIDs.rollout] = true; // lock-in
        bannedForDamagingMove[MoveIDs.iceBall] = true; // Rollout clone
        bannedForDamagingMove[MoveIDs.synchronoise] = true; // hard to use
        bannedForDamagingMove[MoveIDs.shellTrap] = true; // hard to use
        bannedForDamagingMove[MoveIDs.foulPlay] = true; // doesn't depend on your own attacking stat
        bannedForDamagingMove[MoveIDs.spitUp] = true; // hard to use

        // make sure these cant roll
        bannedForDamagingMove[MoveIDs.sonicBoom] = true;
        bannedForDamagingMove[MoveIDs.dragonRage] = true;
        bannedForDamagingMove[MoveIDs.hornDrill] = true;
        bannedForDamagingMove[MoveIDs.guillotine] = true;
        bannedForDamagingMove[MoveIDs.fissure] = true;
        bannedForDamagingMove[MoveIDs.sheerCold] = true;

    }

    /* @formatter:off */
    public static final List<Integer> normalMultihitMoves = Arrays.asList(
            MoveIDs.armThrust, MoveIDs.barrage, MoveIDs.boneRush, MoveIDs.bulletSeed, MoveIDs.cometPunch, MoveIDs.doubleSlap,
            MoveIDs.furyAttack, MoveIDs.furySwipes, MoveIDs.icicleSpear, MoveIDs.pinMissile, MoveIDs.rockBlast, MoveIDs.spikeCannon,
            MoveIDs.tailSlap, MoveIDs.waterShuriken);

    public static final List<Integer> doubleHitMoves = Arrays.asList(
            MoveIDs.bonemerang, MoveIDs.doubleHit, MoveIDs.doubleIronBash, MoveIDs.doubleKick, MoveIDs.dragonDarts,
            MoveIDs.dualChop, MoveIDs.gearGrind, MoveIDs.twineedle);

    public static final List<Integer> varyingPowerZMoves = Arrays.asList(
            MoveIDs.breakneckBlitzPhysical, MoveIDs.breakneckBlitzSpecial,
            MoveIDs.allOutPummelingPhysical, MoveIDs.allOutPummelingSpecial,
            MoveIDs.supersonicSkystrikePhysical, MoveIDs.supersonicSkystrikeSpecial,
            MoveIDs.acidDownpourPhysical, MoveIDs.acidDownpourSpecial,
            MoveIDs.tectonicRagePhysical, MoveIDs.tectonicRageSpecial,
            MoveIDs.continentalCrushPhysical, MoveIDs.continentalCrushSpecial,
            MoveIDs.savageSpinOutPhysical, MoveIDs.savageSpinOutSpecial,
            MoveIDs.neverEndingNightmarePhysical, MoveIDs.neverEndingNightmareSpecial,
            MoveIDs.corkscrewCrashPhysical, MoveIDs.corkscrewCrashSpecial,
            MoveIDs.infernoOverdrivePhysical, MoveIDs.infernoOverdriveSpecial,
            MoveIDs.hydroVortexPhysical, MoveIDs.hydroVortexSpecial,
            MoveIDs.bloomDoomPhysical, MoveIDs.bloomDoomSpecial,
            MoveIDs.gigavoltHavocPhysical, MoveIDs.gigavoltHavocSpecial,
            MoveIDs.shatteredPsychePhysical, MoveIDs.shatteredPsycheSpecial,
            MoveIDs.subzeroSlammerPhysical, MoveIDs.subzeroSlammerSpecial,
            MoveIDs.devastatingDrakePhysical, MoveIDs.devastatingDrakeSpecial,
            MoveIDs.blackHoleEclipsePhysical, MoveIDs.blackHoleEclipseSpecial,
            MoveIDs.twinkleTacklePhysical, MoveIDs.twinkleTackleSpecial);

    public static final List<Integer> fixedPowerZMoves = Arrays.asList(
            MoveIDs.catastropika, MoveIDs.sinisterArrowRaid, MoveIDs.maliciousMoonsault, MoveIDs.oceanicOperetta,
            MoveIDs.guardianOfAlola, MoveIDs.soulStealing7StarStrike, MoveIDs.stokedSparksurfer, MoveIDs.pulverizingPancake,
            MoveIDs.extremeEvoboost, MoveIDs.genesisSupernova, MoveIDs.tenMillionVoltThunderbolt, MoveIDs.lightThatBurnsTheSky,
            MoveIDs.searingSunrazeSmash, MoveIDs.menacingMoonrazeMaelstrom, MoveIDs.letsSnuggleForever,
            MoveIDs.splinteredStormshards, MoveIDs.clangorousSoulblaze);

    public static final List<Integer> zMoves = Stream.concat(fixedPowerZMoves.stream(),
            varyingPowerZMoves.stream()).collect(Collectors.toList());

    /* @formatter:on */

    public static final List<Integer> xItems = Arrays.asList(ItemIDs.guardSpec, ItemIDs.direHit, ItemIDs.xAttack,
            ItemIDs.xDefense, ItemIDs.xSpeed, ItemIDs.xAccuracy, ItemIDs.xSpAtk, ItemIDs.xSpDef);

    public static final List<Integer> regularShopItems = Arrays.asList(
            ItemIDs.pokeBall, ItemIDs.greatBall, ItemIDs.ultraBall,
            ItemIDs.potion, ItemIDs.superPotion,ItemIDs.hyperPotion,  ItemIDs.maxPotion,
            ItemIDs.antidote, ItemIDs.burnHeal, ItemIDs.iceHeal, ItemIDs.awakening, ItemIDs.paralyzeHeal,
            ItemIDs.fullHeal, ItemIDs.fullRestore, ItemIDs.revive,
            ItemIDs.repel, ItemIDs.superRepel, ItemIDs.maxRepel, ItemIDs.escapeRope
    );

    public static final List<Integer> battleTrappingAbilities = Arrays.asList(AbilityIDs.shadowTag, AbilityIDs.magnetPull,
            AbilityIDs.arenaTrap);

    public static final List<Integer> negativeAbilities = Arrays.asList(
            AbilityIDs.defeatist, AbilityIDs.slowStart, AbilityIDs.truant, AbilityIDs.klutz, AbilityIDs.stall
    );

    public static final List<Integer> badAbilities = Arrays.asList(
            AbilityIDs.minus, AbilityIDs.plus, AbilityIDs.anticipation, AbilityIDs.forewarn, AbilityIDs.frisk,
            AbilityIDs.honeyGather, AbilityIDs.auraBreak, AbilityIDs.receiver, AbilityIDs.powerOfAlchemy
    );

    public static final List<Integer> doubleBattleAbilities = Arrays.asList(
            AbilityIDs.friendGuard, AbilityIDs.healer, AbilityIDs.telepathy, AbilityIDs.symbiosis,
            AbilityIDs.battery
    );

    public static final List<Integer> duplicateAbilities = Arrays.asList(
            AbilityIDs.vitalSpirit, AbilityIDs.whiteSmoke, AbilityIDs.purePower, AbilityIDs.shellArmor, AbilityIDs.airLock,
            AbilityIDs.solidRock, AbilityIDs.ironBarbs, AbilityIDs.turboblaze, AbilityIDs.teravolt, AbilityIDs.emergencyExit,
            AbilityIDs.dazzling, AbilityIDs.tanglingHair, AbilityIDs.powerOfAlchemy, AbilityIDs.fullMetalBody,
            AbilityIDs.shadowShield, AbilityIDs.prismArmor, AbilityIDs.libero, AbilityIDs.stalwart
    );

    public static final List<Integer> noPowerNonStatusMoves = Arrays.asList(
            MoveIDs.guillotine, MoveIDs.hornDrill, MoveIDs.sonicBoom, MoveIDs.lowKick, MoveIDs.counter, MoveIDs.seismicToss,
            MoveIDs.dragonRage, MoveIDs.fissure, MoveIDs.nightShade, MoveIDs.bide, MoveIDs.psywave, MoveIDs.superFang,
            MoveIDs.flail, MoveIDs.revenge, MoveIDs.returnTheMoveNotTheKeyword, MoveIDs.present, MoveIDs.frustration,
            MoveIDs.magnitude, MoveIDs.mirrorCoat, MoveIDs.beatUp, MoveIDs.spitUp, MoveIDs.sheerCold
    );

    public static final List<Integer> cannotBeObsoletedMoves = Arrays.asList(
            MoveIDs.returnTheMoveNotTheKeyword, MoveIDs.frustration, MoveIDs.endeavor, MoveIDs.flail, MoveIDs.reversal,
            MoveIDs.hiddenPower, MoveIDs.storedPower, MoveIDs.smellingSalts, MoveIDs.fling, MoveIDs.powerTrip, MoveIDs.counter,
            MoveIDs.mirrorCoat, MoveIDs.superFang
    );

    public static final List<Integer> cannotObsoleteMoves = Arrays.asList(
            MoveIDs.gearUp, MoveIDs.magneticFlux, MoveIDs.focusPunch, MoveIDs.explosion, MoveIDs.selfDestruct, MoveIDs.geomancy,
            MoveIDs.venomDrench
    );

    public static final List<Integer> doubleBattleMoves = Arrays.asList(
            MoveIDs.followMe, MoveIDs.helpingHand, MoveIDs.ragePowder, MoveIDs.afterYou, MoveIDs.allySwitch, MoveIDs.healPulse,
            MoveIDs.quash, MoveIDs.ionDeluge, MoveIDs.matBlock, MoveIDs.aromaticMist, MoveIDs.electrify, MoveIDs.instruct,
            MoveIDs.spotlight, MoveIDs.decorate, MoveIDs.lifeDew, MoveIDs.coaching
    );

    public static final List<Integer> uselessMoves = Arrays.asList(
            MoveIDs.splash, MoveIDs.celebrate, MoveIDs.holdHands, MoveIDs.teleport,
            MoveIDs.reflectType       // the AI does not know how to use this move properly
    );

    public static final List<Integer> requiresOtherMove = Arrays.asList(
            MoveIDs.spitUp, MoveIDs.swallow, MoveIDs.dreamEater, MoveIDs.nightmare
    );

    public static final int vanillaHappinessToEvolve = 220, easierHappinessToEvolve = 160;

    public static final int MIN_DAMAGING_MOVE_POWER = 50;

    public static final int HIGHEST_POKEMON_GEN = 9;

    // Eevee has 8 potential evolutions
    public static final int LARGEST_NUMBER_OF_SPLIT_EVOS = 8;
}
