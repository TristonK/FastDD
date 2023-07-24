package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.dfset.longclueimpl.LongClueSetBuilder;
import ddfinder.predicate.DifferentialFunctionBuilder;

import java.util.*;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSet implements Iterable<Evidence>{

    private HashMap<LongBitSet, Evidence> clueToEvidence;
    private HashMap<Long, Evidence> longClueToEvidence;

    private List<Evidence> evidences;

    /**
     * count: a bit of 1 in the whole clue
     * predicatesSet: all statisfied predicates
     * example: predicates:{<=2, <=1, <=0, <=0, >0, >1, >2}
     *          clue: 0010 => {<=2, >0, >1}
     * */
    private final List<List<LongBitSet>> offsetToPredicateSets;

    public EvidenceSet(DifferentialFunctionBuilder differentialFunctionBuilder){
        clueToEvidence = new HashMap<>();
        longClueToEvidence = new HashMap<>();
        evidences = new ArrayList<>();
        this.offsetToPredicateSets = new ArrayList<>();
        for(int i = 0; i < differentialFunctionBuilder.getColSize(); i++){
            offsetToPredicateSets.add(differentialFunctionBuilder.getOffset2SatisfiedPredicates(i));
        }
    }

    public void build(HashMap<LongBitSet, Long> clueSet){
        for (var entry : clueSet.entrySet()) {
            LongBitSet clue = entry.getKey();
            Evidence evi = new Evidence(clue, entry.getValue(), offsetToPredicateSets);
            clueToEvidence.put(clue, evi);
            evidences.add(evi);
        }
    }

    // v2 using long
    public void buildFromLong(HashMap<Long, Long> clueSet){
        for(var entry: clueSet.entrySet()){
            long clue = entry.getKey();
            Evidence evi = new Evidence(clue, entry.getValue(), offsetToPredicateSets, LongClueSetBuilder.bases);
            //if(evi==null){System.out.println("xxxxxx");}
            longClueToEvidence.put(clue, evi);
            evidences.add(evi);
        }
    }

    public int size() {
        return evidences.size();
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
