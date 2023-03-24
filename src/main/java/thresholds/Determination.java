package thresholds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/3/9
 */
public class Determination {
    /**
     * produce tuple pairs with size rowLimit*rowLimit
     * */
    int rowLimit;

    /**
     * max thresholds size
     * */
    int maxThresholdsSize;

    /**
     * min thresholds size
     * */
    int minThrehsoldsSize;

    ThresholdsStrategy strategy;

    List<HashMap<Double, Integer>> columnsDiff;

    int colSize;

    public Determination(int rowLimit, int maxThresholdsSize, int minThrehsoldsSize, int colSize, ThresholdsStrategy strategy){
        this.rowLimit = rowLimit;
        this.maxThresholdsSize = maxThresholdsSize;
        this.minThrehsoldsSize = minThrehsoldsSize;
        this.colSize = colSize;
        this.columnsDiff = new ArrayList<>();
        for(int i = 0; i < colSize; i++){
            HashMap<Double, Integer> diff = new HashMap<>();
            columnsDiff.add(diff);
        }
        this.strategy = strategy;
    }

    public void sampleAndCalculate(){

    }

    public List<List<Double>> determine(){
        List<List<Double>> allColumnsThresholds = new ArrayList<>();
        for(int i = 0; i < colSize; i++){
            allColumnsThresholds.add(strategy.calculateThresholds(columnsDiff.get(i), maxThresholdsSize));
        }
        return allColumnsThresholds;
    }

}
