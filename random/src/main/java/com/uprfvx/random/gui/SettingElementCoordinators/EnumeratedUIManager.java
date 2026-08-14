package com.uprfvx.random.gui.SettingElementCoordinators;

import java.util.Collection;
import java.util.Map;

public interface EnumeratedUIManager<E extends Enum<E>> extends UIManager<E> {

    void setEnabled(Map<E, Boolean> enablement);
    void setVisible(Map<E, Boolean> visibility);

    Collection<E> getValues();
}
