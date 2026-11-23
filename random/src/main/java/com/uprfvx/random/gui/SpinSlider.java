package com.uprfvx.random.gui;

// Based on Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Dictionary;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider() {
        // Set up slider
        slider.addChangeListener(_ -> {
            if (slider.getValueIsAdjusting()) {
                spinner.setValue(slider.getValue());
            }
        });
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // Set up Spinner
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0'%'");
        spinner.setEditor(editor);
        spinner.addChangeListener(_ -> slider.setValue((Integer) spinner.getValue()));
        spinner.setPreferredSize(new Dimension(80, 30));
        spinner.setModel(new SpinnerNumberModel(0, null, null, 1));

        // Assemble SpinSlider
        this.setLayout(new java.awt.BorderLayout(5, 0));
        this.add(slider, BorderLayout.CENTER);
        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spinnerPanel.add(spinner);
        this.add(spinnerPanel, BorderLayout.EAST);

        adjustTickSpacing();
    }

    public void setValue(int value) {
        slider.setValue(value);
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setValue(value);
    }

    public int getValue() {
        return slider.getValue();
    }

    public void setMinimum(int minimum) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMinimum(minimum);
        slider.setMinimum(minimum);
        adjustTickSpacing();
    }

    public void setMaximum(int maximum) {
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setMaximum(maximum);
        slider.setMaximum(maximum);
        adjustTickSpacing();
    }

    public void setExtents(int minimum, int maximum) {
        setMinimum(minimum);
        setMaximum(maximum);
    }

    private void adjustTickSpacing() {
        // Spacing is a bit arbitrary, merely what looks good on the spinsliders that exist
        int range = slider.getMaximum() - slider.getMinimum();
        slider.setMinorTickSpacing(5);
        Dictionary<Integer, JComponent> labels;
        if (range >= 50) {
            slider.setMajorTickSpacing(50);
            labels = slider.createStandardLabels(50);
        } else {
            slider.setMajorTickSpacing(range);
            labels = slider.createStandardLabels(range);
        }

        slider.setLabelTable(labels);
    }

    public int getMinimum() {
        return slider.getMinimum();
    }

    public void addChangeListener(ChangeListener l) {
        slider.addChangeListener(l);
    }

    @Override
    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(String toolTipText) {
        slider.setToolTipText(String.format(toolTipText, "slider"));
        spinner.setToolTipText(String.format(toolTipText, "spinner"));
    }
}
