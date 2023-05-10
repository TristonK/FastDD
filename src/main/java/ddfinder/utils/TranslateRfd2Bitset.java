package ddfinder.utils;

import ddfinder.predicate.PredicateBuilder;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * @author tristonK 2023/5/10
 */
public class TranslateRfd2Bitset {
    // 可以直接抄最小化那里的转化，或者直接实现下面这个也可以
    public static List<BitSet> translate(String rfdPath){
        Map<String, List<Double>> thresholds = parseRFDThresholds.getThresholds(rfdPath);
        // TODO
        return null;
    }

    public static List<BitSet> translate2PredicateSet(String rfdPath, PredicateBuilder predicateBuilder){
        return null;
    }
}
