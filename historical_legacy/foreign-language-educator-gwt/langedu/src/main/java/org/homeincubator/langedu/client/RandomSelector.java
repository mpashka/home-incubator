package org.homeincubator.langedu.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
*/
public final class RandomSelector<T> {
    private static final Logger log = Logger.getLogger(RandomSelector.class.getName());

    List<Item<T>> itemList = new ArrayList<Item<T>>();
    double probabilitySum;


    public void add(T value, double probability) {
        itemList.add(new Item<T>(value, probability));
        probabilitySum += probability;
    }

    public T select() {
        double selectResult = Math.random() * probabilitySum;
        double probabilitySum = 0;
        for (Item<T> probability : itemList) {
            probabilitySum += probability.probability;
            if (probabilitySum > selectResult) return probability.value;
        }
        // error!
        RuntimeException e = new RuntimeException("Internal error");
        log.log(Level.SEVERE, "Can't be here. Sum: " + probabilitySum + ", result: " + selectResult, e);
        throw e;
    }


    private static final class Item<T> {
        T value;
        double probability;

        private Item(T value, double probability) {
            this.value = value;
            this.probability = probability;
        }
    }


    public static <T> T randomSelect(List<T> list, boolean remove) {
        int itemIndex = (int) Math.floor(Math.random() * list.size());
        T item = remove ? list.remove(itemIndex) : list.get(itemIndex);
        return item;
    }


}
