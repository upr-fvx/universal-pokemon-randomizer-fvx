package com.dabomstew.pkrandom.gui;

// Based on Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider() {
        int MIN_VALUE = -100;
        int MAX_VALUE = 150;
        int DEFAULT_VALUE = 0;

        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        slider.addChangeListener(e -> {
            JSlider s = (JSlider) e.getSource();
            spinner.setValue(s.getValue());
        });
        slider.setMinimum(MIN_VALUE);
        slider.setMaximum(MAX_VALUE);
        slider.setValue(DEFAULT_VALUE);
        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        this.add(slider);

        spinner.setModel(new SpinnerNumberModel(DEFAULT_VALUE, MIN_VALUE, MAX_VALUE, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0'%'"));
        spinner.addChangeListener(e -> {
            JSpinner s = (JSpinner) e.getSource();
            slider.setValue((Integer) s.getValue());
        });
        this.add(spinner);
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
