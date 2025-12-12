package test.com.dabomstew.pkromio.gamedata;

import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.gamedata.SpeciesSet;
import com.dabomstew.pkromio.gamedata.Type;
import org.junit.jupiter.api.Test;

import javax.print.attribute.UnmodifiableSetException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SpeciesSetTest {

    private static final double MAX_DEVIATION = 0.1;

    private final Random random = new Random();

    @Test
    public void addingElementMultipleTimesDoesNotAffectGetRandom() {
        Species a = new Species(0);
        a.setName("A");
        Species b = new Species(1);
        b.setName("B");
        SpeciesSet pokes = new SpeciesSet();
        pokes.add(a);
        for (int i = 0; i < 1000; i++) {
            pokes.add(b);
        }

        int[] count = new int[2];
        for (int i = 0; i < 5000; i++) {
            Species pick = pokes.getRandomSpecies(random);
            count[pick.getNumber()]++;
        }

        System.out.println(Arrays.toString(count));
        System.out.println(Math.abs(1 - ((double) count[0] / (double) count[1])));
        assertTrue(Math.abs(1 - ((double) count[0] / (double) count[1])) < MAX_DEVIATION);
    }

    @Test
    public void removingAnElementPreventsItFromGettingChosenByGetRandom() {
        Species a = new Species(0);
        a.setName("A");
        Species b = new Species(1);
        b.setName("B");
        Species c = new Species(2);
        c.setName("C");
        SpeciesSet pokes = new SpeciesSet();
        pokes.add(a);
        pokes.add(b);
        pokes.add(c);
        pokes.remove(b);

        int[] count = new int[3];
        for (int i = 0; i < 5000; i++) {
            Species pick = pokes.getRandomSpecies(random);
            count[pick.getNumber()]++;
        }

        System.out.println(Arrays.toString(count));
        assertEquals(0, count[b.getNumber()]);
    }

    @Test
    public void readdingARemovedElementMakesItChoosableByGetRandom() {
        Species a = new Species(0);
        a.setName("A");
        Species b = new Species(1);
        b.setName("B");
        Species c = new Species(2);
        c.setName("C");
        SpeciesSet pokes = new SpeciesSet();
        pokes.add(a);
        pokes.add(b);
        pokes.add(c);
        pokes.remove(b);
        pokes.add(b);

        int[] count = new int[3];
        for (int i = 0; i < 5000; i++) {
            Species pick = pokes.getRandomSpecies(random);
            count[pick.getNumber()]++;
        }

        System.out.println(Arrays.toString(count));
        assertTrue(count[b.getNumber()] > 0);
    }

    @Test
    public void unmodifiableSetCopiesElementsWhenInitiated() {
        Species a = new Species(0);
        a.setName("A");
        Species b = new Species(1);
        b.setName("B");
        SpeciesSet specs = SpeciesSet.unmodifiable(new HashSet<>(Arrays.asList(a, b)));
        System.out.println(specs);
        assertEquals(specs, new HashSet<>(Arrays.asList(a, b)));
    }

    @Test
    public void unmodifiableSetThrowsWhenAdding() {
        Species a = new Species(0);
        a.setName("A");
        Species b = new Species(1);
        b.setName("B");
        SpeciesSet specs = SpeciesSet.unmodifiable(Collections.singleton(a));
        assertThrows(UnmodifiableSetException.class, () -> {specs.add(b);});
    }

    @Test
    public void unmodifiableSetThrowsWhenRemoving() {
        Species a = new Species(0);
        a.setName("A");
        SpeciesSet specs = SpeciesSet.unmodifiable(Collections.singleton(a));
        assertThrows(UnmodifiableSetException.class, () -> {specs.remove(a);});
    }

    @Test
    public void unmodifiableSetThrowsWhenRemovingThroughIterator() {
        Species a = new Species(0);
        a.setName("A");
        SpeciesSet specs = SpeciesSet.unmodifiable(Collections.singleton(a));
        assertThrows(UnmodifiableSetException.class, () -> {
            Iterator<Species> it = specs.iterator();
            it.next();
            it.remove();
            System.out.println(specs); // in case nothing is thrown, shows whether the element was removed or not
        });
    }

    @Test
    public void unmodifiableSetThrowsWhenClearing() {
        Species a = new Species(0);
        a.setName("A");
        SpeciesSet specs = SpeciesSet.unmodifiable(Collections.singleton(a));
        assertThrows(UnmodifiableSetException.class, specs::clear);
    }

    @Test
    public void sortByTypesWorks() {
        SpeciesSet specs = new SpeciesSet();
        Random random = new Random();
        List<Type> types = Type.getAllTypes(7);
        for(int i = 0; i < 1000; i++){
            Species species = new Species(i);
            species.setName("Random" + i);
            species.setPrimaryType(types.get(random.nextInt(types.size())));
            if(random.nextBoolean()) {
                species.setSecondaryType(types.get(random.nextInt(types.size())));
            }
            specs.add(species);
        }

        Map<Type, SpeciesSet> specsByTypes = specs.sortByType(false);
        for(Type type : types) {
            SpeciesSet speciesOfType = specsByTypes.get(type);
            if(speciesOfType != null) {
                for (Species species : speciesOfType) {
                    assertTrue(species.hasType(type, false));
                }
            }
        }
    }

    @Test
    public void sortByTypesWorksWithChangedTypes() {
        SpeciesSet specs = new SpeciesSet();
        Random random = new Random();
        List<Type> types = Type.getAllTypes(7);
        for(int i = 0; i < 1000; i++){
            Species species = new Species(i);
            species.setName("Random" + i);
            species.setPrimaryType(types.get(random.nextInt(types.size())));
            if(random.nextBoolean()) {
                species.setSecondaryType(types.get(random.nextInt(types.size())));
            } else {
                species.setSecondaryType(null);
            }

            species.setPrimaryType(types.get(random.nextInt(types.size())));
            if(random.nextBoolean()) {
                species.setSecondaryType(types.get(random.nextInt(types.size())));
            } else {
                species.setSecondaryType(null);
            }
            specs.add(species);
        }

        Map<Type, SpeciesSet> specsByTypes = specs.sortByType(false);
        for(Type type : types) {
            SpeciesSet speciesOfType = specsByTypes.get(type);
            for (Species species : speciesOfType) {
                assertTrue(species.hasType(type, false));
            }
        }

        specsByTypes = specs.sortByType(true);
        for(Type type : types) {
            SpeciesSet speciesOfType = specsByTypes.get(type);
            for (Species species : speciesOfType) {
                assertTrue(species.hasType(type, true));
            }
        }
    }
}
