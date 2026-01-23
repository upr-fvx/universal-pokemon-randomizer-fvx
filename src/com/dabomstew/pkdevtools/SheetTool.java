package com.dabomstew.pkdevtools;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SheetTool {

    private static class SheetImageLabel extends JLabel {

        private BufferedImage bufferedImage;
        private final ImageIcon icon;

        private SheetImageLabel(BufferedImage bufferedImage) {
            if (bufferedImage == null) {
                bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            }
            this.icon = new ImageIcon();
            setIcon(icon);
            setImage(bufferedImage);
        }

        public void setImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            update();
        }

        private void update() {
            icon.setImage(bufferedImage);
            revalidate();
            repaint();
        }

    }

    private JButton openFromImageButton;
    private JButton openToImageButton;
    private JScrollPane fromImageScrollPane;
    private JScrollPane toImageScrollPane;
    private JButton saveToImageButton;
    private JPanel mainPanel;
    private JLabel infoLabel;

    private final JFileChooser imageChooser;

    private final SheetImageLabel toImageLabel;
    private final SheetImageLabel fromImageLabel;

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
                ImageIO.write(toImageLabel.bufferedImage, "png", f);
                infoLabel.setText("Successfully saved \"to\" image.");
            } catch (IOException e) {
                infoLabel.setText("Could not write \"to\" image!");
            }
        }
    }
}
