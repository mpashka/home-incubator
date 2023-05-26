package org.test.mpashka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SimpleTest {

    public List<Integer> top3(List<Integer> in) {
        SortedList out = new SortedList(3);
        in.forEach(out::add);
        return out.top();
    }

    @Test
    public void test() {
//        assertThat(List.of(1,2,3,4,5), is(List.of(5,4,3)));
        assertEquals(top3(List.of(1,2)), List.of(2,1));
        assertEquals(top3(List.of(1,2,3,4,5)), List.of(5,4,3));
        assertEquals(top3(List.of(1,2,3,4,5,4)), List.of(5,4,4));
        assertEquals(top3(List.of(1,2,3,4,5,4,5,5)), List.of(5,5,5));
        assertEquals(top3(List.of(1,2,Integer.MIN_VALUE,4,5,4,5,Integer.MAX_VALUE, 0, -1, 1, -2, 2, -0)), List.of(Integer.MAX_VALUE,5,5));
        List<Integer> list = List.of(1, 2, 3, 4, 5, 4, 5, 5);
        Collections.reverse(new ArrayList<>(list));
        assertEquals(top3(list), List.of(5,5,5));
    }

    private static class SortedList {
        private List<Integer> list = new LinkedList<>();
        private int size;

        public SortedList(int size) {
            this.size = size;
        }

        public void add(int number) {
            ListIterator<Integer> listIter = list.listIterator();
            for (int i = 0; i < size && listIter.hasNext(); i++) {
                int element = listIter.next();
                if (number > element) {
                    listIter.previous();
                    listIter.add(number);
                    return;
                }
            }
            if (list.size() < size) {
                list.add(number);
            }
        }

        public List<Integer> top() {
            return list.subList(0, Math.min(list.size(), size));
        }
    }
}
