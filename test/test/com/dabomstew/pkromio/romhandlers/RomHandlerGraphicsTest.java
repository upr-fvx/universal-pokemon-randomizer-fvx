package test.com.dabomstew.pkromio.romhandlers;

import com.dabomstew.pkromio.constants.Gen3Constants;
import com.dabomstew.pkromio.gamedata.PlayerCharacterType;
import com.dabomstew.pkromio.graphics.packs.*;
import com.dabomstew.pkromio.romhandlers.Gen3RomHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class RomHandlerGraphicsTest extends RomHandlerTest {

    private static final String TEST_CPG_PATH = "test/resources/players";

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void dumpAllPokemonImages(String romName) {
        loadROM(romName);
        romHandler.dumpAllPokemonImages();
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void canSetCustomPlayerGraphicsWithoutThrowing(String romName) {
        loadROM(romName);
        assumeTrue(romHandler.hasCustomPlayerGraphicsSupport());
        GraphicsPack cpg = getCustomPlayerGraphics();
        romHandler.setCustomPlayerGraphics(cpg, PlayerCharacterType.PC1);
        if (romHandler.hasMultiplePlayerCharacters()) {
            romHandler.setCustomPlayerGraphics(cpg, PlayerCharacterType.PC2);
        }
        assertTrue(true);
    }

    private static final List<GraphicsPackEntry> cpgEntries = initCPGEntries();

    private static List<GraphicsPackEntry> initCPGEntries() {
        try {
            return GraphicsPackEntry.readAllFromFolder(TEST_CPG_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GraphicsPack getCustomPlayerGraphics() {
        switch (romHandler.generationOfPokemon()) {
            case 1:
                return new Gen1PlayerCharacterGraphics(cpgEntries.get(0));
            case 2:
                return new Gen2PlayerCharacterGraphics(cpgEntries.get(1));
            case 3:
                Gen3RomHandler gen3RomHandler = (Gen3RomHandler) romHandler;
                return gen3RomHandler.getRomEntry().getRomType() == Gen3Constants.RomType_FRLG ?
                        new FRLGPlayerCharacterGraphics(cpgEntries.get(3)) :
                        new RSEPlayerCharacterGraphics(cpgEntries.get(2));
            default:
                return null;
        }
    }
}
