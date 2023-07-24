package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.DifferentialFunctionBuilder;

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
//        if(Objects.equals(clue.toString(), "{001000010000000010000001000001}")) {
//            System.out.println(clue);
//        }
        this.count = count;
        this.bitset = buildEvidenceFromClue(countToPredicateSets);
    }

    public Evidence(long clue, Long count, List<List<LongBitSet>> countToPredicateSets, long[] bases){
        this.clue = null;
        this.count = count;
        LongBitSet evidenceBitSet = new LongBitSet();
        for(int i = countToPredicateSets.size() - 1; i >= 0; i--){
            long offset = clue / bases[i];
            clue %= bases[i];
            LongBitSet mask = countToPredicateSets.get(i).get((int)offset);
            evidenceBitSet.or(mask);
        }
        this.bitset = evidenceBitSet;
    }

    private LongBitSet buildEvidenceFromClue(List<List<LongBitSet>> countToPredicateSets){
        LongBitSet evidenceBitSet = new LongBitSet();
        for(int i = clue.nextSetBit(0); i >=0 ; i = clue.nextSetBit(i+1)){
            int col = ClueSetBuilder.bit2ColMap[i];
            int offset = i - ClueSetBuilder.col2FirstBitMap[col];
            //获得clue里i对应的列属性和偏移量（即在阈值列表中的位置）
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

    public String toDFString(){
        StringBuilder sb = new StringBuilder();
        for(int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)){
            sb.append(DifferentialFunctionBuilder.predicateIdProvider.getObject(i).toString());
            sb.append(",");
        }
        return sb.toString();
    }
}
