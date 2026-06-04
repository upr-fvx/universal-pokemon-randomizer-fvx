package com.uprfvx.random.customnames;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OldCustomNamesImporterTest {

    private static final String PATH = "src/test/resources/oldcustomnames";

    @Test
    public void readPreRNCNTextFilesWorks() throws IOException {
        CustomNamesSet cns = OldCustomNamesImporter.readPreRNCNTextFiles(
                PATH + "/trainernames.txt",
                PATH + "/trainerclasses.txt",
                PATH + "/nicknames.txt"
        );
        System.out.println(cns);

        assertEquals("Foo", cns.trainerNames().get(0));
        assertEquals("Bar", cns.trainerNames().get(1));
        assertEquals("Baz", cns.trainerNames().get(2));

        assertEquals("Foo&Bar", cns.doublesTrainerNames().get(0));
        assertEquals("Foo & Bar", cns.doublesTrainerNames().get(1));

        assertEquals("Single", cns.trainerClasses().get(0));
        assertEquals("Singless", cns.trainerClasses().get(1));

        assertEquals("Double couple", cns.doublesTrainerClasses().get(0));
        assertEquals("Double and double", cns.doublesTrainerClasses().get(1));
        assertEquals("Double kin", cns.doublesTrainerClasses().get(2));
        assertEquals("Double team", cns.doublesTrainerClasses().get(3));
        assertEquals("Double & double", cns.doublesTrainerClasses().get(4));
        assertEquals("Doubles", cns.doublesTrainerClasses().get(5));

        assertEquals("Foomon", cns.pokemonNicknames().get(0));
        assertEquals("Barmon", cns.pokemonNicknames().get(1));
        assertEquals("Bazmon", cns.pokemonNicknames().get(2));
    }

    @Test
    public void readRNCNFileWorks() throws IOException {
        CustomNamesSet cns = OldCustomNamesImporter.readRNCNFile(
                PATH + "/customnames.rncn"
        );
        System.out.println(cns);

        assertEquals("Foo", cns.trainerNames().get(0));
        assertEquals("Bar", cns.trainerNames().get(1));

        assertEquals("Foos", cns.doublesTrainerNames().get(0));
        assertEquals("Bars", cns.doublesTrainerNames().get(1));

        assertEquals("Fooclass", cns.trainerClasses().get(0));
        assertEquals("Barclass", cns.trainerClasses().get(1));

        assertEquals("Foosclass", cns.doublesTrainerClasses().get(0));
        assertEquals("Barsclass", cns.doublesTrainerClasses().get(1));

        assertEquals("Foomon", cns.pokemonNicknames().get(0));
        assertEquals("Barmon", cns.pokemonNicknames().get(1));
    }

}
