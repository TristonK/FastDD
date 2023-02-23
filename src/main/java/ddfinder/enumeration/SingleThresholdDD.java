package ddfinder.enumeration;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateSet;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tristonK 2023/2/20
 */
public class SingleThresholdDD implements Enumeration{

    private Map<Integer, BitSet> pred2ClueMap;
    private PredicateSet predicateSet;

    @Override
    public DifferentialDependencySet buildDifferentialDenpendency(Set<LongBitSet> clueSet, PredicateSet predicateSet) {
        DifferentialDependencySet dds = new DifferentialDependencySet();
        buildClueIndexes();
        sortPredicateByCount(predicateSet);
        return dds;
    }

    private void sortPredicateByCount(PredicateSet predicates){

    }

    private void buildClueIndexes() {

        pred2ClueMap = new HashMap<>(predicateSet.size());

        for (int pid = 0; pid < predicateSet.size(); pid++) {
            pred2ClueMap.put(pid, new BitSet());
        }

        BitSet initialEviIDs = new BitSet();




    }
}
