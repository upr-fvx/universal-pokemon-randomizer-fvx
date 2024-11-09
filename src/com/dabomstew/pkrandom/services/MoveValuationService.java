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
            lostInaccuracyValue = (int) ((effectsValue - perUseValue) * finalMultiplier);
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

        switch (move.criticalChance) {
            case NONE:
                powerValue -= power * .12;
                break;
            case INCREASED:
                powerValue += power * .12;
                break;
            case GUARANTEED:
                powerValue += power;
                break;
            //TODO: make these values accurate outside of Gen 2-5
        }

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
        effectsValue += 10 * priority;

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
        if(move.hitRatio == 0) {
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

        if(move.hitRatio != 0) {
            if(move.hitRatio < 1) {
                values.setAccuracy((int) (move.hitRatio * 100));
            } else {
                values.setAccuracy((int) move.hitRatio);
            }
        }

        values.setSpeedDependantEffectsValue(generateUniqueSpeedValue(move));
        double useRestrictionMultiplier = generateUniqueUseLimitValue(move);
        if(move.pp < 10) {
            useRestrictionMultiplier *= .9;
        }
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
            case MoveIDs.endeavor:
                return 100; //a bit arbitrary
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
            case MoveIDs.gyroBall:
                return 150; //maximum power, reduced by SpeedEffect.
            case MoveIDs.naturalGift:
                return 70; //middle of possible powers (IF they have a berry)
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
                return 2; //doubled if slower (i.e. when speed modifier doesn't apply)
            case MoveIDs.assurance:
                return 1; //needs synergy or double battle to pull off
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && romHandler.generationOfPokemon() == 1) {
                    return 2.5; //Multi-turn incapacitating moves
                }
                return 1;
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
            case MoveIDs.metalBurst:
                return -120; //does nothing if moves first.
            case MoveIDs.copycat:
            case MoveIDs.mimic:
            case MoveIDs.mirrorMove:
            case MoveIDs.sketch:
                return 30; //Allows making choices about the move copied (although I'm not sure the AI *does* that)
            case MoveIDs.destinyBond:
                return 40; //can't avoid the kill
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && generation == 1) {
                    return 100; //essentially a 100% chance to flinch
                }
                return 0;
        }
    }

    private double generateUniqueUseLimitValue(Move move) {
        switch(move.internalId) {
            case MoveIDs.fakeOut:
                return .5; //only first turn
            case MoveIDs.naturalGift:
                return .5; //only once and consumes a potentially-useful resource
            case MoveIDs.thrash:
            case MoveIDs.outrage:
            case MoveIDs.petalDance:
                return .85; //after 2-3 turns (.7), become confused (.5) (multiply inverses together)
            case MoveIDs.bide:
                return .33; //two turns prep
            case MoveIDs.swallow:
            case MoveIDs.spitUp:
                return .5; //need to use Stockpile at least once first
            case MoveIDs.feint:
                return .1; //depends on opponent using protect/detect
            default:
                return 1;
        }
        //TODO: consider if multi-turn moves (rollout, uproar, etc) get a penalty
    }

    private int generateUniqueEffectsValue(Move move) {
        switch (move.internalId) {
            //non-power damaging moves
            case MoveIDs.superFang:
                return 100; //half HP
            case MoveIDs.endeavor:
                return 100; //a bit arbitrary, but "on average" about half HP?
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
                return 40; //seismic toss/night shade peak around l20
            case MoveIDs.dragonRage:
                return 70; //static damage moves will be given synergy multipliers based on level
            case MoveIDs.counter:
            case MoveIDs.mirrorCoat:
                return 80;
            case MoveIDs.metalBurst:
            case MoveIDs.bide:
                return 120;
            //not really sure how to handle the retaliate moves

            //damaging moves' secondary effects
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                return -150; //for causing faint
            case MoveIDs.skullBash:
                if(romHandler.generationOfPokemon() > 1) {
                    return 40; //for raising defence
                } else {
                    return 0;
                }
            case MoveIDs.rage:
                return 30; //attack boost at fairly high reliability
            case MoveIDs.secretPower:
                return 25; //30% to do various things, many (but not all) of them valued at 100.
            case MoveIDs.uTurn:
                return 20; //not innately that good, but it has combo potential
            //these super narrow ones probably should just get 0s
            case MoveIDs.brickBreak:
            case MoveIDs.knockOff:
                return 15; //narrow, but very useful when applicable
            case MoveIDs.skyUppercut:
                return 5; //REALLY narrow
            case MoveIDs.feint:
                return 30; //really good, when it hits
            case MoveIDs.pluck:
            case MoveIDs.bugBite:
                return 15; //narrow but useful
            case MoveIDs.thief:
            case MoveIDs.covet:
                return 5; //deliberately undervaluing this effect since it's annoying for players
            case MoveIDs.pursuit:
                return 10; //good, but very situational
            case MoveIDs.rapidSpin:
                return 10; //doesn't apply to many effects
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
                return 100; //Only works on half the pokemon, but twice as well as paralysis.
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
                return 60;
            case MoveIDs.healBell:
            case MoveIDs.aromatherapy:
            case MoveIDs.safeguard:
            case MoveIDs.refresh:
            case MoveIDs.substitute:
                return 70;

            //damage over time
            case MoveIDs.nightmare:
                return 100; //synergy baked in
            case MoveIDs.curse:
                return 65; //the default value for the stat changes; Ghost types count as synergy.
            //(Although, valuing Curse's effect at 140 (-75 for half health) isn't too far off.)
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
            case MoveIDs.roost:
                return 95; //Removes immunities, so slightly worse than other 50% heal moves.
            case MoveIDs.rest:
            case MoveIDs.swallow:
                return 150; //(up to) full heal
            case MoveIDs.healingWish:
                return 5; //The AI doesn't typically switch pokemon enough to make this work.
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
            case MoveIDs.mindReader:
            case MoveIDs.lockOn:
            case MoveIDs.foresight:
            case MoveIDs.odorSleuth:
            case MoveIDs.miracleEye:
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
            case MoveIDs.acupressure:
                return 80; //+2 any stat. Can hit acc/eva, but no choice, so evens out.


            //protect / damage reduction
            case MoveIDs.protect:
            case MoveIDs.detect:
                return 80;
            case MoveIDs.endure:
                return 50;
            case MoveIDs.reflect:
            case MoveIDs.lightScreen:
                return 70;
            case MoveIDs.mudSport:
            case MoveIDs.waterSport:
                return 30; //synergy goes up if they're weak to that type

            //other effects
            case MoveIDs.whirlwind:
            case MoveIDs.roar:
                if(romHandler.generationOfPokemon() == 1) {
                    return 0;
                } else {
                    return 40;
                }
            case MoveIDs.conversion:
                return 50;
            case MoveIDs.conversion2:
                return 70;
            case MoveIDs.spiderWeb:
            case MoveIDs.meanLook:
            case MoveIDs.block:
                return 40; //trap moves
            case MoveIDs.spite:
                return 50;
            case MoveIDs.grudge:
                return 30; //grudge is just Destiny Bond but bad
            case MoveIDs.rainDance:
            case MoveIDs.sunnyDay:
                return 15; //weather moves do barely anything without synergy
            case MoveIDs.gravity:
                return 15;
            case MoveIDs.batonPass:
                return 20; //like weather, low base value but high synergy potential
            case MoveIDs.destinyBond:
                return 50;
            case MoveIDs.perishSong:
                return 30; //without synergy, just forces a switch
            case MoveIDs.trick:
            case MoveIDs.rolePlay:
            case MoveIDs.recycle:
            case MoveIDs.skillSwap:
            case MoveIDs.camouflage:
                return 5; //these moves are too weird for the AI to use correctly
            case MoveIDs.magicCoat:
            case MoveIDs.snatch:
                return 50; //potent effects, but mostly luck based to see if they work
            // (excepting with very specific synergy)

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

    private int generateUniqueDoubleBattleEffectsValue(Move move) {
        switch(move.internalId) {
            case MoveIDs.assurance:
                //this is also dependent on speed, not sure how to handle that
                //ignoring it for now
                return 50;
            case MoveIDs.followMe:
                return 30; //low value, high synergy. Synergizes with defenses.
            case MoveIDs.helpingHand:
                return 50;
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
                //hits both foes and ally
                return 1.2;



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
            case MoveIDs.expandingForce:
                return 1; //this is a synergy case
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
