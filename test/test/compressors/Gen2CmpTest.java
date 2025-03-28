package test.compressors;

import com.dabomstew.pkrandom.graphics.images.GBCImage;
import compressors.Gen2Cmp;
import compressors.Gen2Decmp;
import compressors.gen2.Gen2Compressor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static test.compressors.CmpTestConstants.IN_ADRESS;
import static test.compressors.CmpTestConstants.TEST_FILE_NAMES;

public class Gen2CmpTest {

    public static String[] getImageNames() {
        return TEST_FILE_NAMES;
    }

    @ParameterizedTest
    @MethodSource("getImageNames")
    public void testImage(String name) throws IOException {
        System.out.println(name);
        GBCImage bim = new GBCImage.Builder(new File(IN_ADRESS + "/" + name + ".png")).columnMode(true).build();

        List<Gen2Compressor> compressors = Gen2Cmp.COMPRESSORS;

        List<Gen2Compressor> succeeded = new ArrayList<>();
        List<Gen2Compressor> failed = new ArrayList<>();
        List<Gen2Compressor> erred = new ArrayList<>();

        byte[] uncompressed = bim.toBytes();
        byte[] bitFlipped = Gen2Cmp.flipBits(uncompressed);
        for (Gen2Compressor cmp : compressors) {
            try {
                byte[] compressed = cmp.compress(uncompressed, bitFlipped);
                byte[] decompressed = Gen2Decmp.decompress(compressed, 0);
                if (Arrays.equals(uncompressed, decompressed)) {
                    succeeded.add(cmp);
                } else {
                    failed.add(cmp);
                }
            } catch (Exception e) {
                System.out.println(cmp);
                e.printStackTrace();
                erred.add(cmp);
            }
        }

        System.out.println("Succed: " + succeeded);
        System.out.println("Failed: " + failed);
        System.out.println("Errord: " + erred);

        assertTrue(failed.isEmpty());
        assertTrue(erred.isEmpty());
    }



}
