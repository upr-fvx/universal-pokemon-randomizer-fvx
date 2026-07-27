package com.uprfvx.random.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

public class ThemeSelectionDialog extends JDialog {
    private JPanel contentPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JPanel mainPanel;

    private Theme selectedTheme;
    private final RandomizerGUI parentGUI;

    public ThemeSelectionDialog(RandomizerGUI parentGUI, JFrame frame) {
        super(frame, true);

        this.parentGUI = parentGUI;

        ButtonGroup buttonGroup = new ButtonGroup();
        for (Theme t : Theme.values()) {
            JRadioButton rb = new JRadioButton(t.getUiName());
            rb.addActionListener(e -> selectedTheme = t);
            rb.setEnabled(t.isInstalled());
            mainPanel.setLayout(new FlowLayout());
            mainPanel.add(rb);

            buttonGroup.add(rb);
            if (t == parentGUI.getTheme()) {
                rb.doClick();
            }
        }

        ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/uprfvx/random/gui/Bundle");

        setTitle(bundle.getString("GUI.themeSelectionMenuItem.text"));
        setContentPane(contentPane);
        setLocationRelativeTo(frame);
        getRootPane().setDefaultButton(okButton);

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
        applyButton.addActionListener(e -> onApply());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setVisible(true);
    }

    private void onOK() {
        parentGUI.setTheme(selectedTheme);
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onApply() {
        parentGUI.setTheme(selectedTheme);
    }
}
