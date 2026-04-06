package com.uprfvx.random.gui;

// Based on Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider() {
        // Set up slider
        slider.addChangeListener(e -> {
            if (slider.getValueIsAdjusting()) {
                spinner.setValue(slider.getValue());
            }
        });
        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // Set up Spinner
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0'%'");
        spinner.setEditor(editor);
        spinner.addChangeListener(e -> slider.setValue((Integer) spinner.getValue()));
        spinner.setPreferredSize(new Dimension(80, 30));

        // Assemble SpinSlider
        this.setLayout(new java.awt.BorderLayout(5, 0));
        this.add(slider, BorderLayout.CENTER);
        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spinnerPanel.add(spinner);
        this.add(spinnerPanel, BorderLayout.EAST);
    }

    public void setModel(SpinnerNumberModel model) {
        slider.setMinimum((Integer) model.getMinimum());
        slider.setMaximum((Integer) model.getMaximum());
        spinner.setModel(model);
        setValue((Integer) model.getValue());
    }

    public void setValue(int value) {
        slider.setValue(value);
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        model.setValue(value);
    }

    public int getValue() {
        return slider.getValue();
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
