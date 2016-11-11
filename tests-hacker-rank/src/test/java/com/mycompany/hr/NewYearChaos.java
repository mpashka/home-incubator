package com.mycompany.hr;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * NewYearChaos
 * https://www.hackerrank.com/challenges/new-year-chaos
 *
 * It's New Year's Day and everyone's in line for the Wonderland rollercoaster ride!

 There are  people queued up, and each person wears a sticker indicating their initial position in the queue (i.e.:  with the first number denoting the frontmost position).

 Any person in the queue can bribe the person directly in front of them to swap positions. If two people swap positions, they still wear the same sticker denoting their original place in line. One person can bribe at most two other persons.

 That is to say, if  and  bribes , the queue will look like this: .

 Fascinated by this chaotic queue, you decide you must know the minimum number of bribes that took place to get the queue into its current state!
 *
 * @author Pavel Moukhataev
 */
public class NewYearChaos {

    @Test
    public void testMy() {
        System.setIn(new ByteArrayInputStream((
                "1\n" +
/*
                        "8\n" +
                        "5 1 2 3 7 8 6 4"

                        "8\n" +
                        "1 2 5 3 7 8 6 4"
*/
                        "3\n" +
                        "3 2 1"
        ).getBytes()
        ));
        main(null);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int T = in.nextInt();
        for(int a0 = 0; a0 < T; a0++){
            int n = in.nextInt();
            int q[] = new int[n];
            for(int q_i=0; q_i < n; q_i++){
                q[q_i] = in.nextInt();
            }
            // your code goes here
            int[] q_ = new int[n];
            for (int i = 0; i < n; i++) {
                q_[i] = i+1;
            }
            int bribes = 0;
            boolean unordered = false;
            for (int i = 0; i < n; i++) {
                if (q[i] == q_[i]) {
                    // ok
                } else if (i < n-1 && q[i] == q_[i+1]) {
                    q_[i+1] = q_[i];
                    bribes++;
                } else if (i < n-2 && q[i] == q_[i+2]) {
                    q_[i + 2] = q_[i + 1];
                    q_[i + 1] = q_[i];
                    bribes+=2;
                } else {
                    unordered = true;
//                    System.out.println("i " + i + ", q_[i] " + q_[i] + ", q[i] " + q[i]);
                    break;
                }
            }
            System.out.println(unordered ? "Too chaotic" : "" /*bribes1 + "/" + bribes2 + "/"*/ + bribes);
        }
    }

}
