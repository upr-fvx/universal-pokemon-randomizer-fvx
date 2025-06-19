package com.dabomstew.pkdevtools;

import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.Shop;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.ROMFilter;
import com.dabomstew.pkromio.romio.RomOpener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.List;

public class ShopEditor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShopEditor::new);
    }

    private final RomOpener romOpener = new RomOpener();

    private RomHandler romHandler;
    private List<Shop> shops;
    private List<Integer> prices;

    private final JFrame frame;
    private final JComboBox<String> shopComboBox;
    private final JTable shopTable;
    private final JButton removeItemButton;
    private final JButton addItemButton;

    private ShopEditor() {
        frame = new JFrame("Universal Pokémon Shop Editor");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center window

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openRomItem = new JMenuItem("Open ROM");
        openRomItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openRomItem.addActionListener(e -> openROM());
        JMenuItem saveRomItem = new JMenuItem("Save ROM");
        saveRomItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveRomItem.addActionListener(e -> saveROM());
        fileMenu.add(openRomItem);
        fileMenu.add(saveRomItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // Rest of the UI
        shopComboBox = new JComboBox<>(new String[]{"No Shops Loaded - Open a ROM file!"});
        shopComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int index = ((JComboBox<String>) e.getSource()).getSelectedIndex();
                updateTable(shops.get(index));
            }
        });
        shopComboBox.setEnabled(false);

        shopTable = new JTable(new DefaultTableModel(
                new Object[][]{}, new String[]{"Item", "Price"}
        ));
        JScrollPane tableScrollPane = new JScrollPane(shopTable);
        // TODO: editing the entries in the table

        removeItemButton = new JButton("-- Remove Item");
        removeItemButton.addActionListener(e -> removeItem());
        removeItemButton.setEnabled(false);
        addItemButton = new JButton("++ Add Item");
        addItemButton.addActionListener(e -> addItem());
        addItemButton.setEnabled(false);
An exception was thrown
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(removeItemButton);
        buttonPanel.add(addItemButton);

        frame.add(shopComboBox, BorderLayout.NORTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void openROM() {
        JFileChooser romChooser = new JFileChooser();
        romChooser.setFileFilter(new ROMFilter());
        if (romChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File romFile = romChooser.getSelectedFile();
        try {
            RomOpener.Results results = romOpener.openRomFile(romFile);
            if (results.wasOpeningSuccessful()) {
                if (results.getRomHandler().hasShopSupport()) {
                    romHandler = results.getRomHandler();
                    romOpened();
                } else {
                    JOptionPane.showMessageDialog(frame,
                            romFile.getName() + " could be opened, but does not have shop editing support.",
                            "Invalid ROM", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        romFile.getName() + " could not be opened. Reason: " + results.getFailType(),
                        "Could not open ROM", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    romFile.getName() + " could not be opened. An exception was thrown: " + e,
                    "Could not open ROM", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void romOpened() {
        shops = romHandler.getShops();
        prices = romHandler.getShopPrices();
        shopComboBox.removeAllItems();
        for (Shop shop : shops) {
            shopComboBox.addItem(shop.getName());
        }
        shopComboBox.setSelectedIndex(0);
        shopComboBox.setEnabled(true);
        removeItemButton.setEnabled(true);
        addItemButton.setEnabled(true);
    }

    private void saveROM() {
        // TODO
        System.out.println("saving ROM (not yet implemented)");
    }

    private void updateTable(Shop shop) {
        DefaultTableModel model = (DefaultTableModel) shopTable.getModel();
        model.setRowCount(0);
        for (Item item : shop.getItems()) {
            model.insertRow(0, new Object[]{item, prices.get(item.getId())});
        }
    }

    private void removeItem() {
        Shop shop = shops.get(shopComboBox.getSelectedIndex());
        List<Item> shopItems = shop.getItems();
        shopItems.remove(shopItems.size() - 1);
        updateTable(shop);
    }

    private void addItem() {
        // TODO
        System.out.println("adding item (not yet implemented)");
    }
}
