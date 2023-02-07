package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import com.koloboke.function.LongLongConsumer;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;

import java.util.HashMap;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSetBuilder {
    private final EvidenceSet evidenceSet;
    public EvidenceSetBuilder(PredicateBuilder predicateBuilder){
        ClueSetBuilder.configure(predicateBuilder);
        evidenceSet = new EvidenceSet(predicateBuilder, ClueSetBuilder.colMap);
    }

    public EvidenceSet buildEvidenceSet(PliShard[] pliShards){
        if (pliShards.length != 0) {
            long t1 = System.currentTimeMillis();
            HashMap<LongBitSet, Long> clueSet = linearBuildClueSet(pliShards);
            System.out.println("[Time] build clueSet: " + (System.currentTimeMillis()-t1));
            //evidenceSet.build(clueSet);
        }
        return evidenceSet;
    }

    private HashMap<LongBitSet, Long> linearBuildClueSet(PliShard[] pliShards){
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashMap<LongBitSet, Long> clueSet = new HashMap<>();

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                ClueSetBuilder builder = i == j ? new SingleClueSetBuilder(pliShards[i]) : new CrossClueSetBuilder(pliShards[i], pliShards[j]);
                HashMap<LongBitSet, Long> partialClueSet = builder.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.put(k, clueSet.getOrDefault(k,0L) + 1));
            }
        }
        System.out.println(" [CLUE] # of clueSet size: " + clueSet.size());
        return clueSet;
    }


    private int getLevenshteinDistance(String s1, String s2){
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if (s1.equals(s2)) {
            return 0;
        }

        if (s1.length() == 0) {
            return s2.length();
        }

        if (s2.length() == 0) {
            return s1.length();
        }
        // create two work vectors of integer distances
        int[] v0 = new int[s2.length() + 1];
        int[] v1 = new int[s2.length() + 1];
        int[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s1.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            int minv1 = v1[0];

            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.length(); j++) {
                int cost = 1;
                if (s1.charAt(i) == s2.charAt(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(
                        v1[j] + 1,              // Cost of insertion
                        Math.min(
                                v0[j + 1] + 1,  // Cost of remove
                                v0[j] + cost)); // Cost of substitution

                minv1 = Math.min(minv1, v1[j + 1]);
            }

            if (minv1 >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        return v0[s2.length()];
    }

    private int getQGramDistance(String a, String b){
        return 0;
    }

    public EvidenceSet getEvidenceSet(){return evidenceSet;}
}
