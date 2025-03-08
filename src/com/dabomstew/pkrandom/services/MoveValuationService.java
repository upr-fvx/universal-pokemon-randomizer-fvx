package com.dabomstew.pkrandom.services;

import com.dabomstew.pkrandom.constants.GlobalConstants;
import com.dabomstew.pkrandom.constants.MoveIDs;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveValuationService {
    private final RomHandler romHandler;
    private final List<Move> allMoves;
    private final Map<Move, MoveValues> moveValues;
    private final int generation;

    public MoveValuationService(RomHandler romHandler) {
        this.romHandler = romHandler;
        allMoves = romHandler.getMoves();
        moveValues = new HashMap<>();
        generation = romHandler.generationOfPokemon();
    }

    public static class MoveValues{
        private final Move move;
        private int powerValue = 0;
        private int effectsValue = 0;
        private int speedDependantEffectsValue = 0;
        private int doubleBattleEffectsValue = 0;
        private double doubleBattleRangeModifier = 1;
        private int accuracy = 100;
        private double useMultiplier = 1; //for charge moves, etc

        //calculated values
        private int baseValue;
        private int totalEffectsValue;
        private int lostInaccuracyValue;
        private boolean finalized = false;


        public MoveValues(Move move) {
            this.move = move;
        }

        public MoveValues(Move move, int baseValue, int powerValue, int speedDependantEffectsValue) {
            this.move = move;
            this.baseValue = baseValue;
            this.powerValue = powerValue;
            this.speedDependantEffectsValue = speedDependantEffectsValue;
        }
        private void setPowerValue(int value) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            powerValue = value;
        }

        private void setEffectsValue(int effectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.effectsValue = effectsValue;
        }

        private void setSpeedDependantEffectsValue(int speedDependantEffectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.speedDependantEffectsValue = speedDependantEffectsValue;
        }

        private void setAccuracy(int accuracy) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.accuracy = accuracy;
        }

        public void setUseMultiplier(double useMultiplier) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.useMultiplier = useMultiplier;
        }

        public void setDoubleBattleEffectsValue(int doubleBattleEffectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.doubleBattleEffectsValue = doubleBattleEffectsValue;
        }

        public void setDoubleBattleRangeModifier(double doubleBattleRangeModifier) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.doubleBattleRangeModifier = doubleBattleRangeModifier;
        }

        private void calculateValues() {
            finalized = true;

            totalEffectsValue = powerValue + effectsValue + (speedDependantEffectsValue / 2)
                    + (doubleBattleEffectsValue / 2);
            int perUseValue = (totalEffectsValue * accuracy) / 100;
            double finalMultiplier = useMultiplier * ((doubleBattleRangeModifier + 1) / 2.0);
            baseValue = (int) (perUseValue * finalMultiplier);
            lostInaccuracyValue = (int) ((totalEffectsValue - perUseValue) * finalMultiplier);
        }

        public int getBaseValue() {
            if(!finalized) {
                throw new IllegalStateException("Attempted to get value of non-finalized move!");
            }

            return baseValue;
        }


    }

    public List<Move> getAllMoves() {
        return new ArrayList<>(allMoves);
    }

    public int getBaseValue(int moveID) {
        return getBaseValue(allMoves.get(moveID));
    }

    public int getBaseValue(Move move) {
        if(!moveValues.containsKey(move)) {
            MoveValues value;
            if(move.category != MoveCategory.STATUS) {
                value = generateDamagingMoveValues(move);
            } else {
                value = generateStatusMoveValue(move);
            }

            moveValues.put(move, value);
        }

        return moveValues.get(move).getBaseValue();
    }

    private MoveValues generateDamagingMoveValues(Move move) {
        MoveValues values = new MoveValues(move);

        int power = move.power;
        if(power <= 1) {
            power = generateUniqueDamagingMovePower(move);
        }
        double powerValue = power;

        if(move.hitCount > 1) {
            powerValue *= move.hitCount;
        } else {
            powerValue *= generateUniquePowerMultiplier(move);
        }

        powerValue += move.absorbPercent / 100.0 * power;
        powerValue -= (move.recoilPercent / 100.0 * power) / 2;

        powerValue += power * generateCriticalChanceMultiplier(generation, move.criticalChance);
        values.setPowerValue((int)powerValue);


        int effectsValue = 0;

        if(move.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            effectsValue += (int) generateStatChangeValue(move);
        }

        if(move.statusPercentChance != 0) {
            effectsValue += (int) ((move.statusPercentChance / 100) * generateStatusConditionValue(move));
        }

        int priority = move.priority;
        if(romHandler.generationOfPokemon() == 2) {
            priority--; //All gen 2 moves have one higher priority than typical
        }
        int priorityValue;
        if(priority > 0 ) {
            priorityValue = 10 + priority;
        } else if (priority < 0) {
            priorityValue = -10 + priority;
        } else {
            priorityValue = 0;
        }
        effectsValue += priorityValue;

        if(GlobalConstants.semiInvulnerableMoves.contains(move)) {
            effectsValue += 10;
        }

        effectsValue += generateUniqueEffectsValue(move);

        values.setEffectsValue(effectsValue);


        int speedValue;
        if(move.flinchPercentChance > 0 && move.flinchPercentChance < 1) {
            speedValue = (int) (move.flinchPercentChance * 100);
        } else {
            speedValue = (int) move.flinchPercentChance;
        }

        speedValue += generateUniqueSpeedValue(move);

        values.setSpeedDependantEffectsValue(speedValue);


        int accuracy;
        if(romHandler.getPerfectAccuracy() != 100 && move.hitRatio == romHandler.getPerfectAccuracy()) {
            accuracy = 150;
        } else {
            switch(move.internalId) {
                case MoveIDs.swift:
                case MoveIDs.feintAttack:
                case MoveIDs.vitalThrow:
                    //because gen 1 through 3 encoded perfect-accuracy moves differently, and they can't be distinguished
                    //from other moves except by ID.
                    accuracy = 150;
                    break;
                default:
                    if(move.hitRatio < 1) {
                        accuracy = (int) (move.hitRatio * 100);
                    } else {
                        accuracy = (int) move.hitRatio;
                    }
            }
        }
        //TODO: have romHandlers standardize accuracy, so we don't need all this shenanigans

        values.setAccuracy(accuracy);

        double useMultiplier = 1;
        if(move.isChargeMove && !GlobalConstants.semiInvulnerableMoves.contains(move.internalId)) {
            useMultiplier = .5;
        }
        if(move.isRechargeMove)
            useMultiplier = .75;

        if(move.pp < 10) {
            useMultiplier *= .95;
        }

        useMultiplier *= generateUniqueUseLimitValue(move);

        values.setUseMultiplier(useMultiplier);

        values.setDoubleBattleEffectsValue(generateUniqueDoubleBattleEffectsValue(move));
        values.setDoubleBattleRangeModifier(generateDoubleBattleRangeModifier(move));

        values.calculateValues();

        return values;
    }

    private MoveValues generateStatusMoveValue(Move move) {
        MoveValues values = new MoveValues(move);

        if(GlobalConstants.uselessMoves.contains(move.internalId)) {
            values.effectsValue = 0;
            values.calculateValues();
            return values;
        }

        int effectsValue = 0;

        if(move.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            effectsValue += (int) generateStatChangeValue(move);
        }
        if(move.statusMoveType != StatusMoveType.NONE_OR_UNKNOWN) {
            effectsValue += generateStatusConditionValue(move);
        }
        effectsValue += generateUniqueEffectsValue(move);

        values.setEffectsValue(effectsValue);

        int accuracy;
        if(romHandler.getPerfectAccuracy() != 100 && move.hitRatio == romHandler.getPerfectAccuracy()) {
            accuracy = 110; //perfect accuracy is less valuable for status moves than attack moves
        } else {
            if(move.hitRatio < 1) {
                accuracy = (int) (move.hitRatio * 100);
            } else {
                accuracy = (int) move.hitRatio;
            }
        }

        values.setAccuracy(accuracy);

        values.setSpeedDependantEffectsValue(generateUniqueSpeedValue(move));

        double useRestrictionMultiplier = 1;
        if(move.isChargeMove && !GlobalConstants.semiInvulnerableMoves.contains(move.internalId)) {
            useRestrictionMultiplier = .5;
        }
        if(move.isRechargeMove)
            useRestrictionMultiplier = .75;

        if(move.pp < 10) {
            useRestrictionMultiplier *= .9;
        }
        useRestrictionMultiplier *= generateUniqueUseLimitValue(move);
        values.setUseMultiplier(useRestrictionMultiplier);

        values.setDoubleBattleEffectsValue(generateUniqueDoubleBattleEffectsValue(move));
        values.setDoubleBattleRangeModifier(generateDoubleBattleRangeModifier(move));

        values.calculateValues();
        return values;
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
                case SPECIAL:
                    changeValue = 80;
                    //doubled due to being both spatk and spdef
                    //(this may not be totally correct?)
                    break;
                case ACCURACY:
                case EVASION:
                    changeValue = 60;
                    //this value makes sense for reducing accuracy or increasing evasion,
                    //not so much the opposite...
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
            case TOXIC_POISON:
                return 70;
            case BURN:
                return 80; //all the DOT effects get boosts from defensive moves/stats
            case CONFUSION:
                return 90; //would be much higher, except it's temporary
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
            case MoveIDs.lowKick:
            case MoveIDs.grassKnot:
                return 60;
                //median weight is around 30 kg
                //this puts it into the 60-power zone (25-50kg)
            case MoveIDs.heavySlam:
            case MoveIDs.heatCrash:
                return 40;
                //in a vacuum (i.e. with two random pokemon) the target's weight being less than half the
                // user's is unlikely.
                //Of course, synergy can increase this dramatically.
            case MoveIDs.electroBall:
                return 40; //base; the rest is speed-dependent
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
            case MoveIDs.gyroBall:
                return 150; //maximum power, reduced by SpeedEffect.
            case MoveIDs.naturalGift:
                return 70; //middle of possible powers (IF they have a berry)
            case MoveIDs.trumpCard:
                return 60; //a guess at median, but this is a weird move
            case MoveIDs.wringOut:
            case MoveIDs.crushGrip:
                return 100; //120 for first use, but then gets worse
            case MoveIDs.punishment:
                return 100; //60 + assume two stat stages as typical
            case MoveIDs.beatUp:
                return 14; //base attack / 10 + 5 is generally in the 12-17 range
        }

        return 0;
    }

    private double generateUniquePowerMultiplier(Move move) {
        switch (move.internalId) {
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                if(romHandler.generationOfPokemon() <= 4) {
                    return 2; //These moves halve defence (effectively doubling power)
                } else {
                    return 1;
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
            case MoveIDs.echoedVoice:
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
            case MoveIDs.wakeUpSlap:
                //useless without synergy
                return 1;
            case MoveIDs.eruption:
            case MoveIDs.waterSpout:
                return 0.8; //usually will get an attack off at full health?
            case MoveIDs.futureSight:
            case MoveIDs.doomDesire:
                return 0.95; //the neutral damage type mostly cancels the delay
            case MoveIDs.brine:
                return 1.25; //doubled at a time that may be important
            case MoveIDs.payback:
            case MoveIDs.avalanche:
                return 1.8; //doubled if opponenent damages (assumed at .8)
                //payback also requires being slower, so that's in speed dependent
            case MoveIDs.assurance:
                return 1; //needs synergy or double battle to pull off
            case MoveIDs.rage:
                if(generation == 2) {
                    return 3; //each time damaged, increases multiplier by 1; 3 is probably a reasonable assumption?
                } else {
                    return 1;
                }
            case MoveIDs.chipAway:
            case MoveIDs.sacredSword:
                return 1.5; //ignores enemy tanking
            case MoveIDs.storedPower:
                return 1; //common synergy multiplier
            case MoveIDs.hex:
                return 1; //needs synergy for higher multiplier
            case MoveIDs.acrobatics:
                return 2; //most trainers have no held items, so default to this, synergy it back down.
            case MoveIDs.retaliate:
                return 1.1; //most likely will get only one good one in. Switching synergy may increase this.
            case MoveIDs.psyshock:
            case MoveIDs.psystrike:
            case MoveIDs.secretSword:
                return 1; //not inherently valuable, but useful in combination with special moves
            case MoveIDs.freezeDry:
                return 1.2; //good, but only against Water-types
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && romHandler.generationOfPokemon() == 1) {
                    return 2.5; //Multi-turn incapacitating moves
                }
                return 1;
        }
    }

    private static double generateCriticalChanceMultiplier(int generation, CriticalChance criticalChance) {
        double baseCritChance; //base crit chance, out of 1
        double critChanceIncrease; //amount crit chance increases for "increased" crit chance moves
        //(base chance + increase = actual chance for "increased" moves)
        double critMultiplier; //damage multiplier for a critical hit
        switch (generation) {
            case 1:
                baseCritChance = .1; //actually dependent on base speed, generally in the range of 7-20%
                critChanceIncrease = .7; // eight times more likely
                critMultiplier = 1.85; //dependent on level, but 1.8 at l20 and 1.95 at l95
                break;
            case 2:
                baseCritChance = .0664; //doesn't need to be this specific, but it doesn't hurt
                critChanceIncrease = .25 - baseCritChance;
                critMultiplier = 2;
                break;
            case 3:
            case 4:
            case 5:
                baseCritChance = .0625;
                critChanceIncrease = .0625;
                critMultiplier = 2;
                break;
            case 6:
                baseCritChance = .0625;
                critChanceIncrease = .0625;
                critMultiplier = 1.5;
                break;
            case 7:
            case 8:
            case 9:
                baseCritChance = .0417;
                critChanceIncrease = .125 - baseCritChance;
                critMultiplier = 1.5;
                break;
            default:
                throw new IllegalArgumentException("Crit chance calculator does not support generation " + generation + "!");
        }

        switch (criticalChance) {
            case NORMAL:
                return 0;
            case NONE:
                return baseCritChance * critMultiplier * -1;
            case INCREASED:
                return critChanceIncrease * critMultiplier;
            case GUARANTEED:
                return critMultiplier; //technically this probably should be times 1 - base chance, but...
                // this *feels* more correct, even if it isn't.
            default:
                //should never occur
                throw new IllegalArgumentException("Unsupported critical chance: " + criticalChance.name());
        }
    }

    private int generateUniqueSpeedValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.gyroBall:
                return -250; //at speed neutral, power is 25.
                // (Ideally there'd be a way to do this which put this factor into power,
                // but that seems too specific to bother. (100 counting STAB isn't that great for a damage move anyway.))
            case MoveIDs.payback:
                return -50; //hmm... maybe we do need a speed-based power value?
            case MoveIDs.electroBall:
                return 110; //also speed-dependent-power.
            case MoveIDs.metalBurst:
                return -120; //no effect if moves first.
            case MoveIDs.copycat:
            case MoveIDs.mimic:
            case MoveIDs.mirrorMove:
            case MoveIDs.sketch:
                return 30; //Allows making choices about the move copied (although I'm not sure the AI *does* that)
            case MoveIDs.destinyBond:
                return 40; //if faster, player can't avoid the kill
            case MoveIDs.meFirst:
                return 70; //only works on damaging moves, but buffs them.
            case MoveIDs.trickRoom:
                return -80; //I guess? Like Tailwind for slow pokemon.

            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && generation == 1) {
                    return 100; //essentially a 100% chance to flinch
                }
                return 0;
        }
    }

    private double generateUniqueUseLimitValue(Move move) {
        switch(move.internalId) {
            //Moves with multiple turns:
            case MoveIDs.thrash:
            case MoveIDs.outrage:
            case MoveIDs.petalDance:
                return .85; //after 2-3 turns (.7), become confused (.5) (multiply inverses together)
            case MoveIDs.bide:
                return .33; //two turns prep
            case MoveIDs.swallow:
            case MoveIDs.spitUp:
                return .5; //need to use Stockpile at least once first
            //TODO: consider if standard multi-turn moves (rollout, uproar, etc) get a penalty

            //Moves that depend on opponent using certain moves:
            //(Not totally sure if these should be in this category)
            case MoveIDs.suckerPunch:
            case MoveIDs.meFirst:
                return .8; //opponent must use damaging move (which is pretty likely)
            case MoveIDs.counter:
            case MoveIDs.mirrorCoat:
                return .4; //opponent must use correct category of damaging move (unlikely)
            case MoveIDs.feint:
                if(generation == 4) {
                    return .1; //depends on opponent using protect/detect
                } else {
                    return 1; //works normally
                }

            //unique conditions
            case MoveIDs.fakeOut:
            case MoveIDs.matBlock:
                return .5; //only first turn
            case MoveIDs.naturalGift:
                return .5; //only once and consumes a potentially-useful resource
            case MoveIDs.lastResort:
                return .7; //have to use all other moves first
                //(This partially depends on if the AI uses it correctly)
            case MoveIDs.captivate:
            case MoveIDs.attract:
                return .49; //only works on half (ish) of opponents
            case MoveIDs.synchronoise:
                return .07; //if my math is right, this is the chance of two Pokemon sharing a type,
                //assuming that each Pokemon is a random type combination.
                //(Since types are not all equally common, this is not entirely accurate.)
            case MoveIDs.powder:
                return .25; //only works against fire moves. Honestly, this is generous.

            case MoveIDs.belch:
                return .9; //this is mostly a synergy deal

            default:
                return 1;
        }

    }

    private int generateUniqueEffectsValue(Move move) {
        switch (move.internalId) {
            //non-power damaging moves
            case MoveIDs.superFang:
                return 100; //half HP
            case MoveIDs.endeavor:
                return 100; //a bit arbitrary, but "on average" about half HP?
            case MoveIDs.finalGambit:
                return 50; //like endeavor, "on average" full HP, but -150 for causing faint.
            case MoveIDs.guillotine:
            case MoveIDs.hornDrill:
            case MoveIDs.fissure:
            case MoveIDs.sheerCold:
                return 200; //instakill
            case MoveIDs.psywave:
                return 30;
            case MoveIDs.sonicBoom:
            case MoveIDs.seismicToss:
            case MoveIDs.nightShade:
                return 60;
                //seismic toss/night shade peak around l20
                //(Well, actually they mostly improve with level, but l20 is around when evolutions start happening)
            case MoveIDs.dragonRage:
                return 80; //static damage moves will be given synergy multipliers based on level
            case MoveIDs.counter:
            case MoveIDs.mirrorCoat:
            case MoveIDs.metalBurst:
            case MoveIDs.bide:
                return 120; //not really sure how to handle the retaliate moves, but they're strong when they work

            //damaging moves' drawbacks
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                return -150; //for causing faint

            //TODO: sort these
            case MoveIDs.secretPower:
                return 25; //30% to do various things, many (but not all) of them valued at 100.
            //these super narrow ones probably should just get 0s tbh
            case MoveIDs.brickBreak:
            case MoveIDs.knockOff:
                return 15; //narrow, but very useful when applicable
            case MoveIDs.skyUppercut:
                return 5; //REALLY narrow
            case MoveIDs.pursuit:
                return 10; //good, but very situational
            case MoveIDs.uproar:
                //this effect is marginal without synergy
                return 10;

            //disabling moves
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
            case MoveIDs.attract:
                return 200; //stupid powerful, when it works
            case MoveIDs.yawn:
                return 100; //guaranteed means >100, but delay puts it back.

            //copying moves
            case MoveIDs.mirrorMove:
            case MoveIDs.copycat:
                return 50;
            case MoveIDs.mimic:
            case MoveIDs.sketch:
                return 55;
            case MoveIDs.transform:
                return 80;

            //other calling moves
            case MoveIDs.sleepTalk:
            case MoveIDs.assist:
                return 1; //Synergy based on other moves' potency.
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

            //status prevention/healing
            case MoveIDs.mist:
                return 50;
            case MoveIDs.haze:
            case MoveIDs.clearSmog:
                return 60;
            case MoveIDs.healBell:
            case MoveIDs.aromatherapy:
            case MoveIDs.safeguard:
            case MoveIDs.refresh:
            case MoveIDs.substitute:
                return 70;
            case MoveIDs.mistyTerrain:
                return 65; //also protects foes... although, that doesn't usually matter without anti-synergy
            case MoveIDs.psychoShift:
                return 90; //significantly better than just a status heal, but still needs a status effect to do anything

            //damage over time
            case MoveIDs.nightmare:
                return 100; //synergy baked in
            case MoveIDs.curse:
                return 40; //technically default for the stat changes, but the speed change isn't that important
            case MoveIDs.spikes:
            case MoveIDs.stealthRock:
                return 40;
            case MoveIDs.toxicSpikes:
                return 70;
            case MoveIDs.sandstorm:
            case MoveIDs.hail:
                return 20; //weather moves have extreme synergy but low inherent value

            //HP recovery
            case MoveIDs.leechSeed:
                return 60;
            case MoveIDs.ingrain:
                return 40; //too little healing to be much good
            case MoveIDs.aquaRing:
                return 50; //same, but without the drawback
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
            case MoveIDs.healOrder:
                return 100;
            case MoveIDs.roost:
                return 95; //Removes immunities, so slightly worse than other 50% heal moves.
            case MoveIDs.rest:
            case MoveIDs.swallow:
                return 150; //(up to) full heal
            case MoveIDs.healingWish:
            case MoveIDs.lunarDance:
                return 5; //The AI doesn't typically switch pokemon enough to make this work.
                //Synergy for those with switch moves, though.
                // (But it has to be switch moves on the rest of the party... hmm.)
            case MoveIDs.painSplit:
                return 70; //...if used correctly, which im not confident the AI can do.

            //stat change+
            case MoveIDs.bellyDrum:
                return 165; //atk (40) * 6 stages - 75 (for half health)
                //I don't know that I value it that highly, but I'm not the kind of player that likes such a move
            case MoveIDs.memento:
                return 60; //160 for reducing 4 stat stages, -100 for causing faint.
            case MoveIDs.focusEnergy:
                return 50; //sort of a stat change
            case MoveIDs.luckyChant:
                return 50; //inverse of focus energy
            case MoveIDs.mindReader:
            case MoveIDs.lockOn:
            case MoveIDs.foresight:
            case MoveIDs.odorSleuth:
            case MoveIDs.miracleEye:
            case MoveIDs.telekinesis:
                return 50; //without synergy (including <100% accurate moves), both groups have the same use case
            case MoveIDs.stockpile:
                if(generation == 3) {
                    return 1; //does nothing on its own
                } else {
                    return 80; //raises def + spdef
                }
            case MoveIDs.psychUp:
                return 60; //weird stat change
            case MoveIDs.swagger:
                return 80; //the two effects have synergy with each other,
                //so boosting the opponent's Attack should not count as a negative.
            case MoveIDs.tailwind:
                return 80; //double speed is a lot, but has short duration.
            case MoveIDs.trickRoom:
                return 80; //similar to tailwind, except with a speed penalty
            case MoveIDs.acupressure:
                return 80; //+2 any stat. Can hit acc/eva, but no choice, so evens out.
            case MoveIDs.powerTrick:
            case MoveIDs.wonderRoom:
                return 30; //tricky, doubt the AI knows how to use effectively
            case MoveIDs.powerSplit:
            case MoveIDs.guardSplit:
                return 20; //entirely dependent on synergy (poor stats) to determine if it's useful
            case MoveIDs.powerSwap:
            case MoveIDs.guardSwap:
                return 20; //without synergy, is likely to do nothing
            case MoveIDs.heartSwap:
                return 40; //somewhat more likely to have effect
            case MoveIDs.skullBash:
                if(romHandler.generationOfPokemon() > 1) {
                    return 40; //for raising defence
                } else {
                    return 0;
                }
            case MoveIDs.rage:
                if(generation == 2) {
                    return 0; //in gen 2, is a power multiplier instead.
                } else {
                    return 32; //attack boost (40) contingent on opponent damaging (*.8)
                }
            case MoveIDs.defog:
                return 70; //lower evasion + essentially rapid spin
                //has some negative synergies
            case MoveIDs.shellSmash:
                return 160; //gain 6 stages, lose 2, all of them 40-value stats
            case MoveIDs.rototiller:
                return 80; //1 stage in 2 stats... when it works (synergy)
            case MoveIDs.flowerShield:
                return 35; //1 stage, and it might also give it to opponents. (synergy makes better).
            case MoveIDs.stickyWeb:
                return 80; //1 stage on each new pokemon; 2 is a reasonable estimate. May do synergy: front of party.
            case MoveIDs.fellStinger:
                if(generation == 6) {
                    return 40; //two stages, but only if you can pull off the kill
                } else {
                    return 70; //now three stages (and easier to do)
                }
            case MoveIDs.topsyTurvy:
                return 60; //if opponent has any stat buffs, very solid. also potential synergy
            case MoveIDs.diamondStorm:
                if(generation == 6) {
                    return 20; //50% to raise one stage
                } else {
                    return 40; //now two stages
                }
            case MoveIDs.venomDrench:
                return 120; //three stats by 1; requires synergy


            //protect / damage reduction
            case MoveIDs.protect:
            case MoveIDs.detect:
                return 80;
            case MoveIDs.matBlock:
                return 60; //only works on damaging moves
            case MoveIDs.craftyShield:
                return 30; //only works on status moves
            case MoveIDs.kingsShield:
                if(generation <= 7) {
                    return 130; //blocks damaging moves PLUS reduces attack if contacted PLUS form change
                    //although, maybe the form change should be a synergy thing? hmm.
                } else {
                    return 110; //attack reduction nerfed. still good.
                }
            case MoveIDs.spikyShield:
                return 95; //blocks all move plus damages if contacted
            case MoveIDs.wideGuard:
            case MoveIDs.quickGuard:
                return 20; //only work on small subset of moves
            case MoveIDs.endure:
                return 50;
            case MoveIDs.reflect:
            case MoveIDs.lightScreen:
                return 70;
            case MoveIDs.mudSport:
            case MoveIDs.waterSport:
                return 30; //synergy goes up if they're weak to that type

            //item shenanigans
            //most of these are deliberately undervalued due to being annoying to players
            //(as they can result in permanent item loss AFAIK)
            case MoveIDs.thief:
            case MoveIDs.covet:
                return 5;
            case MoveIDs.trick:
            case MoveIDs.switcheroo:
                return 5; //AI doesn't know how to use these moves anyway
            case MoveIDs.recycle:
                return 5; //not an annoying one, but I don't think the AI knows how to use it
            case MoveIDs.fling:
                return 1; //power entirely depends on held item
            case MoveIDs.embargo:
                return 60; //hard to value, but pretty solid
            case MoveIDs.magicRoom:
                return 40; //doesn't prevent active items, so less useful
            case MoveIDs.pluck:
            case MoveIDs.bugBite:
                return 15; //narrow but useful
                //(Not undervalued because berries are consumable anyway)
            case MoveIDs.incinerate:
                return 10; //only destroys it, rather than stealing, but still a bit useful
            case MoveIDs.bestow:
                return 5; //without synergy, is actively bad

            //switch effects
            case MoveIDs.whirlwind:
            case MoveIDs.roar:
            case MoveIDs.circleThrow:
            case MoveIDs.dragonTail:
                //switch opponent
                if(generation == 1) {
                    return 0;
                } else {
                    return 50;
                }
            case MoveIDs.uTurn:
            case MoveIDs.batonPass:
            case MoveIDs.voltSwitch:
            case MoveIDs.partingShot:
                //switch user
                //not that *inherently* useful (since switching is just an option, even if the AI doesn't often use it),
                //but get around trap moves & can be synergized
                return 30;
            case MoveIDs.teleport:
                if(generation < 8) {
                    return 0;
                } else {
                    return 30;
                }

            //type changes
            case MoveIDs.conversion:
                return 50;
            case MoveIDs.conversion2:
                return 90; //guaranteed resistance
            case MoveIDs.soak:
                return 50; //better with synergy
            case MoveIDs.reflectType:
                return 70; //usually gives resistance
            case MoveIDs.trickOrTreat:
            case MoveIDs.forestsCurse:
                return 70; //need synergy to be good. (easy synergy though)

            //ability changes
            case MoveIDs.gastroAcid:
                return 90; //depends on foe, but usually pretty good
            case MoveIDs.worrySeed:
                return 92; //also prevents Rest
                //(Has anti-synergy with inflicting sleep, obviously)
            case MoveIDs.simpleBeam:
                return 80; //could end up increasing opponent's stats
                //(Has synergy with stat reductions)
            case MoveIDs.entrainment:
                return 80; //depends largely on the user's ability,
                //but this seems like a decent base value

            //work despite protection
            case MoveIDs.feint:
            case MoveIDs.shadowForce:
            case MoveIDs.phantomForce:
            case MoveIDs.hyperspaceHole:
            case MoveIDs.hyperspaceFury:
                return 10; //works through protect/detect/etc
            //not putting the substitute-piercing moves because a: that's very specific and b: there's so many
            case MoveIDs.thousandArrows:
                return 30; //works against ungrounded, + grounds them

            //other effects
            case MoveIDs.spiderWeb:
            case MoveIDs.meanLook:
            case MoveIDs.block:
            case MoveIDs.thousandWaves:
                return 40; //trap moves
            case MoveIDs.fairyLock:
                return 15; //trap, but only for one turn.
            case MoveIDs.spite:
                return 50;
            case MoveIDs.grudge:
                return 30; //grudge is just Destiny Bond but bad
            case MoveIDs.rainDance:
            case MoveIDs.sunnyDay:
                return 15; //weather moves do barely anything without synergy
            case MoveIDs.grassyTerrain:
            case MoveIDs.electricTerrain:
            case MoveIDs.psychicTerrain:
                return 15; //same for terrains
            case MoveIDs.gravity:
                return 15;
            case MoveIDs.destinyBond:
                return 50;
            case MoveIDs.perishSong:
                return 30; //without synergy, just forces a switch
            case MoveIDs.rolePlay:
            case MoveIDs.skillSwap:
            case MoveIDs.camouflage:
                return 5; //these moves are too weird for the AI to use correctly
            case MoveIDs.magicCoat:
            case MoveIDs.snatch:
                return 30; //potent effects, but mostly luck based to see if they work
            // (excepting with very specific synergy)
            case MoveIDs.healBlock:
                return 40; //potent, but situational
            case MoveIDs.magnetRise:
                return 40; //become immune to a category of moves
                //(Very common synergy for types)
            case MoveIDs.smackDown:
                return 20; //remove opponent's immunity to move category
                //(synergy dependent)
            case MoveIDs.thunderFang:
                return 5; //has synergy with ITSELF (paralysis+flinch) but it's still a small chance
            case MoveIDs.rapidSpin:
                return 10; //doesn't apply to many effects
            case MoveIDs.foulPlay:
                return 0; //not inherently valuable, but good if the user has low attack
            case MoveIDs.autotomize:
                return 2; //I think there's slightly more punish-heavy moves than punish-light moves,
                //but not many of each.
            case MoveIDs.ionDeluge:
                return 20; //might make a resist?
            case MoveIDs.electrify:
                return 40; //at least doesn't need it to be Normal type. still kinda bad.
            case MoveIDs.powder:
                return 80; //protect plus damage! when it works
            case MoveIDs.falseSwipe:
            case MoveIDs.holdBack:
                return -5; //actively bad in a trainer battle


            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId)) {
                    if(romHandler.generationOfPokemon() == 1) {
                        return 100; //essentially, 100% chance to flinch
                    } else {
                        return 30; //trap temporarily, plus a tiny bit of damage
                    }
                }
                if(GlobalConstants.semiInvulnerableMoves.contains(move.internalId)) {
                    return 80; //about as good as a protect move
                }
                return 0;
        }
    }

    private int generateUniqueDoubleBattleEffectsValue(Move move) {
        switch(move.internalId) {
            case MoveIDs.assurance:
                //this is also dependent on speed, not sure how to handle that
                //ignoring it for now
                return 50;
            case MoveIDs.followMe:
            case MoveIDs.ragePowder:
                return 30; //low value, high synergy. Synergizes with defenses.
            case MoveIDs.helpingHand:
                return 50;
            case MoveIDs.feint:
            case MoveIDs.shadowForce:
            case MoveIDs.phantomForce:
            case MoveIDs.hyperspaceHole:
            case MoveIDs.hyperspaceFury:
                return 10; //lifts protect/detect/etc
            case MoveIDs.flameBurst:
                return 5; //very small amount of damage
            case MoveIDs.afterYou:
            case MoveIDs.quash:
                return 10; //even for a fast pokemon with a slow partner, not that great
            case MoveIDs.allySwitch:
                return 5; //kinda useless tbh?
            case MoveIDs.healPulse:
                return 100; //half health healing
            case MoveIDs.waterPledge:
            case MoveIDs.firePledge:
            case MoveIDs.grassPledge:
                return 0; //synergy only
            case MoveIDs.fusionFlare:
            case MoveIDs.fusionBolt:
                return 0; //synergy only and also hard to use even then
        }

        return 0;
    }

    private double generateDoubleBattleRangeModifier(Move move) {
        switch(move.internalId) {
            //this one I feel like should be a field on the move.
            //But it isn't, so whatever.
            case MoveIDs.acid:
            case MoveIDs.airCutter:
            case MoveIDs.blizzard:
            case MoveIDs.bubble:
            case MoveIDs.captivate:
            case MoveIDs.clangingScales:
            case MoveIDs.coreEnforcer:
            case MoveIDs.darkVoid:
            case MoveIDs.dazzlingGleam:
            case MoveIDs.disarmingVoice:
            case MoveIDs.electroweb:
            case MoveIDs.eruption:
            case MoveIDs.glaciate:
            case MoveIDs.growl:
            case MoveIDs.healBlock:
            case MoveIDs.heatWave:
            case MoveIDs.icyWind:
            case MoveIDs.hyperVoice:
            case MoveIDs.incinerate:
            case MoveIDs.landsWrath:
            case MoveIDs.leer:
            case MoveIDs.muddyWater:
            case MoveIDs.originPulse:
            case MoveIDs.overdrive:
            case MoveIDs.powderSnow:
            case MoveIDs.precipiceBlades:
            case MoveIDs.razorLeaf:
            case MoveIDs.razorWind:
            case MoveIDs.relicSong:
            case MoveIDs.rockSlide:
            case MoveIDs.snarl:
            case MoveIDs.spikes:
            case MoveIDs.stealthRock:
            case MoveIDs.stickyWeb:
            case MoveIDs.stringShot:
            case MoveIDs.struggleBug:
            case MoveIDs.sweetScent:
            case MoveIDs.swift:
            case MoveIDs.tailWhip:
            case MoveIDs.toxicSpikes:
            case MoveIDs.twister:
            case MoveIDs.waterSpout:
                //hits both foes (or foe side of field)
                return 2;

            case MoveIDs.boomburst:
            case MoveIDs.bulldoze:
            case MoveIDs.brutalSwing:
            case MoveIDs.corrosiveGas:
            case MoveIDs.earthquake:
            case MoveIDs.discharge:
            case MoveIDs.explosion:
            case MoveIDs.lavaPlume:
            case MoveIDs.magnitude:
            case MoveIDs.mistyExplosion:
            case MoveIDs.parabolicCharge:
            case MoveIDs.petalBlizzard:
            case MoveIDs.searingShot:
            case MoveIDs.selfDestruct:
            case MoveIDs.sludgeWave:
            case MoveIDs.sparklingAria:
            case MoveIDs.synchronoise:
            case MoveIDs.teeterDance:
            case MoveIDs.mindBlown:
                //hits both foes, but also ally
                return 1.2;

            case MoveIDs.magicRoom:
            case MoveIDs.fairyLock:
                return 2;
                //effects all pokemon - allies are dealt with via synergy

            case MoveIDs.aromatherapy:
            case MoveIDs.auroraVeil:
            case MoveIDs.craftyShield:
            case MoveIDs.healBell:
            case MoveIDs.lightScreen:
            case MoveIDs.lifeDew:
            case MoveIDs.luckyChant:
            case MoveIDs.matBlock:
            case MoveIDs.mist:
            case MoveIDs.quickGuard:
            case MoveIDs.reflect:
            case MoveIDs.safeguard:
            case MoveIDs.tailwind:
            case MoveIDs.wideGuard:
            case MoveIDs.jungleHealing:
                return 2;
                //affects all allies

            case MoveIDs.poisonGas:
                if(generation >= 5) {
                    return 2;
                } else {
                    return 1;
                }
            case MoveIDs.cottonSpore:
                if(generation >= 6) {
                    return 2;
                } else {
                    return 1;
                }
            case MoveIDs.surf:
                if(generation >= 4) {
                    return 1.2;
                } else {
                    return 2;
                }
            case MoveIDs.howl:
                if(generation >= 8) {
                    return 2;
                } else {
                    return 1;
                }

            case MoveIDs.gearUp:
            case MoveIDs.magneticFlux:
            case MoveIDs.expandingForce:
                return 1; //these are synergy cases
        }
        return 1;
    }

    private int averageMoveValue(int... moveIDs) {
        Move[] moves = new Move[moveIDs.length];
        for(int i = 0; i < moveIDs.length; i++) {
            moves[i] = allMoves.get(moveIDs[i]);
        }
        return averageMoveValue(moves);
    }

    private int averageMoveValue(Move... moves) {
        int count = 0;
        int totalValue = 0;
        for(Move move : moves) {
            count++;
            totalValue += getBaseValue(move);
        }
        return totalValue / count;
    }
}
