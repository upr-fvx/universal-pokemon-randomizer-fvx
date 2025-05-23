package com.dabomstew.pkrandom.gui;

/*----------------------------------------------------------------------------*/
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

import com.dabomstew.pkromio.gamedata.GenRestrictions;

import javax.swing.*;

/**
 * A GUI interface to allow users to limit which Pokemon appear based on their generation of origin.
 */
public class GenerationLimitDialog extends javax.swing.JDialog {
    private JCheckBox gen1CheckBox;
    private JCheckBox gen2CheckBox;
    private JCheckBox gen3CheckBox;
    private JCheckBox gen4CheckBox;
    private JCheckBox gen5CheckBox;
    private JCheckBox gen6CheckBox;
    private JCheckBox gen7CheckBox;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JLabel xyWarningLabel;
    private JCheckBox allowEvolutionaryRelativesCheckBox;

    private boolean pressedOk;
    private boolean isXY;

    public GenerationLimitDialog(JFrame parent, GenRestrictions current, int generation, boolean isXY) {
        super(parent, true);
        add(mainPanel);
        this.isXY = isXY;
        initComponents();
        initialState(generation);
        if (current != null) {
            current.limitToGen(generation);
            restoreFrom(current);
        }
        enableAndDisableBoxes();
        pressedOk = false;
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public boolean pressedOK() {
        return pressedOk;
    }

    public GenRestrictions getChoice() {
        GenRestrictions gr = new GenRestrictions();
        gr.setGenAllowed(1, gen1CheckBox.isSelected());
        gr.setGenAllowed(2, gen2CheckBox.isSelected());
        gr.setGenAllowed(3, gen3CheckBox.isSelected());
        gr.setGenAllowed(4, gen4CheckBox.isSelected());
        gr.setGenAllowed(5, gen5CheckBox.isSelected());
        gr.setGenAllowed(6, gen6CheckBox.isSelected());
        gr.setGenAllowed(7, gen7CheckBox.isSelected());
        gr.setAllowEvolutionaryRelatives(allowEvolutionaryRelativesCheckBox.isSelected());
        return gr;
    }

    private void initialState(int generation) {
        if (generation < 2) {
            gen2CheckBox.setVisible(false);
        }
        if (generation < 3) {
            gen3CheckBox.setVisible(false);
        }
        if (generation < 4) {
            gen4CheckBox.setVisible(false);
        }
        if (generation < 5) {
            gen5CheckBox.setVisible(false);
        }
        if (generation < 6) {
            gen6CheckBox.setVisible(false);
        }
        if (generation < 7) {
            gen7CheckBox.setVisible(false);
        }

        allowEvolutionaryRelativesCheckBox.setEnabled(false);
        allowEvolutionaryRelativesCheckBox.setSelected(false);
    }

    private void restoreFrom(GenRestrictions restrict) {
        gen1CheckBox.setSelected(restrict.isGenAllowed(1));
        gen2CheckBox.setSelected(restrict.isGenAllowed(2));
        gen3CheckBox.setSelected(restrict.isGenAllowed(3));
        gen4CheckBox.setSelected(restrict.isGenAllowed(4));
        gen5CheckBox.setSelected(restrict.isGenAllowed(5));
        gen6CheckBox.setSelected(restrict.isGenAllowed(6));
        gen7CheckBox.setSelected(restrict.isGenAllowed(7));
        allowEvolutionaryRelativesCheckBox.setSelected(restrict.isAllowEvolutionaryRelatives());
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/dabomstew/pkrandom/gui/Bundle");
        setTitle(bundle.getString("GenerationLimitDialog.title"));
        gen1CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen2CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen3CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen4CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen5CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen6CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen7CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        allowEvolutionaryRelativesCheckBox.addActionListener(ev -> enableAndDisableBoxes());
        okButton.addActionListener(evt -> okButtonActionPerformed());
        cancelButton.addActionListener(evt -> cancelButtonActionPerformed());
        xyWarningLabel.setVisible(isXY);
        if (isXY) {
            okButton.setEnabled(false);
        }
        pack();
    }

    private void enableAndDisableBoxes() {
        // To prevent softlocks on the Successor Korrina fight, only turn
        // on the OK button for XY if at least one of Gens 1-4 is selected.
        if (isXY) {
            if (gen1CheckBox.isSelected() || gen2CheckBox.isSelected() || gen3CheckBox.isSelected() || gen4CheckBox.isSelected()) {
                okButton.setEnabled(true);
            } else {
                okButton.setEnabled(false);
            }
        }

        if (gen1CheckBox.isSelected() || gen2CheckBox.isSelected() || gen3CheckBox.isSelected() ||
                gen4CheckBox.isSelected() || gen5CheckBox.isSelected() || gen6CheckBox.isSelected() ||
                gen7CheckBox.isSelected()) {
            allowEvolutionaryRelativesCheckBox.setEnabled(true);
        } else {
            allowEvolutionaryRelativesCheckBox.setEnabled(false);
            allowEvolutionaryRelativesCheckBox.setSelected(false);
        }
    }

    private void okButtonActionPerformed() {
        pressedOk = true;
        setVisible(false);
    }

    private void cancelButtonActionPerformed() {
        pressedOk = false;
        setVisible(false);
    }
}
