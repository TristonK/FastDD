package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.List;

/**
 * @author tristonK 2022/12/31
 */
public class Evidence {
    public LongBitSet bitset;

    private final long count;
    private final LongBitSet clue;

    private List<List<LongBitSet>> countToPredicateSets;
    public Evidence(LongBitSet clue, Long count, List<List<LongBitSet>> countToPredicateSets){
        this.clue = clue;
        this.count = count;
        this.countToPredicateSets = countToPredicateSets;
        buildEvidenceFromClue();
    }

    private void buildEvidenceFromClue(){
        LongBitSet bitSet = new LongBitSet(countToPredicateSets.size()*3);
        for(int i = 0; i < countToPredicateSets.size(); i++){
            //long mask = ((7L<<(i*3))&clue)>>(i*3);
            //bitSet.or(countToPredicateSets.get(i).get((int)mask));
        }
        this.bitset = bitSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Evidence evidence = (Evidence) o;
        return clue == evidence.clue;
    }

    @Override
    public int hashCode() {
        return clue.hashCode();
    }

    public long getCount() {
        return count;
    }
}
