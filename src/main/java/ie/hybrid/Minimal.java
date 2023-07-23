package ie.hybrid;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.DifferentialFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ie.hybrid.SearchSpace.dfBuilder;

/**
 * @author tristonK 2023/7/15
 */
public class Minimal {
    public DifferentialDependencySet minimize(DifferentialDependencySet ddSet) {
        //TODO
        Set<Integer> minimizeIndex = new HashSet<>();
        List<DifferentialDependency> dds = new ArrayList<>(ddSet.getDependencies());
        for (int i = 0; i < dds.size(); i++) {
            DifferentialDependency pivot = dds.get(i);
//            LongBitSet pivotPredicate = new LongBitSet(pivot.getPredicateSet());
            LongBitSet pivotLeftPredicate = new LongBitSet(pivot.getLeftPredicateSet());
            DifferentialFunction pivotRight = pivot.getRight();

            for (int j = i + 1; j < dds.size(); j++) {
                DifferentialDependency later = dds.get(j);
//                LongBitSet laterPredicate = new LongBitSet(later.getPredicateSet());
                LongBitSet laterLeftPredicate = new LongBitSet(later.getLeftPredicateSet());
                DifferentialFunction laterRight = later.getRight();
                //先判断右边单属性是否满足删除条件
                if (isRightReduce(pivotRight, laterRight)) {
                    //进行leftReduce判断，记录需要被删去的dd下标
                    if (isLeftReduce(pivotLeftPredicate, laterLeftPredicate)) {//pivot的LHS属性被later的所包含，并且pivot对应的属性范围更大
                        minimizeIndex.add(j);
                        continue;//跳过反向判断
                    }
                }
                ////反过来判断一遍，保证不被dd的顺序影响
                if (isRightReduce(laterRight, pivotRight)) {
                    if (isLeftReduce(laterLeftPredicate, pivotLeftPredicate)) {//later的LHS属性被pivot的所包含，并且later对应的属性范围更大
                        minimizeIndex.add(i);
                    }
                }
            }
        }
        //去除minimizeIndex中所含下标的dd，完成minimize
        if(minimizeIndex.isEmpty()){
            return ddSet;
        }
        DifferentialDependencySet minimizeDdSet = new DifferentialDependencySet();
        for(int i =0;i<ddSet.size();i++){
            if(!minimizeIndex.contains(i)){
                minimizeDdSet.add(dds.get(i));
            }
        }
        return minimizeDdSet;
    }

    // 在a的属性上，a表示的范围更大，返回true
    // 也就是 a subsumes b on projection of Attrs(a)
    private boolean isLeftReduce(LongBitSet a, LongBitSet b) {
        if (a.cardinality() > b.cardinality()) {
            return false;
        }
        int last = b.nextSetBit(0);
        for (int i = a.nextSetBit(0); i >= 0; i = a.nextSetBit(i + 1)) {
            int opHash = dfBuilder.getPredicateIdProvider().getObject(i).operandWithOpHash();
            for (; last >= 0; last = b.nextSetBit(last + 1)) {
                if (dfBuilder.getPredicateIdProvider().getObject(last).operandWithOpHash() == opHash) {
                    break;
                }
            }
            if (last < 0) {
                return false;
            }
            if (last < i) {
                return false;
            }
        }
        return true;
    }

    // 若a所表达的范围比b更小，满足b规约a条件，返回true，反之返回false
    private boolean isRightReduce(DifferentialFunction a, DifferentialFunction b) {
        if (a.getOperator().equals(b.getOperator()) && a.getOperand().getColumn().equals(b.getOperand().getColumn())) {
            if (a.getOperator().toString().equals("LESS_EQUAL") && a.getDistance() <= b.getDistance()) {
                return true;
            } else return a.getOperator().toString().equals("GREATER") && a.getDistance() >= b.getDistance();
        }
        return false;
    }
}
