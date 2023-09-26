package fastdd.dfset;

import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.List;

/**
 * @author tristonK 2022/12/31
 */
public class MatchDF {
    public final LongBitSet bitset;

    /**
     * occur times in all tuple pairs
     * */
    private final long count;
    private final LongBitSet clue;

    public MatchDF(LongBitSet clue, Long count, List<List<LongBitSet>> countToPredicateSets){
        this.clue = clue;
        this.count = count;
        this.bitset = buildMatchDFFromBitsetISN(countToPredicateSets);
    }

    public MatchDF(long clue, Long count, List<List<LongBitSet>> countToPredicateSets, long[] bases){
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

    private LongBitSet buildMatchDFFromBitsetISN(List<List<LongBitSet>> countToPredicateSets){
        LongBitSet dfBitSet = new LongBitSet();
        for(int i = clue.nextSetBit(0); i >=0 ; i = clue.nextSetBit(i+1)){
            int col = BitSetISNBuilder.bit2ColMap[i];
            int offset = i - BitSetISNBuilder.col2FirstBitMap[col];
            //获得clue里i对应的列属性和偏移量（即在阈值列表中的位置）
            LongBitSet mask = countToPredicateSets.get(col).get(offset);
            if(mask == null){System.out.println("sss");}
            dfBitSet.or(mask);
        }
        return dfBitSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MatchDF matchDF = (MatchDF) o;
        return clue == matchDF.clue;
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
            sb.append(DifferentialFunctionBuilder.dfIdProvider.getObject(i).toString());
            sb.append(",");
        }
        return sb.toString();
    }
}
