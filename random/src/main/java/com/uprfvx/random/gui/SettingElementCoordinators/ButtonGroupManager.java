package com.uprfvx.random.gui.SettingElementCoordinators;

import com.uprfvx.random.settings.Settings;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.romhandlers.RomHandler;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A class that coordinates an Enum setting with a group of buttons (such as a set of radio buttons or a dropdown menu)
 * @param <E> The Enum type to use.
 * @param <J> The type of button to use.
 */
public class ButtonGroupManager<E extends Enum<E>, J extends AbstractButton>
        implements EnumeratedUIManager<E> {

    protected final Map<E, J> elements;
    private E lastKnownValue;

    public ButtonGroupManager(Map<E, J> elements) {
        ButtonGroup group = new ButtonGroup();
        elements.values().forEach(group::add);

        this.elements = elements;
    }

    //These will probably never be called, but they should be implemented anyway.
    @Override
    public void setEnabled(boolean enabled) {
        elements.values().forEach(j-> j.setEnabled(enabled));
    }

    @Override
    public void setVisible(boolean visible) {
        elements.values().forEach(j -> j.setVisible(visible));
    }

    @Override
    public void addListener(Runnable listener) {
        elements.values().forEach(j -> j.addChangeListener(_ -> listener.run()));
    }

    @Override
    public void displayValue(E newValue) {
        elements.get(newValue).setSelected(true);
        lastKnownValue = newValue;
    }

    @Override
    public E getElementValue() {
        for (Map.Entry<E, J> element : elements.entrySet()) {
            if(element.getValue().isSelected()) {
                lastKnownValue = element.getKey();
                return element.getKey();
            }
        }

        //when switching buttons, all are briefly deselected. Therefore we need to report a value in that case.
        return lastKnownValue;
    }

    @Override
    public void setEnabled(Map<E, Boolean> enablement) {
        elements.forEach((e, j) -> j.setEnabled(enablement.get(e)));
    }

    @Override
    public void setVisible(Map<E, Boolean> visibility) {
        elements.forEach((e, j) -> j.setVisible(visibility.get(e)));
    }

    @Override
    public Collection<E> getValues() {
        return elements.keySet();
    }
}
