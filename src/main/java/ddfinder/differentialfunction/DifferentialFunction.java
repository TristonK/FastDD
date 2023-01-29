package ddfinder.differentialfunction;

import ddfinder.predicate.Predicate;
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
        for (Predicate predicate : this.predicates) {
            if (count == 0) {
                sb.append(predicate.toString());
            } else {
                sb.append(AND + predicate.toString());
            }
            count++;
        }
        return sb.toString();
    }
}
