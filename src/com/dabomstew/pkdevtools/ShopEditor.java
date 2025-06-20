package com.dabomstew.pkdevtools;

import com.dabomstew.pkromio.gamedata.Item;
import com.dabomstew.pkromio.gamedata.Shop;
import com.dabomstew.pkromio.romhandlers.RomHandler;
import com.dabomstew.pkromio.romio.ROMFilter;
import com.dabomstew.pkromio.romio.RomOpener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
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

    private File romFile;
    private RomHandler romHandler;
    private List<Shop> shops;
    private List<Integer> prices;

    private boolean hasAddedItems;

    private final JFrame frame;
    private final JComboBox<String> shopComboBox;
    private final JTable shopTable;
    private final JComboBox<Item> itemsCombobox = new JComboBox<>();
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
                int index = shopComboBox.getSelectedIndex();
                updateTable(shops.get(index));
            }
        });
        shopComboBox.setEnabled(false);

        shopTable = new JTable(new DefaultTableModel(
                new Object[][]{}, new String[]{"Item", "Price"}
        ));
        shopTable.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
                int itemIndex = e.getFirstRow();
                Item newItem = (Item) shopTable.getModel().getValueAt(itemIndex, 0);
                Shop currShop = shops.get(shopComboBox.getSelectedIndex());
                currShop.getItems().set(itemIndex, newItem);
                System.out.println(currShop);
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(shopTable);

        removeItemButton = new JButton("-- Remove Item");
        removeItemButton.addActionListener(e -> removeItem());
        removeItemButton.setEnabled(false);
        addItemButton = new JButton("++ Add Item");
        addItemButton.addActionListener(e -> addItem());
        addItemButton.setEnabled(false);

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

        File tryFile = romChooser.getSelectedFile();
        loadROM(tryFile, false);
    }

    private void loadROM(File tryFile, boolean reload) {
        try {
            RomOpener.Results results = romOpener.openRomFile(tryFile);
            if (results.wasOpeningSuccessful()) {
                if (results.getRomHandler().hasShopSupport()) {
                    romFile = tryFile;
                    romHandler = results.getRomHandler();
                    if (!reload) {
                        romOpened();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                            tryFile.getName() + " could be opened, but does not have shop editing support.",
                            "Invalid ROM", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        tryFile.getName() + " could not be opened.\nReason: " + results.getFailType(),
                        "Could not open ROM", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    tryFile.getName() + " could not be opened.\nAn exception was thrown: " + e,
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
        itemsCombobox.removeAllItems();
        for (Item item : romHandler.getItems()) {
            if (item == null) continue;
            itemsCombobox.addItem(item);
        }
        shopComboBox.setSelectedIndex(0);
        shopComboBox.setEnabled(true);
        removeItemButton.setEnabled(romHandler.canChangeShopSizes());
        addItemButton.setEnabled(romHandler.canChangeShopSizes());
    }

    private void saveROM() {
        try {
            romHandler.setShops(shops);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "The shops could not be written to ROM.\n " +
                            "This is probably because too many items were added,\n" +
                            "causing the ROM to run out of space. Try removing some\n" +
                            "items, before attempting to save again.\n" +
                            "If you did not add any items, please file a bug report.\n\n" +
                            "The ROM will now be reloaded (this may take a few seconds).",
                    "Could not write shops.", JOptionPane.ERROR_MESSAGE);
            loadROM(romFile, true);
        }
        // TODO
        System.out.println("saving ROM (not yet implemented)");
    }

    private void updateTable(Shop shop) {
        DefaultTableModel model = (DefaultTableModel) shopTable.getModel();
        model.setRowCount(0);
        for (Item item : shop.getItems()) {
            model.insertRow(model.getRowCount(), new Object[]{item, prices.get(item.getId())});
        }
        shopTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(itemsCombobox));
    }

    private void removeItem() {
        Shop shop = shops.get(shopComboBox.getSelectedIndex());
        List<Item> shopItems = shop.getItems();
        if (shopItems.size() == 1) {
            JOptionPane.showMessageDialog(frame,
                    "Item could not be removed, as it is the last item in the shop.",
                    "Can not remove item", JOptionPane.WARNING_MESSAGE);
        } else {
            shopItems.remove(shopItems.size() - 1);
            updateTable(shop);
        }
    }

    private void addItem() {
        if (!hasAddedItems) {
            int answer = JOptionPane.showConfirmDialog(frame,
                            "All ROMs have limited space for storing shop items.\n" +
                                    "This program was made to support adding one (1) item\n" +
                                    "to every shop in the game; adding any more is done at own risk.\n" +
                                    "If too many items are added, the ROM will not be able to be saved.\n\n" +
                                    "Will you add an item? (this dialog will only be shown once)",
                                "Will you add an item?", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.NO_OPTION) {
                return;
            }
        }
        Item defaultItem = romHandler.getItems().get(1);

        Shop currShop = shops.get(shopComboBox.getSelectedIndex());
        currShop.getItems().add(defaultItem);

        DefaultTableModel tableModel = (DefaultTableModel) shopTable.getModel();
        tableModel.insertRow(tableModel.getRowCount(), new Object[] {defaultItem, prices.get(defaultItem.getId())});

        hasAddedItems = true;
    }
}
