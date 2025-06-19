package com.dabomstew.pkdevtools;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ShopEditor {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Shop Editor");
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null); // Center window

            // Menu bar
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem openRomItem = new JMenuItem("Open ROM (ctrl-O)");
            JMenuItem saveRomItem = new JMenuItem("Save ROM (ctrl-S)");
            fileMenu.add(openRomItem);
            fileMenu.add(saveRomItem);
            menuBar.add(fileMenu);
            frame.setJMenuBar(menuBar);

            // Rest of the UI
            JComboBox<String> shopComboBox = new JComboBox<>(new String[]{"Select Shop"});
            JTable shopTable = new JTable(new DefaultTableModel(
                    new Object[][]{}, new String[]{"Item", "Price"}
            ));
            JScrollPane tableScrollPane = new JScrollPane(shopTable);
            JButton removeItemButton = new JButton("-- Remove Item");
            JButton addItemButton = new JButton("++ Add Item");
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.add(removeItemButton);
            buttonPanel.add(addItemButton);

            frame.add(shopComboBox, BorderLayout.NORTH);
            frame.add(tableScrollPane, BorderLayout.CENTER);
            frame.add(buttonPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        });
    }
}
