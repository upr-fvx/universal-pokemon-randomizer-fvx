package com.uprfvx.random;

import com.uprfvx.random.cli.CliRandomizer;
import com.uprfvx.random.cli.SettingsProfileGenerator;
import com.uprfvx.random.gui.RandomizerGUI;
import com.uprfvx.romio.RootPath;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class RandomizerLauncher {

    private RandomizerLauncher() {
    }

    public static void main(String[] args) {
        setRootPath();

        String firstCliArg = args.length > 0 ? args[0] : "";
        if (firstCliArg.equals("cli")) {
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            System.exit(CliRandomizer.invoke(commandArgs));
        } else if (firstCliArg.equals("settings-profile")) {
            String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);
            System.exit(SettingsProfileGenerator.invoke(commandArgs));
        } else {
            RandomizerGUI.main(args);
        }
    }

    private static void setRootPath() {
        URL location = RandomizerLauncher.class.getProtectionDomain().getCodeSource().getLocation();
        String file = location.getFile();
        String plusEncoded = file.replaceAll("\\+", "%2b");
        File f = new File(java.net.URLDecoder.decode(plusEncoded, StandardCharsets.UTF_8));
        RootPath.path = f.getParentFile() + File.separator;
    }
}
