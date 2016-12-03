package com.mycompany.hr;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * JumpingOnTheClouds
 * https://www.hackerrank.com/challenges/jumping-on-the-clouds
 *
 * @author Pavel Moukhataev
 */
public class JumpingOnTheClouds {

    @Test
    public void testIt() {
        doTestIt("6\n" +
                "0 0 0 1 0 0");
        doTestIt("7\n" +
                "0 0 1 0 0 1 0");
    }

    private void doTestIt(String in) {
        System.out.println(in);
        System.setIn(new ByteArrayInputStream((in).getBytes()));
        main(null);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int c[] = new int[n];
        for(int c_i=0; c_i < n; c_i++){
            c[c_i] = in.nextInt();
        }
        int num = 0;
        int i = 0;
        while (true) {
            num++;
            i += 2;
            if (i >=n-1) break;
            if (c[i] == 1) i--;
//            System.out.println("i:" + i + ", num:" + num);
        }
        System.out.println(num);
    }
}
