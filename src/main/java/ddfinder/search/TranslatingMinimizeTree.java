package ddfinder.search;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

import java.util.*;

/**
 * @author tristonK 2023/4/26
 */
public class TranslatingMinimizeTree {
    private int intervalSize;

    // first leq for all col, then greater
    private int[] predicateId2NodeId;

    //col -> first interval index in clue bitset
    int[] col2Interval;

    int[] intervalLength;
    int colSize;
    // col -> first predicate index in predicate bitset
    int[] col2PredicateId;
    MinimizeTree search;

    public TranslatingMinimizeTree(int intervalSize, int[] predicateId2NodeId, int[] col2Interval,
                                   int[] col2PredicateId, int colSize, int[] intervalLength){
        this.intervalSize = intervalSize;
        this.predicateId2NodeId = predicateId2NodeId;
        this.colSize = colSize;
        this.col2Interval = col2Interval;
        this.col2PredicateId = col2PredicateId;
        this.intervalLength = intervalLength;
        search = new MinimizeTree();
    }

    private List<Integer> transform2Nodes(IBitSet candidate){
        ArrayList<Integer> nodes = new ArrayList<>();
        for(int i = candidate.nextSetBit(0); i >= 0; i = candidate.nextSetBit(i + 1)){
           nodes.add(predicateId2NodeId[i]);
        }
        return nodes;
    }

    private IBitSet transform2Bitset(IBitSet candidate){
        LongBitSet transformed = LongBitSet.FACTORY.createAllSet(intervalSize);
        for(int i = candidate.nextSetBit(0); i >= 0; i = candidate.nextSetBit(i + 1)){
            // <=, <=, <= , > , >
            int nodeId = predicateId2NodeId[i];
            boolean isGreater = nodeId >= colSize;
            int col = nodeId % colSize;
            int diff = (i - col2PredicateId[col]) % intervalLength[col];
            if(isGreater){
                for(int j = col2Interval[col] + intervalLength[col] - 1 - diff; j >= col2Interval[col]; j--){
                    transformed.clear(j);
                }
            }else{
                diff = intervalLength[col] - diff;
                for(int j = col2Interval[col] + diff; j < col2Interval[col] + intervalLength[col]; j++){
                    transformed.clear(j);
                }
            }
        }
        return transformed;
    }

    private IBitSet retransform2DD(IBitSet transformed, List<Integer> nodes){
        //TODO
        return null;
    }

    public Set<IBitSet> minimize(List<IBitSet> candidates){
        Collections.sort(candidates, new Comparator<IBitSet>() {
            @Override
            public int compare(IBitSet o1, IBitSet o2) {
                if (o1.cardinality() == o2.cardinality()){
                    return o1.compareTo(o2);
                }
                return o1.cardinality() - o2.cardinality();
            }
        });
        Set<IBitSet> list = new HashSet<>();
        for(IBitSet candidate: candidates){
            if(search.addTree(transform2Bitset(candidate), transform2Nodes(candidate))){
                list.add(candidate);
            }
        }
        return list;
    }
}
