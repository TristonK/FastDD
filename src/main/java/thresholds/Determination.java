package thresholds;

import ddfinder.utils.DistanceCalculation;
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

    List<HashMap<Double, Integer>> columnsDiff;//属性差值->元组个数/频度    //List[hashmap1<>,...,hashmapn<>]顺序为int->double->string

    ArrayList<List<Double>> hashKeys = new ArrayList<List<Double>>();//存储升序排列的hashmap键值，便于分析

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
        //读入指定数量的元组对，并计算属性之间的差值与对应频度，结果存入this.columnsDiff
        //采样：从input中随机取出rowlimit行tuple
        //小于200行就不采样


        Random r = new Random();
        int[] arr = new int[this.rowLimit];//存储不重复的随机数
        for (int i = 0; i < arr.length; ) {
            int num = 1 + r.nextInt(input.getRowCount()-1);//生成[1~行]数的随机整数
            if (!isRepeat(arr, num)) {//保证不重复
                arr[i++] = num;
            }
        }
        //int -> double -> string

        double[][] dInput = input.getDoubleInput();//二维数组第一位表示该类型属性数量，第二位表示行数
        long[][] iInput = input.getLongInput();
        String[][] sInput = input.getStringInput();

        int index = 0;//
        for (int i = 0; i < this.rowLimit - 1; i++) {
            for (int j = i + 1; j < this.rowLimit; j++) {
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][arr[i]] - iInput[k][arr[j]]);//获得差值
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);//更新差值频率
                    } else {
                        tmpmap.put(diff, 1);//首次记录该diff
                    }
                    this.columnsDiff.set(k, tmpmap);//存入hashmap中
                }
                for (int k = 0; k < dInput.length; k++) {
                    double diff = Math.abs(dInput[k][arr[i]] - dInput[k][arr[j]]);//获得差值
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k + iInput.length);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);//更新差值频率
                    } else {
                        tmpmap.put(diff, 1);//首次记录该diff
                    }
                    this.columnsDiff.set(k + iInput.length, tmpmap);//存入hashmap中
                }
                for (int k = 0; k < sInput.length; k++) {
                    double diff = DistanceCalculation.StringDistance(sInput[k][arr[i]], sInput[k][arr[j]]);
                    HashMap<Double, Integer> tmpmap = this.columnsDiff.get(k + iInput.length + dInput.length);//获取对应属性的hashmap
                    if (tmpmap.containsKey(diff)) {
                        tmpmap.put(diff, tmpmap.get(diff) + 1);//更新差值频率
                    } else {
                        tmpmap.put(diff, 1);//首次记录该diff
                    }
                    this.columnsDiff.set(k + iInput.length + dInput.length, tmpmap);//存入hashmap中
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


//        System.out.println("采样并计算差值结束！");

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
