package ddfinder.predicate;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

import java.util.Objects;

/**
 * @author tristonK 2023/3/6
 */
public class IntervalPredicate {
    private final double ERR = 0.000000001;
    private double leftThreshold, rightThreshold;
    private String name;
    /***
     * inverse = flase: (left, right]
     * inverse = true: [-NaN, left] \cup (right, NaN)
     */
    private boolean inverse;

    private IntervalPredicate inversePredicate;

    public IntervalPredicate(String name, double leftThreshold, double rightThreshold){
        this(name, leftThreshold, rightThreshold, false, null);
    }

    public IntervalPredicate(String name, double leftThreshold, double rightThreshold, boolean inverse){
        this(name, leftThreshold, rightThreshold, inverse, null);
    }

    public IntervalPredicate(String name, double leftThreshold, double rightThreshold, boolean inverse, IntervalPredicate inversePredicate){
        if (name == null) {
            throw new IllegalArgumentException("Column name must not be null.");
        }
        this.name = name;
        this.leftThreshold = leftThreshold;
        this.rightThreshold = rightThreshold;
        this.inverse = inverse;
        this.inversePredicate = inversePredicate;
    }


    @Override
    public String toString() {
        if(leftThreshold < 0){
            if(inverse){return "["+ name +" > " + rightThreshold +"]";}
            else{return "["+ name +" <= " + rightThreshold +"]"; }
        } else if(rightThreshold < 0){
            if(inverse){return "["+ name +" <= " + leftThreshold +"]";}
            else{return "["+ name +" > " + leftThreshold +"]"; }
        }
        if(inverse){
            return "[" + name + " <= " + leftThreshold + " or " + name +" > " + rightThreshold +"]";
        }
        return "["+ leftThreshold + " < " + name + " <= " + rightThreshold + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + (int) leftThreshold;
        result = prime * result + (int) rightThreshold;
        result = prime * result + (inverse?1:0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        IntervalPredicate other = (IntervalPredicate) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (inverse != other.inverse){
            return false;
        }
        return Math.abs(leftThreshold-other.leftThreshold) < ERR && Math.abs(rightThreshold - other.rightThreshold) < ERR;
    }

    public IntervalPredicate getInversePredicate() {
        if(inversePredicate == null){
            inversePredicate = new IntervalPredicate(name, leftThreshold, rightThreshold, true, this);
        }
        return inversePredicate;
    }

    public double getRightThreshold() {
        return rightThreshold;
    }

    public double getLeftThreshold() {
        return leftThreshold;
    }

    public String getName() {
        return name;
    }

    public boolean isInverse() {
        return inverse;
    }
}
