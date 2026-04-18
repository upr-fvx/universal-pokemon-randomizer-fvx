package com.uprfvx.random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {

    @Test
    public void latestVersion_HasNewID() {
        for (Version v : Version.ALL_VERSIONS) {
            if (v == Version.LATEST) continue;
            assertNotEquals(Version.LATEST.id, v.id);
        }
    }

    @Test
    public void latestVersion_IsInAllVersionsList() {
        assertTrue(Version.ALL_VERSIONS.contains(Version.LATEST));
    }
}
