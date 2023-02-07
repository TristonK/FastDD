package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import ddfinder.pli.Pli;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;

import java.util.HashMap;
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

    public HashMap<LongBitSet, Long> buildClueSet() {
        LongBitSet[] forwardClues = new LongBitSet[evidenceCount];   // plis1 -> plis2
        for(int i = 0; i < evidenceCount; i++){
            forwardClues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }

        for(PredicatePack strPack: strPacks){
            correctStr(forwardClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.pos);
        }
        for (PredicatePack numPack: numPacks){
            correctNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
        }

        return accumulateClues(forwardClues);
    }

    private void setNumMask(LongBitSet[] clues1, Pli pli1, int i, Pli pli2, int j, int pos) {
        int beg1 = pli1.pliShard.beg;
        int beg2 = pli2.pliShard.beg, range2 = pli2.pliShard.end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1, r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues1[r1 + tid2].set(pos);
                //clues2[(tid2 - beg2) * range1 + t1].set(pos);
            }
        }
    }

    private void correctStr(LongBitSet[] clues1, Pli pivotPli, Pli probePli, int pos) {
        //TODO
    }


    private void correctNum(LongBitSet[] forwardArray, Pli pivotPli, Pli probePli, int pos, List<Double>thresholds) {
        final double[] pivotKeys = pivotPli.getKeys();
        final double[] probeKeys = probePli.getKeys();

        for(int i = 0; i < pivotKeys.length; i++){
            int thresholdIndex = 1;
            for(int j = 0; j < probeKeys.length; j++){
                if(probeKeys[j] == pivotKeys[i]){continue;}
                while(thresholdIndex < thresholds.size() && Math.abs(probeKeys[j] - pivotKeys[i]) > thresholds.get(thresholdIndex)){
                    thresholdIndex ++;
                }
                setNumMask(forwardArray, pivotPli, i, probePli, j, pos+thresholdIndex);
            }
        }
    }

}