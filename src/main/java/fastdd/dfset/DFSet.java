package fastdd.dfset;

import ch.javasoft.bitset.LongBitSet;
import fastdd.dfset.isnimpl.ISNBuilder;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.*;

/**
 * @author tristonK 2022/12/31
 */
public class DFSet implements Iterable<MatchDF>{

    private HashMap<LongBitSet, MatchDF> clueToEvidence;
    private HashMap<Long, MatchDF> longClueToEvidence;

    private List<MatchDF> matchDFS;

    /**
     * count: a bit of 1 in the whole clue
     * predicatesSet: all statisfied predicates
     * example: predicates:{<=2, <=1, <=0, <=0, >0, >1, >2}
     *          clue: 0010 => {<=2, >0, >1}
     * */
    private final List<List<LongBitSet>> offsetToPredicateSets;

    public DFSet(DifferentialFunctionBuilder differentialFunctionBuilder){
        clueToEvidence = new HashMap<>();
        longClueToEvidence = new HashMap<>();
        matchDFS = new ArrayList<>();
        this.offsetToPredicateSets = new ArrayList<>();
        for(int i = 0; i < differentialFunctionBuilder.getColSize(); i++){
            offsetToPredicateSets.add(differentialFunctionBuilder.getOffset2SatisfiedDFs(i));
        }
    }

    public void build(HashMap<LongBitSet, Long> clueSet){
        for (var entry : clueSet.entrySet()) {
            LongBitSet clue = entry.getKey();
            MatchDF evi = new MatchDF(clue, entry.getValue(), offsetToPredicateSets);
            clueToEvidence.put(clue, evi);
            matchDFS.add(evi);
        }
    }

    // v2 using long
    public void buildFromLong(HashMap<Long, Long> clueSet){
        for(var entry: clueSet.entrySet()){
            long clue = entry.getKey();
            MatchDF evi = new MatchDF(clue, entry.getValue(), offsetToPredicateSets, ISNBuilder.bases);
            longClueToEvidence.put(clue, evi);
            matchDFS.add(evi);
        }
    }

    public int size() {
        return matchDFS.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        clueToEvidence.forEach((k, v) -> sb.append(k.toString() + "\t" + v + "\n"));
        return sb.toString();
    }

    @Override
    public Iterator<MatchDF> iterator() {
        return matchDFS.iterator();
    }

    public MatchDF getEvidenceById(int id){
        if(id >= matchDFS.size()){
            throw new IllegalArgumentException("No such evidence id {" + id + "} in evidence set");
        }
        return matchDFS.get(id);
    }

    public List<MatchDF> getEvidences(){
        return matchDFS;
    }
}
