package thresholds;

import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/3/24
 */
public interface ThresholdsStrategy {
    List<List<Double>> calculateThresholds(HashMap<Double, Integer> diff2Freq, List<Double> orderedKeyList, int thresholdsNum, int minThresholdsNum, double freqBoundary, double indexBoundary);
}
