package ddfinder.predicate;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import com.sun.java.accessibility.util.AccessibilityListenerList;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


import java.io.*;
import java.util.*;

/**
 * @author tristonK 2022/12/29
 */
public class PredicateBuilder {
    private List<Predicate> predicates;
    private List<Predicate> rejectedPredicates;
    private List<Predicate> acceptedPredicates;
    private final PredicateProvider predicateProvider;
    private final IndexProvider<Predicate> predicateIdProvider;

    // 属性为long的谓词的列的序号
    private List<Integer> longPredicatesGroup;

    private List<Integer> doublePredicatesGroup;
    private List<Integer> strPredicatesGroup;


    private Map<Integer, Set<Integer>> interval2IntervalGroups;
    private static int intervalCnt;

    private Map<Integer, List<Predicate>> colToPredicatesGroup;

    private Map<Integer, IntervalPredicate> intervalPredicateMap;

    private List<BitSet> colPredicateGroup;

    private LongBitSet predicatesBitSet;
    private LongBitSet acceptPredicatesBitSet;
    private LongBitSet rejectPredicatesBitSet;

    private final boolean isRfdTest = true;

    public PredicateBuilder(Input input) {
        intervalCnt = 0;
        predicates = new ArrayList<>();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
        longPredicatesGroup = new ArrayList<>();
        doublePredicatesGroup = new ArrayList<>();
        strPredicatesGroup = new ArrayList<>();
        colToPredicatesGroup = new HashMap<>();
        interval2IntervalGroups = new HashMap<>();
        intervalPredicateMap = new HashMap<>();
        rejectedPredicates = new ArrayList<>();
        acceptedPredicates = new ArrayList<>();
        for (ParsedColumn<?> column : input.getColumns()) {
            List<List<Double>> thresholdsAll = CalculateThresholds(column, 0, 5);
            //System.out.println(thresholdsAll.get(0).toString() + " ] [ " + thresholdsAll.get(1).toString());
            addPredicates(column, thresholdsAll.get(0), thresholdsAll.get(1));
        }
        predicateIdProvider.addAll(predicates);
        Predicate.configure(predicateProvider);
        PredicateSet.configure(predicateIdProvider);
        calBitSet();
    }

