package ie.hybrid;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.DifferentialFunctionBuilder;
import ddfinder.predicate.PredicateSet;

import javax.swing.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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
        //int col = dfBuilder.getPredicateIdProvider().getObject(right).getOperand().getIndex();
        phis = new ArrayList<>();
        getPhis(dfBuilder.getColPredicateGroup(), 0, new LongBitSet(dfBuilder.size()), right);
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

    /* 返回值：
    * RET[0]: 找到属性集个数比 pivot 大并且能规约到 pivot 的(只在pivot对应的属性上)
    * RET[1]: 剩下的
    */
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

    /* 返回值：
     * RET[0]: 找到属性集个数比 pivot 小并且从 pivot 规约得到
     * RET[1]: 剩下的
     */
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

    // 在a的属性上，a表示的范围更大，返回true
    // 也就是 a subsumes b on projection of Attrs(a)
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
}
