package ddfinder.evidence.offsetimpl;

import ddfinder.evidence.IClueOffset;
import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class BinaryCalOffset implements IClueOffset {
    @Override
    public int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds) {
        int[] posTothreshold = new int[probePli.size()];
        //SinglePli
        if(1 == isSingle){

            int start = startPos + 1;
            for (int index = 1; index < thresholds.size() && start < probePli.size(); index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index), start, 1);
                for (int correct = start; correct < end && correct < probePli.size(); correct++) {
//                setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    posTothreshold[correct - startPos] = index;
                }
                start = end;
            }

            for (int correct = start; correct < probePli.size(); correct++) {
//            setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
                posTothreshold[correct - startPos] = thresholds.size();
            }

            return posTothreshold;

        }
        //CrossPli
        else{
            final Double[] probeKeys = (Double[]) probePli.getKeys();
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
                    posTothreshold[j] = thresholds.size();
                }
            }
            return posTothreshold;

        }
    }

    @Override
    public int[] countInt(IPli probePli, int isSingle, Integer[] keys, int startPos, int key, List<Double> thresholds) {
        int[] posTothreshold = new int[probePli.size()];
        //SinglePli
        if(isSingle == 1){
            int start = startPos + 1;
            for (int index = 1; index < thresholds.size() && start < probePli.size(); index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(key - thresholds.get(index).intValue(), start, 1);
                for (int correct = start; correct < end && correct < probePli.size(); correct++) {
//                setNumMask(clues, pli.get(i), pli.get(correct), pos + index);
                    posTothreshold[correct - startPos] = index;//因为循环函数里是pos + offsets[j - i]，所以这里对应的是correct-startpos
                }
                start = end;
            }

            for (int correct = start; correct < probePli.size(); correct++) {
//            setNumMask(clues, pli.get(i), pli.get(correct), pos + thresholds.size());
                posTothreshold[correct - startPos] = thresholds.size();
            }

            return posTothreshold;
        }
        //CrossPli
        else{
            final Integer[] probeKeys = (Integer[]) probePli.getKeys();
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
                    posTothreshold[j] = thresholds.size();
                }
            }
            return posTothreshold;
        }
    }
}
