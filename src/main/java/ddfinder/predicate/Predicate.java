package ddfinder.predicate;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

/**
 * @author tristonK 2022/12/29
 */
public class Predicate {
    private final Operator operator;
    private final double distance;
    private final ColumnOperand operand;

    private static PredicateProvider predicateProvider;

    public static void configure(PredicateProvider provider) {
        Predicate.predicateProvider = provider;
    }

    public Predicate(Operator op, double distance, ColumnOperand<?> operand){
        if (op == null) {
            throw new IllegalArgumentException("Operator must not be null.");
        }
        if (operand == null) {
            throw new IllegalArgumentException("Operand must not be null.");
        }

        this.operator = op;
        this.operand = operand;
        this.distance = distance;
    }

    public ColumnOperand getOperand() {
        return operand;
    }

    public double getDistance() {
        return distance;
    }

    public Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return "["+operand.toString() + "(" + operator.getShortString() + distance + ")]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operator.hashCode();
        result = prime * result + operand.hashCode();
        result = prime * result + (int)distance;
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

        Predicate other = (Predicate) obj;
        if (operator != other.operator) {
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }

        return distance == other.getDistance();
    }

}