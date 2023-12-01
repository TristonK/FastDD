package fastdd.differentialdependency;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.List;

/**
 * @author tristonK 2023/12/1
 */
public class Parser {

    /*
    * Sample: { [phone(<=0.0)] -> [addr(<=0.0)]}
    * */
    String dd;
    public LongBitSet left, right;
    static DifferentialFunctionBuilder dfBuilder;

    public Parser(String dd){
        this.dd = dd;
        parse();
    }

    public static void configure(DifferentialFunctionBuilder dfBuilder){
        Parser.dfBuilder = dfBuilder;
    }

    public void parse(){
        dd = dd.substring(1, dd.length() -1);
        String[] token = dd.split(" -> ");
        String[] leftStrings = token[0].split(" ∧ ");
        left = new LongBitSet(); right = new LongBitSet();
        for(String leftS: leftStrings){
            DifferentialFunction df = parseDF(leftS.trim());
            int idx = dfBuilder.getPredicateIdProvider().getIndex(df);
            left.set(idx);
        }
        String rightString = token[1].trim();
        DifferentialFunction df = parseDF(rightString);
        int idx = dfBuilder.getPredicateIdProvider().getIndex(df);
        right.set(idx);
    }

    private static final double EPSILON = 1e-9;

    private DifferentialFunction parseDF(String str) {
        Operator op = null;
        double threshold = 0;
        String attr;

        if (str.contains("<=")) {
            op = Operator.LESS_EQUAL;
        } else if (str.contains(">")) {
            op = Operator.GREATER;
        } else {
            throw new IllegalArgumentException("Invalid input string: " + str);
        }

        str = str.substring(1, str.length()-1);
        String[] strs = str.split("<=|>");
        attr = strs[0].substring(0, strs[0].length()-1);
        threshold = Double.parseDouble(strs[1].substring(0,strs[1].length()-1));

        List<DifferentialFunction> dfs = dfBuilder.getPredicates();

        for (DifferentialFunction df : dfs) {
            if (dfMatches(df, attr, op, threshold)) {
                return df;
            }
        }

        throw new IllegalStateException("No matching DifferentialFunction found for input string: " + str);
    }

    private boolean dfMatches(DifferentialFunction df, String attr, Operator op, double threshold) {
        return df.getOperand().getColumn().getColumnName().equals(attr) &&
                df.getOperator().equals(op) &&
                Math.abs(df.getDistance() - threshold) < EPSILON;
    }

}
