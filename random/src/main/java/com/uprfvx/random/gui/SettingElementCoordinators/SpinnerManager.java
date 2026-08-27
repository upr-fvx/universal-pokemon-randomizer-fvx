package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.text.ParseException;

public class SpinnerManager extends SingleElementManager<Integer, JSpinner> implements NumericUIManager<Integer> {

    public SpinnerManager(JSpinner element) {
        super(element);

        element.setModel(new SpinnerNumberModel(0, null, null, 1));
    }

    @Override
    public void displayValue(Integer newValue) {
        element.setValue(newValue);

        try {
            element.commitEdit();
        } catch(ParseException e) {
            //Pretty sure this should never happen, so a do-nothing catch is fine?
            System.out.println("Code thought unreachable has been reached in SpinnerManager.");
        }
    }

    @Override
    public Integer getElementValue() {
        return (Integer) element.getValue();
    }

    @Override
    public void addListener(Runnable listener) {
        element.addChangeListener(_ -> listener.run());
    }

    @Override
    public void setMinimum(Integer min) {
        SpinnerNumberModel model = (SpinnerNumberModel) element.getModel();
        model.setMinimum(min);
    }

    @Override
    public void setMaximum(Integer max) {
        SpinnerNumberModel model = (SpinnerNumberModel) element.getModel();
        model.setMaximum(max);
    }
}
