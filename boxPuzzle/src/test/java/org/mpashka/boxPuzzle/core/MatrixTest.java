package org.mpashka.boxPuzzle.core;

import org.junit.Assert;
import org.junit.Test;
import org.mpashka.boxPuzzle.Figures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mpashka.boxPuzzle.core.Utils.reflect;
import static org.mpashka.boxPuzzle.core.Utils.rotate;


public class MatrixTest {

    private static final Logger log = LoggerFactory.getLogger(MatrixTest.class);

    @Test
    public void testMultiply() {

        Figures figures = new Figures();
        Matrix g1 = figures.g1;

        log.debug("Rotate: {}", rotate);
        log.debug("Reflect: {}", reflect);

        Matrix gr = g1.multiply(rotate).normalize();

        Matrix gr0 = new Matrix("gr0", new int[][]{
                {0, 1, 1, 1, },
                {0, 0, 0, 1, },
                {1, 1, 0, 0, },
        });

        assertEquals(gr, gr0);

        // todo check
        g1.multiply(rotate).multiply(rotate).normalize();

        Matrix gr3 = g1.multiply(rotate).multiply(rotate).multiply(rotate).normalize();
        Assert.assertEquals(g1.toString(), gr3.setName(g1.getName()).toString());


        Matrix gref = g1.multiply(reflect).normalize();
        Matrix grefExpected = new Matrix(gref.getName(), new int[][]{
                {1, 0, 0, 0, },
                {0, 0, 1, 1, },
                {1, 1, 1, 0, },
        });

        Assert.assertEquals(grefExpected.toString(), gref.toString());

    }

    private void assertEquals(Matrix m1, Matrix m2) {
        Assert.assertEquals(m1.columns(), m2.columns());
        Assert.assertEquals(m1.rows(), m2.rows());
        for (int i = 0; i < m1.rows(); i++) {
            Assert.assertArrayEquals("Row " + i, m1.getMatrix()[i], m2.getMatrix()[i]);
        }
    }

}
