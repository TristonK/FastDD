package thresholds;

import fastdd.utils.DistanceCalculation;
import de.metanome.algorithms.dcfinder.input.Input;

import java.util.*;

/**
 * @author tristonK 2023/3/9
 */
public class Determination {
    /**
     * produce tuple pairs with size rowLimit*rowLimit
     */
    int rowLimit;

    /**
     * max thresholds size
     */
    int maxThresholdsSize;

    /**
     * min thresholds size
     */
    int minThresholdsSize;

    ThresholdsStrategy strategy;

    List<HashMap<Double, Integer>> columnsDiff;

    ArrayList<List<Double>> hashKeys = new ArrayList<List<Double>>();

    int colSize;

    public Determination(int rowLimit, int maxThresholdsSize, int minThresholdsSize, int colSize, ThresholdsStrategy strategy) {
        this.rowLimit = rowLimit;
        this.maxThresholdsSize = maxThresholdsSize;
        this.minThresholdsSize = minThresholdsSize;
        this.colSize = colSize;
        this.columnsDiff = new ArrayList<>();
        for (int i = 0; i < colSize; i++) {
            HashMap<Double, Integer> diff = new HashMap<>();
            columnsDiff.add(diff);
        }
        this.strategy = strategy;
    }

    public void sampleAndCalculate(Input input) {

        Random r = new Random();
        int[] arr = new int[this.rowLimit];
        for (int i = 0; i < arr.length; i++) {
            int num = 1 + r.nextInt(input.getRowCount()-1);
            if (!isRepeat(arr, num)) {
                arr[i++] = num;
            }
        }
        //int -> double -> string

        double[][] dInput = input.getDoubleInput();
        long[][] iInput = input.getLongInput();
        String[][] sInput = input.getStringInput();

        int index = 0;//
        for (int i = 0; i < this.rowLimit - 1; i++) {
            for (int j = i + 1; j < this.rowLimit; j++) {
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][arr[i]] - iInput[k][arr[j]]);//获得差值
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);
                    } else {
                        tmpmap.put(diff, 1);
                    }
                    this.columnsDiff.set(k, tmpmap);
                }
                for (int k = 0; k < dInput.length; k++) {
                    double diff = Math.abs(dInput[k][arr[i]] - dInput[k][arr[j]]);
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k + iInput.length);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);
                    } else {
                        tmpmap.put(diff, 1);
                    }
                    this.columnsDiff.set(k + iInput.length, tmpmap);
                }
                for (int k = 0; k < sInput.length; k++) {
                    double diff = DistanceCalculation.StringDistance(sInput[k][arr[i]], sInput[k][arr[j]]);
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k + iInput.length + dInput.length);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);
                    } else {
                        tmpmap.put(diff, 1);
                    }
                    this.columnsDiff.set(k + iInput.length + dInput.length, tmpmap);
                }
            }
        }


        //得到升序排列的hashmap键值
        for (int i = 0; i < colSize; i++) {
            List<Double> tmplist = new ArrayList<>(this.columnsDiff.get(i).keySet());
            hashKeys.add(i, tmplist);
        }
        for (List<Double> it : hashKeys) {
            Collections.sort(it);
        }



    }

    public List<List<List<Double>>> determine() {
        List<List<List<Double>>> allColumnsThresholds = new ArrayList<>();
        for (int i = 0; i < colSize; i++) {
            allColumnsThresholds.add(strategy.calculateThresholds(columnsDiff.get(i), hashKeys.get(i), maxThresholdsSize, minThresholdsSize, 0.3,0.75));
        }
        return allColumnsThresholds;
    }

    public boolean isRepeat(int[] arr, int num) {
        for (int j : arr) {
            if (j == num) {
                return true;
            }
        }
        return false;
    }
}
