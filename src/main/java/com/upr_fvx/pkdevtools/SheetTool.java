package com.upr_fvx.pkdevtools;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * This is a developer tool for swiftly transferring sprites from one sheet format to another.
 * <br><br>
 * The Randomizer can read Custom Player Graphics from sprite sheets. On sites such as
 * Spriters' Resource, people share sprites they've made, organized in sheets. However,
 * the Randomizer can only read sheets formatted in specific ways (see the CPG wikipage),
 * and most sheets shared online won't share that format. Thus, there is a transfer
 * process, which this tool aims to reduce the tedium of.
 */
public class SheetTool {

    private static final boolean DEBUG_VISUALIZE_CLICKS = false;

    private static class ImageLabel extends JLabel {

        public BufferedImage image;
        private final ImageIcon icon;

        private ImageLabel(BufferedImage image) {
            this.icon = new ImageIcon();
            setIcon(icon);
            setImage(image);
            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.TOP);
        }

        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            if (image == null) {
                image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, 0x00000000); // transparent
            }
            this.image = image;
            update();
        }

        public void update() {
            icon.setImage(image);
            revalidate();
            repaint();
        }

        public void addClickListener(Consumer<MouseEvent> consumer) {
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (DEBUG_VISUALIZE_CLICKS) {
                        System.out.println(e.getX() + ", " + e.getY());
                        image.setRGB(e.getX(), e.getY(), 0xFFFF0000);
                        image.setRGB(e.getX() - 1, e.getY(), 0xFFFF0000);
                        image.setRGB(e.getX() + 1, e.getY(), 0xFFFF0000);
                        image.setRGB(e.getX(), e.getY() - 1, 0xFFFF0000);
                        image.setRGB(e.getX(), e.getY() + 1, 0xFFFF0000);
                        update();
                    }
                    consumer.accept(e);
                }
                @Override
                public void mousePressed(MouseEvent e) {
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                }
                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        }
    }

    private class SheetImageLabel extends ImageLabel {

        public class Frame {
            int x, y, w, h;
            private Frame(int x, int y, int w, int h) {
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
            }
            @Override
            public String toString() {
                return String.format("{x=%d, y=%d, w=%d, h=%d}", x, y, w, h);
            }
        }

        private int backgroundColor;
        private List<Frame> frames;

        private SheetImageLabel(BufferedImage bufferedImage) {
            super(bufferedImage);
        }

        @Override
        public void setImage(BufferedImage image) {
            super.setImage(image);
            this.backgroundColor = this.image.getRGB(0, 0); // TODO: manual selection
            this.frames = initFrames();
        }

        private List<Frame> initFrames() {
            List<Frame> frames = new ArrayList<>();

            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    Frame f = lookForFrame(x, y);
                    if (f != null) {
                        frames.add(f);
                    }
                }
            }

            return frames;
        }

        private Frame lookForFrame(int x1, int y1) {
            // Checks starting coord and north/west first.
            if (isBG(x1, y1) || !isBG(x1 - 1, y1) || !isBG(x1, y1 - 1)) {
                return null;
            }

            int w = 0;
            int h = 0;
            while (!isBG(x1 + w, y1)) w++;
            while (!isBG(x1, y1 + h)) h++;

            // checks for holes
            for (int x = x1; x < x1 + w; x++) {
                for (int y = y1; y < y1 + h; y++) {
                    if (isBG(x, y)) return null;
                }
            }

            // checks that there is a BG border
            for (int x = x1; x < x1 + w; x++) {
                if (!isBG(x, y1 - 1)) return null;
                if (!isBG(x, y1 + h)) return null;
            }
            for (int y = y1; y < y1 + h; y++) {
                if (!isBG(x1 - 1, y)) return null;
                if (!isBG(x1 + w, y)) return null;
            }

            return new Frame(x1, y1, w, h);
        }

        private boolean isBG(int x, int y) {
            if (x < 0 || y < 0) {
                return true;
            } else if (x > image.getWidth() || y > image.getHeight()) {
                return false;
            } else {
                return image.getRGB(x, y) == backgroundColor;
            }
        }

        public Frame findFrame(int x, int y) {
            for (Frame f : frames) {
                if (f.x <= x && x < (f.x + f.w) && f.y <= y && y < (f.y + f.h)) {
                    return f;
                }
            }
            return null;
        }
    }

    private JButton openFromImageButton;
    private JButton openToImageButton;
    private JScrollPane fromImageScrollPane;
    private JScrollPane toImageScrollPane;
    private JButton saveToImageButton;
    private JPanel mainPanel;
    private JLabel infoLabel;
    private JPanel selectedFramePanel;
    private JLabel selectedFrameInfoLabel;

    private final JFileChooser imageChooser;

    private final SheetImageLabel toImageLabel;
    private final SheetImageLabel fromImageLabel;
    private final ImageLabel selectedFrameLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Sheet Tool");
            frame.setContentPane(new SheetTool().mainPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null); // Center window

            frame.setVisible(true);
        });
    }

    private SheetTool() {
        this.imageChooser = new JFileChooser();
        imageChooser.setFileFilter(new FileNameExtensionFilter("PNG/BMP file", "png", "bmp"));

        this.fromImageLabel = new SheetImageLabel(null);
        fromImageLabel.addClickListener(this::selectFrameImage);
        fromImageScrollPane.setViewportView(fromImageLabel);
        this.toImageLabel = new SheetImageLabel(null);
        toImageLabel.addClickListener(this::drawFrameImage);
        toImageScrollPane.setViewportView(toImageLabel);
        this.selectedFrameLabel = new ImageLabel(null);
        selectedFramePanel.add(selectedFrameLabel);

        infoLabel.setText("");

        openFromImageButton.addActionListener(e -> openFromImage());
        openToImageButton.addActionListener(e -> openToImage());
        saveToImageButton.addActionListener(e -> saveToImage());
    }

    private void selectFrameImage(MouseEvent e) {
        SheetImageLabel source = (SheetImageLabel) e.getSource();
        SheetImageLabel.Frame f = source.findFrame(e.getX(), e.getY());
        if (f == null) {
            infoLabel.setText(String.format("Can't select! No frame found at (x=%d, y=%d).", e.getX(), e.getY()));
            return;
        }
        selectedFrameInfoLabel.setText(String.format("Selected (%dx%d):", f.w, f.h));
        selectedFrameLabel.setImage(source.getImage().getSubimage(f.x, f.y, f.w, f.h));
        infoLabel.setText("Selected frame: " + f);
    }

    private void drawFrameImage(MouseEvent e) {
        SheetImageLabel source = (SheetImageLabel) e.getSource();
        SheetImageLabel.Frame f = source.findFrame(e.getX(), e.getY());
        if (f == null) {
            infoLabel.setText(String.format("Can't draw! No frame found at (x=%d, y=%d).", e.getX(), e.getY()));
            return;
        }
        BufferedImage toDraw = selectedFrameLabel.getImage();
        if (toDraw.getWidth() != f.w || toDraw.getHeight() != f.h) {
            infoLabel.setText(String.format("Can't draw! Wrong size of target frame!! (%dx%d) != (%dx%d)",
                    toDraw.getWidth(), toDraw.getHeight(), f.w, f.h));
            return;
        }
        BufferedImage toDrawOnto = source.getImage();
        toDrawOnto.getGraphics().drawImage(toDraw, f.x, f.y, f.w, f.h, null);
        source.setImage(toDrawOnto);
        infoLabel.setText(String.format("Successfully drew frame at (x=%d, y=%d).", e.getX(), e.getY()));
    }

    private void openFromImage() {
        int returnVal = imageChooser.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = imageChooser.getSelectedFile();
            try {
                fromImageLabel.setImage(ImageIO.read(f));
                infoLabel.setText("Successfully loaded \"from\" image.");
            } catch (IOException e) {
                infoLabel.setText("Could not load \"from\" image!");
            }
        }
    }

    private void openToImage() {
        int returnVal = imageChooser.showOpenDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = imageChooser.getSelectedFile();
            try {
                toImageLabel.setImage(ImageIO.read(f));
                infoLabel.setText("Successfully loaded \"to\" image.");
            } catch (IOException e) {
                infoLabel.setText("Could not load \"to\" image!");
            }
        }
    }

    private void saveToImage() {
        int returnVal = imageChooser.showSaveDialog(mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = imageChooser.getSelectedFile();
            try {
                ImageIO.write(toImageLabel.getImage(), "png", f);
                infoLabel.setText("Successfully saved \"to\" image.");
            } catch (IOException e) {
                infoLabel.setText("Could not write \"to\" image!");
            }
        }
    }
}
