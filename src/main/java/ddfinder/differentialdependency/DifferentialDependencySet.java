package ddfinder.differentialdependency;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author tristonK 2022/12/31
 */
public class DifferentialDependencySet implements Iterable<DifferentialDependency>{
    private Set<DifferentialDependency> dependencies;

    public DifferentialDependencySet(){
        dependencies = new HashSet<>();
    }

    public Set<DifferentialDependency> getDependencies() {
        return dependencies;
    }

    public boolean add(DifferentialDependency dependency){return dependencies.add(dependency);}

    public boolean contains(DifferentialDependency dependency){return  dependencies.contains(dependency);}

    public int size(){return dependencies.size();}

    @Override
    public Iterator<DifferentialDependency> iterator() {
        return dependencies.iterator();
    }
}
