package ddfinder.differentialdependency;

import ddfinder.differentialfunction.DifferentialFunction;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateSet;

/**
 * @author tristonk
 */
public class DifferentialDependency {

    private final DifferentialFunction left, right;

    public DifferentialDependency(DifferentialFunction left, DifferentialFunction right){
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
        for(Predicate p : left.getPredicates()){
            result += p.hashCode();
        }
        for(Predicate p: right.getPredicates()){
            result += p.hashCode();
        }
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
        if (left.getPredicates().size() != other.left.getPredicates().size() || right.getPredicates().size() != right.getPredicates().size()){
            return false;
        } else {
           return left.getPredicates().equals(other.left.getPredicates()) && right.getPredicates().equals(other.right.getPredicates());
        }
    }
}
