package test.randomizers;

import com.dabomstew.pkrandom.settings.SettingsManager;
import com.dabomstew.pkrandom.gamedata.Species;
import com.dabomstew.pkrandom.gamedata.SpeciesSet;
import com.dabomstew.pkrandom.randomizers.SpeciesTypeRandomizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class SpeciesTraitRandomizersTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomizeTypesCompletelyFollowsCosmeticFormes(String romName) {
        activateRomHandler(romName);

        SettingsManager s = new SettingsManager();
        s.setSpeciesTypesMod(SettingsManager.SpeciesTypesMod.COMPLETELY_RANDOM);

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
