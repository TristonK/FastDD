package fastdd.dfset.longclueimpl;

import fastdd.Config;
import fastdd.dfset.Executor;
import fastdd.dfset.IClueOffset;
import fastdd.pli.DoublePli;
import fastdd.pli.IPli;
import fastdd.pli.PliShard;
import fastdd.utils.DistanceCalculation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author tristonK 2023/6/2
 */
public class LongCrossClueSetBuilder extends LongClueSetBuilder implements Runnable {
    private final List<IPli> plis1, plis2;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;//用来代替longbitset，实验测试
    private IClueOffset calUtils;

    public LongCrossClueSetBuilder(PliShard shard1, PliShard shard2, IClueOffset calUtils) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
        this.calUtils = calUtils;
    }

    public HashMap<Long, Long> buildClueSet() {
        forwardClues = new long[evidenceCount];   // plis1 -> plis2
        for (PredicatePack strPack : strPacks) {
            correctStr(plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.base, strPack.thresholds);
        }
        for (PredicatePack longPack : longPacks) {
            linerCorrectNum(plis1.get(longPack.colIndex), plis2.get(longPack.colIndex), longPack.base, longPack.thresholds);
        }
        for (PredicatePack numPack : doublePacks) {
            linerCorrectNum(plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.base, numPack.thresholds);
        }
        return accumulateClues(forwardClues);
    }

    //public static long setMaskTimeCnt = 0;
    private void setNumMask(IPli pli1, int i, IPli pli2, int j, long base, int offset) {
        //long time1 = System.nanoTime();
        int beg1 = pli1.getPliShard().beg, beg2 = pli2.getPliShard().beg;
        int range2 = pli2.getPliShard().end - beg2;
        long diff = base * offset;
        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                forwardClues[r1 + tid2] += diff;
            }
        }
        //setMaskTimeCnt += System.nanoTime() - time1;
    }

    //public static long cntStrTime = 0;
    private void correctStr(IPli pivotPli, IPli probePli, long base, List<Double> thresholds) {
        final String[] pivotKeys = (String[]) pivotPli.getKeys();
        final String[] probeKeys = (String[]) probePli.getKeys();
        for (int i = 0; i < pivotKeys.length; i++) {
            for (int j = 0; j < probeKeys.length; j++) {
                //long time1 = System.nanoTime();
                double diff;
                if(Config.TestMD){
                    diff = DistanceCalculation.MDLevenstheinDistance(pivotKeys[i], probeKeys[j]);
                }else{
                    diff = getLevenshteinDistance(pivotKeys[i], probeKeys[j]);
                    //diff = DistanceCalculation.StringDistance(pivotKeys[i], probeKeys[j]);
                }
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
                //cntStrTime += System.nanoTime() - time1;
                setNumMask(pivotPli, i, probePli, j, base, c);
            }
        }
    }

    private void linerCorrectNum(IPli pivotPli, IPli probePli, long base, List<Double> thresholds) {
        for (int i = 0; i < pivotPli.size(); i++) {
            int[] offsets;
            if (pivotPli.getClass() == DoublePli.class) {
                offsets = calUtils.countDouble(probePli, 0, (Double[]) probePli.getKeys(), 0, (Double) pivotPli.getKeys()[i], thresholds);
            } else {
                offsets = calUtils.countInt(probePli, 0, (Long[]) probePli.getKeys(), 0, (Long) pivotPli.getKeys()[i], thresholds);
            }
            for (int j = 0; j < probePli.size(); j++) {
                setNumMask(pivotPli, i, probePli, j, base, offsets[j]);
            }
        }
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
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            int minv1 = v1[0];

            // use formula to fill in the rest of the row
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

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        return v0[s2.length()];
    }

    @Override
    public void run(){
        HashMap<Long, Long> result = buildClueSet();
        result.forEach((k,v)-> Executor.res.merge(k,v, Long::sum));
    }
}

