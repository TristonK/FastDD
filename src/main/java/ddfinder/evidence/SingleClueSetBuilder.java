package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.Cluster;
import ddfinder.pli.IPli;
import ddfinder.pli.DoublePli;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        for(int i = 0; i < evidenceCount; i++){
            clues[i] = new LongBitSet(PredicateBuilder.getIntervalCnt());
        }
        for(PredicatePack intPack: intPacks){
            correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for(PredicatePack doublePack: doublePacks){
            correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
        }
        for(PredicatePack strPack : strPacks){
            correctStr(clues, plis.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        return accumulateClues(clues);
    }

    private void setNumMask(LongBitSet[] clues,Cluster cluster1, Cluster cluster2, int pos) {
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

    private void correctStr(LongBitSet[] clues, IPli pli, int pos, List<Double>thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            // index为0的情况
            Cluster pli1 = pli.get(i);
            for (int q = 0; q < pli1.size() -1; q++) {
                int tid1 = pli1.get(q);
                for (int w = q + 1; w < pli1.size(); w++) {
                    int tid2 = pli1.get(w);
                    if(tid2 < tid1){int temp = tid1; tid1 = tid2; tid2 = temp;}
                    int t1 = tid1 - tidBeg;
                    clues[(tid2-tid1-1)+t1*(2*tidRange- t1 -1)/2].set(pos);
                }
            }
            for(int j = i + 1; j < pli.size(); j++){
                int diff = StringCalculation.getDistance((String) pli.getKeys()[i], (String) pli.getKeys()[j]);
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
                setNumMask(clues, pli.get(i), pli.get(j), pos + c);
            }
        }
    }

    private void correctNum(LongBitSet[] clues, IPli pli, int pos, List<Double>thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            // index为0的情况
            Cluster pli1 = pli.get(i);
            for (int q = 0; q < pli1.size() -1; q++) {
                int tid1 = pli1.get(q);
                for (int w = q + 1; w < pli1.size(); w++) {
                    int tid2 = pli1.get(w);
                    if(tid2 < tid1){int temp = tid1; tid1 = tid2; tid2 = temp;}
                    int t1 = tid1 - tidBeg;
                    clues[(tid2-tid1-1)+t1*(2*tidRange- t1 -1)/2].set(pos);
                }
            }
            int start = i+1;
            if(pli.getClass() == DoublePli.class){
                Double key = (Double)pli.getKeys()[i];
                for(int index = 1; index < thresholds.size() && start < pli.size(); index++){
                    int end = pli.getFirstIndexWhereKeyIsLT(key-thresholds.get(index), start, 1);
                    for(int correct = start ; correct < end && correct < pli.size(); correct++){
                        setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    }
                    start = end;
                }
            }else {
                Integer key = (Integer) pli.getKeys()[i];
                for(int index = 1; index < thresholds.size() && start < pli.size(); index++){
                    int end = pli.getFirstIndexWhereKeyIsLT(key-thresholds.get(index).intValue(), start, 1);
                    for(int correct = start ; correct < end && correct < pli.size(); correct++){
                        setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    }
                    start = end;
                }
            }

            for(int correct = start; correct < pli.size(); correct++){
                setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
            }

//            int thresholdIndex = 1;
//            for(int j = i + 1; j < pli.size(); j++){
//                while(thresholdIndex < thresholds.size() && pli.keys[i] - pli.keys[j] > thresholds.get(thresholdIndex)){
//                    thresholdIndex ++;
//                }
//                setNumMask(clues, pli.get(i), pli.get(j), pos + thresholdIndex);
//            }
        }
    }

}
