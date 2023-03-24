package thresholds;

import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/3/24
 */
public interface ThresholdsStrategy {
    List<Double> calculateThresholds(HashMap<Double, Integer> diff2Freq, int thresholdsNum);
}
