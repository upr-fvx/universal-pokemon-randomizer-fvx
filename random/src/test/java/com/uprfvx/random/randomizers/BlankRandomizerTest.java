package com.uprfvx.random.randomizers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class BlankRandomizerTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void foobar(String romName) {
        activateRomHandler(romName);
    }

}
