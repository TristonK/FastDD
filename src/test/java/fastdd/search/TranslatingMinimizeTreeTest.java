package fastdd.search;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * @author tristonK 2023/7/23
 */
class TranslatingMinimizeTreeTest {
    TranslatingMinimizeTree tree = new TranslatingMinimizeTree(
            15, new int[]{0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 3, 3, 4, 4, 4},
            new int[]{0, 5, 8, 10, 12}, new int[]{0, 5, 8, 10, 12}, 5,
            new int[]{5, 3, 2, 2, 3},
            new HashMap<Integer, Integer>() {
                {
                put(0,0);put(1,1);put(2,2);put(3,3);put(4,4);
                put(5,0);put(6,1);put(7,2);put(8,0);put(9,1);
                put(10,0);put(11,1);put(12,0);put(13,1);put(14,2);
                }
            }
    );

    @Test
    public void minimize(){
        Set<IBitSet> ret = tree.minimize(TestDLeq0());
        System.out.println(Arrays.toString(ret.toArray()));
    }

    private List<IBitSet> TestDLeq0(){
        List<IBitSet> ret = new ArrayList<>();
        // a<=1
        ret.add(ProduceCandidate(new int[]{3}));
        // b<=0
        ret.add(ProduceCandidate(new int[]{7}));
        // a<=2, b<=2
        ret.add(ProduceCandidate(new int[]{2, 6}));
        // a<=1, b<=3
        ret.add(ProduceCandidate(new int[]{3, 5}));
        // c<=2, d<=1
        ret.add(ProduceCandidate(new int[]{8, 10}));
        // a<=3, b<=3, c<=2, d<=0
        ret.add(ProduceCandidate(new int[]{1, 5, 8, 11}));
        return ret;
    }

    private LongBitSet ProduceCandidate(int[] sets){
        LongBitSet ret = new LongBitSet();
        for(int a: sets){ret.set(a);}
        return ret;
    }



}