package ddfinder.predicate;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


import java.io.File;
import java.util.*;

/**
 * @author tristonK 2022/12/29
 */
public class PredicateBuilder {
    private List<Predicate> predicates;
    private final PredicateProvider predicateProvider;
    private final IndexProvider<Predicate> predicateIdProvider;

    private List<Integer> numericPredicatesGroup;

    private List<Integer> longPredicatesGroup;

    private List<Integer> doublePredicatesGroup;
    private List<Integer> strPredicatesGroup;

    private static int intervalCnt;

    private Map<Integer, List<Predicate>> colToPredicatesGroup;

    public PredicateBuilder(Input input){
        intervalCnt = 0;
        predicates = new ArrayList<>();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
        longPredicatesGroup = new ArrayList<>();
        doublePredicatesGroup = new ArrayList<>();
        strPredicatesGroup = new ArrayList<>();
        colToPredicatesGroup = new HashMap<>();
        for(ParsedColumn<?> column: input.getColumns()){
            addPredicates(column, 0, 5);
        }
        predicateIdProvider.addAll(predicates);
        Predicate.configure(predicateProvider);
        PredicateSet.configure(predicateIdProvider);
    }

    public PredicateBuilder(File index, Input input){
        predicates = new ArrayList<>();
        predicateProvider = new PredicateProvider();
        predicateIdProvider = new IndexProvider<>();
    }

    public List<Predicate> getPredicates() {
        return predicates;
    }

    public int size(){return predicates.size();}

    /**
     * mode:
     *  - simple(0): 0~maxThr with num threshold
     *  - log(1): 0 ~ log(Thershold)
     * */
    private void addPredicates(ParsedColumn<?> column, int mode, int threshold){
        List<Double> thresholds = new ArrayList<>();
        Double diffD = 5.0;
        if(column.isNum()){
            diffD =  column.getMaxNum() - column.getMinNum();
        }
        if(mode == 0 || !column.isNum()){
            //TODO:修改String属性列情况
            double step = diffD/(threshold+1);
            for(int i = 0; i < threshold; i++){
                thresholds.add(i*step);
            }
        } else if(mode == 1){
            //TODO
        } else{
            throw  new IllegalArgumentException("Bad add predicates mode.");
        }
        column.setThresholds(thresholds);
        List<Predicate> partialPredicates = new ArrayList<>();
        ColumnOperand<?> operand = new ColumnOperand<>(column, 0);
        for(int i = threshold - 1; i >= 0; i--){
            partialPredicates.add(predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, thresholds.get(i)));
        }
        for(int i = 0; i< threshold; i++){
            partialPredicates.add(predicateProvider.getPredicate(Operator.GREATER, operand, thresholds.get(i)));
        }
        predicates.addAll(partialPredicates);
        colToPredicatesGroup.put(column.getIndex(), partialPredicates);
        intervalCnt += thresholds.size() + 1;
        if(column.isLong()){
            longPredicatesGroup.add(column.getIndex());
        } else if(column.isDouble()){
          doublePredicatesGroup.add(column.getIndex());
        } else{
            strPredicatesGroup.add(column.getIndex());
        }
    }

    public List<Integer> getNumericPredicatesGroup() {
        return numericPredicatesGroup;
    }

    public List<Integer> getStrPredicatesGroup() {
        return strPredicatesGroup;
    }

    public List<Integer> getLongPredicatesGroup() {return longPredicatesGroup;}

    public List<Integer> getDoublePredicatesGroup() {return doublePredicatesGroup;}

    public static int getIntervalCnt() {return  intervalCnt;}

    public ParsedColumn<?> getPredicateColumn(int colIndex){
        return colToPredicatesGroup.get(colIndex).get(0).getOperand().getColumn();
    }

    public List<LongBitSet> getColPredicateSet(int col){
        List<LongBitSet> predicateSets = new ArrayList<>();
        List<Predicate> predicatesOfCol = colToPredicatesGroup.get(col);
        int thresholdSize = predicatesOfCol.size()/2;
        for(int i = 0; i < thresholdSize + 1; i++){
            PredicateSet mask = new PredicateSet();
            for(int j = 0; j < thresholdSize - i; j++){
                mask.add(predicatesOfCol.get(j));
            }
            for(int j = thresholdSize; j < thresholdSize + i; j++){
                mask.add(predicatesOfCol.get(j));
            }
            predicateSets.add(mask.getLongBitSet());
        }
        return predicateSets;
    }
}
