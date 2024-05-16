package org.test.mpashka;

import java.util.BitSet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class BitSetTest {
    @Test
    public void test() {
        BitSet bitSet = new BitSet();
        bitSet.set(1);
        bitSet.set(2);
        bitSet.set(5);

        BitSet oldBitSet = new BitSet();
        oldBitSet.set(1);
        oldBitSet.set(7);

        BitSet bitSet1 = (BitSet) bitSet.clone();
        bitSet.xor(oldBitSet);
        bitSet.and(bitSet1);
        log.info("Result: {}", bitSet);
    }
}
