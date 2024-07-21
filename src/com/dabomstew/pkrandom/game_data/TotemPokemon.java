package com.dabomstew.pkrandom.game_data;

/*----------------------------------------------------------------------------*/
/*--  TotemPokemon.java - represents a Totem Pokemon encounter              --*/
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

import java.util.Map;
import java.util.TreeMap;

public class TotemPokemon extends StaticEncounter {
    private Aura aura;
    private Map<Integer,StaticEncounter> allies = new TreeMap<>();

    private boolean unused = false;

    public TotemPokemon() {}

    public TotemPokemon(Species pkmn) {
        this.setPkmn(pkmn);
    }

    public Aura getAura() {
        return aura;
    }

    public void setAura(Aura aura) {
        this.aura = aura;
    }

    public Map<Integer, StaticEncounter> getAllies() {
        return allies;
    }

    public void setAllies(Map<Integer, StaticEncounter> allies) {
        this.allies = allies;
    }

    public boolean isUnused() {
        return unused;
    }

    public void setUnused(boolean unused) {
        this.unused = unused;
    }

    @Override
    public String toString() {
        // The %s will be formatted to include the held item.
        String ret = getPkmn().fullName() + "@%s Lv" + getLevel() + "\n    Aura: " + aura.toString() + "\n";
        int i = 1;
        for (StaticEncounter ally: allies.values()) {
            ret = ret.concat("    Ally " + i + ": " + ally.toString() + "\n");
            i++;
        }
        return ret.concat("\n");
    }
}