package ddfinder.differentialfunction;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.BitSet;
import java.util.List;

/**
 * @author tristonK 2023/4/18
 */
public class PredicateSpace {
    private PredicateSet predicateSet;
    private PredicateSet acceptPredicateSet;
    private PredicateSet rejectPredicateSet;

    private IndexProvider<DifferentialFunction> predicateIndexProvider;

    private List<BitSet> colPredicateGroup;

    public PredicateSpace(LongBitSet predicateSet, LongBitSet acceptPredicateSet, LongBitSet rejectPredicateSet, IndexProvider<DifferentialFunction> indexProvider, List<BitSet> colPredicates){
        this.predicateSet = new PredicateSet(predicateSet);
        this.acceptPredicateSet = new PredicateSet(acceptPredicateSet);
        this.rejectPredicateSet = new PredicateSet(rejectPredicateSet);
        this.predicateIndexProvider = indexProvider;
        this.colPredicateGroup = colPredicates;
    }

    public PredicateSet getPredicateSet() {
        return predicateSet;
    }

    public PredicateSet getAcceptPredicateSet() {
        return acceptPredicateSet;
    }

    public PredicateSet getRejectPredicateSet() {
        return rejectPredicateSet;
    }

    public IndexProvider<DifferentialFunction> getPredicateIndexProvider() {
        return predicateIndexProvider;
    }

    public List<BitSet> getColPredicateGroup() {
        return colPredicateGroup;
    }

    @Override
    public String toString() {
        return "{full predicates: " + predicateSet.toString() + " accepted: " + acceptPredicateSet.toString() + " rejected: " + rejectPredicateSet.toString() + "}";
    }

    public int size(){
        return predicateSet.size();
    }
}
