package ddfinder.differentialfunction;

import ddfinder.predicate.PredicateSet;

/**
 * @author tristonk
 */
public class DifferentialFunction {
    private final PredicateSet predicates;
    public DifferentialFunction(PredicateSet predicateSet){
        this.predicates = predicateSet;
    }

    public PredicateSet getPredicates() {
        return predicates;
    }

    public static final String AND = " ∧ ";
    public String toString(){
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (ddfinder.predicate.DifferentialFunction differentialFunction : this.predicates) {
            if (count == 0) {
                sb.append(differentialFunction.toString());
            } else {
                sb.append(AND + differentialFunction.toString());
            }
            count++;
        }
        return sb.toString();
    }
}
