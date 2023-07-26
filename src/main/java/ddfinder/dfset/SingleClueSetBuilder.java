package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.*;
import ddfinder.differentialfunction.DifferentialFunctionBuilder;
import ddfinder.utils.DistanceCalculation;

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
    private long[] forwardClues;
    private long[] bases;
    private IClueOffset calUtils;

    public SingleClueSetBuilder(PliShard shard, IClueOffset calUtils) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * (tidRange - 1) / 2;
        this.calUtils = calUtils;
    }


    public HashMap<LongBitSet, Long> buildClueSet() {
        LongBitSet[] clues = new LongBitSet[evidenceCount];
        for (int i = 0; i < evidenceCount; i++) {
            clues[i] = new LongBitSet(DifferentialFunctionBuilder.getIntervalCnt());
        }
        for (PredicatePack intPack : intPacks) {
            linerCorrectNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
//            correctNum(clues, plis.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack doublePack : doublePacks) {
            linerCorrectNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
//            correctNum(clues, plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds);
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
                int diff = DistanceCalculation.StringDistance((String) pli.getKeys()[i], (String) pli.getKeys()[j]);
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
            //接口实现
            int start = i + 1;
            if (pli.getClass() == DoublePli.class) {
                Double key = (Double) pli.getKeys()[i];//获取pli的第i个key值
                for (int index = 1; index < thresholds.size() && start < pli.size(); index++) {
                    int end = pli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
                    for (int correct = start; correct < end && correct < pli.size(); correct++) {//correct对应从目标key开始往后的下标，循环遍历完之后也就是对应的j
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
                offsets = calUtils.countDouble(pli, 1, (Double[]) pli.getKeys(), i, (Double) pli.getKeys()[i], thresholds);
            } else {
                offsets = calUtils.countInt(pli, 1, (Long[]) pli.getKeys(), i, (Long) pli.getKeys()[i], thresholds);
            }
            setSelfNumMask(clues, pli.get(i), pos);
            for (int j = i + 1; j < pli.size(); j++) {
                setNumMask(clues, pli.get(i), pli.get(j), pos + offsets[j - i]);//i是当前的目标key下标，j是在其之后的所有key,j-i代表offset数组中的下标（i对应0）
            }
        }
    }


}
