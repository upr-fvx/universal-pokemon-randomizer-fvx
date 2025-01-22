package com.dabomstew.pkrandom.gamedata;

/*----------------------------------------------------------------------------*/
/*--  EvolutionType.java - describes what process is necessary for an       --*/
/*--                       evolution to occur                               --*/
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

public enum EvolutionType {

    // Gen 1+
    LEVEL, STONE, TRADE,
    // Gen 2+
    TRADE_ITEM,
    HAPPINESS,
    HAPPINESS_DAY, HAPPINESS_NIGHT,
    LEVEL_ATTACK_HIGHER, LEVEL_DEFENSE_HIGHER, LEVEL_ATK_DEF_SAME, // used by Tyrogue, should be used together
    // Gen 3+
    LEVEL_LOW_PV, LEVEL_HIGH_PV, // used by Wurmple, should be used together
    LEVEL_CREATE_EXTRA, LEVEL_IS_EXTRA, // used by Nincada, should be used together
    LEVEL_HIGH_BEAUTY, // used by Feebas
    // Gen 4+
    STONE_MALE_ONLY, STONE_FEMALE_ONLY,
    LEVEL_ITEM_DAY, LEVEL_ITEM_NIGHT,
    LEVEL_WITH_MOVE,
    LEVEL_WITH_OTHER, // used by Mantyke
    LEVEL_MALE_ONLY, LEVEL_FEMALE_ONLY,
    LEVEL_ELECTRIFIED_AREA, LEVEL_MOSS_ROCK, LEVEL_ICY_ROCK,
    // Gen 5+
    TRADE_SPECIAL, // used by Karrablast and Shelmet. The mon to trade with is likely hard-coded. // TODO: confirm
    // Gen 6+
    FAIRY_AFFECTION, // used by Eevee -> Sylveon
    LEVEL_WITH_DARK, // used by Pancham
    LEVEL_UPSIDE_DOWN, // used by Inkay
    LEVEL_RAIN, // used by Sliggoo
    LEVEL_DAY, LEVEL_NIGHT,
    LEVEL_FEMALE_ESPURR, // used by Meowstic. Separation from LEVEL_FEMALE likely has to do with this implying a forme.
    // Gen 7+
    LEVEL_GAME, // used by Cosmoem. Unclear how it works
    // used by Rockruff-base. Same as LEVEL_DAY/NIGHT except it also prevents evolution in the wrong game... somehow.
    LEVEL_DAY_GAME, LEVEL_NIGHT_GAME,
    LEVEL_SNOWY,
    LEVEL_DUSK, // used by Rockruff-OwnTempo
    LEVEL_ULTRA, // used by Cubone -> Marowak-K; in Ultra Space.
    STONE_ULTRA, // used by Pikachu -> Raichu-K, and Exeggute -> Exeggutor-K; in Ultra Space.
    // Other
    NONE;

    public boolean usesLevel() {
        return (this == LEVEL) || (this == LEVEL_ATTACK_HIGHER) || (this == LEVEL_DEFENSE_HIGHER)
                || (this == LEVEL_ATK_DEF_SAME) || (this == LEVEL_LOW_PV) || (this == LEVEL_HIGH_PV)
                || (this == LEVEL_CREATE_EXTRA) || (this == LEVEL_IS_EXTRA) || (this == LEVEL_MALE_ONLY)
                || (this == LEVEL_FEMALE_ONLY) || (this == LEVEL_WITH_DARK)|| (this == LEVEL_UPSIDE_DOWN)
                || (this == LEVEL_RAIN) || (this == LEVEL_DAY)|| (this == LEVEL_NIGHT)|| (this == LEVEL_FEMALE_ESPURR)
                || (this == LEVEL_GAME) || (this == LEVEL_DAY_GAME) || (this == LEVEL_NIGHT_GAME)
                || (this == LEVEL_SNOWY) || (this == LEVEL_DUSK) || (this == LEVEL_ULTRA);
    }

    public boolean usesItem() {
        return false; // TODO;
    }

    public boolean usesMove() {
        return false; // TODO;
    }

    public boolean usesSpecies() {
        return this == LEVEL_WITH_OTHER;
    }

    public boolean usesLocation() {
        return false; // TODO;
    }

    public boolean skipSplitEvo() {
        return (this == LEVEL_HIGH_BEAUTY) || (this == LEVEL_ULTRA) || (this == STONE_ULTRA);
    }
}
