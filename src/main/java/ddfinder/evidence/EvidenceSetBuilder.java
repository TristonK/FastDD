package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;

import java.util.HashMap;
import java.util.Set;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSetBuilder {
    private final EvidenceSet evidenceSet;
    public EvidenceSetBuilder(PredicateBuilder predicateBuilder){
        ClueSetBuilder.configure(predicateBuilder);
        evidenceSet = new EvidenceSet(predicateBuilder, ClueSetBuilder.colMap);
    }

    public Set<LongBitSet> buildEvidenceSet(PliShard[] pliShards){
        if (pliShards.length != 0) {
            long t1 = System.currentTimeMillis();
            HashMap<LongBitSet, Long> clueSet = linearBuildClueSet(pliShards);
            System.out.println("[ClueSet] build cost: " + (System.currentTimeMillis()-t1) + " ms");
            System.out.println("[ClueSet] # clueSet size: " + clueSet.size());
            return clueSet.keySet();
            //evidenceSet.build(clueSet);
        }
        return null;
    }

    private HashMap<LongBitSet, Long> linearBuildClueSet(PliShard[] pliShards){
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashMap<LongBitSet, Long> clueSet = new HashMap<>();
        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                ClueSetBuilder builder = i == j ? new SingleClueSetBuilder(pliShards[i]) : new CrossClueSetBuilder(pliShards[i], pliShards[j]);
                HashMap<LongBitSet, Long> partialClueSet = builder.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.merge(k, v, Long::sum));
            }
        }
        return clueSet;
    }



    public EvidenceSet getEvidenceSet(){return evidenceSet;}
}
