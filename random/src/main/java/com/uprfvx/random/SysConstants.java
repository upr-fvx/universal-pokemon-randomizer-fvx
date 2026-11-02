package com.uprfvx.random;

/*----------------------------------------------------------------------------*/
/*--  SysConstants.java - contains constants not related to the             --*/
/*--                      randomization process itself, such as those       --*/
/*--                      relating to file I/O and the updating system.     --*/
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

public class SysConstants {

    public static final String RELEASES_URL = "https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/releases";
    public static final String RELEASES_API_URL = "https://api.github.com/repos/upr-fvx/universal-pokemon-randomizer-fvx/releases/latest";
    public static final String WIKI_URL = "https://upr-fvx.github.io/universal-pokemon-randomizer-fvx/wikipages";
    public static final String WIKI_HOME_URL = WIKI_URL + "/home.html";
    public static final String WIKI_IMPORTANT_INFO_URL = WIKI_URL + "/important_info.html";
    public static final String WIKI_3DS_INFO_URL = WIKI_URL + "/3ds_games.html";
    public static final String ISSUES_URL = "https://github.com/upr-fvx/universal-pokemon-randomizer-fvx/issues";

    public static final String cpgDirectory = "data/custom_player_graphics";

    // Arbitrary numbers to make cheating via editing a preset file slightly harder.
    // They are today's date and Wormadam(my favorite mon)'s Pokédex num repeated.
    // (doesn't matter they are non-random, because the code is open source anyway)
    // -- voliol 2026-10-31
    public static final long RACE_MODE_SEED_ADDEND = 2026103120261031L;
    public static final long RACE_MODE_SEED_LOG_ADDEND = 413413413413413413L;
    public static final String RACE_MODE_IDENTIFIER = "Race mode!";
    public static final String RACE_MODE_LOG_IDENTIFIER = "Race log created.";
    public static final String SEED_IDENTIFIER = "Random seed: ";
}
