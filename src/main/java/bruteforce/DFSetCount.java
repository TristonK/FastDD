package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.*;

/**
 * @author tristonK 2023/2/7
 */
public class DFSetCount {
    private final double ERR = 0.000000001;

    public Set<LongBitSet> calculate(Input input) {
        Set<LongBitSet> DFSet = new HashSet<>();
        //int -> double -> string
        double[][] dInput = input.getDoubleInput();
        long[][] iInput = input.getLongInput();
        String[][] sInput = input.getStringInput();
        List<ParsedColumn<?>> columns = input.getParsedColumns();
        int rows = 0;
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
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet dfset = new LongBitSet(DifferentialFunctionBuilder.getIntervalCnt());
                int cnt = 0;
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][i] - iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    dfset.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                for (int k = 0; k < dInput.length; k++) {
                    double diff = Math.abs(dInput[k][i] - dInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length).getThresholds();

                    dfset.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                for (int k = 0; k < sInput.length; k++) {
                    double diff = DistanceCalculation.StringDistance(sInput[k][i], sInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length + dInput.length).getThresholds();
                    dfset.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                DFSet.add(dfset);
            }
        }
        return DFSet;
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

    boolean TestDup = false;
    LongBitSet DDLeft = null;
    public Set<LongBitSet> calculateDFSet(Input input, DifferentialFunctionBuilder differentialFunctionBuilder) {
        List<List<LongBitSet>> countToDFSets = new ArrayList<>();
        for (int i = 0; i < differentialFunctionBuilder.getColSize(); i++) {
            countToDFSets.add(differentialFunctionBuilder.getOffset2SatisfiedDFs(i));
        }
        Set<LongBitSet> setOfDFSet = new HashSet<>();
        //int -> double -> string
        double[][] dInput = input.getDoubleInput();
        long[][] iInput = input.getLongInput();
        String[][] sInput = input.getStringInput();
        List<ParsedColumn<?>> columns = input.getParsedColumns();
        int rows = 0;
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
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet dfset = new LongBitSet();
                int cnt = 0;
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][i] - iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    LongBitSet mask = countToDFSets.get(k).get(findMaskPos(diff, th));
                    dfset.or(mask);
                }
                ;
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
                setOfDFSet.add(dfset);
                if(TestDup && DDLeft.isSubSetOf(dfset)){
                    System.out.println("dup "+ i + " "+ j);
                }
            }
        }
        return setOfDFSet;
    }
}
