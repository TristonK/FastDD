package ddfinder.evidence;

import ddfinder.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/5/29
 */
public interface IClueOffset {
    int[] brutalCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds);
    int[] brutalCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds);
    int[] linerCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds);
    int[] linerCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds);
    int[] binaryCountCrossDouble(IPli probePli, int startPos, double key, List<Double> thresholds);
    int[] binaryCountCrossInt(IPli probePli, int startPos, int key, List<Double> thresholds);
    int[] binaryCountSingleDouble(IPli pli, int startPos, double key, List<Double> thresholds);
    int[] binaryCountSingleInt(IPli pli, int startPos, int key, List<Double> thresholds);
}
