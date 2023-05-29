package ddfinder.evidence;

import java.util.List;

/**
 * @author tristonK 2023/5/29
 */
public interface IClueOffset {
    public int[] linerCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds);
    public int[] linerCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds);
}
