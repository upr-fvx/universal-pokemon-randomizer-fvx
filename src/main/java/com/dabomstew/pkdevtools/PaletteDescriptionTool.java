package com.dabomstew.pkdevtools;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
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

import com.dabomstew.pkrandom.Settings;
import com.dabomstew.pkrandom.randomizers.Gen3to5PaletteRandomizer;
import com.dabomstew.pkromio.GFXFunctions;
import com.dabomstew.pkromio.exceptions.RomIOException;
import com.dabomstew.pkromio.gamedata.Species;
import com.dabomstew.pkromio.graphics.palettes.*;
import com.dabomstew.pkromio.romhandlers.PokemonImageGetter;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.ROMFilter;
import com.dabomstew.pkromio.romio.RomOpener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * A developer tool for filling in files with {@link PaletteDescription}s, and
 * see how each related image looks with accordingly randomized palettes.
 * <p>
 * Which file to fill is decided automatically, depending on the assigned paletteFilesID.
 * <p>
 * Currently only works for Pokémon palettes.
 */
public class PaletteDescriptionTool extends javax.swing.JFrame {

    private static final long serialVersionUID = 7741836888133659367L;

    private static final Settings SETTINGS = new Settings();
    private static final Random RND = new Random();

    private static final String TITLE = "Palette Description Tool";

    private static final int DEFAULT_SCALE = 2;
    private static final String FILE_KEY = "pokePalettes";
    private static final String ROMS_FOLDER = "test/roms";

    private static class PaletteImageLabel extends JLabel {

        /**
         * 1.0
         */
        private static final long serialVersionUID = 7324176616059578530L;

        private BufferedImage bufferedImage;
        private ImageIcon icon;
        private int scale = DEFAULT_SCALE;

        private PaletteImageLabel(BufferedImage bufferedImage) {
            if (bufferedImage == null) {
                bufferedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            }
            this.icon = new ImageIcon();
            setIcon(icon);
            setImage(bufferedImage);
        }

        public void setImage(BufferedImage bufferedImage) {
            this.bufferedImage = bufferedImage;
            update();
        }

        public void setScale(int scale) {
            this.scale = scale;
            update();
        }

        public int getPaletteIndexAtCoord(int x, int y) {
            int pixelX = x / scale;
            int pixelY = y / scale;
            return bufferedImage.getData().getSample(pixelX, pixelY, 0) + 1;
        }

        private void update() {
            Image scaled = bufferedImage.getScaledInstance(bufferedImage.getWidth() * scale,
                    bufferedImage.getHeight() * scale, Image.SCALE_DEFAULT);
            icon.setImage(scaled);
            revalidate();
            repaint();
        }

    }

    private static GridBagConstraints quickGBC(int gridx, int gridy) {
        return quickGBC(gridx, gridy, 1, 1);
    }

    private static GridBagConstraints quickGBC(int gridx, int gridy, int gridwidth, int gridheight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        return gbc;
    }

    private RomHandler romHandler;
    private final RomOpener romOpener = new RomOpener(3, 4, 5);
    private boolean romLoaded;

    private final Map<Species, Palette> originalPalettes = new HashMap<>();

    private JScrollPane entryScrollPane;
    private final JList<PaletteDescription> paletteDescriptions;
    private int lastIndex;
    private final PaletteImageLabel originalImage;
    private final PaletteImageLabel shinyImage;
    private final PaletteImageLabel exampleImage;
    private final JTextField descNameField;
    private final JTextField descBodyField;
    private final JTextField descNoteField;

    private boolean autoName;

    private final JButton saveDescButton;
    private boolean autoSave;

    private String unchangedName;
    private String unchangedBody;
    private String unchangedNote;

    public static void main(String[] args) {
        new PaletteDescriptionTool().setVisible(true);
    }

    private PaletteDescriptionTool() {
        this.setTitle(TITLE);
        this.setSize(new Dimension(800, 400));
        JPanel mainPanel = new JPanel();
        setContentPane(mainPanel);

        // ---------

        JPanel leftPanel = new JPanel();
        mainPanel.add(leftPanel);
        leftPanel.setLayout(new GridBagLayout());

        JButton loadFilesButton = new JButton("Load ROM");
        loadFilesButton.addActionListener(e -> loadBoth());
        leftPanel.add(loadFilesButton, quickGBC(0, 0));

        JButton saveButton = new JButton("Save to file");
        saveButton.addActionListener(e -> savePaletteDescriptionsFile());
        leftPanel.add(saveButton, quickGBC(1, 0));

        JButton prevButton = new JButton("Prev.");
        prevButton.addActionListener(e -> previousPaletteDescription());
        leftPanel.add(prevButton, quickGBC(0, 1));

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> nextPaletteDescription());
        leftPanel.add(nextButton, quickGBC(1, 1));

