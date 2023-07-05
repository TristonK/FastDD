package ddfinder.predicate;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.Config;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static ddfinder.Config.OutputPredicateFlag;

/**
 * @author tristonK 2022/12/29
 */
public class DifferentialFunctionBuilder {
    private List<DifferentialFunction> differentialFunctions;
    private final PredicateProvider predicateProvider;
    public static IndexProvider<DifferentialFunction> predicateIdProvider;
    private Map<Integer, List<Double>> col2Thresholds;

    // 属性为long的谓词的列的序号
    private List<Integer> longPredicatesGroup;

    private List<Integer> doublePredicatesGroup;
    private List<Integer> strPredicatesGroup;

    private static int intervalCnt;

    private Map<Integer, List<DifferentialFunction>> colToPredicatesGroup;

    private List<BitSet> colPredicateGroup;

    private LongBitSet differentialFunctionsBitSet;
    public DifferentialFunctionBuilder(Input input) {
        init();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
        for (ParsedColumn<?> column : input.getColumns()) {
            List<List<Double>> thresholdsAll = CalculateThresholds(column, 0, 5);
            addPredicates(column, thresholdsAll.get(0), thresholdsAll.get(1));
        }
        predicateIdProvider.addAll(differentialFunctions);
        DifferentialFunction.configure(predicateProvider);
        PredicateSet.configure(predicateIdProvider);
        differentialFunctionsBitSet = new LongBitSet.LongBitSetFactory().createAllSet(differentialFunctions.size());
    }

    /**
     * @param index: file contents:
     *               col1 [thresholds1,thresholds2][thresholds3...]
     */
    public DifferentialFunctionBuilder(File index, Input input) throws IOException {
        init();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
        BufferedReader reader = new BufferedReader(new FileReader(index));
        String line;
        // accepted: xx <= y
        Map<String, List<Double>> smallerThresholds = new HashMap<>();
        // accepted: xx > y
        Map<String, List<Double>> biggerThresholds = new HashMap<>();
        while ((line = reader.readLine()) != null) {
            String[] contents = line.split("\\[");
            contents[0] = contents[0].trim();
            if (contents.length == 3) {
                contents[1] = contents[1].trim();
                String smallerString = contents[1].substring(0, contents[1].length() - 1);
                List<Double> sThresholds = handleThresholdString(smallerString, true);
                smallerThresholds.put(contents[0], sThresholds);
                contents[2] = contents[2].trim();
                String biggerString = contents[2].substring(0, contents[2].length() - 1);
                List<Double> bThresholds = handleThresholdString(biggerString, false);
                biggerThresholds.put(contents[0], bThresholds);
            } else {
                throw new IllegalArgumentException("Please using correct predicates file: 'colName [t1,t2,..][t3,t4,..]'");
            }
        }
        for (ParsedColumn<?> column : input.getColumns()) {
            addPredicates(column, smallerThresholds.getOrDefault(column.getColumnName(), new ArrayList<>()), biggerThresholds.getOrDefault(column.getColumnName(), new ArrayList<>()));
        }
        predicateIdProvider.addAll(differentialFunctions);
        DifferentialFunction.configure(predicateProvider);
        PredicateSet.configure(predicateIdProvider);
        differentialFunctionsBitSet = new LongBitSet.LongBitSetFactory().createAllSet(differentialFunctions.size());
    }

    public List<DifferentialFunction> getPredicates() {
        return differentialFunctions;
    }

    public int size() {
        return differentialFunctions.size();
    }

    /**
     * mode:
     * - simple(0): 0~maxThr with num threshold
     * - log(1): 0 ~ log(Thershold)
     */
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

    private void addPredicates(ParsedColumn<?> column, List<Double> smallThresholds, List<Double> bigThresholds) {
        if (smallThresholds == null || smallThresholds.size() == 0) {
            throw new IllegalArgumentException("Null or empty thresholds is not supported");
        }

        List<DifferentialFunction> partialDifferentialFunctions = new ArrayList<>();
        ColumnOperand<?> operand = new ColumnOperand<>(column, 0);

        // 确定所有的Differential Function
        Collections.sort(smallThresholds);
        Collections.sort(bigThresholds);
        // <=, 阈值降序
        for (int i = smallThresholds.size() - 1; i >= 0; i--) {
            DifferentialFunction p = predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, smallThresholds.get(i));
            partialDifferentialFunctions.add(p);
        }
        // >, 阈值升序
        for (Double bigThreshold : bigThresholds) {
            DifferentialFunction p = predicateProvider.getPredicate(Operator.GREATER, operand, bigThreshold);
            partialDifferentialFunctions.add(p);
        }
        differentialFunctions.addAll(partialDifferentialFunctions);

