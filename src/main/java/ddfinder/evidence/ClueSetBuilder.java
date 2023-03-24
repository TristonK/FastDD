package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.DoublePli;
import ddfinder.pli.IPli;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

abstract public class ClueSetBuilder {

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

    public static void configure(PredicateBuilder pBuilder) {
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

    private static void buildPredicateGroupsAndCorrectMap(PredicateBuilder pBuilder) {
        List<Integer> strPredicatesGroup = pBuilder.getStrPredicatesGroup();
        List<Integer> longPredicatesGroup = pBuilder.getLongPredicatesGroup();
        List<Integer> doublePredicatesGroup = pBuilder.getDoublePredicatesGroup();

        bit2ColMap = new int[PredicateBuilder.getIntervalCnt()];
        col2FirstBitMap = new int [strPredicatesGroup.size() + longPredicatesGroup.size() + doublePredicatesGroup.size()];

        int count = 0;
        for(Integer colIndex: longPredicatesGroup){
            intPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
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

    int[] linerCountDouble(Double[] keys, int startPos, double key, List<Double> thresholds){
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        // > key
        while(pos < keys.length && keys[pos] - key > ERR){
            // handle diff > max(thresholds)
            while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId) + ERR){
                posTothreshold[pos - startPos] = thresholdsId+1;
                pos++;
            }
            if(pos == keys.length){break;}
            // handle diff <= max(thresholds)
            while(thresholdsId > 0 && keys[pos] - key < thresholds.get(thresholdsId - 1) + ERR){
                thresholdsId--;
            }
            posTothreshold[pos-startPos] = thresholdsId;
            pos++;
        }
        // = key
        if(pos < keys.length && Math.abs(key - keys[pos]) < ERR){
            posTothreshold[pos - startPos] = 0;
            pos++;
        }

        // < key
        thresholdsId = 1;
        for(; pos < keys.length; pos++){
            while(thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId) + ERR){
                thresholdsId++;
            }
            //handle diff > max(thresholds)
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }

    int[] linerCountInt(Integer[] keys, int startPos, int key, List<Double> thresholds){
        int pos = startPos;
        int[] posTothreshold = new int[keys.length - startPos];
        int thresholdsId = thresholds.size() - 1;
        // > key
        while(pos < keys.length && key < keys[pos]){
            // handle diff > max(thresholds)
            while (pos < keys.length && keys[pos] - key > thresholds.get(thresholdsId)){
                posTothreshold[pos - startPos] = thresholdsId+1;
                pos++;
            }
            if(pos == keys.length){break;}
            // handle diff <= max(thresholds)
            while(thresholdsId > 0 && keys[pos] - key <= thresholds.get(thresholdsId - 1)){
                thresholdsId--;
            }
            posTothreshold[pos-startPos] = thresholdsId;
            pos++;
        }
        // = key
        if(pos < keys.length && key == keys[pos]){
            posTothreshold[pos - startPos] = 0;
            pos++;
        }
        // < key
        thresholdsId = 1;
        for(; pos < keys.length; pos++){
            while(thresholdsId < thresholds.size() && key - keys[pos] > thresholds.get(thresholdsId)){
                thresholdsId++;
            }
            //handle diff > max(thresholds)
            posTothreshold[pos - startPos] = thresholdsId;
        }
        return posTothreshold;
    }


}

