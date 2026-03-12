package com.dabomstew.pkrandom.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public enum Theme {
    WINDOWS("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"),
    METAL("Metal", "javax.swing.plaf.metal.MetalLookAndFeel"),
    FLATLAF_LIGHT("FlatLaf (Light)", "com.formdev.flatlaf.FlatLightLaf"),
    FLATLAF_DARK("FlatLaf (Dark)", "com.formdev.flatlaf.FlatDarkLaf");

    public static final Theme DEFAULT = METAL;

    private final String uiName;
    private final boolean installed;
    private final LookAndFeel laf;

    Theme(String uiName, String className) {
        // this is a little ugly, doing it every time,
        // but I want to keep this installation where it is relevant,
        // and afaik it's not possible to put things in static scope before enum instantiation
        // -- voliol 2026-01-17
        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();

        this.uiName = uiName;
        boolean installed = false;
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getClassName().equals(className)) {
                installed = true;
                break;
            }
        }

        LookAndFeel laf;
        if (installed) {
            try {
                laf = (LookAndFeel) Class.forName(className).getConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                installed = false;
                laf = null;
                System.out.println("Could not init Theme.laf from class name=" + className + ". " + e);
            }
        } else {
            laf = null;
        }
        this.installed = installed;
        this.laf = laf;
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
