package text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UnicodeParserTest {

    @Test
    public void canReadTableResource() {
        // Will fail if the table resource file can't be read.
        // The assertion itself isn't important, but just a way to "touch" the UnicodeParserTest class.
        assertNotEquals(null, UnicodeParser.tb);
    }

}
