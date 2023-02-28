package ddfinder.enumeration;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.PredicateSet;

import java.util.Set;

/**
 * @author tristonK 2023/2/27
 */
public class HybridEvidenceInversion implements Enumeration{
    /**
     * @param clueSet:     clue set
     * @param predicateSet
     * @return
     */
    @Override
    public DifferentialDependencySet buildDifferentialDenpendency(Set<LongBitSet> clueSet, PredicateSet predicateSet) {
        return null;
    }
}
