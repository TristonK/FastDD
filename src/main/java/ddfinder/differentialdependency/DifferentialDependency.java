package ddfinder.differentialdependency;

import ddfinder.differentialfunction.DifferentialFunction;
import ddfinder.predicate.IntervalPredicate;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateSet;

import java.util.List;

/**
 * @author tristonk
 */
public class DifferentialDependency {

    private List<IntervalPredicate> left;
    private IntervalPredicate right;

    public DifferentialDependency(List<IntervalPredicate> left, IntervalPredicate right){
        if(left == null || right == null){
            throw new IllegalArgumentException("DifferentialFunction should not be null.");
        }
        this.left = left;
        this.right = right;
    }

    public String toString(){
        return "{ " +
                left.toString() +
                " -> " +
                right.toString() +
                " }";
    }

    @Override
    public int hashCode() {
        int result = 0;
        int prime1 = 23;
        for(IntervalPredicate p : left){
            result += p.hashCode();
        }
        result += right.hashCode() * prime1;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        DifferentialDependency other = (DifferentialDependency) obj;
        if (left.size() != other.left.size()){
            return false;
        } else {
           return left.equals(other.left) && right.equals(other.right);
        }
    }
}
