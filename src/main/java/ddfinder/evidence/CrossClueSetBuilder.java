package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
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

    static public long timeCnt = 0;

    private void setNumMask(LongBitSet[] clues1, Pli pli1, int i, Pli pli2, int j, int pos) {

        int beg1 = pli1.pliShard.beg, beg2 = pli2.pliShard.beg;
        int range2 = pli2.pliShard.end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
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
            int start = 0;
            for(int index = thresholds.size()-1; index >= 0 && start < probeKeys.length; index--){
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i]+thresholds.get(index), start, 0);
                for(int j = start; j < end; j++){
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);
                }
                start = end;
            }
            if(start >= probeKeys.length){
                continue;
            }
            if(probeKeys[start] == pivotKeys[i]){
                setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
                start ++;
            }
            for(int index = 1; index < thresholds.size() && start < probeKeys.length; index++){
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i]-thresholds.get(index), start, 1);
                for(int j = start; j < end; j++){
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index);
                }
                start = end;
            }
            if(start < probeKeys.length){
                for(int j = start; j < probeKeys.length; j++){
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                }
            }

//            int thresholdIndexl = 1;
//            int thresholdIndexb = thresholds.size()-1;
//            for(int j = 0; j < probeKeys.length; j++){
//                if(probeKeys[j] == pivotKeys[i]){
//                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos);
//                }
//                else if(probeKeys[j] < pivotKeys[i]){
//                    while(thresholdIndexl < thresholds.size() && pivotKeys[i] - probeKeys[j] > thresholds.get(thresholdIndexl)){
//                        thresholdIndexl ++;
//                    }
//                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos+thresholdIndexl);
//                }else{
//                    while (thresholdIndexb > 0 && probeKeys[j] - pivotKeys[i] <= thresholds.get(thresholdIndexb)){
//                        thresholdIndexb--;
//                    }
//                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos+thresholdIndexb+1);
//                }
//            }


        }
    }

}