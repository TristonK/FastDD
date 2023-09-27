package fastdd.dfset.offsetimpl;

import fastdd.dfset.IClueOffset;
import fastdd.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class BruteCalOffset implements IClueOffset {
    private int findMaskPos(double diff, List<Double> th) {
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


    public int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];

        while (pos < keys.length) {
            double diff = Math.abs(keys[pos] - key);
            posTothreshold[pos - startPos] = findMaskPos(diff, thresholds);
            pos++;
        }
        return posTothreshold;
    }

    public int[] countInt(IPli probePli, int isSingle, Long[] keys, int startPos, long key, List<Double> thresholds) {
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        while (pos < keys.length) {
            double diff = Math.abs(keys[pos] - key);
            posTothreshold[pos - startPos] = findMaskPos(diff, thresholds);
            pos++;

        }
        return posTothreshold;
    }


}
