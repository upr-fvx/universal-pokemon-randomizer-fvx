package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.SpeciesTypeRandomizer;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpeciesTraitRandomizersTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeTypesCompletelyFollowsCosmeticFormes(String romName) {
        activateRomHandler(romName);

        Settings s = new Settings();
        s.setSpeciesTypesMod(Settings.SpeciesTypesMod.COMPLETELY_RANDOM);

        new SpeciesTypeRandomizer(romHandler, s, RND).randomizeSpeciesTypes();

        checkCosmeticFormesHaveSameTypesAsBaseForme();
    }

    private void checkCosmeticFormesHaveSameTypesAsBaseForme() {
        SpeciesSet all = romHandler.getSpeciesSetInclFormes();
        SpeciesSet formes = all.filter(s -> !s.isBaseForme());

        for(Species forme : formes) {
            if(forme.isActuallyCosmetic()) {
                System.out.println(forme.getFullName() + ": Is cosmetic");
                System.out.println("\t" + forme.getPrimaryType(false) +
                        (forme.hasSecondaryType(false) ? "/" + forme.getSecondaryType(false) : ""));
                Species base = forme.getBaseForme();
                System.out.println("\tBase: " + base.getFullName() + "; " + base.getPrimaryType(false) +
                        (base.hasSecondaryType(false) ? "/" + base.getSecondaryType(false) : ""));
                assertEquals(forme.getPrimaryType(false), base.getPrimaryType(false),
                        "Primary types do not match");
                assertEquals(forme.getSecondaryType(false), base.getSecondaryType(false),
                        "Secondary types do not match");
            } else {
                System.out.println(forme.getFullName() + ": Is not cosmetic");
            }
        }
    }
}
