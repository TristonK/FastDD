package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import fastdd.differentialdependency.Parser;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tristonK 2023/12/1
 */
public class CalculateDF {
    double[][] dInput;long[][] iInput;String[][] sInput;
    List<List<LongBitSet>> countToDFSets;
    List<ParsedColumn<?>> columns;
    int rows;

    private final double ERR = 0.000000001;

    public CalculateDF(Input input, DifferentialFunctionBuilder dfBuilder){
        countToDFSets = new ArrayList<>();
        for (int i = 0; i < dfBuilder.getColSize(); i++) {
            countToDFSets.add(dfBuilder.getOffset2SatisfiedDFs(i));
        }
        //int -> double -> string
        dInput = input.getDoubleInput();
        iInput = input.getLongInput();
        sInput = input.getStringInput();
        columns = input.getParsedColumns();
        if (dInput.length == 0) {
            if (iInput.length != 0) {
                rows = iInput[0].length;
            } else {
                if (sInput.length == 0) {
                    assert false : "input rows is 0";
                } else {
                    rows = sInput[0].length;
                }
            }
        } else {
            rows = dInput[0].length;
        }
    }

    public LongBitSet getDF(int i, int j){
        LongBitSet dfset = new LongBitSet();
        int cnt = 0;
        for (int k = 0; k < iInput.length; k++) {
            double diff = Math.abs(iInput[k][i] - iInput[k][j]);
            List<Double> th = columns.get(k).getThresholds();
            LongBitSet mask = countToDFSets.get(k).get(findMaskPos(diff, th));
            dfset.or(mask);
        }
        for (int k = 0; k < dInput.length; k++) {
            double diff = Math.abs(dInput[k][i] - dInput[k][j]);
            List<Double> th = columns.get(k + iInput.length).getThresholds();
            LongBitSet mask = countToDFSets.get(k + iInput.length).get(findMaskPos(diff, th));
            dfset.or(mask);
        }
        for (int k = 0; k < sInput.length; k++) {
            double diff = DistanceCalculation.StringDistance(sInput[k][i], sInput[k][j]);
            List<Double> th = columns.get(k + iInput.length + dInput.length).getThresholds();
            LongBitSet mask = countToDFSets.get(k + iInput.length + dInput.length).get(findMaskPos(diff, th));
            dfset.or(mask);
        }
        return dfset;
    }

    private int findMaskPos(double diff, List<Double> th) {
        int c = 0;
        if (diff < th.get(0) + ERR) {
            c = 0;
        } else if (diff > th.get(th.size() - 1) + ERR) {
            c = th.size();
        } else {
            while (c < th.size() - 1) {
                if (diff > th.get(c) + ERR && diff < th.get(c + 1) + ERR) {
                    c++;
                    break;
                }
                c++;
            }
        }
        return c;
    }
}
