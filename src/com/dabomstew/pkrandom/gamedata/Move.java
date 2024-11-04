package com.dabomstew.pkrandom.gamedata;

/*----------------------------------------------------------------------------*/
/*--  Move.java - represents a move usable by Pokemon.                      --*/
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

import com.dabomstew.pkrandom.constants.GlobalConstants;

import java.util.Objects;

public class Move {

    public static class StatChange {
        public StatChangeType type;
        public int stages;
        public double percentChance;

        @Override
        public boolean equals(Object o) {
            if (o instanceof StatChange) {
                StatChange other = (StatChange) o;
                return this.type == other.type && this.stages == other.stages && this.percentChance == other.percentChance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, stages, percentChance);
        }
    }

    public String name;
    public int number;
    public int internalId;
    public int power;
    public int pp;
    public double hitRatio;
    public Type type;
    public MoveCategory category;
    public StatChangeMoveType statChangeMoveType = StatChangeMoveType.NONE_OR_UNKNOWN;
    public StatChange[] statChanges = new StatChange[3];
    public StatusMoveType statusMoveType = StatusMoveType.NONE_OR_UNKNOWN;
    public StatusType statusType = StatusType.NONE;
    public CriticalChance criticalChance = CriticalChance.NORMAL;
    public double statusPercentChance;
    public double flinchPercentChance;
    public int recoilPercent;
    public int absorbPercent;
    public int priority;
    public boolean makesContact;
    public boolean isChargeMove;
    public boolean isRechargeMove;
    public boolean isPunchMove;
    public boolean isSoundMove;
    public boolean isTrapMove; // True for both binding moves (like Wrap) and trapping moves (like Mean Look)
    public int effectIndex;
    public int target;
    public double hitCount = 1; // not saved, only used in randomized move powers.

    private int baseValue = -1; //for moveset randomization

    public Move() {
        // Initialize all statStageChanges to something sensible so that we don't need to have
        // each RomHandler mess with them if they don't need to.
        for (int i = 0; i < this.statChanges.length; i++) {
            this.statChanges[i] = new StatChange();
            this.statChanges[i].type = StatChangeType.NONE;
        }
    }

    public boolean hasSpecificStatChange(StatChangeType type, boolean isPositive) {
        for (StatChange sc: this.statChanges) {
            if (sc.type == type && (isPositive ^ sc.stages < 0)) {
                return true;
            }
            if(sc.type == StatChangeType.ALL && type.containedInAll() && (isPositive ^ sc.stages < 0)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBeneficialStatChange() {
        return (statChangeMoveType == StatChangeMoveType.DAMAGE_TARGET && statChanges[0].stages < 0) ||
                statChangeMoveType == StatChangeMoveType.DAMAGE_USER && statChanges[0].stages > 0;
    }

    public boolean isGoodDamaging(int perfectAccuracy) {
        return (power * hitCount) >= 2 * GlobalConstants.MIN_DAMAGING_MOVE_POWER
                || ((power * hitCount) >= GlobalConstants.MIN_DAMAGING_MOVE_POWER && (hitRatio >= 90 || hitRatio == perfectAccuracy));
    }

    public String toString() {
        return "#" + number + " " + name + " - Power: " + power + ", Base PP: " + pp + ", Type: " + type + ", Hit%: "
                + (hitRatio) + ", Effect: " + effectIndex + ", Priority: " + priority;
    }

    /**
     * Gives a number representing the usefulness of the move in a vacuum.
     * @return The move's base value.
     */
    //Hmm. I kinda feel like this should be in MoveSynergy instead... but.
    //Well, the reason it isn't is so it can be stored after being calculated. Definitely don't want to recalculate it
    //every time.
    public int getBaseValue() {
        if(baseValue == -1) {
            if(category != MoveCategory.STATUS) {
                baseValue = generateDamagingMoveValue();
            } else {
                baseValue = generateStatusMoveValue();
            }
        }
        return baseValue;
    }

    private int generateDamagingMoveValue() {
        double value = power;
        if(power <= 0) {
            value = generateUniqueDamagingMovePower();
        }
        if(hitCount > 1) {
            value *= hitCount;
        }

        value += absorbPercent * power;
        value -= recoilPercent * power;
        value += flinchPercentChance * 100;

        switch (criticalChance) {
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

        if(statusPercentChance != 0) {
            int secondaryEffectValue = 0;
            if(statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
                secondaryEffectValue += generateStatChangeValue();
            }
            if(statusMoveType != StatusMoveType.NONE_OR_UNKNOWN) {
                secondaryEffectValue += generateStatusConditionValue();
            }
            if(secondaryEffectValue == 0) {
                secondaryEffectValue += generateUniqueSecondaryValue();
            }
            value += statusPercentChance * secondaryEffectValue;
        }

        if(hitRatio == 0) {
            value *= 1.5;
        } else {
            value *= hitRatio;
        }

        if(isChargeMove)
            value *= .5;
        if(isRechargeMove)
            value *= .6;

        if(priority > 0) {
            value += 10 + priority;
        } else if(priority < 0) {
            value -= 10 + priority;
        }

        return (int) value;
    }



    private int generateStatusMoveValue() {
        double value = 0;

        if(statChangeMoveType != StatChangeMoveType.NONE_OR_UNKNOWN) {
            value += generateStatChangeValue();
        }
        if(statusMoveType != StatusMoveType.NONE_OR_UNKNOWN) {
            value += generateStatusConditionValue();
        }
        if(value == 0) {
            value += generateUniqueStatusMoveValue();
        }

        if(hitRatio != 0) {
            value *= hitRatio;
        }

        return (int) value;
    }

    private double generateStatChangeValue() {
        double value = 0;

        for(StatChange change : statChanges) {
            if(change == null) {
                continue;
            }
            int changeValue;
            switch(change.type) {
                case ATTACK:
                case DEFENSE:
                case SPECIAL_ATTACK:
                case SPECIAL_DEFENSE:
                    changeValue = 30; //somewhat arbitrary baseline
                    break;
                case SPEED:
                    changeValue = 15; //using a move just to change speed is usually a waste
                    break;
                case SPECIAL: //because this is both attack and defence
                case ACCURACY:
                case EVASION:
                    changeValue = 40;
                    break;
                case ALL:
                    changeValue = 135; //att + def + spatk + spdef + spd
                    break;
                case NONE:
                default:
                    changeValue = 0; //this shouldn't be reached? but whatever
                    break;
            }
            changeValue *= change.stages;

            value += changeValue * change.percentChance;
        }

        switch (statChangeMoveType) {
            case DAMAGE_USER:
            case NO_DAMAGE_ALLY:
            case NO_DAMAGE_USER:
                //no change
                break;
            case DAMAGE_TARGET:
            case NO_DAMAGE_TARGET:
                value *= -1;
            case NO_DAMAGE_ALL:
            case NONE_OR_UNKNOWN:
                //?????
                value = 0;
                break;
        }

        return value;
    }

    private int generateStatusConditionValue() {
        switch(statusType) {
            case POISON:
                return 50;
            case BURN:
                return 60;
            case TOXIC_POISON:
                return 70;
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

    private int generateUniqueSecondaryValue() {
        return 0;
    }

    private double generateUniqueDamagingMovePower() {
        return 0;
    }

    private double generateUniqueStatusMoveValue() {
        return 0;
    }
}
