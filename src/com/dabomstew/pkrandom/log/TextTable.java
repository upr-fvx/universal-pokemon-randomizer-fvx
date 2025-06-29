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

    public enum Alignment {
        LEFT, RIGHT
    }

    private static class Cell {
        final String s;
        Alignment a;

        Cell(String s, Alignment a) {
            this.s = s;
            this.a = a;
        }
    }

    private static final Cell EMPTY_CELL = new Cell("", Alignment.LEFT);
    private static final Cell NULL_CELL = new Cell("", Alignment.LEFT);

    private static final String COLUMN_SEPARATOR = "|";

    private final int columns;
    private final List<List<Cell>> data = new ArrayList<>();
    private final List<Alignment> colAlignments;

    public TextTable(int columns) {
        this.columns = columns;
        colAlignments = new ArrayList<>(columns);
        for (int col = 0; col < columns; col++) {
            colAlignments.add(Alignment.LEFT);
        }
    }

    public void addRow(List<String> row) {
        if (row.size() != columns) {
            throw new IllegalArgumentException("invalid row.size(); must be " + columns + ", was " + row.size());
        }
        evenOut();
        List<Cell> cellRow = new ArrayList<>(columns);
        for (int col = 0; col < columns; col++) {
            cellRow.add(new Cell(row.get(col), colAlignments.get(col)));
        }
        data.add(cellRow);
    }

    public void addRow(String... row) {
        addRow(Arrays.asList(row));
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
        data.get(row).set(col, new Cell(cell, colAlignments.get(col)));
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

    /**
     * Sets the alignment of all future cells to be added to columns cols.
     */
    public void setColumnAlignments(Alignment a, int... cols) {
        for (int col : cols) {
            colAlignments.set(col, a);
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
            for (int col = 0; col < columns; col++) {
                Cell c = row.get(col);
                String fString = "%" + (c.a == Alignment.LEFT ? "-" : "") + colLengths[col] + "s";
                sb.append(String.format(fString, row.get(col).s));
                if (col != columns - 1) {
                    sb.append(COLUMN_SEPARATOR);
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