    /**
     * @param index: file contents:
     *               col1 [thresholds1,thresholds2][thresholds3...]
     *               col2 [thresholds1...]
     */
    public PredicateBuilder(File index, Input input) throws IOException {
        predicates = new ArrayList<>();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
        intervalCnt = 0;
        longPredicatesGroup = new ArrayList<>();
        doublePredicatesGroup = new ArrayList<>();
        strPredicatesGroup = new ArrayList<>();
        colToPredicatesGroup = new HashMap<>();
        rejectedPredicates = new ArrayList<>();
        acceptedPredicates = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(index));
        String line;
        // accepted: xx <= y, rejected: xx > y
        Map<String, List<Double>> smallerThresholds = new HashMap<>();
        // accepted: xx > y, rejected: xx <= y
        Map<String, List<Double>> biggerThresholds = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String[] contents = line.split("\\[");
            contents[0] = contents[0].trim();
            if (contents.length == 2) {
                contents[1] = contents[1].trim();
                String thresholdsString = contents[1].substring(0, contents[1].length() - 1);
                List<Double> nThresholds = handleThresholdString(thresholdsString, true);
                smallerThresholds.put(contents[0], nThresholds);
                biggerThresholds.put(contents[0], nThresholds);
            } else if (contents.length == 3) {
                contents[1] = contents[1].trim();
                String smallerString = contents[1].substring(0, contents[1].length() - 1);
                List<Double> sThresholds = handleThresholdString(smallerString, true);
                smallerThresholds.put(contents[0], sThresholds);
                contents[2] = contents[2].trim();
                String biggerString = contents[2].substring(0, contents[2].length() - 1);
                List<Double> bThresholds = handleThresholdString(biggerString, false);
                biggerThresholds.put(contents[0], bThresholds);
            } else {
                throw new IllegalArgumentException("Please using correct predicates file: 'colName [t1,t2,..][t3,t4,..]' or 'colName [t1,t2,..]'");
            }
        }
        for (ParsedColumn<?> column : input.getColumns()) {
            addPredicates(column, smallerThresholds.getOrDefault(column.getColumnName(), null), biggerThresholds.getOrDefault(column.getColumnName(), null));
        }
        predicateIdProvider.addAll(predicates);
        Predicate.configure(predicateProvider);
        PredicateSet.configure(predicateIdProvider);
        calBitSet();
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public int size() {
        return predicates.size();
    }

    private List<List<Double>> CalculateThresholds(ParsedColumn<?> column, int mode, int threshold) {
        List<List<Double>> thresholds = new ArrayList<>();
        Double diffD = 5.0;
        if (column.isNum()) {
            diffD = column.getMaxNum() - column.getMinNum();
        }
        if (mode == 0 || !column.isNum()) {
            //TODO:修改String属性列情况
            double step = diffD / (threshold + 1);
            List<Double> sThresholds = new ArrayList<>();
            for (int i = 0; i < (threshold + 1) / 2; i++) {
                String str = String.format("%.2f", i * step);
                double isteps = Double.parseDouble(str);
                sThresholds.add(isteps);
            }
            thresholds.add(sThresholds);
            List<Double> bThresholds = new ArrayList<>();
            for (int i = (threshold + 1) / 2; i < threshold; i++) {
                String str = String.format("%.2f", i * step);
                double isteps = Double.parseDouble(str);
                bThresholds.add(isteps);
            }
            thresholds.add(bThresholds);
        } else if (mode == 1) {
            //TODO
        } else {
            throw new IllegalArgumentException("Bad add predicates mode.");
        }
        return thresholds;
    }

    /**
     * mode:
     * - simple(0): 0~maxThr with num threshold
     * - log(1): 0 ~ log(Thershold)
     */
    private void addPredicates(ParsedColumn<?> column, List<Double> smallThresholds, List<Double> bigThresholds) {
        if (smallThresholds == null) {
            throw new IllegalArgumentException("Null thresholds is not supported");
        }
        List<Predicate> partialPredicates = new ArrayList<>();
        ColumnOperand<?> operand = new ColumnOperand<>(column, 0);
        List<Double> all = new ArrayList<>(smallThresholds);
        if (smallThresholds.equals(bigThresholds)) {//左右阈值相同，也就是只有一侧阈值
            if (!isRfdTest) {
                for (int i = smallThresholds.size() - 1; i >= 0; i--) {
                    Predicate p = predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, smallThresholds.get(i));
                    partialPredicates.add(p);
                    acceptedPredicates.add(p);
                    rejectedPredicates.add(p);
                }
                for (int i = 0; i < smallThresholds.size(); i++) {
                    Predicate p = predicateProvider.getPredicate(Operator.GREATER, operand, smallThresholds.get(i));
                    partialPredicates.add(p);
                    acceptedPredicates.add(p);
                    rejectedPredicates.add(p);
                }
            } else {
                for (int i = smallThresholds.size() - 1; i >= 0; i--) {
                    Predicate p = predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, smallThresholds.get(i));
                    partialPredicates.add(p);
                    acceptedPredicates.add(p);
                }
                for (int i = 0; i < smallThresholds.size(); i++) {
                    Predicate p = predicateProvider.getPredicate(Operator.GREATER, operand, smallThresholds.get(i), false);
                    partialPredicates.add(p);
                    rejectedPredicates.add(p);
                }
            }
            //TODO:修改成允许左右阈值有交叉
