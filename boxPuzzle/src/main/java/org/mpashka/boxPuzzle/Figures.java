package org.mpashka.boxPuzzle;

import org.mpashka.boxPuzzle.core.Matrix;
import org.mpashka.boxPuzzle.core.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Figures {

    private static final Logger log = LoggerFactory.getLogger(Figures.class);


    static final int SIZE = 3;

    public final Matrix g1 = new Matrix("g1", new int[][]{
            {0, 0, 0, 1, },
            {1, 1, 0, 0, },
            {0, 1, 1, 1, },
    });

    private final Matrix g2 = new Matrix("g2", new int[][]{
            {1, 0, 0, 0, },
            {1, 1, 1, 0, },
            {0, 0, 1, 1, },
    });

    private final Matrix l1 = new Matrix("l1", new int[][]{
            {0, 0, 1,},
            {1, 0, 0,},
            {0, 0, 0,},
    });

    private final Matrix l2 = new Matrix("l2", new int[][]{
            {1, 1, 1, 0, },
            {2, 1, 0, 0, },
            {0, 0, 0, 0, },
    });

    private final Matrix octopus = new Matrix("o", new int[][]{
            {0, 0, 1, 0, },
            {0, 1, 0, 0, },
            {0, 0, 0, 1, },
    });

    private final Matrix t = new Matrix("t", new int[][]{
            {0, 1, 1, 2, },
            {0, 0, 1, 0, },
            {0, 0, 0, 0, },
    });

    private final Matrix z = new Matrix("z", new int[][]{
            {0, 0, 1, 1, },
            {0, 1, 1, 2, },
            {0, 0, 0, 0, },
    });

    public final Matrix[] all = {
            g1, g2, l1, l2, octopus, t, z
    };

    private Matrix[][] allFigures;

    public Figures() {
        createAllFigurePositions();
    }

    private void createAllFigurePositions() {
        allFigures = new Matrix[all.length][];
        for (int i = 0; i < allFigures.length; i++) {
            Matrix figure = all[i];
            Matrix[] figurePositions = new Matrix[6];
            figurePositions[0] = figure.normalize();
            figurePositions[1] = figure.multiply(Utils.rotate).normalize();
            figurePositions[2] = figurePositions[1].multiply(Utils.rotate).normalize();
            figurePositions[3] = figure.multiply(Utils.reflect).normalize();
            figurePositions[4] = figurePositions[3].multiply(Utils.rotate).normalize();
            figurePositions[5] = figurePositions[4].multiply(Utils.rotate).normalize();

            figurePositions = removeSame(figurePositions);
            allFigures[i] = figurePositions;
        }

    }

    private Matrix[] removeSame(Matrix[] figurePositions) {
        boolean[] same = new boolean[6];
        int sameCount = 0;
        for (int i = 0; i < 6; i++) {
            if (same[i]) continue;
            Matrix m1 = figurePositions[i];
            int[] v1 = matrixToVec(m1);
            for (int j = i+1; j < 6; j++) {
                Matrix m2 = figurePositions[j];
                int[] v2 = matrixToVec(m2);
                if (Arrays.equals(v1, v2)) {
                    same[j] = true;
                    sameCount++;
                }
            }
        }

        if (sameCount == 0) {
            return figurePositions;
        }

        log.info("Same figures found: {}, {}", figurePositions[0].getName(), sameCount);
        Matrix[] result = new Matrix[figurePositions.length - sameCount];
        int resultIndex = 0;
        for (int i = 0; i < 6; i++) {
            if (same[i]) continue;
            result[resultIndex++] = figurePositions[i];
        }
        return result;
    }

    private int[] matrixToVec(Matrix m) {
        int[] columns = new int[m.columns()];
        for (int column = 0; column < columns.length; column++) {
            // value = x + y * 3 + z * 3^2
            int value = 0;
            int multiplier = 1;
            for (int row = 0; row < m.rows(); row++) {
                value += m.get(column, row) * multiplier;
                multiplier *= SIZE;
            }
            columns[column] = value;
        }
        Arrays.sort(columns);
        return columns;
    }

    public Matrix[][] getAllFigures() {
        return allFigures;
    }


}
