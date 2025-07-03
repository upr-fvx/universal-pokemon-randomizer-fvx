package test.compressors;

import com.dabomstew.pkromio.graphics.images.GBCImage;
import compressors.Gen1Cmp;
import compressors.Gen1Decmp;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test.compressors.CmpTestConstants.*;

public class Gen1CmpTest {

    public static String[] getImageNames() {
        return TEST_FILE_NAMES;
    }

    @ParameterizedTest
    @MethodSource("getImageNames")
    public void testImage(String name) {
        System.out.println(name);
        GBCImage bim = null;
        try {
            bim = new GBCImage.Builder(new File(IN_ADDRESS + "/" + name + ".png")).columnMode(true).build();
        } catch (IOException ignored) {
        }

        writeBitplaneImages(bim, name);

        int[][] succeeded = new int[3][2];
        int[][] failed = new int[3][2];
        int[][] erred = new int[3][2];

        for (int mode = 0; mode <= 2; mode++) {
            for (int order = 0; order <= 1; order++) {
                try {

                    Gen1Cmp compressor = new Gen1Cmp(bim);
                    byte[] compressed = compressor.compressUsingModeAndOrder(mode, order == 1);

                    byte[] rom = Arrays.copyOf(compressed, 0x100000);
                    Gen1Decmp sprite = new Gen1Decmp(rom, 0);
                    sprite.decompress();
                    GBCImage bim2 = new GBCImage.Builder(sprite.getWidth() / 8, sprite.getHeight() / 8,
                            GBCImage.DEFAULT_PALETTE, sprite.getData()).columnMode(true).build();
                    try {
                        ImageIO.write(bim2, "png", new File(OUT_ADDRESS + "/" + name + "_m" + mode + "o" + order + ".png"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (bim.equals(bim2)) {
                        succeeded[mode][order] = 1;
                    } else {
                        failed[mode][order] = 1;
                    }

                } catch (Exception e) {
                    erred[mode][order] = 1;
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Succed: " + Arrays.deepToString(succeeded));
        System.out.println("Failed: " + Arrays.deepToString(failed));
        System.out.println("Errord: " + Arrays.deepToString(erred));
        if (!Arrays.deepEquals(new int[3][2], erred)) {
            throw new RuntimeException("Erred");
        }
        assertTrue(Arrays.deepEquals(new int[][]{{1, 1}, {1, 1}, {1, 1}}, succeeded));
    }

    private static void writeBitplaneImages(BufferedImage bim, String name) {
        GBCImage image = new GBCImage.Builder(bim).build();
        try {
            ImageIO.write(image.getBitplane1Image(), "png", new File(OUT_ADDRESS + "/" + name + "_bp1.png"));
            ImageIO.write(image.getBitplane2Image(), "png", new File(OUT_ADDRESS + "/" + name + "_bp2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @MethodSource("getImageNames")
    public void getCompressedLengthWorks(String name) {
        byte[] rom = new byte[1000];
        int offset = 23;
        GBCImage image = null;
        System.out.println(IN_ADDRESS + "/" + name + ".png");
        try {
            image = new GBCImage.Builder(ImageIO.read(new File(IN_ADDRESS + "/" + name + ".png")))
                    .columnMode(true).build();
        } catch (IOException ignored) {
        }
        byte[] compressed = Gen1Cmp.compress(image);
        System.arraycopy(compressed, 0, rom, offset, compressed.length);
        Gen1Decmp decmp = new Gen1Decmp(rom, offset);
        decmp.decompress();
        assertEquals(compressed.length, decmp.getCompressedLength());
    }

}
