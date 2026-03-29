package com.uprfvx.random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test/class exists only to give the rom module a test that doesn't depend on loading ROMs.
 * Without it, the "test" task panics.
 */
public class DummyTest {

    @Test
    public void dummyTest() {
        assertTrue(true);
    }
}
