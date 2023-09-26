package fastdd.dfset.offsetimpl;

import fastdd.dfset.IOffset;
import fastdd.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class LinearCalOffset implements IOffset {
    public int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId) + ERR) {
            posTothreshold[pos - startPos] = thresholdsId + 1;
            pos++;
        }
        while (pos < keys.length && keys[pos] - key > ERR) {
            while (thresholdsId > 0 && keys[pos] - key < thresholds.get(thresholdsId - 1) + ERR) {
                thresholdsId--;
            }
            posTothreshold[pos - startPos] = thresholdsId;
            pos++;
        }
        if (pos < keys.length && Math.abs(key - keys[pos]) < ERR) {
            posTothreshold[pos - startPos] = 0;
            pos++;
        }
        thresholdsId = 1;
        for (; pos < keys.length; pos++) {
            while (thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId) + ERR) {
                thresholdsId++;
            }
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }

    public int[] countInt(IPli probePli, int isSingle, Long[] keys, int startPos, long key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId)) {
            posTothreshold[pos - startPos] = thresholdsId + 1;
            pos++;
        }
        while (pos < keys.length && key < keys[pos]) {
            while (thresholdsId > 0 && keys[pos] - key <= thresholds.get(thresholdsId - 1)) {
                thresholdsId--;
            }
            posTothreshold[pos - startPos] = thresholdsId;
            pos++;
        }
        if (pos < keys.length && key == keys[pos]) {
            posTothreshold[pos - startPos] = 0;
            pos++;
        }
        thresholdsId = 1;
        for (; pos < keys.length; pos++) {
            while (thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId)) {
                thresholdsId++;
            }
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }

}
