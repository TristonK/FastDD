package ddfinder.predicate;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.Iterator;

/**
 * @author tristonK 2022/12/29
 */
public class PredicateSet implements Iterable<Predicate>{
    public static IndexProvider<Predicate> indexProvider = new IndexProvider<>();

    public static void configure(IndexProvider<Predicate> indexProvider) {
        PredicateSet.indexProvider = indexProvider;
    }


    private final LongBitSet bitset;

    public PredicateSet() {
        this.bitset = new LongBitSet();
    }

    public PredicateSet(LongBitSet bitset) {
        this.bitset = bitset.clone();
    }

    public PredicateSet(PredicateSet pS) {
        this.bitset = pS.getBitset().clone();
    }

    public void remove(Predicate predicate) {
        this.bitset.clear(indexProvider.getIndex(predicate));
    }

    public boolean containsPredicate(Predicate predicate) {
        return bitset.get(indexProvider.getIndex(predicate));
    }

    public boolean isSubsetOf(PredicateSet superset) {
        return bitset.isSubSetOf(superset.getBitset());
    }

    public LongBitSet getBitset() {
        return bitset;
    }

    public LongBitSet getLongBitSet() {
        return bitset;
    }

    public void addAll(PredicateSet predicateBitSet) {
        bitset.or(predicateBitSet.getBitset());
    }

    public int size() {
        return bitset.cardinality();
    }

    public boolean add(Predicate predicate) {
        int index = indexProvider.getIndex(predicate);
        boolean newAdded = !bitset.get(index);
        bitset.set(index);
        return newAdded;
    }

    @Override
    public Iterator<Predicate> iterator() {
        return new Iterator<>() {
            private int currentIndex = bitset.nextSetBit(0);

            @Override
            public Predicate next() {
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
       PredicateSet other = (PredicateSet) obj;
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
