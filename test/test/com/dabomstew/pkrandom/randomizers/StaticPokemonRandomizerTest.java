package test.com.dabomstew.pkrandom.randomizers;

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.StaticPokemonRandomizer;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.StaticEncounter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class StaticPokemonRandomizerTest extends RandomizerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomMatchingSwapsLegsWithLegsOnly(String romName) {
        activateRomHandler(romName);
        List<StaticEncounter> before = deepCopy(romHandler.getStaticPokemon());

        Settings s = new Settings();
        s.setStaticPokemonMod(Settings.StaticPokemonMod.RANDOM_MATCHING);
        new StaticPokemonRandomizer(romHandler, s, RND).randomizeStaticPokemon();

        List<StaticEncounter> after = romHandler.getStaticPokemon();
        if (before.size() != after.size()) {
            throw new RuntimeException("static pokemon list mismatch");
        }
        for (int i = 0; i < before.size(); i++) {
            Species befPk = before.get(i).getSpecies();
            Species aftPk = after.get(i).getSpecies();
            System.out.println("bef=" + befPk.getFullName() + (befPk.isLegendary() ? " (legendary)" : "") +
                    ", aft=" + aftPk.getFullName() + (aftPk.isLegendary() ? " (legendary)" : ""));
            assertEquals(befPk.isLegendary(), aftPk.isLegendary());
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomMatchingSwapsUBsWithUBsOnly(String romName) {
        assumeTrue(getGenerationNumberOf(romName) == 7);

        activateRomHandler(romName);
        List<StaticEncounter> before = deepCopy(romHandler.getStaticPokemon());

        Settings s = new Settings();
        s.setStaticPokemonMod(Settings.StaticPokemonMod.RANDOM_MATCHING);
        new StaticPokemonRandomizer(romHandler, s, RND).randomizeStaticPokemon();

        List<StaticEncounter> after = romHandler.getStaticPokemon();
        if (before.size() != after.size()) {
            throw new RuntimeException("static pokemon list mismatch");
        }
        for (int i = 0; i < before.size(); i++) {
            Species befPk = before.get(i).getSpecies();
            Species aftPk = after.get(i).getSpecies();
            System.out.println("bef=" + befPk.getFullName() + (isUltraBeast(befPk) ? " (ultra beast)" : "") +
                    ", aft=" + aftPk.getFullName() + (isUltraBeast(aftPk) ? " (ultra beast)" : ""));
            assertEquals(isUltraBeast(befPk), isUltraBeast(aftPk));
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void randomSwapMegaEvolvablesWorks(String romName) {
        activateRomHandler(romName);
        List<StaticEncounter> before = deepCopy(romHandler.getStaticPokemon());

        Settings s = new Settings();
        s.setStaticPokemonMod(Settings.StaticPokemonMod.COMPLETELY_RANDOM);
        s.setSwapStaticMegaEvos(true);
        new StaticPokemonRandomizer(romHandler, s, RND).randomizeStaticPokemon();

        List<StaticEncounter> after = romHandler.getStaticPokemon();
        if (before.size() != after.size()) {
            throw new RuntimeException("static pokemon list mismatch");
        }
        for (int i = 0; i < before.size(); i++) {
            StaticEncounter bef = before.get(i);
            StaticEncounter aft = after.get(i);
            System.out.println("bef=" + bef.getSpecies().getFullName()
                    + (bef.getHeldItem() == null ? "" : "w. " + bef.getHeldItem().getName())
                    + (bef.canMegaEvolve() ? " (can mega evolve)" : "") +
                    ", aft=" + aft.getSpecies().getFullName()
                    + (aft.getHeldItem() == null ? "" : "w. " + aft.getHeldItem().getName())
                    + (aft.canMegaEvolve() ? " (can mega evolve)" : ""));
            assertEquals(bef.canMegaEvolve(), aft.canMegaEvolve());
        }
    }

    private boolean isUltraBeast(Species pk) {
        return romHandler.getRestrictedSpeciesService().getUltrabeasts(false).contains(pk);
    }

    private List<StaticEncounter> deepCopy(List<StaticEncounter> original) {
        List<StaticEncounter> copy = new ArrayList<>(original.size());
        for (StaticEncounter se : original) {
            copy.add(new StaticEncounter(se));
        }
        return copy;
    }

}
