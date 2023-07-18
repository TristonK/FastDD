package ddfinder.evidence.offsetimpl;

import ddfinder.evidence.IClueOffset;
import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class BinaryCalOffset implements IClueOffset {
    public static long cntTime = 0;

    @Override
    public int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds) {
        long time1 = System.nanoTime();
        int[] posTothreshold = new int[probePli.size()];
        //SinglePli
        if(1 == isSingle){
            int start = startPos + 1;
            for (int index = 1; index < thresholds.size() && start < probePli.size(); index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
                for (int correct = start; correct < end && correct < probePli.size(); correct++) {
                    posTothreshold[correct - startPos] = index;
                }
                start = end;
            }

            for (int correct = start; correct < probePli.size(); correct++) {
                posTothreshold[correct - startPos] = thresholds.size();
            }
        }
        //CrossPli
        else{
            final Double[] probeKeys = (Double[]) probePli.getKeys();
            int start = 0;
            for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key + thresholds.get(index), start, 0);
                for (int j = start; j < end; j++) {
                    posTothreshold[j] = index + 1;
                }
                start = end;
            }
            if (start >= probeKeys.length) {
                return posTothreshold;
            }
            // = key
            if (Math.abs(probeKeys[start] - key) < ERR) {
                posTothreshold[start] = 0;
                start++;
            }
            for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
                for (int j = start; j < end; j++) {
                    posTothreshold[j] = index;
                }
                start = end;
            }
            if (start < probeKeys.length) {
                for (int j = start; j < probeKeys.length; j++) {
                    //setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                    posTothreshold[j] = thresholds.size();
                }
            }
        }
        cntTime += System.nanoTime() - time1;
        return posTothreshold;
    }

    @Override
    public int[] countInt(IPli probePli, int isSingle, Long[] keys, int startPos, long key, List<Double> thresholds) {
        long time1  = System.nanoTime();
        int[] posTothreshold = new int[probePli.size()];
        //SinglePli
        if(isSingle == 1){
            int start = startPos + 1;
            for (int index = 1; index < thresholds.size() && start < probePli.size(); index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).longValue(), start, 1);
                for (int correct = start; correct < end && correct < probePli.size(); correct++) {
                    posTothreshold[correct - startPos] = index;//因为循环函数里是pos + offsets[j - i]，所以这里对应的是correct-startpos
                }
                start = end;
            }

            for (int correct = start; correct < probePli.size(); correct++) {
                posTothreshold[correct - startPos] = thresholds.size();
            }
        }
        //CrossPli
        else{
            final Long[] probeKeys = (Long[]) probePli.getKeys();
            int start = 0;
            for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
                int end;
                end = probePli.getFirstIndexWhereKeyIsLT(key + thresholds.get(index).longValue(), start, 0);
                for (int j = start; j < end; j++) {
                    posTothreshold[j] = index + 1;
                }
                start = end;
            }
            if (start >= probeKeys.length) {
                return posTothreshold;
            }
            // = key
            if (Math.abs(probeKeys[start] - key) < ERR) {
                posTothreshold[start] = 0;
                start++;
            }
            for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
                int end;
                end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).longValue(), start, 1);
                for (int j = start; j < end; j++) {
                    posTothreshold[j] = index;
                }
                start = end;
            }
            if (start < probeKeys.length) {
                for (int j = start; j < probeKeys.length; j++) {
                    posTothreshold[j] = thresholds.size();
                }
            }
        }
        cntTime += System.nanoTime() - time1;
        return posTothreshold;
    }
}
