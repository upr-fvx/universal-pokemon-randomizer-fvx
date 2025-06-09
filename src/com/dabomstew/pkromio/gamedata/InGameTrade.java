package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
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

/**
 * Represents a Pokemon trade with an in-game NPC.
 */
public class InGameTrade {

    // requestedSpecies can be null, in which case it represents "any Pokémon"
    private Species requestedSpecies;
    private Species givenSpecies;

    private String nickname;
    private String otName;

    private int otId;

    private int[] ivs = new int[0];

    private Item heldItem;

    public Species getRequestedSpecies() {
        return requestedSpecies;
    }

    public void setRequestedSpecies(Species requestedSpecies) {
        this.requestedSpecies = requestedSpecies;
    }

    public Species getGivenSpecies() {
        return givenSpecies;
    }

    public void setGivenSpecies(Species givenSpecies) {
        this.givenSpecies = givenSpecies;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getOtName() {
        return otName;
    }

    public void setOtName(String otName) {
        this.otName = otName;
    }

    public int getOtId() {
        return otId;
    }

    public void setOtId(int otId) {
        this.otId = otId;
    }

    public int[] getIVs() {
        return ivs;
    }

    public void setIVs(int[] ivs) {
        this.ivs = ivs;
    }

    public Item getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(Item heldItem) {
        this.heldItem = heldItem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestedSpecies, givenSpecies, otId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InGameTrade) {
            InGameTrade other = (InGameTrade) o;
            return otId == other.otId
                    && Objects.equals(requestedSpecies, other.requestedSpecies) && givenSpecies.equals(other.givenSpecies)
                    && nickname.equals(other.nickname) && Objects.equals(otName, other.otName)
                    && Arrays.equals(ivs, other.ivs) && Objects.equals(heldItem, other.heldItem);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IngameTrade(requested=" + requestedSpecies + ", given=" + givenSpecies +
                ", nickname=" + nickname + ", otName=" + otName + ", otId=" + otId + ", ivs=" + Arrays.toString(ivs) +
                ", heldItem=" + heldItem + ")";
    }
}
