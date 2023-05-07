package ch.javasoft.bitset.search;

import ch.javasoft.bitset.LongBitSet;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tristonK 2023/4/16
 */
class NTreeSearchTest {
    NTreeSearch nt = new NTreeSearch();

    @org.junit.jupiter.api.Test
    void add() {
        LongBitSet bs1 = new LongBitSet("011101");
        nt.add(bs1);
        LongBitSet bs2 = new LongBitSet("011101");
        System.out.println(nt.containsSubset(bs2));
    }

    @org.junit.jupiter.api.Test
    void containsSubset() {

    }
}