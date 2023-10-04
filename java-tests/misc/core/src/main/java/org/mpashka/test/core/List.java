package org.mpashka.test.core;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
//        List<Integer> cons = List.cons(42, List.nil());
//        String s = List.nil().head();
    }

    public <I, O> Function<Collection<I>, O> testLambda(Function<I, O> sfn) {
        return is -> {
            Collection<I> is1 = is;
            Optional<O> first = is1.stream().map(sfn).findFirst();
            return first.orElse(null);
        };
    }

    public void searchIdentityMethod() {
        Stream.of("aaa").map(i -> Function.identity())
    }
}
