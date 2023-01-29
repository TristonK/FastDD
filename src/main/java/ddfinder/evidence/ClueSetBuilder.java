package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.predicate.PredicateSet;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;


import java.util.*;

abstract public class ClueSetBuilder {

    abstract public HashLongLongMap buildClueSet();

    HashLongLongMap accumulateClues(long[]... clueArrays) {
        HashLongLongMap clueSet = HashLongLongMaps.newMutableMap();
        for (long[] map : clueArrays) {
            for (long clue : map) {
                clueSet.addValue(clue, 1L, 0L);
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

        List<Long> masks;

        public PredicatePack(ParsedColumn<?> column, int pos){
            this.colIndex = column.getIndex();
            this.thresholds = new ArrayList<>(column.getThresholds());
            this.masks = new ArrayList<>();
            for(int i = 0; i < thresholds.size() + 1; i++){
                masks.add((long) i <<pos);
            }
        }
    }

    // count/3 -> colIndex
    static int[] colMap;

    static List<ClueSetBuilder.PredicatePack> strPacks;  // String single-column predicate packs
    static List<ClueSetBuilder.PredicatePack> numPacks;  // numerical single-column predicate packs

    public static void configure(PredicateBuilder pBuilder) {
        strPacks = new ArrayList<>();
        numPacks = new ArrayList<>();

        buildPredicateGroupsAndCorrectMap(pBuilder);
    }

    static public int[] getCorrectionMap() {
        return colMap;
    }

    private static void buildPredicateGroupsAndCorrectMap(PredicateBuilder pBuilder) {
        List<Integer> numericPredicatesGroup = pBuilder.getNumericPredicatesGroup();
        List<Integer> strPredicatesGroup = pBuilder.getStrPredicatesGroup();

        colMap = new int[numericPredicatesGroup.size()+strPredicatesGroup.size()];

        int count = 0;

        for(Integer colIndex: numericPredicatesGroup){
            numPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            colMap[count/3] = colIndex;
            count += 3;
        }

        for(Integer colIndex: strPredicatesGroup){
            strPacks.add(new PredicatePack(pBuilder.getPredicateColumn(colIndex), count));
            colMap[count/3] = colIndex;
            count+=3;
        }

        System.out.println("  [CLUE] # of bits in clue: " + count);
        if (count > 64) {
            throw new UnsupportedOperationException("Too many predicates! Not supported yet!");
        }
    }


}

