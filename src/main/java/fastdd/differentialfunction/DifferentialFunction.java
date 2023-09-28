package fastdd.differentialfunction;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

/**
 * @author tristonK 2022/12/29
 */
public class DifferentialFunction {
    private final Operator operator;
    private final double distance;
    private final ColumnOperand operand;
    private int index;

    private static DFProvider predicateProvider;

    public static void configure(DFProvider provider) {
        DifferentialFunction.predicateProvider = provider;
    }

    private final boolean accepted;

    public DifferentialFunction(Operator op, double distance, ColumnOperand<?> operand){
        this(op, distance, operand, true);
    }

    public DifferentialFunction(Operator op, double distance, ColumnOperand<?> operand, boolean accepted){
        if (op == null) {
            throw new IllegalArgumentException("Operator must not be null.");
        }
        if (operand == null) {
            throw new IllegalArgumentException("Operand must not be null.");
        }
        this.operator = op;
        this.operand = operand;
        this.distance = distance;
        this.accepted = accepted;
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
        return "["+operand.getColumn().getColumnName() + "(" + operator.getShortString() + distance + ")]";
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

        DifferentialFunction other = (DifferentialFunction) obj;
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

    public DifferentialFunction getInversePredicate(){
        if(operator == Operator.LESS_EQUAL){
            return predicateProvider.getPredicate(Operator.GREATER, operand, distance);
        }
        return predicateProvider.getPredicate(Operator.LESS_EQUAL, operand, distance);
    }

    /**
     * @return 0: not for a column or not the same operator
     * 1: this have a bigger distance
     * -1: p has a bigger distance
     * */
    public int compare(DifferentialFunction p){
        if(p.operand.equals(this.operand)  && p.operator.equals(this.operator)){
            int flag = operator == Operator.LESS_EQUAL? 1 : -1;
            if(this.distance >= p.distance){
                return flag;
            }else {
                return -flag;
            }
        }
        return 0;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setIndex(int index){
        this.index = index;
    }
    public int getIndex(){
        return index;
    }

    public int operandWithOpHash(){
        return operand.hashCode() * 31 + operator.hashCode();
    }
}
