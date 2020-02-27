package com.wybitul.examplanner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Matrix<T> {
    List<List<T>> columns = new ArrayList<>();

    public void addColumn(Collection<T> col) {
        columns.add(List.copyOf(col));
    }

    public void addColumn(int index, Collection<T> col) {
        columns.add(index, List.copyOf(col));
    }

    public void addRow(Collection<T> row) {
        List<T> rowList = List.copyOf(row);
        IntStream.range(0, row.size()).forEach(i -> columns.get(i).add(rowList.get(i)));
    }

    public void addRow(int index, Collection<T> row) {
        List<T> rowList = List.copyOf(row);
        IntStream.range(0, row.size()).forEach(i -> columns.get(i).add(index, rowList.get(i)));
    }

    // ADAM Jde to nějak bez passování Class<T>?
    public T[][] toArray(Class<T> tClass) {
        int cols = columns.size();
        int rows = cols > 0 ? columns.get(0).size() : 0;

        @SuppressWarnings("unchecked")
        T[][] result = (T[][]) Array.newInstance(tClass, cols, rows);
        IntStream.range(0, rows).forEach(i ->
            IntStream.range(0, cols).forEach(j -> result[i][j] = columns.get(j).get(i))
        );
        return result;
    }

    public int[][] toIntArray() {
        int cols = columns.size();
        int rows = cols > 0 ? columns.get(0).size() : 0;

        @SuppressWarnings("unchecked")
        int[][] result = new int[cols][rows];
        IntStream.range(0, rows).forEach(i ->
                IntStream.range(0, cols).forEach(j -> result[i][j] = (int) columns.get(j).get(i))
        );
        return result;
    }

    public List<List<T>> getColumns() {
        return columns;
    }

    public List<List<T>> getRows() {
        int cols = columns.size();
        int rows = cols > 0 ? columns.get(0).size() : 0;

        return IntStream.range(0, rows).mapToObj(i ->
                IntStream.range(0, cols).mapToObj(j -> columns.get(j).get(i)).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }
}
