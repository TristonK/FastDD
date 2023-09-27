package ie.hybrid;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunction;
import de.metanome.algorithms.dcfinder.predicates.Operator;

import java.util.*;

import static ie.hybrid.SearchSpace.dfBuilder;

/**
 * @author tristonK 2023/7/15
 */
public class Minimal {
    public DifferentialDependencySet minimize(DifferentialDependencySet ddSet) {
        Set<Integer> minimizeIndex = new HashSet<>();
        List<DifferentialDependency> dds = new ArrayList<>(ddSet.getDependencies());

        for (int i = 0; i < dds.size(); i++) {
            DifferentialDependency pivot = dds.get(i);
            LongBitSet pivotLeftPredicate = new LongBitSet(pivot.getLeftPredicateSet());
            DifferentialFunction pivotRight = pivot.getRight();

            for (int j = i + 1; j < dds.size(); j++) {
                DifferentialDependency later = dds.get(j);
                DifferentialFunction laterRight = later.getRight();
                if (pivotRight.operandWithOpHash() != laterRight.operandWithOpHash() || minimizeIndex.contains(j)){
                    continue;
                }
                LongBitSet laterLeftPredicate = new LongBitSet(later.getLeftPredicateSet());

                if (isRightReduce(pivotRight, laterRight)) {

                    if (isLeftReduce(pivotLeftPredicate, laterLeftPredicate)) {

                        minimizeIndex.add(j);
                        continue;
                    }
                }
                if (isRightReduce(laterRight, pivotRight)) {
                    if (isLeftReduce(laterLeftPredicate, pivotLeftPredicate)) {
                        minimizeIndex.add(i);
                        break;
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

    private boolean isRightReduce(DifferentialFunction a, DifferentialFunction b) {
        if (a.getOperator().equals(b.getOperator()) && a.getOperand().getColumn().equals(b.getOperand().getColumn())) {
            if (a.getOperator() == Operator.LESS_EQUAL && a.getDistance() <= b.getDistance()) {
                return true;
            } else return a.getOperator() == Operator.GREATER && a.getDistance() >= b.getDistance();
        }
        return false;
    }
}
