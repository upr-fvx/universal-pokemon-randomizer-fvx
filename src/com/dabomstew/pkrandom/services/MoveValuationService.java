package com.dabomstew.pkrandom.services;

import com.dabomstew.pkrandom.constants.*;
import com.dabomstew.pkrandom.gamedata.*;
import com.dabomstew.pkrandom.romhandlers.RomHandler;

import java.util.*;

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

        private int power;
        private int effectivePower = power;
        private int speedDependentPower = 0;
        private EffectsValue powerScalingEffectsValue;

        private EffectsValue effectsValue;
        private EffectsValue fastEffectsValue;
        private EffectsValue slowEffectsValue;
        private int doubleBattleEffectsValue = 0;
        private double doubleBattleRangeModifier = 1;
        private double accuracy = 1;
        private double useMultiplier = 1; //for charge moves, etc

        //calculated values
        private EffectsValue baseValue;
        private EffectsValue totalResultsValue; //results meaning power and effects combined
        private EffectsValue lostInaccuracyValue;
        private boolean finalized = false;


        public MoveValues(Move move) {
            this.move = move;
            power = move.power;
        }

        private void setEffectivePower(int power) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.effectivePower = power;
        }

        private void setSpeedDependentPower(int power) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            speedDependentPower = power;
        }

        /**
         * Allows to set values for effects that scale with power.
         * The values given should be the value of the effect if power is 100.
         * @param value
         */
        private void setPowerScalingEffectsValue(EffectsValue value) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            powerScalingEffectsValue = value;
        }

        private void setEffectsValue(EffectsValue effectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.effectsValue = effectsValue;
        }

        private void setFastEffectsValue(EffectsValue fastEffectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.fastEffectsValue = fastEffectsValue;
        }

        private void setSlowEffectsValue(EffectsValue slowEffectsValue) {
            if(finalized) {
                throw new IllegalStateException("Attempted to change value of finalized move!");
            }

            this.slowEffectsValue = slowEffectsValue;
        }

        private void setAccuracy(double accuracy) {
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

            int totalPower = effectivePower + speedDependentPower / 2;
            EffectsValue fullValue = new EffectsValue(totalPower, 0);
            fullValue = fullValue.add(powerScalingEffectsValue.multiply((double)totalPower / 100));
            fullValue = fullValue.add(effectsValue);
            fullValue = fullValue.add(fastEffectsValue.divide(2)).add(slowEffectsValue.divide(2));
            fullValue.offensive += doubleBattleEffectsValue / 2;
            fullValue.defensive += doubleBattleEffectsValue / 2;

            totalResultsValue = fullValue;

            EffectsValue perUseValue = totalResultsValue.multiply(accuracy);
            double finalMultiplier = useMultiplier * ((doubleBattleRangeModifier + 1) / 2.0);
            baseValue = perUseValue.multiply(finalMultiplier);

            lostInaccuracyValue = totalResultsValue.subtract(perUseValue).multiply(finalMultiplier);
        }

        public int getBaseValue() {
            if(!finalized) {
                throw new IllegalStateException("Attempted to get value of non-finalized move!");
            }

            return baseValue.total();
        }

        public EffectsValue getBaseValues() {
            if(!finalized) {
                throw new IllegalStateException("Attempted to get value of non-finalized move!");
            }

            return baseValue;
        }

        public int getValueForPokemon(Species species, int ability, List<Move> currentMoves, int heldItem){
            if(!finalized) {
                throw new IllegalStateException("Attempted to get value of non-finalized move!");
            }

            int hp = species.getHp();
            int attack = species.getAttack();
            int defense = species.getDefense();
            int speed = species.getSpeed();
            int spAtk, spDef;
            if(species instanceof Gen1Species) {
                spAtk = species.getSpecial();
                spDef = spAtk;
            } else {
                spAtk = species.getSpatk();
                spDef = species.getSpdef();
            }

            int averageStat = (hp + attack + defense + speed + spAtk + spDef) / 6;
            int offenseValue = (int) ((Math.max(attack, spAtk) + speed / 2) / 1.5);
            int defenseValue = (hp + defense + spDef) / 3;
            double atkSpRatio = species.getAttackSpecialAttackRatio();

            //step 1: speed-dependent power and unique power modifiers
            int effectivePower = move.power;
            effectivePower += MoveSynergy.getSpeedFactoredPower(speedDependentPower, speed, averageStat, currentMoves);

            //step 2: modify power value for atk/spatk ratio & STAB

            //step 3: type valuation of power (covers new weaknesses, resistances, other new types)

            //step 4: speed-dependent value and unique effects modifiers

            //step 5: add in synergy with other moves (to effects and/or power)

            //step 6: modify effects & power values by offense/defense ratio; combine into results

            //step 7: modify results value by accuracy (unless synergied away)

            //is that all? it's all i can think of right now, it might be all the steps.

        }
    }

    private static class EffectsValue {
        public int offensive;
        public int defensive;

        public EffectsValue(int offensive, int defensive) {
            this.offensive = offensive;
            this.defensive = defensive;
        }

        public EffectsValue add(EffectsValue other) {
            return new EffectsValue(offensive + other.offensive, defensive + other.defensive);
        }

        public EffectsValue subtract(EffectsValue other) {
            return new EffectsValue(offensive - other.offensive, defensive - other.defensive);
        }

        public EffectsValue multiply(int factor) {
            return new EffectsValue(offensive * factor, defensive * factor);
        }

        public EffectsValue multiply(double factor) {
            return new EffectsValue((int)(offensive * factor), (int)(defensive * factor));
        }

        public EffectsValue divide(int factor) {
            return new EffectsValue(offensive / factor, defensive / factor);
        }

        public EffectsValue halve() {
            return divide(2);
        }

        public int total() {
            return offensive + defensive;
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
            moveValues.put(move, generateMoveValues(move));
        }

        return moveValues.get(move).getBaseValue();
    }

    private EffectsValue getBaseValues(Move move) {
        if(!moveValues.containsKey(move)) {
            moveValues.put(move, generateMoveValues(move));
        }

        return moveValues.get(move).getBaseValues();
    }

    public int getValueForTrainerPokemon(Move move, TrainerPokemon tp){
        Species species = tp.species;
        int ability;
        switch (tp.abilitySlot) {
            case 1:
                ability = species.getAbility1();
                break;
            case 2:
                ability = species.getAbility2();
                break;
            case 3:
                ability = species.getAbility3();
                break;
            default:
                throw new IllegalArgumentException(tp + " has no valid ability");
        }
        List<Move> moves = new ArrayList<>();
        for(int moveID : tp.moves) {
            if(moveID > 0) {
                moves.add(allMoves.get(moveID));
            }
        }
        return getValueForSpecificPokemon(move, species, ability, moves, tp.heldItem);
    }

    public int getValueForSpecificPokemon(Move move, Species species, int ability, List<Move> currentMoves,
                                          int heldItem){
        return moveValues.get(move).getValueForPokemon(species, ability, currentMoves, heldItem);
    }

    public List<Integer> getValuesForTrainerPokemon(List<Move> moves, TrainerPokemon tp){
        Species species = tp.species;
        int ability;
        switch (tp.abilitySlot) {
            case 1:
                ability = species.getAbility1();
                break;
            case 2:
                ability = species.getAbility2();
                break;
            case 3:
                ability = species.getAbility3();
                break;
            default:
                throw new IllegalArgumentException(tp + " has no valid ability");
        }
        List<Move> currentMoves = new ArrayList<>();
        for(int moveID : tp.moves) {
            if(moveID > 0) {
                currentMoves.add(allMoves.get(moveID));
            }
        }
        return getValuesForSpecificPokemon(moves, species, ability, currentMoves, tp.heldItem);
    }

    public List<Integer> getValuesForSpecificPokemon(List<Move> moves, Species species, int ability,
                                                    List<Move> currentMoves, int heldItem) {

    }

    private MoveValues generateMoveValues(Move move) {
        MoveValues values = new MoveValues(move);

        if(GlobalConstants.uselessMoves.contains(move.internalId)) {
            values.effectsValue = new EffectsValue(0,0);
            values.calculateValues();
            return values;
        }

        double accuracy;
        if(romHandler.getPerfectAccuracy() != 100 && move.hitRatio == romHandler.getPerfectAccuracy()) {
            accuracy = 1.5;
        } else {
            switch (move.internalId) {
                case MoveIDs.swift:
                case MoveIDs.feintAttack:
                case MoveIDs.vitalThrow:
                    //because gen 1 through 3 encoded perfect-accuracy moves differently, and they can't be distinguished
                    //from other moves except by ID.
                    //TODO: standardize the encoding of perfect accuracy, so we can skip this
                    accuracy = 1.5;
                    break;
                default:
                    accuracy = getStandardizedChance(move.hitRatio);
            }
        }

        if(move.category != MoveCategory.STATUS) {
            generatePowerRelevantValues(move, values);
        } else if(accuracy == 1.5) {
            accuracy = 1.2; //perfect accuracy is less valuable for status moves
        }

        values.setAccuracy(accuracy);


        EffectsValue effectsValue = new EffectsValue(0,0);

        if(move.statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            effectsValue = effectsValue.add(generateStatChangeValue(move));
        }

        if(move.statusMoveType != StatusMoveType.NONE_OR_UNKNOWN) {
            effectsValue = effectsValue.add(generateStatusConditionValue(move));
        }

        if(GlobalConstants.semiInvulnerableMoves.contains(move)) {
            effectsValue = effectsValue.add(new EffectsValue(0, 60));
        }

        effectsValue = effectsValue.add(generateUniqueEffectsValue(move));
        values.setEffectsValue(effectsValue);

        values.setFastEffectsValue(generateFastEffectsValue(move));
        values.setSlowEffectsValue(generateSlowEffectsValue(move));

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
        //TODO: convert double battle to EffectsValue
        values.setDoubleBattleRangeModifier(generateDoubleBattleRangeModifier(move));

        values.calculateValues();

        return values;
    }

    private void generatePowerRelevantValues(Move move, MoveValues values) {
        int power = move.power;
        if(power <= 1) {
            power = generateUniqueDamagingMovePower(move);
        }
        if(move.hitCount > 1) {
            power *= move.hitCount;
        } else {
            power *= generateUniquePowerMultiplier(move);
        }
        if(power != move.power) {
            values.setEffectivePower(power);
        }

        values.setSpeedDependentPower(generateSpeedDependentPowerValue(move));

        int critValue = (int)(generateCriticalChanceMultiplier(generation, move.criticalChance) * 100);
        EffectsValue powerValue = new EffectsValue(critValue, move.absorbPercent - move.recoilPercent);
        values.setPowerScalingEffectsValue(powerValue);
    }

    /**
     * Standardizes a chance given to it to be between 0 and 1.
     * @param statedChance The chance initially stated by the move; expected to be on a scale of either 0-1 or 0-100.
     * @return The same chance expressed as between 0-1.
     */
    private double getStandardizedChance(double statedChance) {
        //TODO: standardize all chances in ROMHandlers, then remove this method
        //(Flinch, status condition, stat change, accuracy, (more?) )
        if(statedChance > 1) {
            return statedChance / 100;
        } else {
            return statedChance;
        }
    }

    private EffectsValue generateStatChangeValue(Move move) {
        EffectsValue value = new EffectsValue(0,0);

        for(Move.StatChange change : move.statChanges) {
            if(change == null) {
                continue;
            }
            EffectsValue changeValue;
            StatChangeType type = change.type;
            if(move.statChangeMoveType.affectsFoe()) {
                type = type.opposingStat();
            }
            switch(type) {
                case ATTACK:
                case SPECIAL_ATTACK:
                    changeValue = new EffectsValue(40, 0);
                    //somewhat arbitrary baseline of 40 per stage.
                    //May adjust as more move-value data is obtained.
                    break;
                case DEFENSE:
                case SPECIAL_DEFENSE:
                    changeValue = new EffectsValue(0, 40);
                    break;
                case SPEED:
                    changeValue = new EffectsValue(0, 0);
                    //speed change is a speed-dependent effect, so will be handled by FastEffectValue and SlowEffectValue
                    break;
                case SPECIAL:
                    changeValue = new EffectsValue(40, 40);
                    //doubled due to being both spatk and spdef
                    //(this may not be totally correct?)
                    break;
                case ACCURACY:
                    changeValue = new EffectsValue(60, 0);
                    //somewhat higher since it effects both physical and special, plus some status
                    //This value makes less sense for increasing accuracy or reducing evasion, though...
                    break;
                case EVASION:
                    changeValue = new EffectsValue(0, 60);
                    break;
                case ALL:
                    changeValue = new EffectsValue(80, 80);
                    //add ATK+SPATK+DEF+SPDEF together (speed covered elsewhere)
                    break;
                case NONE:
                default:
                    changeValue = new EffectsValue(0,0); //this shouldn't be reached? but whatever
                    break;
            }
            changeValue = changeValue.multiply(change.stages);

            if(change.percentChance != 0) {
                value = value.add(changeValue.multiply(getStandardizedChance(change.percentChance)));
            } else {
                value = value.add(changeValue);
            }

        }

        if (move.statChangeMoveType.affectsFoe()) {
            value = value.multiply(-1);
        }

        return value;
    }

    private EffectsValue generateStatusConditionValue(Move move) {
        EffectsValue baseValue;
        switch(move.statusType) {
            case POISON:
                baseValue = new EffectsValue(0, 50);
                //DOT is technically an offensive effect, but it synergizes with defensive moves and stats
                //and therefore is better classified as a defensive effect.
            case TOXIC_POISON:
                baseValue = new EffectsValue(0, 70);
            case BURN:
                baseValue = new EffectsValue(0, 80);
                //course, here's where having them as offensive would help clarify things...
            case CONFUSION:
                baseValue = new EffectsValue(0, 90);
                //again, confusion is partially offensive - but it works better with defensive buffs.
                //bonus 10 FastEffectsValue for less time *not* confused
            case PARALYZE:
                baseValue = new EffectsValue(0, 60);
                //not shown: 60/20 SlowEffectsValue for the near-guaranteed speed change
            case SLEEP:
            case FREEZE:
                baseValue = new EffectsValue(0, 100);
                //also get a bonus 10 FastEffectsValue
            case NONE:
            default:
                baseValue = new EffectsValue(0,0);
        }
        //These values are a bit arbitrary and may betray my personal biases.
        //But that applies to all status values... and some of the damage factors as well...
        if(move.statusPercentChance != 0) {
            return baseValue.multiply(getStandardizedChance(move.statusPercentChance));
        } else {
            return baseValue;
        }
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

    private int generateSpeedDependentPowerValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.gyroBall:
                return -149;
            case MoveIDs.electroBall:
                return 110;
            case MoveIDs.payback:
                return -50;
                //Payback cares about *turn order*, while the other two care about *speed*.
                //This does mean that its value should be handled slightly differently, synergy-wise
                //(for example, Trick Room effects it but not the other two.)
                //However, it's close enough that I probably just won't bother.
            default:
                return 0;
        }
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
            case MoveIDs.stompingTantrum:
                return 1.5; //doubles under certain conditions (synergy ho)
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
        double damageIncrease = critMultiplier - 1;

        switch (criticalChance) {
            case NORMAL:
                return 0;
            case NONE:
                return baseCritChance * damageIncrease * -1;
            case INCREASED:
                return critChanceIncrease * damageIncrease;
            case GUARANTEED:
                return damageIncrease; //technically this probably should be times 1 - base chance, but...
                // this *feels* more correct, even if it isn't.
            default:
                //should never occur
                throw new IllegalArgumentException("Unsupported critical chance: " + criticalChance.name());
        }
    }

    private EffectsValue generateFastEffectsValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.copycat:
            case MoveIDs.mimic:
            case MoveIDs.mirrorMove:
            case MoveIDs.sketch:
                return new EffectsValue(15, 15);
                //Allows making choices about the move copied (although I'm not sure the AI *does* that)
                //this makes them better for all purposes
            case MoveIDs.disable:
            case MoveIDs.encore:
                return new EffectsValue(0, 30);
                //similarly, allows choosing what move is disabled/encored
                //this, however, is a purely defensive effect
            case MoveIDs.destinyBond:
                return new EffectsValue(40, 0); //if faster, player can't avoid the kill
            case MoveIDs.meFirst:
                return new EffectsValue(70, 0); //only works on damaging moves, but buffs them.
            case MoveIDs.trickRoom:
            case MoveIDs.speedSwap:
                return new EffectsValue(-60, -20);
                //temporary swap of speed... which is actively bad if fast.
                //(Although, it could provide synergy.)
            case MoveIDs.matBlock:
                return new EffectsValue(0, 60); //no effect unless moves first
            case MoveIDs.slow:
                return new EffectsValue(-30, -10);
            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId) && generation == 1) {
                    return new EffectsValue(0, 100); //essentially a 100% chance to flinch
                }

                //now general stuff
                EffectsValue fastEffectsValue = new EffectsValue(0, 0);

                if(move.flinchPercentChance > 0) {
                    fastEffectsValue = fastEffectsValue.add(new EffectsValue(0,
                            (int) getStandardizedChance(move.flinchPercentChance) * 100));
                }

                int priority = getStandardizedPriority(move);
                if (priority < 0) {
                    fastEffectsValue = fastEffectsValue.add(new EffectsValue(-10 + priority, 0));
                }

                fastEffectsValue = fastEffectsValue.add(generateSpeedChangeValue(move, true));

                if(move.statusType == StatusType.SLEEP || move.statusType == StatusType.FREEZE
                        || move.statusType == StatusType.CONFUSION) {
                    fastEffectsValue = fastEffectsValue.add(
                            new EffectsValue(0, (int)(10 * getStandardizedChance(move.statusPercentChance))));
                    //because this reduces the time gap between it wearing off & reapplying it
                    //(unless you happen to be using the same move anyway)
                }
                return fastEffectsValue;
        }
    }

    private EffectsValue generateSlowEffectsValue(Move move) {
        switch (move.internalId) {
            case MoveIDs.tailwind:
            case MoveIDs.trickRoom:
            case MoveIDs.speedSwap:
                return new EffectsValue(60, 20);
                //tailwind isn't guaranteed to swap like the other two... but nearly so
            case MoveIDs.coreEnforcer:
                return new EffectsValue(20, 70);
                //removes ability if slower
            case MoveIDs.metalBurst:
                return new EffectsValue(120, 0);
            default:

                //now general stuff
                EffectsValue slowEffectsValue = new EffectsValue(0, 0);

                int priority = getStandardizedPriority(move);
                if (priority > 0) {
                    slowEffectsValue = slowEffectsValue.add(new EffectsValue(-10 + priority, 0));
                }

                slowEffectsValue = slowEffectsValue.add(generateSpeedChangeValue(move, false));

                if(move.statusType == StatusType.PARALYZE) {
                    EffectsValue paralysisValue = new EffectsValue(60, 20);
                    paralysisValue = paralysisValue.add(generateFastEffectsValue(move));
                    paralysisValue = paralysisValue.multiply(getStandardizedChance(move.statusPercentChance));
                    slowEffectsValue = slowEffectsValue.add(paralysisValue);
                }
                return slowEffectsValue;
        }
    }

    private int getStandardizedPriority(Move move) {
        int priority = move.priority;
        if(romHandler.generationOfPokemon() == 2) {
            priority--; //All gen 2 moves have one higher priority than typical
            //TODO: have Gen2ROMHandler standardize priority
            //After this is done, this method can be removed.
        }
        return priority;
    }

    private EffectsValue generateSpeedChangeValue(Move move, boolean fast) {
        int speedChange = 0;
        double chance = 1;

        for(Move.StatChange change : move.statChanges) {
            if(change == null) {
                continue;
            }
            if (!(change.type == StatChangeType.SPEED || change.type == StatChangeType.ALL)) {
                continue;
            }

            if(move.statChangeMoveType.affectsSelf()){
                speedChange += change.stages;
            } else if(move.statChangeMoveType.affectsFoe()) {
                speedChange -= change.stages;
            } else {
                //probably unreachable, BUT
                continue;
            }

            chance = getStandardizedChance(change.percentChance);
            //theoretically, there shouldn't be any moves which have multiple separate speed-changing chances...
        }

        if(fast && speedChange < 0) {
            EffectsValue value = new EffectsValue(30, 10).multiply(speedChange);

            //check for self synergy
            value = value.add(generateSlowEffectsValue(move));

            return value.multiply(chance);
        } else if (!fast && speedChange > 0) {
            EffectsValue value = new EffectsValue(30, 10).multiply(speedChange);

            //check for self synergy
            value = value.add(generateFastEffectsValue(move));

            return value.multiply(chance);
        } else {
            return new EffectsValue(0,0);
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
            case MoveIDs.shellTrap:
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
            case MoveIDs.firstImpression:
                return .5; //only first turn
            case MoveIDs.naturalGift:
            case MoveIDs.burnUp:
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
            case MoveIDs.auroraVeil:
                return .75; //only works during hail/snow. (This is assuming the need for a weather move.)

            case MoveIDs.belch:
                return .9; //this is mostly a synergy deal

            default:
                return 1;
        }

    }

    private EffectsValue generateUniqueEffectsValue(Move move) {
        switch (move.internalId) {
            //non-power damaging moves
            case MoveIDs.superFang:
            case MoveIDs.naturesMadness:
                return new EffectsValue(100, 0); //half HP
            case MoveIDs.endeavor:
                return new EffectsValue(50, 50);
                //a bit arbitrary, but "on average" about half HP?
            case MoveIDs.finalGambit:
                return new EffectsValue(200, -200);
                //like endeavor, "on average" full HP, but -200 for causing faint.
            case MoveIDs.guillotine:
            case MoveIDs.hornDrill:
            case MoveIDs.fissure:
            case MoveIDs.sheerCold:
                return new EffectsValue(200,0);
            case MoveIDs.psywave:
                switch (generation) {
                    case 1:
                    case 2:
                        return new EffectsValue(30, 0);
                    default:
                        return new EffectsValue(60, 0);
                }
            case MoveIDs.sonicBoom:
            case MoveIDs.seismicToss:
            case MoveIDs.nightShade:
                return new EffectsValue(60, 0);
                //seismic toss/night shade peak around l20
                //(Well, actually they mostly improve with level, but l20 is around when evolutions start happening,
                //which nerfs them heavily)
            case MoveIDs.dragonRage:
                return new EffectsValue(80, 0);
            case MoveIDs.counter:
            case MoveIDs.mirrorCoat:
            case MoveIDs.bide:
                return new EffectsValue(120, 0);
                //not really sure how to handle the retaliate moves, but they're strong when they work

            //damaging moves' drawbacks
            case MoveIDs.selfDestruct:
            case MoveIDs.explosion:
                return new EffectsValue(0, -200); //for causing faint
            case MoveIDs.mindBlown:
                return new EffectsValue(0, -100); //for half hp

            //TODO: sort these
            case MoveIDs.secretPower:
                return new EffectsValue(0, 25);
                //30% to do various things, many (but not all) of them valued at 100.
            case MoveIDs.skyUppercut:
                return new EffectsValue(5,0); //hits certain semi-invulnerables
            case MoveIDs.pursuit:
                return new EffectsValue(10, 0); //good, but situational
            case MoveIDs.uproar:
                return new EffectsValue(0, 10);

            //disabling moves
            case MoveIDs.disable:
                return new EffectsValue(0, 70);
            case MoveIDs.encore:
                return new EffectsValue(0, 65);
            case MoveIDs.torment:
                return new EffectsValue(0, 60);
            case MoveIDs.taunt:
                return new EffectsValue(0, 70);
            case MoveIDs.imprison:
                return new EffectsValue(0, 10); //near useless without synergy
            case MoveIDs.throatChop:
                return new EffectsValue(0, 10); //small subset of moves
            case MoveIDs.attract:
                return new EffectsValue(0, 200); //stupid powerful, when it works
            case MoveIDs.yawn:
                return new EffectsValue(0, 100); //guaranteed means >100, but delay puts it back.

            //copying moves
            case MoveIDs.mirrorMove:
            case MoveIDs.copycat:
                return new EffectsValue(25, 25);
                //can't know if the effect is offensive or defensive
            case MoveIDs.mimic:
            case MoveIDs.sketch:
                return new EffectsValue(20, 35);
                //these ones are better defensively, because of the extra turn involved
            case MoveIDs.transform:
                return new EffectsValue(0, 80);
                //????

            //other calling moves
            case MoveIDs.sleepTalk:
            case MoveIDs.assist:
                return new EffectsValue(0, 1); //Synergy based on other moves' potency.
            case MoveIDs.metronome:
                Collection<Integer> impossibleMoveIDs;
                if (romHandler.generationOfPokemon() > 7) {
                    impossibleMoveIDs = Collections.emptyList();
                } else {
                    impossibleMoveIDs = Arrays.asList(
                            Gen1Constants.impossibleMetronomeMoves, Gen2Constants.impossibleMetronomeMoves,
                            Gen3Constants.impossibleMetronomeMoves, Gen4Constants.impossibleMetronomeMoves,
                            Gen5Constants.impossibleMetronomeMoves, Gen6Constants.impossibleMetronomeMoves,
                            Gen7Constants.impossibleMetronomeMoves
                            ).get(romHandler.generationOfPokemon());
                }
                int[] possibleMoveIDs = getAllMoves().stream()
                        .filter(Objects::nonNull)
                        .mapToInt(m -> m.number)
                        .filter(id -> !impossibleMoveIDs.contains(id))
                        .toArray();
                return averageMoveValue(possibleMoveIDs);
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
                        return new EffectsValue(0,0); //unsupported generation?
                }

            //status prevention/healing
            case MoveIDs.mist:
                return new EffectsValue(0, 50);
            case MoveIDs.haze:
            case MoveIDs.clearSmog:
                return new EffectsValue(0, 60);
            case MoveIDs.healBell:
            case MoveIDs.aromatherapy:
            case MoveIDs.safeguard:
            case MoveIDs.refresh:
            case MoveIDs.substitute:
                return new EffectsValue(0, 70);
            case MoveIDs.mistyTerrain:
                return new EffectsValue(0, 65);
                //also protects foes... although, that doesn't usually matter without anti-synergy
            case MoveIDs.psychoShift:
                return new EffectsValue(0, 90);
                //significantly better than just a status heal, but still needs a status effect to do anything
            case MoveIDs.sparklingAria:
                return new EffectsValue(0, -5);
                //heals damaged pokemon of burns?? it doesn't usually matter but why would you want that?

            //damage over time
            case MoveIDs.nightmare:
                return new EffectsValue(0, 100); //synergy baked into usage
            case MoveIDs.curse:
                return new EffectsValue(0, 50);
                //150 for massive dot - 100 for half health
            case MoveIDs.spikes:
            case MoveIDs.stealthRock:
                return new EffectsValue(40, 0);
                //...Not sure if it's correct to call this offensive but it feels right.
            case MoveIDs.toxicSpikes:
                return new EffectsValue(0, 70);
            case MoveIDs.sandstorm:
            case MoveIDs.hail:
                return new EffectsValue(0, 20);
                //weather moves have extreme synergy but low inherent value

            //HP recovery
            case MoveIDs.leechSeed:
                return new EffectsValue(0, 60);
            case MoveIDs.ingrain:
                return new EffectsValue(0, 40); //too little healing to be much good
            case MoveIDs.aquaRing:
                return new EffectsValue(0, 50); //same, but without the drawback
            case MoveIDs.morningSun:
            case MoveIDs.moonlight:
            case MoveIDs.synthesis:
                if(romHandler.generationOfPokemon() == 2) {
                    return new EffectsValue(0, 50); //if not join the 50% heal moves below
                }
            case MoveIDs.recover:
            case MoveIDs.softBoiled:
            case MoveIDs.milkDrink:
            case MoveIDs.wish: //the delay could make it worse or better, so it'll stay at 100.
            case MoveIDs.slackOff:
            case MoveIDs.healOrder:
            case MoveIDs.shoreUp:
            case MoveIDs.floralHealing:
                return new EffectsValue(0, 100);
            case MoveIDs.roost:
                return new EffectsValue(0, 95);
                //Removes immunities, so slightly worse than other 50% heal moves.
            case MoveIDs.rest:
            case MoveIDs.swallow:
                return new EffectsValue(0, 200);
                //(up to) full heal
                //(Rest also causes sleep, but removes other non-volatiles, I feel that cancels out reasonably)
            case MoveIDs.healingWish:
            case MoveIDs.lunarDance:
                return new EffectsValue(0, 5);
                //The AI doesn't typically switch pokemon enough to make this work.
                //Synergy for those with switch moves, though.
                // (But it has to be switch moves on the rest of the party... hmm.)
            case MoveIDs.painSplit:
                return new EffectsValue(35, 35);
                //...if used correctly, which im not confident the AI can do.
            case MoveIDs.purify:
                return new EffectsValue(0, 50);
                //half heal, but removes opponent's status. Also, requires status.

            //stat change+
            case MoveIDs.bellyDrum:
                return new EffectsValue(240, -100);
                //atk (40) * 6 stages; half health
                //I don't know that I value it that highly, but I'm not the kind of player that likes such a move
            case MoveIDs.memento:
                return new EffectsValue(160, -200);
                //160 for reducing 4 stat stages, -200 for causing faint.
                //(Kinda not sure that's right, though...)
            case MoveIDs.focusEnergy:
                return new EffectsValue(50, 0); //sort of a stat change
            case MoveIDs.laserFocus:
                return new EffectsValue(40, 0); //guaranteed but only once
            case MoveIDs.luckyChant:
                return new EffectsValue(0, 50); //inverse of focus energy
            case MoveIDs.mindReader:
            case MoveIDs.lockOn:
            case MoveIDs.foresight:
            case MoveIDs.odorSleuth:
            case MoveIDs.miracleEye:
            case MoveIDs.telekinesis:
            case MoveIDs.gravity:
                return new EffectsValue(50, 0);
                //without synergy (including <100% accurate moves as synergy), both groups have the same use case
            case MoveIDs.stockpile:
                if(generation == 3) {
                    return new EffectsValue(0, 1); //does nothing on its own
                } else {
                    return new EffectsValue(0, 80); //raises def + spdef
                }
            case MoveIDs.psychUp:
                return new EffectsValue(20, 40); //weird stat change
            case MoveIDs.swagger:
                return new EffectsValue(0, 80); //the two effects have synergy with each other,
                //so boosting the opponent's Attack should not count as a negative.
            case MoveIDs.acupressure:
                return new EffectsValue(20, 60);
                //+2 any stat. Can hit acc/eva, but no choice, so evens out.
                //Actually more likely to hit an offensive stat (since speed counts as offensive),
                //but needing to use it multiple times makes it more effective for defenders.
            case MoveIDs.powerTrick:
            case MoveIDs.wonderRoom:
                return new EffectsValue(15, 15); //tricky, doubt the AI knows how to use effectively
            case MoveIDs.powerSplit:
            case MoveIDs.guardSplit:
                return new EffectsValue(0, 50);
            case MoveIDs.powerSwap:
            case MoveIDs.guardSwap:
                return new EffectsValue(5, 15); //without synergy, is likely to do nothing
            case MoveIDs.heartSwap:
                return new EffectsValue(10, 30); //somewhat more likely to have effect
            case MoveIDs.skullBash:
                if(romHandler.generationOfPokemon() > 1) {
                    return new EffectsValue(0, 40); //for raising defence
                } else {
                    return new EffectsValue(0, 0);
                }
            case MoveIDs.rage:
                if(generation == 2) {
                    return new EffectsValue(0, 0); //in gen 2, is a power multiplier instead.
                } else {
                    return new EffectsValue(32, 0); //attack boost (40) contingent on opponent damaging (*.8)
                }
            case MoveIDs.defog:
                if (generation <= 5) {
                    return new EffectsValue(70, 0);
                    //lowers evasion + essentially rapid spin
                    //has some negative synergies
                } else {
                    return new EffectsValue(70, 10); //now also clears entry hazards from user's side
                }
            case MoveIDs.shellSmash:
                return new EffectsValue(240, -80);
                //gain 6 stages, lose 2, all of them 40-value stats
            case MoveIDs.rototiller:
                return new EffectsValue(80, 0); //1 stage in 2 stats... when it works (synergy)
            case MoveIDs.flowerShield:
                return new EffectsValue(0, 35);
                //1 stage, and it might also give it to opponents. (synergy makes better).
            case MoveIDs.stickyWeb:
                return new EffectsValue(60, 20);
                //1 stage on each new pokemon; 2 is a reasonable estimate. May do synergy: front of party.
            case MoveIDs.fellStinger:
                if(generation == 6) {
                    return new EffectsValue(50, 0);
                    //two stages, but only if you can pull off the kill
                } else {
                    return new EffectsValue(90, 0);
                    //now three stages (and easier to do)
                }
            case MoveIDs.topsyTurvy:
                return new EffectsValue(20, 40);
                //if opponent has any stat buffs, very solid. also potential synergy
            case MoveIDs.diamondStorm:
                if(generation == 6) {
                    return new EffectsValue(0, 30);
                    //only 50% chance, but there was always some premium for the first
                } else {
                    return new EffectsValue(0, 50); //now two stages
                }
            case MoveIDs.venomDrench:
                return new EffectsValue(0, 120);
                //three stats by 1; requires synergy
            case MoveIDs.strengthSap:
                return new EffectsValue(0, 140);
                //heals... probably around half of HP?? also one stat stage
            case MoveIDs.spectralThief:
                return new EffectsValue(50, 50);
                //steal any positive stat changes! if they exist
                //more possible offensive stats is balanced by more likely for opponent to have stats if defensive
            case MoveIDs.slow:
                return new EffectsValue(40, 40);
                //(aka, Curse when used by a non-Ghost type)


            //protect / damage reduction
            case MoveIDs.protect:
            case MoveIDs.detect:
                return new EffectsValue(0, 80);
            case MoveIDs.craftyShield:
                return new EffectsValue(0, 30); //only works on status moves
            case MoveIDs.kingsShield:
                if(generation <= 7) {
                    return new EffectsValue(0, 130);
                    //blocks damaging moves PLUS reduces attack if contacted PLUS form change
                    //although, maybe the form change should be a synergy thing? hmm.
                } else {
                    return new EffectsValue(0, 110); //attack reduction nerfed. still good.
                }
            case MoveIDs.spikyShield:
                return new EffectsValue(0, 95);
                //blocks all move plus damages if contacted
            case MoveIDs.banefulBunker:
                return new EffectsValue(0, 115); //blocks all moves, poison if contacted
            case MoveIDs.wideGuard:
            case MoveIDs.quickGuard:
                return new EffectsValue(0, 20); //only work on small subset of moves
            case MoveIDs.endure:
                return new EffectsValue(0, 50); //only saves last HP
            case MoveIDs.reflect:
            case MoveIDs.lightScreen:
                return new EffectsValue(0, 70);
            case MoveIDs.auroraVeil:
                return new EffectsValue(0, 140); //both physical AND special halved!
            case MoveIDs.mudSport:
            case MoveIDs.waterSport:
                return new EffectsValue(0, 30); //synergy goes up if they're weak to that type

            //item shenanigans
            //most of these are deliberately undervalued due to being annoying to players
            //(as they can result in permanent item loss AFAIK)
            //...text from some NPCs indicates that thievery is NOT permanent so maybe will not deliberately undervalue?
            //...starting in gen 5. Okay.
            case MoveIDs.thief:
            case MoveIDs.covet:
                if(generation <= 5) {
                    return new EffectsValue(0, 5);
                    //undervalue to avoid player annoyance
                } else {
                    return new EffectsValue(0, 20);
                    //still not that great TBH. depends on the item though.
                }
            case MoveIDs.trick:
            case MoveIDs.switcheroo:
                return new EffectsValue(0, 5);
                //AI doesn't know how to use these moves anyway
                //...but, after gen 5, will probably do some synergy about it
            case MoveIDs.recycle:
                return new EffectsValue(0, 5);
                //not an annoying one, but limited use
            case MoveIDs.fling:
                return new EffectsValue(1, 0);
                //power entirely depends on held item
            case MoveIDs.embargo:
                return new EffectsValue(60, 20);
                //hard to value, but pretty solid
            case MoveIDs.magicRoom:
                return new EffectsValue(20, 20); //doesn't prevent active items, so less useful
            case MoveIDs.pluck:
            case MoveIDs.bugBite:
                return new EffectsValue(10, 10);
                //narrow but useful
                //(Not undervalued because berries are consumable anyway)
            case MoveIDs.incinerate:
                return new EffectsValue(10, 0);
                //only destroys it, rather than stealing, but still a bit useful
            case MoveIDs.bestow:
                return new EffectsValue(0, 5); //without synergy, is actively bad
            case MoveIDs.knockOff:
                return new EffectsValue(20, 20); //not undervalued because only temporarily removes

            //switch effects
            case MoveIDs.whirlwind:
            case MoveIDs.roar:
            case MoveIDs.circleThrow:
            case MoveIDs.dragonTail:
                //switch opponent
                if(generation == 1) {
                    return new EffectsValue(0, 0); //useless in this gen
                } else {
                    return new EffectsValue(20, 30);
                    //bit higher for defense because of "tank up, then remove enemy's tanking" strategy
                }
            case MoveIDs.uTurn:
            case MoveIDs.voltSwitch:
            case MoveIDs.partingShot:
                //switch user
                //not that *inherently* useful (since switching is just an option, even if the AI doesn't often use it),
                //but get around trap moves & can be synergized
                return new EffectsValue(30, 0);
                //defender is more likely to not want to switch
            case MoveIDs.batonPass:
                return new EffectsValue(0, 30);
                //for "tank up, then give stats to ally" strat
            case MoveIDs.teleport:
                if(generation < 8) {
                    return new EffectsValue(0,0);
                } else {
                    return new EffectsValue(0, 30);
                    //lowered priority means the user tanks a hit, then switches out
                }

            //type changes
            case MoveIDs.conversion:
                if(generation == 1) {
                    new EffectsValue(0, 70);
                    //matching opponent's type means likely resistance
                } else {
                    new EffectsValue(50, 0);
                    //matching move's type means STAB
                }
            case MoveIDs.conversion2:
                return new EffectsValue(0, 90);
                //guaranteed resistance
            case MoveIDs.soak:
            case MoveIDs.trickOrTreat:
            case MoveIDs.forestsCurse:
                return new EffectsValue(0, 50);
                //much offensive synergy, but without synergy it mostly just turns off STAB
            case MoveIDs.reflectType:
                return new EffectsValue(0, 70);
                //usually gives resistance

            //ability changes
            case MoveIDs.gastroAcid:
                return new EffectsValue(20, 70);
                //depends on foe, but usually pretty good
            case MoveIDs.worrySeed:
                return new EffectsValue(22, 70);
                //also prevents Rest
                //(Has anti-synergy with inflicting sleep, obviously)
            case MoveIDs.simpleBeam:
                return new EffectsValue(15, 65);
                //could end up increasing opponent's stats
                //(Has synergy with stat reductions)
            case MoveIDs.entrainment:
                return new EffectsValue(20, 60);
                //depends largely on the user's ability,
                //but still removes the original ability
            case MoveIDs.sunsteelStrike:
            case MoveIDs.moongeistBeam:
            case MoveIDs.photonGeyser:
                return new EffectsValue(20, 0);
                //ignores abilities. not sure how many that matters for.

            //work despite protection
            case MoveIDs.feint:
            case MoveIDs.shadowForce:
            case MoveIDs.phantomForce:
            case MoveIDs.hyperspaceHole:
            case MoveIDs.hyperspaceFury:
                return new EffectsValue(10, 0);
                //works through protect/detect/etc
            //not putting the substitute-piercing moves because a: that's very specific and b: there's so many
            case MoveIDs.thousandArrows:
                return new EffectsValue(30, 0);
                //works against ungrounded, + grounds them
            case MoveIDs.darkestLariat:
                return new EffectsValue(50, 0);
                //ignores defence and evasion buffs
            case MoveIDs.brickBreak:
            case MoveIDs.psychicFangs:
                return new EffectsValue(15, 0);
                //break screens; narrow, but pretty good when it comes up

            //other effects
            case MoveIDs.spiderWeb:
            case MoveIDs.meanLook:
            case MoveIDs.block:
            case MoveIDs.thousandWaves:
            case MoveIDs.spiritShackle:
            case MoveIDs.anchorShot:
                return new EffectsValue(0, 40); //trap moves
            case MoveIDs.fairyLock:
                return new EffectsValue(0, 15); //trap, but only for one turn.
            case MoveIDs.spite:
                return new EffectsValue(0, 50);
            case MoveIDs.grudge:
                return new EffectsValue(0, 30);
                //grudge is just Destiny Bond but bad
            case MoveIDs.rainDance:
            case MoveIDs.sunnyDay:
                return new EffectsValue(0, 15);
                //weather moves do barely anything without synergy
                //(they only nerf certain types of moves)
            case MoveIDs.grassyTerrain:
            case MoveIDs.electricTerrain:
            case MoveIDs.psychicTerrain:
                return new EffectsValue(0, 15); //same for terrains
            case MoveIDs.destinyBond:
                return new EffectsValue(50, 0);
                //weird to call it offensive, but defenses nerf it
            case MoveIDs.perishSong:
                return new EffectsValue(0, 30);
                //without synergy, just forces a switch
            case MoveIDs.rolePlay:
            case MoveIDs.skillSwap:
            case MoveIDs.camouflage:
                return new EffectsValue(0, 5); //these moves are too weird for the AI to use correctly
            case MoveIDs.magicCoat:
            case MoveIDs.snatch:
                return new EffectsValue(0, 30);
                //potent effects, but mostly luck based to see if they work
                //(excepting with very specific synergy)
            case MoveIDs.healBlock:
                return new EffectsValue(40, 0); //potent, but situational
            case MoveIDs.magnetRise:
                return new EffectsValue(0, 40);
                //become immune to a category of moves
                //(Very common synergy for types)
            case MoveIDs.smackDown:
                return new EffectsValue(20, 0);
                //remove opponent's immunity to move category
                //(synergy dependent)
            case MoveIDs.rapidSpin:
                return new EffectsValue(0, 10);
                //doesn't apply to many effects
            case MoveIDs.foulPlay:
                return new EffectsValue(0, 5);
                //not inherently valuable, but good if the user has low attack
            case MoveIDs.autotomize:
                return new EffectsValue(0, 5);
                //I think there's slightly more punish-heavy moves than punish-light moves,
                //but not many of each.
            case MoveIDs.ionDeluge:
            case MoveIDs.plasmaFists:
                return new EffectsValue(0, 20);
                //might make a resist? should only apply if that resist exists
            case MoveIDs.electrify:
                return new EffectsValue(0, 40);
                //at least doesn't need it to be Normal type. still kinda bad.
            case MoveIDs.powder:
                return new EffectsValue(0, 30); //protect plus damage! when it works
            case MoveIDs.falseSwipe:
            case MoveIDs.holdBack:
                return new EffectsValue(-5, 0);
                //actively bad in a trainer battle
            case MoveIDs.beakBlast:
                return new EffectsValue(0, 30);
                //burns on contact


            default:
                if(GlobalConstants.bindingMoves.contains(move.internalId)) {
                    if(romHandler.generationOfPokemon() == 1) {
                        return new EffectsValue(0,0); //covered elsewhere
                    } else {
                        return new EffectsValue(5, 25);
                        //trap temporarily, plus a tiny bit of damage
                    }
                }
                if(GlobalConstants.semiInvulnerableMoves.contains(move.internalId)) {
                    return new EffectsValue(0, 80);
                    //about as good as a protect move
                }
                return new EffectsValue(0, 0);
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
            case MoveIDs.spotlight:
                return 35; //slightly better since it's targeted. But only slightly.
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
            case MoveIDs.pollenPuff:
                return 100; //half health healing
            case MoveIDs.waterPledge:
            case MoveIDs.firePledge:
            case MoveIDs.grassPledge:
                return 0; //synergy only
            case MoveIDs.fusionFlare:
            case MoveIDs.fusionBolt:
                return 0; //synergy only and also hard to use even then
            case MoveIDs.purify:
                return 60; //much better when used to cure an ally
            case MoveIDs.instruct:
                return 80; //???? idk what this is for tbh
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

            case MoveIDs.courtChange:
            case MoveIDs.electricTerrain:
            case MoveIDs.fairyLock:
            case MoveIDs.flowerShield:
            case MoveIDs.grassyTerrain:
            case MoveIDs.gravity:
            case MoveIDs.hail:
            case MoveIDs.haze:
            case MoveIDs.ionDeluge:
            case MoveIDs.magicRoom:
            case MoveIDs.mistyTerrain:
            case MoveIDs.mudSport:
            case MoveIDs.perishSong:
            case MoveIDs.psychicTerrain:
            case MoveIDs.rainDance:
            case MoveIDs.rototiller:
            case MoveIDs.sandstorm:
            case MoveIDs.sunnyDay:
            case MoveIDs.teatime:
            case MoveIDs.trickRoom:
            case MoveIDs.waterSport:
            case MoveIDs.wonderRoom:
                return 2;
                //effects all pokemon - whether this is desirable for allies is dealt with via synergy

            case MoveIDs.aromatherapy:
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

            case MoveIDs.auroraVeil:
                return 1.3; //nerfed when spread out

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

    private EffectsValue averageMoveValue(int... moveIDs) {
        Move[] moves = new Move[moveIDs.length];
        for(int i = 0; i < moveIDs.length; i++) {
            moves[i] = allMoves.get(moveIDs[i]);
        }
        return averageMoveValue(moves);
    }

    private EffectsValue averageMoveValue(Move... moves) {
        int count = 0;
        EffectsValue totalValue = new EffectsValue(0,0);
        for(Move move : moves) {
            count++;
            totalValue = totalValue.add(getBaseValues(move));
        }
        return totalValue.divide(count);
    }
}
