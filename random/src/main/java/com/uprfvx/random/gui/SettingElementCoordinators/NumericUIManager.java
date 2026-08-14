package com.uprfvx.random.gui.SettingElementCoordinators;

public interface NumericUIManager<N extends Number & Comparable<N>> extends UIManager<N> {

    void setMinimum(N min);
    void setMaximum(N max);
}
