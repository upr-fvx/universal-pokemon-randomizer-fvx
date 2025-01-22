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

import java.util.Arrays;
import java.util.List;

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

    private static final List<EvolutionType> USES_LEVEL = Arrays.asList(
            LEVEL, LEVEL_ATTACK_HIGHER, LEVEL_DEFENSE_HIGHER,
            LEVEL_ATK_DEF_SAME, LEVEL_LOW_PV, LEVEL_HIGH_PV,
            LEVEL_CREATE_EXTRA, LEVEL_IS_EXTRA, LEVEL_MALE_ONLY,
            LEVEL_FEMALE_ONLY, LEVEL_WITH_DARK, LEVEL_UPSIDE_DOWN,
            LEVEL_RAIN, LEVEL_DAY, LEVEL_NIGHT, LEVEL_FEMALE_ESPURR,
            LEVEL_GAME, LEVEL_DAY_GAME, LEVEL_NIGHT_GAME,
            LEVEL_SNOWY, LEVEL_DUSK, LEVEL_ULTRA
    );

    private static final List<EvolutionType> USES_ITEM = Arrays.asList(
            STONE, TRADE_ITEM, STONE_MALE_ONLY, STONE_FEMALE_ONLY, LEVEL_ITEM_DAY, LEVEL_ITEM_NIGHT, STONE_ULTRA
    );

    private static final List<EvolutionType> USES_LOCATION = Arrays.asList(
            LEVEL_ELECTRIFIED_AREA, LEVEL_MOSS_ROCK, LEVEL_ICY_ROCK, LEVEL_SNOWY
    );

    public boolean usesLevel() {
        return USES_LEVEL.contains(this);
    }

    public boolean usesItem() {
        return USES_ITEM.contains(this);
    }

    public boolean usesMove() {
        return this == LEVEL_WITH_MOVE;
    }

    public boolean usesSpecies() {
        return this == LEVEL_WITH_OTHER;
    }

    public boolean usesLocation() {
        return USES_LOCATION.contains(this);
    }

    public boolean skipSplitEvo() {
        return (this == LEVEL_HIGH_BEAUTY) || (this == LEVEL_ULTRA) || (this == STONE_ULTRA);
    }
}
