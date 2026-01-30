package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  MoveLearnt.java - represents a move learnt by a Pokemon at a level.   --*/
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

import java.util.Objects;

public class MoveLearnt {

    public int move;
    public int level;

    public String toString() {
        return "move " + move + " at level " + level;
    }

    public MoveLearnt(int move, int level) {
        this.move = move;
        this.level = level;
    }

    public MoveLearnt(MoveLearnt original) {
        this.move = original.move;
        this.level = original.level;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MoveLearnt) {
            MoveLearnt other = (MoveLearnt) o;
            return other.move == move && other.level == level;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(move, level);
    }
}
