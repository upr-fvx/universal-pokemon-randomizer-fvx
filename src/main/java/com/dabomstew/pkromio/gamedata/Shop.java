package com.dabomstew.pkromio.gamedata;

/*----------------------------------------------------------------------------*/
/*--  Shop.java - represents a shop with a list of purchasable items.       --*/
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Shop {
    private List<Item> items;
    private String name;
    private boolean isMainGame;
    private boolean isSpecialShop; // the shops that have been randomizable up until now

    public Shop() {
        this.isMainGame = false;
    }

    public Shop(Shop otherShop) {
        this.items = new ArrayList<>(otherShop.items);
        this.name = otherShop.name;
        this.isMainGame = otherShop.isMainGame;
        this.isSpecialShop = otherShop.isSpecialShop;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMainGame() {
        return isMainGame;
    }

    public void setMainGame(boolean mainGame) {
        isMainGame = mainGame;
    }

    public void setSpecialShop(boolean specialShop) {
        isSpecialShop = specialShop;
    }

    public boolean isSpecialShop() {
        return isSpecialShop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, name, isMainGame);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Shop) {
            Shop other = (Shop) o;
            return Objects.equals(items, other.items) && Objects.equals(name, other.name) && isMainGame == other.isMainGame;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Shop [name=" + name + ", isMainGame=" + isMainGame + ", isSpecialShop=" + isSpecialShop +", items=" + items + "]";
    }
}
