package com.dabomstew.pkrandom.gui;

import javax.swing.*;
import java.awt.event.*;

public class PresetLoadDialogNew extends JDialog {
    private JPanel contentPane;
    private JButton applyButton;
    private JButton cancelButton;
    private JButton presetFileButton;
    private JButton romButton;
    private JTextField presetFileField;
    private JTextField configStringField;
    private JTextField seedField;
    private JTextField romField;
    private JButton cpgChooseButton;
    private JCheckBox cpgUseLastCheckBox;
    private JLabel romRequiredLabel;

    public PresetLoadDialogNew() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(applyButton);

        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        PresetLoadDialogNew dialog = new PresetLoadDialogNew();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