        //确定涉及到的全部阈值
        Set<Double> thresholdsSet = new HashSet<>();
        thresholdsSet.addAll(smallThresholds);
        thresholdsSet.addAll(bigThresholds);
        List<Double> thresholds = new ArrayList<>(thresholdsSet);
        Collections.sort(thresholds);
        column.setThresholds(thresholds);
        col2Thresholds.put(column.getIndex(), thresholds);

        colToPredicatesGroup.put(column.getIndex(), partialDifferentialFunctions);
        intervalCnt += thresholds.size() + 1;
        if (column.isLong()) {
            longPredicatesGroup.add(column.getIndex());
        } else if (column.isDouble()) {
            doublePredicatesGroup.add(column.getIndex());
        } else {
            strPredicatesGroup.add(column.getIndex());
        }
        if (Config.OutputPredicateFlag){
            StringBuilder sb = new StringBuilder();
            for(DifferentialFunction df: partialDifferentialFunctions){
                sb.append(df.toString()).append("; ");
            }
            System.out.println(sb.toString());
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
     * @return construct each offset => statisfied predicates of cloumn {@param col}
     * thresholds: - 0 - 1 - 2 - 3
     * offset:     0 -  1 - 2 - 3 - 4
     * offset为2满足的：<=2, <=3, >0, >1
     */
    public List<LongBitSet> getOffset2SatisfiedPredicates(int col) {
        List<LongBitSet> predicateSets = new ArrayList<>();
        List<DifferentialFunction> differentialFunctionsOfCol = colToPredicatesGroup.get(col);
        List<Double> thresholds = col2Thresholds.get(col);
        for (int i = 0; i < thresholds.size(); i++) {
            double threshold = thresholds.get(i);
            PredicateSet mask = new PredicateSet();
            for(DifferentialFunction df: differentialFunctionsOfCol){
                if(df.getOperator() ==Operator.LESS_EQUAL && df.getDistance() >= threshold){
                    mask.add(df);
                }else if(df.getOperator() == Operator.GREATER && df.getDistance() < threshold){
                    mask.add(df);
                }
            }
            predicateSets.add(mask.getLongBitSet());
        }
        PredicateSet mask = new PredicateSet();
        double threshold = thresholds.get(thresholds.size() - 1);
        for(DifferentialFunction df: differentialFunctionsOfCol){
            if(df.getOperator() == Operator.GREATER && df.getDistance() < threshold){
                mask.add(df);
            }
        }
        predicateSets.add(mask.getLongBitSet());
        return predicateSets;
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
    public List<DifferentialFunction> getDFByCol(int col) {
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
        return col2Thresholds.get(col).size();
    }

    public int getPredicateId(DifferentialFunction differentialFunction) {
        return predicateIdProvider.getIndex(differentialFunction);
    }

    public PredicateProvider getPredicateProvider() {
        return predicateProvider;
    }

    public IndexProvider<DifferentialFunction> getPredicateIdProvider() {
        return predicateIdProvider;
    }

    public List<BitSet> getColPredicateGroup() {
        if (colPredicateGroup == null) {
            colPredicateGroup = new ArrayList<>();
            for (int i = 0; i < getColSize(); i++) {
                BitSet bs = new BitSet();
                for (DifferentialFunction pred : getDFByCol(i)) {
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
        if(s.equals("")){
            if(needZero){thresholds.add(0.0);}
            return thresholds;
        }
        //System.out.println("thresholds size: " + thresholdString.length + " " + thresholdString[0]);
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

    public LongBitSet getFullDFBitSet() {
        return differentialFunctionsBitSet;
    }

    private void init(){
        intervalCnt = 0;
        differentialFunctions = new ArrayList<>();
        longPredicatesGroup = new ArrayList<>();
        doublePredicatesGroup = new ArrayList<>();
        strPredicatesGroup = new ArrayList<>();
        colToPredicatesGroup = new HashMap<>();
        col2Thresholds = new HashMap<>();
    }
}
