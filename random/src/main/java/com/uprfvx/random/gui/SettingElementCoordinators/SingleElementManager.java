package com.uprfvx.random.gui.SettingElementCoordinators;

import javax.swing.*;
import java.io.Serializable;

/**
 * Coordinates a Setting with a single UI element.
 */
public abstract class SingleElementManager<V extends Serializable, J extends JComponent>
        implements UIManager<V> {

    protected final J element;

    public SingleElementManager(J element) {
        this.element = element;
    }

    public void setEnabled(boolean enabled) {
        element.setEnabled(enabled);
    }

    public void setVisible(boolean visible) {
        element.setVisible(visible);
    }
}
