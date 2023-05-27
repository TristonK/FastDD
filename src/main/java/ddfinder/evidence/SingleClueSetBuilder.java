package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.ClueSetBuilder;
import ddfinder.pli.*;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To build the clue set of one Pli shard
 */
public class SingleClueSetBuilder extends ClueSetBuilder {

    private final List<IPli> plis;
    private final int tidBeg, tidRange;
    private final int evidenceCount;

    private final double ERR = 0.000000001;

    public SingleClueSetBuilder(PliShard shard) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * (tidRange - 1) / 2;
    }


    public HashMap<LongBitSet, Long> buildClueSet() {


        LongBitSet[] clues = new LongBitSet[evidenceCount];
//        for(int i = 0; i < evidenceCount; i++){
//            clues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
//        }
//        for(PredicatePack intPack: intPacks){
//            //linerCorrectNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
//            correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
//        }
//        for(PredicatePack doublePack: doublePacks){
//            //linerCorrectNum(clues,  plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
//            correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
//        }
//        for(PredicatePack strPack : strPacks){
//            correctStr(clues, plis.get(strPack.colIndex), strPack.pos, strPack.thresholds);
//        }

        for (int i = 0; i < evidenceCount; i++) {
            clues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for (PredicatePack intPack : intPacks) {
            //linerCorrectNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack doublePack : doublePacks) {
            //linerCorrectNum(clues,  plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
            correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
        }
        for (PredicatePack strPack : strPacks) {
            correctStr(clues, plis.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        return accumulateClues(clues);
    }

    public HashMap<LongBitSet, Long> linearBuildClueSet() {
        LongBitSet[] clues = new LongBitSet[evidenceCount];
        for (int i = 0; i < evidenceCount; i++) {
            clues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for (PredicatePack intPack : intPacks) {
            linerCorrectNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            //correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack doublePack : doublePacks) {
            linerCorrectNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
            //correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
        }
        for (PredicatePack strPack : strPacks) {
            correctStr(clues, plis.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        return accumulateClues(clues);
    }

    public HashMap<LongBitSet, Long> binaryBuildClueSet() {
        LongBitSet[] clues = new LongBitSet[evidenceCount];//确定clue的个数
        for (int i = 0; i < evidenceCount; i++) {
            clues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for (PredicatePack intPack : intPacks) {
            //linerCorrectNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
            correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack doublePack : doublePacks) {
            //linerCorrectNum(clues,  plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
            correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
        }
        for (PredicatePack strPack : strPacks) {
            correctStr(clues, plis.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        return accumulateClues(clues);
    }

    private void setNumMask(LongBitSet[] clues, Cluster cluster1, Cluster cluster2, int pos) {
        List<Integer> rawCluster1 = cluster1.getRawCluster();
        List<Integer> rawCluster2 = cluster2.getRawCluster();

        for (Integer value : rawCluster1) {
            for (Integer integer : rawCluster2) {
                int tid1 = value;
                int tid2 = integer;
                if (tid2 < tid1) {
                    int temp = tid1;
                    tid1 = tid2;
                    tid2 = temp;
                }
                int t1 = tid1 - tidBeg;
                clues[(tid2 - tid1 - 1) + t1 * (2 * tidRange - t1 - 1) / 2].set(pos);
            }
        }
    }

    private void setSelfNumMask(LongBitSet[] clues, Cluster cluster, int pos) {
        for (int q = 0; q < cluster.size() - 1; q++) {
            int tid1 = cluster.get(q);
            for (int w = q + 1; w < cluster.size(); w++) {
                int tid2 = cluster.get(w);
                if (tid2 < tid1) {
                    int temp = tid1;
                    tid1 = tid2;
                    tid2 = temp;
                }
                int t1 = tid1 - tidBeg;
                clues[(tid2 - tid1 - 1) + t1 * (2 * tidRange - t1 - 1) / 2].set(pos);
            }
        }
    }

    private void correctStr(LongBitSet[] clues, IPli pli, int pos, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            // index为0的情况
            setSelfNumMask(clues, pli.get(i), pos);
            for (int j = i + 1; j < pli.size(); j++) {
                /*String smallOne = (String) pli.getKeys()[i], biggerOne = (String) pli.getKeys()[j];
                if(smallOne.compareTo(biggerOne) > 0){
                    String tmp = biggerOne;
                    biggerOne = smallOne;
                    smallOne = tmp;
                }
                if(ClueSetBuilder.stringDistance.containsKey(smallOne) && ClueSetBuilder.stringDistance.get(smallOne).containsKey(biggerOne)){
                    setNumMask(clues, pli.get(i), pli.get(j), pos + ClueSetBuilder.stringDistance.get(smallOne).get(biggerOne));
                    continue;
                }*/
                int diff = StringCalculation.getDistance((String) pli.getKeys()[i], (String) pli.getKeys()[j]);
                int c = 0;
                if (diff < ERR + thresholds.get(0)) {
                    c = 0;
                } else if (diff > ERR + thresholds.get(thresholds.size() - 1)) {
                    c = thresholds.size();
                } else {
                    while (c < thresholds.size() - 1) {
                        if (diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c + 1)) {
                            c++;
                            break;
                        }
                        c++;
                    }
                }
                setNumMask(clues, pli.get(i), pli.get(j), pos + c);
                //ConcurrentHashMap<String, Integer> newMap = stringDistance.getOrDefault(smallOne, new ConcurrentHashMap<String, Integer>());
                //newMap.put(biggerOne, c);
                //stringDistance.put(smallOne, newMap);
            }
        }
    }

    private void correctNum(LongBitSet[] clues, IPli pli, int pos, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            // index为0的情况
            setSelfNumMask(clues, pli.get(i), pos);
            int start = i + 1;
            if (pli.getClass() == DoublePli.class) {
                Double key = (Double) pli.getKeys()[i];
                for (int index = 1; index < thresholds.size() && start < pli.size(); index++) {
                    int end = pli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
                    for (int correct = start; correct < end && correct < pli.size(); correct++) {
                        setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    }
                    start = end;
                }
            } else {
                Integer key = (Integer) pli.getKeys()[i];
                for (int index = 1; index < thresholds.size() && start < pli.size(); index++) {
                    int end = pli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).intValue(), start, 1);
                    for (int correct = start; correct < end && correct < pli.size(); correct++) {
                        setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    }
                    start = end;
                }
            }

            for (int correct = start; correct < pli.size(); correct++) {
                setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
            }

        }
    }

    private void linerCorrectNum(LongBitSet[] clues, IPli pli, int pos, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            int[] offsets;
            if (pli.getClass() == DoublePli.class) {
                offsets = linerCountDouble((Double[]) pli.getKeys(), i, (Double) pli.getKeys()[i], thresholds);
            } else {
                offsets = linerCountInt((Integer[]) pli.getKeys(), i, (Integer) pli.getKeys()[i], thresholds);
            }
            setSelfNumMask(clues, pli.get(i), pos);
            for (int j = i + 1; j < pli.size(); j++) {
                setNumMask(clues, pli.get(i), pli.get(j), pos + offsets[j - i]);
            }
        }
    }

    //TODO: 改造bruteforce适应plishard形式
//    private void brutalCount(LongBitSet[] clues, IPli pli, int pos, List<Double> thresholds) {
//        Object[] keys = pli.getKeys();
//        if (keys instanceof Integer[]) {
//            Integer[] key = (Integer[]) keys;
//            for (int i = 0; i < pli.size() - 1; i++) {//遍历某个属性的plishard的key值，用来做差
//                for (int j = i + 1; j < pli.size(); j++) {
//                    double diff = Math.abs(key[i] - key[j]);
//                    clue.set(findMaskPos(diff, thresholds) + pos);
//                }
//            }
//        }
//        else if (keys instanceof Double[]) {
//            Double[] key = (Double[]) keys;
//            for (int i = 0; i < pli.size() - 1; i++) {//遍历某个属性的plishard的key值，用来做差
//                for (int j = i + 1; j < pli.size(); j++) {
//                    double diff = Math.abs(key[i] - key[j]);
//                    clue.set(findMaskPos(diff, thresholds) + pos);
//                }
//            }
//        }
//        else{
//            String[] key = (String[]) keys;
//            for (int i = 0; i < pli.size() - 1; i++) {//遍历某个属性的plishard的key值，用来做差
//                for (int j = i + 1; j < pli.size(); j++) {
//                    double diff = StringCalculation.getDistance(key[i], key[j]);
//                    clue.set(findMaskPos(diff, thresholds) + pos);
//                }
//            }
//        }
//
//
//
//    }

    private int findMaskPos(double diff, List<Double> th) {//找到元组差值在阈值列表中的位置
        int c = 0;
        if (diff < th.get(0) + ERR) {
            c = 0;
        } else if (diff > th.get(th.size() - 1) + ERR) {
            c = th.size();
        } else {
            while (c < th.size() - 1) {
                if (diff > th.get(c) + ERR && diff < th.get(c + 1) + ERR) {
                    c++;
                    break;
                }
                c++;
            }
        }
        return c;
    }


}
