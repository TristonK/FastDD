package ddfinder.evidence.offsetimpl;

import ddfinder.evidence.IClueOffset;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class LinearCalOffset implements IClueOffset {
    public int[] countDouble(Double[] keys, int startPos, double key, List<Double> thresholds) {
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

    public int[] countInt(Integer[] keys, int startPos, int key, List<Double> thresholds) {
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
}
