package ie.hybrid;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.*;

/**
 * @author tristonK 2023/7/16
 */
public class SearchSpace {
    // set of diff functions
    public List<LongBitSet> phis;
    public static DifferentialFunctionBuilder dfBuilder;

    public static void configure(DifferentialFunctionBuilder builder){
        dfBuilder = builder;
    }
    public SearchSpace(int right){
        phis = new ArrayList<>();
        getPhis(dfBuilder.getColDFGroup(), 0, new LongBitSet(dfBuilder.size()), right);
        phis.sort(new Comparator<IBitSet>() {
            @Override
            public int compare(IBitSet o1, IBitSet o2) {
                if (o1.cardinality() == o2.cardinality()) {
                    return o2.compareTo(o1);
                }
                return o1.cardinality() - o2.cardinality();
            }
        });
    }

    public SearchSpace(List<LongBitSet> phis){
        this.phis = phis;
    }

    private void getPhis(List<BitSet> colDfs, int currIndex, LongBitSet bs, int rightCol){
        if(currIndex == colDfs.size()){
            if(bs.cardinality() != 0){
                phis.add(bs.clone());
            }
            return;
        }
        BitSet curColDFs = colDfs.get(currIndex);
        getPhis(colDfs, currIndex + 1, bs, rightCol);
        if(curColDFs.get(rightCol)){return;}
        for(int i = curColDFs.nextSetBit(0); i >= 0; i = curColDFs.nextSetBit(i+1)){
            bs.set(i);
            getPhis(colDfs, currIndex + 1, bs, rightCol);
            bs.clear(i);
        }

    }

    public List<SearchSpace> extractPositive(LongBitSet pivot){
        List<LongBitSet> phi1 = new ArrayList<>();
        List<LongBitSet> phi2 = new ArrayList<>();
        for(LongBitSet bs: phis){
            if(bs == pivot){continue;}
            if(isLeftReduce(pivot, bs)){
                    phi1.add(bs);
            }else{
                phi2.add(bs);
            }
        }
        return List.of(new SearchSpace(phi1), new SearchSpace(phi2));
    }

    public List<SearchSpace> extractNegative(LongBitSet pivot){
        List<LongBitSet> phi1 = new ArrayList<>();
        List<LongBitSet> phi2 = new ArrayList<>();
        for(LongBitSet bs: phis){
            if(isLeftReduce(bs, pivot)){
                phi1.add(bs);
            }else{
                phi2.add(bs);
            }
        }
        return List.of(new SearchSpace(phi1), new SearchSpace(phi2));
    }

    private boolean isLeftReduce(LongBitSet a, LongBitSet b){
        if (a.cardinality() > b.cardinality()){
            return false;
        }
        int last = b.nextSetBit(0);
        for(int i = a.nextSetBit(0); i >= 0; i  = a.nextSetBit(i + 1)){
            int opHash = dfBuilder.getPredicateIdProvider().getObject(i).operandWithOpHash();
            for(; last >= 0; last = b.nextSetBit(last + 1)){
                if(dfBuilder.getPredicateIdProvider().getObject(last).operandWithOpHash() == opHash){
                    break;
                }
            }
            if(last < 0){return false;}
            if(last < i){return false;}
        }
        return true;
    }

    public int size(){
        return phis.size();
    }
}
