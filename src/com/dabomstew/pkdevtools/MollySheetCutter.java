package com.dabomstew.pkdevtools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * A developer tool for cutting up Gen 2 sprite sheets by MollyChan on Spriter's Resource,
 * into separate images that can be used as Custom Player Graphics.
 * <br><br>
 * How to use:
 * <ul><li>Put the sprite sheet in the root folder and name it "in.png".</li>
 * <li>If the sprite sheet only contains a single character, set {@link #MONOSECTION} to true.<br>
 * Otherwise, set MONOSECTION to false, and use {@link #NAMES} to set suffixes to differentiate the output images
 * of each of the characters.</li></ul>
 */
public class MollySheetCutter {

    // --- Options ---
    private static final boolean MONOSECTION = false;
    private static final String[] NAMES = new String[]{"r", "o", "p"};
    // ---------------

    private static final int EXTRA_MARGIN = 4;
    private static final int SECTION_WIDTH = 112;

    private static final int FRONT_DIM = 56;
    private static final int BACK_DIM = 48;
    private static final int OV_DIM = 16;
    private static final int FISH_WIDTH = 16;
    private static final int FISH_HEIGHT = 8;

    private static final int CYAN = 0xFF00FFFF;
    private static final int WHITE = 0xFFFFFFFF;

    final boolean monoSection;
    final String[] names;

    public static void main(String[] args) throws IOException {
        new MollySheetCutter(MONOSECTION, NAMES).cut();
    }

    private MollySheetCutter(boolean monoSection, String[] names) {
        this.monoSection = monoSection;
        this.names = names;
    }

    private void cut() throws IOException {
        BufferedImage in = ImageIO.read(new File("in.png"));
        in = in.getSubimage(EXTRA_MARGIN, 0, in.getWidth() - EXTRA_MARGIN, in.getHeight());

        int i = 1;
        while (in.getWidth() >= SECTION_WIDTH) {
            System.out.println("Section " + i);
            BufferedImage front = in.getSubimage(5, 3, FRONT_DIM, FRONT_DIM);
            ImageIO.write(front, "png", fromName("front", i));

            BufferedImage back = in.getSubimage(64, 11, BACK_DIM, BACK_DIM);
            ImageIO.write(back, "png", fromName("back", i));

            BufferedImage walk = new BufferedImage(OV_DIM, OV_DIM * 6, 1);
            Graphics2D walkG = walk.createGraphics();
            walkG.drawImage(in.getSubimage(8, 62, OV_DIM, OV_DIM), 0, 0, null);
            walkG.drawImage(in.getSubimage(25, 62, OV_DIM, OV_DIM), 0, OV_DIM, null);
            walkG.drawImage(in.getSubimage(42, 62, OV_DIM, OV_DIM), 0, OV_DIM * 2, null);
            walkG.drawImage(in.getSubimage(8, 79, OV_DIM, OV_DIM), 0, OV_DIM * 3, null);
            walkG.drawImage(in.getSubimage(25, 79, OV_DIM, OV_DIM), 0, OV_DIM * 4, null);
            walkG.drawImage(in.getSubimage(42, 79, OV_DIM, OV_DIM), 0, OV_DIM * 5, null);
            replaceCyanPixelsWithWhite(walk);
            ImageIO.write(walk, "png", fromName("walk", i));

            BufferedImage bike = new BufferedImage(OV_DIM, OV_DIM * 6, 1);
            Graphics2D bikeG = bike.createGraphics();
            bikeG.drawImage(in.getSubimage(59, 62, OV_DIM, OV_DIM), 0, 0, null);
            bikeG.drawImage(in.getSubimage(76, 62, OV_DIM, OV_DIM), 0, OV_DIM, null);
            bikeG.drawImage(in.getSubimage(93, 62, OV_DIM, OV_DIM), 0, OV_DIM * 2, null);
            bikeG.drawImage(in.getSubimage(59, 79, OV_DIM, OV_DIM), 0, OV_DIM * 3, null);
            bikeG.drawImage(in.getSubimage(76, 79, OV_DIM, OV_DIM), 0, OV_DIM * 4, null);
            bikeG.drawImage(in.getSubimage(93, 79, OV_DIM, OV_DIM), 0, OV_DIM * 5, null);
            replaceCyanPixelsWithWhite(bike);
            ImageIO.write(bike, "png", fromName("bike", i));

            BufferedImage fish = new BufferedImage(FISH_WIDTH, FISH_HEIGHT * 3, 1);
            Graphics2D fishG = fish.createGraphics();
            fishG.drawImage(in.getSubimage(8, 104, FISH_WIDTH, FISH_HEIGHT), 0, 0, null);
            fishG.drawImage(in.getSubimage(25, 112, FISH_WIDTH, FISH_HEIGHT), 0, FISH_HEIGHT, null);
            fishG.drawImage(in.getSubimage(50, 112, FISH_WIDTH, FISH_HEIGHT), 0, FISH_HEIGHT * 2, null);
            replaceCyanPixelsWithWhite(fish);
            ImageIO.write(fish, "png", fromName("fish", i));

            in = in.getSubimage(SECTION_WIDTH, 0, in.getWidth() - SECTION_WIDTH, in.getHeight());
            i++;
        }

        System.out.println("Done!");
    }

    private File fromName(String name, int i) {
        File f = new File(name + (monoSection ? "" : "_" + names[i - 1]) + ".png");
        System.out.println(f);
        return f;
    }

    private void replaceCyanPixelsWithWhite(BufferedImage bim) {
        for (int x = 0; x < bim.getWidth(); x++) {
            for (int y = 0; y < bim.getHeight(); y++) {
                if (bim.getRGB(x, y) == CYAN) {
                    bim.setRGB(x, y, WHITE);
                }
            }
        }
    }

}
