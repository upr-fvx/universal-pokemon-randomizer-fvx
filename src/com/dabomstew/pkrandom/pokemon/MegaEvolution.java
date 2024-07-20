package com.dabomstew.pkrandom.pokemon;

/*----------------------------------------------------------------------------*/
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

/**
 * Represents a mega evolution.
 */
public class MegaEvolution {
    private final Pokemon from;
    private final Pokemon to;
    private final boolean needsItem;
    private final Item item;

    public MegaEvolution(Pokemon from, Pokemon to, boolean needsItem, Item item) {
        this.from = from;
        this.to = to;
        this.needsItem = needsItem;
        this.item = item;
    }

    public Pokemon getFrom() {
        return from;
    }

    public Pokemon getTo() {
        return to;
    }

    public boolean isNeedsItem() {
        return needsItem;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "MegaEvolution[from=" + from.fullName() + ", to=" + to.fullName() + ", needsItem=" + needsItem
                + (needsItem ? ", item=" + item : "") + "]";
    }
}
