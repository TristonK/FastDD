package fastdd.dfset.isnimpl;

import fastdd.differentialfunction.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public abstract class ISNBuilder {
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
    static class DFPack {
        List<Double> thresholds;
        int colIndex;
        long base;
        public DFPack(ParsedColumn<?> column, long base){
            this.colIndex = column.getIndex();
            this.thresholds = new ArrayList<>(column.getThresholds());
            this.base = base;
        }
    }


    public static long[] bases;

    static List<DFPack> strPacks;
    static List<DFPack> doublePacks;
    static List<DFPack> longPacks;

   public static long calDiffTime = 0;

    public static void configure(DifferentialFunctionBuilder pBuilder) {
        strPacks = new ArrayList<>();
        doublePacks = new ArrayList<>();
        longPacks = new ArrayList<>();
        bases = new long[pBuilder.getColSize()];
        buildDFGroupsAndCorrectMap(pBuilder);
    }

    private static void buildDFGroupsAndCorrectMap(DifferentialFunctionBuilder pBuilder) {
        List<Integer> strDFsGroup = pBuilder.getStrDFsGroup();
        List<Integer> longDFsGroup = pBuilder.getLongDFsGroup();
        List<Integer> doubleDFsGroup = pBuilder.getDoubleDFsGroup();

        bases = new long [strDFsGroup.size() + longDFsGroup.size() + doubleDFsGroup.size()];

        long count = 1;
        for(Integer colIndex: longDFsGroup){
            longPacks.add(new DFPack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }
        for(Integer colIndex: doubleDFsGroup){
            doublePacks.add(new DFPack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }
        for(Integer colIndex: strDFsGroup){
            strPacks.add(new DFPack(pBuilder.getPredicateColumn(colIndex), count));
            int interval  = pBuilder.getColThresholdsSize(colIndex) + 1;
            bases[colIndex] = count;
            count *= interval;
        }

        System.out.println("  [CLUE] max base in clue: " + count);
    }
}
