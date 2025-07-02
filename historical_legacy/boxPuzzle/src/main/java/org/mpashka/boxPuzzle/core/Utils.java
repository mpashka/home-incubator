package org.mpashka.boxPuzzle.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static Matrix turnLeftZ = new Matrix("turnLeftZ", new int[][]{
            {0, -1, 0},
            {1,  0, 0},
            {0,  0, 1},
    });
    private static Matrix turnLeftY = new Matrix("turnLeftY", new int[][]{
            { 0,  0, 1},
            { 0,  1, 0},
            {-1,  0, 0},
    });
    private static Matrix turnRightY = new Matrix("turnRightY", new int[][]{
            { 0,  0, -1},
            { 0,  1,  0},
            { 1,  0,  0},
    });

    public static Matrix rotate = turnLeftZ.multiply(turnLeftY).setName("rotate");
    public static Matrix reflect = turnLeftZ.multiply(turnLeftZ).multiply(turnRightY).setName("reflect");

    /*
    org.mpashka.boxPuzzle.core.MatrixTest - Rotate: Matrix{ rotate
[0, 0, 1]
[1, 0, 0]
[0, 1, 0]
}
2018-08-18 22:54:11,528 bh DEBUG [      main]    org.mpashka.boxPuzzle.core.MatrixTest - Reflect: Matrix{ reflect
[0, 0, -1]
[0, -1, 0]
[-1, 0, 0]
}
     */

    public static Matrix multMatrix(Matrix m1, Matrix m2) {
        if (m1.rows() != m2.columns()) {
            throw new RuntimeException("Invalid size " + m1.rows() + " != " + m2.columns());
        }

//        log.info("Multiple {}, {}", m1, m2);
        Matrix matrix = new Matrix(m1.getName() + '*' + m2.getName(), m1.columns(), m2.rows());
        for (int column = 0; column < m1.columns(); column++) {
            for (int row = 0; row < m2.rows(); row++) {
                int result = 0;
                StringBuilder s = new StringBuilder();
                s.append(column);
                s.append(" , ");
                s.append(row);
                s.append(" : = ");
                for (int i = 0; i < m1.rows(); i++) {
                    s.append(m1.get(column, i));
                    s.append("*");
                    s.append(m2.get(i, row));
                    s.append(" ; ");
                    result += m1.get(column, i) * m2.get(i, row);
                }
                s.append(" = ");
                s.append(result);
//                log.info("Multiple: {}", s);
                matrix.set(column, row, result);
            }
        }
//        log.info("Result {}", matrix);
        return matrix;
    }

}
