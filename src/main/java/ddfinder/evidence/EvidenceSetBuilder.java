package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.longclueimpl.LongClueSetBuilder;
import ddfinder.evidence.longclueimpl.LongCrossClueSetBuilder;
import ddfinder.evidence.longclueimpl.LongSingleClueSetBuilder;
import ddfinder.evidence.offsetimpl.LinearCalOffset;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSetBuilder {
    private final EvidenceSet evidenceSet;

    public EvidenceSetBuilder(PredicateBuilder predicateBuilder) {
        ClueSetBuilder.configure(predicateBuilder);
        LongClueSetBuilder.configure(predicateBuilder);
        evidenceSet = new EvidenceSet(predicateBuilder);
    }

    public Set<LongBitSet> buildEvidenceSet(PliShard[] pliShards) {
        if (pliShards.length != 0) {
            long t1 = System.currentTimeMillis();
            HashMap<LongBitSet, Long> clueSet = linearBuildClueSet(pliShards);
            System.out.println("[ClueSet] build cost: " + (System.currentTimeMillis() - t1) + " ms");
            System.out.println("[ClueSet] # clueSet size: " + clueSet.size());
            evidenceSet.build(clueSet);
            return clueSet.keySet();
        }
        return null;
    }

    public Set<Long> buildEvidenceSetFromLongClue(PliShard[] pliShards){
        long t2 = System.currentTimeMillis();
        HashMap<Long, Long> longClueSet = buildLongClueSet(pliShards);
        System.out.println("[LongClueSet] build cost: " + (System.currentTimeMillis() - t2) + " ms");
        System.out.println("[LongClueSet] # clueSet size: " + longClueSet.size());
        evidenceSet.buildFromLong(longClueSet);
        return longClueSet.keySet();
    }

    public Set<LongBitSet> buildFullClueSet(PliShard[] pliShards) {
        if (pliShards.length != 1) {
            throw new IllegalCallerException("Only for fullPli");
        }
        long t1 = System.currentTimeMillis();
        ClueSetBuilder clueSetBuilder = new FullClueSetBuilder(pliShards[0]);
        HashMap<LongBitSet, Long> clueSet = clueSetBuilder.buildClueSet();
        System.out.println("[ClueSet] build cost: " + (System.currentTimeMillis() - t1) + " ms");
        System.out.println("[ClueSet] # clueSet size: " + clueSet.size());
        return clueSet.keySet();
    }

    public HashMap<LongBitSet, Long> buildClueContextSet(PliShard[] pliShards) {
        if (pliShards.length != 1) {
            throw new IllegalCallerException("Only for fullPli");
        }
        ClueSetBuilder clueSetBuilder = new ClueContextSetBuilder(pliShards[0]);
        HashMap<LongBitSet, Long> clueSet = clueSetBuilder.buildClueSet();//
        return clueSet;
    }


    //

    private HashMap<LongBitSet, Long> linearBuildClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashMap<LongBitSet, Long> clueSet = new HashMap<>();

        IClueOffset calUtils = new LinearCalOffset();
        System.out.println("[ClueOffset] Using Strategy: " + calUtils.getClass().getSimpleName());

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                ClueSetBuilder builder = i == j ? new SingleClueSetBuilder(pliShards[i],calUtils) : new CrossClueSetBuilder(pliShards[i], pliShards[j], calUtils);
                HashMap<LongBitSet, Long> partialClueSet = builder.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.merge(k, v, Long::sum));
            }
        }
        return clueSet;
    }


    public EvidenceSet getEvidenceSet() {
        return evidenceSet;
    }

    private HashMap<Long, Long> buildLongClueSet(PliShard[] pliShards) {

        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashMap<Long, Long> clueSet = new HashMap<>();

        IClueOffset calUtils = new LinearCalOffset();
        System.out.println("[LongClueOffset] Using Strategy: " + calUtils.getClass().getSimpleName());

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                LongClueSetBuilder builder = i == j ? new LongSingleClueSetBuilder(pliShards[i],calUtils) : new LongCrossClueSetBuilder(pliShards[i], pliShards[j], calUtils);
                HashMap<Long, Long> partialClueSet = builder.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.merge(k, v, Long::sum));
            }
        }
        return clueSet;
    }
}
