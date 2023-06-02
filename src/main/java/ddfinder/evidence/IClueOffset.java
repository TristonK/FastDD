package ddfinder.evidence;

import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/5/29
 */
public interface IClueOffset {
    final double ERR = 0.000000001;
    int[] countDouble(Double[] keys, int startPos, double key, List<Double> thresholds);
    int[] countInt(Integer[] keys, int startPos, int key, List<Double> thresholds);
}