//        } else if(smallThresholds.get(smallThresholds.size() - 1) >= bigThresholds.get(0)){
//            throw new IllegalArgumentException("For 'colName [t1,t2,..][t3,t4,..]', all thresholds in the first should smaller than later");
        } else {//同时存在左右阈值，分别生成accepted和rejected谓词
            for (int i = bigThresholds.size() - 1; i >= 0; i--) {
                Predicate p = predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, bigThresholds.get(i), false);
                partialPredicates.add(p);
                rejectedPredicates.add(p);
            }
            for (int i = smallThresholds.size() - 1; i >= 0; i--) {
                Predicate p = predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, smallThresholds.get(i));
                partialPredicates.add(p);
                acceptedPredicates.add(p);
            }
            for (int i = 0; i < smallThresholds.size(); i++) {
                Predicate p = predicateProvider.getPredicate(Operator.GREATER, operand, smallThresholds.get(i), false);
                partialPredicates.add(p);
                rejectedPredicates.add(p);
            }
            for (int i = 0; i < bigThresholds.size(); i++) {
                Predicate p = predicateProvider.getPredicate(Operator.GREATER, operand, bigThresholds.get(i));
                partialPredicates.add(p);
                acceptedPredicates.add(p);
            }
            all.addAll(bigThresholds);
            //去重，排序来消除阈值交叉存在的影响
            Set<Double> deduplicatedAll = new HashSet<>();
            for(Double threshold : all){
                deduplicatedAll.add(threshold);
            }
