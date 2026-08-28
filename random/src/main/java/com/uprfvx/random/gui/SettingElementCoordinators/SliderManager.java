package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public class SliderManager extends SingleElementManager<Integer, JSlider> implements NumericUIManager<Integer> {

    public SliderManager(JSlider element) {
        super(element);
    }

    @Override
    public void addListener(Runnable listener) {
        element.addChangeListener(_ -> this.filteredListen(listener));
    }

    private void filteredListen(Runnable listener) {
        if(!element.getValueIsAdjusting())
            listener.run();
    }

    @Override
    public void setMinimum(Integer min) {
        element.setMinimum(min);
    }

    @Override
    public void setMaximum(Integer max) {
        element.setMaximum(max);
    }

    @Override
    public void displayValue(Integer value) {
        element.setValue(value);
    }

    @Override
    public Integer getElementValue() {
        return element.getValue();
    }
}