        JTextField entrySearchTextField = new JTextField(20);
        entrySearchTextField.addActionListener(new PaletteDescriptionSearchListener());

        leftPanel.add(entrySearchTextField, quickGBC(0, 2, 2, 1));


        entryScrollPane = new JScrollPane();
        paletteDescriptions = new JList<>();
        paletteDescriptions.addListSelectionListener(new PaletteDescriptionSelectionListener());
        entryScrollPane.setViewportView(paletteDescriptions);
        leftPanel.add(entryScrollPane, quickGBC(0, 3, 2, 1));

        // -----------

        JPanel rightPanel = new JPanel();
        mainPanel.add(rightPanel);
        rightPanel.setLayout(new BorderLayout());

        JPanel scalePanel = new JPanel();
        rightPanel.add(scalePanel, BorderLayout.PAGE_START);
        scalePanel.setLayout(new GridBagLayout());

        JLabel scaleSliderLabel = new JLabel("Scale:");
        scalePanel.add(scaleSliderLabel, quickGBC(0, 0));
        JSlider scaleSlider = new JSlider(1, 8);
        scaleSlider.setMajorTickSpacing(1);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintTrack(false);
        scaleSlider.setValue(DEFAULT_SCALE);
        scaleSlider.addChangeListener(new ScaleListener());
        scaleSlider.updateUI();
        scalePanel.add(scaleSlider, quickGBC(1, 0));

        JPanel imagePanel = new JPanel();
        rightPanel.add(imagePanel, BorderLayout.CENTER);
        imagePanel.setLayout(new GridBagLayout());

        MouseHelperListener mouseHelperListener = new MouseHelperListener();
        ShowPaletteListener showPaletteListener = new ShowPaletteListener();

        JLabel originalImageLabel = new JLabel("Original:");
        imagePanel.add(originalImageLabel, quickGBC(0, 0));
        originalImage = new PaletteImageLabel(null);
        originalImage.addMouseListener(mouseHelperListener);
        originalImage.addMouseMotionListener(showPaletteListener);
        imagePanel.add(originalImage, quickGBC(0, 1));

        JLabel shinyImageLabel = new JLabel("Orig. shiny:");
        imagePanel.add(shinyImageLabel, quickGBC(1, 0));
        shinyImage = new PaletteImageLabel(null);
        shinyImage.addMouseListener(mouseHelperListener);
        shinyImage.addMouseMotionListener(showPaletteListener);
        imagePanel.add(shinyImage, quickGBC(1, 1));

        JLabel exampleImageLabel = new JLabel("Randomized example:");
        imagePanel.add(exampleImageLabel, quickGBC(2, 0));
        exampleImage = new PaletteImageLabel(null);
        exampleImage.addMouseListener(mouseHelperListener);
        exampleImage.addMouseMotionListener(showPaletteListener);
        imagePanel.add(exampleImage, quickGBC(2, 1));

        JButton newExampleButton = new JButton("New example");
        newExampleButton.addActionListener(event -> newRandomizedExample());
        imagePanel.add(newExampleButton, quickGBC(3, 1));

        JPanel descPanel = new JPanel();
        rightPanel.add(descPanel, BorderLayout.PAGE_END);
        descPanel.setLayout(new GridBagLayout());

        JLabel descNameLabel = new JLabel("Name:");
        descPanel.add(descNameLabel, quickGBC(0, 0));
        descNameField = new JTextField();
        descNameField.setPreferredSize(new Dimension(300, 20));
        descPanel.add(descNameField, quickGBC(1, 0));
        JCheckBox autoNameCB = new JCheckBox("Auto-name");
        autoNameCB.addItemListener(new AutoNameListener());
        descPanel.add(autoNameCB, quickGBC(2, 0));

        JLabel descBaseLabel = new JLabel("Palette desc.:");
        descPanel.add(descBaseLabel, quickGBC(0, 1));
        descBodyField = new JTextField();
        descBodyField.setPreferredSize(new Dimension(300, 20));
        descPanel.add(descBodyField, quickGBC(1, 1, 2, 1));

