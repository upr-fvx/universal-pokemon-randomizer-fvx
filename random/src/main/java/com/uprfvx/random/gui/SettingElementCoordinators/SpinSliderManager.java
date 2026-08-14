package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.gui.SpinSlider;

import javax.swing.event.ChangeListener;

public class SpinSliderManager extends SingleElementManager<Integer, SpinSlider> implements NumericUIManager<Integer> {

    public SpinSliderManager(SpinSlider element) {
        super(element);
    }

    @Override
    public void addListener(ChangeListener listener) {
        element.addChangeListener(listener);
    }

    @Override
    public void displayValue(Integer newValue) {
        element.setValue(newValue);
    }

    @Override
    public Integer getElementValue() {
        return element.getValue();
    }

    @Override
    public void setMinimum(Integer min) {
        element.setMinimum(min);
    }

    @Override
    public void setMaximum(Integer max) {
        element.setMaximum(max);
    }
}
