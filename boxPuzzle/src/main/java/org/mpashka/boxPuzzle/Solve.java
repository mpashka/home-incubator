package org.mpashka.boxPuzzle;

import org.mpashka.boxPuzzle.core.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Solve {

    private static final Logger log = LoggerFactory.getLogger(Solve.class);


    private static final int DIMENSIONS = 3;

    private boolean[][][] busy = new boolean[Figures.SIZE][Figures.SIZE][Figures.SIZE];
    private int iterations, failIterations, solutions;

    private Figures figures = new Figures();

    private void solve() {
        Iteration[] solution = new Iteration[figures.getAllFigures().length];
        try {
            checkFigures(solution, 0);
            log.info("Total iterations: {} / {} / {}", iterations, failIterations, solutions);
        } catch (Exception e) {
            log.error("Error solving", e);
            for (Iteration iteration : solution) {
                if (iteration != null) {
                    log.info("Figure {}, variant {}, position {}", iteration.getFigureIndex(), iteration.getVariant(), Arrays.toString(iteration.getPositions()));
                }
            }
        }
    }

    private void checkFigures(Iteration[] solution, int figureIndex) {
        Matrix[] figure = figures.getAllFigures()[figureIndex];
        Iteration iteration = new Iteration(figureIndex, figure);
        solution[figureIndex] = iteration;

        while (iteration.inc()) {
            if (fits(iteration)) {
                this.iterations++;
                if (figureIndex == figures.getAllFigures().length - 1) {
                    logSolutionFound(solution);
                } else {
                    put(iteration, true);
                    checkFigures(solution, figureIndex + 1);
                    put(iteration, false);
                }
            } else {
                this.failIterations++;
            }
        }
    }

    private void logSolutionFound(Iteration[] solution) {
        solutions++;
        log.info("Solution found");
        for (Iteration iteration : solution) {
            log.info("Figure {}, variant {} ({}), position {}", iteration.getFigureIndex(), iteration.getVariant()
                    , iteration.getFigure().getName(), Arrays.toString(iteration.getPositions()));
        }
    }

    private boolean fits(Iteration iteration) {
        Matrix figure = iteration.getFigure();
        int[] positions = iteration.getPositions();
        for (int i = 0; i < figure.columns(); i++) {
            int x = figure.get(i, 0) + positions[0];
            int y = figure.get(i, 1) + positions[1];
            int z = figure.get(i, 2) + positions[2];
            if (busy[x][y][z]) return false;
/*
            try {
            } catch (RuntimeException e) {
                log.error("Error. X {}, Y {}, Z {}. Iteration {}", x, y, z, iteration);
                throw e;
            }
*/
        }
        return true;
    }

    private void put(Iteration iteration, boolean value) {
        Matrix figure = iteration.getFigure();
        int[] positions = iteration.getPositions();
        for (int i = 0; i < figure.columns(); i++) {
            int x = figure.get(i, 0) + positions[0];
            int y = figure.get(i, 1) + positions[1];
            int z = figure.get(i, 2) + positions[2];
            busy[x][y][z] = value;
        }
    }

    public static void main(String[] args) {
        new Solve().solve();
    }

    public static class Iteration {
        private int figureIndex;
        private Matrix[] figure;
        private int variant;
        private int[] positions;

        public Iteration(int figureIndex, Matrix[] figure) {
            this.figureIndex = figureIndex;
            this.figure = figure;
            positions = new int[DIMENSIONS];
            positions[DIMENSIONS - 1] = -1;
            this.variant = 0;
        }

        public boolean inc() {
            while (!incPosition(DIMENSIONS - 1)) {
                variant++;
                positions[DIMENSIONS - 1] = -1;
                if (variant >= figure.length) {
                    return false;
                }
            }
            return true;
        }

        public Matrix getFigure() {
            return figure[variant];
        }

        public int[] getPositions() {
            return positions;
        }

        private boolean incPosition(int position) {
            this.positions[position]++;
            Matrix matrix = figure[variant];
            if (this.positions[position] >= Figures.SIZE - matrix.getSize(position)) {
                this.positions[position] = 0;
                if (position == 0) return false;
                return incPosition(position-1);
            }
            return true;
        }

        public int getFigureIndex() {
            return figureIndex;
        }

        public int getVariant() {
            return variant;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Iteration{");
            sb.append("figure=").append(figureIndex);
            sb.append(", variant=").append(variant);
            sb.append(", positions=").append(Arrays.toString(positions));
            sb.append('}');
            return sb.toString();
        }
    }
}
