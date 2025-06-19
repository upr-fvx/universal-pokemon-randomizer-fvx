package com.dabomstew.pkdevtools;

import com.dabomstew.pkromio.gamedata.Shop;
import com.dabomstew.pkromio.romhandlers.RomHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ShopEditor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShopEditor::new);
    }

    private RomHandler romHandler;
    private List<Shop> shops;
    private List<Integer> itemPrices;

    private ShopEditor() {
        JFrame frame = new JFrame("Shop Editor");
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
        openRomItem.addActionListener(e -> saveROM());
        fileMenu.add(openRomItem);
        fileMenu.add(saveRomItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // Rest of the UI
        JComboBox<String> shopComboBox = new JComboBox<>(new String[]{"No Shops Loaded - Open a ROM file!"});
        shopComboBox.addItemListener(e -> updateTable(shops.get(e.getID())));
        shopComboBox.setEnabled(false);

        JTable shopTable = new JTable(new DefaultTableModel(
                new Object[][]{}, new String[]{"Item", "Price"}
        ));
        JScrollPane tableScrollPane = new JScrollPane(shopTable);
        // TODO: editing the entries in the table
        shopTable.setEnabled(false);

        JButton removeItemButton = new JButton("-- Remove Item");
        removeItemButton.addActionListener(e -> removeItem());
        removeItemButton.setEnabled(false);
        JButton addItemButton = new JButton("++ Add Item");
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
        // TODO
        System.out.println("opening ROM (not yet implemented)");
    }

    private void saveROM() {
        // TODO
        System.out.println("saving ROM (not yet implemented)");
    }

    private void updateTable(Shop shop) {
        // TODO
    }

    private void removeItem() {
        // TODO
        System.out.println("removing item (not yet implemented)");
    }

    private void addItem() {
        // TODO
        System.out.println("adding item (not yet implemented)");
    }
}
