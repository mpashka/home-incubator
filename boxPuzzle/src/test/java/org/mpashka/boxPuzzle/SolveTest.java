package org.mpashka.boxPuzzle;

import org.junit.Test;
import org.mpashka.boxPuzzle.core.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class SolveTest {

    private static final Logger log = LoggerFactory.getLogger(SolveTest.class);

    @Test
    public void iterationTest0() {
        Matrix figure = new Matrix("f1", new int[][]{
                {0, 2,},
                {0, 2,},
                {0, 2,},
        });
        Solve.Iteration iteration = new Solve.Iteration(0, new Matrix[]{
                figure
        });

        assertTrue(iteration.inc());
        assertSame(figure, iteration.getFigure());
        assertArrayEquals(new int[]{0, 0, 0}, iteration.getPositions());
        assertFalse(iteration.inc());
    }

    @Test
    public void iterationTest() {
        Matrix figure0 = new Matrix("f0", new int[][]{
                {0, 0,},
                {0, 1,},
                {0, 2,},
        });
        Matrix figure1 = new Matrix("f1", new int[][]{
                {0, 2,},
                {0, 2,},
                {0, 2,},
        });
        Solve.Iteration iteration = new Solve.Iteration(0, new Matrix[]{
                figure0, figure1
        });

        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{0, 0, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{0, 1, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{1, 0, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{1, 1, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{2, 0, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure0, iteration.getFigure());
        assertArrayEquals(new int[]{2, 1, 0}, iteration.getPositions());
        assertTrue(iteration.inc());
        assertSame(figure1, iteration.getFigure());
        assertArrayEquals(new int[]{0, 0, 0}, iteration.getPositions());
        assertFalse(iteration.inc());
    }

    @Test
    public void test1() {
        Matrix[] figure = new Figures().getAllFigures()[0];
        Matrix figureVariant = figure[2];
        log.info("Figure {}", figureVariant);
    }
}
