package com.uprfvx.random.gui;

import com.uprfvx.romio.MiscTweak;

import java.util.ResourceBundle;

public class MiscTweakStrings {

    public static String getName(MiscTweak miscTweak, ResourceBundle bundle) {
        return bundle.getString("CodeTweaks." + miscTweak.getID() + ".name");
    }

    public static String getToolTipText(MiscTweak miscTweak, ResourceBundle bundle) {
        return bundle.getString("CodeTweaks." + miscTweak.getID() + ".toolTipText");
    }
}
