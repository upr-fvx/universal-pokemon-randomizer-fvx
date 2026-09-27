package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.*;
import java.util.*;
import java.util.function.Function;

/**
 * A class for managing Integers in a {@link JComboBox}.
 * These essentially act as enums, being individually enable-able and not caring about the underlying min/max values.
 * Thus, the name of this class.
 */
public class IntegerEnumComboBoxManager extends SingleElementManager<Integer, JComboBox<String>> {

    // This class is very similar to EnumComboBoxManager. If you can manage to merge their functionality in
    // some pretty way, feel free.

    private final List<Integer> valueOrder;
    private final Function<Integer, String> displayFunction;

    Map<Integer, String> valueToDisplay;
    Map<String, Integer> displayToValue;
    Map<Integer, Boolean> enablement;
    Map<Integer, Boolean> visibility;

    public IntegerEnumComboBoxManager(JComboBox<String> element, List<Integer> valueOrder,
                                      Function<Integer, String> displayFunction) {
        super(element);

        this.valueOrder = valueOrder;
        this.displayFunction = displayFunction;
        updateDisplay();
    }

    public void updateDisplay() {
        valueToDisplay = new HashMap<>();
        displayToValue = new HashMap<>();
        for (int value : valueOrder) {
            valueToDisplay.put(value, displayFunction.apply(value));
            displayToValue.put(displayFunction.apply(value), value);
        }
        refreshModel();
    }

    public void setEnabled(Map<Integer, Boolean> enablement) {
        this.enablement = enablement;
        refreshModel();
    }

    public void setVisible(Map<Integer, Boolean> visibility) {
        this.visibility = visibility;
        refreshModel();
    }

    private void refreshModel() {
        List<String> displayedItems = new ArrayList<>();

        for (int value : valueOrder) {
            if((enablement == null || enablement.get(value))
                    && (visibility == null || visibility.get(value))) {
                displayedItems.add(valueToDisplay.get(value));
            }
        }

        String selectedItem = element.getItemAt(element.getSelectedIndex());
        element.setModel(new DefaultComboBoxModel<>(displayedItems.toArray(new String[0])));
        element.setSelectedItem(selectedItem);
    }

    public Collection<Integer> getValues() {
        return valueOrder;
    }

    @Override
    public void displayValue(Integer value) {
        element.setSelectedItem(valueToDisplay.get(value));
    }

    @Override
    public Integer getElementValue() {
        return displayToValue.get(element.getItemAt(element.getSelectedIndex()));
    }

    @Override
    public void addListener(Runnable listener) {
        element.addActionListener(_ -> listener.run());
    }
}
