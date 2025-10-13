package com.dabomstew.pkrandom.gui;

import com.dabomstew.pkrandom.SysConstants;
import com.dabomstew.pkromio.constants.Gen3Constants;
import com.dabomstew.pkromio.gamedata.PlayerCharacterType;
import com.dabomstew.pkromio.graphics.packs.*;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPGSelection {

    private static final Random RND = new Random();

    private JPanel form;
    private JComboBox<GraphicsPack> comboBox;
    private JButton randomButton;
    private GraphicsPackInfo infoForm;
    private JLabel replaceLabel;
    private JRadioButton replaceRadioButton1;
    private JRadioButton replaceRadioButton2;

    public CPGSelection() {
        comboBox.addItemListener(e -> {
            GraphicsPack cpg = (GraphicsPack) e.getItem();
            infoForm.setGraphicsPack(cpg);
        });
        randomButton.addActionListener(e -> {
            int randomIndex;
            do {
                randomIndex = RND.nextInt(comboBox.getItemCount());
            } while (randomIndex == comboBox.getSelectedIndex());
            comboBox.setSelectedIndex(randomIndex);
        });
        setInitialState();
    }

    public void setInitialState() {
        setEnabled(false);
        setReplaceChoiceVisible(true);
        comboBox.setModel(new DefaultComboBoxModel<>());
        infoForm.setGraphicsPack(null);
    }

    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
        randomButton.setEnabled(enabled && comboBox.getModel().getSize() > 1);
        infoForm.setEnabled(enabled);
        replaceRadioButton1.setEnabled(enabled);
        replaceRadioButton2.setEnabled(enabled);
        if (enabled) {
            replaceRadioButton1.setSelected(true);
        } else {
            replaceRadioButton1.setSelected(false);
            replaceRadioButton2.setSelected(false);
        }
    }

    public void setReplaceChoiceVisible(boolean visible) {
        replaceLabel.setVisible(visible);
        replaceRadioButton1.setVisible(visible);
        replaceRadioButton2.setVisible(visible);
    }

    public boolean isReplaceChoiceVisible() {
        return replaceLabel.isVisible();
    }

    public void setVisible(boolean visible) {
        form.setVisible(visible);
    }

    public void fillComboBox(RomHandler romHandler) {
        DefaultComboBoxModel<GraphicsPack> comboBoxModel = new DefaultComboBoxModel<>();
        comboBox.setModel(comboBoxModel);
        File players = new File(SysConstants.customPCGDirectory);
        File[] playerDirectories = players.listFiles(File::isDirectory);
        if (playerDirectories != null) {
            for (File playerDir : playerDirectories) {
                try {
                    String path = playerDir.getCanonicalPath();
                    List<GraphicsPackEntry> entries = GraphicsPackEntry.readAllFromFolder(path);
                    entries.forEach(entry -> {
                        if (entry.getStringValue("RomType").equalsIgnoreCase("Gen1") && romHandler.generationOfPokemon() == 1) {
                            comboBoxModel.addElement(new Gen1PlayerCharacterGraphics(entry));
                        } else if (entry.getStringValue("RomType").equalsIgnoreCase("Gen2") && romHandler.generationOfPokemon() == 2) {
                            comboBoxModel.addElement(new Gen2PlayerCharacterGraphics(entry));
                        } else if (romHandler.generationOfPokemon() == 3) {
                            if ((romHandler.getROMType() == Gen3Constants.RomType_Ruby ||
                                    romHandler.getROMType() == Gen3Constants.RomType_Sapp ||
                                    romHandler.getROMType() == Gen3Constants.RomType_Em) &&
                                    entry.getStringValue("RomType").equalsIgnoreCase("RSE")) {
                                comboBoxModel.addElement(new RSEPlayerCharacterGraphics(entry));
                            } else if (romHandler.getROMType() == Gen3Constants.RomType_FRLG &&
                                    entry.getStringValue("RomType").equalsIgnoreCase("FRLG")) {
                                comboBoxModel.addElement(new FRLGPlayerCharacterGraphics(entry));
                            }
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Could not read " + playerDir);
                    e.printStackTrace();
                }
            }
        }
        setReplaceChoiceVisible(romHandler.hasMultiplePlayerCharacters());
    }

    /**
     * Returns the full list of {@link GraphicsPack}s, that the CPGSelection has available.
     */
    public List<GraphicsPack> getGraphicsPacks() {
        List<GraphicsPack> graphicsPacks = new ArrayList<>(comboBox.getModel().getSize());
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            graphicsPacks.add(comboBox.getItemAt(i));
        }
        return graphicsPacks;
    }

    public void setCustomPlayerGraphics(CustomPlayerGraphics cpg) {
        if (!isReplaceChoiceVisible() && cpg.getTypeToReplace() != PlayerCharacterType.PC1) {
            throw new IllegalArgumentException("PlayerCharacterType " + cpg.getTypeToReplace() + " is not allowed " +
                    "for this CPGSelection.");
        }
        int setIndex = -1;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            if (cpg.getGraphicsPack() == comboBox.getItemAt(i)) {
                setIndex = i;
                break;
            }
        }
        if (setIndex == -1) {
            throw new IllegalArgumentException("The GraphicsPack of cpg (" + cpg.getGraphicsPack().getName() +
                    ")is not a valid option for this CPGSelection.");
        }
        comboBox.setSelectedIndex(setIndex);
        selectReplaceRadioButton(cpg.getTypeToReplace());
    }

    private void selectReplaceRadioButton(PlayerCharacterType typeToReplace) {
        if (typeToReplace == PlayerCharacterType.PC1) {
            replaceRadioButton1.setSelected(true);
        } else {
            replaceRadioButton2.setSelected(true);
        }
    }

    public CustomPlayerGraphics getCustomPlayerGraphics() {
        GraphicsPack pack = (GraphicsPack) comboBox.getSelectedItem();
        PlayerCharacterType type = replaceRadioButton1.isSelected() ? PlayerCharacterType.PC1 : PlayerCharacterType.PC2;
        return new CustomPlayerGraphics(pack, type);
    }

}
