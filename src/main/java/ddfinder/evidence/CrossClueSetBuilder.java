package ddfinder.evidence;

import com.koloboke.collect.map.hash.HashLongLongMap;
import ddfinder.pli.Pli;
import ddfinder.pli.PliShard;

import java.util.List;

/**
 * To build the clue set of two Pli shards
 */
public class CrossClueSetBuilder extends ClueSetBuilder {

    private final List<Pli> plis1, plis2;
    private final int evidenceCount;

    public CrossClueSetBuilder(PliShard shard1, PliShard shard2) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
    }

    public HashLongLongMap buildClueSet() {
        long[] forwardClues = new long[evidenceCount];   // plis1 -> plis2
        long[] reverseClues = new long[evidenceCount];   // plis2 -> plis1

        for(PredicatePack strPack: strPacks){
            correctStr(forwardClues, reverseClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.masks);
        }
        for (PredicatePack numPack: numPacks){
            correctNum(forwardClues, reverseClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.masks, numPack.thresholds);
        }

        return accumulateClues(forwardClues, reverseClues);
    }

    private void setNumMask(long[] clues1, long[] clues2, Pli pli1, int i, Pli pli2, int j, long mask) {
        int beg1 = pli1.pliShard.beg, range1 = pli1.pliShard.end - beg1;
        int beg2 = pli2.pliShard.beg, range2 = pli2.pliShard.end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1, r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues1[r1 + tid2] |= mask;
                clues2[(tid2 - beg2) * range1 + t1] |= mask;
            }
        }
    }

    private void correctStr(long[] clues1, long[] clues2, Pli pivotPli, Pli probePli, List<Long> masks) {
        //TODO
    }


    private void correctNum(long[] forwardArray, long[] reverseArray, Pli pivotPli, Pli probePli, List<Long> masks, List<Double>thresholds) {
        final double[] pivotKeys = pivotPli.getKeys();
        final double[] probeKeys = probePli.getKeys();

        for(int i = 0; i < pivotKeys.length; i++){
            int thresholdIndex = 1;
            for(int j = 0; j < probeKeys.length; j++){
                if(probeKeys[j] == pivotKeys[i]){continue;}
                while(thresholdIndex < thresholds.size() && Math.abs(probeKeys[j] - pivotKeys[i]) > thresholds.get(thresholdIndex)){
                    thresholdIndex ++;
                }
                setNumMask(forwardArray, reverseArray, pivotPli, i, probePli, j, masks.get(thresholdIndex));
            }
        }
    }

}