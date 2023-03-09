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
    private List<Evidence> evidences;

    /**
     * count: a bit of 1 in the whole clue
     * predicatesSet: all statisfied predicates
     * example: predicates:{<=2, <=1, <=0, <=0, >0, >1, >2}
     *          clue: 0010 => {<=2, >0, >1}
     * */
    private List<List<LongBitSet>> countToPredicateSets;

    public EvidenceSet(PredicateBuilder predicateBuilder){
        clueToEvidence = new HashMap<LongBitSet, Evidence>();
        evidences = new ArrayList<>();
        this.countToPredicateSets = new ArrayList<>();
        for(int i = 0; i < predicateBuilder.getColSize(); i++){
            // colMap : bit in clue => colIndex
            countToPredicateSets.add(predicateBuilder.getColPredicateSet(i));
        }
    }

    public void build(HashMap<LongBitSet, Long> clueSet){
        for (var entry : clueSet.entrySet()) {
            LongBitSet clue = entry.getKey();
            Evidence evi = new Evidence(clue, entry.getValue(), countToPredicateSets);
            clueToEvidence.put(clue, evi);
            evidences.add(evi);
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
        return evidences.iterator();
    }

    public Evidence getEvidenceById(int id){
        if(id >= evidences.size()){
            throw new IllegalArgumentException("No such evidence id {" + id + "} in evidence set");
        }
        return evidences.get(id);
    }

    public List<Evidence> getEvidences(){
        return evidences;
    }
}
