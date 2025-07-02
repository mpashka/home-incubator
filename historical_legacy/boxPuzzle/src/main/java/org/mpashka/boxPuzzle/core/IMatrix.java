package org.mpashka.boxPuzzle.core;

public interface IMatrix {
    int columns();
    int rows();
    int get(int column, int row);
    void set(int column, int row, int value);
    IMatrix normalize();
    default Matrix multiply(Matrix m) {
        return Utils.multMatrix((Matrix) this, m);
    }
}
