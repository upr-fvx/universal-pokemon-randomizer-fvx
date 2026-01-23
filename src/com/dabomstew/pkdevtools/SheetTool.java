package com.dabomstew.pkdevtools;

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

public class SheetTool {

    private static class ImageLabel extends JLabel {

        protected BufferedImage bim;
        private final ImageIcon icon;

        private ImageLabel(BufferedImage bufferedImage) {
            this.icon = new ImageIcon();
            setIcon(icon);
            setImage(bufferedImage);
        }

        public void setImage(BufferedImage bim) {
            if (bim == null) {
                bim = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
            }
            this.bim = bim;
            update();
        }

        private void update() {
            icon.setImage(bim);
            revalidate();
            repaint();
        }
    }

    private class SheetImageLabel extends ImageLabel {

        private class Frame {
            int x, y, w, h;
            private Frame(int x, int y, int w, int h) {
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
            }
            @Override
            public String toString() {
                return String.format("{%d, %d, %d, %d}", x, y, w, h);
            }
        }

        private int backgroundColor;
        private List<Frame> frames;

        private SheetImageLabel(BufferedImage bufferedImage) {
            super(bufferedImage);
            // stupid super-long mouse listeners. why can't there just be a clicklistener...
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectFrameImage(e);
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

        @Override
        public void setImage(BufferedImage bim) {
            super.setImage(bim);
            this.backgroundColor = this.bim.getRGB(0, 0); // TODO: manual selection
            this.frames = initFrames();
            System.out.println(frames);
        }

        private List<Frame> initFrames() {
            List<Frame> frames = new ArrayList<>();

            for (int x = 0; x < bim.getWidth(); x++) {
                for (int y = 0; y < bim.getHeight(); y++) {
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
            } else if (x > bim.getWidth() || y > bim.getHeight()) {
                return false;
            } else {
                return bim.getRGB(x, y) == backgroundColor;
            }
        }

        private void selectFrameImage(MouseEvent e) {
            Frame f = findFrame(e.getX(), e.getY());
            if (f == null) {
                return;
            }
            selectedFrameLabel.setImage(bim.getSubimage(f.x, f.y, f.w, f.h));
        }

        private Frame findFrame(int x, int y) {
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
        fromImageScrollPane.setViewportView(fromImageLabel);
        this.toImageLabel = new SheetImageLabel(null);
        toImageScrollPane.setViewportView(toImageLabel);
        this.selectedFrameLabel = new ImageLabel(null);
        selectedFramePanel.add(selectedFrameLabel);

        infoLabel.setText("");

        openFromImageButton.addActionListener(e -> openFromImage());
        openToImageButton.addActionListener(e -> openToImage());
        saveToImageButton.addActionListener(e -> saveToImage());
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
                ImageIO.write(toImageLabel.bim, "png", f);
                infoLabel.setText("Successfully saved \"to\" image.");
            } catch (IOException e) {
                infoLabel.setText("Could not write \"to\" image!");
            }
        }
    }
}
