package fastdd.differentialdependency;

import ch.javasoft.bitset.IBitSet;
import fastdd.differentialfunction.DifferentialFunction;

import java.util.List;

/**
 * @author tristonk
 */
public class DifferentialDependency {

    private List<DifferentialFunction> left;
    private DifferentialFunction right;

    private IBitSet dfSet;
    private IBitSet leftDFSet;

    public DifferentialDependency(List<DifferentialFunction> left, DifferentialFunction right, IBitSet dfSet, IBitSet leftPredicates){
        if(left == null || right == null){
            throw new IllegalArgumentException("DifferentialFunction should not be null.");
        }
        this.left = left;
        this.right = right;
        this.dfSet = dfSet;
        this.leftDFSet = leftPredicates;
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

    public IBitSet getDfSet() {
        return dfSet;
    }

    public IBitSet getLeftDFSet() {
        return leftDFSet;
    }

    public DifferentialFunction getRight() {
        return right;
    }
}
