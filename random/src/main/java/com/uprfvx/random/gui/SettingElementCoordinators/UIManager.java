package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.event.ChangeListener;
import java.io.Serializable;

public interface UIManager<V extends Serializable> {

    void displayValue(V value);
    V getElementValue();

    void setEnabled(boolean enabled);
    void setVisible(boolean visible);

    void addListener(ChangeListener listener);
}
