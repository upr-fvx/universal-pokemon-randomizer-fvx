package com.uprfvx.romio.romhandlers;

import com.uprfvx.romio.MiscTweak;
import com.uprfvx.romio.gamedata.BreedingInfo;
import com.uprfvx.romio.gamedata.EggGroup;
import com.uprfvx.romio.gamedata.Species;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Gen3FastEggHatchingTweakTest {

    @Test
    public void fastEggHatchingSkipsSpeciesWithoutBreedingInfo() throws Exception {
        Gen3RomHandler romHandler = new Gen3RomHandler();
        Species withoutBreedingInfo = species(2);
        Species withBreedingInfo = species(3);
        withBreedingInfo.setBreedingInfo(new BreedingInfo(EggGroup.MONSTER, EggGroup.DRAGON, 20));
        setField(romHandler, "speciesList", Arrays.asList(null, withoutBreedingInfo, withBreedingInfo));

        romHandler.applyMiscTweak(MiscTweak.FAST_EGG_HATCHING);

        assertNull(withoutBreedingInfo.getBreedingInfo());
        assertEquals(0, withBreedingInfo.getBreedingInfo().getEggCycles());
    }

    private static Species species(int number) {
        Species species = new Species(number);
        species.setName("Species" + number);
        return species;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
