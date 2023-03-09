package ddfinder.differentialdependency;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.Evidence;
import ddfinder.predicate.IntervalPredicate;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.predicate.PredicateProvider;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.predicates.Operator;

import java.util.*;

/**
 * @author tristonK 2022/12/31
 */
public class DifferentialDependencySet implements Iterable<DifferentialDependency>{
    private Set<DifferentialDependency> dependencies;

    public DifferentialDependencySet(){
        dependencies = new HashSet<>();
    }

    public DifferentialDependencySet(Set<LongBitSet> covers, IndexProvider<Predicate> indexProvider){
        dependencies = new HashSet<>();
        for(LongBitSet cover: covers){
            if(cover.cardinality() < 2){
                System.out.println("bad cover");
                assert false;
            }
            List<Predicate> left = new ArrayList<>();
            Predicate right = null;
            for(int i = cover.nextSetBit(0); i>=0; i = cover.nextSetBit(i+1)){
                Predicate p = indexProvider.getObject(i);
                if(right == null && p.getOperator() == Operator.GREATER){
                    right = p.getInversePredicate();
                }else {
                    left.add(p);
                }
            }
            if(right == null){
                right = left.get(left.size() -1).getInversePredicate();
                left.remove(left.size() - 1);
            }
            dependencies.add(new DifferentialDependency(left, right));
        }
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

    public void printDDs(){
        for(DifferentialDependency dd: dependencies){
            System.out.println(dd.toString());
        }
    }

}
