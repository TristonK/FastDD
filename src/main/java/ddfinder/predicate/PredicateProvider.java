package ddfinder.predicate;

import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.operands.ColumnOperand;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tristonK 2022/12/31
 */
public class PredicateProvider {
    private final Map<Operator, Map<ColumnOperand<?>, Map<Integer, Predicate>>> predicates;

    public PredicateProvider() {
        predicates = new HashMap<>();
    }

    public Predicate getPredicate(Operator op, ColumnOperand<?> op1, Integer distance) {
        Map<Integer, Predicate> map = predicates.computeIfAbsent(op, a -> new HashMap<>()).computeIfAbsent(op1, a -> new HashMap<>());
        Predicate p = map.get(distance);
        if (p == null) {
            p = new Predicate(op, distance, op1);
            map.put(distance, p);
        }
        return p;
    }
}
