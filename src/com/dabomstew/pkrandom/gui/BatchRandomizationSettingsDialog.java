package com.dabomstew.pkrandom.gui;

/*----------------------------------------------------------------------------*/
/*--  BatchRandomizationSettingsDialog.java - a dialog for configuring      --*/
/*--                                          batch randomization settings  --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

import com.dabomstew.pkrandom.BatchRandomizationSettings;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class BatchRandomizationSettingsDialog extends JDialog {
    private JPanel mainPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JCheckBox enableBatchRandomizationCheckBox;
    private JSpinner numberOfRandomizedROMsSpinner;
    private JSpinner startingIndexSpinner;
    private JTextField fileNamePrefixTextField;
    private JCheckBox generateLogFilesCheckBox;
    private JCheckBox autoAdvanceIndexCheckBox;
    private JButton chooseDirectoryButton;
    private JLabel outputDirectoryLabel;

    private JFileChooser outputDirectoryFileChooser;

    private final BatchRandomizationSettings currentSettings;

    public BatchRandomizationSettings getCurrentSettings() {
        return this.currentSettings;
    }

    public BatchRandomizationSettingsDialog(JFrame parent, BatchRandomizationSettings currentSettings) {
        super(parent, true);
        add(mainPanel);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dabomstew/pkrandom/gui/Bundle");
        setTitle(bundle.getString("BatchRandomizationSettingsDialog.title"));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        getRootPane().setDefaultButton(okButton);

        this.currentSettings = currentSettings.clone();

        initializeControls();
        setLocationRelativeTo(parent);
        pack();
        setVisible(true);
    }

    private void onOK() {
        updateSettings();
        setVisible(false);
    }

    private void onCancel() {
        // add your code here if necessary
        setVisible(false);
    }

    private void initializeControls() {
        outputDirectoryFileChooser = new JFileChooser();
        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        mainPanel.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        SpinnerNumberModel numberOfRandomizedROMsModel = new SpinnerNumberModel(1,1, Integer.MAX_VALUE, 1);
        numberOfRandomizedROMsSpinner.setModel(numberOfRandomizedROMsModel);

        SpinnerNumberModel startingIndexModel = new SpinnerNumberModel(1,0, Integer.MAX_VALUE, 1);
        startingIndexSpinner.setModel(startingIndexModel);

        chooseDirectoryButton.addActionListener(e -> {
            int selectionResult = outputDirectoryFileChooser.showDialog(this, "Select");
            if (selectionResult == JFileChooser.APPROVE_OPTION) {
                outputDirectoryFileChooser.setCurrentDirectory(outputDirectoryFileChooser.getSelectedFile());
                outputDirectoryLabel.setText(outputDirectoryFileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        setInitialControlValues();
        setControlsEnabled(currentSettings.isBatchRandomizationEnabled());
    }

    private void setInitialControlValues() {
        enableBatchRandomizationCheckBox.setSelected(currentSettings.isBatchRandomizationEnabled());
        generateLogFilesCheckBox.setSelected(currentSettings.shouldGenerateLogFile());
        autoAdvanceIndexCheckBox.setSelected(currentSettings.shouldAutoAdvanceStartingIndex());
        numberOfRandomizedROMsSpinner.setValue(currentSettings.getNumberOfRandomizedROMs());
        startingIndexSpinner.setValue(currentSettings.getStartingIndex());
        fileNamePrefixTextField.setText(currentSettings.getFileNamePrefix());
        outputDirectoryLabel.setText(currentSettings.getOutputDirectory());
        outputDirectoryFileChooser.setCurrentDirectory(new File(currentSettings.getOutputDirectory()));
        outputDirectoryFileChooser.setSelectedFile(new File(currentSettings.getOutputDirectory()));
        outputDirectoryFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        enableBatchRandomizationCheckBox.addActionListener(a -> setControlsEnabled(enableBatchRandomizationCheckBox.isSelected()));
    }

    private void setControlsEnabled(boolean enabled) {
        numberOfRandomizedROMsSpinner.setEnabled(enabled);
        startingIndexSpinner.setEnabled(enabled);
        fileNamePrefixTextField.setEnabled(enabled);
        generateLogFilesCheckBox.setEnabled(enabled);
        autoAdvanceIndexCheckBox.setEnabled(enabled);
        chooseDirectoryButton.setEnabled(enabled);
    }

    private void updateSettings() {
        currentSettings.setBatchRandomizationEnabled(enableBatchRandomizationCheckBox.isSelected());
        currentSettings.setGenerateLogFile(generateLogFilesCheckBox.isSelected());
        currentSettings.setAutoAdvanceStartingIndex(autoAdvanceIndexCheckBox.isSelected());
        currentSettings.setNumberOfRandomizedROMs((Integer) numberOfRandomizedROMsSpinner.getValue());
        currentSettings.setStartingIndex((Integer) startingIndexSpinner.getValue());
        currentSettings.setFileNamePrefix(fileNamePrefixTextField.getText());
        currentSettings.setOutputDirectory(outputDirectoryFileChooser.getSelectedFile().getAbsolutePath());
    }
}
