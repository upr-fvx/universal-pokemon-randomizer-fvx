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
    public void addListener(ChangeListener listener) {
        elements.values().forEach(j -> j.addChangeListener(listener));
    }

    @Override
    public void displayValue(E newValue) {
        elements.get(newValue).setSelected(true);
    }

    @Override
    public E getElementValue() {
        for (Map.Entry<E, J> element : elements.entrySet()) {
            if(element.getValue().isSelected()) {
                return element.getKey();
            }
        }

        //shouldn't reach here unless no button is selected, somehow.
        Class e = elements.keySet().toArray()[0].getClass(); //awkward ass statement but it should be okay

        throw new IllegalStateException("No button selected in group! Enum: " + e.getName());
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
