package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.PredicateBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSet implements Iterable<Evidence>{

    private HashMap<LongBitSet, Evidence> clueToEvidence;
    private final int[] colMap;


    private List<List<LongBitSet>> countToPredicateSets;

    public EvidenceSet(PredicateBuilder predicateBuilder, int[] colMap){
        clueToEvidence = new HashMap<LongBitSet, Evidence>();
        this.colMap = colMap;
        this.countToPredicateSets = new ArrayList<>();
        buildColMasks(predicateBuilder);
    }

    public void buildColMasks(PredicateBuilder predicateBuilder){
        for(int i = 0; i < colMap.length; i++){
            int col = colMap[i];
            countToPredicateSets.add(predicateBuilder.getColPredicateSet(col));
        }
    }


    public void build(HashMap<LongBitSet, Long> clueSet){
        for (var entry : clueSet.entrySet()) {
            LongBitSet clue = entry.getKey();
            Evidence evi = new Evidence(clue, entry.getValue(), countToPredicateSets);
            clueToEvidence.put(clue, evi);
        }
    }

    public int size() {
        return clueToEvidence.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        clueToEvidence.forEach((k, v) -> sb.append(k.toString() + "\t" + v + "\n"));
        return sb.toString();
    }

    @Override
    public Iterator<Evidence> iterator() {
        return clueToEvidence.values().iterator();
    }
}
