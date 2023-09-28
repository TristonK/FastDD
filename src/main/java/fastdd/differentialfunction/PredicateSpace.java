package fastdd.differentialfunction;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.BitSet;
import java.util.List;

/**
 * @author tristonK 2023/4/18
 */
public class PredicateSpace {
    private DifferentialFunctionSet differentialFunctionSet;
    private DifferentialFunctionSet acceptDifferentialFunctionSet;
    private DifferentialFunctionSet rejectDifferentialFunctionSet;

    private IndexProvider<DifferentialFunction> predicateIndexProvider;

    private List<BitSet> colPredicateGroup;

    public PredicateSpace(LongBitSet predicateSet, LongBitSet acceptPredicateSet, LongBitSet rejectPredicateSet, IndexProvider<DifferentialFunction> indexProvider, List<BitSet> colPredicates){
        this.differentialFunctionSet = new DifferentialFunctionSet(predicateSet);
        this.acceptDifferentialFunctionSet = new DifferentialFunctionSet(acceptPredicateSet);
        this.rejectDifferentialFunctionSet = new DifferentialFunctionSet(rejectPredicateSet);
        this.predicateIndexProvider = indexProvider;
        this.colPredicateGroup = colPredicates;
    }

    public DifferentialFunctionSet getPredicateSet() {
        return differentialFunctionSet;
    }

    public DifferentialFunctionSet getAcceptPredicateSet() {
        return acceptDifferentialFunctionSet;
    }

    public DifferentialFunctionSet getRejectPredicateSet() {
        return rejectDifferentialFunctionSet;
    }

    public IndexProvider<DifferentialFunction> getPredicateIndexProvider() {
        return predicateIndexProvider;
    }

    public List<BitSet> getColPredicateGroup() {
        return colPredicateGroup;
    }

    @Override
    public String toString() {
        return "{full predicates: " + differentialFunctionSet.toString() + " accepted: " + acceptDifferentialFunctionSet.toString() + " rejected: " + rejectDifferentialFunctionSet.toString() + "}";
    }

    public int size(){
        return differentialFunctionSet.size();
    }
}
