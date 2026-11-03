package com.uprfvx.random.gui;

import com.uprfvx.random.RandomizationCoordinator;
import com.uprfvx.random.Version;
import com.uprfvx.random.settings.SettingsManager;
import com.uprfvx.romio.RootPath;
import com.uprfvx.romio.gamedata.PlayerCharacterType;
import com.uprfvx.romio.graphics.packs.CustomPlayerGraphics;
import com.uprfvx.romio.graphics.packs.GraphicsPack;
import com.uprfvx.romio.romhandlers.RomHandler;
import com.uprfvx.romio.romio.RomOpener;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * A {@link JDialog} to allow use of preset files to produce premade ROMs.
 */
public class PresetLoadDialog extends JDialog {
    private JPanel contentPane;
    private JButton applyButton;
    private JButton cancelButton;
    private JButton presetFileButton;
    private JButton romButton;
    private JTextField presetFileField;
    private JTextField romField;
    private JCheckBox cpgUseCheckButton;
    private JButton cpgSelectLastButton;
    private JLabel romRequiredLabel;
    private CPGSelection cpgSelection;

    private final RandomizerGUI parentGUI;
    private final ResourceBundle bundle;
    private final JFileChooser presetFileChooser;
    private final JFileChooser romFileChooser;
    private final RomOpener romOpener;

    private CustomPlayerGraphics lastUsedCPG;

    private RomHandler currentROM;
    private SettingsManager settings;
    private long seed;
    private boolean raceMode;
    private boolean raceModeSavedLog;
    private CustomPlayerGraphics customPlayerGraphics;
    private String requiredName;
    private boolean completed;

    public PresetLoadDialog(RandomizerGUI parentGUI, JFrame frame, JFileChooser romFileChooser, RomOpener romOpener) {
        super(frame, true);

        this.parentGUI = parentGUI;

        this.bundle = java.util.ResourceBundle.getBundle("com/uprfvx/random/gui/Bundle");

        this.romFileChooser = romFileChooser;
        this.presetFileChooser = new JFileChooser();
        presetFileChooser.setFileFilter(new PresetFileFilter());
        presetFileChooser.setCurrentDirectory(new File(RootPath.path));

        this.romOpener = romOpener;

        setTitle(bundle.getString("PresetLoadDialog.title"));
        setContentPane(contentPane);
        setResizable(false);
        setLocationRelativeTo(frame);
        getRootPane().setDefaultButton(cancelButton);

        cpgSelection.setVisible(false);

        initListeners();

        pack();
        setVisible(true);
    }

    private void initListeners() {
        presetFileButton.addActionListener(_ -> onPresetFileButton());
        romButton.addActionListener(_ -> onRomButton());

        cpgUseCheckButton.addActionListener(_ -> onCPGUseCheckButton());
        cpgSelectLastButton.addActionListener(_ -> onCPGSelectLastButton());

        applyButton.addActionListener(_ -> onApply());
        cancelButton.addActionListener(_ -> dispose());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void promptForDifferentRandomizerVersion(int presetVN) {
        // so what version number was it?
        if (presetVN > Version.LATEST.id) {
            // it's for a newer version
            JOptionPane.showMessageDialog(this, bundle.getString("PresetLoadDialog.newerVersionRequired"));
        } else {
            // Tell them which older version to use to load this preset.
            // Occasionally it can't tell and gives you all the possible ones.
            String versionWanted;
            List<String> posVersions = new ArrayList<>();
            for (Version v : Version.ALL_VERSIONS) {
                if (v.id == presetVN) {
                    posVersions.add(v.name + "(" + v.branchName + ")");
                }
            }
            versionWanted = posVersions.isEmpty() ? "Unknown" : String.join(" OR ", posVersions);
            JOptionPane.showMessageDialog(this,
                    String.format(bundle.getString("PresetLoadDialog.olderVersionRequired"), versionWanted));
        }
    }

    private void invalidPreset() {
        currentROM = null;
        romField.setText("");
        romRequiredLabel.setText(bundle.getString("PresetLoadDialog.romRequiredLabel.text"));
        romButton.setEnabled(false);
        applyButton.setEnabled(false);
        requiredName = null;
        disableCPGSelection();
    }

    private void onPresetFileButton() {
        presetFileChooser.setSelectedFile(null);
        int returnVal = presetFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fh = presetFileChooser.getSelectedFile();
            RandomizationCoordinator rc = new RandomizationCoordinator(parentGUI);
            RandomizationCoordinator.ReadPresetResults results = rc.readPresetFile(fh);
            if (results.success()) {
                this.seed = results.seed();
                this.raceMode = results.raceMode();
                this.raceModeSavedLog = results.raceModeLogExists();
                this.settings = results.settings();

                requiredName = results.romName();
                romRequiredLabel.setText(String.format(
                        bundle.getString("PresetLoadDialog.romRequiredLabel.textWithROM"), requiredName));
                romButton.setEnabled(true);

                presetFileField.setText(fh.getAbsolutePath());

            } else {
                switch (results.exception().getType()) {
                    case IO_ERROR -> JOptionPane.showMessageDialog(this,
                            bundle.getString("PresetLoadDialog.loadingSeedFileFailed"));
                    case NO_SEED -> JOptionPane.showMessageDialog(this,
                            bundle.getString("PresetLoadDialog.invalidSeedFile"));
                    case INVALID_SETTINGS -> JOptionPane.showMessageDialog(this,
                            bundle.getString("PresetLoadDialog.invalidSeedFile")); // TODO: string for this
                    case WRONG_VERSION -> promptForDifferentRandomizerVersion(results.versionID());
                }
                invalidPreset();
            }
        }
    }

