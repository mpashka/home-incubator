package org.mpashka.boxPuzzle.core;

import java.util.Arrays;

public class Matrix implements IMatrix {
    private int[] size;
    private String name;
    private int[][] matrix;

    public Matrix(String name, int columns, int rows) {
        this.name = name;
        matrix = new int[rows][columns];
        normalize();
    }

    public Matrix(String name, int[][] matrix) {
        this.name = name;
        this.matrix = matrix;
    }

    public String getName() {
        return name;
    }

    public Matrix setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public int columns() {
        return matrix[0].length;
    }

    @Override
    public int rows() {
        return matrix.length;
    }

    @Override
    public int get(int column, int row) {
        return matrix[row][column];
    }

    @Override
    public void set(int column, int row, int value) {
        matrix[row][column] = value;
    }

    public int[] getSize() {
        return size;
    }

    public int getSize(int row) {
        return size[row];
    }

    /**
     * Set min values to 0
     */
    public Matrix normalize() {
        this.size = new int[rows()];
        for (int row = 0; row < rows(); row++) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int value : matrix[row]) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            if (min != 0) {
                for (int column = 0; column < columns(); column++) {
                    matrix[row][column] -= min;
                }
            }
            this.size[row] = max - min;
        }
        return this;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Matrix{ ");
        sb.append(name);
        sb.append('\n');
        for (int[] row : matrix) {
            sb.append(Arrays.toString(row));
            sb.append("\n");
        }
        sb.append('}');
        return sb.toString();
    }
}
