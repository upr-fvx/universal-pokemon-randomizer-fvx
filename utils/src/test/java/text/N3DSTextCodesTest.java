package text;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests for N3DSTextCodes.
 *
 * N3DSTextCodes provides static maps of variable codes for X/Y, ORAS, S/M,
 * and USUM rom types (0-3). USUM (type 3) reuses the S/M map.
 *
 * BEHAVIOR QUIRK: getTextVariableCodes returns an empty map for any romType
 * that is not 0 (XY), 1 (ORAS), 2 (SM), or 3 (USUM). It does NOT throw.
 */
public class N3DSTextCodesTest {

    private static final int Type_XY = 0;
    private static final int Type_ORAS = 1;
    private static final int Type_SM = 2;
    private static final int Type_USUM = 3;

    @Test
    public void getTextVariableCodes_XY_notEmpty() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(Type_XY);
        assertFalse(map.isEmpty(), "XY codes map should not be empty");
    }

    @Test
    public void getTextVariableCodes_ORAS_notEmpty() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(Type_ORAS);
        assertFalse(map.isEmpty(), "ORAS codes map should not be empty");
    }

    @Test
    public void getTextVariableCodes_SM_notEmpty() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(Type_SM);
        assertFalse(map.isEmpty(), "SM codes map should not be empty");
    }

    @Test
    public void getTextVariableCodes_USUM_returnsSameAsSM() {
        // USUM (type 3) should return the SM map
        Map<Integer, String> smMap = N3DSTextCodes.getTextVariableCodes(Type_SM);
        Map<Integer, String> usumMap = N3DSTextCodes.getTextVariableCodes(Type_USUM);
        assertEquals(smMap, usumMap, "USUM map should equal SM map");
    }

    @Test
    public void getTextVariableCodes_unknownType_returnsEmptyMap() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(99);
        assertNotNull(map, "Should return non-null even for unknown type");
        assertTrue(map.isEmpty(), "Should return empty map for unknown romType");
    }

    @Test
    public void getTextVariableCodes_XY_containsTRNAME() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(Type_XY);
        assertTrue(map.containsValue("TRNAME"),
                "XY codes should contain TRNAME variable");
    }

    @Test
    public void getTextVariableCodes_XY_containsCOLOR() {
        Map<Integer, String> map = N3DSTextCodes.getTextVariableCodes(Type_XY);
        assertEquals("COLOR", map.get(0xFF00),
                "Code 0xFF00 should be 'COLOR' in XY");
    }

    @Test
    public void getVariableCode_knownVariable_returnsCode() {
        int code = N3DSTextCodes.getVariableCode("TRNAME", Type_XY);
        assertEquals(0x0100, code, "TRNAME code should be 0x0100 for XY");
    }

    @Test
    public void getVariableCode_unknownVariable_returnsZero() {
        int code = N3DSTextCodes.getVariableCode("NONEXISTENT", Type_XY);
        assertEquals(0, code, "Unknown variable should return 0");
    }

    @Test
    public void getVariableCode_unknownType_returnsZero() {
        int code = N3DSTextCodes.getVariableCode("TRNAME", 99);
        assertEquals(0, code, "Unknown romType should return 0");
    }
}
