package ddfinder.differentialdependency;

import ch.javasoft.bitset.IBitSet;
import ddfinder.predicate.DifferentialFunction;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.*;

/**
 * @author tristonK 2022/12/31
 */
public class DifferentialDependencySet implements Iterable<DifferentialDependency>{
    private Set<DifferentialDependency> dependencies;

    public DifferentialDependencySet(){
        dependencies = new HashSet<>();
    }

    public DifferentialDependencySet(Set<IBitSet> covers, IndexProvider<DifferentialFunction> indexProvider){
        dependencies = new HashSet<>();
        for(IBitSet cover: covers){
            IBitSet newCover = cover.clone();
            IBitSet lCover =cover.clone();
            if(cover.cardinality() < 2){
                System.out.println("bad cover");
                assert false;
            }
            List<DifferentialFunction> left = new ArrayList<>();
            DifferentialFunction right = null;
            for(int i = cover.nextSetBit(0); i>=0; i = cover.nextSetBit(i+1)){
                DifferentialFunction p = indexProvider.getObject(i);
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
                DifferentialFunction chooseRight = left.get(left.size() - 1);
                right = chooseRight.getInversePredicate();
                left.remove(left.size() - 1);
                lCover.clear(indexProvider.getIndex(chooseRight));
                newCover.clear(indexProvider.getIndex(chooseRight));
                newCover.set(indexProvider.getIndex(right));
            }
            dependencies.add(new DifferentialDependency(left, right, newCover, lCover));
        }
    }

    public DifferentialDependencySet(Set<IBitSet> covers, int rightID, IndexProvider<DifferentialFunction> indexProvider){
        dependencies = new HashSet<>();
        for(IBitSet cover: covers){
            IBitSet newCover = cover.clone();
            IBitSet lCover =cover.clone();
            if(cover.cardinality() < 1){
                System.out.println("bad cover");
                assert false;
            }
            List<DifferentialFunction> left = new ArrayList<>();
            DifferentialFunction right = indexProvider.getObject(rightID);
            for(int i = cover.nextSetBit(0); i>=0; i = cover.nextSetBit(i+1)){
                if(i == rightID){
                    System.out.println("right ID exists in Left");
                    assert false;
                }
                DifferentialFunction p = indexProvider.getObject(i);
                left.add(p);
            }
            newCover.set(rightID);
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

    public void addAll(DifferentialDependencySet dds){
        if(dds == null){return;}
        dependencies.addAll(dds.getDependencies());
    }

}
