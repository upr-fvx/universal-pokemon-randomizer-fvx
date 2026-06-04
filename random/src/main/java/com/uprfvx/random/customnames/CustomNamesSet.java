package com.uprfvx.random.customnames;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A record for all lists of custom names; for trainers, trainer classes, and (gift/trade) Pokémon nicknames.
 * @param trainerNames
 * @param trainerClasses
 * @param doublesTrainerNames
 * @param doublesTrainerClasses
 * @param pokemonNicknames
 */
public record CustomNamesSet(List<String> trainerNames, List<String> trainerClasses, List<String> doublesTrainerNames,
                             List<String> doublesTrainerClasses, List<String> pokemonNicknames) {

    // TODO: standardize CustomNamesSet to work like the /data resources

    public static final String FOLDER_PATH = "data/trainer_and_mon_names";
    private static final String TRAINER_NAMES_PATH = FOLDER_PATH + "/TrainerNames.txt";
    private static final String TRAINER_CLASSES_PATH = FOLDER_PATH + "/TrainerClasses.txt";
    private static final String DOUBLES_TRAINER_NAMES_PATH = FOLDER_PATH + "/DoublesTrainerNames.txt";
    private static final String DOUBLES_TRAINER_CLASSES_PATH = FOLDER_PATH + "/DoublesTrainerClasses.txt";
    private static final String POKEMON_NICKNAMES_PATH = FOLDER_PATH + "/PokemonNicknames.txt";

    public static CustomNamesSet readNamesFromFile() throws IOException {
        // TODO: do some input checking, don't just read lines
        List<String> trainerNames = Files.readAllLines(Path.of(TRAINER_NAMES_PATH), StandardCharsets.UTF_8);
        List<String> trainerClasses = Files.readAllLines(Path.of(TRAINER_CLASSES_PATH), StandardCharsets.UTF_8);
        List<String> doublesTrainerNames = Files.readAllLines(Path.of(DOUBLES_TRAINER_NAMES_PATH), StandardCharsets.UTF_8);
        List<String> doublesTrainerClasses = Files.readAllLines(Path.of(DOUBLES_TRAINER_CLASSES_PATH), StandardCharsets.UTF_8);
        List<String> pokemonNicknames = Files.readAllLines(Path.of(POKEMON_NICKNAMES_PATH), StandardCharsets.UTF_8);
        return new CustomNamesSet(trainerNames, trainerClasses,
                doublesTrainerNames, doublesTrainerClasses,
                pokemonNicknames);
    }

    public static void writeNamesToFile(CustomNamesSet customNamesSet) throws IOException {
        Files.write(Path.of(TRAINER_NAMES_PATH), customNamesSet.trainerNames, StandardCharsets.UTF_8);
        Files.write(Path.of(TRAINER_CLASSES_PATH), customNamesSet.trainerClasses, StandardCharsets.UTF_8);
        Files.write(Path.of(DOUBLES_TRAINER_NAMES_PATH), customNamesSet.doublesTrainerNames, StandardCharsets.UTF_8);
        Files.write(Path.of(DOUBLES_TRAINER_CLASSES_PATH), customNamesSet.doublesTrainerClasses, StandardCharsets.UTF_8);
        Files.write(Path.of(POKEMON_NICKNAMES_PATH), customNamesSet.pokemonNicknames, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> trainerNames() {
        return Collections.unmodifiableList(trainerNames);
    }

    @Override
    public List<String> trainerClasses() {
        return Collections.unmodifiableList(trainerClasses);
    }

    @Override
    public List<String> doublesTrainerNames() {
        return Collections.unmodifiableList(doublesTrainerNames);
    }

    @Override
    public List<String> doublesTrainerClasses() {
        return Collections.unmodifiableList(doublesTrainerClasses);
    }

    @Override
    public List<String> pokemonNicknames() {
        return Collections.unmodifiableList(pokemonNicknames);
    }

}
