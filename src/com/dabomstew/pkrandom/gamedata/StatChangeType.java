package com.dabomstew.pkrandom.gamedata;

/*----------------------------------------------------------------------------*/
/*--  StatChangeType.java - represents the types of stat buffs that a move  --*/
/*--                      can apply.                                        --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
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

public enum StatChangeType {
    NONE,
    ATTACK,
    DEFENSE,
    SPECIAL_ATTACK,
    SPECIAL_DEFENSE,
    SPEED,
    ACCURACY,
    EVASION,
    ALL,
    SPECIAL,
    ANY; //a special case used internally. Should not be applied to any moves.
    //(Although, actually, an argument could be made for Acupressure...)

    public boolean containedInAll() {
        switch(this) {
            case ATTACK:
            case DEFENSE:
            case SPECIAL_ATTACK:
            case SPECIAL_DEFENSE:
            case SPEED:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the StatChangeType that has (essentially) the same effect if inversely applied on the opposing pokemon.
     * For example, Attack and Defense are opposing because raising a Pokemon's attack is roughly equivalent to
     * lowering its opponent's defense.
     * This is a two-way relationship, so calling opposingStat on the opposing stat should yield the initial stat.
     * @return The opposing stat.
     */
    public StatChangeType opposingStat() {
        switch (this) {
            case ATTACK:
                return DEFENSE;
            case DEFENSE:
                return ATTACK;
            case SPECIAL_ATTACK:
                return SPECIAL_DEFENSE;
            case SPECIAL_DEFENSE:
                return SPECIAL_ATTACK;
            case ACCURACY:
                return EVASION;
            case EVASION:
                return ACCURACY;
            case NONE:
            case ALL:
            case ANY:
            case SPEED:
            case SPECIAL:
                return this;
            default:
                throw new UnsupportedOperationException("No opposing stat recorded for stat " + this);
        }
    }
}