package org.mpashka.boxPuzzle;

import org.mpashka.boxPuzzle.core.Matrix;
import org.mpashka.boxPuzzle.core.Utils;

public class Figures {

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
            allFigures[i] = figurePositions;
        }
    }

    public Matrix[][] getAllFigures() {
        return allFigures;
    }


}
