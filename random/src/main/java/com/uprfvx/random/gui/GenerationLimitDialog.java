package com.uprfvx.random.gui;

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

import com.uprfvx.romio.gamedata.GenRestrictions;

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
    private JCheckBox gen8CheckBox;
    private JCheckBox gen9CheckBox;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JLabel xyWarningLabel;
    private JCheckBox allowEvolutionaryRelativesCheckBox;
    private JCheckBox includeMegaFormsCheckBox;
    private JCheckBox includeGigantamaxFormsCheckBox;
    private JCheckBox allowRegionalFormsAcrossGenLimitCheckBox;
    private JCheckBox includeMegaItemsCheckBox;
    private JCheckBox includeZCrystalItemsCheckBox;
    private JCheckBox includeDynamaxGmaxItemsCheckBox;

    private boolean pressedOk;
    private boolean isXY;

    public GenerationLimitDialog(JFrame parent, GenRestrictions current, int generation, boolean isXY,
                                 boolean allowMegaForms, boolean allowGigantamaxForms,
                                 boolean allowRegionalFormsAcrossGenLimit,
                                 boolean includeMegaItems, boolean includeZCrystalItems,
                                 boolean includeDynamaxGmaxItems) {
        super(parent, true);
        add(mainPanel);
        this.isXY = isXY;
        initComponents();
        initialState(generation);
        restoreSpecialFormAndItemOptions(allowMegaForms, allowGigantamaxForms,
                allowRegionalFormsAcrossGenLimit, includeMegaItems, includeZCrystalItems,
                includeDynamaxGmaxItems);
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
        JCheckBox[] generationCheckBoxes = generationCheckBoxes();
        for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            gr.setGenAllowed(gen, generationCheckBoxes[gen - 1].isSelected());
        }
        gr.setAllowEvolutionaryRelatives(allowEvolutionaryRelativesCheckBox.isSelected());
        return gr;
    }

    public boolean isAllowMegaForms() {
        return includeMegaFormsCheckBox.isSelected();
    }

    public boolean isAllowGigantamaxForms() {
        return includeGigantamaxFormsCheckBox.isSelected();
    }

    public boolean isAllowRegionalFormsAcrossGenLimit() {
        return allowRegionalFormsAcrossGenLimitCheckBox.isSelected();
    }

    public boolean isIncludeMegaItems() {
        return includeMegaItemsCheckBox.isSelected();
    }

    public boolean isIncludeZCrystalItems() {
        return includeZCrystalItemsCheckBox.isSelected();
    }

    public boolean isIncludeDynamaxGmaxItems() {
        return includeDynamaxGmaxItemsCheckBox.isSelected();
    }

    private void initialState(int generation) {
        JCheckBox[] generationCheckBoxes = generationCheckBoxes();
        for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            generationCheckBoxes[gen - 1].setVisible(gen <= generation);
        }

        allowEvolutionaryRelativesCheckBox.setEnabled(false);
        allowEvolutionaryRelativesCheckBox.setSelected(false);
    }

    private void restoreSpecialFormAndItemOptions(boolean allowMegaForms, boolean allowGigantamaxForms,
                                                  boolean allowRegionalFormsAcrossGenLimit,
                                                  boolean includeMegaItems, boolean includeZCrystalItems,
                                                  boolean includeDynamaxGmaxItems) {
        includeMegaFormsCheckBox.setSelected(allowMegaForms);
        includeGigantamaxFormsCheckBox.setSelected(allowGigantamaxForms);
        allowRegionalFormsAcrossGenLimitCheckBox.setSelected(allowRegionalFormsAcrossGenLimit);
        includeMegaItemsCheckBox.setSelected(includeMegaItems);
        includeZCrystalItemsCheckBox.setSelected(includeZCrystalItems);
        includeDynamaxGmaxItemsCheckBox.setSelected(includeDynamaxGmaxItems);
    }

    private void restoreFrom(GenRestrictions restrict) {
        JCheckBox[] generationCheckBoxes = generationCheckBoxes();
        for (int gen = 1; gen <= GenRestrictions.MAX_GENERATION; gen++) {
            generationCheckBoxes[gen - 1].setSelected(restrict.isGenAllowed(gen));
        }
        allowEvolutionaryRelativesCheckBox.setSelected(restrict.isAllowEvolutionaryRelatives());
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/uprfvx/random/gui/Bundle");
        setTitle(bundle.getString("GenerationLimitDialog.title"));
        gen1CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen2CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen3CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen4CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen5CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen6CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen7CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen8CheckBox.addActionListener(ev -> enableAndDisableBoxes());
        gen9CheckBox.addActionListener(ev -> enableAndDisableBoxes());
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

        if (anyGenerationSelected()) {
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

    private JCheckBox[] generationCheckBoxes() {
        return new JCheckBox[] {
                gen1CheckBox, gen2CheckBox, gen3CheckBox, gen4CheckBox, gen5CheckBox,
                gen6CheckBox, gen7CheckBox, gen8CheckBox, gen9CheckBox
        };
    }

    private boolean anyGenerationSelected() {
        for (JCheckBox checkBox : generationCheckBoxes()) {
            if (checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }
}
