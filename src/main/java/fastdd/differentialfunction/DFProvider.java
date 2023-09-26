package fastdd.differentialfunction;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tristonK 2022/12/31
 */
public class DFProvider {
    private final Map<Operator, Map<ColumnOperand<?>, Map<Double, DifferentialFunction>>> predicates;

    public DFProvider() {
        predicates = new HashMap<>();
    }

    public DifferentialFunction getPredicate(Operator op, ColumnOperand<?> op1, Double distance) {
        return getPredicate(op, op1, distance, true);
    }

    public DifferentialFunction getPredicate(Operator op, ColumnOperand<?> op1, Double distance, boolean accepted) {
        Map<Double, DifferentialFunction> map = predicates.computeIfAbsent(op, a -> new HashMap<>()).computeIfAbsent(op1, a -> new HashMap<>());
        DifferentialFunction p = map.get(distance);
        if (p == null) {
            p = new DifferentialFunction(op, distance, op1, accepted);
            map.put(distance, p);
        }
        return p;
    }
}
