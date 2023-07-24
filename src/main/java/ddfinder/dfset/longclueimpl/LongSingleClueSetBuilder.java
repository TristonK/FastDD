package ddfinder.dfset.longclueimpl;

import ddfinder.dfset.IClueOffset;
import ddfinder.pli.Cluster;
import ddfinder.pli.DoublePli;
import ddfinder.pli.IPli;
import ddfinder.pli.PliShard;
import ddfinder.utils.DistanceCalculation;

import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class LongSingleClueSetBuilder extends LongClueSetBuilder {

    private final List<IPli> plis;
    private final int tidBeg, tidRange;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;
    private IClueOffset calUtils;

    public LongSingleClueSetBuilder(PliShard shard, IClueOffset calUtils) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * (tidRange - 1) / 2;
        this.calUtils = calUtils;
    }


    public HashMap<Long, Long> buildClueSet() {
        forwardClues = new long[evidenceCount];
        for (PredicatePack longPack : longPacks) {
            linerCorrectNum(plis.get(longPack.colIndex), longPack.base, longPack.thresholds);
        }
        for (PredicatePack doublePack : doublePacks) {
            linerCorrectNum(plis.get(doublePack.colIndex), doublePack.base, doublePack.thresholds);
        }
        for (PredicatePack strPack : strPacks) {
            correctStr(plis.get(strPack.colIndex), strPack.base, strPack.thresholds);
        }
        return accumulateClues(forwardClues);
    }


    public static long setMaskTimecnt = 0;
    private void setNumMask(Cluster cluster1, Cluster cluster2, long base, int offset) {
        long time1  = System.nanoTime();
        List<Integer> rawCluster1 = cluster1.getRawCluster();
        List<Integer> rawCluster2 = cluster2.getRawCluster();
        long diff = base * offset;
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
                forwardClues[(tid2 - tid1 - 1) + t1 * (2 * tidRange - t1 - 1) / 2] += diff;
            }
        }
        setMaskTimecnt += System.nanoTime()- time1;
    }

    private void setSelfNumMask(Cluster cluster, long base) {
        return;
    }

    public static long cntStrTime = 0;
    private void correctStr(IPli pli, long base, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            // index为0的情况
            setSelfNumMask(pli.get(i), base);
            for (int j = i + 1; j < pli.size(); j++) {
                long time1 = System.currentTimeMillis();
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
                cntStrTime += System.currentTimeMillis() - time1;
                setNumMask(pli.get(i), pli.get(j), base, c);
            }
        }
    }

    private void linerCorrectNum(IPli pli, long base, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            int[] offsets;
            if (pli.getClass() == DoublePli.class) {
                offsets = calUtils.countDouble(pli, 1, (Double[]) pli.getKeys(), i, (Double) pli.getKeys()[i], thresholds);
            } else {
                offsets = calUtils.countInt(pli, 1, (Long[])pli.getKeys(), i, (Long) pli.getKeys()[i], thresholds);
            }
            setSelfNumMask(pli.get(i), base);
            for (int j = i + 1; j < pli.size(); j++) {
                setNumMask(pli.get(i), pli.get(j), base, offsets[j - i]);
            }
        }
    }


}
