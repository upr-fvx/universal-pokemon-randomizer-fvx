package com.dabomstew.pkrandom.gui;

import com.dabomstew.pkrandom.Version;
import com.dabomstew.pkrandom.customnames.CustomNamesSet;
import com.dabomstew.pkrandom.exceptions.InvalidSupplementFilesException;
import com.dabomstew.pkromio.RootPath;
import com.dabomstew.pkromio.gamedata.PlayerCharacterType;
import com.dabomstew.pkromio.graphics.packs.CustomPlayerGraphics;
import com.dabomstew.pkromio.graphics.packs.GraphicsPack;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.RomOpener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * A {@link JDialog} to allow use of preset files or random seed/config string pairs to produce premade ROMs.
 */
public class PresetLoadDialog extends JDialog {
    private JPanel contentPane;
    private JButton applyButton;
    private JButton cancelButton;
    private JButton presetFileButton;
    private JButton romButton;
    private JTextField presetFileField;
    private JTextField settingsStringField;
    private JTextField seedField;
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

    private boolean enforceFieldCheck = true;
    private CustomPlayerGraphics lastUsedCPG;

    private RomHandler currentROM;
    private CustomNamesSet customNames;
    private CustomPlayerGraphics customPlayerGraphics;
    private String requiredName;
    private boolean completed;

    public PresetLoadDialog(RandomizerGUI parentGUI, JFrame frame, JFileChooser romFileChooser, RomOpener romOpener) {
        super(frame, true);

        this.parentGUI = parentGUI;

        this.bundle = java.util.ResourceBundle.getBundle("com/dabomstew/pkrandom/gui/Bundle");

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
        presetFileButton.addActionListener(e -> onPresetFileButton());
        romButton.addActionListener(e -> onRomButton());

        DocumentListener checkListener = new CheckDocumentListener();
        seedField.getDocument().addDocumentListener(checkListener);
        settingsStringField.getDocument().addDocumentListener(checkListener);

        cpgUseCheckButton.addActionListener(e -> onCPGUseCheckButton());
        cpgSelectLastButton.addActionListener(e -> onCPGSelectLastButton());

        applyButton.addActionListener(e -> onApply());
        cancelButton.addActionListener(e -> dispose());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private class CheckDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (enforceFieldCheck) checkValues();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (enforceFieldCheck) checkValues();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (enforceFieldCheck) checkValues();
        }
    }

    private boolean checkValues() {
        String name;
        try {
            Long.parseLong(seedField.getText());
        } catch (NumberFormatException ex) {
            invalidValues();
            return false;
        }

        // 161 onwards: look for version number
        String configString = settingsStringField.getText();
        if (configString.length() < 3) {
            invalidValues();
            return false;
        }

        try {
            int presetVersionNumber = Integer.parseInt(configString.substring(0, 3));
            if (presetVersionNumber != Version.VERSION) {
                promptForDifferentRandomizerVersion(presetVersionNumber);
                safelyClearFields();
                invalidValues();
                return false;
            }
        } catch (NumberFormatException ex) {
            invalidValues();
            return false;
        }

        try {
            name = parentGUI.getValidRequiredROMName(configString.substring(3), customNames);
        } catch (InvalidSupplementFilesException ex) {
            safelyClearFields();
            invalidValues();
            return false;
        } catch (Exception ex) {
            // other exception, just call it invalid for now
            invalidValues();
            return false;
        }
        if (name == null) {
            invalidValues();
            return false;
        }
        requiredName = name;
        romRequiredLabel.setText(String.format(bundle.getString("PresetLoadDialog.romRequiredLabel.textWithROM"),
                name));
        romButton.setEnabled(true);

        if (currentROM != null && !currentROM.getROMName().equals(name)) {
            currentROM = null;
            applyButton.setEnabled(false);
            romField.setText("");
            disableCPGSelection();
        }
        return true;
    }

    private void promptForDifferentRandomizerVersion(int presetVN) {
        // so what version number was it?
        if (presetVN > Version.VERSION) {
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


    private void safelyClearFields() {
        SwingUtilities.invokeLater(() -> {
            enforceFieldCheck = false;
            settingsStringField.setText("");
            seedField.setText("");
            enforceFieldCheck = true;
        });
    }

    private void invalidValues() {
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
            try {
                DataInputStream dis = new DataInputStream(Files.newInputStream(fh.toPath()));
                int checkInt = dis.readInt();
                if (checkInt != Version.VERSION) {
                    dis.close();
                    promptForDifferentRandomizerVersion(checkInt);
                    return;
                }
                long seed = dis.readLong();
                String preset = dis.readUTF();
                customNames = new CustomNamesSet(dis);
                enforceFieldCheck = false;
                seedField.setText(Long.toString(seed));
                settingsStringField.setText(checkInt + "" + preset);
                enforceFieldCheck = true;
                if (checkValues()) {
                    seedField.setEnabled(false);
                    settingsStringField.setEnabled(false);
                    presetFileField.setText(fh.getAbsolutePath());
                } else {
                    seedField.setText("");
                    settingsStringField.setText("");
                    seedField.setEnabled(true);
                    settingsStringField.setEnabled(true);
                    presetFileField.setText("");
                    customNames = null;
                    JOptionPane.showMessageDialog(this, bundle.getString("PresetLoadDialog.invalidSeedFile"));
                }
                dis.close();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, bundle.getString("PresetLoadDialog.loadingSeedFileFailed"));
            }
        }
    }

    private void onRomButton() {
        romFileChooser.setSelectedFile(null);
        int returnVal = romFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = romFileChooser.getSelectedFile();

            JDialog opDialog = new OperationDialog(bundle.getString("GUI.loadingText"), this, true);
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
                                bundle.getString("GUI.loadFailedNoLog"));
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
            Scanner scanner = new Scanner(config, "UTF-8");
            while (scanner.hasNext()) {
                String q = scanner.nextLine().trim();
                System.out.println(q);
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
        } catch (FileNotFoundException ignored) {
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
        if (customNames == null) {
            try {
                customNames = CustomNamesSet.readNamesFromFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        return Long.parseLong(seedField.getText());
    }

    public String getSettingsString() {
        return settingsStringField.getText().substring(3);
    }

    public CustomNamesSet getCustomNames() {
        return customNames;
    }

    public CustomPlayerGraphics getCustomPlayerGraphics() {
        return customPlayerGraphics;
    }

}
