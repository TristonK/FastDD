package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;

import java.util.List;

/**
 * @author tristonK 2022/12/31
 */
public class Evidence {
    public final LongBitSet bitset;

    /**
     * occur times in all tuple pairs
     * */
    private final long count;
    private final LongBitSet clue;

    public Evidence(LongBitSet clue, Long count, List<List<LongBitSet>> countToPredicateSets){
        this.clue = clue;
        this.count = count;
        this.bitset = buildEvidenceFromClue(countToPredicateSets);
    }

    private LongBitSet buildEvidenceFromClue(List<List<LongBitSet>> countToPredicateSets){
        LongBitSet evidenceBitSet = new LongBitSet();
        for(int i = clue.nextSetBit(0); i >=0 ; i = clue.nextSetBit(i+1)){
            int col = ClueSetBuilder.bit2ColMap[i];
            int offset = i - ClueSetBuilder.col2FirstBitMap[col];
            LongBitSet mask = countToPredicateSets.get(col).get(offset);
            if(mask == null){System.out.println("sss");}
            evidenceBitSet.or(mask);
        }
        return evidenceBitSet;
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

    public LongBitSet getBitset(){
        return bitset;
    }

    @Override
    public String toString() {
        return bitset.toString();
    }
}
