package com.mycompany.hr;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

/**
 * HardHomework
 * https://www.hackerrank.com/contests/w26/challenges/hard-homework
 *
 * @author Pavel Moukhataev
 */
public class HardHomework {
    @Test
    public void test1() {
        doTestIt("3");
    }

    private void doTestIt(String in) {
        System.out.println(in);
        System.setIn(new ByteArrayInputStream((in).getBytes()));
        main(null);
    }

    public static void main(String[] args) {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT. Your class should be named Solution. */
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        double max = 0;
        for (int i = 1; i <= n-2; i++) {
            double sinI = Math.sin(i);
            if (sinI < max-2) continue;
            for (int j = 1; j <= n-i-1; j++) {
                double sinJ = Math.sin(j);
                if (sinI+sinJ < max-1) continue;
                int k = n-i-j;
                double sinK = Math.sin(k);
                double sum = sinI + sinJ + sinK;
                if (sum > max) {
                    max = sum;
                }
            }
        }
        NumberFormat numberFormat = new DecimalFormat("0.000000000", DecimalFormatSymbols.getInstance(Locale.US));
        System.out.println(numberFormat.format(max));
    }
}