        JLabel descNoteLabel = new JLabel("Note:");
        descPanel.add(descNoteLabel, quickGBC(0, 2));
        descNoteField = new JTextField();
        descNoteField.setPreferredSize(new Dimension(300, 20));
        descPanel.add(descNoteField, quickGBC(1, 2, 2, 1));

        saveDescButton = new JButton("Save desc.");
        saveDescButton.addActionListener(e -> savePaletteDescription());
        descPanel.add(saveDescButton, quickGBC(1, 3));
        JButton resetDescButton = new JButton("Reset desc.");
        resetDescButton.addActionListener(e -> resetPaletteDescription());
        descPanel.add(resetDescButton, quickGBC(2, 3));
        JCheckBox autoSaveDescCB = new JCheckBox("Auto-save desc.");
        autoSaveDescCB.addItemListener(new AutoSaveListener());
        descPanel.add(autoSaveDescCB, quickGBC(1, 4));
    }

    private void loadBoth() {

        JFileChooser romChooser = new JFileChooser(new File(ROMS_FOLDER));
        romChooser.setFileFilter(new ROMFilter(3, 4, 5));
        if (romChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            openRom(romChooser.getSelectedFile());
        } catch (RomIOException e) {
            e.printStackTrace();
            return;
        }

        Gen3to5PaletteRandomizer paletteRandomizer = new Gen3to5PaletteRandomizer(romHandler, SETTINGS, RND);
        for (Species pk : romHandler.getSpeciesSetInclFormes()) {
            originalPalettes.put(pk, pk.getNormalPalette());
        }
        paletteDescriptions
                .setListData(paletteRandomizer.getPaletteDescriptions(FILE_KEY).toArray(new PaletteDescription[0]));
        paletteDescriptions.setSelectedIndex(0);

    }

    private void openRom(File file) {
        System.out.println("Loading " + file.getName() + "...");
        RomOpener.Results results = romOpener.openRomFile(file);
        if (results.wasOpeningSuccessful()) {
            romHandler = results.getRomHandler();
            this.setTitle(TITLE + " - " + romHandler.getROMName());
            romLoaded = true;
        } else {
            System.out.println("Could not load " + file.getName() + ". Reason: " + results.getFailType());
        }
    }

    private void savePaletteDescriptionsFile() {
        if (autoSave) {
            savePaletteDescription();
        }
        Gen3to5PaletteRandomizer paletteRandomizer = new Gen3to5PaletteRandomizer(romHandler, SETTINGS, RND);
        paletteRandomizer.savePaletteDescriptionSource(FILE_KEY, jlistToList(paletteDescriptions));
    }

    private List<PaletteDescription> jlistToList(JList<? extends PaletteDescription> jlist) {
        List<PaletteDescription> list = new ArrayList<PaletteDescription>();
        for (int i = 0; i < jlist.getModel().getSize(); i++) {
            list.add(jlist.getModel().getElementAt(i));
        }
        return list;
    }

    private void resetPaletteDescription() {
        if (!autoName) {
            descNameField.setText(unchangedName);
        }
        descBodyField.setText(unchangedBody);
        descNoteField.setText(unchangedNote);
    }

    private void savePaletteDescription() {
        savePaletteDescription(paletteDescriptions.getSelectedIndex());
        unchangedName = descNameField.getText();
        unchangedBody = descBodyField.getText();
        unchangedNote = descNoteField.getText();
    }

    private void savePaletteDescription(int index) {
        PaletteDescription paletteDescription = paletteDescriptions.getModel().getElementAt(index);
        paletteDescription.setName(descNameField.getText());
        paletteDescription.setBody(descBodyField.getText());
        paletteDescription.setNote(descNoteField.getText());
    }

    private void previousPaletteDescription() {
        int currentIndex = paletteDescriptions.getSelectedIndex();
        int newIndex = currentIndex == 0 ? paletteDescriptions.getModel().getSize() - 1 : currentIndex - 1;
        paletteDescriptions.setSelectedIndex(newIndex);
    }

    private void nextPaletteDescription() {
        int currentIndex = paletteDescriptions.getSelectedIndex();
        int newIndex = currentIndex == paletteDescriptions.getModel().getSize() - 1 ? 0 : currentIndex + 1;
        paletteDescriptions.setSelectedIndex(newIndex);
    }

    private void newRandomizedExample() {
        randomizePalette(getCurrentPokemon(), descBodyField.getText());
        exampleImage.setImage(getPokemonImage(getCurrentPokemon(), false));
    }

    private void randomizePalette(Species pk, String paletteDescriptionBody) {
        pk.setNormalPalette(new Palette(originalPalettes.get(pk)));
        Palette palette = pk.getNormalPalette();
        PalettePopulator pp = new PalettePopulator(RND);
        TypeBaseColorList typeBaseColorList = new TypeBaseColorList(pk, false, RND);
        PalettePartDescription[] palettePartDescriptions = PalettePartDescription.allFrom(paletteDescriptionBody);

        Gen3to5PaletteRandomizer paletteRandomizer = new Gen3to5PaletteRandomizer(romHandler, SETTINGS, RND);
        paletteRandomizer.populatePalette(palette, pp, typeBaseColorList, palettePartDescriptions);
    }

    private void updateImagesAndText() {
        if (romLoaded) {
            descNameField.setText(paletteDescriptions.getSelectedValue().getName());
            unchangedName = descNameField.getText();
            if (autoName) {
                autoNameDesc();
            }
            descBodyField.setText(paletteDescriptions.getSelectedValue().getBody());
            unchangedBody = descBodyField.getText();
            descNoteField.setText(paletteDescriptions.getSelectedValue().getNote());
            unchangedNote = descNoteField.getText();

            Species pk = getCurrentPokemon();
            pk.setNormalPalette(new Palette(originalPalettes.get(pk)));
            originalImage.setImage(getPokemonImage(pk, false));
            shinyImage.setImage(getPokemonImage(pk, true));
            newRandomizedExample();
        }
    }

    private Species getCurrentPokemon() {
        return romHandler.getSpeciesInclFormes().get(paletteDescriptions.getSelectedIndex() + 1);
    }

    private BufferedImage getPokemonImage(Species pk, boolean shiny) {
        PokemonImageGetter pig = romHandler.createPokemonImageGetter(pk);
        pig.setShiny(shiny);
        BufferedImage front = pig.get();
        BufferedImage back = pig.setBack(true).get();
        return GFXFunctions.stitchToGrid(new BufferedImage[][]{{front, back}});
    }

    private void autoNameDesc() {
        String autoName = getCurrentPokemon().getFullName();
        descNameField.setText(autoName);
    }

    private class PaletteDescriptionSearchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String snippet = ((JTextField) e.getSource()).getText();
            int found = lastIndex;
            for (int i = 0; i < paletteDescriptions.getModel().getSize(); i++) {
                String palDescString = paletteDescriptions.getModel().getElementAt(i).getName();
                if (palDescString.toLowerCase().startsWith(snippet.toLowerCase())) {
                    found = i;
                    break;
                }
            }
            if (found != lastIndex) {
                paletteDescriptions.setSelectedIndex(found);
            }
        }
    }

    private class PaletteDescriptionSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (autoSave) {
                savePaletteDescription(lastIndex);
            }
            updateImagesAndText();
            @SuppressWarnings("unchecked")
            JList<PaletteDescription> source = (JList<PaletteDescription>) e.getSource();
            lastIndex = source.getSelectedIndex();
        }

    }

    private class ScaleListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider) e.getSource();
            int scale = source.getValue();
            originalImage.setScale(scale);
            shinyImage.setScale(scale);
            exampleImage.setScale(scale);
        }

    }

    private class MouseHelperListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        private void addIndexAtCoordToDescBody(MouseEvent e) {
            PaletteImageLabel source = (PaletteImageLabel) e.getSource();
            int paletteIndex = source.getPaletteIndexAtCoord(e.getX(), e.getY());

            String oldText = descBodyField.getText();
            StringBuilder sb = new StringBuilder(oldText);
            if (!oldText.isEmpty() && !oldText.endsWith("/")) {
                sb.append(",");
            }
            sb.append(paletteIndex);

            descBodyField.setText(sb.toString());
        }

        private void addSlashToDescBody() {
            descBodyField.setText(descBodyField.getText() + "/");
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                addIndexAtCoordToDescBody(e);
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                addSlashToDescBody();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private static class ShowPaletteListener implements MouseMotionListener {

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            PaletteImageLabel source = (PaletteImageLabel) e.getSource();
            int paletteIndex = source.getPaletteIndexAtCoord(e.getX(), e.getY());
            source.setToolTipText("" + paletteIndex);
        }

    }

    private class AutoNameListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            autoName = e.getStateChange() == ItemEvent.SELECTED;
            descNameField.setEnabled(!autoName);
            autoNameDesc();
        }

    }

    private class AutoSaveListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            autoSave = e.getStateChange() == ItemEvent.SELECTED;
            saveDescButton.setEnabled(!autoSave);
        }

    }

}