    private void onRomButton() {
        romFileChooser.setSelectedFile(null);
        int returnVal = romFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = romFileChooser.getSelectedFile();

            JDialog opDialog = new OperationDialog(bundle.getString("GUI.loadROM.loadingDialog.message"), this, true);
            Thread t = new Thread(() -> {
                SwingUtilities.invokeLater(() -> opDialog.setVisible(true));

                try {
                    RomOpener.Results results = romOpener.openRomFile(f);

                    SwingUtilities.invokeLater(() -> {
                        opDialog.setVisible(false);
                        if (results.wasOpeningSuccessful()) {
                            RomHandler checkHandler = results.getRomHandler();
                            if (checkHandler.getROMName().equals(requiredName)) {
                                // Got it
                                romField.setText(f.getAbsolutePath());
                                currentROM = checkHandler;
                                maybeEnableCPGSelection();
                                applyButton.setEnabled(true);
                            } else {
                                JOptionPane.showMessageDialog(this, String.format(
                                        bundle.getString("PresetLoadDialog.notRequiredROM"), requiredName,
                                        checkHandler.getROMName()));
                            }
                        } else {
                            parentGUI.reportOpenRomFailure(f, results);
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        opDialog.setVisible(false);
                        JOptionPane.showMessageDialog(this,
                                bundle.getString("GUI.loadROM.loadFailedNoLogDialog.message"));
                    });
                }
            });
            t.start();
        }
    }

    private void disableCPGSelection() {
        cpgUseCheckButton.setSelected(false);
        cpgUseCheckButton.setEnabled(false);
        cpgSelectLastButton.setEnabled(false);
        cpgSelection.setVisible(false);
        pack();
    }

    private void maybeEnableCPGSelection() {
        boolean cpgSupport = currentROM.hasCustomPlayerGraphicsSupport();
        cpgUseCheckButton.setEnabled(cpgSupport);
        cpgSelection.fillComboBox(currentROM);
        cpgSelection.setEnabled(true);

        if (cpgSupport) {
            lastUsedCPG = getLastUsedCPGFromConfig();
            cpgSelectLastButton.setEnabled(lastUsedCPG != null);
        }
    }

    private CustomPlayerGraphics getLastUsedCPGFromConfig() {
        String cpgName = null;
        PlayerCharacterType typeToReplace = null;
        File config = new File(RootPath.path + "config.ini");
        try {
            Scanner scanner = new Scanner(config, StandardCharsets.UTF_8);
            while (scanner.hasNext()) {
                String q = scanner.nextLine().trim();
                if (q.contains("//")) {
                    q = q.substring(0, q.indexOf("//")).trim();
                }
                String[] tokens = q.split("=", 2);
                if (tokens.length == 2) {
                    if (tokens[0].startsWith("lastusedcpg." + currentROM.getROMName() + ".pack")) {
                        cpgName = tokens[1];
                    } else if (tokens[0].startsWith("lastusedcpg." + currentROM.getROMName() + ".type")) {
                        typeToReplace = PlayerCharacterType.valueOf(tokens[1]);
                    }
                }
            }
        } catch (IOException ignored) {
            return null;
        }
        if (cpgName == null || typeToReplace == null) {
            return null;
        }
        GraphicsPack graphicsPack = null;
        for (GraphicsPack gp : cpgSelection.getGraphicsPacks()) {
            if (gp.getName().equals(cpgName)) {
                graphicsPack = gp;
                break;
            }
        }
        if (graphicsPack == null) {
            return null;
        }
        return new CustomPlayerGraphics(graphicsPack, typeToReplace);
    }

    private void onCPGUseCheckButton() {
        cpgSelection.setVisible(cpgUseCheckButton.isSelected());
        pack();
    }

    private void onCPGSelectLastButton() {
        cpgSelection.setCustomPlayerGraphics(lastUsedCPG);
        if (!cpgUseCheckButton.isSelected()) {
            cpgUseCheckButton.doClick();
        }
    }

    private void onApply() {
        if (cpgUseCheckButton.isSelected()) {
            customPlayerGraphics = cpgSelection.getCustomPlayerGraphics();
        }
        completed = true;
        dispose();
    }

    public boolean isCompleted() {
        return completed;
    }

    public RomHandler getROM() {
        return currentROM;
    }

    public long getSeed() {
        return seed;
    }

    public boolean isRaceMode() {
        return raceMode;
    }

    public boolean isRaceModeSavedLog() {
        return raceModeSavedLog;
    }

    public SettingsManager getSettings() {
        return settings;
    }

    public CustomPlayerGraphics getCustomPlayerGraphics() {
        return customPlayerGraphics;
    }

}
