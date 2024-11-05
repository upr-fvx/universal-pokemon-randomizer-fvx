package com.dabomstew.pkrandom.services;

import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.constants.MoveIDs;
import com.dabomstew.pkrandom.gamedata.Move;
import com.dabomstew.pkrandom.gamedata.MoveCategory;
import com.dabomstew.pkrandom.gamedata.StatChangeMoveType;
import com.dabomstew.pkrandom.gamedata.StatusMoveType;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveValuationService {
    private final RomHandler romHandler;
    private final List<Move> allMoves;
    private final Map<Move, Integer> baseValues;

    public MoveValuationService(RomHandler romHandler) {
        this.romHandler = romHandler;
        allMoves = romHandler.getMoves();
        baseValues = new HashMap<>();
    }

    public static class MoveValues{
        private int baseValue;
        private int synergyPotential;

        public MoveValues(int baseValue, int synergyPotential) {
            this.baseValue = baseValue;
            this.synergyPotential = synergyPotential;
        }

        public int getBaseValue() {
            return baseValue;
        }

        public int getSynergyPotential() {
            return synergyPotential;
        }
    }

    public List<Move> getAllMoves() {
        return new ArrayList<>(allMoves);
    }

    public int getBaseValue(int moveID) {
        return getBaseValue(allMoves.get(moveID));
    }

    public int getBaseValue(Move move) {
        if(!baseValues.containsKey(move)) {
            int value;
            if(move.category != MoveCategory.STATUS) {
                value = generateDamagingMoveValue(move);
            } else {
                value = generateStatusMoveValue(move);
            }

            baseValues.put(move, value);
        }

        return baseValues.get(move);
    }

    private int generateDamagingMoveValue(Move move) {
        int power = move.power;
        if(power <= 1) {
            power = generateUniqueDamagingMovePower(move);
        }
        double value = power;

        if(move.hitCount > 1) {
            value *= move.hitCount;
        } else {
            value *= generateUniquePowerMultiplier(move);
        }

        value += move.absorbPercent / 100.0 * power;
        value -= (move.recoilPercent / 100.0 * power) / 2;
        value *= 1 + (move.flinchPercentChance / 100.0);

        switch (move.criticalChance) {
            case NONE:
                value -= power * .12;
                break;
            case INCREASED:
                value += power * .12;
                break;
            case GUARANTEED:
                value += power;
                break;
            //TODO: make these values accurate outside of Gen 2-5
        }

        if(move.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            value += generateStatChangeValue(move);
        }

        if(move.statusPercentChance != 0) {
            value += (move.statusPercentChance / 100) * generateStatusConditionValue(move);
        }

        value += generateUniqueSecondaryValue(move);


        if(move.hitRatio == 0) {
            value *= 1.5;
        } else {
            switch(move.internalId) {
                case MoveIDs.swift:
                case MoveIDs.feintAttack:
                case MoveIDs.vitalThrow:
                    //because gen 1 through 3 encoded perfect-accuracy moves differently, and they can't be distinguished
                    //from other moves except by ID.
                    value *= 1.5;
                    break;
                default:
                    value *= move.hitRatio / 100;
            }
        }

        if(move.isChargeMove) {
            if(GlobalConstants.semiInvulnerableMoves.contains(move.internalId)) {
                value += 10;
            } else {
                value *= .5;
            }
        }
        if(move.isRechargeMove)
            value *= .75;

        int priority = move.priority;
        if(romHandler.generationOfPokemon() == 2) {
            priority--; //All gen 2 moves have one higher priority than typical
        }

        value += 10 * priority;

        if(move.pp < 10) {
            value *= .95;
        }

        return (int) value;
    }

    private int generateStatusMoveValue(Move move) {
        if(GlobalConstants.uselessMoves.contains(move.internalId)) {
            return 0;
        }

        double value = 0;

        if(move.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            value += generateStatChangeValue(move);
        }
        if(move.statusMoveType != StatusMoveType.NONE_OR_UNKNOWN) {
            value += generateStatusConditionValue(move);
        }
        if(value == 0) {
            value += generateUniqueStatusMoveValue(move);
        }

        if(move.hitRatio != 0) {
            value *= move.hitRatio / 100;
        }

        if(move.pp < 10) {
            value *= .9;
        }

        return (int) value;
    }

    private double generateStatChangeValue(Move move) {
        double value = 0;

        for(Move.StatChange change : move.statChanges) {
            if(change == null) {
                continue;
            }
            int changeValue;
            switch(change.type) {
                case ATTACK:
                case DEFENSE:
                case SPECIAL_ATTACK:
                case SPECIAL_DEFENSE:
                case SPEED:
                    changeValue = 40; //somewhat arbitrary baseline
                    //(Speed-only moves get a hefty synergy penalty without e.g. flinch moves)
                    break;
                case SPECIAL: //because this is both attack and defence
                    changeValue = 50;
                    break;
                case ACCURACY:
                case EVASION:
                    changeValue = 60;
                    break;
                case ALL:
                    changeValue = 200; //att + def + spatk + spdef + spd
                    break;
                case NONE:
                default:
                    changeValue = 0; //this shouldn't be reached? but whatever
                    break;
            }
            changeValue *= change.stages;

            if(change.percentChance > 1) {
                value += changeValue * change.percentChance / 100;
                //aaarrrgh why is it encoded differently in different games....
            } else if(change.percentChance != 0) {
                value += changeValue * change.percentChance;
            } else {
                value += changeValue;
            }


        }

        switch (move.statChangeMoveType) {
            case DAMAGE_USER:
            case NO_DAMAGE_ALLY:
            case NO_DAMAGE_USER:
                //no change
                break;
            case DAMAGE_TARGET:
            case NO_DAMAGE_TARGET:
                value *= -1;
                break;
            case NO_DAMAGE_ALL:
            case NONE_OR_UNKNOWN:
                //?????
                value = 0;
                break;
        }

        return value;
    }

    private int generateStatusConditionValue(Move move) {
        switch(move.statusType) {
            case POISON:
                return 50;
            case BURN:
                return 60;
            case TOXIC_POISON:
                return 70; //These all have significant boosts from defensive moves / stat ratios
            case CONFUSION:
                return 90;
            case PARALYZE:
            case SLEEP:
            case FREEZE:
                return 100;
            case NONE:
            default:
                return 0;
        }
        //These values are a bit arbitrary and may betray my personal biases.
        //But that applies to all status values... and some of the damage factors as well...
    }

    private int generateUniqueDamagingMovePower(Move move) {
        switch(move.internalId) {
            case MoveIDs.superFang:
                return 100; //half HP is considered 100 power
            case MoveIDs.guillotine:
            case MoveIDs.hornDrill:
            case MoveIDs.fissure:
            case MoveIDs.sheerCold:
                return 200; //instakill considered to be 200 power
            case MoveIDs.psywave:
                return 30;
            case MoveIDs.sonicBoom:
            case MoveIDs.seismicToss:
            case MoveIDs.nightShade:
                return 40;
            case MoveIDs.dragonRage:
                return 50; //static damage moves will be given synergy multipliers based on level
            case MoveIDs.counter:
            case MoveIDs.mirrorCoat:
                return 80;
            case MoveIDs.bide:
                return 60;
                //not really sure how to handle the retaliate moves
            case MoveIDs.lowKick:
            case MoveIDs.grassKnot:
                return 60;
            //this is a guess at the median power
            //Interestingly, these moves may become higher powered at higher levels (as opponents evolve)
            case MoveIDs.flail:
            case MoveIDs.reversal:
                return 60; //in most cases, it will be 20, 40, or 80; this is a rough estimate of the mean
            case MoveIDs.returnTheMoveNotTheKeyword:
            case MoveIDs.frustration:
                return 50; //if trainer pokemon have neutral happiness (127), then the power is 50.
            //I *think* that is always the case.
            case MoveIDs.present:
                return 40; //.4 * 40 + .3 * 80 + .1 * 120 + .2 * -60 (for healing 1/4 hp)
            case MoveIDs.magnitude:
                return 71;
            case MoveIDs.hiddenPower:
                return 50; //ranges from 30-70, I presume in a linear fashion
            case MoveIDs.spitUp:
                return 100; //actually ranges from 0-300, but 100 seems reasonable given the prep time.
            case MoveIDs.endeavor:
                return 100; //a bit arbitrary,
        }

        return 0;
    }

    private double generateUniquePowerMultiplier(Move move) {
        switch (move.internalId) {
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                if(romHandler.generationOfPokemon() <= 4) {
                    return 2; //These moves halve defence (effectively doubling power)
                }
            case MoveIDs.tripleKick:
                if(romHandler.generationOfPokemon() == 2) {
                    return 1.5;
                } else {
                    return 2; //each strike gains power. Accuracy differs in gen 2.
                }
            case MoveIDs.rollout:
            case MoveIDs.iceBall:
            case MoveIDs.furyCutter:
                return 2.5; //doubles with each hit, 2 is usually reasonable to expect (and then averaged and rounded)
                //though, the AI probably doesn't use Fury Cutter correctly...
            case MoveIDs.beatUp:
                //TODO: make this a synergy instead
                return 3;
            case MoveIDs.facade:
                return 1.1; //without synergy, not an amazing effect
            case MoveIDs.focusPunch:
                //move has a high failure rate
                return 0.5;
            case MoveIDs.smellingSalts:
                //useless without synergy
                return 1;
            case MoveIDs.eruption:
            case MoveIDs.waterSpout:
                return 0.8; //usually will get an attack off at full health?
            case MoveIDs.futureSight:
            case MoveIDs.doomDesire:
                return 0.95; //the neutral damage type mostly cancels the delay
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && romHandler.generationOfPokemon() == 1) {
                    return 2.5; //Multi-turn incapacitating moves
                }
                return 1;
        }
    }

    private int generateUniqueSecondaryValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                return -150; //for causing faint
            case MoveIDs.skullBash:
                if(romHandler.generationOfPokemon() > 1) {
                    return 40; //for raising defence
                } else {
                    return 0;
                }
            case MoveIDs.thrash:
            case MoveIDs.outrage:
            case MoveIDs.petalDance:
                return -36; //90 for confusion, divided by 2.5 turns
            case MoveIDs.rage:
                return 30; //attack boost at fairly high reliability
            case MoveIDs.thief:
            case MoveIDs.covet:
                return 5; //deliberately undervaluing this effect since it's annoying for players
            case MoveIDs.pursuit:
                return 10; //good, but very situational
            case MoveIDs.rapidSpin:
                return 10; //doesn't apply to many effects
            case MoveIDs.fakeOut:
                if(romHandler.generationOfPokemon() <= 3) {
                    return 0; //does not factor in auto-flinch, so that cancels out
                } else {
                    return -40; //only usable on first turn
                }
            case MoveIDs.uproar:
                //this effect is marginal without synergy
                return 10;
            case MoveIDs.brickBreak:
            case MoveIDs.knockOff:
                return 15; //narrow, but very useful when applicable
            case MoveIDs.secretPower:
                return 25; //30% to do various things, many of them valued at 100.
            case MoveIDs.skyUppercut:
                return 10; //REALLY narrow
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId)) {
                    if(romHandler.generationOfPokemon() == 1) {
                        return 100; //essentially, 100% chance to flinch
                    } else {
                        return 30; //trap temporarily, plus a tiny bit of damage
                    }
                }
                return 0;
        }
    }

    private double generateUniqueStatusMoveValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.whirlwind:
            case MoveIDs.roar:
                if(romHandler.generationOfPokemon() == 1) {
                    return 0;
                } else {
                    return 40;
                }
            case MoveIDs.disable:
                return 70;
            case MoveIDs.encore:
                return 65;
            case MoveIDs.torment:
                return 60;
            case MoveIDs.taunt:
                return 70;
            case MoveIDs.imprison:
                return 10; //near useless without synergy
            case MoveIDs.substitute:
            case MoveIDs.mist:
                return 50;
            case MoveIDs.haze:
                return 60;
            case MoveIDs.healBell:
            case MoveIDs.aromatherapy:
            case MoveIDs.safeguard:
            case MoveIDs.refresh:
                return 70;
            case MoveIDs.leechSeed:
                return 60;
            case MoveIDs.ingrain:
                return 40; //too little healing to be much good
            case MoveIDs.mirrorMove:
            case MoveIDs.copycat:
                return 50;
            case MoveIDs.mimic:
            case MoveIDs.sketch:
                return 55;
            case MoveIDs.transform:
                return 60;
            case MoveIDs.morningSun:
            case MoveIDs.moonlight:
            case MoveIDs.synthesis:
                if(romHandler.generationOfPokemon() == 2) {
                    return 50; //if not join the 50% heal moves below
                }
            case MoveIDs.recover:
            case MoveIDs.softBoiled:
            case MoveIDs.milkDrink:
            case MoveIDs.wish: //the delay could make it worse or better, so it'll stay at 100.
            case MoveIDs.slackOff:
                return 100;
            case MoveIDs.rest:
            case MoveIDs.swallow:
                return 150;
            case MoveIDs.reflect:
            case MoveIDs.lightScreen:
                return 70;
            case MoveIDs.focusEnergy:
                return 50;
            case MoveIDs.conversion:
                return 50;
            case MoveIDs.conversion2:
                return 70;
            case MoveIDs.spiderWeb:
            case MoveIDs.meanLook:
            case MoveIDs.block:
                return 40;
            case MoveIDs.mindReader:
            case MoveIDs.lockOn:
            case MoveIDs.foresight:
            case MoveIDs.odorSleuth:
                return 50; //without synergy, both groups have the same use case
            case MoveIDs.nightmare:
                return 100; //synergy baked in
            case MoveIDs.curse:
                return 65; //the default value for the stat changes; Ghost types count as synergy.
            case MoveIDs.spite:
                return 50;
            case MoveIDs.protect:
            case MoveIDs.detect:
                return 80;
            case MoveIDs.endure:
                return 50;
            case MoveIDs.bellyDrum:
                return 160; //atk (40) * 6 stages - 80 (for half health)
            //I don't know that I value it that highly, but I'm not the kind of player that likes such a move
            case MoveIDs.spikes:
            case MoveIDs.stealthRock:
                return 40;
            case MoveIDs.toxicSpikes:
                return 70;
            case MoveIDs.destinyBond:
                return 70;
            case MoveIDs.grudge:
                return 30; //grudge is just Destiny Bond but bad
            case MoveIDs.perishSong:
                return 50;
            case MoveIDs.sandstorm:
            case MoveIDs.hail:
                return 20; //weather moves have extreme synergy but low inherent value
            case MoveIDs.rainDance:
            case MoveIDs.sunnyDay:
                return 15;
            case MoveIDs.batonPass:
                return 20; //like weather, low base value but high synergy potential
            case MoveIDs.attract:
                return 100; //Only works on half the pokemon, but twice as well as paralysis.
            case MoveIDs.yawn:
                return 100; //guaranteed means >100, but delay puts it back.
            case MoveIDs.sleepTalk:
            case MoveIDs.assist:
                return 1; //Synergy based on other moves' potency.
            case MoveIDs.painSplit:
                return 70; //...if used correctly, which im not confident the AI can do.
            case MoveIDs.psychUp:
                return 60;
            case MoveIDs.stockpile:
                return 80; //in gen 3, should be 0 without synergy
            case MoveIDs.memento:
                return 60; //160 for reducing 4 stat stages, -100 for causing faint.
            case MoveIDs.followMe:
                return 30; //another low value high synergy move
            case MoveIDs.helpingHand:
                return 50;
            case MoveIDs.metronome:
                return 50; //TODO: calculate average move value
            case MoveIDs.naturePower:
                switch (romHandler.generationOfPokemon()) {
                    case 3:
                        return averageMoveValue(MoveIDs.swift, MoveIDs.earthquake, MoveIDs.shadowBall,
                                MoveIDs.rockSlide, MoveIDs.stunSpore, MoveIDs.razorLeaf, MoveIDs.bubbleBeam,
                                MoveIDs.surf, MoveIDs.hydroPump);
                    case 4:
                    case 5:
                        return averageMoveValue(MoveIDs.triAttack, MoveIDs.earthquake, MoveIDs.rockSlide,
                                MoveIDs.seedBomb, MoveIDs.hydroPump, MoveIDs.blizzard, MoveIDs.iceBeam, MoveIDs.mudBomb);
                    case 6:
                        return averageMoveValue(MoveIDs.triAttack, MoveIDs.powerGem, MoveIDs.earthPower, MoveIDs.mudBomb,
                                MoveIDs.hydroPump, MoveIDs.frostBreath, MoveIDs.iceBeam, MoveIDs.lavaPlume,
                                MoveIDs.shadowBall, MoveIDs.airSlash, MoveIDs.dracoMeteor, MoveIDs.energyBall,
                                MoveIDs.moonblast, MoveIDs.thunderbolt);
                    case 7:
                        return averageMoveValue(MoveIDs.triAttack, MoveIDs.powerGem, MoveIDs.earthPower,
                                MoveIDs.hydroPump, MoveIDs.iceBeam, MoveIDs.lavaPlume, MoveIDs.psyshock,
                                MoveIDs.energyBall, MoveIDs.moonblast, MoveIDs.thunderbolt, MoveIDs.psychic);
                    default:
                        return 0; //unsupported generation?
                }
            case MoveIDs.swagger:
                return 80; //the two effects have synergy with each other,
                //so boosting the opponent's Attack should not count as a negative.
            case MoveIDs.trick:
            case MoveIDs.rolePlay:
            case MoveIDs.recycle:
            case MoveIDs.skillSwap:
            case MoveIDs.camouflage:
                return 5; //these moves are too weird for the AI to use correctly
            case MoveIDs.magicCoat:
            case MoveIDs.snatch:
                return 50; //potent effects, but mostly luck based to see if they work (excepting with synergy)
            case MoveIDs.mudSport:
            case MoveIDs.waterSport:
                return 30; //synergy goes up if they're weak to that type
        }
        //These are *especially* arbitrary and debatable.

        return 0;
    }

    private double averageMoveValue(int... moveIDs) {
        Move[] moves = new Move[moveIDs.length];
        for(int i = 0; i < moveIDs.length; i++) {
            moves[i] = allMoves.get(moveIDs[i]);
        }
        return averageMoveValue(moves);
    }

    private double averageMoveValue(Move... moves) {
        int count = 0;
        int totalValue = 0;
        for(Move move : moves) {
            count++;
            totalValue += getBaseValue(move);
        }
        return totalValue / (double) count;
    }
}
