package com.dabomstew.pkrandom.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;

enum Theme {
    WINDOWS(new WindowsLookAndFeel()),
    METAL(new MetalLookAndFeel()),
    FLATLAF_LIGHT(new FlatLightLaf()),
    FLATLAF_DARK(new FlatDarkLaf());

    public static final Theme DEFAULT = METAL;

    static {
        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();
    }

    private final LookAndFeel laf;
    private final boolean installed;

    Theme(LookAndFeel laf) {
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

    public LookAndFeel getLaf() {
        return laf;
    }

    public boolean isInstalled() {
        return installed;
    }
}
