package com.dabomstew.pkrandom.gui;

// Based on Source - https://stackoverflow.com/a/6067986
// Posted by trashgod, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-07, License - CC BY-SA 3.0

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFormattedTextField;
import java.awt.*;

public class SpinSlider extends JPanel {
    private final JSpinner spinner = new JSpinner();
    private final JSlider slider = new JSlider();

    public SpinSlider(int minValue, int maxValue, int defaultValue) {
        this.setLayout(new java.awt.BorderLayout(5, 0));

        // Construct slider
        slider.addChangeListener(e -> {
            JSlider s = (JSlider) e.getSource();
            //slider.getValueIsAdjusting()
            spinner.setValue(s.getValue());
        });
        slider.setMinimum(minValue);
        slider.setMaximum(maxValue);
        slider.setValue(defaultValue);
        slider.setMinorTickSpacing(5);
        slider.setMajorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        this.add(slider, java.awt.BorderLayout.CENTER);

        // Construct Spinner
        spinner.setModel(new SpinnerNumberModel(defaultValue, minValue, maxValue, 1));

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0'%'");
        spinner.setEditor(editor);
        autoSizeSpinner(spinner);

        spinner.addChangeListener(e -> {
            JSpinner s = (JSpinner) e.getSource();
            slider.setValue((Integer) s.getValue());
        });
        this.add(spinner, java.awt.BorderLayout.EAST);
    }

    public SpinSlider() {
        this(-100, 155, 0);
    }

    private static void autoSizeSpinner(JSpinner spinner) {
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        JFormattedTextField tf = editor.getTextField();

        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();

        String min = editor.getFormat().format(model.getMinimum());
        String max = editor.getFormat().format(model.getMaximum());

        String widest = min.length() > max.length() ? min : max;

        FontMetrics fm = tf.getFontMetrics(tf.getFont());
        int textWidth = fm.stringWidth(widest);

        int width = textWidth + 40; // padding + arrow buttons
        
        Dimension d = spinner.getPreferredSize();
        spinner.setPreferredSize(new Dimension(width, d.height));
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
