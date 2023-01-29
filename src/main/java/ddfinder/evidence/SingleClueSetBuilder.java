package ddfinder.evidence;

import com.koloboke.collect.map.hash.HashLongLongMap;
import ddfinder.pli.Cluster;
import ddfinder.pli.Pli;
import ddfinder.pli.PliShard;

import java.util.List;

/**
 * To build the clue set of one Pli shard
 */
public class SingleClueSetBuilder extends ClueSetBuilder {

    private final List<Pli> plis;
    private final int tidBeg, tidRange;
    private final int evidenceCount;

    public SingleClueSetBuilder(PliShard shard) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * tidRange;
    }

    public HashLongLongMap buildClueSet() {
        long[] clues = new long[evidenceCount];

        for(PredicatePack numPack: numPacks){
            correctNum(clues, plis.get(numPack.colIndex), numPack.masks, numPack.thresholds);
        }

        for(PredicatePack strPack : strPacks){
            correctStr(clues, plis.get(strPack.colIndex), strPack.masks);
        }

        HashLongLongMap clueSet = accumulateClues(clues);

        if (0L == clueSet.addValue(0L, -tidRange))     // remove reflex evidence
        {
            clueSet.remove(0L);
        }

        return clueSet;
    }

    private void setNumMask(long[] clues,Cluster cluster1, Cluster cluster2, long mask) {
        List<Integer> rawCluster1 = cluster1.getRawCluster();
        List<Integer> rawCluster2 = cluster2.getRawCluster();

        for (int i = 0; i < rawCluster1.size() ; i++) {
            int t1 = rawCluster1.get(i) - tidBeg, r1 = t1 * tidRange;
            for (int j = 0; j < rawCluster2.size(); j++) {
                int tid2 = rawCluster2.get(j) - tidBeg;
                clues[r1 + tid2] |= mask;               // (cluster.get(i)-tidBeg)*tidRange + (cluster.get(j)-tidBeg)
                clues[tid2 * tidRange + t1] |= mask;    // (cluster.get(j)-tidBeg)*tidRange + (cluster.get(i)-tidBeg)
            }
        }
    }

    private void correctStr(long[] clues, Pli pli, List<Long>masks) {
        //TODO
    }

    private void correctNum(long[] clues, Pli pli, List<Long> masks, List<Double>thresholds) {
        for (int i = 0; i < pli.size() - 1; i++) {
            // index为0的默认为0，不需要修改
            int thresholdIndex = 1;
            for(int j = i + 1; j < pli.size(); j++){
                Cluster probeCluster = pli.get(j);
                while(thresholdIndex < thresholds.size() && Math.abs(pli.keys[j] - pli.keys[i]) > thresholds.get(thresholdIndex)){
                    thresholdIndex ++;
                }
                setNumMask(clues, pli.get(i), pli.get(j), masks.get(thresholdIndex));
            }
        }
    }

}
