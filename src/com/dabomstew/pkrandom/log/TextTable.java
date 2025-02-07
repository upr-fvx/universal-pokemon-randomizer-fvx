package com.dabomstew.pkrandom.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a table made out of text characters.<br>
 * To print it, use its {@link #toString()} method.
 */
public class TextTable {

    private static final String COLUMN_SEPARATOR = "|";

    private final int columns;
    private final List<List<String>> data = new ArrayList<>();

    public TextTable(int columns) {
        this.columns = columns;
    }

    public void addRow(List<String> row) {
        if (row.size() != columns) {
            throw new IllegalArgumentException("invalid row.size(); must be " + columns + ", was " + row.size());
        }
        data.add(row);
    }

    @Override
    public String toString() {
        int[] colLengths = new int[columns];
        for (int col = 0; col < columns; col++) {
            int max = 0;
            for (List<String> row : data) {
                max = Math.max(max, row.get(col).length());
            }
            colLengths[col] = max;
        }

        StringBuilder sb = new StringBuilder();
        for (List<String> row : data) {
            System.out.println(row);
            for (int col = 0; col < columns; col++) {
                String cell = row.get(col);
                System.out.println(col + "\t" + cell);
                sb.append(String.format("%-" + colLengths[col] + "s", cell));
                if (col != columns - 1) {
                    sb.append(COLUMN_SEPARATOR);
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
