package com.dabomstew.pkrandom.gui;

import com.dabomstew.pkromio.graphics.packs.GraphicsPack;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GraphicsPackInfo {

    private static final String EMPTY_TEXT = "---";

    private JPanel ImagesPanel;
    private JPanel InfoPanel;
    private JLabel nameInfoLabel;
    private JLabel descriptionInfoLabel;
    private JLabel fromInfoLabel;
    private JLabel creatorInfoLabel;
    private JLabel adapterInfoLabel;
    private JLabel nameLabel;
    private JTextArea descriptionTextArea;
    private JLabel fromLabel;
    private JLabel creatorLabel;
    private JLabel adapterLabel;
    private JPanel form;

    public GraphicsPackInfo() {
        setGraphicsPack(null);
    }

    public void setGraphicsPack(GraphicsPack graphicsPack) {
        if (graphicsPack == null) {
            setNullGraphicsPack();
        } else {
            setSampleImages(graphicsPack);
            nameLabel.setText(graphicsPack.getName());
            descriptionTextArea.setText(graphicsPack.getDescription());
            fromLabel.setText(graphicsPack.getFrom());
            creatorLabel.setText(graphicsPack.getOriginalCreator());
            adapterLabel.setText(graphicsPack.getAdapter());
        }
    }

    private void setSampleImages(GraphicsPack graphicsPack) {
        for (Component c : ImagesPanel.getComponents()) {
            ImagesPanel.remove(c);
        }
        for (BufferedImage sampleImage : graphicsPack.getSampleImages()) {
            if (sampleImage != null) {
                JLabel imageLabel = new JLabel(new ImageIcon(sampleImage));
                ImagesPanel.add(imageLabel);
            }
        }
    }

    private void setNullGraphicsPack() {
        nameLabel.setText(EMPTY_TEXT);
        descriptionTextArea.setText(EMPTY_TEXT);
        fromLabel.setText(EMPTY_TEXT);
        creatorLabel.setText(EMPTY_TEXT);
        adapterLabel.setText(EMPTY_TEXT);
    }

    public void setVisible(boolean visible) {
        form.setVisible(visible);
    }

    public void setEnabled(boolean enabled) {
        form.setEnabled(enabled);
        for (Component comp : InfoPanel.getComponents()) {
            comp.setEnabled(enabled);
        }
    }
}
