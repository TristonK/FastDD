package fastdd.dfset.longclueimpl;

import fastdd.differentialfunction.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public abstract class LongClueSetBuilder {
    abstract public HashMap<Long, Long> buildClueSet();
    private final double ERR = 0.000000001;

    HashMap<Long, Long> accumulateClues(long[]... clueArrays) {
        HashMap<Long, Long> clueSet = new HashMap<>();
        for (long[] map : clueArrays) {
            for (long clue : map) {
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
        long base;
        public PredicatePack(ParsedColumn<?> column, long base){
            this.colIndex = column.getIndex();
            this.thresholds = new ArrayList<>(column.getThresholds());
            this.base = base;
        }
    }


    public static long[] bases;

    static List<PredicatePack> strPacks;  // String single-column predicate packs
    static List<PredicatePack> doublePacks;  // numerical single-column predicate packs
    static List<PredicatePack> longPacks;

   public static long calDiffTime = 0;

    public static void configure(DifferentialFunctionBuilder pBuilder) {
        strPacks = new ArrayList<>();
        doublePacks = new ArrayList<>();
        longPacks = new ArrayList<>();
        bases = new long[pBuilder.getColSize()];
        buildPredicateGroupsAndCorrectMap(pBuilder);
    }

    private static void buildPredicateGroupsAndCorrectMap(DifferentialFunctionBuilder pBuilder) {
        List<Integer> strPredicatesGroup = pBuilder.getStrPredicatesGroup();
        List<Integer> longPredicatesGroup = pBuilder.getLongPredicatesGroup();
        List<Integer> doublePredicatesGroup = pBuilder.getDoublePredicatesGroup();

        bases = new long [strPredicatesGroup.size() + longPredicatesGroup.size() + doublePredicatesGroup.size()];

        long count = 1;
        for(Integer colIndex: longPredicatesGroup){
            longPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }
        for(Integer colIndex: doublePredicatesGroup){
            doublePacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }
        for(Integer colIndex: strPredicatesGroup){
            strPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }

        System.out.println("  [CLUE] max base in clue: " + count);
    }
}
