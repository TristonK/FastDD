package fastdd.dfset.isnimpl;

import fastdd.dfset.IOffset;
import fastdd.pli.Cluster;
import fastdd.pli.DoublePli;
import fastdd.pli.IPli;
import fastdd.pli.PliShard;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author tristonK 2023/6/2
 */
public class SingleISNBuilder extends ISNBuilder implements Callable<HashMap<Long, Long>> {

    private final List<IPli> plis;
    private final int tidBeg, tidRange;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;
    private IOffset calUtils;

    public SingleISNBuilder(PliShard shard, IOffset calUtils) {
        plis = shard.plis;
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        evidenceCount = tidRange * (tidRange - 1) / 2;
        this.calUtils = calUtils;
    }


    public HashMap<Long, Long> buildClueSet() {
        forwardClues = new long[evidenceCount];
        for (DFPack longPack : longPacks) {
            linerCorrectNum(plis.get(longPack.colIndex), longPack.base, longPack.thresholds);
        }
        for (DFPack doublePack : doublePacks) {
            linerCorrectNum(plis.get(doublePack.colIndex), doublePack.base, doublePack.thresholds);
        }
        for (DFPack strPack : strPacks) {
            correctStr(plis.get(strPack.colIndex), strPack.base, strPack.thresholds);
        }
        return accumulateClues(forwardClues);
    }

    private void setNumMask(Cluster cluster1, Cluster cluster2, long base, int offset) {
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
    }

    private void setSelfNumMask(Cluster cluster, long base) {
        return;
    }

    private void correctStr(IPli pli, long base, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            setSelfNumMask(pli.get(i), base);
            for (int j = i + 1; j < pli.size(); j++) {
                double diff;
                long t1 = System.nanoTime();
                diff = getLevenshteinDistance((String) pli.getKeys()[i], (String) pli.getKeys()[j]);
                calDiffTime += (System.nanoTime() - t1);
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
                setNumMask(pli.get(i), pli.get(j), base, c);
            }
        }
    }

    private void linerCorrectNum(IPli pli, long base, List<Double> thresholds) {
        for (int i = 0; i < pli.size(); i++) {
            int[] offsets;
            long t1 = System.nanoTime();
            if (pli.getClass() == DoublePli.class) {
                offsets = calUtils.countDouble(pli, 1, (Double[]) pli.getKeys(), i, (Double) pli.getKeys()[i], thresholds);
            } else {
                offsets = calUtils.countInt(pli, 1, (Long[])pli.getKeys(), i, (Long) pli.getKeys()[i], thresholds);
            }
            calDiffTime += (System.nanoTime() - t1);
            setSelfNumMask(pli.get(i), base);
            for (int j = i + 1; j < pli.size(); j++) {
                setNumMask(pli.get(i), pli.get(j), base, offsets[j - i]);
            }
        }
    }

    @Override
    public HashMap<Long, Long> call() throws Exception {
        return buildClueSet();
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
            v1[0] = i + 1;
            int minv1 = v1[0];
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
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;
        }
        return v0[s2.length()];
    }
}
