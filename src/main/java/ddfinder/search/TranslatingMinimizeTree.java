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
        this.predicateId2NodeId = predicateId2NodeId.clone();
        this.colSize = colSize;
        this.col2Interval = col2Interval.clone();
        this.col2PredicateId = col2PredicateId.clone();
        this.intervalLength = intervalLength;
        search = new MinimizeTree();
        /*for(int i = 0; i < predicateId2NodeId.length; i++){
            System.out.println("predicate " + i + " to node " + predicateId2NodeId[i]);
        }
        for(int i = 0; i < col2Interval.length; i++){
            System.out.println("col "+i+" to interval "+col2Interval[i]);
        }*/
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
       // LongBitSet bss = new LongBitSet("00001000000000000100");
       // if(candidate.equals(bss)){System.out.println("ssssssssssssss");}
        for(int i = candidate.nextSetBit(0); i >= 0; i = candidate.nextSetBit(i + 1)){
            // <=, <=, <= , > , >
            int nodeId = predicateId2NodeId[i];
            boolean isGreater = nodeId >= colSize;
            int col = nodeId % colSize;
            int diff = (i - col2PredicateId[col]) % intervalLength[col];
            if(isGreater){
                for(int j = col2Interval[col] ; j < col2Interval[col] + diff + 1; j++){
                    transformed.clear(j);
                }
            }else{
                diff = intervalLength[col] - diff - 1;
                if(diff<0){assert false;}
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
                    return o2.compareTo(o1);
                }
                return o1.cardinality() - o2.cardinality();
            }
        });
        Set<IBitSet> list = new HashSet<>();
        for(IBitSet candidate: candidates){
            IBitSet removedBy = search.addTree(transform2Bitset(candidate), transform2Nodes(candidate));
            if(removedBy == null){
                list.add(candidate);
            }//else{
               // LongBitSet bss = new LongBitSet("000010 000000 0000 0100");
                //100000 0011 11 11 1111
//                //110000 0011 11 11 1111
                 //if(candidate.equals(bss))
               // System.out.println("candidate " + candidate + " transform to " + transform2Bitset(candidate) + "removed by " + removedBy);
            //}
        }
        return list;
    }
}
