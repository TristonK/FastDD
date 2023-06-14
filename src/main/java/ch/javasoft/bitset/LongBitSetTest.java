package ch.javasoft.bitset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tristonK 2023/6/8
 */
class LongBitSetTest {
    @Test
    void compareTo() {
        LongBitSet a = new LongBitSet(3);
        LongBitSet b = new LongBitSet(3);
        a.set(1);b.set(2);
        System.out.println(b.compareTo(a));
        // 在前的更大
    }

}