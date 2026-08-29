package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.event.ChangeListener;
import java.io.Serializable;

public interface UIManager<V extends Serializable> {

    void displayValue(V value);
    V getElementValue();

    void setEnabled(boolean enabled);
    void setVisible(boolean visible);

    void addListener(Runnable listener);
    //Adds a "listener" that will be run when the value is changed.
    //The Manager may filter out calls that involve no value change,
    //but the only important part is that the listener IS called any time the value DOES change.
    //(With exceptions for non-final changes, such as those occurring while dragging a slider around.)
}
