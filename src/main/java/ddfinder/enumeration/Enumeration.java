package ddfinder.enumeration;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.PredicateSet;

import java.util.Set;

/**
 * @author tristonK 2023/2/20
 */
public interface Enumeration {
    /**
     * @return differential dependency set
     */
    public DifferentialDependencySet buildDifferentialDenpendency();
}
