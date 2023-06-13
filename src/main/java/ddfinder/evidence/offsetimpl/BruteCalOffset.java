package ddfinder.evidence.offsetimpl;

import ddfinder.evidence.IClueOffset;
import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class BruteCalOffset implements IClueOffset {
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

    public int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds) {
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

    public int[] countInt(IPli probePli, int isSingle, Integer[] keys, int startPos, int key, List<Double> thresholds) {
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


}
