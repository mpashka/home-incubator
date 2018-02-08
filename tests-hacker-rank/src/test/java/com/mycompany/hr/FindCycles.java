package com.mycompany.hr;

import java.util.*;

/**
 * Created by pmoukhataev on 17.08.17.
 * https://www.hackerrank.com/contests/code-the-next/challenges/the-marathon
 */
public class FindCycles {
    static int findStreets(int n, int[][] inStreetPaths) {
        // Complete this function
        Map<Integer /* to */, Street>[] streets = new Map[n+1];
        for (int i = 1; i <=n; i++) {
            streets[i] = new HashMap<>();
        }
        for (int[] ints : inStreetPaths) {
            int from = ints[0];
            int to = ints[1];
            Street street = streets[from].computeIfAbsent(to, k -> new Street(from, to));
            street.addCount();
        }

        Set<Set<Integer>> cycles = new HashSet<>();
        Set<Street> cycledStreets = new HashSet<>();
        List<Path> paths = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            if (!streets[i].isEmpty()) {
                paths.add(new Path(i));
            }
        }
        int newPathsFound;
        do {
            newPathsFound = 0;
            List<Path> newPaths = new ArrayList<>();
            for (Iterator<Path> pathIterator = paths.iterator(); pathIterator.hasNext(); ) {
                Path path = pathIterator.next();
                Map<Integer, Street> nextStreets = streets[path.last];
                if (nextStreets.isEmpty()) {
                    // a dead end
                    pathIterator.remove();
                } else if (nextStreets.size() == 1) {
                    // Optimization for the case of one street from current junk
                    Street next = nextStreets.values().iterator().next();
                    if (next.to.equals(path.first)) {
                        // cycle found
                        cycles.add(path.visitedJunks);
                        path.streets.add(next);
                        cycledStreets.addAll(path.streets);
                        pathIterator.remove();
                    } else if (path.add(next)) {
                        // next node
                        newPathsFound++;
                    } else {
                        // another cycle. will be found by other path
                        pathIterator.remove();
                    }
                } else {
                    nextStreets = new HashMap<>(nextStreets);
                    for (Iterator<Map.Entry<Integer, Street>> nextStreetsIterator = nextStreets.entrySet().iterator(); nextStreetsIterator.hasNext(); ) {
                        Map.Entry<Integer, Street> nextStreetEntry = nextStreetsIterator.next();
                        Integer nextStreetJunk = nextStreetEntry.getKey();
                        if (nextStreetJunk.equals(path.first)) {
                            // path found
                            cycles.add(new HashSet<>(path.visitedJunks));
                            cycledStreets.addAll(path.streets);
                            cycledStreets.add(nextStreetEntry.getValue());
                            nextStreetsIterator.remove();
                        }
                    }
                    // remove junks that where visited already
                    nextStreets.keySet().removeAll(path.visitedJunks);
                    newPathsFound += nextStreets.size();
                    if (nextStreets.isEmpty()) {
                        // already visitedJunks all places
                        pathIterator.remove();
                    } else if (nextStreets.size() == 1) {
                        path.add(nextStreets.values().iterator().next());
                    } else {
                        Iterator<Street> nextStreetsIterator = nextStreets.values().iterator();
                        Street firstStreet = nextStreetsIterator.next();
                        while (nextStreetsIterator.hasNext()) {
                            newPaths.add(path.cloneAndAdd(nextStreetsIterator.next()));
                        }
                        path.add(firstStreet);
                    }
                }
            }
            paths.addAll(newPaths);
        } while (newPathsFound > 0);

        // calculate streets count
        int count = 0;
        for (Set<Integer> cycle : cycles) {
//            for (Integer junk : cycle) {
//
//            }
//            System.out.println("    Cycle: " + cycle);
            count += cycle.size();
        }

        count = 0;
//        System.out.println("    Streets in cycle: " + cycledStreets);
        for (Street street : cycledStreets) {
            count += street.count;
        }
        return count;
    }

    public static void main(String[] args) {
        System.out.println("=" + findStreets(
                7,
                new int[][]{
                        {7, 2},
                        {7, 5},
                        {7, 2},
                        {2, 4},
                        {4, 2},
                        {6, 7},
                        {4, 6},
                }
        ));
    }

    private static class Street {
        private Integer from;
        private Integer to;
        private int count;

        public Street(Integer from, Integer to) {
            this.from = from;
            this.to = to;
        }

        public void addCount() {
            count++;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Street street = (Street) o;
            return from.equals(street.from) && to.equals(street.to);
        }

        @Override
        public int hashCode() {
            return 31 * from + to;
        }

        @Override
        public String toString() {
            return from + "->" + to;
        }
    }

    private static class Path {
        private Integer first;
        private Integer last;
        private Set<Street> streets;
        private Set<Integer> visitedJunks;

        public Path(Integer first) {
            this.first = this.last = first;
            visitedJunks = new HashSet<>();
            streets = new HashSet<>();
            visitedJunks.add(first);
        }

        public Path(Integer first, Integer last, Set<Integer> visitedJunks, Set<Street> streets) {
            this.first = first;
            this.last = last;
            this.streets = streets;
            this.visitedJunks = visitedJunks;
        }

        /**
         * @return true if next was not visitedJunks yet
         */
        public boolean add(Street next) {
            last = next.to;
            streets.add(next);
            return visitedJunks.add(next.to);
        }

        public Path cloneAndAdd(Street next) {
            Set<Integer> visitedJunks = new HashSet<>(this.visitedJunks);
            Set<Street> streets = new HashSet<>(this.streets);
            visitedJunks.add(next.to);
            streets.add(next);
            return new Path(first, next.to, visitedJunks, streets);
        }
    }
}
