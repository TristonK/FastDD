package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

abstract public class ClueSetBuilder{


    abstract public HashMap<LongBitSet, Long> buildClueSet();


    private final double ERR = 0.000000001;

    HashMap<LongBitSet, Long> accumulateClues(LongBitSet[]... clueArrays) {
        HashMap<LongBitSet, Long> clueSet = new HashMap<>();
        for (LongBitSet[] map : clueArrays) {
            for (LongBitSet clue : map) {
                clueSet.merge(clue, 1L, Long::sum);
            }
        }
        return clueSet;
    }

    /**
     * thresholds of one column
     */
    static class PredicatePack {
        List<Double> thresholds;

        int colIndex;

        int pos;
        public PredicatePack(ParsedColumn<?> column, int pos){
            this.colIndex = column.getIndex();
            this.thresholds = new ArrayList<>(column.getThresholds());
            this.pos = pos;
        }
    }

    /**
     * bit pos -> colIndex
    */
     static int[] bit2ColMap;
     static int[] col2FirstBitMap;

    static List<ClueSetBuilder.PredicatePack> strPacks;  // String single-column predicate packs
    static List<ClueSetBuilder.PredicatePack> doublePacks;  // numerical single-column predicate packs
    static List<ClueSetBuilder.PredicatePack> intPacks;

    static List<Integer> startPositions;

    static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> stringDistance;

    public static void configure(DifferentialFunctionBuilder pBuilder) {
        strPacks = new ArrayList<>();
        doublePacks = new ArrayList<>();
        intPacks = new ArrayList<>();
        stringDistance = new ConcurrentHashMap<>();
        startPositions = new ArrayList<>();
        buildPredicateGroupsAndCorrectMap(pBuilder);
    }

    static public int[] getCorrectionMap() {
        return bit2ColMap;
    }

    private static void buildPredicateGroupsAndCorrectMap(DifferentialFunctionBuilder pBuilder) {
        List<Integer> strPredicatesGroup = pBuilder.getStrPredicatesGroup();
        List<Integer> longPredicatesGroup = pBuilder.getLongPredicatesGroup();
        List<Integer> doublePredicatesGroup = pBuilder.getDoublePredicatesGroup();

        bit2ColMap = new int[DifferentialFunctionBuilder.getIntervalCnt()];
        col2FirstBitMap = new int [strPredicatesGroup.size() + longPredicatesGroup.size() + doublePredicatesGroup.size()];



        int count = 0;
        for(Integer colIndex: longPredicatesGroup){
            intPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;//对于某个属性，n个阈值有n+1个interval，现在修改成n个，无视0阈值
            for(int i = count; i < count + interval; i++){
                bit2ColMap[i] = colIndex;
            }
            col2FirstBitMap[colIndex] = count;
            startPositions.add(count);
            count += interval;
        }
        for(Integer colIndex: doublePredicatesGroup){
            doublePacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            for(int i = count; i < count + interval; i++){
                bit2ColMap[i] = colIndex;
            }
            col2FirstBitMap[colIndex] = count;
            startPositions.add(count);
            count += interval;
        }
        for(Integer colIndex: strPredicatesGroup){
            strPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            for(int i = count; i < count + interval; i++){
                bit2ColMap[i] = colIndex;
            }
            col2FirstBitMap[colIndex] = count;
            startPositions.add(count);
            count += interval;
        }

        System.out.println("  [CLUE] # of bits in clue: " + count);
    }





}
