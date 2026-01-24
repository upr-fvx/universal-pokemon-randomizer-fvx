package com.dabomstew.pkrandom.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

public enum Theme {
    WINDOWS("Windows", new WindowsLookAndFeel()),
    METAL("Metal", new MetalLookAndFeel()),
    FLATLAF_LIGHT("FlatLaf (Light)", new FlatLightLaf()),
    FLATLAF_DARK("FlatLaf (Dark)", new FlatDarkLaf());

    public static final Theme DEFAULT = METAL;

    private final String uiName;
    private final LookAndFeel laf;
    private final boolean installed;

    Theme(String uiName, LookAndFeel laf) {
        // this is a little ugly, doing it every time,
        // but I want to keep this installation where it is relevant,
        // and afaik it's not possible to put things in static scope before enum instantiation
        // -- voliol 2026-01-17
        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();

        this.uiName = uiName;
        this.laf = laf;
        boolean installed = false;
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getClassName().equals(laf.getClass().getName())) {
                installed = true;
                break;
            }
        }
        this.installed = installed;
    }

    public String getUiName() {
        return uiName;
    }

    public LookAndFeel getLaf() {
        return laf;
    }

    public boolean isInstalled() {
        return installed;
    }
}
