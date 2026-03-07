package com.dabomstew.pkrandom.gui;

// Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import java.awt.EventQueue;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider() {
        this.setLayout(new FlowLayout());

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider s = (JSlider) e.getSource();
                spinner.setValue(s.getValue());
            }
        });
        slider.setMinimum(-100);
        slider.setMaximum(150);
        slider.setValue(0);
        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        this.add(slider);
        spinner.setModel(new SpinnerNumberModel(0, -100, 150, 1));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0'%'"));
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                slider.setValue((Integer) s.getValue());
            }
        });
        this.add(spinner);
    }

    public int getValue() {
        return slider.getValue();
    }

    public void setValue(int value) {
        slider.setValue(value);
    }

    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        spinner.setEnabled(enabled);
    }
}
