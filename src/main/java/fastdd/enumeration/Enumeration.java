package fastdd.enumeration;

import fastdd.differentialdependency.DifferentialDependencySet;

/**
 * @author tristonK 2023/2/20
 */
public interface Enumeration {
    /**
     * @return differential dependency set
     */
    public DifferentialDependencySet buildDifferentialDenpendency();
}
