package com.dabomstew.pkrandom.gui;

// Based on Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.*;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider(int minValue, int maxValue, int defaultValue) {
        this.setLayout(new java.awt.BorderLayout(5, 0));

        // Construct slider
        slider.addChangeListener(e -> {
            if (slider.getValueIsAdjusting()) {
                spinner.setValue(slider.getValue());
            }
        });
        slider.setMinimum(minValue);
        slider.setMaximum(maxValue);
        slider.setValue(defaultValue);
        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        this.add(slider, BorderLayout.CENTER);

        // Construct Spinner
        spinner.setModel(new SpinnerNumberModel(defaultValue, minValue, maxValue, 1));

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0'%'");
        spinner.setEditor(editor);

        spinner.addChangeListener(e -> {
            if (e.getSource() == spinner) {
                slider.setValue((Integer) spinner.getValue());
            }
        });

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spinnerPanel.add(spinner);

        Dimension d = new Dimension(80, 30);
        spinner.setPreferredSize(d);

        this.add(spinnerPanel, BorderLayout.EAST);
    }

    public SpinSlider() {
        this(-100, 155, 0);
    }

    public int getValue() {
        return slider.getValue();
    }

    public void setValue(int value) {
        slider.setValue(value);
    }

    @Override
    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(String toolTipText) {
        slider.setToolTipText(toolTipText);
        spinner.setToolTipText(toolTipText);
    }
}
