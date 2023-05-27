package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.ClueSetBuilder;
import ddfinder.pli.Cluster;
import ddfinder.pli.DoublePli;
import ddfinder.pli.IPli;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To build the clue set of two Pli shards
 */
public class CrossClueSetBuilder extends ClueSetBuilder {

    private final List<IPli> plis1, plis2;
    private final int evidenceCount;

    private final double ERR = 0.000000001;

    public CrossClueSetBuilder(PliShard shard1, PliShard shard2) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
    }

    public HashMap<LongBitSet, Long> buildClueSet() {

        //TODO；改造成暴力

        LongBitSet[] forwardClues = new LongBitSet[evidenceCount];   // plis1 -> plis2
        for(int i = 0; i < evidenceCount; i++){
            forwardClues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for(PredicatePack strPack: strPacks){
            correctStr(forwardClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        for (PredicatePack intPack: intPacks){
            //linerCorrectNum(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            correctInteger(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack numPack: doublePacks){
            //linerCorrectNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
            correctNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
        }


        return accumulateClues(forwardClues);
    }

    public HashMap<LongBitSet, Long> linearBuildClueSet() {
        LongBitSet[] forwardClues = new LongBitSet[evidenceCount];   // plis1 -> plis2
        for(int i = 0; i < evidenceCount; i++){
            forwardClues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for(PredicatePack strPack: strPacks){
            correctStr(forwardClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        for (PredicatePack intPack: intPacks){
            linerCorrectNum(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            //correctInteger(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack numPack: doublePacks){
            linerCorrectNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
            //correctNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
        }


        return accumulateClues(forwardClues);
    }

    public HashMap<LongBitSet, Long> binaryBuildClueSet() {
        LongBitSet[] forwardClues = new LongBitSet[evidenceCount];   // plis1 -> plis2
        for(int i = 0; i < evidenceCount; i++){
            forwardClues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for(PredicatePack strPack: strPacks){
            correctStr(forwardClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        for (PredicatePack intPack: intPacks){
            //linerCorrectNum(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            correctInteger(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack numPack: doublePacks){
            //linerCorrectNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
            correctNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
        }


        return accumulateClues(forwardClues);
    }

    static public long timeCnt = 0;

    private void setNumMask(LongBitSet[] clues1, IPli pli1, int i, IPli pli2, int j, int pos) {

        int beg1 = pli1.getPliShard().beg, beg2 = pli2.getPliShard().beg;
        int range2 = pli2.getPliShard().end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues1[r1 + tid2].set(pos);
                //clues2[(tid2 - beg2) * range1 + t1].set(pos);
            }
        }

    }

    private void correctStr(LongBitSet[] clues1, IPli pivotPli, IPli probePli, int pos, List<Double>thresholds) {
        final String[] pivotKeys = (String[]) pivotPli.getKeys();
        final String[] probeKeys = (String[]) probePli.getKeys();
        for(int i = 0; i < pivotKeys.length; i++){
            for(int j = 0; j < probeKeys.length; j++){
               /* String smallOne = pivotKeys[i], biggerOne = probeKeys[j];
                if(smallOne.compareTo(biggerOne) > 0){
                    String tmp = biggerOne;
                    biggerOne = smallOne;
                    smallOne = tmp;
                }
                if(ClueSetBuilder.stringDistance.containsKey(smallOne) && ClueSetBuilder.stringDistance.get(smallOne).containsKey(biggerOne)){
                    setNumMask(clues1, pivotPli, i, probePli, j, pos +  ClueSetBuilder.stringDistance.get(smallOne).get(biggerOne));
                    continue;
                }*/
                int diff = StringCalculation.getDistance(pivotKeys[i], probeKeys[j]);
                int c = 0;
                if(diff < ERR + thresholds.get(0)){
                    c = 0;
                } else if (diff > ERR + thresholds.get(thresholds.size()-1)) {
                    c = thresholds.size();
                }else{
                    while(c < thresholds.size()-1){
                        if(diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c+1)){
                            c++;
                            break;
                        }
                        c++;
                    }
                }
                setNumMask(clues1, pivotPli, i, probePli, j, pos + c);
                /*ConcurrentHashMap<String, Integer> newMap = stringDistance.getOrDefault(smallOne, new ConcurrentHashMap<String, Integer>());
                newMap.put(biggerOne, c);
                stringDistance.put(smallOne, newMap);*/
            }
        }
    }

    private void correctNum(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double>thresholds) {
        final Double[] pivotKeys = (Double[]) pivotPli.getKeys();
        final Double[] probeKeys = (Double[]) probePli.getKeys();

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
            if(Math.abs(probeKeys[start] - pivotKeys[i]) < ERR){
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
    private void correctInteger(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double>thresholds) {
        final Integer[] pivotKeys = (Integer[]) pivotPli.getKeys();
        final Integer[] probeKeys = (Integer[]) probePli.getKeys();

        for(int i = 0; i < pivotKeys.length; i++){
            int start = 0;
            for(int index = thresholds.size()-1; index >= 0 && start < probeKeys.length; index--){
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i]+thresholds.get(index).intValue(), start, 0);
                for(int j = start; j < end; j++){
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);
                }
                start = end;
            }
            if(start >= probeKeys.length){
                continue;
            }
            if(probeKeys[start].equals(pivotKeys[i])){
                setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
                start ++;
            }
            for(int index = 1; index < thresholds.size() && start < probeKeys.length; index++){
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i]-thresholds.get(index).intValue(), start, 1);
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
        }
    }

    private void linerCorrectNum(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double>thresholds){
        for(int i = 0; i < pivotPli.size(); i++){
            int[] offsets;
            if(pivotPli.getClass() == DoublePli.class){
                offsets = linerCountDouble((Double[]) probePli.getKeys(), 0 , (Double) pivotPli.getKeys()[i], thresholds);
            }else{
                offsets = linerCountInt((Integer[]) probePli.getKeys(), 0, (Integer) pivotPli.getKeys()[i], thresholds);
            }
            for(int j = 0; j < probePli.size(); j++){
                setNumMask(forwardArray, pivotPli, i, probePli, j, pos + offsets[j]);
            }
        }
    }

}