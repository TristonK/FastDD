package ddfinder.differentialdependency;

import ch.javasoft.bitset.IBitSet;
import ddfinder.differentialfunction.DifferentialFunction;
import ddfinder.predicate.IntervalPredicate;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateSet;

import java.util.List;

/**
 * @author tristonk
 */
public class DifferentialDependency {

    private List<Predicate> left;
    private Predicate right;

    private IBitSet predicateSet;
    private IBitSet leftPredicateSet;

    public DifferentialDependency(List<Predicate> left, Predicate right, IBitSet predicateSet, IBitSet leftPredicates){
        if(left == null || right == null){
            throw new IllegalArgumentException("DifferentialFunction should not be null.");
        }
        this.left = left;
        this.right = right;
        this.predicateSet = predicateSet;
        this.leftPredicateSet = leftPredicates;
    }

    public static final String AND = " ∧ ";
    public String toString(){

        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        for(Predicate predicate : left){
            if(first){
                sb.append(predicate.toString());
                first = false;
            }else{
                sb.append(AND).append(predicate.toString());
            }
        }
        sb.append(" -> ").append(right.toString()).append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 0;
        int prime1 = 23;
        for(Predicate p : left){
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

    public IBitSet getPredicateSet() {
        return predicateSet;
    }

    public IBitSet getLeftPredicateSet() {
        return leftPredicateSet;
    }
}
