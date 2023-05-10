package ddfinder.differentialdependency;

import ch.javasoft.bitset.IBitSet;
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

    public DifferentialDependencySet(Set<IBitSet> covers, IndexProvider<Predicate> indexProvider){
        dependencies = new HashSet<>();
        for(IBitSet cover: covers){
            IBitSet newCover = cover.clone();
            IBitSet lCover =cover.clone();
            if(cover.cardinality() < 2){
                System.out.println("bad cover");
                assert false;
            }
            List<Predicate> left = new ArrayList<>();
            Predicate right = null;
            for(int i = cover.nextSetBit(0); i>=0; i = cover.nextSetBit(i+1)){
                Predicate p = indexProvider.getObject(i);
                if(right == null && !p.isAccepted()){
                    right = p.getInversePredicate();
                    newCover.clear(i);
                    lCover.clear(i);
                    newCover.set(indexProvider.getIndex(right));
                }else {
                    left.add(p);
                }
            }
            if(right == null){
                Predicate chooseRight = left.get(left.size() - 1);
                right = chooseRight.getInversePredicate();
                left.remove(left.size() - 1);
                lCover.clear(indexProvider.getIndex(chooseRight));
                newCover.clear(indexProvider.getIndex(chooseRight));
                newCover.set(indexProvider.getIndex(right));
            }
            dependencies.add(new DifferentialDependency(left, right, newCover, lCover));
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

    public void minimize(){

    }

}
