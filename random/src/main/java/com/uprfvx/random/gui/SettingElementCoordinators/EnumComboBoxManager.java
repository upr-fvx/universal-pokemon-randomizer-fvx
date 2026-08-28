package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.*;
import java.util.*;

public class EnumComboBoxManager<E extends Enum<E>> extends SingleElementManager<E, JComboBox<String>>
        implements EnumeratedUIManager<E> {

    List<E> valueOrder;
    Map<E, String> valueToDisplay;
    Map<String, E> displayToValue;
    Map<E, Boolean> enablement;
    Map<E, Boolean> visibility;

    public EnumComboBoxManager(JComboBox<String> element, List<E> valueOrder, Map<E, String> valueToDisplay) {
        super(element);

        this.valueOrder = Collections.unmodifiableList(valueOrder);

        this.valueToDisplay = Collections.unmodifiableMap(valueToDisplay);
        Map<String, E> inverse = new HashMap<>();
        valueToDisplay.forEach((e, s) -> inverse.put(s, e));
        displayToValue = Collections.unmodifiableMap(inverse);

        refreshModel();
    }

    @Override
    public void setEnabled(Map<E, Boolean> enablement) {
        this.enablement = enablement;
        refreshModel();
    }

    @Override
    public void setVisible(Map<E, Boolean> visibility) {
        this.visibility = visibility;
        refreshModel();
    }

    private void refreshModel() {
        List<String> displayedItems = new ArrayList<>();

        for (E value : valueOrder) {
            if((enablement == null || enablement.get(value))
                    && (visibility == null || visibility.get(value))) {
                displayedItems.add(valueToDisplay.get(value));
            }
        }

        String selectedItem = element.getItemAt(element.getSelectedIndex());
        element.setModel(new DefaultComboBoxModel<>(displayedItems.toArray(new String[0])));
        element.setSelectedItem(selectedItem);
    }

    @Override
    public Collection<E> getValues() {
        return valueOrder;
    }

    @Override
    public void displayValue(E value) {
        element.setSelectedItem(valueToDisplay.get(value));
    }

    @Override
    public E getElementValue() {
        return displayToValue.get(element.getItemAt(element.getSelectedIndex()));
    }

    @Override
    public void addListener(Runnable listener) {
        element.addActionListener(_ -> listener.run());
    }
}
