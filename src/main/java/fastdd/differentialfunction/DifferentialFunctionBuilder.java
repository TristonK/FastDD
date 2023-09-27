package fastdd.differentialfunction;

import ch.javasoft.bitset.LongBitSet;
import fastdd.Config;
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

/**
 * @author tristonK 2022/12/29
 */
public class DifferentialFunctionBuilder {
    private List<DifferentialFunction> differentialFunctions;
    private final DFProvider dfProvider;
    public static IndexProvider<DifferentialFunction> dfIdProvider;
    private Map<Integer, List<Double>> col2Thresholds;

    // 属性为long的谓词的列的序号
    private List<Integer> longDFsGroup;

    private List<Integer> doubleDFsGroup;
    private List<Integer> strDFsGroup;

    private static int intervalCnt;

    private Map<Integer, List<DifferentialFunction>> colToDFsGroup;

    private List<BitSet> colDFGroup;

    private Map<Integer, Integer> bitsetIndex2ThresholdsIndex;

    private LongBitSet differentialFunctionsBitSet;
    public List<DifferentialFunction> HighestDfOfAttr = new ArrayList<>();
    public DifferentialFunctionBuilder(Input input) {
        init();
        dfProvider = new DFProvider();
        dfIdProvider = new IndexProvider<>();
        for (ParsedColumn<?> column : input.getColumns()) {
            List<List<Double>> thresholdsAll = CalculateThresholds(column, 0, 5);
            addDifferentialFunctions(column, thresholdsAll.get(0), thresholdsAll.get(1));
        }
        dfIdProvider.addAll(differentialFunctions);
        DifferentialFunction.configure(dfProvider);
        DifferentialFunctionSet.configure(dfIdProvider);
        differentialFunctionsBitSet = new LongBitSet.LongBitSetFactory().createAllSet(differentialFunctions.size());
        buildBitSetIndexMap();
    }
    public DifferentialFunctionBuilder(Input input, List<List<List<Double>>> thresholds) {
        init();
        dfProvider = new DFProvider();
        dfIdProvider = new IndexProvider<>();
        int index =0;
        for (ParsedColumn<?> column : input.getColumns()) {
            List<List<Double>> thresholdsAll = thresholds.get(index);
            addDifferentialFunctions(column, thresholdsAll.get(0), thresholdsAll.get(1));
            index++;
        }
        dfIdProvider.addAll(differentialFunctions);
        DifferentialFunction.configure(dfProvider);
        DifferentialFunctionSet.configure(dfIdProvider);
        differentialFunctionsBitSet = new LongBitSet.LongBitSetFactory().createAllSet(differentialFunctions.size());
        buildBitSetIndexMap();
    }

