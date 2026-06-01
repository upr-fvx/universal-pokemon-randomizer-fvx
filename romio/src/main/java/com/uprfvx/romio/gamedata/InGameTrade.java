package com.uprfvx.romio.gamedata;

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

    private Species requestedSpecies;
    private final SpeciesHolder givenSpeciesHolder;

    private String nickname;
    private String otName;

    private int otId;

    private int[] ivs = new int[0];

    private Item heldItem;

    /**
     * Creates an InGameTrade.
     * @param requestedSpecies The {@link Species} which is requested by the trader.
     *                         It must be either a base forme, or null (which represents "any Pokémon").
     *                         Note that the null behavior might not be supported by all RomHandlers.
     * @param givenSpecies The {@link Species} which is received through the trade.
     *                     It must be a non-null base forme.
     * @throws IllegalArgumentException if requestedSpecies is not a base forme.
     * @throws IllegalArgumentException if givenSpecies is not a base forme.
     * @throws NullPointerException if givenSpecies is null.
     */
    public InGameTrade(Species requestedSpecies, Species givenSpecies) {
        setRequestedSpecies(requestedSpecies);
        this.givenSpeciesHolder = new SpeciesHolder(givenSpecies);
    }

    public Species getRequestedSpecies() {
        return requestedSpecies;
    }

    /**
     * Sets the {@link Species} which is requested by the trader.
     * It must be either a base forme, or null (which represents "any Pokémon").
     * Note that the null behavior might not be supported by all RomHandlers.
     * @throws IllegalArgumentException if requestedSpecies is an alt forme.
     */
    public void setRequestedSpecies(Species requestedSpecies) {
        if (requestedSpecies != null && !requestedSpecies.isBaseForme()) {
            throw new IllegalArgumentException("requestedSpecies (" + requestedSpecies.getNumberAndFullName()
                    + ") is not a base forme.");
        }
        this.requestedSpecies = requestedSpecies;
    }

    public SpeciesHolder getGivenSpeciesHolder() {
        return givenSpeciesHolder;
    }

    /**
     * Short for {@link #getGivenSpeciesHolder()}.{@link SpeciesHolder#getSpecies()}
     */
    public Species getGivenSpecies() {
        return getGivenSpeciesHolder().getSpecies();
    }

    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname of the Pokémon received through trade.
     * @throws NullPointerException if nickname is null.
     */
    public void setNickname(String nickname) {
        if (nickname == null) {
            throw new NullPointerException();
        }
        this.nickname = nickname;
    }

    public String getOtName() {
        return otName;
    }

    /**
     * Sets the name of the Original Trainer of the Pokémon received through trade.
     * @throws NullPointerException if otName is null.
     */
    public void setOtName(String otName) {
        if (otName == null) {
            throw new NullPointerException();
        }
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
        return Objects.hash(requestedSpecies, getGivenSpecies(), otId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InGameTrade other) {
            return otId == other.otId
                    && Objects.equals(requestedSpecies, other.requestedSpecies)
                    && givenSpeciesHolder.equals(other.givenSpeciesHolder)
                    && nickname.equals(other.nickname) && Objects.equals(otName, other.otName)
                    && Arrays.equals(ivs, other.ivs) && Objects.equals(heldItem, other.heldItem);
        }
        return false;
    }

    @Override
    public String toString() {
        return "IngameTrade(requested=" + requestedSpecies + ", given=" + getGivenSpecies() +
                ", nickname=" + nickname + ", otName=" + otName + ", otId=" + otId + ", ivs=" + Arrays.toString(ivs) +
                ", heldItem=" + heldItem + ")";
    }
}
