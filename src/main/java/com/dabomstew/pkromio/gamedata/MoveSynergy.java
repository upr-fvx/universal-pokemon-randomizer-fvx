package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  MoveSynergy.java - synergies between moves, or between                --*/
/*--                     abilities/stats and moves                          --*/
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

import com.dabomstew.pkromio.constants.AbilityIDs;
import com.dabomstew.pkromio.constants.MoveIDs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class MoveSynergy {

    public static List<Move> getSoftAbilityMoveSynergy(int ability, List<Move> moveList, Type pkType1, Type pkType2) {
        List<Integer> synergisticMoves = new ArrayList<>();

        switch(ability) {
            case AbilityIDs.drizzle:
            case AbilityIDs.primordialSea:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.WATER && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number).collect(Collectors.toList()));
                break;
            case AbilityIDs.drought:
            case AbilityIDs.desolateLand:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.FIRE && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number).collect(Collectors.toList()));
                break;
            case AbilityIDs.refrigerate:
                if (pkType1 == Type.ICE || pkType2 == Type.ICE) {
                    synergisticMoves.addAll(moveList
                            .stream()
                            .filter(mv -> mv.type == Type.NORMAL && mv.category != MoveCategory.STATUS)
                            .map(mv -> mv.number).collect(Collectors.toList()));
                }
                break;
            case AbilityIDs.galeWings:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.FLYING)
                        .map(mv -> mv.number).collect(Collectors.toList()));
                break;
            case AbilityIDs.pixilate:
                if (pkType1 == Type.FAIRY || pkType2 == Type.FAIRY) {
                    synergisticMoves.addAll(moveList
                            .stream()
                            .filter(mv -> mv.type == Type.NORMAL && mv.category != MoveCategory.STATUS)
                            .map(mv -> mv.number)
                            .collect(Collectors.toList()));
                }
                break;
            case AbilityIDs.aerilate:
                if (pkType1 == Type.FLYING || pkType2 == Type.FLYING) {
                    synergisticMoves.addAll(moveList
                            .stream()
                            .filter(mv -> mv.type == Type.NORMAL && mv.category != MoveCategory.STATUS)
                            .map(mv -> mv.number)
                            .collect(Collectors.toList()));
                }
                break;
            case AbilityIDs.darkAura:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.DARK && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.fairyAura:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.FAIRY && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.steelworker:
            case AbilityIDs.steelySpirit:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.STEEL && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.galvanize:
                if (pkType1 == Type.ELECTRIC || pkType2 == Type.ELECTRIC) {
                    synergisticMoves.addAll(moveList
                            .stream()
                            .filter(mv -> mv.type == Type.NORMAL && mv.category != MoveCategory.STATUS)
                            .map(mv -> mv.number)
                            .collect(Collectors.toList()));
                }
                break;
            case AbilityIDs.electricSurge:
            case AbilityIDs.transistor:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.ELECTRIC && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.psychicSurge:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.PSYCHIC && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.grassySurge:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.GRASS && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.dragonsMaw:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.DRAGON && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
        }

        return moveList
                .stream()
                .filter(mv -> synergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getSoftAbilityMoveAntiSynergy(int ability, List<Move> moveList) {
        List<Integer> antiSynergisticMoves = new ArrayList<>();

        switch (ability) {
            case AbilityIDs.drizzle:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.FIRE && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.drought:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.WATER && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.mistySurge:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.DRAGON && mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
        }

        return moveList
                .stream()
                .filter(mv -> antiSynergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getHardAbilityMoveSynergy(int ability, Type pkType1, Type pkType2, List<Move> moveList,
                                                       int generation, int perfectAccuracy) {
        List<Integer> synergisticMoves = new ArrayList<>();

        switch(ability) {
            case AbilityIDs.drizzle:
            case AbilityIDs.primordialSea:
                synergisticMoves.add(MoveIDs.thunder);
                synergisticMoves.add(MoveIDs.hurricane);
                if (pkType1 == Type.WATER || pkType2 == Type.WATER) {
                    synergisticMoves.add(MoveIDs.weatherBall);
                }
                break;
            case AbilityIDs.speedBoost:
                synergisticMoves.add(MoveIDs.batonPass);
                synergisticMoves.add(MoveIDs.storedPower);
                synergisticMoves.add(MoveIDs.powerTrip);
                break;
            case AbilityIDs.sturdy:
                if (generation >= 5) {
                    synergisticMoves.add(MoveIDs.endeavor);
                    synergisticMoves.add(MoveIDs.counter);
                    synergisticMoves.add(MoveIDs.mirrorCoat);
                    synergisticMoves.add(MoveIDs.flail);
                    synergisticMoves.add(MoveIDs.reversal);
                }
                break;
            case AbilityIDs.sandVeil:
            case AbilityIDs.sandRush:
                synergisticMoves.add(MoveIDs.sandstorm);
                break;
            case AbilityIDs.staticTheAbilityNotTheKeyword:
                synergisticMoves.add(MoveIDs.smellingSalts);
                synergisticMoves.add(MoveIDs.hex);
                break;
            case AbilityIDs.compoundEyes:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.hitratio > 0 && mv.hitratio <= 80)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.ownTempo:
            case AbilityIDs.tangledFeet:
                synergisticMoves.add(MoveIDs.petalDance);
                synergisticMoves.add(MoveIDs.thrash);
                synergisticMoves.add(MoveIDs.outrage);
                break;
            case AbilityIDs.shadowTag:
            case AbilityIDs.arenaTrap:
                synergisticMoves.add(MoveIDs.perishSong);
                break;
            case AbilityIDs.poisonPoint:
                synergisticMoves.add(MoveIDs.venoshock);
                // fallthrough
            case AbilityIDs.effectSpore:
            case AbilityIDs.flameBody:
                synergisticMoves.add(MoveIDs.hex);
                break;
            case AbilityIDs.sereneGrace:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> ((mv.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET ||
                                mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER) &&
                                mv.statChanges[0].percentChance < 100) ||
                                (mv.statusMoveType == StatusMoveType.DAMAGE && mv.statusPercentChance < 100) ||
                        mv.flinchPercentChance > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.swiftSwim:
            case AbilityIDs.rainDish:
            case AbilityIDs.drySkin:
            case AbilityIDs.hydration:
                synergisticMoves.add(MoveIDs.rainDance);
                break;
            case AbilityIDs.chlorophyll:
            case AbilityIDs.harvest:
            case AbilityIDs.leafGuard:
                synergisticMoves.add(MoveIDs.sunnyDay);
                break;
            case AbilityIDs.soundproof:
                synergisticMoves.add(MoveIDs.perishSong);
                break;
            case AbilityIDs.sandStream:
                if (pkType1 == Type.ROCK || pkType2 == Type.ROCK) {
                    synergisticMoves.add(MoveIDs.weatherBall);
                }
                break;
            case AbilityIDs.earlyBird:
            case AbilityIDs.shedSkin:
                synergisticMoves.add(MoveIDs.rest);
                break;
            case AbilityIDs.truant:
                synergisticMoves.add(MoveIDs.transform);
                break;
            case AbilityIDs.hustle:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                                mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                mv.hasSpecificStatChange(StatChangeType.ACCURACY, true))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.PHYSICAL && mv.hitratio == perfectAccuracy)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.guts:
                synergisticMoves.add(MoveIDs.facade);
                break;
            case AbilityIDs.rockHead:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.recoilPercent > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.drought:
            case AbilityIDs.desolateLand:
                synergisticMoves.add(MoveIDs.solarBeam);
                synergisticMoves.add(MoveIDs.solarBlade);
                synergisticMoves.add(MoveIDs.morningSun);
                synergisticMoves.add(MoveIDs.synthesis);
                synergisticMoves.add(MoveIDs.moonlight);
                if (generation >= 5) {
                    synergisticMoves.add(MoveIDs.growth);
                }
                if (pkType1 == Type.FIRE || pkType2 == Type.FIRE) {
                    synergisticMoves.add(MoveIDs.weatherBall);
                }
                break;
            case AbilityIDs.ironFist:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.isPunchMove)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.snowCloak:
            case AbilityIDs.iceBody:
            case AbilityIDs.slushRush:
                synergisticMoves.add(MoveIDs.hail);
                break;
            case AbilityIDs.unburden:
                synergisticMoves.add(MoveIDs.fling);
                synergisticMoves.add(MoveIDs.acrobatics);
                break;
            case AbilityIDs.simple:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                                mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                mv.statChanges[0].stages > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.acupressure);
                break;
            case AbilityIDs.adaptability:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category != MoveCategory.STATUS && (mv.type == pkType1 || mv.type == pkType2))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.skillLink:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.hitCount >= 3)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.sniper:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.criticalChance == CriticalChance.INCREASED ||
                                mv.criticalChance == CriticalChance.GUARANTEED)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.magicGuard:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.recoilPercent > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.mindBlown);
                break;
            case AbilityIDs.stall:
                synergisticMoves.add(MoveIDs.metalBurst);
                synergisticMoves.add(MoveIDs.payback);
                break;
            case AbilityIDs.superLuck:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.criticalChance == CriticalChance.INCREASED)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.analytic:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.power > 0 && mv.priority < 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.noGuard:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.hitratio > 0 && mv.hitratio <= 70)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.technician:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.power >= 40 && mv.power <= 60) || mv.hitCount > 1)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.slowStart:
                synergisticMoves.add(MoveIDs.transform);
                synergisticMoves.add(MoveIDs.protect);
                synergisticMoves.add(MoveIDs.detect);
                synergisticMoves.add(MoveIDs.kingsShield);
                synergisticMoves.add(MoveIDs.banefulBunker);
                synergisticMoves.add(MoveIDs.fly);
                synergisticMoves.add(MoveIDs.dig);
                synergisticMoves.add(MoveIDs.bounce);
                synergisticMoves.add(MoveIDs.dive);
                break;
            case AbilityIDs.snowWarning:
                synergisticMoves.add(MoveIDs.auroraVeil);
                synergisticMoves.add(MoveIDs.blizzard);
                if (pkType1 == Type.ICE || pkType2 == Type.ICE) {
                    synergisticMoves.add(MoveIDs.weatherBall);
                }
                break;
            case AbilityIDs.reckless:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.recoilPercent > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.jumpKick);
                synergisticMoves.add(MoveIDs.highJumpKick);
                break;
            case AbilityIDs.badDreams:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statusType == StatusType.SLEEP)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.sheerForce:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET ||
                                (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER &&
                                        mv.statChanges[0].stages > 0) ||
                                mv.statusMoveType == StatusMoveType.DAMAGE ||
                                mv.flinchPercentChance > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.contrary:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER &&
                                mv.statChanges[0].stages < 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.heavyMetal:
                synergisticMoves.add(MoveIDs.heatCrash);
                synergisticMoves.add(MoveIDs.heavySlam);
                break;
            case AbilityIDs.moody:
                synergisticMoves.add(MoveIDs.storedPower);
                synergisticMoves.add(MoveIDs.powerTrip);
                break;
            case AbilityIDs.poisonTouch:
            case AbilityIDs.toughClaws:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.makesContact)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.regenerator:
                synergisticMoves.add(MoveIDs.uTurn);
                synergisticMoves.add(MoveIDs.voltSwitch);
                synergisticMoves.add(MoveIDs.partingShot);
                break;
            case AbilityIDs.prankster:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statusMoveType == StatusMoveType.NO_DAMAGE)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.destinyBond);
                synergisticMoves.add(MoveIDs.encore);
                synergisticMoves.add(MoveIDs.reflect);
                synergisticMoves.add(MoveIDs.lightScreen);
                synergisticMoves.add(MoveIDs.grudge);
                synergisticMoves.add(MoveIDs.painSplit);
                synergisticMoves.add(MoveIDs.substitute);
                break;
            case AbilityIDs.strongJaw:
                synergisticMoves.add(MoveIDs.bite);
                synergisticMoves.add(MoveIDs.crunch);
                synergisticMoves.add(MoveIDs.fireFang);
                synergisticMoves.add(MoveIDs.fishiousRend);
                synergisticMoves.add(MoveIDs.hyperFang);
                synergisticMoves.add(MoveIDs.iceFang);
                synergisticMoves.add(MoveIDs.jawLock);
                synergisticMoves.add(MoveIDs.poisonFang);
                synergisticMoves.add(MoveIDs.psychicFangs);
                synergisticMoves.add(MoveIDs.thunderFang);
                break;
            case AbilityIDs.megaLauncher:
                synergisticMoves.add(MoveIDs.auraSphere);
                synergisticMoves.add(MoveIDs.darkPulse);
                synergisticMoves.add(MoveIDs.dragonPulse);
                synergisticMoves.add(MoveIDs.originPulse);
                synergisticMoves.add(MoveIDs.terrainPulse);
                synergisticMoves.add(MoveIDs.waterPulse);
                break;
            case AbilityIDs.wimpOut:
            case AbilityIDs.emergencyExit:
                synergisticMoves.add(MoveIDs.fakeOut);
                synergisticMoves.add(MoveIDs.firstImpression);
                break;
            case AbilityIDs.merciless:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statusType == StatusType.POISON || mv.statusType == StatusType.TOXIC_POISON)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.banefulBunker);
                break;
            case AbilityIDs.liquidVoice:
                if (pkType1 == Type.WATER || pkType2 == Type.WATER) {
                    synergisticMoves.addAll(moveList
                            .stream()
                            .filter(mv -> mv.isSoundMove && mv.power > 0)
                            .map(mv -> mv.number)
                            .collect(Collectors.toList()));
                }
                break;
            case AbilityIDs.triage:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.absorbPercent > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.surgeSurfer:
                synergisticMoves.add(MoveIDs.electricTerrain);
                break;
            case AbilityIDs.corrosion:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.STATUS &&
                                (mv.statusType == StatusType.POISON || mv.statusType == StatusType.TOXIC_POISON))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
        }

        return moveList
                .stream()
                .filter(mv -> synergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getHardAbilityMoveAntiSynergy(int ability, List<Move> moveList) {
        List<Integer> antiSynergisticMoves = new ArrayList<>();

        switch(ability) {
            case AbilityIDs.primordialSea:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.FIRE &&
                                mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                // fallthrough
            case AbilityIDs.drizzle:
            case AbilityIDs.sandStream:
            case AbilityIDs.snowWarning:
                antiSynergisticMoves.add(MoveIDs.solarBeam);
                antiSynergisticMoves.add(MoveIDs.solarBlade);
                antiSynergisticMoves.add(MoveIDs.morningSun);
                antiSynergisticMoves.add(MoveIDs.synthesis);
                antiSynergisticMoves.add(MoveIDs.moonlight);
                antiSynergisticMoves.add(MoveIDs.rainDance);
                antiSynergisticMoves.add(MoveIDs.sunnyDay);
                antiSynergisticMoves.add(MoveIDs.hail);
                antiSynergisticMoves.add(MoveIDs.sandstorm);
                break;
            case AbilityIDs.speedBoost:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                mv.statChanges[0].type == StatChangeType.SPEED &&
                                mv.statChanges[0].stages > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                antiSynergisticMoves.add(MoveIDs.psychUp);
                antiSynergisticMoves.add(MoveIDs.haze);
                break;
            case AbilityIDs.desolateLand:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.type == Type.WATER &&
                                mv.category != MoveCategory.STATUS)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                // fallthrough
            case AbilityIDs.drought:
                antiSynergisticMoves.add(MoveIDs.thunder);
                antiSynergisticMoves.add(MoveIDs.hurricane);
                antiSynergisticMoves.add(MoveIDs.rainDance);
                antiSynergisticMoves.add(MoveIDs.sunnyDay);
                antiSynergisticMoves.add(MoveIDs.hail);
                antiSynergisticMoves.add(MoveIDs.sandstorm);
                break;
            case AbilityIDs.noGuard:
                antiSynergisticMoves.add(MoveIDs.lockOn);
                antiSynergisticMoves.add(MoveIDs.mindReader);
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.hasSpecificStatChange(StatChangeType.ACCURACY, true) ||
                                mv.hasSpecificStatChange(StatChangeType.EVASION, true) ||
                                mv.hasSpecificStatChange(StatChangeType.EVASION, false))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.damp:
                antiSynergisticMoves.add(MoveIDs.selfDestruct);
                antiSynergisticMoves.add(MoveIDs.explosion);
                antiSynergisticMoves.add(MoveIDs.mindBlown);
                antiSynergisticMoves.add(MoveIDs.mistyExplosion);
                break;
            case AbilityIDs.insomnia:
            case AbilityIDs.vitalSpirit:
            case AbilityIDs.comatose:
            case AbilityIDs.sweetVeil:
                antiSynergisticMoves.add(MoveIDs.rest);
                break;
            case AbilityIDs.airLock:
            case AbilityIDs.cloudNine:
            case AbilityIDs.deltaStream:
                antiSynergisticMoves.add(MoveIDs.rainDance);
                antiSynergisticMoves.add(MoveIDs.sunnyDay);
                antiSynergisticMoves.add(MoveIDs.sandstorm);
                antiSynergisticMoves.add(MoveIDs.hail);
                break;
            case AbilityIDs.simple:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                                mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                mv.statChanges[0].stages < 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.contrary:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                                mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                mv.statChanges[0].stages > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                antiSynergisticMoves.add(MoveIDs.shellSmash);
                break;
            case AbilityIDs.lightMetal:
                antiSynergisticMoves.add(MoveIDs.heatCrash);
                antiSynergisticMoves.add(MoveIDs.heavySlam);
                break;
            case AbilityIDs.electricSurge:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.category == MoveCategory.STATUS && mv.statusType == StatusType.SLEEP))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                antiSynergisticMoves.add(MoveIDs.rest);
                break;
            case AbilityIDs.psychicSurge:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.priority > 0))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case AbilityIDs.mistySurge:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.category == MoveCategory.STATUS &&
                                (mv.statusType == StatusType.BURN ||
                                mv.statusType == StatusType.FREEZE ||
                                mv.statusType == StatusType.PARALYZE ||
                                mv.statusType == StatusType.SLEEP ||
                                mv.statusType == StatusType.POISON ||
                                mv.statusType == StatusType.TOXIC_POISON)))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                antiSynergisticMoves.add(MoveIDs.rest);
                break;
            case AbilityIDs.grassySurge:
                antiSynergisticMoves.add(MoveIDs.earthquake);
                antiSynergisticMoves.add(MoveIDs.magnitude);
                antiSynergisticMoves.add(MoveIDs.bulldoze);
                break;


        }

        return moveList
                .stream()
                .filter(mv -> antiSynergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getStatMoveSynergy(Species sp, List<Move> moveList) {
        List<Integer> synergisticMoves = new ArrayList<>();

        if ((double) sp.getHp() / (double)sp.getBST() < 1.0/8) {
            synergisticMoves.add(MoveIDs.painSplit);
            synergisticMoves.add(MoveIDs.endeavor);
        }

        if ((double) sp.getHp() / (double)sp.getBST() >= 1.0/4) {
            synergisticMoves.add(MoveIDs.waterSpout);
            synergisticMoves.add(MoveIDs.eruption);
            synergisticMoves.add(MoveIDs.counter);
            synergisticMoves.add(MoveIDs.mirrorCoat);
        }

        if (sp.getAttack() * 2 < sp.getDefense()) {
            synergisticMoves.add(MoveIDs.powerTrick);
        }

        if ((double)(sp.getAttack() + sp.getSpatk()) / (double)sp.getBST() < 1.0/4) {
            synergisticMoves.add(MoveIDs.powerSplit);
        }

        if ((double)(sp.getDefense() + sp.getSpdef()) / (double)sp.getBST() < 1.0/4) {
            synergisticMoves.add(MoveIDs.guardSplit);
        }

        if ((double) sp.getSpeed() / (double)sp.getBST() < 1.0/8) {
            synergisticMoves.add(MoveIDs.gyroBall);
        }

        if ((double) sp.getSpeed() / (double)sp.getBST() >= 1.0/4) {
            synergisticMoves.add(MoveIDs.electroBall);
        }

        return moveList
                .stream()
                .filter(mv -> synergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getStatMoveAntiSynergy(Species sp, List<Move> moveList) {
        List<Integer> antiSynergisticMoves = new ArrayList<>();

        if ((double) sp.getHp() / (double)sp.getBST() >= 1.0/4) {
            antiSynergisticMoves.add(MoveIDs.painSplit);
            antiSynergisticMoves.add(MoveIDs.endeavor);
        }

        if (sp.getDefense() * 2 < sp.getAttack()) {
            antiSynergisticMoves.add(MoveIDs.powerTrick);
        }

        if ((double)(sp.getAttack() + sp.getSpatk()) / (double)sp.getBST() >= 1.0/3) {
            antiSynergisticMoves.add(MoveIDs.powerSplit);
        }

        if ((double)(sp.getDefense() + sp.getSpdef()) / (double)sp.getBST() >= 1.0/3) {
            antiSynergisticMoves.add(MoveIDs.guardSplit);
        }

        if ((double) sp.getSpeed() / (double)sp.getBST() >= 1.0/4) {
            antiSynergisticMoves.add(MoveIDs.gyroBall);
        }

        if ((double) sp.getSpeed() / (double)sp.getBST() < 1.0/8) {
            antiSynergisticMoves.add(MoveIDs.electroBall);
        }

        return moveList
                .stream()
                .filter(mv -> antiSynergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getMoveSynergy(Move mv1, List<Move> moveList, int generation) {
        List<Integer> synergisticMoves = new ArrayList<>();

        if ((mv1.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET &&
                mv1.hasSpecificStatChange(StatChangeType.SPEED, false)) ||
                ((mv1.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                        mv1.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                        mv1.hasSpecificStatChange(StatChangeType.SPEED, true))) {
            synergisticMoves.addAll(moveList
                    .stream()
                    .filter(mv -> mv.flinchPercentChance > 0 && mv.priority == 0)
                    .map(mv -> mv.number)
                    .collect(Collectors.toList()));
        }

        if (mv1.flinchPercentChance > 0 && mv1.priority == 0) {
            synergisticMoves.addAll(moveList
                    .stream()
                    .filter(mv -> (mv.statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET &&
                            mv.hasSpecificStatChange(StatChangeType.SPEED, false)) ||
                            ((mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER ||
                                    mv.statChangeMoveType == StatChangeMoveType.NO_DAMAGE_USER) &&
                                    mv.hasSpecificStatChange(StatChangeType.SPEED, true)))
                    .map(mv -> mv.number)
                    .collect(Collectors.toList()));
        }

        if (mv1.statChanges[0].stages >= 2 || mv1.statChanges[1].type != StatChangeType.NONE) {
            synergisticMoves.add(MoveIDs.batonPass);
            synergisticMoves.add(MoveIDs.storedPower);
            synergisticMoves.add(MoveIDs.powerTrip);
        }

        if (mv1.statusType == StatusType.SLEEP) {
            synergisticMoves.add(MoveIDs.dreamEater);
            synergisticMoves.add(MoveIDs.nightmare);
            synergisticMoves.add(MoveIDs.hex);
        }

        switch(mv1.number) {
            case MoveIDs.toxic:
                synergisticMoves.add(MoveIDs.protect);
                synergisticMoves.add(MoveIDs.detect);
                synergisticMoves.add(MoveIDs.kingsShield);
                synergisticMoves.add(MoveIDs.dig);
                synergisticMoves.add(MoveIDs.fly);
                synergisticMoves.add(MoveIDs.bounce);
                synergisticMoves.add(MoveIDs.dive);
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.isTrapMove))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                // fallthrough
            case MoveIDs.poisonPowder:
            case MoveIDs.poisonGas:
            case MoveIDs.banefulBunker:
            case MoveIDs.toxicThread:
                synergisticMoves.add(MoveIDs.venoshock);
                synergisticMoves.add(MoveIDs.hex);
                break;
            case MoveIDs.venoshock:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.STATUS &&
                                (mv.statusType == StatusType.POISON || mv.statusType == StatusType.TOXIC_POISON))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.protect:
            case MoveIDs.detect:
            case MoveIDs.kingsShield:
                synergisticMoves.add(MoveIDs.toxic);
                synergisticMoves.add(MoveIDs.leechSeed);
                synergisticMoves.add(MoveIDs.willOWisp);
                break;
            case MoveIDs.batonPass:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statChanges[0].stages >= 2 || mv.statChanges[1].type != StatChangeType.NONE)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.shellSmash);
                break;
            case MoveIDs.willOWisp:
                synergisticMoves.add(MoveIDs.hex);
                break;
            case MoveIDs.lockOn:
            case MoveIDs.mindReader:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.hitratio <= 50))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.sunnyDay:
                synergisticMoves.add(MoveIDs.solarBlade);
                synergisticMoves.add(MoveIDs.solarBeam);
                break;
            case MoveIDs.rainDance:
                synergisticMoves.add(MoveIDs.thunder);
                synergisticMoves.add(MoveIDs.hurricane);
                break;
            case MoveIDs.powerSwap:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statChangeMoveType == StatChangeMoveType.DAMAGE_USER &&
                                (mv.hasSpecificStatChange(StatChangeType.ATTACK, false) ||
                                        mv.hasSpecificStatChange(StatChangeType.SPECIAL_ATTACK, false)))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.endure:
                synergisticMoves.add(MoveIDs.reversal);
                synergisticMoves.add(MoveIDs.flail);
                synergisticMoves.add(MoveIDs.endeavor);
                break;
            case MoveIDs.endeavor:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category != MoveCategory.STATUS && mv.priority > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.thunderWave:
            case MoveIDs.glare:
            case MoveIDs.stunSpore:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.flinchPercentChance > 0)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.STATUS && mv.statusType == StatusType.CONFUSION)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.hex);
                break;
            case MoveIDs.hail:
                synergisticMoves.add(MoveIDs.blizzard);
                synergisticMoves.add(MoveIDs.auroraVeil);
                break;
            case MoveIDs.stockpile:
                synergisticMoves.add(MoveIDs.spitUp);
                synergisticMoves.add(MoveIDs.swallow);
                break;
            case MoveIDs.spitUp:
            case MoveIDs.swallow:
                synergisticMoves.add(MoveIDs.stockpile);
                break;
            case MoveIDs.leechSeed:
            case MoveIDs.perishSong:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.isTrapMove))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.spikes:
            case MoveIDs.stealthRock:
            case MoveIDs.toxicSpikes:
                synergisticMoves.add(MoveIDs.roar);
                synergisticMoves.add(MoveIDs.whirlwind);
                synergisticMoves.add(MoveIDs.dragonTail);
                synergisticMoves.add(MoveIDs.circleThrow);
                break;
            case MoveIDs.rest:
                synergisticMoves.add(MoveIDs.sleepTalk);
                break;
            case MoveIDs.focusEnergy:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.criticalChance == CriticalChance.INCREASED)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.focusPunch:
            case MoveIDs.dreamEater:
            case MoveIDs.nightmare:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statusMoveType == StatusMoveType.NO_DAMAGE &&
                                mv.statusType == StatusType.SLEEP)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.torment:
                synergisticMoves.add(MoveIDs.encore);
                break;
            case MoveIDs.encore:
                synergisticMoves.add(MoveIDs.torment);
                break;
            case MoveIDs.hex:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statusMoveType == StatusMoveType.NO_DAMAGE &&
                                mv.statusType != StatusType.CONFUSION)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.banefulBunker);
                break;
            case MoveIDs.storedPower:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.statChanges[0].stages > 1 || mv.statChanges[1].type != StatChangeType.NONE)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                synergisticMoves.add(MoveIDs.acupressure);
                synergisticMoves.add(MoveIDs.shellSmash);
                break;
            case MoveIDs.swagger:
                synergisticMoves.add(MoveIDs.punishment);
                break;
            case MoveIDs.punishment:
                synergisticMoves.add(MoveIDs.swagger);
                break;
            case MoveIDs.shellSmash:
                synergisticMoves.add(MoveIDs.storedPower);
                break;
        }

        return moveList
                .stream()
                .filter(mv -> synergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getSoftMoveSynergy(Move mv1, List<Move> moveList, TypeTable typeTable) {
        List<Integer> synergisticMoves = new ArrayList<>();

        if (mv1.category != MoveCategory.STATUS) {
            List<Type> notVeryEffective = new ArrayList<>();
            notVeryEffective.addAll(typeTable.notVeryEffectiveWhenAttacking(mv1.type));
            notVeryEffective.addAll(typeTable.immuneWhenAttacking(mv1.type));
            for (Type nveType: notVeryEffective) {
                List<Type> superEffectiveAgainstNVE =
                        typeTable.against(nveType, null)
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getValue() == Effectiveness.DOUBLE)
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category != MoveCategory.STATUS &&
                                superEffectiveAgainstNVE.contains(mv.type))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
            }
        }

        switch (mv1.number) {
            case MoveIDs.swordsDance:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.PHYSICAL)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.nastyPlot:
            case MoveIDs.tailGlow:
                synergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> mv.category == MoveCategory.SPECIAL)
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
        }

        return moveList
                .stream()
                .filter(mv -> synergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getHardMoveAntiSynergy(Move mv1, List<Move> moveList) {
        List<Integer> antiSynergisticMoves = new ArrayList<>();


        if (mv1.category == MoveCategory.STATUS && mv1.statusType != StatusType.NONE) {
            antiSynergisticMoves.addAll(moveList
                    .stream()
                    .filter(mv -> (mv.category == MoveCategory.STATUS && mv.statusType != StatusType.NONE &&
                            (mv.statusType == mv1.statusType ||
                                    (mv1.statusType != StatusType.CONFUSION && mv.statusType != StatusType.CONFUSION))))
                    .map(mv -> mv.number)
                    .collect(Collectors.toList()));
        }

        switch (mv1.number) {
            case MoveIDs.protect: {
                antiSynergisticMoves.add(MoveIDs.detect);
                antiSynergisticMoves.add(MoveIDs.banefulBunker);
                antiSynergisticMoves.add(MoveIDs.kingsShield);
                break;
            }
            case MoveIDs.detect: {
                antiSynergisticMoves.add(MoveIDs.protect);
                antiSynergisticMoves.add(MoveIDs.banefulBunker);
                antiSynergisticMoves.add(MoveIDs.kingsShield);
                break;
            }
            case MoveIDs.kingsShield: {
                antiSynergisticMoves.add(MoveIDs.protect);
                antiSynergisticMoves.add(MoveIDs.detect);
                antiSynergisticMoves.add(MoveIDs.banefulBunker);
                break;
            }
            case MoveIDs.banefulBunker: {
                antiSynergisticMoves.add(MoveIDs.protect);
                antiSynergisticMoves.add(MoveIDs.detect);
                antiSynergisticMoves.add(MoveIDs.kingsShield);
                break;
            }
            case MoveIDs.returnTheMoveNotTheKeyword:
                antiSynergisticMoves.add(MoveIDs.frustration);
                break;
            case MoveIDs.frustration:
                antiSynergisticMoves.add(MoveIDs.returnTheMoveNotTheKeyword);
                break;
            case MoveIDs.leechSeed:
            case MoveIDs.perishSong: {
                antiSynergisticMoves.add(MoveIDs.whirlwind);
                antiSynergisticMoves.add(MoveIDs.roar);
                antiSynergisticMoves.add(MoveIDs.circleThrow);
                antiSynergisticMoves.add(MoveIDs.dragonTail);
                break;
            }
        }

        if (mv1.type != null) {
            switch (mv1.type) {
                case FIRE:
                    if (mv1.category != MoveCategory.STATUS) {
                        antiSynergisticMoves.add(MoveIDs.waterSport);
                    }
                    break;
                case ELECTRIC:
                    if (mv1.category != MoveCategory.STATUS) {
                        antiSynergisticMoves.add(MoveIDs.mudSport);
                    }
                    break;
            }
        }

        return moveList
                .stream()
                .filter(mv -> antiSynergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> getSoftMoveAntiSynergy(Move mv1, List<Move> moveList) {
        List<Integer> antiSynergisticMoves = new ArrayList<>();


        if (mv1.category != MoveCategory.STATUS) {
            antiSynergisticMoves.addAll(moveList
                    .stream()
                    .filter(mv -> (mv.category != MoveCategory.STATUS && mv.type == mv1.type))
                    .map(mv -> mv.number)
                    .collect(Collectors.toList()));
        }

        switch (mv1.number) {
            case MoveIDs.waterSport:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.category != MoveCategory.STATUS && mv.type == Type.FIRE))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.mudSport:
                antiSynergisticMoves.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.category != MoveCategory.STATUS && mv.type == Type.ELECTRIC))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
        }

        return moveList
                .stream()
                .filter(mv -> antiSynergisticMoves.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }

    public static List<Move> requiresOtherMove(Move mv1, List<Move> moveList) {
        List<Integer> requiresMove = new ArrayList<>();
        switch (mv1.number) {
            case MoveIDs.spitUp:
            case MoveIDs.swallow:
                requiresMove.add(MoveIDs.stockpile);
                break;
            case MoveIDs.dreamEater:
            case MoveIDs.nightmare:
                requiresMove.addAll(moveList
                        .stream()
                        .filter(mv -> (mv.category == MoveCategory.STATUS && mv.statusType == StatusType.SLEEP))
                        .map(mv -> mv.number)
                        .collect(Collectors.toList()));
                break;
            case MoveIDs.snore:
            case MoveIDs.sleepTalk:
                requiresMove.add(MoveIDs.rest);
                break;
        }
        return moveList
                .stream()
                .filter(mv -> requiresMove.contains(mv.number))
                .distinct()
                .collect(toCollection(ArrayList::new));
    }
}