    /**
     * @param index: file contents:
     *               col1 [thresholds1,thresholds2][thresholds3...]
     */
    public DifferentialFunctionBuilder(File index, Input input) throws IOException {
        init();
        dfProvider = new DFProvider();
        dfIdProvider = new IndexProvider<>();
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
            if (!smallerThresholds.containsKey(column.getColumnName())){System.out.println(column.getColumnName()+column.getColumnName().length());}
            addDifferentialFunctions(column, smallerThresholds.getOrDefault(column.getColumnName(), new ArrayList<>()), biggerThresholds.getOrDefault(column.getColumnName(), new ArrayList<>()));
        }
        dfIdProvider.addAll(differentialFunctions);
        DifferentialFunction.configure(dfProvider);
        DifferentialFunctionSet.configure(dfIdProvider);
        differentialFunctionsBitSet = new LongBitSet.LongBitSetFactory().createAllSet(differentialFunctions.size());
        buildBitSetIndexMap();
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
        } else {
            throw new IllegalArgumentException("Bad add predicates mode.");
        }
        return thresholds;
    }

    private void addDifferentialFunctions(ParsedColumn<?> column, List<Double> smallThresholds, List<Double> bigThresholds) {
        if (smallThresholds == null || smallThresholds.size() == 0) {
            throw new IllegalArgumentException("Null or empty thresholds is not supported on" + column.getColumnName());
        }

        List<DifferentialFunction> partialDifferentialFunctions = new ArrayList<>();
        ColumnOperand<?> operand = new ColumnOperand<>(column, 0);

        // 确定所有的Differential Function
        smallThresholds = roundList(smallThresholds, true);
        bigThresholds = roundList(bigThresholds, false);
        smallThresholds = dedup(smallThresholds);
        bigThresholds = dedup(bigThresholds);
        Collections.sort(smallThresholds);
        Collections.sort(bigThresholds);

        System.out.println(column.getColumnName() + smallThresholds.toString()+bigThresholds.toString());

        // <=, 阈值降序
        for (int i = smallThresholds.size() - 1; i >= 0; i--) {
            DifferentialFunction p = dfProvider.getPredicate(Operator.LESS_EQUAL, operand, smallThresholds.get(i));
            partialDifferentialFunctions.add(p);
            if(i == smallThresholds.size() - 1){HighestDfOfAttr.add(p);}
        }
        // >, 阈值升序
        for (int i = 0; i < bigThresholds.size(); i++) {
            Double bigThreshold = bigThresholds.get(i);
            DifferentialFunction p = dfProvider.getPredicate(Operator.GREATER, operand, bigThreshold);
            partialDifferentialFunctions.add(p);
            if(i == 0){HighestDfOfAttr.add(p);}
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

        colToDFsGroup.put(column.getIndex(), partialDifferentialFunctions);
        intervalCnt += thresholds.size() + 1;
        if (column.isLong()) {
            longDFsGroup.add(column.getIndex());
        } else if (column.isDouble()) {
            doubleDFsGroup.add(column.getIndex());
        } else {
            strDFsGroup.add(column.getIndex());
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
    public List<Integer> getStrDFsGroup() {
        return strDFsGroup;
    }

    /**
     * @return list of cloumns indexes that are integers
     */
    public List<Integer> getLongDFsGroup() {
        return longDFsGroup;
    }

    /**
     * @return list of cloumns indexes that are double numbers
     */
    public List<Integer> getDoubleDFsGroup() {
        return doubleDFsGroup;
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
        return colToDFsGroup.get(colIndex).get(0).getOperand().getColumn();
    }

    /**
     * @return construct each offset => statisfied predicates of cloumn {@param col}
     * thresholds: - 0 - 1 - 2 - 3
     * offset:     0 -  1 - 2 - 3 - 4
     */
    public List<LongBitSet> getOffset2SatisfiedPredicates(int col) {
        List<LongBitSet> predicateSets = new ArrayList<>();
        List<DifferentialFunction> differentialFunctionsOfCol = colToDFsGroup.get(col);
        List<Double> thresholds = col2Thresholds.get(col);
        for (int i = 0; i < thresholds.size(); i++) {
            double threshold = thresholds.get(i);
            DifferentialFunctionSet mask = new DifferentialFunctionSet();
            for(DifferentialFunction df: differentialFunctionsOfCol){
                if(df.getOperator() ==Operator.LESS_EQUAL && df.getDistance() >= threshold){
                    mask.add(df);
                }else if(df.getOperator() == Operator.GREATER && df.getDistance() < threshold){
                    mask.add(df);
                }
            }
            predicateSets.add(mask.getLongBitSet());
        }
        DifferentialFunctionSet mask = new DifferentialFunctionSet();
        double threshold = thresholds.get(thresholds.size() - 1);
        for(DifferentialFunction df: differentialFunctionsOfCol){
            if(df.getOperator() == Operator.GREATER){
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
        return colToDFsGroup.size();
    }

    /**
     * @return predicates of {@param col}
     */
    public List<DifferentialFunction> getDFByCol(int col) {
        return colToDFsGroup.get(col);
    }

    /**
     * @return Predicates Size of {@param col}
     */
    public int getColPredicatesSize(int col) {
        return colToDFsGroup.get(col).size();
    }

    /**
     * @return Thresholds size of {@param col}
     */
    public int getColThresholdsSize(int col) {
        return col2Thresholds.get(col).size();
    }

    public int getPredicateId(DifferentialFunction differentialFunction) {
        return dfIdProvider.getIndex(differentialFunction);
    }

    public DFProvider getDfProvider() {
        return dfProvider;
    }

    public IndexProvider<DifferentialFunction> getPredicateIdProvider() {
        return dfIdProvider;
    }

    public List<BitSet> getColDFGroup() {
        if (colDFGroup == null) {
            colDFGroup = new ArrayList<>();
            for (int i = 0; i < getColSize(); i++) {
                BitSet bs = new BitSet();
                for (DifferentialFunction pred : getDFByCol(i)) {
                    bs.set(dfIdProvider.getIndex(pred));
                }
                colDFGroup.add(bs);
            }
        }
        return colDFGroup;
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
        longDFsGroup = new ArrayList<>();
        doubleDFsGroup = new ArrayList<>();
        strDFsGroup = new ArrayList<>();
        colToDFsGroup = new HashMap<>();
        col2Thresholds = new HashMap<>();
    }

    private List<Double> dedup(List<Double> list){
        Set<Double> ret = new HashSet<>(list);
        return new ArrayList<>(ret);
    }

    private void buildBitSetIndexMap(){
        bitsetIndex2ThresholdsIndex = new HashMap<>();
        for(int col: colToDFsGroup.keySet()){
            List<DifferentialFunction> dfs = colToDFsGroup.get(col);
            for(int i =0; i <  dfs.size(); i++){
                DifferentialFunction df = dfs.get(i);
                int index = dfIdProvider.getIndex(df);
                assert col2Thresholds.get(col).get(i) == df.getDistance();
                bitsetIndex2ThresholdsIndex.put(index, i);
            }
        }
    }

    public Map<Integer, Integer> getBitsetIndex2ThresholdsIndex() {
        return bitsetIndex2ThresholdsIndex;
    }

    private List<Double> roundList(List<Double> thresholds, boolean isSmaller){
        List<Double> ret = new ArrayList<>();
        for(Double t: thresholds){
            ret.add(Math.round(t*1000)*1.0/1000);
        }
        return ret;
    }
}