//            all.clear();
            all =new ArrayList<>(deduplicatedAll);
            Collections.sort(all);
            //对partialPredicates进行去重和排序
            Set<Predicate> LEset = new HashSet<>();
            Set<Predicate> Bset = new HashSet<>();
            for (Predicate predicates : partialPredicates) {
                if (predicates.getOperator().name() == "LESS_EQUAL") {
                    LEset.add(predicates);
                } else {
                    Bset.add(predicates);
                }
            }
            List<Predicate> LEp = new ArrayList<>(LEset);
            List<Predicate> Bp = new ArrayList<>(Bset);
            Collections.sort(LEp, Comparator.comparing(Predicate::getDistance).reversed());
            Collections.sort(Bp, Comparator.comparing(Predicate::getDistance));
            List<Predicate> deduplicatedPredicates = new ArrayList<>(LEp);
            deduplicatedPredicates.addAll(Bp);
            partialPredicates =new ArrayList<>(deduplicatedPredicates);


        }
        column.setThresholds(all);
        predicates.addAll(partialPredicates);

        colToPredicatesGroup.put(column.getIndex(), partialPredicates);
        intervalCnt += all.size() + 1;
        if (column.isLong()) {
            longPredicatesGroup.add(column.getIndex());
        } else if (column.isDouble()) {
            doublePredicatesGroup.add(column.getIndex());
        } else {
            strPredicatesGroup.add(column.getIndex());
        }
    }


    /**
     * @return list of cloumns indexes that are strings
     */
    public List<Integer> getStrPredicatesGroup() {
        return strPredicatesGroup;
    }

    /**
     * @return list of cloumns indexes that are integers
     */
    public List<Integer> getLongPredicatesGroup() {
        return longPredicatesGroup;
    }

    /**
     * @return list of cloumns indexes that are double numbers
     */
    public List<Integer> getDoublePredicatesGroup() {
        return doublePredicatesGroup;
    }

    /**
     * @return count of all intervals
     */
    public static int getIntervalCnt() {
        return intervalCnt;
    }

    /**
     * @return ParsedColumn associated with @Param colIndex
     */
    public ParsedColumn<?> getPredicateColumn(int colIndex) {
        return colToPredicatesGroup.get(colIndex).get(0).getOperand().getColumn();
    }

    /**
     * @return construct each bit in clue => statisfied predicates of cloumn {@param col}
     * example: predicates:{<=2, <=1, <=0, >0, >1, >2}
     * clue: 0010 => {<=2, >0, >1}
     */
    public List<LongBitSet> getColPredicateSet(int col) {
        List<LongBitSet> predicateSets = new ArrayList<>();
        List<Predicate> predicatesOfCol = colToPredicatesGroup.get(col);
        int thresholdSize = predicatesOfCol.size() / 2;
        for (int i = 0; i < thresholdSize + 1; i++) {
            PredicateSet mask = new PredicateSet();
            for (int j = 0; j < thresholdSize - i; j++) {
                mask.add(predicatesOfCol.get(j));
            }
            for (int j = thresholdSize; j < thresholdSize + i; j++) {
                mask.add(predicatesOfCol.get(j));
            }
            predicateSets.add(mask.getLongBitSet());
        }
        return predicateSets;
    }

    public Map<Integer, Set<Integer>> getInterval2IntervalGroups() {
        return interval2IntervalGroups;
    }

    public Map<Integer, IntervalPredicate> getIntervalPredicateMap() {
        return intervalPredicateMap;
    }

    /**
     * @return counts of columns
     */
    public int getColSize() {
        return colToPredicatesGroup.size();
    }

    /**
     * @return predicates of {@param col}
     */
    public List<Predicate> getColPredicates(int col) {
        return colToPredicatesGroup.get(col);
    }

    /**
     * @return Predicates Size of {@param col}
     */
    public int getColPredicatesSize(int col) {
        return colToPredicatesGroup.get(col).size();
    }

    /**
     * @return Thresholds size of {@param col}
     */
    public int getColThresholdsSize(int col) {
        return getColPredicatesSize(col) / 2;
    }

    public int getPredicateId(Predicate predicate) {
        return predicateIdProvider.getIndex(predicate);
    }

    public PredicateProvider getPredicateProvider() {
        return predicateProvider;
    }

    public IndexProvider<Predicate> getPredicateIdProvider() {
        return predicateIdProvider;
    }

    public List<BitSet> getColPredicateGroup() {
        if (colPredicateGroup == null) {
            colPredicateGroup = new ArrayList<>();
            for (int i = 0; i < getColSize(); i++) {
                BitSet bs = new BitSet();
                for (Predicate pred : getColPredicates(i)) {
                    bs.set(predicateIdProvider.getIndex(pred));
                }
                colPredicateGroup.add(bs);
            }
        }
        return colPredicateGroup;
    }

    private List<Double> handleThresholdString(String s, boolean needZero) {
        s = s.trim();
        String[] thresholdString = s.split(",");
        List<Double> thresholds = new ArrayList<>();
        boolean hasZero = false;
        for (int i = 0; i < thresholdString.length; i++) {
            if (Double.parseDouble(thresholdString[i]) == 0) {
                hasZero = true;
            }
            thresholds.add(Double.parseDouble(thresholdString[i]));
        }
        if (!hasZero && needZero) {
            thresholds.add(0.0);
        }
        Collections.sort(thresholds);
        //System.out.println(thresholds);
        return thresholds;
    }

    private void calBitSet() {
        predicatesBitSet = new LongBitSet();
        acceptPredicatesBitSet = new LongBitSet();
        rejectPredicatesBitSet = new LongBitSet();
        for (Predicate p : acceptedPredicates) {
            //  System.out.println("accept: " + p);
            int id = predicateIdProvider.getIndex(p);
            predicatesBitSet.set(id);
            acceptPredicatesBitSet.set(id);
        }
        for (Predicate p : rejectedPredicates) {
            // System.out.println("reject: " + p);
            int id = predicateIdProvider.getIndex(p);
            predicatesBitSet.set(id);
            rejectPredicatesBitSet.set(id);
        }
    }

    public LongBitSet getAcceptPredicatesBitSet() {
        return acceptPredicatesBitSet;
    }

    public LongBitSet getRejectPredicatesBitSet() {
        return rejectPredicatesBitSet;
    }

    public LongBitSet getPredicatesBitSet() {
        return predicatesBitSet;
    }
}
