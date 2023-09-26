package fastdd.differentialfunction;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.Iterator;

/**
 * @author tristonK 2022/12/29
 */
public class DifferentialFunctionSet implements Iterable<DifferentialFunction>{
    public static IndexProvider<DifferentialFunction> indexProvider = new IndexProvider<>();

    public static void configure(IndexProvider<DifferentialFunction> indexProvider) {
        DifferentialFunctionSet.indexProvider = indexProvider;
    }

    private final LongBitSet bitset;

    public DifferentialFunctionSet() {
        this.bitset = new LongBitSet();
    }

    public DifferentialFunctionSet(LongBitSet bitset) {
        this.bitset = bitset.clone();
    }

    public DifferentialFunctionSet(int haveAllPredicates){
        this.bitset = new LongBitSet(haveAllPredicates);
        for(int i = 0; i < haveAllPredicates; i++){this.bitset.set(i);}
    }

    public DifferentialFunctionSet(DifferentialFunctionSet pS) {
        this.bitset = pS.getBitset().clone();
    }

    public void remove(DifferentialFunction differentialFunction) {
        this.bitset.clear(indexProvider.getIndex(differentialFunction));
    }

    public boolean containsPredicate(DifferentialFunction differentialFunction) {
        return bitset.get(indexProvider.getIndex(differentialFunction));
    }

    public boolean isSubsetOf(DifferentialFunctionSet superset) {
        return bitset.isSubSetOf(superset.getBitset());
    }

    public LongBitSet getBitset() {
        return bitset;
    }

    public LongBitSet getLongBitSet() {
        return bitset;
    }

    public void addAll(DifferentialFunctionSet predicateBitSet) {
        bitset.or(predicateBitSet.getBitset());
    }

    public int size() {
        return bitset.cardinality();
    }

    public boolean add(DifferentialFunction differentialFunction) {
        int index = indexProvider.getIndex(differentialFunction);
        boolean newAdded = !bitset.get(index);
        bitset.set(index);
        return newAdded;
    }

    @Override
    public Iterator<DifferentialFunction> iterator() {
        return new Iterator<>() {
            private int currentIndex = bitset.nextSetBit(0);

            @Override
            public DifferentialFunction next() {
                int lastIndex = currentIndex;
                currentIndex = bitset.nextSetBit(currentIndex + 1);
                return indexProvider.getObject(lastIndex);
            }

            @Override
            public boolean hasNext() {
                return currentIndex >= 0;
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
       DifferentialFunctionSet other = (DifferentialFunctionSet) obj;
        if (bitset == null) {
            return other.bitset == null;
        } else {
            return bitset.equals(other.bitset);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((bitset == null) ? 0 : bitset.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.forEach(p -> sb.append(p).append(" "));
        return sb.toString();
    }
}
