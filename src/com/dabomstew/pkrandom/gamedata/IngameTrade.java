package com.dabomstew.pkrandom.gamedata;

/*----------------------------------------------------------------------------*/
/*--  IngameTrade.java - stores Pokemon trades with in-game NPCs.           --*/
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
import java.util.Objects;

public class IngameTrade {

    public int id;

    // requestedSpecies can be null, in which case it represents "any Pokémon"
    public Species requestedSpecies, givenSpecies;

    public String nickname, otName;

    public int otId;

    public int[] ivs = new int[0];

    public int item = 0;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IngameTrade) {
            IngameTrade other = (IngameTrade) o;
            return id == other.id && otId == other.otId && item == other.item
                    && Objects.equals(requestedSpecies, other.requestedSpecies) && givenSpecies.equals(other.givenSpecies)
                    && nickname.equals(other.nickname) && Objects.equals(otName, other.otName)
                    && Arrays.equals(ivs, other.ivs);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IngameTrade(id=" + id + ", requested=" + requestedSpecies + ", given=" + givenSpecies +
                ", nickname=" + nickname + ", otName=" + otName + ", otId=" + otId + ", ivs=" + Arrays.toString(ivs) +
                ", item=" + item + ")";
    }
}
