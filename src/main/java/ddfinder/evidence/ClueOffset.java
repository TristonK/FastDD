package ddfinder.evidence;

import ddfinder.pli.IPli;

import java.util.List;

public class ClueOffset implements IClueOffset {

    private final double ERR = 0.000000001;

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

    public int[] brutalCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
//        int thresholdsId = thresholds.size() - 1;

        //循环遍历keys，分别与key做差的到diff，然后找到其在thresholds中的位置，最后添加到posTothreshold中
        while (pos < keys.length) {
            double diff = Math.abs(keys[pos] - key);
            posTothreshold[pos - startPos] = findMaskPos(diff, thresholds);
            pos++;
        }
        return posTothreshold;
    }

    public int[] brutalCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
//        int thresholdsId = thresholds.size() - 1;

        //循环遍历keys，分别与key做差的到diff，然后找到其在thresholds中的位置，最后添加到posTothreshold中
        while (pos < keys.length) {
            double diff = Math.abs(keys[pos] - key);
            posTothreshold[pos - startPos] = findMaskPos(diff, thresholds);
            pos++;

        }
        return posTothreshold;
    }

    public int[] linerCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        // > key
        while (pos < keys.length && keys[pos] - key > ERR) {
            // handle diff > max(thresholds)
            while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId) + ERR) {
                posTothreshold[pos - startPos] = thresholdsId + 1;
                pos++;
            }
            if (pos == keys.length) {
                break;
            }
            // handle diff <= max(thresholds)
            while (thresholdsId > 0 && keys[pos] - key < thresholds.get(thresholdsId - 1) + ERR) {
                thresholdsId--;
            }
            posTothreshold[pos - startPos] = thresholdsId;
            pos++;
        }
        // = key
        if (pos < keys.length && Math.abs(key - keys[pos]) < ERR) {
            posTothreshold[pos - startPos] = 0;
            pos++;
        }

        // < key
        thresholdsId = 1;
        for (; pos < keys.length; pos++) {
            while (thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId) + ERR) {
                thresholdsId++;
            }
            //handle diff > max(thresholds)
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }

    public int[] linerCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        // > key
        while (pos < keys.length && key < keys[pos]) {
            // handle diff > max(thresholds)
            while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId)) {
                posTothreshold[pos - startPos] = thresholdsId + 1;
                pos++;
            }
            if (pos == keys.length) {
                break;
            }
            // handle diff <= max(thresholds)
            while (thresholdsId > 0 && keys[pos] - key <= thresholds.get(thresholdsId - 1)) {
                thresholdsId--;
            }
            posTothreshold[pos - startPos] = thresholdsId;
            pos++;
        }
        // = key
        if (pos < keys.length && key == keys[pos]) {
            posTothreshold[pos - startPos] = 0;
            pos++;
        }
        // < key
        thresholdsId = 1;
        for (; pos < keys.length; pos++) {
            while (thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId)) {
                thresholdsId++;
            }
            //handle diff > max(thresholds)
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }


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


}
