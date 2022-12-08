package org.mpashka.test.core;

public class List<E> {
    public static <Z> List<Z> nil() {
        return null;
    }
    public static <Z> List<Z> cons(Z head, List<Z> tail) {
        return null;
    }
    public E head() {
        return null;
    }

    public void aaa() {
        List<Integer> cons = List.cons(42, List.nil());
//        String s = List.nil().head();
    }
}
