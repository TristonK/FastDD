package ddfinder.evidence.offsetimpl;

import ddfinder.evidence.IClueOffset;
import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class BinaryCalOffset implements IClueOffset {
    public int[] binaryCountCrossDouble(IPli probePli, int startPos, double key, List<Double> thresholds) {
        final Double[] probeKeys = (Double[]) probePli.getKeys();
        int[] posTothreshold = new int[probeKeys.length - startPos];
        int thresholdsId = thresholds.size() - 1;

        int start = 0;
        for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
            int end = probePli.getFirstIndexWhereKeyIsLT(key + thresholds.get(index), start, 0);
            for (int j = start; j < end; j++) {
//                setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);
                posTothreshold[j] = index + 1;
            }
            start = end;
        }
        if (start >= probeKeys.length) {
            return posTothreshold;
        }
        // = key
        if (Math.abs(probeKeys[start] - key) < ERR) {
//            setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
            posTothreshold[start] = 0;
            start++;
        }
        for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
            int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
            for (int j = start; j < end; j++) {
                //setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index);
                posTothreshold[j] = index;
            }
            start = end;
        }
        if (start < probeKeys.length) {
            for (int j = start; j < probeKeys.length; j++) {
                //setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                posTothreshold[j] = thresholdsId;
            }
        }
        return posTothreshold;
    }

    public int[] binaryCountCrossInt(IPli probePli, int startPos, int key, List<Double> thresholds) {
        final Integer[] probeKeys = (Integer[]) probePli.getKeys();
        int[] posTothreshold = new int[probeKeys.length - startPos];
        int thresholdsId = thresholds.size() - 1;

        int start = 0;
        for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
            int end;
            end = probePli.getFirstIndexWhereKeyIsLT(key + thresholds.get(index).intValue(), start, 0);
            for (int j = start; j < end; j++) {
//                setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);
                posTothreshold[j] = index + 1;
            }
            start = end;
        }
        if (start >= probeKeys.length) {
            return posTothreshold;
        }
        // = key
        if (Math.abs(probeKeys[start] - key) < ERR) {
//            setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
            posTothreshold[start] = 0;
            start++;
        }
        for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
            int end;
            end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).intValue(), start, 1);
            for (int j = start; j < end; j++) {
                //setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index);
                posTothreshold[j] = index;
            }
            start = end;
        }
        if (start < probeKeys.length) {
            for (int j = start; j < probeKeys.length; j++) {
                //setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                posTothreshold[j] = thresholdsId;
            }
        }
        return posTothreshold;
    }

    public int[] binaryCountSingleDouble(IPli pli, int startPos, double key, List<Double> thresholds) {
//        final Double[] probeKeys = (Double[]) pli.getKeys();
        int[] posTothreshold = new int[pli.size()];

        int start = startPos + 1;
        for (int index = 1; index < thresholds.size() && start < pli.size(); index++) {
            int end = pli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
            for (int correct = start; correct < end && correct < pli.size(); correct++) {
//                setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                posTothreshold[correct] = index;
            }
            start = end;
        }

        for (int correct = start; correct < pli.size(); correct++) {
//            setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
            posTothreshold[correct] = thresholds.size();
        }

        return posTothreshold;
    }

    public int[] binaryCountSingleInt(IPli pli, int startPos, int key, List<Double> thresholds) {
//        final Integer[] probeKeys = (Integer[]) pli.getKeys();
        int[] posTothreshold = new int[pli.size()];

        int start = startPos + 1;
        for (int index = 1; index < thresholds.size() && start < pli.size(); index++) {
            int end = pli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).intValue(), start, 1);
            for (int correct = start; correct < end && correct < pli.size(); correct++) {
//                setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                posTothreshold[correct] = index;
            }
            start = end;
        }

        for (int correct = start; correct < pli.size(); correct++) {
//            setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
            posTothreshold[correct] = thresholds.size();
        }

        return posTothreshold;
    }

    /**
     * @param keys
     * @param startPos
     * @param key
     * @param thresholds
     * @return
     */
    //TODO
    @Override
    public int[] countDouble(Double[] keys, int startPos, double key, List<Double> thresholds) {
        return new int[0];
    }

    /**
     * @param keys
     * @param startPos
     * @param key
     * @param thresholds
     * @return
     */
    //TODO
    @Override
    public int[] countInt(Integer[] keys, int startPos, int key, List<Double> thresholds) {
        return new int[0];
    }
}
