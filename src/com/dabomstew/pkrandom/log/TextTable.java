package com.dabomstew.pkrandom.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a table made out of text characters.<br>
 * To print it, use its {@link #toString()} method.
 */
public class TextTable {

    private static class Cell {
        final String s;
        Cell(String s) {
            this.s = s;
        }
    }
    private static final Cell EMPTY_CELL = new Cell("");
    private static final Cell NULL_CELL = new Cell("");

    private static final String COLUMN_SEPARATOR = "|";

    private final int columns;
    private final List<List<Cell>> data = new ArrayList<>();

    public TextTable(int columns) {
        this.columns = columns;
    }

    public void addRow(List<String> row) {
        if (row.size() != columns) {
            throw new IllegalArgumentException("invalid row.size(); must be " + columns + ", was " + row.size());
        }
        evenOut();
        data.add(row.stream().map(Cell::new).collect(Collectors.toList()));
    }

    /**
     * Adds a cell to the bottom of column col.
     */
    public void addCell(int col, String cell) {
        int row = 0;
        while (row < data.size() && data.get(row).get(col) != NULL_CELL) {
            row++;
        }
        if (row == data.size()) {
            Cell[] arr = new Cell[columns];
            Arrays.fill(arr, NULL_CELL);
            data.add(Arrays.stream(arr).collect(Collectors.toList()));
        }
        data.get(row).set(col, new Cell(cell));
    }

    /**
     * Evens out all columns. To be used in conjuction with {@link #addCell(int, String)}.
     */
    public void evenOut() {
        for (List<Cell> row : data) {
            for (int col = 0; col < columns; col++) {
                if (row.get(col) == NULL_CELL) {
                    row.set(col, EMPTY_CELL);
                }
            }
        }
    }

    @Override
    public String toString() {
        int[] colLengths = new int[columns];
        for (int col = 0; col < columns; col++) {
            int max = 0;
            for (List<Cell> row : data) {
                max = Math.max(max, row.get(col).s.length());
            }
            colLengths[col] = max;
        }

        StringBuilder sb = new StringBuilder();
        for (List<Cell> row : data) {
            System.out.println(row);
            for (int col = 0; col < columns; col++) {
                sb.append(String.format("%-" + colLengths[col] + "s", row.get(col).s));
                if (col != columns - 1) {
                    sb.append(COLUMN_SEPARATOR);
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
