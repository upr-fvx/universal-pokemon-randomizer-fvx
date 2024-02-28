package test.romhandlers;

import com.dabomstew.pkrandom.pokemon.Effectiveness;
import com.dabomstew.pkrandom.pokemon.Type;
import com.dabomstew.pkrandom.pokemon.TypeTable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class RomHandlerTypeTest extends RomHandlerTest {

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTableHasTypes(String romName) {
        loadROM(romName);
        assertFalse(romHandler.getTypeTable().getTypes().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTableHasNonNeutralEffectivenesses(String romName) {
        loadROM(romName);
        assertNotEquals(0, romHandler.getTypeTable().nonNeutralEffectivenessCount());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTableIdenticalToHardCoded(String romName) {
        // This test might seem silly; why have both hard coded TypeTables and read them from the ROM,
        // when we expect them to be identical? We do that because they might *not* be equal in ROM hacks,
        // and eventually ROM hack support is something we want in the randomizer.
        // Thus, this test is JUST for the vanilla games, to check the TypeTable reading works.
        loadROM(romName);
        TypeTable hardCoded = getHardCodedTypeTable(romHandler.generationOfPokemon());
        TypeTable read = romHandler.getTypeTable();
        assertEquals(new HashSet<>(hardCoded.getTypes()), new HashSet<>(read.getTypes()));
        for (Type attacker : read.getTypes()) {
            for (Type defender : read.getTypes()) {
                System.out.println("\n" + attacker + " vs " + defender);
                Effectiveness hardCodedEff = hardCoded.getEffectiveness(attacker, defender);
                Effectiveness readEff = read.getEffectiveness(attacker, defender);
                System.out.println("hardCoded:\t" + hardCodedEff);
                System.out.println("read:\t\t" + readEff);
                assertEquals(hardCodedEff, readEff);
            }
        }
    }

    private TypeTable getHardCodedTypeTable(int generation) {
        if (generation == 1) {
            return TypeTable.getVanillaGen1Table();
        } else if (generation <= 5) {
            return TypeTable.getVanillaGen2To5Table();
        } else {
            return TypeTable.getVanillaGen6PlusTable();
        }
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void typeTableDoesNotChangeWithGetAndSet(String romName) {
        loadROM(romName);
        TypeTable before = new TypeTable(romHandler.getTypeTable());
        romHandler.setTypeTable(before);
        assertEquals(before, romHandler.getTypeTable());
    }

    @ParameterizedTest
    @MethodSource("getRomNames")
    public void reverseTypeEffectivenessWorks(String romName) {
        loadROM(romName);
        TypeTable before = new TypeTable(romHandler.getTypeTable());
        romHandler.reverseTypeEffectiveness(false);
        TypeTable after = romHandler.getTypeTable();

        for (Type attacker : after.getTypes()) {
            for (Type defender : after.getTypes()) {

                System.out.println(attacker + " vs. " + defender);
                Effectiveness beforeEff = before.getEffectiveness(attacker, defender);
                System.out.println("before: " + beforeEff);
                Effectiveness afterEff = after.getEffectiveness(attacker, defender);
                System.out.println("after: " + afterEff);

                if (beforeEff == Effectiveness.HALF || beforeEff == Effectiveness.ZERO) {
                    assertEquals(Effectiveness.DOUBLE, afterEff);
                } else if (beforeEff == Effectiveness.DOUBLE) {
                    assertEquals(Effectiveness.HALF, afterEff);
                }
            }
        }
    }

}
