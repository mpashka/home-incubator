package com.mycompany.hr;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;

/**
 * BestDivisor
 * https://www.hackerrank.com/contests/w26/challenges/best-divisor
 *
 * @author Pavel Moukhataev
 */
public class BestDivisor {

    @Test
    public void test1() {
        doTestIt("6");
        doTestIt("12");
        doTestIt("100000");
    }

    @Test
    public void testScore() {
        assertEquals(1, score(1));
        assertEquals(5, score(5));
        assertEquals(1, score(10));
        assertEquals(8, score(17));
        assertEquals(13, score(1372));
    }

    private void doTestIt(String in) {
        System.out.println(in);
        System.setIn(new ByteArrayInputStream((in).getBytes()));
        main(null);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int best = 1;
        int bestScore = 1;
        for (int i = 2; i <= n; i++) {
            if (n % i == 0) {
                int score = score(i);
                if (score > bestScore) {
                    best = i;
                    bestScore = score;
                }
            }
        }
        System.out.println(best);
    }

    private static int score(int i) {
        int score = 0;
        while (i > 0) {
            score += i % 10;
            i /= 10;
        }
        return score;
    }
}
