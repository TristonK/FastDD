package ddfinder.differentialdependency;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.DifferentialFunction;

import java.util.List;

/**
 * @author tristonk
 */
public class DifferentialDependency {

    private List<DifferentialFunction> left;
    private DifferentialFunction right;

    private IBitSet predicateSet;
    private IBitSet leftPredicateSet;

    public DifferentialDependency(List<DifferentialFunction> left, DifferentialFunction right, IBitSet predicateSet, IBitSet leftPredicates){
        if(left == null || right == null){
            throw new IllegalArgumentException("DifferentialFunction should not be null.");
        }
        this.left = left;
        this.right = right;
        this.predicateSet = predicateSet;
        this.leftPredicateSet = leftPredicates;
    }

    public DifferentialDependency(LongBitSet left, LongBitSet right){

    }

    public static final String AND = " ∧ ";
    public String toString(){

        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        for(DifferentialFunction differentialFunction : left){
            if(first){
                sb.append(differentialFunction.toString());
                first = false;
            }else{
                sb.append(AND).append(differentialFunction.toString());
            }
        }
        sb.append(" -> ").append(right.toString()).append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 0;
        int prime1 = 23;
        for(DifferentialFunction p : left){
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
